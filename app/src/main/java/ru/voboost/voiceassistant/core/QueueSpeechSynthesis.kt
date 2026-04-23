package ru.voboost.voiceassistant.core

import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException

class QueueSpeechSynthesis(private val speechSynthesis: ISpeechSynthesis) : ISpeechSynthesisCallback {
    companion object {
        const val TAG = "PriorityTtsQueue"
        const val PRIOR_LOW = 1
        const val PRIOR_MEDIUM = 2
        const val PRIOR_HIGH = 3
        const val PRIOR_CRITICAL = 4
    }

    data class SpeechItem(
        val text: String,
        val priority: Int,
        val completion: CompletableDeferred<Unit>? = null
    )

    private val queue = mutableListOf<SpeechItem>()
    @Volatile private var isSpeaking = false
    @Volatile private var currentItem: SpeechItem? = null
    private val queueLock = Any()

    init {
        speechSynthesis.addCallback(this)
    }

    /**
     * Обычный enqueue — не ждёт завершения
     */
    fun enqueue(text: String, priority: Int = PRIOR_LOW) {
        if (text.isBlank()) return
        enqueue(SpeechItem(text, priority, null))
    }

    /**
     * Suspending-версия — ждёт, пока фраза будет полностью озвучена
     */
    suspend fun enqueueAsync(text: String, priority: Int = PRIOR_LOW): Boolean {
        if (text.isBlank()) return true

        val deferred = CompletableDeferred<Unit>()
        enqueue(SpeechItem(text, priority, deferred))

        return try {
            deferred.await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "TTS waiting interrupted", e)
            false
        }
    }
    /**
     * Общая логика добавления в очередь
     */
    private fun enqueue(item: SpeechItem) {
        synchronized(queueLock) {
            // Если очередь пуста и ничего не говорится — запускаем сразу
            if (!isSpeaking && queue.isEmpty()) {
                queue.add(item)
                startNextItem()
                return
            }

            // 🔹 БЕЗ ПРЕРЫВАНИЯ: если что-то уже говорится — просто добавляем в очередь
            // Но сортируем по приоритету среди ожидающих
            insertByPriority(item)

            // Если вдруг освободилось (маловероятно, но на всякий случай)
            if (!isSpeaking) {
                startNextItem()
            }
        }
    }
    /**
     * Вставляет элемент в очередь с учётом приоритета (больше = выше)
     */
    private fun insertByPriority(item: SpeechItem) {
        var inserted = false
        for (i in queue.indices) {
            if (item.priority > queue[i].priority) {
                queue.add(i, item)
                inserted = true
                break
            }
        }
        if (!inserted) {
            queue.add(item)
        }
    }
    /**
     * Запускает следующий элемент из очереди
     */
    private fun startNextItem() {
        synchronized(queueLock) {
            if (queue.isEmpty()) {
                isSpeaking = false
                currentItem = null
                return
            }

            currentItem = queue.removeAt(0)
            isSpeaking = true

            val item = currentItem!!
            Log.d(TAG, "Speaking: ${item.text} (priority: ${item.priority})")
            speechSynthesis.speak(item.text)
            // ← Не вызываем completion здесь! Ждём колбэк от движка
        }
    }
    /**
     * Колбэк от TTS-движка: фраза озвучена полностью
     */
    override fun handleSpeechFinished() {
        synchronized(queueLock) {
            val finishedItem = currentItem

            // ✅ Завершаем ожидающую корутину, если она была
            finishedItem?.completion?.complete(Unit)

            currentItem = null
            if (finishedItem != null) {
                Log.d(TAG, "TTS finished: ${finishedItem.text}")
            }

            // Запускаем следующий элемент, если есть
            if (queue.isNotEmpty()) {
                startNextItem()
            } else {
                isSpeaking = false
            }
        }
    }
    /**
     * Отменяет всю очередь и текущее произношение
     */
    fun cancelAll() {
        synchronized(queueLock) {
            // Завершаем все ожидающие корутины
            queue.forEach { it.completion?.completeExceptionally(CancellationException("Queue cancelled")) }
            currentItem?.completion?.completeExceptionally(CancellationException("Queue cancelled"))

            speechSynthesis.stop()
            queue.clear()
            currentItem = null
            isSpeaking = false
            Log.d(TAG, "TTS Queue cleared")
        }
    }
}
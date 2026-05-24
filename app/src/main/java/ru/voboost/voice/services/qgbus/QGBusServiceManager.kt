package ru.voboost.voice.services.qgbus

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import com.qinggan.bus.QGBusEvent
import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Менеджер для взаимодействия с QGBusService через Messenger-IPC.
 *
 * Использование:
 * 1. Создать экземпляр: val manager = QGBusServiceManager(context)
 * 2. Подключиться: manager.connect()
 * 3. Показать уведомление: manager.showToast(message)
 * 4. Отключиться: manager.disconnect()
 */
class QGBusServiceManager(context: Context) {

    companion object {
        private const val TAG = "QGBusClient"

        // Адрес сервиса (из системного приложения QGBus)
        private const val SERVICE_ACTION = "com.qinggan.QGBus.QGBusService"
        private const val SERVICE_PACKAGE = "com.qinggan.QGBus"

        // Типы сообщений (msg.what) - идентичны QGBusService
        private const val SUBSCRIBE_EVENT = 1
        private const val UNSUBSCRIBE_EVENT = 2
        private const val PUBLISH_EVENT = 3
        private const val NOTIFY_EVENT = 4
        private const val REGISTER_HANDLER = 5
        private const val HEART_BEAT_REQUEST = 6
        private const val HEART_BEAT_RESPONSE = 7

        // Ключи Bundle
        private const val KEY_NAME = "name"
        private const val KEY_EVENT = "event"
        private const val KEY_FILTER = "filter"
    }

    // Состояние подключения
    private var serviceMessenger: Messenger? = null
    private var isOnline = false
    // Обработчики событий: eventType -> callback
    private val eventHandlers = mutableMapOf<String, (QGBusEvent) -> Unit>()
    // Имя компонента (по умолчанию пакет приложения)
    private val componentName: String = context.packageName
    // WeakReference на Context для предотвращения утечек
    private val contextRef = WeakReference(context.applicationContext)
    // CoroutineScope для асинхронных операций
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    // Handler для входящих сообщений от сервиса (работает в главном потоке)
    private val incomingHandler = Handler(Looper.getMainLooper()) { msg ->
        when (msg.what) {
            NOTIFY_EVENT -> { //Критично: событие лежит в message.obj, а не в data!
                val bundle = msg.obj as? Bundle
                // getParcelable() deprecated, но по-прежнему работает. В Android 13+ можно использовать getParcelableCompat()
                bundle?.setClassLoader(QGBusEvent::class.java.classLoader)
                @Suppress("DEPRECATION") val event = bundle?.getParcelable<QGBusEvent>(KEY_EVENT)
                event?.let {
                    eventHandlers[it.mEventType]?.invoke(it)
                }
            }
            HEART_BEAT_REQUEST -> {
                sendHeartBeatResponse()
            }
        }
        true
    }
    // Наш Messenger для отправки в сервис
    private val clientMessenger = Messenger(incomingHandler)

    /**
     * Подключиться к сервису. Вызывайте в onStart()/onCreate().
     */
    fun connect() {
        val context = contextRef.get() ?: return
        val intent = Intent(SERVICE_ACTION).apply { //Обязательно для Android 12+ (API 31+)
            setPackage(SERVICE_PACKAGE)
        }
        try {
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        catch (e: Exception) {
            Log.e(TAG, "Failed to bind service", e)
        }
    }

    // ServiceConnection для bind/unbind
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) =
                this@QGBusServiceManager.handleOnServiceConnected(name, service)

        override fun onServiceDisconnected(name: ComponentName) =
                this@QGBusServiceManager.handleOnServiceDisconnected(name)

        override fun onBindingDied(name: ComponentName?) =
                this@QGBusServiceManager.handleOnBindingDied(name)
    }

    private fun handleOnServiceConnected(name: ComponentName, service: IBinder) {
        Log.d(TAG, "onServiceConnected: $name")
        serviceMessenger = Messenger(service)
        isOnline = true
        registerHandler()
    }

    private fun handleOnServiceDisconnected(name: ComponentName) {
        Log.d(TAG, "onServiceDisconnected: $name")
        serviceMessenger = null
        isOnline = false // Можно добавить логику авто-реконнекта при необходимости
    }

    private fun handleOnBindingDied(name: ComponentName?) {
        Log.w(TAG, "onBindingDied: $name")
        serviceMessenger = null
        isOnline = false
    }

    /**
     * Отключиться от сервиса. Вызывайте в onStop()/onDestroy().
     */
    fun disconnect() {
        val context = contextRef.get() ?: return
        try {
            context.unbindService(connection)
        }
        catch (e: IllegalArgumentException) {
        }
        serviceMessenger = null
        isOnline = false
        eventHandlers.clear()
    }

    /**
     * Подписаться на события.
     * @param eventTypes список имён событий, например ["com.qinggan.account.logined"]
     * @param handler callback, который будет вызван при получении события
     */
    fun subscribe(eventTypes: List<String>, handler: (QGBusEvent) -> Unit) {
        if (eventTypes.isEmpty()) return

        // Сохраняем обработчик для входящих событий
        eventTypes.forEach { eventHandlers[it] = handler }

        if (!isOnline) {
            Log.w(TAG, "Not online, subscription cached locally")
            return
        }

        val msg = Message.obtain().apply {
            what = SUBSCRIBE_EVENT
            data = Bundle().apply {
                putString(KEY_NAME, componentName)
                putStringArrayList(KEY_FILTER, ArrayList(eventTypes))
            }
        }
        safeSend(msg)
    }

    /**
     * Отписаться от событий.
     */
    fun unsubscribe(eventTypes: List<String>) {
        if (eventTypes.isEmpty() || !isOnline) return

        val msg = Message.obtain().apply {
            what = UNSUBSCRIBE_EVENT
            data = Bundle().apply {
                putString(KEY_NAME, componentName)
                putStringArrayList(KEY_FILTER, ArrayList(eventTypes))
            }
        }
        safeSend(msg)

        eventTypes.forEach { eventHandlers.remove(it) }
    }

    /**
     * Опубликовать событие в шину.
     */
    fun publish(event: QGBusEvent) {
        if (!isOnline) {
            Log.w(TAG, "Not online, event not sent")
            return
        }

        val bundle = Bundle().apply {
            setClassLoader(QGBusEvent::class.java.classLoader)
            putParcelable(KEY_EVENT, event)
        }
        val msg = Message.obtain().apply {
            what = PUBLISH_EVENT
            obj = bundle  // <-- Не data, а obj!
        }
        safeSend(msg)
    }

    /**
     * Асинхронная публикация события с подтверждением доставки (опционально).
     * Возвращает true, если сообщение успешно отправлено (не гарантирует обработку сервисом).
     */
    suspend fun publishAsync(event: QGBusEvent): Boolean = suspendCoroutine { cont ->
        if (!isOnline) {
            cont.resume(false)
            return@suspendCoroutine
        }

        val bundle = Bundle().apply {
            setClassLoader(QGBusEvent::class.java.classLoader)
            putParcelable(KEY_EVENT, event)
        }
        val msg = Message.obtain().apply {
            what = PUBLISH_EVENT
            obj = bundle
        }

        try {
            serviceMessenger?.send(msg)
            cont.resume(true)
        }
        catch (e: RemoteException) {
            Log.e(TAG, "Failed to send event", e)
            cont.resume(false)
        }
    }

    /**
     * Проверка: подключён ли клиент к сервису.
     */
    fun isConnected(): Boolean = isOnline && serviceMessenger != null

    /**
     * Регистрация нашего Messenger в сервисе.
     */
    private fun registerHandler() {
        val msg = Message.obtain().apply {
            what = REGISTER_HANDLER
            replyTo = clientMessenger  // Сервис сохранит этот Messenger для обратных вызовов
            data = Bundle().apply {
                putString(KEY_NAME, componentName)
            }
        }
        safeSend(msg)
    }

    /**
     * Ответ на heartbeat-запрос от сервиса.
     */
    private fun sendHeartBeatResponse() {
        val msg = Message.obtain().apply {
            what = HEART_BEAT_RESPONSE
        }
        safeSend(msg)
    }

    /**
     * Безопасная отправка сообщения с обработкой RemoteException.
     */
    private fun safeSend(msg: Message) {
        try {
            serviceMessenger?.send(msg)
        }
        catch (e: RemoteException) {
            Log.e(TAG,
                  "Failed to send message: ${msg.what}",
                  e) // Можно добавить логику реконнекта здесь
            isOnline = false
            serviceMessenger = null
        }
        catch (e: Exception) {
            Log.e(TAG, "Unexpected error sending message", e)
        }
    }

    /**
     * Очистка ресурсов при уничтожении.
     */
    fun destroy() {
        disconnect()
        scope.cancel()
        contextRef.clear()
    }
}

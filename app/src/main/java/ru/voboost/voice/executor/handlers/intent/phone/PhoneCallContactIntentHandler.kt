package ru.voboost.voice.executor.handlers.intent.phone

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import ru.voboost.voice.executor.CommandData
import ru.voboost.voice.executor.handlers.intent.AbstractIntentHandler

/**
 * Звонок контакту через Broadcast Intent
 */
class PhoneCallContactIntentHandler(context: Context)
    : AbstractIntentHandler(context) {

    companion object {
        private const val TAG = "PhoneCallContactCmd"
        const val CALL_URI = "content://com.qinggan.bluetoothphone/contactsinfo"
        const val CALL_PARAM_NAME = "name"
        const val CALL_PARAM_NUMBER = "number"

        // Ключи параметров, которые уйдут в CommandResult для системы TTS
        const val PARAM_CONTACT = "contact"
        const val PARAM_NUMBER = "number"

        // Список командных фраз нейросети, удаляемых при расчете совпадений
        private val STOP_WORDS = setOf("позвони",
                                       "вызови",
                                       "набери",
                                       "звонок",
                                       "соедини",
                                       "номер",
                                       "телефону",
                                       "связаться",
                                       "дозвонись",
                                       "поговорить",
                                       "сделай",
                                       "вызов",
                                       "пожалуйста",
                                       "сейчас",
                                       "хочу",
                                       "с",
                                       "до",
                                       "по",
                                       "о")
    }

    // Вспомогательная структура данных
    private data class FoundContact(val originalName: String, val number: String)

    /**
     * Переопределяем парсинг параметров.
     * Извлекаем имя и телефон из телефонной книги по фразе Vosk.
     */
    override fun parsParams(commandData: CommandData): Map<String, String> { // Берем базовую карту параметров (включая "_zone")
        val params = super.parsParams(commandData).toMutableMap()

        val bluetoothMac = getBluetoothMac()
        if (bluetoothMac == null) {
            Log.e(TAG, "Cannot get Bluetooth MAC address")
            return params
        }

        // Ищем контакт по всей сырой фразе от Vosk
        val matchedContact = findContactInPhrase(commandData.phrase, bluetoothMac)

        if (matchedContact != null) { // Кладем оригинальное имя (например, "Жена") и номер в параметры.
            // Они автоматически попадут в CommandResult для TTS
            params[PARAM_CONTACT] = matchedContact.originalName
            params[PARAM_NUMBER] = matchedContact.number
            Log.i(TAG, "Matched: '${matchedContact.originalName}' -> ${matchedContact.number}")
        }

        return params
    }

    /**
     * Собирает Intent для BluetoothPhone.
     * Вызывается базовым классом автоматически сразу после parsParams.
     */
    override fun buildIntent(voiceParams: Map<String, Any>): Intent? {
        val phoneNumber = voiceParams[PARAM_NUMBER] as? String

        if (phoneNumber.isNullOrEmpty()) {
            Log.w(TAG, "Phone number is missing in voiceParams, canceling broadcast")
            return null // Базовый класс вернет ICommandHandler.NEGATIVE_RESULT
        }

        return Intent(ACTION_IVOKA_PHONE_CALL).apply {
            putExtra(EXTRA_IVOKA_CALL_INFO, phoneNumber)
            putExtra(EXTRA_SCREEN_INT, 0)
            putExtra(EXTRA_MAC, "")
        }
    }

    /**
     * Сканирует контакты и сопоставляет имена с фразой Vosk по корням слов
     */
    private fun findContactInPhrase(phrase: String, bluetoothMac: String): FoundContact? {
        val cleanPhrase = phrase.lowercase().trim()
        val uri = "$CALL_URI/$bluetoothMac".toUri()
        val projection = arrayOf(CALL_PARAM_NAME, CALL_PARAM_NUMBER)

        return try {
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
                         ?: return null.also { Log.e(TAG, "ContentResolver returned null") }

            val nameIdx = cursor.getColumnIndex(CALL_PARAM_NAME)
            val numberIdx = cursor.getColumnIndex(CALL_PARAM_NUMBER)

            var bestContact: FoundContact? = null
            var maxMatchScore = 0

            if (nameIdx >= 0 && numberIdx >= 0 && cursor.count > 0) {
                cursor.moveToFirst()
                do {
                    val originalName = cursor.getString(nameIdx) ?: ""
                    val bookName = originalName.lowercase().trim()
                    val number = cursor.getString(numberIdx) ?: ""

                    if (bookName.isEmpty() || number.isEmpty()) continue

                    val score = calculateMatchScore(bookName, cleanPhrase)

                    if (score > maxMatchScore) {
                        maxMatchScore = score
                        bestContact = FoundContact(originalName, number)
                    }
                } while (cursor.moveToNext())
            }
            cursor.close()

            if (maxMatchScore > 0) bestContact else null
        }
        catch (e: Exception) {
            Log.e(TAG, "Exception querying contacts", e)
            null
        }
    }

    private fun calculateMatchScore(bookName: String, phrase: String): Int {
        val bookWords = bookName.split(Regex("\\s+")).filter { it !in STOP_WORDS }
        val phraseWords = phrase.split(Regex("\\s+")).filter { it !in STOP_WORDS }

        var totalScore = 0

        for (bWord in bookWords) {
            val bStem = getStem(bWord)

            val hasMatch = phraseWords.any { pWord ->
                val pStem = getStem(pWord)
                bStem == pStem || pWord.contains(bStem) || bWord.contains(pStem)
            }

            if (hasMatch) {
                totalScore += bStem.length
            }
            else {
                return 0 // Защита от частичного совпадения ("Иван Сидоров" не сработает на фразу "Позвони Ивану")
            }
        }
        return totalScore
    }

    private fun getStem(word: String): String {
        if (word.length <= 3) return word
        val endings = Regex("(а|е|и|о|у|ы|я|ем|ам|ов|ами|ями|ях|ею|ою|ий|ая|ое|на|не|ну|ей|у|ю)$")
        val stemmed = word.replace(endings, "")
        return if (stemmed.length >= 2) stemmed else word
    }

    private fun getBluetoothMac(): String? {
        return try {
            val clazz = Class.forName("android.os.SystemProperties")
            val method = clazz.getMethod("get", String::class.java, String::class.java)
            val mac = method.invoke(null, "qinggan.bluetooth.mac", "") as String
            if (mac.isNullOrEmpty()) null else mac
        }
        catch (e: Exception) {
            Log.e(TAG, "Failed to get Bluetooth MAC", e)
            null
        }
    }
}

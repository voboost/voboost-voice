package ru.voboost.voice.executor.handlers.intent.phone

import android.content.Context
import android.net.Uri
import android.util.Log
import ru.voboost.voice.executor.handlers.intent.AbstractIntentHandler
import androidx.core.net.toUri

/**
 * Звонок контакту через Broadcast Intent
 *
 * Находит номер контакта по имени через Android Contacts ContentResolver,
 * затем отправляет broadcast на action "com.qinggan.broadcast.action.ivokaphonecall"
 * с параметрами, которые ожидает BluetoothPhone:
 *   - Ivoka_CallInfo: номер телефона (String)
 *   - screen_int: текущий экран (int, по умолчанию 0)
 *   - mac: MAC-адрес Bluetooth (пустая строка)
 */
class PhoneCallContactIntentHandler(context: Context) : AbstractIntentHandler(context) {

    companion object {
        const val CALL_URI = "content://com.qinggan.bluetoothphone/contactsinfo"
        const val CALL_PARAM_NAME = "name"
        const val CALL_PARAM_NUMBER = "number"
    }


    override fun buildIntent(voiceParams: Map<String, Any>): android.content.Intent? {
        val contactName = voiceParams["contact"] as? String ?: ""

        // Ищем номер телефона по имени контакта
        val phoneNumber = findPhoneNumberByName(contactName)

        Log.d(TAG, "Phone call to contact: '$contactName' -> number: '$phoneNumber'")
        Log.d(TAG, "  Action: $ACTION_IVOKA_PHONE_CALL")
        Log.d(TAG, "  Extra Ivoka_CallInfo: '$phoneNumber'")

        if (phoneNumber.isNullOrEmpty()) {
            return null;
        }

        return android.content.Intent(ACTION_IVOKA_PHONE_CALL)
            .apply { // Если номер найден - отправляем его, иначе имя контакта (BluetoothPhone попытается набрать)
                putExtra(EXTRA_IVOKA_CALL_INFO, phoneNumber)
                putExtra(EXTRA_SCREEN_INT, 0)
                putExtra(EXTRA_MAC, "")
            }
    }

    /**
     * Найти номер телефона по имени через BluetoothPhone ContentProvider
     * URI: content://com.qinggan.bluetoothphone/contactsinfo/{MAC}
     * Projection: ["name", "number"] (обязательно!)
     */
    private fun findPhoneNumberByName(contactName: String): String? {
        val bluetoothMac =
            getBluetoothMac() ?: return null.also { Log.e(TAG, "Cannot get Bluetooth MAC address") }

        val uri = "$CALL_URI/$bluetoothMac".toUri()
        val projection = arrayOf(CALL_PARAM_NAME, CALL_PARAM_NUMBER)

        return try {
            Log.d(TAG, "Querying BluetoothPhone contacts: uri=$uri")
            Log.d(TAG, "Looking for contact: '$contactName'")

            val cursor = context.contentResolver.query(uri, projection, null, null, null)

            if (cursor == null) {
                Log.e(TAG, "ContentResolver.query returned null")
                return null
            }

            Log.d(TAG, "Cursor returned: count=${cursor.count}")

            val nameIdx = cursor.getColumnIndex(CALL_PARAM_NAME)
            val numberIdx = cursor.getColumnIndex(CALL_PARAM_NUMBER)

            if (nameIdx >= 0 && numberIdx >= 0 && cursor.count > 0) {
                cursor.moveToFirst()
                do {
                    val name = cursor.getString(nameIdx)
                    val number = cursor.getString(numberIdx)
                    Log.d(TAG, "  Contact: '$name' -> $number")

                    if (name.equals(contactName, ignoreCase = true)) {
                        Log.i(TAG, "? Match found: '$contactName' -> $number")
                        return number
                    }
                } while (cursor.moveToNext())
            }

            cursor.close()
            Log.w(TAG, "Contact '$contactName' not found in BluetoothPhone provider")
            null
        }
        catch (e: Exception) {
            Log.e(TAG, "Exception querying contacts for '$contactName'", e)
            null
        }
    }

    /**
     * Получить MAC адрес Bluetooth устройства через SystemProperties
     * Возвращает пустую строку если не найден
     */
    private fun getBluetoothMac(): String? {
        return try {
            val clazz = Class.forName("android.os.SystemProperties")
            val method = clazz.getMethod("get", String::class.java, String::class.java)
            val mac = method.invoke(null, "qinggan.bluetooth.mac", "") as String
            if (mac.isNullOrEmpty()) null else mac
        }
        catch (e: Exception) {
            Log.e(TAG, "Failed to get Bluetooth MAC via SystemProperties", e)
            null
        }
    }
}



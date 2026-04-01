package com.voboost.voiceassistant.executor

import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Выполнение команд через Broadcast Intent
 * Отправляет Intent в системный сервис (BluetoothPhone/VuiServiceMgr)
 *
 * Основано на декомпилированном коде Ivoka/QGSpeechService
 */
class IntentVehicleCommandExecutor(
    private val context: Context
) : VehicleCommandExecutor {

    companion object {
        private const val TAG = "IntentVehicleCommand"

        // Intent actions из Ivoka
        private const val ACTION_TELEPHONE_CALL = "pateo.dls.ivoka.telephone.CALL"
        private const val ACTION_VEHICLE_CONTROL = "pateo.dls.ivoka.vehicle.CONTROL"

        // Параметры для телефона (из VoiceParam.java)
        private const val VOICE_PARAM_TELEPHONE_NAME = "voice.param.telephone.name"
        private const val VOICE_PARAM_TELEPHONE_NUMBER = "voice.param.telephone.number"
        private const val VOICE_PARAM_TELEPHONE_CMD = "voice.param.telephone.cmd"
        private const val VOICE_PARAM_TELEPHONE_LOCATION = "voice.param.telephone.location"
        private const val VOICE_PARAM_TELEPHONE_OPERATOR = "voice.param.telephone.operator"
        private const val VOICE_PARAM_TELEPHONE_PART = "voice.param.telephone.part"
        private const val VOICE_PARAM_TELEPHONE_CONFIRM_LIST = "voice.param.telephone.confirm_list"

        // Параметры для автомобиля
        private const val VOICE_PARAM_VEHICLE_TARGET = "voice.param.vehicle.target"
        private const val VOICE_PARAM_VEHICLE_CLASSIFY = "voice.param.vehicle.classify"
        private const val VOICE_PARAM_VEHICLE_COMMAND = "voice.param.vehicle.command"
    }

    override val executionMethod: String = "Broadcast Intent"

    override fun execute(
        target: String,
        classify: Int,
        command: Int,
        params: Map<String, Any>
    ): Boolean {
        Log.d(TAG, "Sending vehicle intent: target=$target, classify=$classify, command=$command")

        try {
            val intent = Intent(ACTION_VEHICLE_CONTROL)

            // Основные параметры (как в Ivoka)
            intent.putExtra(VOICE_PARAM_VEHICLE_TARGET, target)
            intent.putExtra(VOICE_PARAM_VEHICLE_CLASSIFY, classify)
            intent.putExtra(VOICE_PARAM_VEHICLE_COMMAND, command)

            // Добавляем дополнительные параметры
            for ((key, value) in params) {
                when (value) {
                    is Int -> intent.putExtra("voice.param.vehicle.$key", value)
                    is Double -> intent.putExtra("voice.param.vehicle.$key", value)
                    is String -> intent.putExtra("voice.param.vehicle.$key", value)
                    is Boolean -> intent.putExtra("voice.param.vehicle.$key", value)
                }
            }

            context.sendBroadcast(intent)
            Log.i(TAG, "Vehicle intent sent successfully")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "Failed to send vehicle intent", e)
            return false
        }
    }

    /**
     * Выполнение команды звонка
     * Основано на IntentFactory.createTelephoneCallIntent из QGSpeechService
     *
     * @param classify класс команды (1 = звонок)
     * @param command команда (1 = позвонить)
     * @param contact имя контакта или номер
     * @param number номер телефона (если задан напрямую)
     * @param callType тип вызова ("contact" или "number")
     */
    override fun executePhoneCommand(
        classify: Int,
        command: Int,
        contact: String?,
        number: String?,
        callType: String
    ): Boolean {
        Log.d(TAG, "Sending phone intent: classify=$classify, command=$command, contact=$contact, number=$number, callType=$callType")

        try {
            // Используем правильный ACTION из Ivoka
            val intent = Intent(ACTION_TELEPHONE_CALL)

            // Добавляем параметры как в оригинальном Ivoka
            if (callType == "contact" && contact != null) {
                // Звонок по контакту
                intent.putExtra(VOICE_PARAM_TELEPHONE_NAME, contact)
                Log.d(TAG, "Phone call to contact: $contact")
            } else if (callType == "number" && number != null) {
                // Звонок по номеру
                intent.putExtra(VOICE_PARAM_TELEPHONE_NUMBER, number)
                Log.d(TAG, "Phone call to number: $number")
            }

            // Дополнительные параметры (опционально)
            intent.putExtra(VOICE_PARAM_TELEPHONE_CMD, command.toString())
            intent.putExtra(VOICE_PARAM_TELEPHONE_LOCATION, "local")

            context.sendBroadcast(intent)
            Log.i(TAG, "Phone intent sent successfully")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "Failed to send phone intent", e)
            return false
        }
    }
}

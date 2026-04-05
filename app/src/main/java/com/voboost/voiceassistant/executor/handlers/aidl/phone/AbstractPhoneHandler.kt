package com.voboost.voiceassistant.executor.handlers.aidl.phone

import android.util.Log
import com.qinggan.canbus.PhoneInfo
import com.qinggan.canbus.PhoneState
import com.voboost.voiceassistant.canbus.CanBusServiceManager
import com.voboost.voiceassistant.config.ActionConfig
import com.voboost.voiceassistant.executor.handlers.ICommandHandler

/**
 * Базовый абстрактный обработчик телефонных команд (через CAN-шину)
 *
 * Конкретные подклассы определяют откуда брать данные для звонка
 *
 * @param commandId Уникальный ID команды (совпадает с config.json)
 * @param canBusManager Менеджер CAN-шины
 */
abstract class AbstractPhoneHandler(
    override val commandId: String,
    protected val canBusManager: CanBusServiceManager
) : ICommandHandler {

    override fun execute(
        config: ActionConfig,
        voiceParams: Map<String, Any>
    ): Boolean {
        if (!canBusManager.isConnected()) {
            Log.w(TAG, "Not connected to CanBusService")
            return false
        }

        val phoneInfo = buildPhoneInfo(voiceParams)
            ?: return false.also { Log.w(TAG, "Phone command '$commandId': missing required parameters") }

        Log.d(TAG, "Phone command: id='$commandId', phoneInfo=$phoneInfo")
        return canBusManager.updatePhoneInfo(phoneInfo)
    }

    /**
     * Строит PhoneInfo из параметров голоса
     */
    protected abstract fun buildPhoneInfo(voiceParams: Map<String, Any>): PhoneInfo?

    /**
     * Создать PhoneInfo для исходящего звонка
     */
    protected fun createPhoneInfo(
        name: String = "",
        phoneNum: String = ""
    ): PhoneInfo {
        return PhoneInfo().apply {
            this.name = name
            this.phoneNum = phoneNum
            this.phoneState = PhoneState.GOING_CALL
            this.duration = 0
            this.connectedStatus = 1
            this.isVehicleCall = 1
        }
    }

    companion object {
        const val TAG = "PhoneCommand"
    }
}

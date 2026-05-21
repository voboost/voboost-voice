package ru.voboost.voice.canbus.handlers

import android.util.Log
import com.qinggan.canbus.CanBusListener
import com.qinggan.canbus.VehicleState
import ru.voboost.voice.canbus.CanBusServiceManager
import ru.voboost.voice.canbus.ICanBusServiceConnectionCallback
import ru.voboost.voice.core.QueueSpeechSynthesis


/**
 * РћР±СЂР°Р±РѕС‚С‡РёРє РїСЂРµРґСѓРїСЂРµР¶РґРµРЅРёР№ TSR (Traffic Sign Recognition)
 *
 * РЎР»СѓС€Р°РµС‚ СЃРѕР±С‹С‚РёСЏ РѕС‚ РєР°РјРµСЂС‹, СЃС‡РёС‚С‹РІР°СЋС‰РµР№ РґРѕСЂРѕР¶РЅС‹Рµ Р·РЅР°РєРё
 * Рё РїСЂРµРґСѓРїСЂРµР¶РґР°РµС‚ Рѕ РїСЂРµРІС‹С€РµРЅРёРё СЃРєРѕСЂРѕСЃС‚Рё
 *
 * @param canBusManager РњРµРЅРµРґР¶РµСЂ CanBusService РґР»СЏ СЂРµРіРёСЃС‚СЂР°С†РёРё callback
 * @param ttsCallback Callback РґР»СЏ РІРѕСЃРїСЂРѕРёР·РІРµРґРµРЅРёСЏ TTS РїСЂРµРґСѓРїСЂРµР¶РґРµРЅРёР№
 */
class TSRSpeedLimitHandler(private val queueSpeech: QueueSpeechSynthesis) :
        ICanBusServiceConnectionCallback {
    private var canBusManager: CanBusServiceManager? = null
    private var currentSpeed = 0
    private var isaWarningEnabled = true
    private var isCallbackRegistered = false

    companion object {
        const val TAG = "TSRSpeedLimitHandler"
    }

    private val mCanBusListener = object : CanBusListener() {

        override fun onVehicleStateChanged(vehicle: VehicleState, state: Int) {
            this@TSRSpeedLimitHandler.handleVehicleStateChanged(vehicle, state)
        }

        override fun onVehicleSpeedChanged(speed: Int) {
            this@TSRSpeedLimitHandler.handleVehicleSpeedChanged(speed)
        }
    }

    private fun handleVehicleStateChanged(vehicle: VehicleState, state: Int) {
           when (vehicle) {
            VehicleState.ISA_ISLC_STATUS -> { // РџСЂРёРЅСЏР»Рё СЃС‚Р°С‚СѓСЃ вЂ” С‚РµРїРµСЂСЊ Р·Р°РїСЂРѕСЃРёРј РґРµС‚Р°Р»Рё
                if (state == 7) {
                    if (isaWarningEnabled) { // Р¤Р°РєС‚ РїСЂРµРІС‹С€РµРЅРёСЏ СЃРєРѕСЂРѕСЃС‚Рё
                        queueSpeech.enqueue("РџСЂРµРІС‹С€РµРЅРёРµ СЃРєРѕСЂРѕСЃС‚Рё",
                                            QueueSpeechSynthesis.Companion.PRIOR_CRITICAL)
                    }
                }
            }
            // РџРµСЂРµРєР»СЋС‡Р°С‚РµР»СЊ РїСЂРµРґСѓРїСЂРµР¶РґРµРЅРёСЏ ISA (Intelligent Speed Assistance)
            VehicleState.ISA_ISLC_OVER_SPEED_WARNING_SWITCH -> {
                Log.d(TAG, "рџ”§ ISA_OVER_SPEED_WARNING_SWITCH: $state")
                isaWarningEnabled = (state == 2)
            }
            else -> {}
        }
    }

    private fun handleVehicleSpeedChanged(speed: Int) { // Log.w(TAG, "вљ пёЏ Speed: ${speed}")
        currentSpeed = speed // Log.d(TAG, "рџљ— РЎРєРѕСЂРѕСЃС‚СЊ: $speed РєРј/С‡")
    }

    override fun handlerConnected(canBusServiceManager: CanBusServiceManager) {
        canBusManager = canBusServiceManager
        register()
        Log.d(TAG, "register")
    }

    override fun handlerDisconnected(canBusServiceManager: CanBusServiceManager) {
        unregister()
        canBusManager = null
        Log.d(TAG, "unregistered")
    }

    override fun handlerConnectionFailed(canBusServiceManager: CanBusServiceManager,
                                         error: String) {
    }

    /**
     * Р—Р°СЂРµРіРёСЃС‚СЂРёСЂРѕРІР°С‚СЊ callback
     */
    private fun register(): Boolean {
        if (isCallbackRegistered) return true
        val success = canBusManager?.registerCallback(mCanBusListener)
        if (success == true) {
            isCallbackRegistered = true
            val isaWarningState =
                canBusManager?.getVehicleState(VehicleState.ISA_ISLC_OVER_SPEED_WARNING_SWITCH)
            isaWarningEnabled = (isaWarningState == 2)
            Log.d(TAG, "рџ”§ start get ISA_OVER_SPEED_WARNING_SWITCH: $isaWarningState")
        }
        return success == true
    }

    /**
     * РћС‚РїРёСЃР°С‚СЊСЃСЏ РѕС‚ callback
     */
    private fun unregister(): Boolean {
        if (!isCallbackRegistered) return true
        val success = canBusManager?.unregisterCallback(mCanBusListener)
        if (success == true) {
            isCallbackRegistered = false
            Log.i(TAG, "TSR handler unregistered")
        }
        return success == true
    }

    fun release() {
        unregister()
        canBusManager = null
    }

    /**
     * РџРѕР»СѓС‡РёС‚СЊ С‚РµРєСѓС‰СѓСЋ СЃРєРѕСЂРѕСЃС‚СЊ
     */
    fun getCurrentSpeed(): Int = currentSpeed

    /**
     * РџСЂРѕРІРµСЂРёС‚СЊ Р°РєС‚РёРІРЅРѕ Р»Рё РїСЂРµРґСѓРїСЂРµР¶РґРµРЅРёРµ ISA
     */
    fun isISAWarningEnabled(): Boolean = isaWarningEnabled
}


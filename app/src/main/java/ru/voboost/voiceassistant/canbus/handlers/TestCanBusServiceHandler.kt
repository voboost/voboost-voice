package ru.voboost.voiceassistant.canbus.handlers

import android.os.Bundle
import android.util.Log
import com.qinggan.canbus.CanBusListener
import com.qinggan.canbus.RealityWarningInfo
import com.qinggan.canbus.VehicleState
import ru.voboost.voiceassistant.canbus.CanBusServiceManager
import ru.voboost.voiceassistant.canbus.ICanBusServiceConnectionCallback
import ru.voboost.voiceassistant.core.QueueSpeechSynthesis

class TestCanBusServiceHandler(private val queueSpeech: QueueSpeechSynthesis) :
        ICanBusServiceConnectionCallback {
    private var canBusManager: CanBusServiceManager? = null
    private var isCallbackRegistered = false

    companion object {
        const val TAG = "TestSpeedLimitHandler"
    }

    private val mCanBusListener = object : CanBusListener() {

        override fun onVehicleStateChanged(vehicle: VehicleState, state: Int) {
            this@TestCanBusServiceHandler.handleVehicleStateChanged(vehicle, state)
        }

        override fun onVehicleSceneModeChanged(mode: Int) {
            this@TestCanBusServiceHandler.handleVehicleSceneModeChanged(mode)
        }

        override fun onAlarmDataChanged(state: Int) {
            this@TestCanBusServiceHandler.handleAlarmDataChanged(state)
        }

        override fun onRealityWarningInfoChange(realityWarningInfo: RealityWarningInfo) {
            this@TestCanBusServiceHandler.handleRealityWarningInfoChange(realityWarningInfo)
        }

        @Override // com.qinggan.canbus.ICanBusServiceCallback
        override fun onRealityWarningInfoChanged(key: Int, value: Int) {
            this@TestCanBusServiceHandler.handleRealityWarningInfoChanged(key, value)
        }

//        override fun onCanRawDataChanged(canID: Int, data: Bundle) {
//            this@TestCanBusServiceHandler.handleCanRawDataChanged(canID, data)
//        }

        override fun onHEVSystemModelChanged(hevMode: Int) {
            this@TestCanBusServiceHandler.handleHEVSystemModelChanged(hevMode)
        }
    }

    private fun handleVehicleStateChanged(vehicle: VehicleState, state: Int) {
        if (vehicle == VehicleState.LEFT_HORIZONTAL_POSITION) return
        if (vehicle == VehicleState.RIGHT_HORIZONTAL_POSITION) return
        if (vehicle == VehicleState.ASC_LF_HEIGHT) return
        if (vehicle == VehicleState.ASC_LR_HEIGHT) return
        if (vehicle == VehicleState.ASC_RR_HEIGHT) return
        if (vehicle == VehicleState.ASC_RF_HEIGHT) return
        if (vehicle == VehicleState.GW_INFO_RMR) return
        if (vehicle == VehicleState.GW_INFO_AWB) return
        if (vehicle == VehicleState.AVG_FUEL) return
        if (vehicle == VehicleState.AVG_POWER) return
        if (vehicle == VehicleState.AVG_SPEED) return
        if (vehicle == VehicleState.ODO_THISTIME) return
        if (vehicle == VehicleState.TIME_THISTIME) return
        if (vehicle == VehicleState.OBC_CHARGE_CURRENT) return
        if (vehicle == VehicleState.OBC_CHARGE_VOLTAGE) return
        if (vehicle == VehicleState.VOICE_ACC_SPEED) return
        if (vehicle == VehicleState.BRAKE_PEDAL_STATUS) return
        if (vehicle == VehicleState.ACCEL_PEDAL_POSITION) return
        if (vehicle == VehicleState.RCW_STATE) return
        if (vehicle == VehicleState.LCDA_STATE) return
        if (vehicle == VehicleState.DMS_STATUS) return
        if (vehicle == VehicleState.DRIVE_SEAT_SLIDE_POSITION) return
        if (vehicle == VehicleState.ELK_SWITCH) return
        if (vehicle == VehicleState.ESA_SWITCH) return
        if (vehicle == VehicleState.MPC_LASSTS) return
        if (vehicle == VehicleState.AEBSwitch) return
        if (vehicle == VehicleState.FCWSwitch) return
        if (vehicle == VehicleState.RIGHT_VERTICAL_POSITION) return
        if (vehicle == VehicleState.BMS_REMAIN_CHARGE_TIME) return

        Log.d(TAG, "⚠️ VehicleStateChanged: $vehicle val: $state")
    }

    private fun handleVehicleSceneModeChanged(mode: Int) {
        Log.d(TAG, "⚠️ VehicleSceneModeChanged: $mode")
    }

    private fun handleAlarmDataChanged(state: Int) {
        Log.d(TAG, "⚠️ AlarmDataChanged: $state")
    }

    private fun handleRealityWarningInfoChange(realityWarningInfo: RealityWarningInfo) {
        Log.d(TAG, "⚠️ RealityWarningInfo: $realityWarningInfo")
    }

    private fun handleRealityWarningInfoChanged(key: Int, value: Int) {
        Log.d(TAG, "⚠️ RealityWarningInfoChanged: $key -> $value")
    }

    private val lastPayloads = mutableMapOf<Int, IntArray>()

    private fun handleCanRawDataChanged(canID: Int, data: Bundle) {

        val payload = data.getIntArray(canID.toString())
        if (payload == null) return

        val prev = lastPayloads[canID]
        if (prev != null && prev.contentEquals(payload)) return

        lastPayloads[canID] = payload.clone()

        val hexPayload = payload.joinToString(" ") { "%02X".format(it and 0xFF) }

        Log.d(TAG, "⚠️ CanRawDataChanged: $canID -> $hexPayload")
    }

    private fun handleHEVSystemModelChanged(hevMode: Int) {
        Log.d(TAG, "⚠️ HEVSystemModelChanged: $hevMode")
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

    private fun register(): Boolean {
        if (isCallbackRegistered) return true
        val success = canBusManager?.registerCallback(mCanBusListener)
        if (success == true) {
            isCallbackRegistered = true
        }
        return success == true
    }

    private fun unregister(): Boolean {
        if (!isCallbackRegistered) return true
        val success = canBusManager?.unregisterCallback(mCanBusListener)
        if (success == true) {
            isCallbackRegistered = false
            Log.i(TAG, "TSR handler unregistered")
        }
        return success == true
    }
}
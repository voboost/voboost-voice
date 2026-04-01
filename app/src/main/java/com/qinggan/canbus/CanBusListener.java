package com.qinggan.canbus;

import android.os.Bundle;
import android.os.RemoteException;

/* loaded from: classes.dex */
public class CanBusListener extends ICanBusServiceCallback.Stub {
    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onDoorStatusChanged(DoorStatus doorStatus) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    @Deprecated
    public void onCanBoxVersionChange(String ver) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onSeatBeltChanged(SeatBelt state) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onAirConditionChanged(AirCondition airConditionData) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onDVRStateChenaged(DVRState DVRState) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onRadarDataChanged(RadarData radarDate) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onParkStateChanged(int soundState, int systemState) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onReTrackingChanged(int angle) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onHandBrakeStatusChanged(int status) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onLightStatusChanged(LightStatus lightStatus) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onEngineFluidStatusChanged(int values) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onGearStatusChanged(GearState gear) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onEngineSpeedChanged(int speed) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onAmbientTemperatureChanged(int temperature) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    @Deprecated
    public void onWheelSpeedChanged(WheelSpeed speed) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onVehicleSpeedChanged(int speed) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onFuelLevelChanged(FuelLevel level) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    @Deprecated
    public void onWheelCountChanged(WheelCount count) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onEngineTemperatureChanged(int temperature) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onBrakePadStatusChanged(BrakePadStatus status) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onBrakeFluidStatusChanged(int status) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onWiperFluidStatusChanged(int status) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onFuelConsumptionChanged(int consumption) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onEngineStatusChanged(int status) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onOdometerChanged(Odometer odometer) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onBatteryStateChanged(BatteryState state) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onCarKeyChanged(int keycode, int keyStatus) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onIlluminationChanged(int illumination) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onVehicleIOChanged(VehicleIO state) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onSecondaryOdometerChanged(int odometer) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onVehicleKeyStateChanged(int state) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onWindowsStatusChanged(WindowStatus state) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    @Deprecated
    public void onAlarmDataChanged(int state) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onSWCAngleChanged(SWCAngle swcAngle) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onHEVSystemModelChanged(int hevMode) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onVehicleStateChanged(VehicleState vehicle, int state) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    @Deprecated
    public void onVehicleCenterControlEnabledChanged(VehicleCenterControlEnabled vehicle, int enabled) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onTravellingInfo(TravellingInfoType type, float data) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onAccStateChanged(int state) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    @Deprecated
    public void onVehicleStateSettingResponse(VehicleState vehicle, boolean isSuccess) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onFactoryModeChanged(String sResult) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onRealityWarningInfoChange(RealityWarningInfo realityWarningInfo) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onRealityWarningInfoChanged(int key, int value) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onVehicleStateSettingResponseEx(VehicleState vehicle, int state) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onTPMSInfoChange(TPMSInfo tpmsInfo) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    @Deprecated
    public void onBatteryRemainingCapacityChanged(float data) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onPowerLevelChanged(PowerLevel powerLevel) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onInstantaneousFuelConsumptionChanged(float consumption) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onRainFallLevelChanged(int rainfallLevel) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onVehicleSceneModeChanged(int mode) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onCanRawDataChanged(int canID, Bundle data) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onICMReqMediaChanged(int flag, int action, int targetType) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onICMReqDialing(int serialNumber) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onICMReqMuteModeChanged(boolean isMute) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onICMReqCallModeChanged(boolean isVehicleCall) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onICMReqCallStatusChanged(int status) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onCameraChanged(CameraState cameraState) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onSeatAdjustStateChanged(SeatAdjustState state) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onEnergyConsumptionPercentChanged(EnergyConsumptionPercent consumptionPercent) throws RemoteException {
    }

    @Override // com.qinggan.canbus.ICanBusServiceCallback
    public void onEnergyConsumptionInfoChanged(EnergyConsumptionInfo consumptionInfo) throws RemoteException {
    }
}

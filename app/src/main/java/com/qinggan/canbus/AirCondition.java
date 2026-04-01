package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public final class AirCondition implements Parcelable {
    public static final int AC_DOR_MODE_DEEP = 1;
    public static final int AC_DOR_MODE_STANDARD = 0;
    public static final int AC_DUAL_DUAL = 1;
    public static final int AC_DUAL_MONO = 0;
    public static final int AC_PM25_STS_COLLECTING = 1;
    public static final int AC_PM25_STS_COMPLETE = 2;
    public static final int AC_PM25_STS_STANDBY = 0;
    public static final int AC_RETURN_ACTIVE = 1;
    public static final int AC_RETURN_AutoCirculation = 2;
    public static final int AC_RETURN_DISPLAY = 2;
    public static final int AC_RETURN_ExternalCirculation = 1;
    public static final int AC_RETURN_FLOW_MODE_AUTO = 7;
    public static final int AC_RETURN_FLOW_MODE_DEF = 4;
    public static final int AC_RETURN_FLOW_MODE_FACE = 0;
    public static final int AC_RETURN_FLOW_MODE_FACE_LEG = 1;
    public static final int AC_RETURN_FLOW_MODE_LEG = 2;
    public static final int AC_RETURN_FLOW_MODE_LEG_DEF = 3;
    public static final int AC_RETURN_INACTIVE = 0;
    public static final int AC_RETURN_InternalCirculation = 0;
    public static final int AC_RETURN_SWITCH_OFF = 0;
    public static final int AC_RETURN_SWITCH_ON = 1;
    public static final int AC_WORKING_MODE_INVALIDE = 0;
    public static final int AC_WORKING_MODE_NORMAL = 2;
    public static final int AC_WORKING_MODE_SOFT = 1;
    public static final int AC_WORKING_MODE_STRONG = 3;
    public static final int AIR_AVN_DISPLAY = 1;
    public static final int AIR_AVN_ERROR_DISPLAY = 3;
    public static final int AIR_AVN_NOT_DISPLAY = 0;
    public static final int AIR_CIRCULATION_AUTO = 255;
    public static final int AIR_CIRCULATION_ERROR = 3;
    public static final int AIR_CIRCULATION_INNER = 2;
    public static final int AIR_CIRCULATION_NONE = 0;
    public static final int AIR_CIRCULATION_OUTER = 1;
    public static final int AIR_DISPLAY_MODE_DEFROST = 5;
    public static final int AIR_DISPLAY_MODE_FACE = 1;
    public static final int AIR_DISPLAY_MODE_FACE_FOOT = 2;
    public static final int AIR_DISPLAY_MODE_FOOT = 3;
    public static final int AIR_DISPLAY_MODE_FOOT_DEFROST = 4;
    public static final int AIR_MODE_AUTO = 0;
    public static final int AIR_MODE_MANUAL = 1;
    public static final int AIR_SUPPLY_AUTO = 1;
    public static final int AIR_SUPPLY_DOWN = 3;
    public static final int AIR_SUPPLY_DOWN_PARALLEL = 4;
    public static final int AIR_SUPPLY_DOWN_UP = 8;
    public static final int AIR_SUPPLY_DOWN_UP_PARALLEL = 9;
    public static final int AIR_SUPPLY_FRONT = 2;
    public static final int AIR_SUPPLY_PARALLEL = 5;
    public static final int AIR_SUPPLY_UP = 6;
    public static final int AIR_SUPPLY_UP_PARALLEL = 7;
    public static final int AIR_SW_OFF = 0;
    public static final int AIR_SW_ON = 1;
    public static final int AIR_TEMP_HIGH = 32;
    public static final int AIR_TEMP_INVALID = 0;
    public static final int AIR_TEMP_LOW = 16;
    public static final int AirTempHigh = 127;
    public static final int AirTempInvalid = 255;
    public static final int AirTempLow = 0;
    public static final int AutoCirculation = 3;
    public static final Parcelable.Creator<AirCondition> CREATOR = new Parcelable.Creator<AirCondition>() { // from class: com.qinggan.canbus.AirCondition.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public AirCondition[] newArray(int size) {
            return new AirCondition[size];
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public AirCondition createFromParcel(Parcel source) {
            AirCondition air = new AirCondition();
            air.airSWStatus = source.readInt();
            air.airACStatus = source.readInt();
            air.airHighWindStatus = source.readInt();
            air.airLowWindStatus = source.readInt();
            air.airDUALStatus = source.readInt();
            air.airDUALAllStatus = source.readInt();
            air.airMaxFrontStatus = source.readInt();
            air.airRearLightStatus = source.readInt();
            air.airSupplyStatus = source.readInt();
            air.airDisplaySW = source.readInt();
            air.airWindSpeed = source.readInt();
            air.airLeftTemperature = source.readFloat();
            air.airRightTemperature = source.readFloat();
            air.airRearTemperature = source.readFloat();
            air.airCirculationMode = source.readInt();
            air.airLeftSeatHeatingLevel = source.readInt();
            air.airRearCtlLockSW = source.readInt();
            air.airACMaxSW = source.readInt();
            air.airRightSeatHeatingLevel = source.readInt();
            air.airRearWindowHeatingStatus = source.readInt();
            air.airECO = source.readInt();
            air.airFrontWindowDefogger = source.readInt();
            air.airMode = source.readInt();
            air.airCirculationStatus = source.readInt();
            air.airRearWindSpeed = source.readInt();
            air.airRearDownSupplyStatus = source.readInt();
            air.airRearParallelSupplyStatus = source.readInt();
            air.airRearUpSupplyStatus = source.readInt();
            air.airRearWindowDefogger = source.readInt();
            air.airLeftMirrorDefogger = source.readInt();
            air.airRightMirrorDefogger = source.readInt();
            air.airSyncStatus = source.readInt();
            air.airDisplay_mode = source.readInt();
            air.IGN_ON = source.readInt();
            air.airTempInCar = source.readInt();
            air.airTempOutCar = source.readInt();
            air.ions_state = source.readInt();
            air.ions_switch = source.readInt();
            air.ac_power_switch = source.readInt();
            air.airACHeatStatus = source.readInt();
            air.airTempLevel = source.readInt();
            air.airCompressorErrorStatus = source.readInt();
            air.airCompressorLimitStatus = source.readInt();
            air.airCompressorWorkStatus = source.readInt();
            air.airPTCErrorStatus = source.readInt();
            air.airPTCLimitStatus = source.readInt();
            air.airPTCWorKStatus = source.readInt();
            air.airEngineStatus = source.readInt();
            air.airPm2_5 = source.readInt();
            air.acRunningState = source.readInt();
            air.airAQSStatus = source.readInt();
            air.airPMStatus = source.readInt();
            air.airPreSupplyStatus = source.readInt();
            air.acRapidCooling = source.readInt();
            air.acOneButtonWarmth = source.readInt();
            air.acHazeMode = source.readInt();
            air.acBabyCareMode = source.readInt();
            air.acAirCleanerMode = source.readInt();
            air.acRainSnowMode = source.readInt();
            air.acSmokingMode = source.readInt();
            air.acStopCarMode = source.readInt();
            air.displaypop = source.readInt();
            air.airPm25_level = source.readInt();
            air.airLeftTemperature_f = source.readFloat();
            air.airRightTemperature_f = source.readFloat();
            air.airPm2_5_outcar = source.readInt();
            air.airPm2_5_sts = source.readInt();
            air.airworking_mode = source.readInt();
            air.acAirVentCoolSts = source.readInt();
            air.airFrontSWStatus = source.readInt();
            air.airRearSWStatus = source.readInt();
            air.airFrontMode = source.readInt();
            air.airRearMode = source.readInt();
            air.airFrontSupplyStatus = source.readInt();
            air.airRearSupplyStatus = source.readInt();
            air.airFrontWindSpeed = source.readInt();
            air.isNeedRefreshAirWorkingMode = source.readInt();
            air.isAirPM2_5NeedRefresh = source.readInt();
            air.isAirPM2_5OutCarNeedRefresh = source.readInt();
            air.airDORSwitch = source.readInt();
            air.airDORMode = source.readInt();
            return air;
        }
    };
    public static final int ExternalCirculation = 1;
    public static final int IGN_FLAG_OFF = 0;
    public static final int IGN_FLAG_ON = 1;
    public static final int IONS_STATE_HIGH = 2;
    public static final int IONS_STATE_LOW = 0;
    public static final int IONS_STATE_MIDDLE = 1;
    public static final int InternalCirculation = 2;
    public static final int NoDisplay = 0;
    public static final int OneHeating = 1;
    public static final int ThreeHeating = 3;
    public static final int TwoHeating = 2;
    public static final int noDisplay = 0;
    private int airSWStatus = -1;
    private int airACStatus = -1;
    private int airHighWindStatus = -1;
    private int airLowWindStatus = -1;
    private int airDUALStatus = -1;
    private int airDUALAllStatus = -1;
    private int airMaxFrontStatus = -1;
    private int airRearLightStatus = -1;
    private int airSupplyStatus = -1;
    private int airDisplaySW = -1;
    private int airWindSpeed = -1;
    private float airLeftTemperature = -1.0f;
    private float airRightTemperature = -1.0f;
    private float airRearTemperature = -1.0f;
    private int airCirculationMode = -1;
    private int airLeftSeatHeatingLevel = -1;
    private int airRightSeatHeatingLevel = -1;
    private int airRearCtlLockSW = -1;
    private int airACMaxSW = -1;
    private int airRearWindowHeatingStatus = -1;
    private int airECO = -1;
    private int airFrontWindowDefogger = -1;
    private int airMode = -1;
    private int airCirculationStatus = -1;
    private int airRearWindSpeed = -1;
    private int airRearDownSupplyStatus = -1;
    private int airRearParallelSupplyStatus = -1;
    private int airRearUpSupplyStatus = -1;
    private int airRearWindowDefogger = -1;
    private int airRearWindowDefoggerEngine = -1;
    private int airLeftMirrorDefogger = -1;
    private int airRightMirrorDefogger = -1;
    private int airSyncStatus = -1;
    private int airDisplay_mode = -1;
    private int IGN_ON = -1;
    private int airTempInCar = -9999;
    private int airTempOutCar = -9999;
    private int displaypop = -1;
    private int ions_switch = -1;
    private int ions_state = -1;
    private int ac_power_switch = -1;
    private int airACHeatStatus = -1;
    private int airTempLevel = -1;
    private int airPTCWorKStatus = -1;
    private int airPTCErrorStatus = -1;
    private int airPTCLimitStatus = -1;
    private int airCompressorWorkStatus = -1;
    private int airCompressorErrorStatus = -1;
    private int airCompressorLimitStatus = -1;
    private int airEngineStatus = -1;
    private int airPm2_5 = -1;
    private int isAirPM2_5NeedRefresh = 1;
    private int acRunningState = -1;
    private int airPm25_level = -1;
    private int airPm25_level_not_display = 0;
    private int airPm25_level_green = 1;
    private int airPm25_level_yellow = 2;
    private int airPm25_level_orange = 3;
    private int airPm25_level_purple = 4;
    private int airPm25_level_maroon = 5;
    private int airPm25_level_red = 6;
    private float airLeftTemperature_f = -1.0f;
    private float airRightTemperature_f = -1.0f;
    private int airPm2_5_outcar = -1;
    private int isAirPM2_5OutCarNeedRefresh = 1;
    private int airPm2_5_sts = -1;
    private int airworking_mode = -1;
    private int isNeedRefreshAirWorkingMode = 1;
    private int airFrontSWStatus = -1;
    private int airRearSWStatus = -1;
    private int airFrontMode = -1;
    private int airRearMode = -1;
    private int airFrontSupplyStatus = -1;
    private int airRearSupplyStatus = -1;
    private int airFrontWindSpeed = -1;
    private int airDORSwitch = -1;
    private int airDORMode = -1;
    private int airAQSStatus = -1;
    private int acRapidCooling = -1;
    private int acOneButtonWarmth = -1;
    private int acHazeMode = -1;
    private int acBabyCareMode = -1;
    private int acAirCleanerMode = -1;
    private int acAirVentCoolSts = -1;
    private int acRainSnowMode = -1;
    private int acSmokingMode = -1;
    private int acStopCarMode = -1;
    private int airPMStatus = -1;
    private int airPreSupplyStatus = -1;
    private int ac_function_1 = -1;
    private int ac_function_2 = -1;
    private int ac_function_3 = -1;
    private int ac_function_4 = -1;
    private int ac_function_5 = -1;
    private int ac_function_6 = -1;
    private int ac_function_7 = -1;
    private int ac_function_8 = -1;

    public int getAc_function_1() {
        return this.ac_function_1;
    }

    public void setAc_function_1(int ac_function_1) {
        this.ac_function_1 = ac_function_1;
    }

    public int getAc_function_2() {
        return this.ac_function_2;
    }

    public void setAc_function_2(int ac_function_2) {
        this.ac_function_2 = ac_function_2;
    }

    public int getAc_function_3() {
        return this.ac_function_3;
    }

    public void setAc_function_3(int ac_function_3) {
        this.ac_function_3 = ac_function_3;
    }

    public int getAc_function_4() {
        return this.ac_function_4;
    }

    public void setAc_function_4(int ac_function_4) {
        this.ac_function_4 = ac_function_4;
    }

    public int getAc_function_5() {
        return this.ac_function_5;
    }

    public void setAc_function_5(int ac_function_5) {
        this.ac_function_5 = ac_function_5;
    }

    public int getAc_function_6() {
        return this.ac_function_6;
    }

    public void setAc_function_6(int ac_function_6) {
        this.ac_function_6 = ac_function_6;
    }

    public int getAc_function_7() {
        return this.ac_function_7;
    }

    public void setAc_function_7(int ac_function_7) {
        this.ac_function_7 = ac_function_7;
    }

    public int getAc_function_8() {
        return this.ac_function_8;
    }

    public void setAc_function_8(int ac_function_8) {
        this.ac_function_8 = ac_function_8;
    }

    public int getAcRapidCooling() {
        return this.acRapidCooling;
    }

    public void setAcRapidCooling(int acRapidCooling) {
        this.acRapidCooling = acRapidCooling;
    }

    public int getAcOneButtonWarmth() {
        return this.acOneButtonWarmth;
    }

    public void setAcOneButtonWarmth(int acOneButtonWarmth) {
        this.acOneButtonWarmth = acOneButtonWarmth;
    }

    public int getAcHazeMode() {
        return this.acHazeMode;
    }

    public void setAcHazeMode(int acHazeMode) {
        this.acHazeMode = acHazeMode;
    }

    public int getAcBabyCareMode() {
        return this.acBabyCareMode;
    }

    public void setAcBabyCareMode(int acBabyCareMode) {
        this.acBabyCareMode = acBabyCareMode;
    }

    public int getAcAirCleanerMode() {
        return this.acAirCleanerMode;
    }

    public void setAcAirCleanerMode(int acAirCleanerMode) {
        this.acAirCleanerMode = acAirCleanerMode;
    }

    public int getAcAirVentCoolSts() {
        return this.acAirVentCoolSts;
    }

    public void setAcAirVentCoolSts(int acAirVentCoolSts) {
        this.acAirVentCoolSts = acAirVentCoolSts;
    }

    public int getAcRainSnowMode() {
        return this.acRainSnowMode;
    }

    public void setAcRainSnowMode(int acRainSnowMode) {
        this.acRainSnowMode = acRainSnowMode;
    }

    public int getAcSmokingMode() {
        return this.acSmokingMode;
    }

    public void setAcSmokingMode(int acSmokingMode) {
        this.acSmokingMode = acSmokingMode;
    }

    public int getAcStopCarMode() {
        return this.acStopCarMode;
    }

    public void setAcStopCarMode(int acStopCarMode) {
        this.acStopCarMode = acStopCarMode;
    }

    public int getAirSupplyPreStatus() {
        return this.airPreSupplyStatus;
    }

    public void setAirSupplyPreStatus(int airPreSupplyStatus) {
        this.airPreSupplyStatus = airPreSupplyStatus;
    }

    public int getAirPMStatus() {
        return this.airPMStatus;
    }

    public void setAirPMStatus(int airPMStatus) {
        this.airPMStatus = airPMStatus;
    }

    public int getAirAQSStatus() {
        return this.airAQSStatus;
    }

    public void setAirAQSStatus(int airAQSStatus) {
        this.airAQSStatus = airAQSStatus;
    }

    public int getAcRunningState() {
        return this.acRunningState;
    }

    public void setAcRunningState(int acRunningState) {
        this.acRunningState = acRunningState;
    }

    public int getAirPm2_5() {
        return this.airPm2_5;
    }

    public void setAirPm2_5(int airPm2_5) {
        this.airPm2_5 = airPm2_5;
    }

    public int getairPm2_5_outcar() {
        return this.airPm2_5_outcar;
    }

    public void setairPm2_5_outcar(int airPm2_5_outcar) {
        this.airPm2_5_outcar = airPm2_5_outcar;
    }

    public int getairPm2_5_sts() {
        return this.airPm2_5_sts;
    }

    public void setairPm2_5_sts(int airPm2_5_sts) {
        this.airPm2_5_sts = airPm2_5_sts;
    }

    public int getairworking_mode() {
        return this.airworking_mode;
    }

    public void setPM2_5RefreshState(int refresh) {
        this.isAirPM2_5NeedRefresh = refresh;
    }

    public int getPM2_5RefreshState() {
        return this.isAirPM2_5NeedRefresh;
    }

    public void setPM2_5OutCarRefreshState(int refresh) {
        this.isAirPM2_5OutCarNeedRefresh = refresh;
    }

    public int getPM2_5OutCarRefreshState() {
        return this.isAirPM2_5OutCarNeedRefresh;
    }

    public void setAirWorkingRefreshState(int refresh) {
        this.isNeedRefreshAirWorkingMode = refresh;
    }

    public int getAirWorkingRefreshState() {
        return this.isNeedRefreshAirWorkingMode;
    }

    public void setairworking_mode(int airworking_mode) {
        this.airworking_mode = airworking_mode;
    }

    public int getAirPm25_level() {
        return this.airPm25_level;
    }

    public void setAirPm25_level(int airPm25_level) {
        this.airPm25_level = airPm25_level;
    }

    public int getAirPTCWorKStatus() {
        return this.airPTCWorKStatus;
    }

    public void setAirPTCWorKStatus(int airPTCWorKStatus) {
        this.airPTCWorKStatus = airPTCWorKStatus;
    }

    public int getAirPTCErrorStasus() {
        return this.airPTCErrorStatus;
    }

    public void setAirPTCErrorStasus(int airPTCErrorStasus) {
        this.airPTCErrorStatus = airPTCErrorStasus;
    }

    public int getAirPTCLimitStatus() {
        return this.airPTCLimitStatus;
    }

    public void setAirPTCLimitStatus(int airPTCLimitStatus) {
        this.airPTCLimitStatus = airPTCLimitStatus;
    }

    public int getAirCompressorWorkStatus() {
        return this.airCompressorWorkStatus;
    }

    public void setAirCompressorWorkStatus(int airCompressorWorkStatus) {
        this.airCompressorWorkStatus = airCompressorWorkStatus;
    }

    public int getAirCompressorErrorStatus() {
        return this.airCompressorErrorStatus;
    }

    public void setAirCompressorErrorStatus(int airCompressorErrorStatus) {
        this.airCompressorErrorStatus = airCompressorErrorStatus;
    }

    public int getAirCompressorLimitStatus() {
        return this.airCompressorLimitStatus;
    }

    public void setAirCompressorLimitStatus(int airCompressorLimitStatus) {
        this.airCompressorLimitStatus = airCompressorLimitStatus;
    }

    public int getAirEngineStatus() {
        return this.airEngineStatus;
    }

    public void setAirEngineStatus(int airEngineStatus) {
        this.airEngineStatus = airEngineStatus;
    }

    public int getAirACHeatStatus() {
        return this.airACHeatStatus;
    }

    public void setAirACHeatStatus(int airACHeatStatus) {
        this.airACHeatStatus = airACHeatStatus;
    }

    public int getAirTempLevel() {
        return this.airTempLevel;
    }

    public void setAirTempLevel(int airTempLevel) {
        this.airTempLevel = airTempLevel;
    }

    public int getAirDORSwitch() {
        return this.airDORSwitch;
    }

    public void setAirDORSwitch(int airDORSwitch) {
        this.airDORSwitch = airDORSwitch;
    }

    public int getAirDORMode() {
        return this.airDORMode;
    }

    public void setAirDORMode(int airDORMode) {
        this.airDORMode = airDORMode;
    }

    public void setAirDisplayMode(int airDisplay_mode) {
        this.airDisplay_mode = airDisplay_mode;
    }

    public int getAirDisplayMode() {
        return this.airDisplay_mode;
    }

    public void setIGN_ON(int IGN_ON) {
        this.IGN_ON = IGN_ON;
    }

    public int getIGN_ON() {
        return this.IGN_ON;
    }

    public void setAirTempInCar(int airTempInCar) {
        this.airTempInCar = airTempInCar;
    }

    public int getAirTempInCar() {
        return this.airTempInCar;
    }

    public void setAirTempOutCar(int airTempOutCar) {
        this.airTempOutCar = airTempOutCar;
    }

    public int getAirTempOutCar() {
        return this.airTempOutCar;
    }

    public void setAirSWStatus(int airSWStatus) {
        this.airSWStatus = airSWStatus;
    }

    public int getAirSWStatus() {
        return this.airSWStatus;
    }

    public int getAirFrontSWStatus() {
        return this.airFrontSWStatus;
    }

    public void setAirFrontSWStatus(int airFrontSWStatus) {
        this.airFrontSWStatus = airFrontSWStatus;
    }

    public int getAirRearSWStatus() {
        return this.airRearSWStatus;
    }

    public void setAirRearSWStatus(int airRearSWStatus) {
        this.airRearSWStatus = airRearSWStatus;
    }

    public float getAirRightTemperature() {
        return this.airRightTemperature;
    }

    public void setAirRightTemperature(float airRightTemperature) {
        this.airRightTemperature = airRightTemperature;
    }

    public float getAirLeftTemperature() {
        return this.airLeftTemperature;
    }

    public void setAirLeftTemperature(float airLeftTemperature) {
        this.airLeftTemperature = airLeftTemperature;
    }

    public float getAirRightTemperature_f() {
        return this.airRightTemperature_f;
    }

    public void setAirRightTemperature_f(float airRightTemperature_f) {
        this.airRightTemperature_f = airRightTemperature_f;
    }

    public float getAirLeftTemperature_f() {
        return this.airLeftTemperature_f;
    }

    public void setAirLeftTemperature_f(float airLeftTemperature_f) {
        this.airLeftTemperature_f = airLeftTemperature_f;
    }

    public int getAirWindSpeed() {
        return this.airWindSpeed;
    }

    public void setAirWindSpeed(int airWindSpeed) {
        this.airWindSpeed = airWindSpeed;
    }

    public int getAirMode() {
        return this.airMode;
    }

    public void setAirMode(int airMode) {
        this.airMode = airMode;
    }

    public int getAirFrontMode() {
        return this.airFrontMode;
    }

    public void setAirFrontMode(int airFrontMode) {
        this.airFrontMode = airFrontMode;
    }

    public int getAirRearMode() {
        return this.airRearMode;
    }

    public void setAirRearMode(int airRearMode) {
        this.airRearMode = airRearMode;
    }

    public int getAirFrontWindowDefogger() {
        return this.airFrontWindowDefogger;
    }

    public void setAirFrontWindowDefogger(int airFrontWindowDefogger) {
        this.airFrontWindowDefogger = airFrontWindowDefogger;
    }

    public int getAirECO() {
        return this.airECO;
    }

    public void setAirECO(int airECO) {
        this.airECO = airECO;
    }

    public int getAirDUALStatus() {
        return this.airDUALStatus;
    }

    public void setAirDUALStatus(int airDUALStatus) {
        this.airDUALStatus = airDUALStatus;
    }

    public int getAirDUALAllStatus() {
        return this.airDUALAllStatus;
    }

    public void setAirDUALAllStatus(int airDUALAllStatus) {
        this.airDUALAllStatus = airDUALAllStatus;
    }

    public int getIons_switch() {
        return this.ions_switch;
    }

    public void setIons_switch(int ions_switch) {
        this.ions_switch = ions_switch;
    }

    public int getIons_state() {
        return this.ions_state;
    }

    public void setIons_state(int ions_state) {
        this.ions_state = ions_state;
    }

    public int getAc_power_switch() {
        return this.ac_power_switch;
    }

    public void setAc_power_switch(int ac_power_switch) {
        this.ac_power_switch = ac_power_switch;
    }

    public int getAirCirculationMode() {
        return this.airCirculationMode;
    }

    public void setAirCirculationMode(int airCirculationMode) {
        this.airCirculationMode = airCirculationMode;
    }

    public int getAirACStatus() {
        return this.airACStatus;
    }

    public void setAirACStatus(int airACStatus) {
        this.airACStatus = airACStatus;
    }

    public int getAirdisplaypop() {
        return this.displaypop;
    }

    public void setAirdisplaypop(int displaypop) {
        this.displaypop = displaypop;
    }

    public int getAirHighWindStatus() {
        return this.airHighWindStatus;
    }

    public void setAirHighWindStatus(int airHighWindStatus) {
        this.airHighWindStatus = airHighWindStatus;
    }

    public int getAirLowWindStatus() {
        return this.airLowWindStatus;
    }

    public void setAirLowWindStatus(int airLowWindStatus) {
        this.airLowWindStatus = airLowWindStatus;
    }

    public int getAirMaxFrontStatus() {
        return this.airMaxFrontStatus;
    }

    public void setAirMaxFrontStatus(int airMaxFrontStatus) {
        this.airMaxFrontStatus = airMaxFrontStatus;
    }

    public int getAirRearLightStatus() {
        return this.airRearLightStatus;
    }

    public void setAirRearLightStatus(int airRearLightStatus) {
        this.airRearLightStatus = airRearLightStatus;
    }

    public int getAirSupplyStatus() {
        return this.airSupplyStatus;
    }

    public void setAirSupplyStatus(int airSupplyStatus) {
        this.airSupplyStatus = airSupplyStatus;
    }

    public int getAirWindSupplyFlagUp() {
        if (this.airSupplyStatus == 6) {
            return 1;
        }
        return 0;
    }

    public int getAirFrontSupplyStatus() {
        return this.airFrontSupplyStatus;
    }

    public void setAirFrontSupplyStatus(int airFrontSupplyStatus) {
        this.airFrontSupplyStatus = airFrontSupplyStatus;
    }

    public int getAirRearSupplyStatus() {
        return this.airRearSupplyStatus;
    }

    public void setAirRearSupplyStatus(int airRearSupplyStatus) {
        this.airRearSupplyStatus = airRearSupplyStatus;
    }

    public int getAirWindSupplyFlagDown() {
        if (this.airSupplyStatus == 3) {
            return 1;
        }
        return 0;
    }

    public int getAirWindSupplyFlagParallel() {
        if (this.airSupplyStatus == 5) {
            return 1;
        }
        return 0;
    }

    public int getAirWindSupplyFlagUpParallel() {
        if (this.airSupplyStatus == 7) {
            return 1;
        }
        return 0;
    }

    public int getAirWindSupplyFlagUpDown() {
        if (this.airSupplyStatus == 8) {
            return 1;
        }
        return 0;
    }

    public int getAirWindSupplyFlagUpDownParallel() {
        if (this.airSupplyStatus == 9) {
            return 1;
        }
        return 0;
    }

    public int getAirWindSupplyFlagDownParallel() {
        if (this.airSupplyStatus == 4) {
            return 1;
        }
        return 0;
    }

    public int getAirWindSupplyFlagFront() {
        if (this.airSupplyStatus == 2) {
            return 1;
        }
        return 0;
    }

    public int getAirRearWindowDefogger() {
        return this.airRearWindowDefogger;
    }

    public void setAirRearWindowDefogger(int airRearWindowDefogger) {
        this.airRearWindowDefogger = airRearWindowDefogger;
    }

    public int getAirRearWindowDefoggerEngine() {
        Log.i("AirCondition=========================", "this.airRearWindowDefoggerEngine = " + this.airRearWindowDefoggerEngine);
        return this.airRearWindowDefoggerEngine;
    }

    public void setAirRearWindowDefoggerEngine(int engineStatus) {
        Log.i("AirCondition=========================", "engineStatus = " + engineStatus);
        this.airRearWindowDefoggerEngine = engineStatus;
    }

    public int getAirSyncStatus() {
        return this.airSyncStatus;
    }

    public void setAirSyncStatus(int syncStatus) {
        this.airSyncStatus = syncStatus;
    }

    public int getAirLeftMirrorDefogger() {
        return this.airLeftMirrorDefogger;
    }

    public void setAirLeftMirrorDefogger(int airLeftMirrorDefogger) {
        this.airLeftMirrorDefogger = airLeftMirrorDefogger;
    }

    public int getAirRightMirrorDefogger() {
        return this.airRightMirrorDefogger;
    }

    public void setAirRightMirrorDefogger(int airRightMirrorDefogger) {
        this.airRightMirrorDefogger = airRightMirrorDefogger;
    }

    public int getAirUpSupplyStatus() {
        int i = this.airSupplyStatus;
        if (i == 6 || i == 7 || i == 8 || i == 9) {
            return 1;
        }
        return 0;
    }

    public int getAirDownSupplyStatus() {
        int i = this.airSupplyStatus;
        if (i == 3 || i == 4 || i == 8 || i == 9) {
            return 1;
        }
        return 0;
    }

    public int getAirParallelStatus() {
        int i = this.airSupplyStatus;
        if (i == 5 || i == 7 || i == 4 || i == 9) {
            return 1;
        }
        return 0;
    }

    public int getAirDisplaySW() {
        return this.airDisplaySW;
    }

    public void setAirDisplaySW(int airDisplaySW) {
        this.airDisplaySW = airDisplaySW;
    }

    public int getAirLeftSeatHeatingLevel() {
        return this.airLeftSeatHeatingLevel;
    }

    public void setAirLeftSeatHeatingLevel(int airLeftSeatHeatingLevel) {
        this.airLeftSeatHeatingLevel = airLeftSeatHeatingLevel;
    }

    public int getAirRightSeatHeatingLevel() {
        return this.airRightSeatHeatingLevel;
    }

    public void setAirRightSeatHeatingLevel(int airRightSeatHeatingLevel) {
        this.airRightSeatHeatingLevel = airRightSeatHeatingLevel;
    }

    public int getAirRearCtlLockSW() {
        return this.airRearCtlLockSW;
    }

    public void setAirRearCtlLockSW(int airRearCtlLockSW) {
        this.airRearCtlLockSW = airRearCtlLockSW;
    }

    public int getAirACMaxSW() {
        return this.airACMaxSW;
    }

    public void setAirACMaxSW(int airACMaxSW) {
        this.airACMaxSW = airACMaxSW;
    }

    public int getAirRearWindowHeatingStatus() {
        return this.airRearWindowHeatingStatus;
    }

    public void setAirRearWindowHeatingStatus(int airRearWindowHeatingStatus) {
        this.airRearWindowHeatingStatus = airRearWindowHeatingStatus;
    }

    public int getAirCirculationStatus() {
        return this.airCirculationStatus;
    }

    public void setAirCirculationStatus(int airCirculationStatus) {
        this.airCirculationStatus = airCirculationStatus;
    }

    public float getAirRearTemperature() {
        return this.airRearTemperature;
    }

    public void setAirRearTemperature(float airRearTemperature) {
        this.airRearTemperature = airRearTemperature;
    }

    public int getAirFrontWindSpeed() {
        return this.airFrontWindSpeed;
    }

    public void setAirFrontWindSpeed(int airFrontWindSpeed) {
        this.airFrontWindSpeed = airFrontWindSpeed;
    }

    public int getAirRearWindSpeed() {
        return this.airRearWindSpeed;
    }

    public void setAirRearWindSpeed(int airRearWindSpeed) {
        this.airRearWindSpeed = airRearWindSpeed;
    }

    public int getAirRearDownSupplyStatus() {
        return this.airRearDownSupplyStatus;
    }

    public void setAirRearDownSupplyStatus(int airRearDownSupplyStatus) {
        this.airRearDownSupplyStatus = airRearDownSupplyStatus;
    }

    public int getAirRearParallelSupplyStatus() {
        return this.airRearParallelSupplyStatus;
    }

    public void setAirRearParallelSupplyStatus(int airRearParallelSupplyStatus) {
        this.airRearParallelSupplyStatus = airRearParallelSupplyStatus;
    }

    public int getAirRearUpSupplyStatus() {
        return this.airRearUpSupplyStatus;
    }

    public void setAirRearUpSupplyStatus(int airRearUpSupplyStatus) {
        this.airRearUpSupplyStatus = airRearUpSupplyStatus;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.airSWStatus);
        dest.writeInt(this.airACStatus);
        dest.writeInt(this.airHighWindStatus);
        dest.writeInt(this.airLowWindStatus);
        dest.writeInt(this.airDUALStatus);
        dest.writeInt(this.airDUALAllStatus);
        dest.writeInt(this.airMaxFrontStatus);
        dest.writeInt(this.airRearLightStatus);
        dest.writeInt(this.airSupplyStatus);
        dest.writeInt(this.airDisplaySW);
        dest.writeInt(this.airWindSpeed);
        dest.writeFloat(this.airLeftTemperature);
        dest.writeFloat(this.airRightTemperature);
        dest.writeFloat(this.airRearTemperature);
        dest.writeInt(this.airCirculationMode);
        dest.writeInt(this.airLeftSeatHeatingLevel);
        dest.writeInt(this.airRearCtlLockSW);
        dest.writeInt(this.airACMaxSW);
        dest.writeInt(this.airRightSeatHeatingLevel);
        dest.writeInt(this.airRearWindowHeatingStatus);
        dest.writeInt(this.airECO);
        dest.writeInt(this.airFrontWindowDefogger);
        dest.writeInt(this.airMode);
        dest.writeInt(this.airCirculationStatus);
        dest.writeInt(this.airRearWindSpeed);
        dest.writeInt(this.airRearDownSupplyStatus);
        dest.writeInt(this.airRearParallelSupplyStatus);
        dest.writeInt(this.airRearUpSupplyStatus);
        dest.writeInt(this.airRearWindowDefogger);
        dest.writeInt(this.airLeftMirrorDefogger);
        dest.writeInt(this.airRightMirrorDefogger);
        dest.writeInt(this.airSyncStatus);
        dest.writeInt(this.airDisplay_mode);
        dest.writeInt(this.IGN_ON);
        dest.writeInt(this.airTempInCar);
        dest.writeInt(this.airTempOutCar);
        dest.writeInt(this.ions_state);
        dest.writeInt(this.ions_switch);
        dest.writeInt(this.ac_power_switch);
        dest.writeInt(this.airACHeatStatus);
        dest.writeInt(this.airTempLevel);
        dest.writeInt(this.airCompressorErrorStatus);
        dest.writeInt(this.airCompressorLimitStatus);
        dest.writeInt(this.airCompressorWorkStatus);
        dest.writeInt(this.airPTCErrorStatus);
        dest.writeInt(this.airPTCLimitStatus);
        dest.writeInt(this.airPTCWorKStatus);
        dest.writeInt(this.airEngineStatus);
        dest.writeInt(this.airPm2_5);
        dest.writeInt(this.acRunningState);
        dest.writeInt(this.airAQSStatus);
        dest.writeInt(this.airPMStatus);
        dest.writeInt(this.airPreSupplyStatus);
        dest.writeInt(this.acRapidCooling);
        dest.writeInt(this.acOneButtonWarmth);
        dest.writeInt(this.acHazeMode);
        dest.writeInt(this.acBabyCareMode);
        dest.writeInt(this.acAirCleanerMode);
        dest.writeInt(this.acRainSnowMode);
        dest.writeInt(this.acSmokingMode);
        dest.writeInt(this.acStopCarMode);
        dest.writeInt(this.displaypop);
        dest.writeInt(this.airPm25_level);
        dest.writeFloat(this.airLeftTemperature_f);
        dest.writeFloat(this.airRightTemperature_f);
        dest.writeInt(this.airPm2_5_outcar);
        dest.writeInt(this.airPm2_5_sts);
        dest.writeInt(this.airworking_mode);
        dest.writeInt(this.acAirVentCoolSts);
        dest.writeInt(this.airFrontSWStatus);
        dest.writeInt(this.airRearSWStatus);
        dest.writeInt(this.airFrontMode);
        dest.writeInt(this.airRearMode);
        dest.writeInt(this.airFrontSupplyStatus);
        dest.writeInt(this.airRearSupplyStatus);
        dest.writeInt(this.airFrontWindSpeed);
        dest.writeInt(this.isNeedRefreshAirWorkingMode);
        dest.writeInt(this.isAirPM2_5NeedRefresh);
        dest.writeInt(this.isAirPM2_5OutCarNeedRefresh);
        dest.writeInt(this.airDORSwitch);
        dest.writeInt(this.airDORMode);
    }

    public String toString() {
        return "AirCondition{airSWStatus=" + this.airSWStatus + ", airACStatus=" + this.airACStatus + ", airHighWindStatus=" + this.airHighWindStatus + ", airLowWindStatus=" + this.airLowWindStatus + ", airDUALStatus=" + this.airDUALStatus + ", airDUALAllStatus=" + this.airDUALAllStatus + ", airMaxFrontStatus=" + this.airMaxFrontStatus + ", airRearLightStatus=" + this.airRearLightStatus + ", airSupplyStatus=" + this.airSupplyStatus + ", airDisplaySW=" + this.airDisplaySW + ", airWindSpeed=" + this.airWindSpeed + ", airLeftTemperature=" + this.airLeftTemperature + ", airRightTemperature=" + this.airRightTemperature + ", airRearTemperature=" + this.airRearTemperature + ", airCirculationMode=" + this.airCirculationMode + ", airLeftSeatHeatingLevel=" + this.airLeftSeatHeatingLevel + ", airRightSeatHeatingLevel=" + this.airRightSeatHeatingLevel + ", airRearCtlLockSW=" + this.airRearCtlLockSW + ", airACMaxSW=" + this.airACMaxSW + ", airRearWindowHeatingStatus=" + this.airRearWindowHeatingStatus + ", airECO=" + this.airECO + ", airFrontWindowDefogger=" + this.airFrontWindowDefogger + ", airMode=" + this.airMode + ", airCirculationStatus=" + this.airCirculationStatus + ", airRearWindSpeed=" + this.airRearWindSpeed + ", airRearDownSupplyStatus=" + this.airRearDownSupplyStatus + ", airRearParallelSupplyStatus=" + this.airRearParallelSupplyStatus + ", airRearUpSupplyStatus=" + this.airRearUpSupplyStatus + ", airRearWindowDefogger=" + this.airRearWindowDefogger + ", airRearWindowDefoggerEngine=" + this.airRearWindowDefoggerEngine + ", airLeftMirrorDefogger=" + this.airLeftMirrorDefogger + ", airRightMirrorDefogger=" + this.airRightMirrorDefogger + ", airSyncStatus=" + this.airSyncStatus + ", airDisplay_mode=" + this.airDisplay_mode + ", IGN_ON=" + this.IGN_ON + ", airTempInCar=" + this.airTempInCar + ", airTempOutCar=" + this.airTempOutCar + ", ions_state=" + this.ions_state + ", ions_switch=" + this.ions_switch + ", airACHeatStatus=" + this.airACHeatStatus + ", airTempLevel=" + this.airTempLevel + ", airCompressorErrorStatus=" + this.airCompressorErrorStatus + ", airCompressorLimitStatus=" + this.airCompressorLimitStatus + ", airCompressorWorkStatus=" + this.airCompressorWorkStatus + ", airPTCErrorStatus=" + this.airPTCErrorStatus + ", airPTCLimitStatus=" + this.airPTCLimitStatus + ", airPTCWorKStatus=" + this.airPTCWorKStatus + ", airEngineStatus=" + this.airEngineStatus + ", airPm2_5=" + this.airPm2_5 + ", acRunningState=" + this.acRunningState + ", acRapidCooling=" + this.acRapidCooling + ", acOneButtonWarmth=" + this.acOneButtonWarmth + ", acHazeMode=" + this.acHazeMode + ", acBabyCareMode=" + this.acBabyCareMode + ", acAirCleanerMode=" + this.acAirCleanerMode + ", acRainSnowMode=" + this.acRainSnowMode + ", acSmokingMode=" + this.acSmokingMode + ", acStopCarMode=" + this.acStopCarMode + ", displaypop=" + this.displaypop + ", airPm25_level=" + this.airPm25_level + ", airLeftTemperature_f=" + this.airLeftTemperature_f + ", airRightTemperature_f=" + this.airRightTemperature_f + ", airPm2_5_outcar=" + this.airPm2_5_outcar + ", airPm2_5_sts=" + this.airPm2_5_sts + ", airworking_mode=" + this.airworking_mode + ", acAirVentCoolSts=" + this.acAirVentCoolSts + ", airFrontSWStatus=" + this.airFrontSWStatus + ", airRearSWStatus=" + this.airRearSWStatus + ", airFrontMode=" + this.airFrontMode + ", airRearMode=" + this.airRearMode + ", airFrontSupplyStatus=" + this.airFrontSupplyStatus + ", airRearSupplyStatus=" + this.airRearSupplyStatus + ", airFrontWindSpeed=" + this.airFrontWindSpeed + ", airWorkModeRefresh=" + this.isNeedRefreshAirWorkingMode + ", airDORSwitch=" + this.airDORSwitch + ", airDORMode=" + this.airDORMode + '}';
    }
}

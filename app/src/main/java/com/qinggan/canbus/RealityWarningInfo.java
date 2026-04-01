package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

public class RealityWarningInfo implements Parcelable {
    public static final String ACU_DRY_SEATBELT_BUCKLE_STATUS_SPEEK_CONTENT = "为了您的安全请帮自己系好安全带";
    public static final int ACU_DRY_SEATBELT_BUCKLE_STATUS_SPEEK_CONTENT_ID = 4106;
    public static final String ADJUST_N_TO_STOP_SPEEK_CONTENT = "请挂N挡位启动";
    public static final int ADJUST_N_TO_STOP_SPEEK_CONTENT_ID = 4112;
    public static final String ADJUST_PORN_TO_START_SPEEK_CONTENT = "启动时请挂P挡或者N挡";
    public static final int ADJUST_PORN_TO_START_SPEEK_CONTENT_ID = 4109;
    public static final String ADJUST_P_TO_STOP_SPEEK_CONTENT = "请挂P挡驻车";
    public static final int ADJUST_P_TO_STOP_SPEEK_CONTENT_ID = 4111;
    public static final String BCM_DRIVER_DOOR_AJAR_STATUS_SPEEK_CONTENT = "请检查驾驶员侧门是否关好";
    public static final int BCM_DRIVER_DOOR_AJAR_STATUS_SPEEK_CONTENT_ID = 4100;
    public static final String BCM_HOOD_AJAR_STATUS_SPEEK_CONTENT = "请检查引擎罩是否关好";
    public static final int BCM_HOOD_AJAR_STATUS_SPEEK_CONTENT_ID = 4098;
    public static final String BCM_LIGHT_ON_SPEEK_CONTENT = "请检查灯光是否全部关闭";
    public static final int BCM_LIGHT_ON_SPEEK_CONTENT_ID = 4097;
    public static final String BCM_PASSENGER_DOOR_AJAR_STATUS_SPEEK_CONTENT = "请检查副驾驶员侧门是否关好";
    public static final int BCM_PASSENGER_DOOR_AJAR_STATUS_SPEEK_CONTENT_ID = 4101;
    public static final String BCM_REAR_LEFT_DOOR_AJAR_STATUS_SPEEK_CONTENT = "请检查左后门是否关好";
    public static final int BCM_REAR_LEFT_DOOR_AJAR_STATUS_SPEEK_CONTENT_ID = 4102;
    public static final String BCM_REAR_RIGHT_DOOR_AJAR_STATUS_SPEEK_CONTENT = "请检查右后门是否关好";
    public static final int BCM_REAR_RIGHT_DOOR_AJAR_STATUS_SPEEK_CONTENT_ID = 4103;
    public static final String BCM_TRUNK_AJAR_STATUS_SPEEK_CONTENT = "请检查后备箱是否关好";
    public static final int BCM_TRUNK_AJAR_STATUS_SPEEK_CONTENT_ID = 4099;
    public static final String CHECK_SMART_KEY_SPEEK_CONTENT = "智能钥匙不在附近";
    public static final int CHECK_SMART_KEY_SPEEK_CONTENT_ID = 4108;
    public static final String CLOSE_CAR_DOOR_TO_LOCK_VEHICLE_SPEEK_CONTENT = "请关闭车门闭锁车辆";
    public static final int CLOSE_CAR_DOOR_TO_LOCK_VEHICLE_SPEEK_CONTENT_ID = 4117;
    public static final Parcelable.Creator<RealityWarningInfo> CREATOR = new Parcelable.Creator<RealityWarningInfo>() { // from class: com.qinggan.canbus.RealityWarningInfo.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public RealityWarningInfo createFromParcel(Parcel source) {
            RealityWarningInfo realityWarningInfo = new RealityWarningInfo();
            realityWarningInfo.mBCM_LightleftOn = source.readInt();
            realityWarningInfo.mBCM_HoodAjarStatus = source.readInt();
            realityWarningInfo.mBCM_TrunkAjarStatus = source.readInt();
            realityWarningInfo.mBCM_DriverDoorAjarStatus = source.readInt();
            realityWarningInfo.mBCM_PassengerDoorAjarStatus = source.readInt();
            realityWarningInfo.mBCM_RearLeftDoorAjarStatus = source.readInt();
            realityWarningInfo.mBCM_RearRightDoorAjarStatus = source.readInt();
            realityWarningInfo.mTCU_ESModReqRejt = source.readInt();
            realityWarningInfo.mICUVehicleDisplaySpeed = source.readInt();
            realityWarningInfo.mFuelLevelStatus = source.readInt();
            realityWarningInfo.mACU_DrySeatbeltBuckleStatus = source.readInt();
            realityWarningInfo.mCheckSmartKey = source.readInt();
            realityWarningInfo.mAdjustPorNToStart = source.readInt();
            realityWarningInfo.mTreadCluth = source.readInt();
            realityWarningInfo.mAdjustPToStop = source.readInt();
            realityWarningInfo.mAdjustNToStop = source.readInt();
            realityWarningInfo.mSmartKeyElectricLow = source.readInt();
            realityWarningInfo.mSmartKeyToBeToken = source.readInt();
            realityWarningInfo.mSmartKeyLeftCar = source.readInt();
            realityWarningInfo.mStartSwitchError = source.readInt();
            realityWarningInfo.mCloseCarDoorToLockVehicle = source.readInt();
            realityWarningInfo.mCarType = source.readInt();
            realityWarningInfo.mVehicleType = source.readInt();
            realityWarningInfo.mEngineTemperatureHigh = source.readInt();
            realityWarningInfo.mPEPSPowerOn = source.readInt();
            realityWarningInfo.mMaintenceRemind = source.readInt();
            realityWarningInfo.mBonnetDoor = source.readInt();
            realityWarningInfo.mLoadSpace = source.readInt();
            realityWarningInfo.mRoadFrozen = source.readInt();
            realityWarningInfo.mSeatbeltBuckleStatus = source.readInt();
            realityWarningInfo.mPassengerSeatBeltSate = source.readInt();
            return realityWarningInfo;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public RealityWarningInfo[] newArray(int size) {
            return new RealityWarningInfo[size];
        }
    };
    public static final int DRIVER_SEAT_BELTSATE_CONTENT_ID = 4127;
    public static final String ENGINE_TEMPERATURE_HIGH_CONTENT = "发动机温度过高请停车";
    public static final int ENGINE_TEMPERATURE_HIGH_CONTENT_ID = 4118;
    public static final String FUEL_LEVEL_STATUS_SPEEK_CONTENT = "油量过低请及时加油";
    public static final int FUEL_LEVEL_STATUS_SPEEK_CONTENT_ID = 4105;
    public static final String ICU_VEHICLE_DISPLAY_SPEED_SPEEK_CONTENT = "速度过快请降低车速";
    public static final int ICU_VEHICLE_DISPLAY_SPEED_SPEEK_CONTENT_ID = 4104;
    public static final int PASSENGER_SEAT_BELTSATE_CONTENT_ID = 4128;
    public static final String PLEASE_AEPAIR_THE_VEHICLE_CONTENT = "请维修车辆";
    public static final int PLEASE_AEPAIR_THE_VEHICLE_CONTENT_ID = 4124;
    public static final String PLEASE_COLOSE_THE_DOOR_CONTENT = "请关闭车门";
    public static final int PLEASE_COLOSE_THE_DOOR_CONTENT_ID = 4121;
    public static final String PLEASE_LOW_TEMPERATURE_FREEZING_CONTENT = "请注意路面结冰";
    public static final int PLEASE_LOW_TEMPERATURE_FREEZING_CONTENT_ID = 4122;
    public static final String PLEASE_MIND_VEHICLE_MAINTENANCE_CONTENT = "请注意车辆保养";
    public static final int PLEASE_MIND_VEHICLE_MAINTENANCE_CONTENT_ID = 4120;
    public static final String PLEASE_RELEASE_PARKING_BRAKE_CONTENT = "请松开手刹";
    public static final int PLEASE_RELEASE_PARKING_BRAKE_CONTENT_ID = 4125;
    public static final String PLEASE_REMOVE_THE_KEY_FROM_THE_IGNITION_CONTENT = "请拔出钥匙";
    public static final int PLEASE_REMOVE_THE_KEY_FROM_THE_IGNITION_CONTENT_ID = 4126;
    public static final String PLEASE_TOP_UP_ENGINE_OIL_LEVEL_CONTENT = "请加注机油";
    public static final int PLEASE_TOP_UP_ENGINE_OIL_LEVEL_CONTENT_ID = 4123;
    public static final String PLEASE_TURN_OFF_THE_POWER_CONTENT = "请关闭电源";
    public static final int PLEASE_TURN_OFF_THE_POWER_CONTENT_ID = 4119;
    public static final int REALITY_WARNING_BASE_ID = 4096;
    public static final String SMART_KEY_ELECTRIC_LOW_SPEEK_CONTENT = "智能钥匙电量低";
    public static final int SMART_KEY_ELECTRIC_LOW_SPEEK_CONTENT_ID = 4113;
    public static final String SMART_KEY_LEFT_CAR_SPEEK_CONTENT = "智能钥匙丢在车里了";
    public static final int SMART_KEY_LEFT_CAR_SPEEK_CONTENT_ID = 4115;
    public static final String SMART_KEY_TO_BE_TOKEN_SPEEK_CONTENT = "智能钥匙正在被带走了";
    public static final int SMART_KEY_TO_BE_TOKEN_SPEEK_CONTENT_ID = 4114;
    public static final String START_SWITCH_ERROR_SPEEK_CONTENT = "启动开关存在故障";
    public static final int START_SWITCH_ERROR_SPEEK_CONTENT_ID = 4116;
    public static final String TCU_ESMODREQREJT_SPEEK_CONTENT = "";
    public static final int TCU_ESMODREQREJT_SPEEK_CONTENT_ID = 4107;
    public static final String TREAD_CLUTH_SPEEK_CONTENT = "请踩离合启动车子";
    public static final int TREAD_CLUTH_SPEEK_CONTENT_ID = 4110;
    private int mCarType = 0;
    private int mVehicleType = 0;
    private int mBCM_LightleftOn = -1;
    private int mBCM_HoodAjarStatus = -1;
    private int mBCM_TrunkAjarStatus = -1;
    private int mBCM_DriverDoorAjarStatus = -1;
    private int mBCM_PassengerDoorAjarStatus = -1;
    private int mBCM_RearLeftDoorAjarStatus = -1;
    private int mBCM_RearRightDoorAjarStatus = -1;
    private int mTCU_ESModReqRejt = -1;
    private int mICUVehicleDisplaySpeed = -1;
    private int mFuelLevelStatus = -1;
    private int mACU_DrySeatbeltBuckleStatus = -1;
    private int mPassengerSeatBeltSate = -1;
    private int mSeatbeltBuckleStatus = -1;
    private int mCheckSmartKey = -1;
    private int mAdjustPorNToStart = -1;
    private int mTreadCluth = -1;
    private int mAdjustPToStop = -1;
    private int mAdjustNToStop = -1;
    private int mSmartKeyElectricLow = -1;
    private int mSmartKeyToBeToken = -1;
    private int mSmartKeyLeftCar = -1;
    private int mStartSwitchError = -1;
    private int mCloseCarDoorToLockVehicle = -1;
    private int mEngineTemperatureHigh = -1;
    private int mPEPSPowerOn = -1;
    private int mMaintenceRemind = -1;
    private int mBonnetDoor = -1;
    private int mLoadSpace = -1;
    private int mRoadFrozen = -1;

    public int getmPassengerSeatBeltSate() {
        return this.mPassengerSeatBeltSate;
    }

    public void setmPassengerSeatBeltSate(int mPassengerSeatBeltSate) {
        this.mPassengerSeatBeltSate = mPassengerSeatBeltSate;
    }

    public int getmRoadFrozen() {
        return this.mRoadFrozen;
    }

    public void setmRoadFrozen(int mRoadFrozen) {
        this.mRoadFrozen = mRoadFrozen;
    }

    public int getmBonnetDoor() {
        return this.mBonnetDoor;
    }

    public void setmBonnetDoor(int mBonnetDoor) {
        this.mBonnetDoor = mBonnetDoor;
    }

    public int getmLoadSpace() {
        return this.mLoadSpace;
    }

    public void setmLoadSpace(int mLoadSpace) {
        this.mLoadSpace = mLoadSpace;
    }

    public int getmMaintenceRemind() {
        return this.mMaintenceRemind;
    }

    public void setmMaintenceRemind(int mMaintenceRemind) {
        this.mMaintenceRemind = mMaintenceRemind;
    }

    public int getmPEPSPowerOn() {
        return this.mPEPSPowerOn;
    }

    public void setmPEPSPowerOn(int mPEPSPowerOn) {
        this.mPEPSPowerOn = mPEPSPowerOn;
    }

    public int getmEngineTemperatureHigh() {
        return this.mEngineTemperatureHigh;
    }

    public void setmEngineTemperatureHigh(int mEngineTemperatureHigh) {
        this.mEngineTemperatureHigh = mEngineTemperatureHigh;
    }

    public int getmSeatbeltBuckleStatus() {
        return this.mSeatbeltBuckleStatus;
    }

    public void setmSeatbeltBuckleStatus(int mSeatbeltBuckleStatus) {
        this.mSeatbeltBuckleStatus = mSeatbeltBuckleStatus;
    }

    public int getmCarType() {
        return this.mCarType;
    }

    public void setmCarType(int mCarType) {
        this.mCarType = mCarType;
    }

    public int getmVehicleType() {
        return this.mVehicleType;
    }

    public void setmVehicleType(int mVehicleType) {
        this.mVehicleType = mVehicleType;
    }

    public RealityWarningInfo() {
    }

    public RealityWarningInfo(int mCarType, int mVehicleType) {
    }

    public int getmBCM_LightleftOn() {
        return this.mBCM_LightleftOn;
    }

    public void setmBCM_LightleftOn(int mBCM_LightleftOn) {
        this.mBCM_LightleftOn = mBCM_LightleftOn;
    }

    public int getmBCM_HoodAjarStatus() {
        return this.mBCM_HoodAjarStatus;
    }

    public void setmBCM_HoodAjarStatus(int mBCM_HoodAjarStatus) {
        this.mBCM_HoodAjarStatus = mBCM_HoodAjarStatus;
    }

    public int getmBCM_TrunkAjarStatus() {
        return this.mBCM_TrunkAjarStatus;
    }

    public void setmBCM_TrunkAjarStatus(int mBCM_TrunkAjarStatus) {
        this.mBCM_TrunkAjarStatus = mBCM_TrunkAjarStatus;
    }

    public int getmBCM_DriverDoorAjarStatus() {
        return this.mBCM_DriverDoorAjarStatus;
    }

    public void setmBCM_DriverDoorAjarStatus(int mBCM_DriverDoorAjarStatus) {
        this.mBCM_DriverDoorAjarStatus = mBCM_DriverDoorAjarStatus;
    }

    public int getmBCM_PassengerDoorAjarStatus() {
        return this.mBCM_PassengerDoorAjarStatus;
    }

    public void setmBCM_PassengerDoorAjarStatus(int mBCM_PassengerDoorAjarStatus) {
        this.mBCM_PassengerDoorAjarStatus = mBCM_PassengerDoorAjarStatus;
    }

    public int getmBCM_RearLeftDoorAjarStatus() {
        return this.mBCM_RearLeftDoorAjarStatus;
    }

    public void setmBCM_RearLeftDoorAjarStatus(int mBCM_RearLeftDoorAjarStatus) {
        this.mBCM_RearLeftDoorAjarStatus = mBCM_RearLeftDoorAjarStatus;
    }

    public int getmBCM_RearRightDoorAjarStatus() {
        return this.mBCM_RearRightDoorAjarStatus;
    }

    public void setmBCM_RearRightDoorAjarStatus(int mBCM_RearRightDoorAjarStatus) {
        this.mBCM_RearRightDoorAjarStatus = mBCM_RearRightDoorAjarStatus;
    }

    public int getmTCU_ESModReqRejt() {
        return this.mTCU_ESModReqRejt;
    }

    public void setmTCU_ESModReqRejt(int mTCU_ESModReqRejt) {
        this.mTCU_ESModReqRejt = mTCU_ESModReqRejt;
    }

    public int getmICUVehicleDisplacySpeed() {
        return this.mICUVehicleDisplaySpeed;
    }

    public void setmICUVehicleDisplacySpeed(int mICUVehicleDisplacySpeed) {
        this.mICUVehicleDisplaySpeed = mICUVehicleDisplacySpeed;
    }

    public int getmFuelLevelStatus() {
        return this.mFuelLevelStatus;
    }

    public void setmFuelLevelStatus(int mFuelLevelStatus) {
        this.mFuelLevelStatus = mFuelLevelStatus;
    }

    public int getmACU_DrySeatbeltBuckleStatus() {
        return this.mACU_DrySeatbeltBuckleStatus;
    }

    public void setmACU_DrySeatbeltBuckleStatus(int mACU_DrySeatbeltBuckleStatus) {
        this.mACU_DrySeatbeltBuckleStatus = mACU_DrySeatbeltBuckleStatus;
    }

    public int getmCheckSmartKey() {
        return this.mCheckSmartKey;
    }

    public void setmCheckSmartKey(int mCheckSmartKey) {
        this.mCheckSmartKey = mCheckSmartKey;
    }

    public int getmAdjustPorNToStart() {
        return this.mAdjustPorNToStart;
    }

    public void setmAdjustPorNToStart(int mAdjustPorNToStart) {
        this.mAdjustPorNToStart = mAdjustPorNToStart;
    }

    public int getmTreadCluth() {
        return this.mTreadCluth;
    }

    public void setmTreadCluth(int mTreadCluth) {
        this.mTreadCluth = mTreadCluth;
    }

    public int getmAdjustPToStop() {
        return this.mAdjustPToStop;
    }

    public void setmAdjustPToStop(int mAdjustPToStop) {
        this.mAdjustPToStop = mAdjustPToStop;
    }

    public int getmAdjustNToStop() {
        return this.mAdjustNToStop;
    }

    public void setmAdjustNToStop(int mAdjustNToStop) {
        this.mAdjustNToStop = mAdjustNToStop;
    }

    public int getmSmartKeyElectricLow() {
        return this.mSmartKeyElectricLow;
    }

    public void setmSmartKeyElectricLow(int mSmartKeyElectricLow) {
        this.mSmartKeyElectricLow = mSmartKeyElectricLow;
    }

    public int getmSmartKeyToBeToken() {
        return this.mSmartKeyToBeToken;
    }

    public void setmSmartKeyToBeToken(int mSmartKeyToBeToken) {
        this.mSmartKeyToBeToken = mSmartKeyToBeToken;
    }

    public int getmSmartKeyLeftCar() {
        return this.mSmartKeyLeftCar;
    }

    public void setmSmartKeyLeftCar(int mSmartKeyLeftCar) {
        this.mSmartKeyLeftCar = mSmartKeyLeftCar;
    }

    public int getmStartSwitchError() {
        return this.mStartSwitchError;
    }

    public void setmStartSwitchError(int mStartSwitchError) {
        this.mStartSwitchError = mStartSwitchError;
    }

    public int getmCloseCarDoorToLockVehicle() {
        return this.mCloseCarDoorToLockVehicle;
    }

    public void setmCloseCarDoorToLockVehicle(int mCloseCarDoorToLockVehicle) {
        this.mCloseCarDoorToLockVehicle = mCloseCarDoorToLockVehicle;
    }

    public static String getTTSSpeekContentByID(int id) {
        switch (id) {
            case 4097:
                return BCM_LIGHT_ON_SPEEK_CONTENT;
            case 4098:
                return BCM_HOOD_AJAR_STATUS_SPEEK_CONTENT;
            case 4099:
                return BCM_TRUNK_AJAR_STATUS_SPEEK_CONTENT;
            case 4100:
                return BCM_DRIVER_DOOR_AJAR_STATUS_SPEEK_CONTENT;
            case 4101:
                return BCM_PASSENGER_DOOR_AJAR_STATUS_SPEEK_CONTENT;
            case 4102:
                return BCM_REAR_LEFT_DOOR_AJAR_STATUS_SPEEK_CONTENT;
            case BCM_REAR_RIGHT_DOOR_AJAR_STATUS_SPEEK_CONTENT_ID /* 4103 */:
                return BCM_REAR_RIGHT_DOOR_AJAR_STATUS_SPEEK_CONTENT;
            case ICU_VEHICLE_DISPLAY_SPEED_SPEEK_CONTENT_ID /* 4104 */:
                return ICU_VEHICLE_DISPLAY_SPEED_SPEEK_CONTENT;
            case FUEL_LEVEL_STATUS_SPEEK_CONTENT_ID /* 4105 */:
                return FUEL_LEVEL_STATUS_SPEEK_CONTENT;
            case ACU_DRY_SEATBELT_BUCKLE_STATUS_SPEEK_CONTENT_ID /* 4106 */:
                return ACU_DRY_SEATBELT_BUCKLE_STATUS_SPEEK_CONTENT;
            case TCU_ESMODREQREJT_SPEEK_CONTENT_ID /* 4107 */:
                return "";
            case CHECK_SMART_KEY_SPEEK_CONTENT_ID /* 4108 */:
                return CHECK_SMART_KEY_SPEEK_CONTENT;
            case ADJUST_PORN_TO_START_SPEEK_CONTENT_ID /* 4109 */:
                return ADJUST_PORN_TO_START_SPEEK_CONTENT;
            case TREAD_CLUTH_SPEEK_CONTENT_ID /* 4110 */:
                return TREAD_CLUTH_SPEEK_CONTENT;
            case ADJUST_P_TO_STOP_SPEEK_CONTENT_ID /* 4111 */:
                return ADJUST_P_TO_STOP_SPEEK_CONTENT;
            case ADJUST_N_TO_STOP_SPEEK_CONTENT_ID /* 4112 */:
                return ADJUST_N_TO_STOP_SPEEK_CONTENT;
            case SMART_KEY_ELECTRIC_LOW_SPEEK_CONTENT_ID /* 4113 */:
                return SMART_KEY_ELECTRIC_LOW_SPEEK_CONTENT;
            case SMART_KEY_TO_BE_TOKEN_SPEEK_CONTENT_ID /* 4114 */:
                return SMART_KEY_TO_BE_TOKEN_SPEEK_CONTENT;
            case SMART_KEY_LEFT_CAR_SPEEK_CONTENT_ID /* 4115 */:
                return SMART_KEY_LEFT_CAR_SPEEK_CONTENT;
            case START_SWITCH_ERROR_SPEEK_CONTENT_ID /* 4116 */:
                return START_SWITCH_ERROR_SPEEK_CONTENT;
            case CLOSE_CAR_DOOR_TO_LOCK_VEHICLE_SPEEK_CONTENT_ID /* 4117 */:
                return CLOSE_CAR_DOOR_TO_LOCK_VEHICLE_SPEEK_CONTENT;
            default:
                return "";
        }
    }

    public static String getX37TTSSpeekContentByID(int id) {
        if (id == 4097) {
            return "请关闭灯光";
        }
        switch (id) {
            case ICU_VEHICLE_DISPLAY_SPEED_SPEEK_CONTENT_ID /* 4104 */:
                return "已超过设定车速";
            case FUEL_LEVEL_STATUS_SPEEK_CONTENT_ID /* 4105 */:
                return "燃油量低,请及时加油";
            case ACU_DRY_SEATBELT_BUCKLE_STATUS_SPEEK_CONTENT_ID /* 4106 */:
                return "安全带未系";
            default:
                switch (id) {
                    case ENGINE_TEMPERATURE_HIGH_CONTENT_ID /* 4118 */:
                        return "发动机温度过高,请停车";
                    case PLEASE_TURN_OFF_THE_POWER_CONTENT_ID /* 4119 */:
                        return PLEASE_TURN_OFF_THE_POWER_CONTENT;
                    case PLEASE_MIND_VEHICLE_MAINTENANCE_CONTENT_ID /* 4120 */:
                        return PLEASE_MIND_VEHICLE_MAINTENANCE_CONTENT;
                    case PLEASE_COLOSE_THE_DOOR_CONTENT_ID /* 4121 */:
                        return PLEASE_COLOSE_THE_DOOR_CONTENT;
                    case PLEASE_LOW_TEMPERATURE_FREEZING_CONTENT_ID /* 4122 */:
                        return PLEASE_LOW_TEMPERATURE_FREEZING_CONTENT;
                    case PLEASE_TOP_UP_ENGINE_OIL_LEVEL_CONTENT_ID /* 4123 */:
                        return PLEASE_TOP_UP_ENGINE_OIL_LEVEL_CONTENT;
                    case PLEASE_AEPAIR_THE_VEHICLE_CONTENT_ID /* 4124 */:
                        return PLEASE_AEPAIR_THE_VEHICLE_CONTENT;
                    case PLEASE_RELEASE_PARKING_BRAKE_CONTENT_ID /* 4125 */:
                        return PLEASE_RELEASE_PARKING_BRAKE_CONTENT;
                    case PLEASE_REMOVE_THE_KEY_FROM_THE_IGNITION_CONTENT_ID /* 4126 */:
                        return PLEASE_RELEASE_PARKING_BRAKE_CONTENT;
                    default:
                        return "";
                }
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mBCM_LightleftOn);
        dest.writeInt(this.mBCM_HoodAjarStatus);
        dest.writeInt(this.mBCM_TrunkAjarStatus);
        dest.writeInt(this.mBCM_DriverDoorAjarStatus);
        dest.writeInt(this.mBCM_PassengerDoorAjarStatus);
        dest.writeInt(this.mBCM_RearLeftDoorAjarStatus);
        dest.writeInt(this.mBCM_RearRightDoorAjarStatus);
        dest.writeInt(this.mTCU_ESModReqRejt);
        dest.writeInt(this.mICUVehicleDisplaySpeed);
        dest.writeInt(this.mFuelLevelStatus);
        dest.writeInt(this.mACU_DrySeatbeltBuckleStatus);
        dest.writeInt(this.mCheckSmartKey);
        dest.writeInt(this.mAdjustPorNToStart);
        dest.writeInt(this.mTreadCluth);
        dest.writeInt(this.mAdjustPToStop);
        dest.writeInt(this.mAdjustNToStop);
        dest.writeInt(this.mSmartKeyElectricLow);
        dest.writeInt(this.mSmartKeyToBeToken);
        dest.writeInt(this.mSmartKeyLeftCar);
        dest.writeInt(this.mStartSwitchError);
        dest.writeInt(this.mCloseCarDoorToLockVehicle);
        dest.writeInt(this.mCarType);
        dest.writeInt(this.mVehicleType);
        dest.writeInt(this.mEngineTemperatureHigh);
        dest.writeInt(this.mPEPSPowerOn);
        dest.writeInt(this.mMaintenceRemind);
        dest.writeInt(this.mBonnetDoor);
        dest.writeInt(this.mLoadSpace);
        dest.writeInt(this.mRoadFrozen);
        dest.writeInt(this.mSeatbeltBuckleStatus);
        dest.writeInt(this.mPassengerSeatBeltSate);
    }

    public String toString() {
        return "RealityWarningInfo [mCarType=" + this.mCarType + ", mVehicleType=" + this.mVehicleType + ", mBCM_LightleftOn=" + this.mBCM_LightleftOn + ", mBCM_HoodAjarStatus=" + this.mBCM_HoodAjarStatus + ", mBCM_TrunkAjarStatus=" + this.mBCM_TrunkAjarStatus + ", mBCM_DriverDoorAjarStatus=" + this.mBCM_DriverDoorAjarStatus + ", mBCM_PassengerDoorAjarStatus=" + this.mBCM_PassengerDoorAjarStatus + ", mBCM_RearLeftDoorAjarStatus=" + this.mBCM_RearLeftDoorAjarStatus + ", mBCM_RearRightDoorAjarStatus=" + this.mBCM_RearRightDoorAjarStatus + ", mTCU_ESModReqRejt=" + this.mTCU_ESModReqRejt + ", mICUVehicleDisplaySpeed=" + this.mICUVehicleDisplaySpeed + ", mFuelLevelStatus=" + this.mFuelLevelStatus + ", mACU_DrySeatbeltBuckleStatus=" + this.mACU_DrySeatbeltBuckleStatus + ", mSeatbeltBuckleStatus=" + this.mSeatbeltBuckleStatus + ", mCheckSmartKey=" + this.mCheckSmartKey + ", mAdjustPorNToStart=" + this.mAdjustPorNToStart + ", mTreadCluth=" + this.mTreadCluth + ", mAdjustPToStop=" + this.mAdjustPToStop + ", mAdjustNToStop=" + this.mAdjustNToStop + ", mSmartKeyElectricLow=" + this.mSmartKeyElectricLow + ", mSmartKeyToBeToken=" + this.mSmartKeyToBeToken + ", mSmartKeyLeftCar=" + this.mSmartKeyLeftCar + ", mStartSwitchError=" + this.mStartSwitchError + ", mCloseCarDoorToLockVehicle=" + this.mCloseCarDoorToLockVehicle + ", mEngineTemperatureHigh=" + this.mEngineTemperatureHigh + ", mPEPSPowerOn=" + this.mPEPSPowerOn + ", mMaintenceRemind=" + this.mMaintenceRemind + ", mBonnetDoor=" + this.mBonnetDoor + ", mLoadSpace=" + this.mLoadSpace + ", mRoadFrozen=" + this.mRoadFrozen + "]";
    }
}

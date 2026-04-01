package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

import com.qinggan.canbus.plugs.VoiceActionID;

public enum VehicleCenterControlEnabled implements Parcelable {
    ASREnabled(39),
    TyresSpeedWarningEnabled(40),
    TyresSpeedEnabled(41),
    TyrePressureMonitoringEnabled(42),
    AdaptiveCruiseControlSystemEnabled(43),
    DriverAlertSystemEnabled(44),
    LastVehicleDistanceEnabled(45),
    FrontAssistSetEnabled(46),
    PrevWarningEnabled(47),
    DisplayDistanceWarningEnabled(48),
    LaneKeepingAssistEnabled(49),
    TravelProgrammeEnabled(50),
    VehicleDistanceEnabled(51),
    ParkingAutoActiveEnabled(52),
    ParkingFrontVolumeEnabled(53),
    ParkingFrontToneEnabled(54),
    ParkingBackVolumeEnabled(55),
    ParkingBackToneEnabled(56),
    LightOpenedTimeEnabled(58),
    AutomaticHeadlightControlEnabled(59),
    LaneChangeFlashEnabled(60),
    InstrumentSwitchLightingEnabled(61),
    ComingHomeFunctionEnabled(62),
    LeavingHomeFunctionEnabled(63),
    TravelModeEnabled(64),
    DoorAmbientLightEnabled(65),
    FootWellLightEnabled(66),
    DynamicBigLightEnabled(67),
    SynchronousAdjustmentEnabled(69),
    LowerWhileReversingEnabled(70),
    AutomaticWipingInRainEnabled(71),
    RearWindowWipingInReverseGearEnabled(72),
    ZhuCheShiNeiZheEnabled(73),
    ConvenienceOpeningEnabled(74),
    DoorUnlockingEnabled(75),
    AutomaticLockingEnabled(76),
    DaytimeRunninglightsEnabled(77),
    DynamicBigLightAssistanceEnabled(78),
    FRSEnabled(130),
    FCSEnabled(131),
    Camera360(132),
    RearCamera(133),
    SWCButtonEnabled(134),
    IPKInteractiveEnabled(135),
    TBOXEnabled(135),
    CVBSDVREnabled(136),
    WIFIDVREnabled(137),
    AirConditionBeltPollenFiltrationEnables(138),
    PM2Dot5AirPurificationEnable(139),
    AQSSystemEnbaled(140),
    LDWSystemEnabled(141),
    BSDSystemEnabled(142),
    ParkAssistSystemEnabled(143),
    LowSpeedDynamicObjectRecognitionEnabled(VoiceActionID.ACTION_DCS_LOCAL_MUSIC_PLAY),
    CentralControlAtmosphereLampEnabled(VoiceActionID.ACTION_DCS_LAUNCH_APP),
    Camera360ReversingEnabled(VoiceActionID.ACTION_DCS_OPEN_STYLE_MUSIC),
    RearCameraReversingEnabled(VoiceActionID.ACTION_DCS_MUSIC_SWITCH_MODE),
    FrontReversingRadarEnabled(VoiceActionID.ACTION_DCS_MUSIC_JUST_LISTEN),
    OutMirrorAutoFoldEnabled(VoiceActionID.ACTION_DCS_CAR_CARE_ALERT),
    FollowMeHomeEnabled(VoiceActionID.ACTION_DCS_CUSTOM_MUSIC_PREV),
    PedestrianPreCollisionWarningEnabled(VoiceActionID.ACTION_DCS_CUSTOM_MUSIC_NEXT),
    AEBPreCollisionWarningEnabled(VoiceActionID.ACTION_DCS_CUSTOM_AIRCONDITIONER_ADJUST_RISE),
    AEBSystemEnabled(VoiceActionID.ACTION_DCS_CUSTOM_AIRCONDITIONER_ADJUST_REDUCE),
    FCWSystemEnabled(VoiceActionID.ACTION_EXIT_OR_CANCEL_NAV),
    SpeakerConfigEnabled(VoiceActionID.ACTION_DCS_CAR_BLUETOOTH_CONTROL),
    WormFunctionEnbaled(VoiceActionID.ACTION_DCS_CAR_ADJUST_BRIGHTNESS),
    SinglePedalFunctionEnabled(VoiceActionID.ACTION_DCS_CUSTOM_CLOSE_MUSIC),
    ChairHeat(VoiceActionID.ACTION_DCS_CAR_BLUETOOTH_CONNECT),
    AirConditionType(VoiceActionID.ACTION_DCS_CAR_WIFI_HOTSPOT),
    V2VV2LCharge(VoiceActionID.ACTION_DCS_CAR_WIFI_CONTROL),
    VoiceRec(VoiceActionID.ACTION_DCS_CAR_SET_BRIGHTNESS);

    public static final Parcelable.Creator<VehicleCenterControlEnabled> CREATOR = new Parcelable.Creator<VehicleCenterControlEnabled>() { // from class: com.qinggan.canbus.VehicleCenterControlEnabled.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public VehicleCenterControlEnabled createFromParcel(Parcel source) {
            VehicleCenterControlEnabled state = VehicleCenterControlEnabled.values()[source.readInt()];
            state.value = source.readInt();
            return state;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public VehicleCenterControlEnabled[] newArray(int size) {
            return new VehicleCenterControlEnabled[size];
        }
    };
    public static final int DISABLED = 0;
    public static final int ENABLED = 1;
    private int value;

    VehicleCenterControlEnabled(int value) {
        this.value = value;
    }

    public int getEnabled() {
        return this.value;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
        dest.writeInt(this.value);
    }
}

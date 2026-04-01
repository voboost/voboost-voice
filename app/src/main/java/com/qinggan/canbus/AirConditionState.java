package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

/* loaded from: classes.dex */
public enum AirConditionState implements Parcelable {
    AC_BLOWER(1),
    HVAC_VD_MODE(2),
    AC_LEFT_TEMP(3),
    AC_RIGHT_TEMP(4),
    AC_SWITCH(5),
    AC_MAX_SWITCH(6),
    AC_AUTO(7),
    AC_POWER_SWITCH(8),
    AC_FRONT_DEFROST_SWITCH(9),
    AC_REAR_DEFROST_SWITCH(10),
    AC_RECIRC_AIR(11),
    AC_DUAL(12),
    AC_FLOW_MODE(13),
    AC_TEMP_DEM(14),
    AC_HEAT_SWITCH(15),
    AC_TEMP_DEM_ADD(16),
    AC_TEMP_DEM_DEC(17),
    AC_BLOWER_DEM_ADD(18),
    AC_BLOWER_DEM_DEC(19),
    AC_ION_SWITCH(23),
    AC_PM_SWITCH(24),
    AC_AQS_SWITCH(25),
    AC_TEMP_OUTCAR(20),
    AC_PM2_5(21),
    AC_RUNNING_STATE(22),
    AC_COMBINATION_FUNCTION_1(23),
    AC_COMBINATION_FUNCTION_2(24),
    AC_COMBINATION_FUNCTION_3(25),
    AC_COMBINATION_FUNCTION_4(26),
    AC_COMBINATION_FUNCTION_5(27),
    AC_COMBINATION_FUNCTION_6(28),
    AC_COMBINATION_FUNCTION_7(29),
    AC_COMBINATION_FUNCTION_8(30),
    AC_RAPID_COOLING_MODE(31),
    AC_ONE_BUTTON_WARMTH_MODE(32),
    AC_HAZE_MODE(33),
    AC_BABY_CARE_MODE(34),
    AC_AIR_CLEANER(35),
    AC_RAIN_SNOW_MODE(36),
    AC_SMOKING_MODE(37),
    AC_STOP_CAR_MODE(38),
    AC_DUAL_TC(39),
    AC_AUTO_TC(40),
    AC_AIR_INTER_TC(41),
    AC_AC_COMPR_TC(42),
    AC_FRONT_DEFORST_TC(43),
    AC_FRONT_BLOWER_DECRE_TC(44),
    AC_FRONT_BLOWER_INCRE_TC(45),
    AC_FLOW_MODE_TC(46),
    AC_DRIVER_TEMP_DECRE_TC(47),
    AC_DRIVER_TEMP_INCRE_TC(48),
    AC_PASSENGER_TEMP_DECRE_TC(49),
    AC_PASSENGER_TEMP_INCRE_TC(50),
    AC_FEEDBACK_DISPALY_STATUS(51),
    AC_OFF_REQUEST_TC(52),
    AC_AUTO_VR(53),
    AC_AC_COMPR_VR(54),
    AC_POWER_REQUEST_VR(55),
    AC_FRONT_BLOWER_SPEED_VR(56),
    AC_DRIVER_TEMP_REQUEST_VR(57),
    AC_FRONT_DEFORST_VR(58),
    AC_FLOW_MODE_VR(59),
    AC_AIR_INTER_VR(60),
    AC_REAR_DEFORST_VR(61),
    AC_PASSENGER_TEMP_REQUEST_VR(62),
    AC_DUAL_VR(63),
    AC_LEFT_TEMP_F(64),
    AC_RIGHT_TEMP_F(65),
    AC_WORKING_MODE(66),
    VENT_COOL_SET(67),
    AC_AUTO_FRONT(68),
    AC_AUTO_REAR(69),
    AC_POWER_SWITCH_FRONT(70),
    AC_POWER_SWITCH_REAR(71),
    AC_FLOW_MODE_FRONT(72),
    AC_FLOW_MODE_REAR(73),
    AC_BLOWER_FRONT(74),
    AC_BLOWER_REAR(75),
    AC_REAR_TEMP(76),
    AC_DUAL_ALL(77),
    AC_DOR_SWITCH(78),
    AC_DOR_MODE(79);

    public static final int ACTIVE = 2;
    public static final int AC_DOR_MODE_DEEP = 2;
    public static final int AC_DOR_MODE_STANDARD = 1;
    public static final int AC_PM_SWITCH_MEASUREING = 1;
    public static final int AC_PM_SWITCH_STANDBY = 0;
    public static final int AC_WORKING_MODE_INVALIDE = 0;
    public static final int AC_WORKING_MODE_NORMAL = 2;
    public static final int AC_WORKING_MODE_SOFT = 1;
    public static final int AC_WORKING_MODE_STRONG = 3;
    public static final int AUTO_BLOWING_IN = 0;
    public static final int CLOSE = 1;
    public static final Parcelable.Creator<AirConditionState> CREATOR = new Parcelable.Creator<AirConditionState>() { // from class: com.qinggan.canbus.AirConditionState.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public AirConditionState createFromParcel(Parcel source) {
            AirConditionState state = AirConditionState.values()[source.readInt()];
            state.state = source.readInt();
            return state;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public AirConditionState[] newArray(int size) {
            return new AirConditionState[size];
        }
    };
    public static final int DEFAULT_LOOP = 0;
    public static final int DEFORST_AND_TO_DOWN_BLOWING_IN = 18;
    public static final int EXTERNAL_LOOP = 2;
    public static final int FLOW_MODE_DEF = 4;
    public static final int FLOW_MODE_FACE = 0;
    public static final int FLOW_MODE_FACE_DEF = 5;
    public static final int FLOW_MODE_FACE_LEG = 1;
    public static final int FLOW_MODE_FACE_LEG_DEF = 6;
    public static final int FLOW_MODE_LEG = 2;
    public static final int FLOW_MODE_LEG_DEF = 3;
    public static final int FLOW_MODE_OF_DEF = 5;
    public static final int FLOW_MODE_OF_FACE = 1;
    public static final int FLOW_MODE_OF_FACE_LEG = 2;
    public static final int FLOW_MODE_OF_LEG = 3;
    public static final int FLOW_MODE_OF_LEG_DEF = 4;
    public static final int FRESH = 1;
    public static final int FRONT_GLASS_BLOWING_IN = 1;
    public static final int INACTIVE = 1;
    public static final int INTERNAL_LOOP = 1;
    public static final int INVAILD_LOOP = 3;
    public static final int LEVEL_1 = 1;
    public static final int LEVEL_2 = 2;
    public static final int LEVEL_3 = 3;
    public static final int LEVEL_4 = 4;
    public static final int LEVEL_5 = 5;
    public static final int LEVEL_6 = 6;
    public static final int LEVEL_7 = 7;
    public static final int LEVEL_8 = 8;
    public static final int LEVEL_NO = 0;
    public static final int NO_ACTION = 0;
    public static final int OPEN = 2;
    public static final int PARALLEL_AND_TO_UP_BLOWING_IN = 12;
    public static final int PARALLEL_BLOWING_IN = 4;
    public static final int POWER_OFF = 1;
    public static final int POWER_ON = 2;
    public static final int PRESSED = 1;
    public static final int RECIRCULATION = 2;
    public static final int SWITCH_OFF = 0;
    public static final int SWITCH_ON = 1;
    public static final int TEMP_HI = 32;
    public static final int TEMP_LOW = 16;
    public static final int TO_DOWN_AND_PARALLEL_BLOWING_IN = 6;
    public static final int TO_DOWN_BLOWING_IN = 2;
    public static final int TO_UP_AND_PARALLEL_AND_TO_DOWN_BLOWING_IN = 14;
    public static final int TO_UP_AND_TO_DOWN_BLOWING_IN = 10;
    public static final int TO_UP_BLOWING_IN = 8;
    private int state;

    AirConditionState(int state) {
        this.state = state;
    }

    public int getState() {
        return this.state;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
        dest.writeInt(this.state);
    }
}

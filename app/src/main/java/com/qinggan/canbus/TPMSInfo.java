package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

import com.qinggan.canbus.plugs.CommandExecution;

public class TPMSInfo implements Parcelable {
    public static final Parcelable.Creator<TPMSInfo> CREATOR = new Parcelable.Creator<TPMSInfo>() { // from class: com.qinggan.canbus.TPMSInfo.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public TPMSInfo createFromParcel(Parcel source) {
            TPMSInfo tpmsInfo = new TPMSInfo();
            tpmsInfo.mTirePositionStatus = source.readInt();
            tpmsInfo.mTemperatureWarningStatus = source.readInt();
            tpmsInfo.mSystemWaringStatus = source.readInt();
            tpmsInfo.mLeftFrontTireWarningStatus = source.readInt();
            tpmsInfo.mRightFrontTireWarningStatus = source.readInt();
            tpmsInfo.mLeftRearTireWarningStatus = source.readInt();
            tpmsInfo.mRightRearTireWarningStatus = source.readInt();
            tpmsInfo.mLeftFrontTirePressureValue = source.readFloat();
            tpmsInfo.mRightFrontTirePressureValue = source.readFloat();
            tpmsInfo.mLeftRearTirePressureValue = source.readFloat();
            tpmsInfo.mRightRearTirePressureValue = source.readFloat();
            return tpmsInfo;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public TPMSInfo[] newArray(int size) {
            return new TPMSInfo[size];
        }
    };
    public static final int PRESSURE_HIGH_PRESSURE_WARNING = 1;
    public static final int PRESSURE_LOST_SENSOR_WARNING = 4;
    public static final int PRESSURE_LOW_PRESSURE_WARNING = 2;
    public static final int PRESSURE_NO_WARNING = 0;
    public static final int PRESSURE_QUIK_LEAKAGE_WARNING = 3;
    public static final int PRESSURE_SENSOR_BATTERY_LOW_WARNING = 5;
    public static final int PRESSURE__SENSOR_FAILURE_WARNING = 6;
    public static final int SYSTEM_CHECKCOMPLETED = 3;
    public static final int SYSTEM_CHECKING = 2;
    public static final int SYSTEM_ERROR = 1;
    public static final int SYSTEM_NO_ANY_ERROR = 0;
    public static final int TEMPERATURE_NO_WARNING = 0;
    public static final int TEMPERATURE_WARNING = 1;
    public static final int TIRE_POSITION_LEFT_FRONT = 1;
    public static final int TIRE_POSITION_LEFT_REAR = 4;
    public static final int TIRE_POSITION_NO_ANY_SENSOR = 0;
    public static final int TIRE_POSITION_RIGHT_FRONT = 2;
    public static final int TIRE_POSITION_RIGHT_REAR = 3;
    public int mTirePositionStatus = -1;
    public int mTemperatureWarningStatus = -1;
    public int mSystemWaringStatus = -1;
    public int mLeftFrontTireWarningStatus = -1;
    public int mRightFrontTireWarningStatus = -1;
    public int mLeftRearTireWarningStatus = -1;
    public int mRightRearTireWarningStatus = -1;
    public float mLeftFrontTirePressureValue = -1.0f;
    public float mRightFrontTirePressureValue = -1.0f;
    public float mLeftRearTirePressureValue = -1.0f;
    public float mRightRearTirePressureValue = -1.0f;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mTirePositionStatus);
        dest.writeInt(this.mTemperatureWarningStatus);
        dest.writeInt(this.mSystemWaringStatus);
        dest.writeInt(this.mLeftFrontTireWarningStatus);
        dest.writeInt(this.mRightFrontTireWarningStatus);
        dest.writeInt(this.mLeftRearTireWarningStatus);
        dest.writeInt(this.mRightRearTireWarningStatus);
        dest.writeFloat(this.mLeftFrontTirePressureValue);
        dest.writeFloat(this.mRightFrontTirePressureValue);
        dest.writeFloat(this.mLeftRearTirePressureValue);
        dest.writeFloat(this.mRightRearTirePressureValue);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("dispinfo Setting :===============\n");
        sb.append("mTirePositionStatus: " + this.mTirePositionStatus + CommandExecution.COMMAND_LINE_END);
        sb.append("mTemperatureWarningStatus:" + this.mTemperatureWarningStatus + CommandExecution.COMMAND_LINE_END);
        sb.append("mSystemWaringStatus: " + this.mSystemWaringStatus + CommandExecution.COMMAND_LINE_END);
        sb.append("mLeftFrontTireWarningStatus:" + this.mLeftFrontTireWarningStatus + CommandExecution.COMMAND_LINE_END);
        sb.append("mRightFrontTireWarningStatus:" + this.mRightFrontTireWarningStatus + CommandExecution.COMMAND_LINE_END);
        sb.append("mLeftRearTireWarningStatus: " + this.mLeftRearTireWarningStatus + CommandExecution.COMMAND_LINE_END);
        sb.append("mRightRearTireWarningStatus:" + this.mRightRearTireWarningStatus + CommandExecution.COMMAND_LINE_END);
        sb.append("mLeftFrontTirePressureValue: " + this.mLeftFrontTirePressureValue + CommandExecution.COMMAND_LINE_END);
        sb.append("mRightFrontTirePressureValue: " + this.mRightFrontTirePressureValue + CommandExecution.COMMAND_LINE_END);
        sb.append("mLeftRearTirePressureValue: " + this.mLeftRearTirePressureValue + CommandExecution.COMMAND_LINE_END);
        sb.append("mRightRearTirePressureValue: " + this.mRightRearTirePressureValue + CommandExecution.COMMAND_LINE_END);
        return sb.toString();
    }
}

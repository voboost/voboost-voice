package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

/* loaded from: classes.dex */
public final class BatteryState implements Parcelable {
    public static final Parcelable.Creator<BatteryState> CREATOR = new Parcelable.Creator<BatteryState>() { // from class: com.qinggan.canbus.BatteryState.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public BatteryState[] newArray(int size) {
            return new BatteryState[size];
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public BatteryState createFromParcel(Parcel source) {
            BatteryState BatteryState = new BatteryState();
            BatteryState.mVoltageLevel = source.readInt();
            BatteryState.mVoltageValue = source.readInt();
            BatteryState.mCurrentValue = source.readFloat();
            return BatteryState;
        }
    };
    public static final int UNKNOWN = -1;
    public static final int VOLTAGE_ERROR = 1023;
    public static final int VOLTAGE_INIT = 1022;
    public static final int VOLTAGE_MAX = 1021;
    public static final int VOLTAGE_MIN = 0;
    public static final int VOLTAGE_UNKNOWN = Integer.MIN_VALUE;
    private int mVoltageLevel = -1;
    private int mVoltageValue = -1;
    private float mCurrentValue = -1.0f;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mVoltageLevel);
        dest.writeInt(this.mVoltageValue);
        dest.writeFloat(this.mCurrentValue);
    }

    public void setBatteryVoltage(int voltageValue) {
        this.mVoltageValue = voltageValue;
    }

    public int getBatteryVoltage() {
        return this.mVoltageValue;
    }

    public void setBatteryVoltageLevel(int voltageLevel) {
        this.mVoltageLevel = voltageLevel;
    }

    public int getBatteryVoltageLevel() {
        return this.mVoltageLevel;
    }

    public float getBatteryCurrentValue() {
        return this.mCurrentValue;
    }

    public void setBatteryCurrentValue(float currentValue) {
        this.mCurrentValue = currentValue;
    }

    public String toString() {
        return "BatteryState{mVoltageLevel=" + this.mVoltageLevel + "mVoltageValue=" + this.mVoltageValue + "mCurrentValue=" + this.mCurrentValue + '}';
    }
}

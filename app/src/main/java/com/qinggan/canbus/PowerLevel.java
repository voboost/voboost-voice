package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

public final class PowerLevel implements Parcelable {
    public static final Parcelable.Creator<PowerLevel> CREATOR = new Parcelable.Creator<PowerLevel>() { // from class: com.qinggan.canbus.PowerLevel.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public PowerLevel[] newArray(int size) {
            return new PowerLevel[size];
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public PowerLevel createFromParcel(Parcel source) {
            PowerLevel dest = new PowerLevel();
            dest.mCapacity = source.readFloat();
            dest.mRemain = source.readFloat();
            dest.mPercentage = source.readFloat();
            dest.mInstantaneousPowerConsumption = source.readFloat();
            dest.mAvgPowerConsumption = source.readFloat();
            dest.mHistoryAvgPowerConsumption = source.readFloat();
            return dest;
        }
    };
    public static final int UNKNOWN = 0;
    public float mCapacity = 0.0f;
    public float mRemain = 0.0f;
    public float mPercentage = 0.0f;
    public float mInstantaneousPowerConsumption = 0.0f;
    public float mAvgPowerConsumption = 0.0f;
    public float mHistoryAvgPowerConsumption = 0.0f;

    public int getCapacity() {
        return (int) this.mCapacity;
    }

    public void setCapacity(int capacity) {
        this.mCapacity = capacity;
    }

    public int getRemain() {
        return (int) this.mRemain;
    }

    public void setRemain(int remain) {
        this.mRemain = remain;
    }

    public float getPercentage() {
        return this.mPercentage;
    }

    public void setPercentage(float percentage) {
        this.mPercentage = percentage;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.mCapacity);
        dest.writeFloat(this.mRemain);
        dest.writeFloat(this.mPercentage);
        dest.writeFloat(this.mInstantaneousPowerConsumption);
        dest.writeFloat(this.mAvgPowerConsumption);
        dest.writeFloat(this.mHistoryAvgPowerConsumption);
    }

    public String toString() {
        return "PowerLevel{mCapacity=" + this.mCapacity + ", mRemain=" + this.mRemain + ", mPercentage=" + this.mPercentage + ", mInstantaneousPowerConsumption=" + this.mInstantaneousPowerConsumption + ", mAvgPowerConsumption=" + this.mAvgPowerConsumption + ", mHistoryAvgPowerConsumption=" + this.mHistoryAvgPowerConsumption + '}';
    }
}

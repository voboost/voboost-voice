package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

public final class FuelLevel implements Parcelable {
    public static final Parcelable.Creator<FuelLevel> CREATOR = new Parcelable.Creator<FuelLevel>() { // from class: com.qinggan.canbus.FuelLevel.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public FuelLevel[] newArray(int size) {
            return new FuelLevel[size];
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public FuelLevel createFromParcel(Parcel source) {
            FuelLevel dest = new FuelLevel();
            dest.mCapacity = source.readInt();
            dest.mRemain = source.readInt();
            dest.mPercentage = source.readFloat();
            dest.isFuelShortage = source.readInt() == 1;
            dest.mInstantaneousFuelConsumption = source.readFloat();
            dest.mAvgFuelConsumption = source.readFloat();
            dest.mHistoryAvgFuelConsumption = source.readFloat();
            return dest;
        }
    };
    public static final int UNKNOWN = 0;
    public int mCapacity = 0;
    public int mRemain = 0;
    public float mPercentage = 0.0f;
    private boolean isFuelShortage = false;
    public float mInstantaneousFuelConsumption = 0.0f;
    public float mAvgFuelConsumption = 0.0f;
    public float mHistoryAvgFuelConsumption = 0.0f;

    public int getCapacity() {
        return this.mCapacity;
    }

    public void setCapacity(int capacity) {
        this.mCapacity = capacity;
    }

    public int getRemain() {
        return this.mRemain;
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

    public boolean isFuelShortage() {
        return this.isFuelShortage;
    }

    public void setFuelShortage(boolean isFuelShortage) {
        this.isFuelShortage = isFuelShortage;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mCapacity);
        dest.writeInt(this.mRemain);
        dest.writeFloat(this.mPercentage);
        dest.writeInt(!this.isFuelShortage ? 0 : 1);
        dest.writeFloat(this.mInstantaneousFuelConsumption);
        dest.writeFloat(this.mAvgFuelConsumption);
        dest.writeFloat(this.mHistoryAvgFuelConsumption);
    }

    public String toString() {
        return "FuelLevel{mCapacity=" + this.mCapacity + ", mRemain=" + this.mRemain + ", mPercentage=" + this.mPercentage + ", isFuelShortage=" + this.isFuelShortage + ", mInstantaneousFuelConsumption=" + this.mInstantaneousFuelConsumption + ", mAvgFuelConsumption=" + this.mAvgFuelConsumption + ", mHistoryAvgFuelConsumption=" + this.mHistoryAvgFuelConsumption + '}';
    }
}

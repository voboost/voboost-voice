package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

public final class Odometer implements Parcelable {
    public static final Parcelable.Creator<Odometer> CREATOR = new Parcelable.Creator<Odometer>() { // from class: com.qinggan.canbus.Odometer.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public Odometer[] newArray(int size) {
            return new Odometer[size];
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public Odometer createFromParcel(Parcel source) {
            Odometer dest = new Odometer();
            dest.mCanTravelMileage = source.readInt();
            dest.mBeenTravelingMileage = source.readInt();
            dest.mEVCruisingRange = source.readInt();
            dest.mEnduranceMileage = source.readInt();
            dest.mPowerConsumption = source.readFloat();
            dest.mChargeReaminTime = source.readInt();
            dest.mTotalTravelingMileage = source.readFloat();
            dest.mEVCruisingRang_real = source.readInt();
            dest.mCanTravelMileage_real = source.readInt();
            dest.mEVCruisingRange_wltc = source.readInt();
            return dest;
        }
    };
    public static final int UNKNOWN = 0;
    public int mCanTravelMileage = 0;
    public int mBeenTravelingMileage = 0;
    private float mTotalTravelingMileage = 0.0f;
    public int mEVCruisingRange = 0;
    public int mChargeReaminTime = 0;
    public int mEnduranceMileage = 0;
    public float mPowerConsumption = Float.MIN_VALUE;
    public int mEVCruisingRang_real = 0;
    public int mCanTravelMileage_real = 0;
    public int mEVCruisingRange_wltc = 0;

    public int getChargeReaminTime() {
        return this.mChargeReaminTime;
    }

    public void setChargeReaminTime(int chargeReaminTime) {
        this.mChargeReaminTime = chargeReaminTime;
    }

    public int getmEVCruisingRange() {
        return this.mEVCruisingRange;
    }

    public void setmEVCruisingRange(int mEVCruisingRange) {
        this.mEVCruisingRange = mEVCruisingRange;
    }

    public double getmPowerConsumption() {
        return this.mPowerConsumption;
    }

    public void setmPowerConsumption(float mPowerConsumption) {
        this.mPowerConsumption = mPowerConsumption;
    }

    public int getCanTravelMileage() {
        return this.mCanTravelMileage;
    }

    public void setCanTravelMileage(int canTravelMileage) {
        this.mCanTravelMileage = canTravelMileage;
    }

    public int getBeenTravelingMileage() {
        return this.mBeenTravelingMileage;
    }

    public void setBeenTravelingMileage(int beenTravelingMileage) {
        this.mBeenTravelingMileage = beenTravelingMileage;
    }

    public int getEnduranceMileage() {
        return this.mEnduranceMileage;
    }

    public void setEnduranceMileage(int mEnduranceMileage) {
        this.mEnduranceMileage = mEnduranceMileage;
    }

    public float getTotalTravelingMileage() {
        return this.mTotalTravelingMileage;
    }

    public void setTotalTravelingMileage(float mTotalTravelingMileage) {
        this.mTotalTravelingMileage = mTotalTravelingMileage;
    }

    public int getmEVCruisingRange_real() {
        return this.mEVCruisingRang_real;
    }

    public void setmEVCruisingRange_real(int mEVCruisingRang_real) {
        this.mEVCruisingRang_real = mEVCruisingRang_real;
    }

    public int getCanTravelMileage_real() {
        return this.mCanTravelMileage_real;
    }

    public void setCanTravelMileage_real(int canTravelMileage_real) {
        this.mCanTravelMileage_real = canTravelMileage_real;
    }

    public int getEVCruisingRange_wltc() {
        return this.mEVCruisingRange_wltc;
    }

    public void setEVCruisingRange_wltc(int evCruisingRange_wltc) {
        this.mEVCruisingRange_wltc = evCruisingRange_wltc;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mCanTravelMileage);
        dest.writeInt(this.mBeenTravelingMileage);
        dest.writeInt(this.mEVCruisingRange);
        dest.writeInt(this.mEnduranceMileage);
        dest.writeFloat(this.mPowerConsumption);
        dest.writeInt(this.mChargeReaminTime);
        dest.writeFloat(this.mTotalTravelingMileage);
        dest.writeInt(this.mEVCruisingRang_real);
        dest.writeInt(this.mCanTravelMileage_real);
        dest.writeInt(this.mEVCruisingRange_wltc);
    }

    public String toString() {
        return "Odometer [mCanTravelMileage=" + this.mCanTravelMileage + ", mBeenTravelingMileage=" + this.mBeenTravelingMileage + ", mTotalTravelingMileage=" + this.mTotalTravelingMileage + ", mEVCruisingRange=" + this.mEVCruisingRange + ", mEnduranceMileage=" + this.mEnduranceMileage + ", mChargeReaminTime=" + this.mChargeReaminTime + ", mPowerConsumption=" + this.mPowerConsumption + ", mEVCruisingRang_real=" + this.mEVCruisingRang_real + ", mCanTravelMileage_real=" + this.mCanTravelMileage_real + ", mEVCruisingRange_wltc=" + this.mEVCruisingRange_wltc + "]";
    }
}

package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

/* loaded from: classes.dex */
public class EnergyConsumptionInfo implements Parcelable {
    public static final Parcelable.Creator<EnergyConsumptionInfo> CREATOR = new Parcelable.Creator<EnergyConsumptionInfo>() { // from class: com.qinggan.canbus.EnergyConsumptionInfo.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public EnergyConsumptionInfo createFromParcel(Parcel source) {
            return new EnergyConsumptionInfo(source);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public EnergyConsumptionInfo[] newArray(int size) {
            return new EnergyConsumptionInfo[size];
        }
    };
    public static final int INVALID = -9999;
    public static final int UNKNOWN = -1000;
    private float currentTripAvgOilConsum;
    private float currentTripAvgPwrConsum;
    private int currentTripAvgSpd;
    private float currentTripDistance;
    private int currentTripLastingTime;
    private float sinceChargeAvgOilConsum;
    private float sinceChargeAvgPwrConsum;
    private int sinceChargeAvgSpd;
    private float sinceChargeDistance;
    private int sinceChargeLastingTime;
    private float sinceRefulAvgOilConsum;
    private float sinceRefulAvgPwrConsum;
    private int sinceRefulAvgSpd;
    private float sinceRefulDistance;
    private int sinceRefulLastingTime;
    private float sinceTripAAvgOilConsum;
    private float sinceTripAAvgPwrConsum;
    private int sinceTripAAvgSpd;
    private float sinceTripADistance;
    private int sinceTripALastingTime;

    public float getCurrentTripDistance() {
        return this.currentTripDistance;
    }

    public void setCurrentTripDistance(float currentTripDistance) {
        this.currentTripDistance = currentTripDistance;
    }

    public float getCurrentTripAvgPwrConsum() {
        return this.currentTripAvgPwrConsum;
    }

    public void setCurrentTripAvgPwrConsum(float currentTripAvgPwrConsum) {
        this.currentTripAvgPwrConsum = currentTripAvgPwrConsum;
    }

    public int getCurrentTripAvgSpd() {
        return this.currentTripAvgSpd;
    }

    public void setCurrentTripAvgSpd(int currentTripAvgSpd) {
        this.currentTripAvgSpd = currentTripAvgSpd;
    }

    public float getCurrentTripAvgOilConsum() {
        return this.currentTripAvgOilConsum;
    }

    public void setCurrentTripAvgOilConsum(float currentTripAvgOilConsum) {
        this.currentTripAvgOilConsum = currentTripAvgOilConsum;
    }

    public float getCurrentTripLastingTime() {
        return this.currentTripLastingTime;
    }

    public void setCurrentTripLastingTime(int currentTripLastingTime) {
        this.currentTripLastingTime = currentTripLastingTime;
    }

    public float getSinceChargeDistance() {
        return this.sinceChargeDistance;
    }

    public void setSinceChargeDistance(float sinceChargeDistance) {
        this.sinceChargeDistance = sinceChargeDistance;
    }

    public float getSinceChargeAvgPwrConsum() {
        return this.sinceChargeAvgPwrConsum;
    }

    public void setSinceChargeAvgPwrConsum(float sinceChargeAvgPwrConsum) {
        this.sinceChargeAvgPwrConsum = sinceChargeAvgPwrConsum;
    }

    public int getSinceChargeAvgSpd() {
        return this.sinceChargeAvgSpd;
    }

    public void setSinceChargeAvgSpd(int sinceChargeAvgSpd) {
        this.sinceChargeAvgSpd = sinceChargeAvgSpd;
    }

    public float getSinceChargeAvgOilConsum() {
        return this.sinceChargeAvgOilConsum;
    }

    public void setSinceChargeAvgOilConsum(float sinceChargeAvgOilConsum) {
        this.sinceChargeAvgOilConsum = sinceChargeAvgOilConsum;
    }

    public float getSinceChargeLastingTime() {
        return this.sinceChargeLastingTime;
    }

    public void setSinceChargeLastingTime(int sinceChargeLastingTime) {
        this.sinceChargeLastingTime = sinceChargeLastingTime;
    }

    public float getSinceRefulDistance() {
        return this.sinceRefulDistance;
    }

    public void setSinceRefulDistance(float sinceRefulDistance) {
        this.sinceRefulDistance = sinceRefulDistance;
    }

    public float getSinceRefulAvgPwrConsum() {
        return this.sinceRefulAvgPwrConsum;
    }

    public void setSinceRefulAvgPwrConsum(float sinceRefulAvgPwrConsum) {
        this.sinceRefulAvgPwrConsum = sinceRefulAvgPwrConsum;
    }

    public int getSinceRefulAvgSpd() {
        return this.sinceRefulAvgSpd;
    }

    public void setSinceRefulAvgSpd(int sinceRefulAvgSpd) {
        this.sinceRefulAvgSpd = sinceRefulAvgSpd;
    }

    public float getSinceRefulAvgOilConsum() {
        return this.sinceRefulAvgOilConsum;
    }

    public void setSinceRefulAvgOilConsum(float sinceRefulAvgOilConsum) {
        this.sinceRefulAvgOilConsum = sinceRefulAvgOilConsum;
    }

    public float getSinceRefulLastingTime() {
        return this.sinceRefulLastingTime;
    }

    public void setSinceRefulLastingTime(int sinceRefulLastingTime) {
        this.sinceRefulLastingTime = sinceRefulLastingTime;
    }

    public float getSinceTripADistance() {
        return this.sinceTripADistance;
    }

    public void setSinceTripADistance(float sinceTripADistance) {
        this.sinceTripADistance = sinceTripADistance;
    }

    public float getSinceTripAAvgPwrConsum() {
        return this.sinceTripAAvgPwrConsum;
    }

    public void setSinceTripAAvgPwrConsum(float sinceTripAAvgPwrConsum) {
        this.sinceTripAAvgPwrConsum = sinceTripAAvgPwrConsum;
    }

    public int getSinceTripAAvgSpd() {
        return this.sinceTripAAvgSpd;
    }

    public void setSinceTripAAvgSpd(int sinceTripAAvgSpd) {
        this.sinceTripAAvgSpd = sinceTripAAvgSpd;
    }

    public float getSinceTripAAvgOilConsum() {
        return this.sinceTripAAvgOilConsum;
    }

    public void setSinceTripAAvgOilConsum(float sinceTripAAvgOilConsum) {
        this.sinceTripAAvgOilConsum = sinceTripAAvgOilConsum;
    }

    public float getSinceTripALastingTime() {
        return this.sinceTripALastingTime;
    }

    public void setSinceTripALastingTime(int sinceTripALastingTime) {
        this.sinceTripALastingTime = sinceTripALastingTime;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.currentTripDistance);
        dest.writeFloat(this.currentTripAvgPwrConsum);
        dest.writeInt(this.currentTripAvgSpd);
        dest.writeFloat(this.currentTripAvgOilConsum);
        dest.writeInt(this.currentTripLastingTime);
        dest.writeFloat(this.sinceChargeDistance);
        dest.writeFloat(this.sinceChargeAvgPwrConsum);
        dest.writeInt(this.sinceChargeAvgSpd);
        dest.writeFloat(this.sinceChargeAvgOilConsum);
        dest.writeInt(this.sinceChargeLastingTime);
        dest.writeFloat(this.sinceRefulDistance);
        dest.writeFloat(this.sinceRefulAvgPwrConsum);
        dest.writeInt(this.sinceRefulAvgSpd);
        dest.writeFloat(this.sinceRefulAvgOilConsum);
        dest.writeInt(this.sinceRefulLastingTime);
        dest.writeFloat(this.sinceTripADistance);
        dest.writeFloat(this.sinceTripAAvgPwrConsum);
        dest.writeInt(this.sinceTripAAvgSpd);
        dest.writeFloat(this.sinceTripAAvgOilConsum);
        dest.writeInt(this.sinceTripALastingTime);
    }

    public void readFromParcel(Parcel source) {
        this.currentTripDistance = source.readFloat();
        this.currentTripAvgPwrConsum = source.readFloat();
        this.currentTripAvgSpd = source.readInt();
        this.currentTripAvgOilConsum = source.readFloat();
        this.currentTripLastingTime = source.readInt();
        this.sinceChargeDistance = source.readFloat();
        this.sinceChargeAvgPwrConsum = source.readFloat();
        this.sinceChargeAvgSpd = source.readInt();
        this.sinceChargeAvgOilConsum = source.readFloat();
        this.sinceChargeLastingTime = source.readInt();
        this.sinceRefulDistance = source.readFloat();
        this.sinceRefulAvgPwrConsum = source.readFloat();
        this.sinceRefulAvgSpd = source.readInt();
        this.sinceRefulAvgOilConsum = source.readFloat();
        this.sinceRefulLastingTime = source.readInt();
        this.sinceTripADistance = source.readFloat();
        this.sinceTripAAvgPwrConsum = source.readFloat();
        this.sinceTripAAvgSpd = source.readInt();
        this.sinceTripAAvgOilConsum = source.readFloat();
        this.sinceTripALastingTime = source.readInt();
    }

    public EnergyConsumptionInfo() {
        this.currentTripDistance = -1000.0f;
        this.currentTripAvgPwrConsum = -1000.0f;
        this.currentTripAvgSpd = -1000;
        this.currentTripAvgOilConsum = -1000.0f;
        this.currentTripLastingTime = -1000;
        this.sinceChargeDistance = -1000.0f;
        this.sinceChargeAvgPwrConsum = -1000.0f;
        this.sinceChargeAvgSpd = -1000;
        this.sinceChargeAvgOilConsum = -1000.0f;
        this.sinceChargeLastingTime = -1000;
        this.sinceRefulDistance = -1000.0f;
        this.sinceRefulAvgPwrConsum = -1000.0f;
        this.sinceRefulAvgSpd = -1000;
        this.sinceRefulAvgOilConsum = -1000.0f;
        this.sinceRefulLastingTime = -1000;
        this.sinceTripADistance = -1000.0f;
        this.sinceTripAAvgPwrConsum = -1000.0f;
        this.sinceTripAAvgSpd = -1000;
        this.sinceTripAAvgOilConsum = -1000.0f;
        this.sinceTripALastingTime = -1000;
    }

    protected EnergyConsumptionInfo(Parcel in) {
        this.currentTripDistance = -1000.0f;
        this.currentTripAvgPwrConsum = -1000.0f;
        this.currentTripAvgSpd = -1000;
        this.currentTripAvgOilConsum = -1000.0f;
        this.currentTripLastingTime = -1000;
        this.sinceChargeDistance = -1000.0f;
        this.sinceChargeAvgPwrConsum = -1000.0f;
        this.sinceChargeAvgSpd = -1000;
        this.sinceChargeAvgOilConsum = -1000.0f;
        this.sinceChargeLastingTime = -1000;
        this.sinceRefulDistance = -1000.0f;
        this.sinceRefulAvgPwrConsum = -1000.0f;
        this.sinceRefulAvgSpd = -1000;
        this.sinceRefulAvgOilConsum = -1000.0f;
        this.sinceRefulLastingTime = -1000;
        this.sinceTripADistance = -1000.0f;
        this.sinceTripAAvgPwrConsum = -1000.0f;
        this.sinceTripAAvgSpd = -1000;
        this.sinceTripAAvgOilConsum = -1000.0f;
        this.sinceTripALastingTime = -1000;
        this.currentTripDistance = in.readFloat();
        this.currentTripAvgPwrConsum = in.readFloat();
        this.currentTripAvgSpd = in.readInt();
        this.currentTripAvgOilConsum = in.readFloat();
        this.currentTripLastingTime = in.readInt();
        this.sinceChargeDistance = in.readFloat();
        this.sinceChargeAvgPwrConsum = in.readFloat();
        this.sinceChargeAvgSpd = in.readInt();
        this.sinceChargeAvgOilConsum = in.readFloat();
        this.sinceChargeLastingTime = in.readInt();
        this.sinceRefulDistance = in.readFloat();
        this.sinceRefulAvgPwrConsum = in.readFloat();
        this.sinceRefulAvgSpd = in.readInt();
        this.sinceRefulAvgOilConsum = in.readFloat();
        this.sinceRefulLastingTime = in.readInt();
        this.sinceTripADistance = in.readFloat();
        this.sinceTripAAvgPwrConsum = in.readFloat();
        this.sinceTripAAvgSpd = in.readInt();
        this.sinceTripAAvgOilConsum = in.readFloat();
        this.sinceTripALastingTime = in.readInt();
    }

    public String toString() {
        return "EnergyConsumptionInfo{currentTripDistance=" + this.currentTripDistance + ", currentTripAvgPwrConsum=" + this.currentTripAvgPwrConsum + ", currentTripAvgSpd=" + this.currentTripAvgSpd + ", currentTripAvgOilConsum=" + this.currentTripAvgOilConsum + ", currentTripLastingTime=" + this.currentTripLastingTime + ", sinceChargeDistance=" + this.sinceChargeDistance + ", sinceChargeAvgPwrConsum=" + this.sinceChargeAvgPwrConsum + ", sinceChargeAvgSpd=" + this.sinceChargeAvgSpd + ", sinceChargeAvgOilConsum=" + this.sinceChargeAvgOilConsum + ", sinceChargeLastingTime=" + this.sinceChargeLastingTime + ", sinceRefulDistance=" + this.sinceRefulDistance + ", sinceRefulAvgPwrConsum=" + this.sinceRefulAvgPwrConsum + ", sinceRefulAvgSpd=" + this.sinceRefulAvgSpd + ", sinceRefulAvgOilConsum=" + this.sinceRefulAvgOilConsum + ", sinceRefulLastingTime=" + this.sinceRefulLastingTime + ", sinceTripADistance=" + this.sinceTripADistance + ", sinceTripAAvgPwrConsum=" + this.sinceTripAAvgPwrConsum + ", sinceTripAAvgSpd=" + this.sinceTripAAvgSpd + ", sinceTripAAvgOilConsum=" + this.sinceTripAAvgOilConsum + ", sinceTripALastingTime=" + this.sinceTripALastingTime + '}';
    }
}

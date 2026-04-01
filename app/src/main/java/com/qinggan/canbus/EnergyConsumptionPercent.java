package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

/* loaded from: classes.dex */
public class EnergyConsumptionPercent implements Parcelable {
    public static final Parcelable.Creator<EnergyConsumptionPercent> CREATOR = new Parcelable.Creator<EnergyConsumptionPercent>() { // from class: com.qinggan.canbus.EnergyConsumptionPercent.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public EnergyConsumptionPercent createFromParcel(Parcel source) {
            return new EnergyConsumptionPercent(source);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public EnergyConsumptionPercent[] newArray(int size) {
            return new EnergyConsumptionPercent[size];
        }
    };
    public static final int INVALID = -9999;
    public static final int UNKNOWN = -1000;
    private int consumInfoNum;
    private float currentTripCabinSoftCnsPer;
    private float currentTripDrvCnsPer;
    private float currentTripEleCnsPer;
    private float currentTripFuelCnsPer;
    private float currentTripOthCnsPer;
    private float currentTripV2LCnsPer;
    private float dayAvgOilConsum;
    private float dayAvgPwrConsum;
    private float instantCnsEV;
    private float instantCnsOil;
    private float sinceChargeCabinSoftCnsPer;
    private float sinceChargeDrvCnsPer;
    private float sinceChargeEVCnsPer;
    private float sinceChargeEleCnsPer;
    private float sinceChargeFuelCnsPer;
    private float sinceChargeOthCnsPer;
    private float sinceChargeV2LCnsPer;
    private float sinceRefulCabinSoftCnsPer;
    private float sinceRefulDrvCnsPer;
    private float sinceRefulEleCnsPer;
    private float sinceRefulFuelCnsPer;
    private float sinceRefulOthCnsPer;
    private float sinceRefulParalCnsPer;
    private float sinceRefulSeriCnsPer;
    private float sinceRefulV2LCnsPer;
    private float sinceTripACabinSoftCnsPer;
    private float sinceTripADrvCnsPer;
    private float sinceTripAEVCnsPer;
    private float sinceTripAEleCnsPer;
    private float sinceTripAFuelCnsPer;
    private float sinceTripAOthCnsPer;
    private float sinceTripAParalCnsPer;
    private float sinceTripASeriCnsPer;
    private float sinceTripAV2LCnsPer;

    public float getCurrentTripDrvCnsPer() {
        return this.currentTripDrvCnsPer;
    }

    public void setCurrentTripDrvCnsPer(float currentTripDrvCnsPer) {
        this.currentTripDrvCnsPer = currentTripDrvCnsPer;
    }

    public float getCurrentTripCabinSoftCnsPer() {
        return this.currentTripCabinSoftCnsPer;
    }

    public void setCurrentTripCabinSoftCnsPer(float currentTripCabinSoftCnsPer) {
        this.currentTripCabinSoftCnsPer = currentTripCabinSoftCnsPer;
    }

    public float getCurrentTripEleCnsPer() {
        return this.currentTripEleCnsPer;
    }

    public void setCurrentTripEleCnsPer(float currentTripEleCnsPer) {
        this.currentTripEleCnsPer = currentTripEleCnsPer;
    }

    public float getCurrentTripFuelCnsPer() {
        return this.currentTripFuelCnsPer;
    }

    public void setCurrentTripFuelCnsPer(float currentTripFuelCnsPer) {
        this.currentTripFuelCnsPer = currentTripFuelCnsPer;
    }

    public float getCurrentTripV2LCnsPer() {
        return this.currentTripV2LCnsPer;
    }

    public void setCurrentTripV2LCnsPer(float currentTripV2LCnsPer) {
        this.currentTripV2LCnsPer = currentTripV2LCnsPer;
    }

    public float getCurrentTripOthCnsPer() {
        return this.currentTripOthCnsPer;
    }

    public void setCurrentTripOthCnsPer(float currentTripOthCnsPer) {
        this.currentTripOthCnsPer = currentTripOthCnsPer;
    }

    public float getSinceChargeDrvCnsPer() {
        return this.sinceChargeDrvCnsPer;
    }

    public void setSinceChargeDrvCnsPer(float sinceChargeDrvCnsPer) {
        this.sinceChargeDrvCnsPer = sinceChargeDrvCnsPer;
    }

    public float getSinceChargeCabinSoftCnsPer() {
        return this.sinceChargeCabinSoftCnsPer;
    }

    public void setSinceChargeCabinSoftCnsPer(float sinceChargeCabinSoftCnsPer) {
        this.sinceChargeCabinSoftCnsPer = sinceChargeCabinSoftCnsPer;
    }

    public float getSinceChargeEleCnsPer() {
        return this.sinceChargeEleCnsPer;
    }

    public void setSinceChargeEleCnsPer(float sinceChargeEleCnsPer) {
        this.sinceChargeEleCnsPer = sinceChargeEleCnsPer;
    }

    public float getSinceChargeFuelCnsPer() {
        return this.sinceChargeFuelCnsPer;
    }

    public void setSinceChargeFuelCnsPer(float sinceChargeFuelCnsPer) {
        this.sinceChargeFuelCnsPer = sinceChargeFuelCnsPer;
    }

    public float getSinceChargeV2LCnsPer() {
        return this.sinceChargeV2LCnsPer;
    }

    public void setSinceChargeV2LCnsPer(float sinceChargeV2LCnsPer) {
        this.sinceChargeV2LCnsPer = sinceChargeV2LCnsPer;
    }

    public float getSinceChargeOthCnsPer() {
        return this.sinceChargeOthCnsPer;
    }

    public void setSinceChargeOthCnsPer(float sinceChargeOthCnsPer) {
        this.sinceChargeOthCnsPer = sinceChargeOthCnsPer;
    }

    public float getSinceRefulDrvCnsPer() {
        return this.sinceRefulDrvCnsPer;
    }

    public void setSinceRefulDrvCnsPer(float sinceRefulDrvCnsPer) {
        this.sinceRefulDrvCnsPer = sinceRefulDrvCnsPer;
    }

    public float getSinceRefulCabinSoftCnsPer() {
        return this.sinceRefulCabinSoftCnsPer;
    }

    public void setSinceRefulCabinSoftCnsPer(float sinceRefulCabinSoftCnsPer) {
        this.sinceRefulCabinSoftCnsPer = sinceRefulCabinSoftCnsPer;
    }

    public float getSinceRefulEleCnsPer() {
        return this.sinceRefulEleCnsPer;
    }

    public void setSinceRefulEleCnsPer(float sinceRefulEleCnsPer) {
        this.sinceRefulEleCnsPer = sinceRefulEleCnsPer;
    }

    public float getSinceRefulFuelCnsPer() {
        return this.sinceRefulFuelCnsPer;
    }

    public void setSinceRefulFuelCnsPer(float sinceRefulFuelCnsPer) {
        this.sinceRefulFuelCnsPer = sinceRefulFuelCnsPer;
    }

    public float getSinceRefulV2LCnsPer() {
        return this.sinceRefulV2LCnsPer;
    }

    public void setSinceRefulV2LCnsPer(float sinceRefulV2LCnsPer) {
        this.sinceRefulV2LCnsPer = sinceRefulV2LCnsPer;
    }

    public float getSinceRefulOthCnsPer() {
        return this.sinceRefulOthCnsPer;
    }

    public void setSinceRefulOthCnsPer(float sinceRefulOthCnsPer) {
        this.sinceRefulOthCnsPer = sinceRefulOthCnsPer;
    }

    public float getSinceTripADrvCnsPer() {
        return this.sinceTripADrvCnsPer;
    }

    public void setSinceTripADrvCnsPer(float sinceTripADrvCnsPer) {
        this.sinceTripADrvCnsPer = sinceTripADrvCnsPer;
    }

    public float getSinceTripACabinSoftCnsPer() {
        return this.sinceTripACabinSoftCnsPer;
    }

    public void setSinceTripACabinSoftCnsPer(float sinceTripACabinSoftCnsPer) {
        this.sinceTripACabinSoftCnsPer = sinceTripACabinSoftCnsPer;
    }

    public float getSinceTripAEleCnsPer() {
        return this.sinceTripAEleCnsPer;
    }

    public void setSinceTripAEleCnsPer(float sinceTripAEleCnsPer) {
        this.sinceTripAEleCnsPer = sinceTripAEleCnsPer;
    }

    public float getSinceTripAFuelCnsPer() {
        return this.sinceTripAFuelCnsPer;
    }

    public void setSinceTripAFuelCnsPer(float sinceTripAFuelCnsPer) {
        this.sinceTripAFuelCnsPer = sinceTripAFuelCnsPer;
    }

    public float getSinceTripAV2LCnsPer() {
        return this.sinceTripAV2LCnsPer;
    }

    public void setSinceTripAV2LCnsPer(float sinceTripAV2LCnsPer) {
        this.sinceTripAV2LCnsPer = sinceTripAV2LCnsPer;
    }

    public float getSinceTripAOthCnsPer() {
        return this.sinceTripAOthCnsPer;
    }

    public void setSinceTripAOthCnsPer(float sinceTripAOthCnsPer) {
        this.sinceTripAOthCnsPer = sinceTripAOthCnsPer;
    }

    public float getSinceChargeEVCnsPer() {
        return this.sinceChargeEVCnsPer;
    }

    public void setSinceChargeEVCnsPer(float sinceChargeEVCnsPer) {
        this.sinceChargeEVCnsPer = sinceChargeEVCnsPer;
    }

    public float getSinceRefulSeriCnsPer() {
        return this.sinceRefulSeriCnsPer;
    }

    public void setSinceRefulSeriCnsPer(float sinceRefulSeriCnsPer) {
        this.sinceRefulSeriCnsPer = sinceRefulSeriCnsPer;
    }

    public float getSinceRefulParalCnsPer() {
        return this.sinceRefulParalCnsPer;
    }

    public void setSinceRefulParalCnsPer(float sinceRefulParalCnsPer) {
        this.sinceRefulParalCnsPer = sinceRefulParalCnsPer;
    }

    public float getSinceTripAEVCnsPer() {
        return this.sinceTripAEVCnsPer;
    }

    public void setSinceTripAEVCnsPer(float sinceTripAEVCnsPer) {
        this.sinceTripAEVCnsPer = sinceTripAEVCnsPer;
    }

    public float getSinceTripASeriCnsPer() {
        return this.sinceTripASeriCnsPer;
    }

    public void setSinceTripASeriCnsPer(float sinceTripASeriCnsPer) {
        this.sinceTripASeriCnsPer = sinceTripASeriCnsPer;
    }

    public float getSinceTripAParalCnsPer() {
        return this.sinceTripAParalCnsPer;
    }

    public void setSinceTripAParalCnsPer(float sinceTripAParalCnsPer) {
        this.sinceTripAParalCnsPer = sinceTripAParalCnsPer;
    }

    public float getDayAvgOilConsum() {
        return this.dayAvgOilConsum;
    }

    public void setDayAvgOilConsum(float dayAvgOilConsum) {
        this.dayAvgOilConsum = dayAvgOilConsum;
    }

    public float getDayAvgPwrConsum() {
        return this.dayAvgPwrConsum;
    }

    public void setDayAvgPwrConsum(float dayAvgPwrConsum) {
        this.dayAvgPwrConsum = dayAvgPwrConsum;
    }

    public int getConsumInfoNum() {
        return this.consumInfoNum;
    }

    public void setConsumInfoNum(int consumInfoNum) {
        this.consumInfoNum = consumInfoNum;
    }

    public float getInstantCnsEV() {
        return this.instantCnsEV;
    }

    public void setInstantCnsEV(float instantCnsEV) {
        this.instantCnsEV = instantCnsEV;
    }

    public float getInstantCnsOil() {
        return this.instantCnsOil;
    }

    public void setInstantCnsOil(float instantCnsOil) {
        this.instantCnsOil = instantCnsOil;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.currentTripDrvCnsPer);
        dest.writeFloat(this.currentTripCabinSoftCnsPer);
        dest.writeFloat(this.currentTripEleCnsPer);
        dest.writeFloat(this.currentTripFuelCnsPer);
        dest.writeFloat(this.currentTripV2LCnsPer);
        dest.writeFloat(this.currentTripOthCnsPer);
        dest.writeFloat(this.sinceChargeDrvCnsPer);
        dest.writeFloat(this.sinceChargeCabinSoftCnsPer);
        dest.writeFloat(this.sinceChargeEleCnsPer);
        dest.writeFloat(this.sinceChargeFuelCnsPer);
        dest.writeFloat(this.sinceChargeV2LCnsPer);
        dest.writeFloat(this.sinceChargeOthCnsPer);
        dest.writeFloat(this.sinceRefulDrvCnsPer);
        dest.writeFloat(this.sinceRefulCabinSoftCnsPer);
        dest.writeFloat(this.sinceRefulEleCnsPer);
        dest.writeFloat(this.sinceRefulFuelCnsPer);
        dest.writeFloat(this.sinceRefulV2LCnsPer);
        dest.writeFloat(this.sinceRefulOthCnsPer);
        dest.writeFloat(this.sinceTripADrvCnsPer);
        dest.writeFloat(this.sinceTripACabinSoftCnsPer);
        dest.writeFloat(this.sinceTripAEleCnsPer);
        dest.writeFloat(this.sinceTripAFuelCnsPer);
        dest.writeFloat(this.sinceTripAV2LCnsPer);
        dest.writeFloat(this.sinceTripAOthCnsPer);
        dest.writeFloat(this.sinceChargeEVCnsPer);
        dest.writeFloat(this.sinceRefulSeriCnsPer);
        dest.writeFloat(this.sinceRefulParalCnsPer);
        dest.writeFloat(this.sinceTripAEVCnsPer);
        dest.writeFloat(this.sinceTripASeriCnsPer);
        dest.writeFloat(this.sinceTripAParalCnsPer);
        dest.writeFloat(this.dayAvgOilConsum);
        dest.writeFloat(this.dayAvgPwrConsum);
        dest.writeInt(this.consumInfoNum);
        dest.writeFloat(this.instantCnsEV);
        dest.writeFloat(this.instantCnsOil);
    }

    public void readFromParcel(Parcel source) {
        this.currentTripDrvCnsPer = source.readFloat();
        this.currentTripCabinSoftCnsPer = source.readFloat();
        this.currentTripEleCnsPer = source.readFloat();
        this.currentTripFuelCnsPer = source.readFloat();
        this.currentTripV2LCnsPer = source.readFloat();
        this.currentTripOthCnsPer = source.readFloat();
        this.sinceChargeDrvCnsPer = source.readFloat();
        this.sinceChargeCabinSoftCnsPer = source.readFloat();
        this.sinceChargeEleCnsPer = source.readFloat();
        this.sinceChargeFuelCnsPer = source.readFloat();
        this.sinceChargeV2LCnsPer = source.readFloat();
        this.sinceChargeOthCnsPer = source.readFloat();
        this.sinceRefulDrvCnsPer = source.readFloat();
        this.sinceRefulCabinSoftCnsPer = source.readFloat();
        this.sinceRefulEleCnsPer = source.readFloat();
        this.sinceRefulFuelCnsPer = source.readFloat();
        this.sinceRefulV2LCnsPer = source.readFloat();
        this.sinceRefulOthCnsPer = source.readFloat();
        this.sinceTripADrvCnsPer = source.readFloat();
        this.sinceTripACabinSoftCnsPer = source.readFloat();
        this.sinceTripAEleCnsPer = source.readFloat();
        this.sinceTripAFuelCnsPer = source.readFloat();
        this.sinceTripAV2LCnsPer = source.readFloat();
        this.sinceTripAOthCnsPer = source.readFloat();
        this.sinceChargeEVCnsPer = source.readFloat();
        this.sinceRefulSeriCnsPer = source.readFloat();
        this.sinceRefulParalCnsPer = source.readFloat();
        this.sinceTripAEVCnsPer = source.readFloat();
        this.sinceTripASeriCnsPer = source.readFloat();
        this.sinceTripAParalCnsPer = source.readFloat();
        this.dayAvgOilConsum = source.readFloat();
        this.dayAvgPwrConsum = source.readFloat();
        this.consumInfoNum = source.readInt();
        this.instantCnsEV = source.readFloat();
        this.instantCnsOil = source.readFloat();
    }

    public EnergyConsumptionPercent() {
        this.currentTripDrvCnsPer = -1000.0f;
        this.currentTripCabinSoftCnsPer = -1000.0f;
        this.currentTripEleCnsPer = -1000.0f;
        this.currentTripFuelCnsPer = -1000.0f;
        this.currentTripV2LCnsPer = -1000.0f;
        this.currentTripOthCnsPer = -1000.0f;
        this.sinceChargeDrvCnsPer = -1000.0f;
        this.sinceChargeCabinSoftCnsPer = -1000.0f;
        this.sinceChargeEleCnsPer = -1000.0f;
        this.sinceChargeFuelCnsPer = -1000.0f;
        this.sinceChargeV2LCnsPer = -1000.0f;
        this.sinceChargeOthCnsPer = -1000.0f;
        this.sinceRefulDrvCnsPer = -1000.0f;
        this.sinceRefulCabinSoftCnsPer = -1000.0f;
        this.sinceRefulEleCnsPer = -1000.0f;
        this.sinceRefulFuelCnsPer = -1000.0f;
        this.sinceRefulV2LCnsPer = -1000.0f;
        this.sinceRefulOthCnsPer = -1000.0f;
        this.sinceTripADrvCnsPer = -1000.0f;
        this.sinceTripACabinSoftCnsPer = -1000.0f;
        this.sinceTripAEleCnsPer = -1000.0f;
        this.sinceTripAFuelCnsPer = -1000.0f;
        this.sinceTripAV2LCnsPer = -1000.0f;
        this.sinceTripAOthCnsPer = -1000.0f;
        this.sinceChargeEVCnsPer = -1000.0f;
        this.sinceRefulSeriCnsPer = -1000.0f;
        this.sinceRefulParalCnsPer = -1000.0f;
        this.sinceTripAEVCnsPer = -1000.0f;
        this.sinceTripASeriCnsPer = -1000.0f;
        this.sinceTripAParalCnsPer = -1000.0f;
        this.dayAvgOilConsum = -1000.0f;
        this.dayAvgPwrConsum = -1000.0f;
        this.consumInfoNum = -1000;
        this.instantCnsEV = -1000.0f;
        this.instantCnsOil = -1000.0f;
    }

    protected EnergyConsumptionPercent(Parcel in) {
        this.currentTripDrvCnsPer = -1000.0f;
        this.currentTripCabinSoftCnsPer = -1000.0f;
        this.currentTripEleCnsPer = -1000.0f;
        this.currentTripFuelCnsPer = -1000.0f;
        this.currentTripV2LCnsPer = -1000.0f;
        this.currentTripOthCnsPer = -1000.0f;
        this.sinceChargeDrvCnsPer = -1000.0f;
        this.sinceChargeCabinSoftCnsPer = -1000.0f;
        this.sinceChargeEleCnsPer = -1000.0f;
        this.sinceChargeFuelCnsPer = -1000.0f;
        this.sinceChargeV2LCnsPer = -1000.0f;
        this.sinceChargeOthCnsPer = -1000.0f;
        this.sinceRefulDrvCnsPer = -1000.0f;
        this.sinceRefulCabinSoftCnsPer = -1000.0f;
        this.sinceRefulEleCnsPer = -1000.0f;
        this.sinceRefulFuelCnsPer = -1000.0f;
        this.sinceRefulV2LCnsPer = -1000.0f;
        this.sinceRefulOthCnsPer = -1000.0f;
        this.sinceTripADrvCnsPer = -1000.0f;
        this.sinceTripACabinSoftCnsPer = -1000.0f;
        this.sinceTripAEleCnsPer = -1000.0f;
        this.sinceTripAFuelCnsPer = -1000.0f;
        this.sinceTripAV2LCnsPer = -1000.0f;
        this.sinceTripAOthCnsPer = -1000.0f;
        this.sinceChargeEVCnsPer = -1000.0f;
        this.sinceRefulSeriCnsPer = -1000.0f;
        this.sinceRefulParalCnsPer = -1000.0f;
        this.sinceTripAEVCnsPer = -1000.0f;
        this.sinceTripASeriCnsPer = -1000.0f;
        this.sinceTripAParalCnsPer = -1000.0f;
        this.dayAvgOilConsum = -1000.0f;
        this.dayAvgPwrConsum = -1000.0f;
        this.consumInfoNum = -1000;
        this.instantCnsEV = -1000.0f;
        this.instantCnsOil = -1000.0f;
        this.currentTripDrvCnsPer = in.readFloat();
        this.currentTripCabinSoftCnsPer = in.readFloat();
        this.currentTripEleCnsPer = in.readFloat();
        this.currentTripFuelCnsPer = in.readFloat();
        this.currentTripV2LCnsPer = in.readFloat();
        this.currentTripOthCnsPer = in.readFloat();
        this.sinceChargeDrvCnsPer = in.readFloat();
        this.sinceChargeCabinSoftCnsPer = in.readFloat();
        this.sinceChargeEleCnsPer = in.readFloat();
        this.sinceChargeFuelCnsPer = in.readFloat();
        this.sinceChargeV2LCnsPer = in.readFloat();
        this.sinceChargeOthCnsPer = in.readFloat();
        this.sinceRefulDrvCnsPer = in.readFloat();
        this.sinceRefulCabinSoftCnsPer = in.readFloat();
        this.sinceRefulEleCnsPer = in.readFloat();
        this.sinceRefulFuelCnsPer = in.readFloat();
        this.sinceRefulV2LCnsPer = in.readFloat();
        this.sinceRefulOthCnsPer = in.readFloat();
        this.sinceTripADrvCnsPer = in.readFloat();
        this.sinceTripACabinSoftCnsPer = in.readFloat();
        this.sinceTripAEleCnsPer = in.readFloat();
        this.sinceTripAFuelCnsPer = in.readFloat();
        this.sinceTripAV2LCnsPer = in.readFloat();
        this.sinceTripAOthCnsPer = in.readFloat();
        this.sinceChargeEVCnsPer = in.readFloat();
        this.sinceRefulSeriCnsPer = in.readFloat();
        this.sinceRefulParalCnsPer = in.readFloat();
        this.sinceTripAEVCnsPer = in.readFloat();
        this.sinceTripASeriCnsPer = in.readFloat();
        this.sinceTripAParalCnsPer = in.readFloat();
        this.dayAvgOilConsum = in.readFloat();
        this.dayAvgPwrConsum = in.readFloat();
        this.consumInfoNum = in.readInt();
        this.instantCnsEV = in.readFloat();
        this.instantCnsOil = in.readFloat();
    }

    public String toString() {
        return "EnergyConsumptionPercent{currentTripDrvCnsPer=" + this.currentTripDrvCnsPer + ", currentTripCabinSoftCnsPer=" + this.currentTripCabinSoftCnsPer + ", currentTripEleCnsPer=" + this.currentTripEleCnsPer + ", currentTripFuelCnsPer=" + this.currentTripFuelCnsPer + ", currentTripV2LCnsPer=" + this.currentTripV2LCnsPer + ", currentTripOthCnsPer=" + this.currentTripOthCnsPer + ", sinceChargeDrvCnsPer=" + this.sinceChargeDrvCnsPer + ", sinceChargeCabinSoftCnsPer=" + this.sinceChargeCabinSoftCnsPer + ", sinceChargeEleCnsPer=" + this.sinceChargeEleCnsPer + ", sinceChargeFuelCnsPer=" + this.sinceChargeFuelCnsPer + ", sinceChargeV2LCnsPer=" + this.sinceChargeV2LCnsPer + ", sinceChargeOthCnsPer=" + this.sinceChargeOthCnsPer + ", sinceRefulDrvCnsPer=" + this.sinceRefulDrvCnsPer + ", sinceRefulCabinSoftCnsPer=" + this.sinceRefulCabinSoftCnsPer + ", sinceRefulEleCnsPer=" + this.sinceRefulEleCnsPer + ", sinceRefulFuelCnsPer=" + this.sinceRefulFuelCnsPer + ", sinceRefulV2LCnsPer=" + this.sinceRefulV2LCnsPer + ", sinceRefulOthCnsPer=" + this.sinceRefulOthCnsPer + ", sinceTripADrvCnsPer=" + this.sinceTripADrvCnsPer + ", sinceTripACabinSoftCnsPer=" + this.sinceTripACabinSoftCnsPer + ", sinceTripAEleCnsPer=" + this.sinceTripAEleCnsPer + ", sinceTripAFuelCnsPer=" + this.sinceTripAFuelCnsPer + ", sinceTripAV2LCnsPer=" + this.sinceTripAV2LCnsPer + ", sinceTripAOthCnsPer=" + this.sinceTripAOthCnsPer + ", sinceChargeEVCnsPer=" + this.sinceChargeEVCnsPer + ", sinceRefulSeriCnsPer=" + this.sinceRefulSeriCnsPer + ", sinceRefulParalCnsPer=" + this.sinceRefulParalCnsPer + ", sinceTripAEVCnsPer=" + this.sinceTripAEVCnsPer + ", sinceTripASeriCnsPer=" + this.sinceTripASeriCnsPer + ", sinceTripAParalCnsPer=" + this.sinceTripAParalCnsPer + ", dayAvgOilConsum=" + this.dayAvgOilConsum + ", dayAvgPwrConsum=" + this.dayAvgPwrConsum + ", consumInfoNum=" + this.consumInfoNum + ", instantCnsEV=" + this.instantCnsEV + ", instantCnsOil=" + this.instantCnsOil + '}';
    }
}

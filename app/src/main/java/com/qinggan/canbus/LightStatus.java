package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

public final class LightStatus implements Parcelable {
    public static final Parcelable.Creator<LightStatus> CREATOR = new Parcelable.Creator<LightStatus>() { // from class: com.qinggan.canbus.LightStatus.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public LightStatus[] newArray(int size) {
            return new LightStatus[size];
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public LightStatus createFromParcel(Parcel source) {
            LightStatus lightStatus = new LightStatus();
            lightStatus.directionL = source.readInt();
            lightStatus.directionR = source.readInt();
            lightStatus.fogFront = source.readInt();
            lightStatus.fogRear = source.readInt();
            lightStatus.mainBeam = source.readInt();
            lightStatus.sideLight = source.readInt();
            lightStatus.ebdLamp = source.readInt();
            lightStatus.dippedBeam = source.readInt();
            lightStatus.positionLamp = source.readInt();
            lightStatus.dayLight = source.readInt();
            lightStatus.stopLight = source.readInt();
            lightStatus.reversLight = source.readInt();
            lightStatus.cautionLight = source.readInt();
            lightStatus.headLight = source.readInt();
            lightStatus.headLightLevel = source.readInt();
            lightStatus.headLightStatusOperability = source.readInt();
            lightStatus.autoLamp = source.readInt();
            return lightStatus;
        }
    };
    public static final int INVALID_COMPOSITE_STATUS = Integer.MIN_VALUE;
    public static final int OFF = 0;
    public static final int ON = 1;
    private int autoLamp;
    public int cautionLight;
    public int dayLight;
    public int dippedBeam;
    public int directionL;
    public int directionR;
    public int ebdLamp;
    public int fogFront;
    public int fogRear;
    private int headLight;
    private int headLightLevel;
    private int headLightStatusOperability;
    public int mainBeam;
    public int positionLamp;
    public int reversLight;
    public int sideLight;
    public int stopLight;

    public int getHeadLightLevel() {
        return this.headLightLevel;
    }

    public void setHeadLightLevel(int headLightLevel) {
        this.headLightLevel = headLightLevel;
    }

    public int getDirectionL() {
        return this.directionL;
    }

    public void setDirectionL(int directionL) {
        this.directionL = directionL;
    }

    public int getDirectionR() {
        return this.directionR;
    }

    public void setDirectionR(int directionR) {
        this.directionR = directionR;
    }

    public int getFogFront() {
        return this.fogFront;
    }

    public void setFogFront(int fogFront) {
        this.fogFront = fogFront;
    }

    public int getFogRear() {
        return this.fogRear;
    }

    public void setFogRear(int fogRear) {
        this.fogRear = fogRear;
    }

    public int getMainBeam() {
        return this.mainBeam;
    }

    public void setMainBeam(int mainBeam) {
        this.mainBeam = mainBeam;
    }

    public int getSideLight() {
        return this.sideLight;
    }

    public void setSideLight(int sideLight) {
        this.sideLight = sideLight;
    }

    public int getEbdLamp() {
        return this.ebdLamp;
    }

    public void setEbdLamp(int ebdLamp) {
        this.ebdLamp = ebdLamp;
    }

    public int getDippedBeam() {
        return this.dippedBeam;
    }

    public void setDippedBeam(int dippedBeam) {
        this.dippedBeam = dippedBeam;
    }

    public int getPositionLamp() {
        return this.positionLamp;
    }

    public void setPositionLamp(int positionLamp) {
        this.positionLamp = positionLamp;
    }

    public int getDayLight() {
        return this.dayLight;
    }

    public void setDayLight(int dayLight) {
        this.dayLight = dayLight;
    }

    public int getStopLight() {
        return this.stopLight;
    }

    public void setStopLight(int stopLight) {
        this.stopLight = stopLight;
    }

    public int getReversLight() {
        return this.reversLight;
    }

    public void setReversLight(int reversLight) {
        this.reversLight = reversLight;
    }

    public int getCautionLight() {
        return this.cautionLight;
    }

    public void setCautionLight(int cautionLight) {
        this.cautionLight = cautionLight;
    }

    public int getHeadLight() {
        return this.headLight;
    }

    public void setHeadLight(int headLight) {
        this.headLight = headLight;
    }

    public int getHeadLightStatusOperability() {
        return this.headLightStatusOperability;
    }

    public void setHeadLightStatusOperability(int headLightStatusOperability) {
        this.headLightStatusOperability = headLightStatusOperability;
    }

    public int getAutoLamp() {
        return this.autoLamp;
    }

    public void setAutoLamp(int autoLamp) {
        this.autoLamp = autoLamp;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.directionL);
        dest.writeInt(this.directionR);
        dest.writeInt(this.fogFront);
        dest.writeInt(this.fogRear);
        dest.writeInt(this.mainBeam);
        dest.writeInt(this.sideLight);
        dest.writeInt(this.ebdLamp);
        dest.writeInt(this.dippedBeam);
        dest.writeInt(this.positionLamp);
        dest.writeInt(this.dayLight);
        dest.writeInt(this.stopLight);
        dest.writeInt(this.reversLight);
        dest.writeInt(this.cautionLight);
        dest.writeInt(this.headLight);
        dest.writeInt(this.headLightLevel);
        dest.writeInt(this.headLightStatusOperability);
        dest.writeInt(this.autoLamp);
    }

    public String toString() {
        return "LightStatus{directionL=" + this.directionL + ", directionR=" + this.directionR + ", fogFront=" + this.fogFront + ", fogRear=" + this.fogRear + ", mainBeam=" + this.mainBeam + ", sideLight=" + this.sideLight + ", ebdLamp=" + this.ebdLamp + ", dippedBeam=" + this.dippedBeam + ", positionLamp=" + this.positionLamp + ", dayLight=" + this.dayLight + ", stopLight=" + this.stopLight + ", reversLight=" + this.reversLight + ", cautionLight=" + this.cautionLight + ", headLight=" + this.headLight + ", headLightStatusOperability=" + this.headLightStatusOperability + ", autoLamp=" + this.autoLamp + '}';
    }
}

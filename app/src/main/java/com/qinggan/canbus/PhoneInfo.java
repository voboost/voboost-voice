package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

/* loaded from: classes.dex */
public final class PhoneInfo implements Parcelable {
    public static final Parcelable.Creator<PhoneInfo> CREATOR = new Parcelable.Creator<PhoneInfo>() { // from class: com.qinggan.canbus.PhoneInfo.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public PhoneInfo[] newArray(int size) {
            return new PhoneInfo[size];
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public PhoneInfo createFromParcel(Parcel source) {
            PhoneInfo info = new PhoneInfo();
            info.phoneNum = source.readString();
            info.name = source.readString();
            info.duration = source.readInt();
            info.phoneState = (PhoneState) source.readParcelable(PhoneState.class.getClassLoader());
            info.deviceName = source.readString();
            info.connectedStatus = source.readInt();
            info.isVehicleCall = source.readInt();
            info.phoneBatteryLevel = source.readInt();
            info.phoneSignalStregth = source.readInt();
            info.phoneMissCallNumber = source.readInt();
            return info;
        }
    };
    int connectedStatus;
    String deviceName;
    int duration;
    int isVehicleCall;
    String name;
    private int phoneBatteryLevel;
    private int phoneMissCallNumber;
    String phoneNum;
    private int phoneSignalStregth;
    PhoneState phoneState;

    public String getDeviceName() {
        return this.deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getConnectedStatus() {
        return this.connectedStatus;
    }

    public void setConnectedStatus(int connectedStatus) {
        this.connectedStatus = connectedStatus;
    }

    public int isVehicleCall() {
        return this.isVehicleCall;
    }

    public void setVehicleCall(int isVehicleCall) {
        this.isVehicleCall = isVehicleCall;
    }

    public PhoneState getPhoneState() {
        return this.phoneState;
    }

    public void setPhoneState(PhoneState phoneState) {
        this.phoneState = phoneState;
    }

    public String getPhoneNum() {
        return this.phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.phoneNum);
        dest.writeString(this.name);
        dest.writeInt(this.duration);
        dest.writeParcelable(this.phoneState, flags);
        dest.writeString(this.deviceName);
        dest.writeInt(this.connectedStatus);
        dest.writeInt(this.isVehicleCall);
        dest.writeInt(this.phoneBatteryLevel);
        dest.writeInt(this.phoneSignalStregth);
        dest.writeInt(this.phoneMissCallNumber);
    }

    public String toString() {
        return "BTPhoneInfo{phoneNum='" + this.phoneNum + "', name='" + this.name + "', duration=" + this.duration + "', phoneState=" + this.phoneState + "', deviceName=" + this.deviceName + "', connectedStatus=" + this.connectedStatus + "', isVehicleCall=" + this.isVehicleCall + "', phoneBatteryLevel=" + this.phoneBatteryLevel + "', phoneSignalStregth=" + this.phoneSignalStregth + "', phoneMissCallNumber=" + this.phoneMissCallNumber + "'}";
    }

    public int getPhoneBatteryLevel() {
        return this.phoneBatteryLevel;
    }

    public void setPhoneBatteryLevel(int phoneBatteryLevel) {
        this.phoneBatteryLevel = phoneBatteryLevel;
    }

    public int getPhoneSignalStregth() {
        return this.phoneSignalStregth;
    }

    public void setPhoneSignalStregth(int phoneSignalStregth) {
        this.phoneSignalStregth = phoneSignalStregth;
    }

    public int getPhoneMissCallNumber() {
        return this.phoneMissCallNumber;
    }

    public void setPhoneMissCallNumber(int phoneMissCallNumber) {
        this.phoneMissCallNumber = phoneMissCallNumber;
    }
}

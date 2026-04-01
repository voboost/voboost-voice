package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

/* loaded from: classes.dex */
public final class WheelSpeed implements Parcelable {
    public static final Parcelable.Creator<WheelSpeed> CREATOR = new Parcelable.Creator<WheelSpeed>() { // from class: com.qinggan.canbus.WheelSpeed.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public WheelSpeed[] newArray(int size) {
            return new WheelSpeed[size];
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public WheelSpeed createFromParcel(Parcel source) {
            WheelSpeed speed = new WheelSpeed();
            speed.rightWheelSpeed = source.readInt();
            speed.leftWheelSpeed = source.readInt();
            speed.frontLeftWheelSpd = source.readInt();
            speed.frontRightWheelSpd = source.readInt();
            speed.rearLeftWheelSpd = source.readInt();
            speed.rearRightWheelSpd = source.readInt();
            return speed;
        }
    };
    public static final int WHEEL_SPEED_UNKNOWN = Integer.MIN_VALUE;
    private int frontLeftWheelSpd;
    private int frontRightWheelSpd;
    public int leftWheelSpeed;
    private int rearLeftWheelSpd;
    private int rearRightWheelSpd;
    public int rightWheelSpeed;

    public int getRightWheelSpeed() {
        return this.rightWheelSpeed;
    }

    public int getLeftWheelSpeed() {
        return this.leftWheelSpeed;
    }

    public int getFrontLeftWheelSpd() {
        return this.frontLeftWheelSpd;
    }

    public int getFrontRightWheelSpd() {
        return this.frontRightWheelSpd;
    }

    public int getRearLeftWheelSpd() {
        return this.rearLeftWheelSpd;
    }

    public int getRearRightWheelSpd() {
        return this.rearRightWheelSpd;
    }

    public void setFrontLeftWheelSpd(int wheelSpeed) {
        this.frontLeftWheelSpd = wheelSpeed;
    }

    public void setFrontRightWheelSpd(int wheelSpeed) {
        this.frontRightWheelSpd = wheelSpeed;
    }

    public void setRearLeftWheelSpd(int wheelSpeed) {
        this.rearLeftWheelSpd = wheelSpeed;
    }

    public void setRearRightWheelSpd(int wheelSpeed) {
        this.rearRightWheelSpd = wheelSpeed;
    }

    public void setRightWheelSpeed(int rightWheelSpeed) {
        this.rightWheelSpeed = rightWheelSpeed;
    }

    public void setLeftWheelSpeed(int leftWheelSpeed) {
        this.leftWheelSpeed = leftWheelSpeed;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.rightWheelSpeed);
        dest.writeInt(this.leftWheelSpeed);
        dest.writeInt(this.frontLeftWheelSpd);
        dest.writeInt(this.frontRightWheelSpd);
        dest.writeInt(this.rearLeftWheelSpd);
        dest.writeInt(this.rearRightWheelSpd);
    }

    public String toString() {
        return "WheelSpeed{rightWheelSpeed=" + this.rightWheelSpeed + ", leftWheelSpeed=" + this.leftWheelSpeed + ", frontLeftWheelSpd=" + this.frontLeftWheelSpd + ", frontRightWheelSpd=" + this.frontRightWheelSpd + ", rearLeftWheelSpd=" + this.rearLeftWheelSpd + ", rearRightWheelSpd=" + this.rearRightWheelSpd + '}';
    }
}

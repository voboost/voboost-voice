package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

/* loaded from: classes.dex */
public final class SeatAdjustState implements Parcelable {
    public static int STATE_NO_PRESS = 0;
    public static int STATE_UP = 1;
    public static int STATE_DOWN = 2;
    public static int STATE_FRONT = 1;
    public static int STATE_REAR = 2;
    public static int MIRROR_NO_PRESS = 0;
    public static int MIRROR_PRESS = 1;
    public static int MIRROR_NULL_OPERATING = 0;
    public static int MIRROR_AROUND_X_AXIS_UP = 1;
    public static int MIRROR_AROUND_X_AXIS_DOWN = 2;
    public static int MIRROR_AROUND_Y_AXIS_LEFT = 3;
    public static int MIRROR_AROUND_Y_AXIS_RIGHT = 4;
    public static final Parcelable.Creator<SeatAdjustState> CREATOR = new Parcelable.Creator<SeatAdjustState>() { // from class: com.qinggan.canbus.SeatAdjustState.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public SeatAdjustState[] newArray(int size) {
            return new SeatAdjustState[size];
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public SeatAdjustState createFromParcel(Parcel source) {
            SeatAdjustState seatAdjustState = new SeatAdjustState();
            seatAdjustState.driverSeatBack = source.readInt();
            seatAdjustState.driverSeatSlide = source.readInt();
            seatAdjustState.driverSeatHeight = source.readInt();
            seatAdjustState.driverSeatAngle = source.readInt();
            seatAdjustState.leftMirrorSwitch = source.readInt();
            seatAdjustState.rightMirrorSwitch = source.readInt();
            seatAdjustState.mirrorAdjust = source.readInt();
            return seatAdjustState;
        }
    };
    private int driverSeatBack = -1;
    private int driverSeatSlide = -1;
    private int driverSeatHeight = -1;
    private int driverSeatAngle = -1;
    private int leftMirrorSwitch = -1;
    private int rightMirrorSwitch = -1;
    private int mirrorAdjust = -1;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public int getDriverSeatBack() {
        return this.driverSeatBack;
    }

    public void setDriverSeatBack(int driverSeatBack) {
        this.driverSeatBack = driverSeatBack;
    }

    public int getDriverSeatSlide() {
        return this.driverSeatSlide;
    }

    public void setDriverSeatSlide(int driverSeatSlide) {
        this.driverSeatSlide = driverSeatSlide;
    }

    public int getDriverSeatHeight() {
        return this.driverSeatHeight;
    }

    public void setDriverSeatHeight(int driverSeatHeight) {
        this.driverSeatHeight = driverSeatHeight;
    }

    public int getDriverSeatAngle() {
        return this.driverSeatAngle;
    }

    public void setDriverSeatAngle(int driverSeatAngle) {
        this.driverSeatAngle = driverSeatAngle;
    }

    public int getLeftMirrorSwitch() {
        return this.leftMirrorSwitch;
    }

    public void setLeftMirrorSwitch(int leftMirrorSwitch) {
        this.leftMirrorSwitch = leftMirrorSwitch;
    }

    public int getRightMirrorSwitch() {
        return this.rightMirrorSwitch;
    }

    public void setRightMirrorSwitch(int rightMirrorSwitch) {
        this.rightMirrorSwitch = rightMirrorSwitch;
    }

    public int getMirrorAdjust() {
        return this.mirrorAdjust;
    }

    public void setMirrorAdjust(int mirrorAdjust) {
        this.mirrorAdjust = mirrorAdjust;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.driverSeatBack);
        dest.writeInt(this.driverSeatSlide);
        dest.writeInt(this.driverSeatHeight);
        dest.writeInt(this.driverSeatAngle);
        dest.writeInt(this.leftMirrorSwitch);
        dest.writeInt(this.rightMirrorSwitch);
        dest.writeInt(this.mirrorAdjust);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("SeatAdjustState{");
        sb.append("driverSeatBack=");
        sb.append(this.driverSeatBack);
        sb.append(", driverSeatSlide=");
        sb.append(this.driverSeatSlide);
        sb.append(", driverSeatHeight=");
        sb.append(this.driverSeatHeight);
        sb.append(", driverSeatAngle=");
        sb.append(this.driverSeatAngle);
        sb.append(", leftMirrorSwitch=");
        sb.append(this.leftMirrorSwitch);
        sb.append(", rightMirrorSwitch=");
        sb.append(this.rightMirrorSwitch);
        sb.append(", mirrorAdjust=");
        sb.append(this.mirrorAdjust);
        sb.append('}');
        return sb.toString();
    }
}

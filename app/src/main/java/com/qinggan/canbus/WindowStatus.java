package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

public final class WindowStatus implements Parcelable {
    public static final int ANTIPINCH = 5;
    public static final int ANTIPINCH_IN_PARTIALLT_SLIDE = 4;
    public static final int ANTIPINCH_IN_VENT = 2;
    public static final int AUTO_DOWN_MOVING = 5;
    public static final int AUTO_UP_MOVING = 3;
    public static final int CLOSED = 0;
    public static final Parcelable.Creator<WindowStatus> CREATOR = new Parcelable.Creator<WindowStatus>() { // from class: com.qinggan.canbus.WindowStatus.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public WindowStatus[] newArray(int size) {
            return new WindowStatus[size];
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public WindowStatus createFromParcel(Parcel source) {
            WindowStatus doorStatusValue = new WindowStatus();
            doorStatusValue.fLWindow = source.readInt();
            doorStatusValue.fRWindow = source.readInt();
            doorStatusValue.rLWindow = source.readInt();
            doorStatusValue.rRWindow = source.readInt();
            doorStatusValue.sunroof = source.readInt();
            doorStatusValue.roll = source.readInt();
            doorStatusValue.sunroofPosition = source.readInt();
            doorStatusValue.rollPosition = source.readInt();
            doorStatusValue.fLWindowPosition = source.readInt();
            doorStatusValue.fRWindowPosition = source.readInt();
            doorStatusValue.rLWindowPosition = source.readInt();
            doorStatusValue.rRWindowPosition = source.readInt();
            doorStatusValue.sunroofTilt = source.readInt();
            doorStatusValue.fLWindowHotProtect = source.readInt();
            doorStatusValue.fRWindowHotProtect = source.readInt();
            doorStatusValue.rLWindowHotProtect = source.readInt();
            doorStatusValue.rRWindowHotProtect = source.readInt();
            doorStatusValue.fLWindowLearn = source.readInt();
            doorStatusValue.fRWindowLearn = source.readInt();
            doorStatusValue.rLWindowLearn = source.readInt();
            doorStatusValue.rRWindowLearn = source.readInt();
            return doorStatusValue;
        }
    };
    public static final int FIRST_HALF_OF_SLIDE = 1;
    public static final int FULLY_CLOSE = 3;
    public static final int FULLY_OPEN = 6;
    public static final int HALF_OPEN = 4;
    public static final int MANUAL_DOWN_MOVING = 6;
    public static final int MANUAL_UP_MOVING = 4;
    public static final int OPEN = 1;
    public static final int PARTIALLT_SLIDE = 5;
    public static final int SECOND_HALF_OF_SLIDE = 2;
    public static final int STOPPED = 2;
    public static final int TILT_UP = 0;
    public static final int UNKNOWN = -1;
    public static final int VENT_AREA = 1;
    public int fLWindow;
    public int fLWindowHotProtect;
    public int fLWindowLearn;
    public int fLWindowPosition;
    public int fRWindow;
    public int fRWindowHotProtect;
    public int fRWindowLearn;
    public int fRWindowPosition;
    public int rLWindow;
    public int rLWindowHotProtect;
    public int rLWindowLearn;
    public int rLWindowPosition;
    public int rRWindow;
    public int rRWindowHotProtect;
    public int rRWindowLearn;
    public int rRWindowPosition;
    public int roll;
    public int rollPosition;
    public int sunroof;
    public int sunroofPosition;
    public int sunroofTilt;

    public int getFLWindow() {
        return this.fLWindow;
    }

    public void setFLWindow(int fLWindow) {
        this.fLWindow = fLWindow;
    }

    public int getFRWindow() {
        return this.fRWindow;
    }

    public void setFRWindow(int fRWindow) {
        this.fRWindow = fRWindow;
    }

    public int getRLWindow() {
        return this.rLWindow;
    }

    public void setRLWindow(int rLWindow) {
        this.rLWindow = rLWindow;
    }

    public int getRRWindow() {
        return this.rRWindow;
    }

    public void setRRWindow(int rRWindow) {
        this.rRWindow = rRWindow;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.fLWindow);
        dest.writeInt(this.fRWindow);
        dest.writeInt(this.rLWindow);
        dest.writeInt(this.rRWindow);
        dest.writeInt(this.sunroof);
        dest.writeInt(this.roll);
        dest.writeInt(this.sunroofPosition);
        dest.writeInt(this.rollPosition);
        dest.writeInt(this.fLWindowPosition);
        dest.writeInt(this.fRWindowPosition);
        dest.writeInt(this.rLWindowPosition);
        dest.writeInt(this.rRWindowPosition);
        dest.writeInt(this.sunroofTilt);
        dest.writeInt(this.fLWindowHotProtect);
        dest.writeInt(this.fRWindowHotProtect);
        dest.writeInt(this.rLWindowHotProtect);
        dest.writeInt(this.rRWindowHotProtect);
        dest.writeInt(this.fLWindowLearn);
        dest.writeInt(this.fRWindowLearn);
        dest.writeInt(this.rLWindowLearn);
        dest.writeInt(this.rRWindowLearn);
    }

    public String toString() {
        return "WindowStatus{fLWindow=" + this.fLWindow + ", fRWindow=" + this.fRWindow + ", rLWindow=" + this.rLWindow + ", rRWindow=" + this.rRWindow + ", sunroof=" + this.sunroof + ", roll=" + this.roll + ", sunroofPosition=" + this.sunroofPosition + ", rollPosition=" + this.rollPosition + ", fLWindowPosition=" + this.fLWindowPosition + ", fRWindowPosition=" + this.fRWindowPosition + ", rLWindowPosition=" + this.rLWindowPosition + ", rRWindowPosition=" + this.rRWindowPosition + ", sunroofTilt=" + this.sunroofTilt + ", fLWindowHotProtect=" + this.fLWindowHotProtect + ", fRWindowHotProtect=" + this.fRWindowHotProtect + ", rLWindowHotProtect=" + this.rLWindowHotProtect + ", rRWindowHotProtect=" + this.rRWindowHotProtect + ", fLWindowLearn=" + this.fLWindowLearn + ", fRWindowLearn=" + this.fRWindowLearn + ", rLWindowLearn=" + this.rLWindowLearn + ", rRWindowLearn=" + this.rRWindowLearn + '}';
    }
}

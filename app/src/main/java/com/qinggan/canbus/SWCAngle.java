package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

/* loaded from: classes.dex */
public final class SWCAngle implements Parcelable {
    public static final Parcelable.Creator<SWCAngle> CREATOR = new Parcelable.Creator<SWCAngle>() { // from class: com.qinggan.canbus.SWCAngle.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public SWCAngle[] newArray(int size) {
            return new SWCAngle[size];
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public SWCAngle createFromParcel(Parcel source) {
            SWCAngle dest = new SWCAngle();
            dest.mWheelDirection = source.readInt();
            dest.mWheelAngle = source.readInt();
            return dest;
        }
    };
    public static final int SWC_DIR_CENTER = 0;
    public static final int SWC_DIR_LEFT = 2;
    public static final int SWC_DIR_RIGHT = 1;
    public static final int UNKNOWN = Integer.MIN_VALUE;
    public int mWheelDirection = Integer.MIN_VALUE;
    public int mWheelAngle = Integer.MIN_VALUE;

    public int getWheelDirection() {
        return this.mWheelDirection;
    }

    public void setWheelDirection(int wheelDirection) {
        this.mWheelDirection = wheelDirection;
    }

    public int getWheelAngle() {
        return this.mWheelAngle;
    }

    public void setWheelAngle(int wheelAngle) {
        this.mWheelAngle = wheelAngle;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mWheelDirection);
        dest.writeInt(this.mWheelAngle);
    }

    public String toString() {
        return "SWCAngle{mWheelDirection=" + this.mWheelDirection + ", mWheelAngle=" + this.mWheelAngle + '}';
    }
}

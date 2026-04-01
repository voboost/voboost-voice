package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

/* loaded from: classes.dex */
public final class WheelCount implements Parcelable {
    public static final Parcelable.Creator<WheelCount> CREATOR = new Parcelable.Creator<WheelCount>() { // from class: com.qinggan.canbus.WheelCount.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public WheelCount[] newArray(int size) {
            return new WheelCount[size];
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public WheelCount createFromParcel(Parcel source) {
            WheelCount count = new WheelCount();
            count.rightWheelCount = source.readInt();
            count.leftWheelCount = source.readInt();
            return count;
        }
    };
    public static final int WHEEL_COUNT_UNKNOWN = Integer.MIN_VALUE;
    public int leftWheelCount;
    public int rightWheelCount;

    public int getRightWheelCount() {
        return this.rightWheelCount;
    }

    public int getLeftWheelCount() {
        return this.leftWheelCount;
    }

    public void setRightWheelCount(int rightWheelCount) {
        this.rightWheelCount = rightWheelCount;
    }

    public void setLeftWheelCount(int leftWheelCount) {
        this.leftWheelCount = leftWheelCount;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.rightWheelCount);
        dest.writeInt(this.leftWheelCount);
    }

    public String toString() {
        return "WheelCount{rightWheelCount=" + this.rightWheelCount + ", leftWheelCount=" + this.leftWheelCount + '}';
    }
}

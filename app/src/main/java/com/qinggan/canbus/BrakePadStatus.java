package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

/* loaded from: classes.dex */
public final class BrakePadStatus implements Parcelable {
    public static final Parcelable.Creator<BrakePadStatus> CREATOR = new Parcelable.Creator<BrakePadStatus>() { // from class: com.qinggan.canbus.BrakePadStatus.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public BrakePadStatus[] newArray(int size) {
            return new BrakePadStatus[size];
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public BrakePadStatus createFromParcel(Parcel source) {
            BrakePadStatus brakePad = new BrakePadStatus();
            brakePad.mFrontPadStatus = source.readInt();
            brakePad.mRearPadStatus = source.readInt();
            return brakePad;
        }
    };
    public int mFrontPadStatus;
    public int mRearPadStatus;

    public BrakePadStatus(int front, int rear) {
        this.mFrontPadStatus = front;
        this.mRearPadStatus = rear;
    }

    public BrakePadStatus() {
    }

    public int getFrontPad() {
        return this.mFrontPadStatus;
    }

    public int getRearPad() {
        return this.mRearPadStatus;
    }

    public void setFrontPad(int frontPad) {
        this.mFrontPadStatus = frontPad;
    }

    public void setRearPad(int rearPad) {
        this.mRearPadStatus = rearPad;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mFrontPadStatus);
        dest.writeInt(this.mRearPadStatus);
    }

    public String toString() {
        return "BrakePadStatus{mFrontPadStatus=" + this.mFrontPadStatus + ", mRearPadStatus=" + this.mRearPadStatus + '}';
    }
}

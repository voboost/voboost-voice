package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

/* loaded from: classes.dex */
public class PreChargeInfo implements Parcelable {
    public static final Parcelable.Creator<PreChargeInfo> CREATOR = new Parcelable.Creator<PreChargeInfo>() { // from class: com.qinggan.canbus.PreChargeInfo.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public PreChargeInfo createFromParcel(Parcel source) {
            PreChargeInfo info = new PreChargeInfo();
            return info;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public PreChargeInfo[] newArray(int size) {
            return new PreChargeInfo[size];
        }
    };
    public static final int OFF = 0;
    public static final int ON = 1;
    public int mStartHour = 0;
    public int mStartMinute = 0;
    public int mEndHour = 0;
    public int mEndMinute = 0;
    public int mUserDetermineFlag = 0;
    public int mStartFlag = 0;
    public int mCancelFlag = 0;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mStartHour);
    }
}

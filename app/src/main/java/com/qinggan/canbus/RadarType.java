package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

/* loaded from: classes.dex */
public enum RadarType implements Parcelable {
    Front,
    Rear,
    Left,
    Right;

    public static final Parcelable.Creator<RadarType> CREATOR = new Parcelable.Creator<RadarType>() { // from class: com.qinggan.canbus.RadarType.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public RadarType createFromParcel(Parcel source) {
            RadarType radar = RadarType.values()[source.readInt()];
            return radar;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public RadarType[] newArray(int size) {
            return new RadarType[size];
        }
    };

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
    }
}

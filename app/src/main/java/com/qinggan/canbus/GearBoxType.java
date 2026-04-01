package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

public enum GearBoxType implements Parcelable {
    Auto,
    Man;

    public static final Parcelable.Creator<GearBoxType> CREATOR = new Parcelable.Creator<GearBoxType>() { // from class: com.qinggan.canbus.GearBoxType.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public GearBoxType createFromParcel(Parcel source) {
            GearBoxType radar = GearBoxType.values()[source.readInt()];
            return radar;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public GearBoxType[] newArray(int size) {
            return new GearBoxType[size];
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

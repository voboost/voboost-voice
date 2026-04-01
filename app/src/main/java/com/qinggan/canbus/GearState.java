package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

public enum GearState implements Parcelable {
    Parking(0),
    Reverse(1),
    Neutral(2),
    Drive(3),
    Battery(4),
    Unknown(-1);

    public static final Parcelable.Creator<GearState> CREATOR = new Parcelable.Creator<GearState>() { // from class: com.qinggan.canbus.GearState.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public GearState createFromParcel(Parcel source) {
            GearState type = GearState.values()[source.readInt()];
            type.value = source.readInt();
            return type;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public GearState[] newArray(int size) {
            return new GearState[size];
        }
    };
    int value;

    GearState(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
        dest.writeInt(this.value);
    }
}

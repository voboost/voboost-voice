package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

/* loaded from: classes.dex */
public enum PhoneState implements Parcelable {
    NOTHING(0),
    COMING_CALL(1),
    HOLD(2),
    HANG_UP(3),
    GOING_CALL(4),
    CONNECTED(5);

    public static final Parcelable.Creator<PhoneState> CREATOR = new Parcelable.Creator<PhoneState>() { // from class: com.qinggan.canbus.PhoneState.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public PhoneState createFromParcel(Parcel source) {
            PhoneState type = PhoneState.values()[source.readInt()];
            type.value = source.readInt();
            return type;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public PhoneState[] newArray(int size) {
            return new PhoneState[size];
        }
    };
    int value;

    PhoneState(int value) {
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
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeInt(ordinal());
        arg0.writeInt(this.value);
    }
}

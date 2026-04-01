package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

public enum FunctionType implements Parcelable {
    VehicleState,
    TravellingInfo,
    CANSteeringKeySupport;

    public static final Parcelable.Creator<FunctionType> CREATOR = new Parcelable.Creator<FunctionType>() { // from class: com.qinggan.canbus.FunctionType.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public FunctionType createFromParcel(Parcel source) {
            FunctionType state = FunctionType.values()[source.readInt()];
            return state;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public FunctionType[] newArray(int size) {
            return new FunctionType[size];
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

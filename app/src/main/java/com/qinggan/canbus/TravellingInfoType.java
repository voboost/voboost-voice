package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

/* loaded from: classes.dex */
public enum TravellingInfoType implements Parcelable {
    DistanceSinceStart(32),
    DistanceSinceRefueling(33),
    DistanceLongTerm(34),
    AvgConsumptionSinceStart(48),
    AvgConsumptionSinceRefueling(49),
    AvgConsumptionLongTerm(50),
    AvgSpeedSinceStart(64),
    AvgSpeedSinceRefuelling(65),
    AvgSpeedLongTerm(66),
    TravellingTimeSinceStart(80),
    TravellingTimeSinceRefueling(81),
    TravellingTimeLongTerm(82);

    public static final Parcelable.Creator<TravellingInfoType> CREATOR = new Parcelable.Creator<TravellingInfoType>() { // from class: com.qinggan.canbus.TravellingInfoType.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public TravellingInfoType createFromParcel(Parcel source) {
            TravellingInfoType state = TravellingInfoType.values()[source.readInt()];
            state.value = source.readInt();
            return state;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public TravellingInfoType[] newArray(int size) {
            return new TravellingInfoType[size];
        }
    };
    private int value;

    TravellingInfoType(int value) {
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

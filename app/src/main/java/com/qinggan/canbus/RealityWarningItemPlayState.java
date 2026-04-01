package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

public enum RealityWarningItemPlayState implements Parcelable {
    ALWAY_PLAY(1),
    THIRTY_SECONDS_PLAY(2),
    SIXTY_SECONDS_PLAY(3),
    ONLY_ON_PLAY(4),
    NO_PLAY(5);

    public static final Parcelable.Creator<RealityWarningItemPlayState> CREATOR = new Parcelable.Creator<RealityWarningItemPlayState>() { // from class: com.qinggan.canbus.RealityWarningItemPlayState.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public RealityWarningItemPlayState createFromParcel(Parcel source) {
            RealityWarningItemPlayState realityWarningItemPlayState = RealityWarningItemPlayState.values()[source.readInt()];
            realityWarningItemPlayState.value = source.readInt();
            return realityWarningItemPlayState;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public RealityWarningItemPlayState[] newArray(int size) {
            return new RealityWarningItemPlayState[size];
        }
    };
    private int value;

    RealityWarningItemPlayState(int value) {
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

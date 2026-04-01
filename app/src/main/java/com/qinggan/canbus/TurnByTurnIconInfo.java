package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

public enum TurnByTurnIconInfo implements Parcelable {
    INVALID(0),
    DIRECT(1),
    TURN_LEFT(2),
    TURN_RIGHT(3),
    LEFT_DIRECT(4),
    RIGHT_DIRECT(5),
    TURN(6),
    RIGHT_BACK(7),
    LEFT_BACK(8),
    AROUND(9),
    AROUND_EXIT1(10),
    AROUND_EXIT2(11),
    AROUND_EXIT3(12),
    AROUND_EXIT4(13),
    AROUND_EXIT5(14),
    AROUND_EXIT6(15),
    AROUND_EXIT7(16),
    AROUND_EXIT8(17),
    AROUND_EXIT9(18),
    AROUND_EXIT10(19),
    MAIN_ROAD(20),
    ASSIST_AOAD(21),
    PASSING_POINT(22),
    TARGET(23),
    TUNNEL(24),
    ARIVE_SERVICE(25),
    ARIVE_CHARGE(26);

    public static final Parcelable.Creator<TurnByTurnIconInfo> CREATOR = new Parcelable.Creator<TurnByTurnIconInfo>() { // from class: com.qinggan.canbus.TurnByTurnIconInfo.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public TurnByTurnIconInfo createFromParcel(Parcel source) {
            TurnByTurnIconInfo type = TurnByTurnIconInfo.values()[source.readInt()];
            type.value = source.readInt();
            return type;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public TurnByTurnIconInfo[] newArray(int size) {
            return new TurnByTurnIconInfo[size];
        }
    };
    int value;

    TurnByTurnIconInfo(int value) {
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

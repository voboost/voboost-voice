package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

public final class SeatBelt implements Parcelable {
    public static final Parcelable.Creator<SeatBelt> CREATOR = new Parcelable.Creator<SeatBelt>() { // from class: com.qinggan.canbus.SeatBelt.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public SeatBelt[] newArray(int size) {
            return new SeatBelt[size];
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public SeatBelt createFromParcel(Parcel source) {
            SeatBelt SeatBelt = new SeatBelt();
            SeatBelt.driverSeatBeltState = source.readInt();
            SeatBelt.passengerSeatBeltState = source.readInt();
            SeatBelt.secondLeftSeatBeltState = source.readInt();
            SeatBelt.secondMidSeatBeltState = source.readInt();
            SeatBelt.secondRightSeatBeltState = source.readInt();
            return SeatBelt;
        }
    };
    public static final int SEATBELT_STATE_PULLED = 0;
    public static final int SEATBELT_STATE_PUSHED = 1;
    public static final int SEATBELT_STATE_UNSUPPORT = -1;
    public int driverSeatBeltState;
    public int passengerSeatBeltState;
    public int secondLeftSeatBeltState;
    public int secondMidSeatBeltState;
    public int secondRightSeatBeltState;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public int getDriverSeatBeltState() {
        return this.driverSeatBeltState;
    }

    public void setDriverSeatBeltState(int state) {
        this.driverSeatBeltState = state;
    }

    public int getPassengerSeatBeltState() {
        return this.passengerSeatBeltState;
    }

    public void setPassengerSeatBeltState(int state) {
        this.passengerSeatBeltState = state;
    }

    public int getSecondLeftSeatBeltState() {
        return this.secondLeftSeatBeltState;
    }

    public void setSecondLeftSeatBeltState(int secondLeftSeatBeltState) {
        this.secondLeftSeatBeltState = secondLeftSeatBeltState;
    }

    public int getSecondMidSeatBeltState() {
        return this.secondMidSeatBeltState;
    }

    public void setSecondMidSeatBeltState(int secondMidSeatBeltState) {
        this.secondMidSeatBeltState = secondMidSeatBeltState;
    }

    public int getSecondRightSeatBeltState() {
        return this.secondRightSeatBeltState;
    }

    public void setSecondRightSeatBeltState(int secondRightSeatBeltState) {
        this.secondRightSeatBeltState = secondRightSeatBeltState;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.driverSeatBeltState);
        dest.writeInt(this.passengerSeatBeltState);
        dest.writeInt(this.secondLeftSeatBeltState);
        dest.writeInt(this.secondMidSeatBeltState);
        dest.writeInt(this.secondRightSeatBeltState);
    }

    public String toString() {
        return "SeatBelt{driverSeatBeltState=" + this.driverSeatBeltState + ", passengerSeatBeltState=" + this.passengerSeatBeltState + ", secondLeftSeatBeltState=" + this.secondLeftSeatBeltState + ", secondMidSeatBeltState=" + this.secondMidSeatBeltState + ", secondRightSeatBeltState=" + this.secondRightSeatBeltState + '}';
    }
}

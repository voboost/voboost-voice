package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

/* loaded from: classes.dex */
public enum CalcState implements Parcelable {
    CALC_BEGIN,
    CALC_SUCCESS,
    CALC_FAIL,
    UNKNOWN;

    public static final Parcelable.Creator<CalcState> CREATOR = new Parcelable.Creator<CalcState>() { // from class: com.qinggan.canbus.CalcState.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public CalcState createFromParcel(Parcel source) {
            CalcState state = CalcState.values()[source.readInt()];
            return state;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public CalcState[] newArray(int size) {
            return new CalcState[size];
        }
    };

    public static CalcState parseCaclState(String state) {
        if (state.equals("calc_begin")) {
            return CALC_BEGIN;
        }
        if (state.equals("calc_success")) {
            return CALC_SUCCESS;
        }
        if (state.equals("calc_fail")) {
            return CALC_FAIL;
        }
        return UNKNOWN;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
    }
}

package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

/* loaded from: classes.dex */
public enum MediaState implements Parcelable {
    CLEAN(0),
    PLAY(1),
    PAUSE(2),
    STOP(3);

    public static final Parcelable.Creator<MediaState> CREATOR = new Parcelable.Creator<MediaState>() { // from class: com.qinggan.canbus.MediaState.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaState createFromParcel(Parcel source) {
            MediaState type = MediaState.values()[source.readInt()];
            type.value = source.readInt();
            return type;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaState[] newArray(int size) {
            return new MediaState[size];
        }
    };
    int value;

    MediaState(int value) {
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

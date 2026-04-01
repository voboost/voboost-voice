package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

public enum MediaType implements Parcelable {
    Tuner(1),
    SimpleAudio(16),
    EnhancedAudio(17),
    IPod(18),
    LocalVideo(32),
    DVD(33),
    OtherVideo(34),
    Aux(48),
    Phone(64);

    public static final Parcelable.Creator<MediaType> CREATOR = new Parcelable.Creator<MediaType>() { // from class: com.qinggan.canbus.MediaType.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaType createFromParcel(Parcel source) {
            MediaType type = MediaType.values()[source.readInt()];
            type.value = source.readInt();
            return type;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaType[] newArray(int size) {
            return new MediaType[size];
        }
    };
    int value;

    MediaType(int value) {
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

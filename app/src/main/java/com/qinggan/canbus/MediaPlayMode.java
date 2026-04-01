package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

public enum MediaPlayMode implements Parcelable {
    Sequence(0),
    Preview(1),
    Random(2),
    Single(3);

    public static final Parcelable.Creator<MediaPlayMode> CREATOR = new Parcelable.Creator<MediaPlayMode>() { // from class: com.qinggan.canbus.MediaPlayMode.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaPlayMode createFromParcel(Parcel source) {
            MediaPlayMode type = MediaPlayMode.values()[source.readInt()];
            type.value = source.readInt();
            return type;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaPlayMode[] newArray(int size) {
            return new MediaPlayMode[size];
        }
    };
    int value;

    MediaPlayMode(int value) {
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

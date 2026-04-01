package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

public enum MediaSrcType implements Parcelable {
    OFF(0),
    FM(1),
    AM(2),
    Disc(3),
    TV(4),
    Navi(5),
    Phone(6),
    IPod(7),
    AUX(8),
    USB(9),
    SD(10),
    DVBT(11),
    A2DP(12),
    Other(13),
    CDC(14),
    Network(36),
    QQ(37),
    IPDALink(38),
    NetworkMusic(39),
    NetworkRadio(40),
    USB2(41),
    USB1_DISMISS(42),
    USB2_DISMISS(43),
    A2DP_DISMISS(44),
    XMLY(45);

    public static final Parcelable.Creator<MediaSrcType> CREATOR = new Parcelable.Creator<MediaSrcType>() { // from class: com.qinggan.canbus.MediaSrcType.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaSrcType createFromParcel(Parcel source) {
            MediaSrcType type = MediaSrcType.values()[source.readInt()];
            type.value = source.readInt();
            return type;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaSrcType[] newArray(int size) {
            return new MediaSrcType[size];
        }
    };
    int value;

    MediaSrcType(int value) {
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

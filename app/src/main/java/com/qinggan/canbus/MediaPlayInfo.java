package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

public final class MediaPlayInfo implements Parcelable {
    public static final Parcelable.Creator<MediaPlayInfo> CREATOR = new Parcelable.Creator<MediaPlayInfo>() { // from class: com.qinggan.canbus.MediaPlayInfo.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaPlayInfo[] newArray(int size) {
            return new MediaPlayInfo[size];
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaPlayInfo createFromParcel(Parcel source) {
            MediaPlayInfo info = new MediaPlayInfo();
            info.songName = source.readString();
            info.artist = source.readString();
            info.albumName = source.readString();
            return info;
        }
    };
    String albumName;
    String artist;
    String songName;

    public String getSongName() {
        return this.songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getArtist() {
        return this.artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbumName() {
        return this.albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.songName);
        dest.writeString(this.artist);
        dest.writeString(this.albumName);
    }

    public String toString() {
        return "MediaPlayInfo{songName='" + this.songName + "', artist='" + this.artist + "', albumName='" + this.albumName + "'}";
    }
}

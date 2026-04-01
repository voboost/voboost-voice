package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

public final class MediaSrcInfo implements Parcelable {
    public static final Parcelable.Creator<MediaSrcInfo> CREATOR = new Parcelable.Creator<MediaSrcInfo>() { // from class: com.qinggan.canbus.MediaSrcInfo.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaSrcInfo[] newArray(int size) {
            return new MediaSrcInfo[size];
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaSrcInfo createFromParcel(Parcel source) {
            MediaSrcInfo info = new MediaSrcInfo();
            info.srcType = (MediaSrcType) source.readParcelable(MediaSrcType.class.getClassLoader());
            info.mediaType = (MediaType) source.readParcelable(MediaType.class.getClassLoader());
            info.frequency = source.readFloat();
            info.mediaState = (MediaState) source.readParcelable(MediaState.class.getClassLoader());
            info.songName = source.readString();
            info.artist = source.readString();
            info.albumName = source.readString();
            return info;
        }
    };
    String albumName;
    String artist;
    float frequency;
    MediaState mediaState;
    MediaType mediaType;
    String songName;
    MediaSrcType srcType;

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

    public MediaState getMediaState() {
        return this.mediaState;
    }

    public void setMediaState(MediaState mediaState) {
        this.mediaState = mediaState;
    }

    public MediaSrcInfo(MediaSrcType type) {
        this.srcType = type;
    }

    public MediaSrcInfo() {
    }

    public MediaSrcType getSrcType() {
        return this.srcType;
    }

    public void setSrcType(MediaSrcType srcType) {
        this.srcType = srcType;
    }

    public MediaType getMediaType() {
        return this.mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public float getFrequency() {
        return this.frequency;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.srcType, flags);
        dest.writeParcelable(this.mediaType, flags);
        dest.writeFloat(this.frequency);
        dest.writeParcelable(this.mediaState, flags);
        dest.writeString(this.songName);
        dest.writeString(this.artist);
        dest.writeString(this.albumName);
    }

    public String toString() {
        return "MediaSrcInfo{srcType=" + this.srcType + ", mediaType=" + this.mediaType + ", frequency=" + this.frequency + ", mediaState" + this.mediaState + ",songName" + this.songName + ",artist" + this.artist + ",albumName" + this.albumName + '}';
    }
}

package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

/* loaded from: classes.dex */
public final class MediaPlayProgressInfo implements Parcelable {
    public static final Parcelable.Creator<MediaPlayProgressInfo> CREATOR = new Parcelable.Creator<MediaPlayProgressInfo>() { // from class: com.qinggan.canbus.MediaPlayProgressInfo.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaPlayProgressInfo[] newArray(int size) {
            return new MediaPlayProgressInfo[size];
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public MediaPlayProgressInfo createFromParcel(Parcel source) {
            MediaPlayProgressInfo info = new MediaPlayProgressInfo();
            info.mediaType = source.readInt();
            info.currentTime = source.readLong();
            info.playTime = source.readLong();
            info.percent = source.readInt();
            return info;
        }
    };
    long curTime;
    long currentTime;
    int mediaType;
    int percent;
    long playTime;

    public int getMediaType() {
        return this.mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public long getPlayTime() {
        return this.playTime;
    }

    public void setPlayTime(long playTime) {
        this.playTime = playTime;
    }

    public long getCurrentTime() {
        return this.currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public int getPercent() {
        return this.percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mediaType);
        dest.writeLong(this.currentTime);
        dest.writeLong(this.playTime);
        dest.writeInt(this.percent);
    }

    public String toString() {
        return "MediaPlayProgressInfo{mediaType='" + this.mediaType + "', currentTime='" + this.currentTime + "', playTime='" + this.playTime + "', percent='" + this.percent + "'}";
    }
}

package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

public class DVRState implements Parcelable {
    public static final Parcelable.Creator<DVRState> CREATOR = new Parcelable.Creator<DVRState>() { // from class: com.qinggan.canbus.DVRState.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public DVRState[] newArray(int size) {
            return new DVRState[size];
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public DVRState createFromParcel(Parcel source) {
            DVRState dvrState = new DVRState();
            dvrState.dvrSysState = source.readInt();
            dvrState.dvrWifiState = source.readInt();
            dvrState.dvrPhotoRes = source.readInt();
            dvrState.dvrsdcardState = source.readInt();
            return dvrState;
        }
    };
    public static final int DVR_PHOTO_FAIL = 2;
    public static final int DVR_PHOTO_FULL = 3;
    public static final int DVR_PHOTO_NO_DISPLAY = 0;
    public static final int DVR_PHOTO_SUCCESS = 1;
    public static final int DVR_SYS_ERROR = 6;
    public static final int DVR_SYS_INIT = 0;
    public static final int DVR_SYS_RECORD = 1;
    public static final int DVR_SYS_RECORD_BY_CRASH = 4;
    public static final int DVR_SYS_RECORD_BY_USER = 3;
    public static final int DVR_SYS_RECORD_PAUSR = 2;
    public static final int DVR_SYS_UPDATE = 5;
    public static final int DVR_WIFI_CLOSE = 0;
    public static final int DVR_WIFI_CONNECT = 3;
    public static final int DVR_WIFI_ERROR = 4;
    public static final int DVR_WIFI_INIT = 1;
    public static final int DVR_WIFI_NORMAL = 2;
    public static final int H97_DVR_PHOTO_NO_DISPLAY = 0;
    public static final int H97_DVR_PHOTO_SUCCESS = 1;
    public static final int H97_DVR_SD_ERROR = 1;
    public static final int H97_DVR_SD_FORMATE = 4;
    public static final int H97_DVR_SD_NOMAL = 0;
    public static final int H97_DVR_SD_PHOTO_FULL = 3;
    public static final int H97_DVR_SD_URGENT_FULL = 2;
    public static final int H97_DVR_SYS_ERROR = 7;
    public static final int H97_DVR_SYS_INIT = 1;
    public static final int H97_DVR_SYS_OFF = 0;
    public static final int H97_DVR_SYS_RECORD = 2;
    public static final int H97_DVR_SYS_RECORD_MONITOR = 6;
    public static final int H97_DVR_SYS_RECORD_STOP = 3;
    public static final int H97_DVR_SYS_RECORD_URGENT = 4;
    public static final int H97_DVR_SYS_VIDEO_PLAY = 5;
    private int dvrSysState = -1;
    private int dvrWifiState = -1;
    private int dvrPhotoRes = -1;
    private int dvrsdcardState = -1;

    public void setDvrSysState(int state) {
        this.dvrSysState = state;
    }

    public int getDvrSysState() {
        return this.dvrSysState;
    }

    public void setDvrWifiState(int state) {
        this.dvrWifiState = state;
    }

    public int getDvrWifiState() {
        return this.dvrWifiState;
    }

    public void setDvrPhotoRes(int res) {
        this.dvrPhotoRes = res;
    }

    public int getDvrPhotoRes() {
        return this.dvrPhotoRes;
    }

    public void setDvrSdcardState(int state) {
        this.dvrsdcardState = state;
    }

    public int getDvrSdcardState() {
        return this.dvrsdcardState;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.dvrSysState);
        dest.writeInt(this.dvrWifiState);
        dest.writeInt(this.dvrPhotoRes);
        dest.writeInt(this.dvrsdcardState);
    }
}

package com.qinggan.canbus;

import android.os.Parcel;
import android.os.Parcelable;

/* loaded from: classes.dex */
public class CameraState implements Parcelable {
    public static final int CAMERA_SYS_CLOSE = 0;
    public static final int CAMERA_SYS_ERROR = 2;
    public static final int CAMERA_SYS_OPEN = 1;
    public static final Parcelable.Creator<CameraState> CREATOR = new Parcelable.Creator<CameraState>() { // from class: com.qinggan.canbus.CameraState.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public CameraState[] newArray(int size) {
            return new CameraState[size];
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public CameraState createFromParcel(Parcel source) {
            CameraState cameraState = new CameraState();
            cameraState.faceidLoginState = source.readInt();
            cameraState.faceidRegistState = source.readInt();
            cameraState.arCameraState = source.readInt();
            cameraState.dvrHicarState = source.readInt();
            cameraState.roaHicarState = source.readInt();
            cameraState.dmsHicarState = source.readInt();
            cameraState.dvrAPPState = source.readInt();
            cameraState.roaAPPState = source.readInt();
            cameraState.dvrCameraState = source.readInt();
            cameraState.roaCameraState = source.readInt();
            cameraState.dmsCameraState = source.readInt();
            return cameraState;
        }
    };
    public static final int FACEID_DEFAULT = 0;
    public static final int FACEID_LOGIN_FAIL = 6;
    public static final int FACEID_LOGIN_INPROGRESS = 4;
    public static final int FACEID_LOGIN_SUCCESS = 5;
    public static final int FACEID_REGIST_FAIL = 3;
    public static final int FACEID_REGIST_INPROGRESS = 1;
    public static final int FACEID_REGIST_SUCCESS = 2;
    private int faceidLoginState = -1;
    private int faceidRegistState = -1;
    private int arCameraState = -1;
    private int dvrHicarState = -1;
    private int roaHicarState = -1;
    private int dmsHicarState = -1;
    private int dvrAPPState = -1;
    private int roaAPPState = -1;
    private int dvrCameraState = -1;
    private int roaCameraState = -1;
    private int dmsCameraState = -1;
    private int nvsCameraState = -1;
    private int dmsAPPState = -1;

    public void setfaceidLoginState(int state) {
        this.faceidLoginState = state;
    }

    public int getfaceidLoginState() {
        return this.faceidLoginState;
    }

    public void setfaceidRegistState(int state) {
        this.faceidRegistState = state;
    }

    public int getfaceidRegistState() {
        return this.faceidRegistState;
    }

    public void setarCameraState(int state) {
        this.arCameraState = state;
    }

    public int getarCameraState() {
        return this.arCameraState;
    }

    public void setdvrHicarState(int state) {
        this.dvrHicarState = state;
    }

    public int getdvrHicarState() {
        return this.dvrHicarState;
    }

    public void setroaHicarState(int state) {
        this.roaHicarState = state;
    }

    public int getroaHicarState() {
        return this.roaHicarState;
    }

    public void setdmsHicarState(int state) {
        this.dmsHicarState = state;
    }

    public int getdmsHicarState() {
        return this.dmsHicarState;
    }

    public void setdvrAPPState(int state) {
        this.dvrAPPState = state;
    }

    public int getdvrAPPState() {
        return this.dvrAPPState;
    }

    public void setroaAPPState(int state) {
        this.roaAPPState = state;
    }

    public int getroaAPPState() {
        return this.roaAPPState;
    }

    public void setdvrCameraState(int state) {
        this.dvrCameraState = state;
    }

    public int getdvrCameraState() {
        return this.dvrCameraState;
    }

    public void setroaCameraState(int state) {
        this.roaCameraState = state;
    }

    public int getroaCameraState() {
        return this.roaCameraState;
    }

    public void setdmsCameraState(int state) {
        this.dmsCameraState = state;
    }

    public int getdmsCameraState() {
        return this.dmsCameraState;
    }

    public void setdmsAPPState(int state) {
        this.dmsAPPState = state;
    }

    public int getdmsAPPState() {
        return this.dmsAPPState;
    }

    public void setnvsCameraState(int state) {
        this.nvsCameraState = state;
    }

    public int getnvsCameraState() {
        return this.nvsCameraState;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.faceidLoginState);
        dest.writeInt(this.faceidRegistState);
        dest.writeInt(this.arCameraState);
        dest.writeInt(this.dvrHicarState);
        dest.writeInt(this.roaHicarState);
        dest.writeInt(this.dmsHicarState);
        dest.writeInt(this.dvrAPPState);
        dest.writeInt(this.roaAPPState);
        dest.writeInt(this.dvrCameraState);
        dest.writeInt(this.roaCameraState);
        dest.writeInt(this.dmsCameraState);
    }

    public String toString() {
        return "cameraState{prio1===>[(dms arg)faceidLoginState=" + this.faceidLoginState + ",(dms)faceidRegistState=" + this.faceidRegistState + "],prio2===>[(dvr)arCameraState=" + this.arCameraState + "],prio3===>[(dvr)dvrHicarState=" + this.dvrHicarState + ",prio3  (roa)roaHicarState=" + this.roaHicarState + ",prio3  (dms)dmsHicarState=" + this.dmsHicarState + "],prio4===>[(dvr)dvrAPPState=" + this.dvrAPPState + ",prio4  (roa)roaAPPState=" + this.roaAPPState + ",prio4  (dvr)dvrCameraState=" + this.dvrCameraState + ",prio4  (roa)roaCameraState=" + this.roaCameraState + ",prio4  (dms)dmsCameraState=" + this.dmsCameraState + "],end  =================================================}";
    }
}

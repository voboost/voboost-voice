package com.qinggan.audiopolicy;

import android.os.Parcel;
import android.os.Parcelable;

/* loaded from: classes.dex */
public class AudioPolicyInfo implements Parcelable {
    public static final Parcelable.Creator<AudioPolicyInfo> CREATOR = new Parcelable.Creator<AudioPolicyInfo>() { // from class: com.qinggan.audiopolicy.AudioPolicyInfo.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public AudioPolicyInfo createFromParcel(Parcel parcel) {
            AudioPolicyInfo audioPolicyInfo = new AudioPolicyInfo();
            audioPolicyInfo.packageName = parcel.readString();
            audioPolicyInfo.streamType = parcel.readInt();
            audioPolicyInfo.clientId = parcel.readString();
            return audioPolicyInfo;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public AudioPolicyInfo[] newArray(int i) {
            return new AudioPolicyInfo[i];
        }
    };
    String packageName = "";
    int streamType = -1;
    String clientId = "";

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String str) {
        this.packageName = str;
    }

    public int getStreamType() {
        return this.streamType;
    }

    public void setStreamType(int i) {
        this.streamType = i;
    }

    public String getClientId() {
        return this.clientId;
    }

    public void setClientId(String str) {
        this.clientId = str;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.packageName);
        parcel.writeInt(this.streamType);
        parcel.writeString(this.clientId);
    }

    public String toString() {
        return "AudioPolicyInfo{packageName='" + this.packageName + "', streamType=" + this.streamType + ", clientId='" + this.clientId + "'}";
    }
}

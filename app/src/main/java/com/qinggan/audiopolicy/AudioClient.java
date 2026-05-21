package com.qinggan.audiopolicy;

import android.os.Parcel;
import android.os.Parcelable;

public class AudioClient implements Parcelable {
    public static final Parcelable.Creator<AudioClient> CREATOR = new Parcelable.Creator<AudioClient>() {
        @Override
        public AudioClient createFromParcel(Parcel source) {
            AudioClient client = new AudioClient();
            client.packageName = source.readString();
            client.streamType = source.readInt();
            client.state = source.readInt();
            client.clientId = source.readString();
            return client;
        }

        @Override
        public AudioClient[] newArray(int size) {
            return new AudioClient[size];
        }
    };

    String clientId;
    String packageName;
    int state;
    int streamType;

    public AudioClient() {
        this.packageName = "";
        this.streamType = -1;
        this.state = -1;
        this.clientId = "";
    }

    public AudioClient(String pkg, String cid, int stream, int st) {
        this.packageName = "";
        this.streamType = -1;
        this.state = -1;
        this.clientId = "";
        this.packageName = pkg;
        this.clientId = cid;
        this.streamType = stream;
        this.state = st;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageName);
        dest.writeInt(this.streamType);
        dest.writeInt(this.state);
        dest.writeString(this.clientId);
    }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public int getStreamType() { return streamType; }
    public void setStreamType(int streamType) { this.streamType = streamType; }
    public int getState() { return state; }
    public void setState(int state) { this.state = state; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    @Override
    public String toString() {
        return "AudioClient [pkg:" + packageName + ", stream:" + streamType + ", state:" + state + ", cid:" + clientId + "]";
    }
}

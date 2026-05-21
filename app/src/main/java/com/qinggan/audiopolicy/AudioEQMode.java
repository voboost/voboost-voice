package com.qinggan.audiopolicy;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.HashMap;

/* loaded from: classes.dex */
public class AudioEQMode implements Parcelable {
    public static final Parcelable.Creator<AudioEQMode> CREATOR = new Parcelable.Creator<AudioEQMode>() { // from class: com.qinggan.audiopolicy.AudioEQMode.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public AudioEQMode createFromParcel(Parcel parcel) {
            AudioEQMode audioEQMode = new AudioEQMode();
            audioEQMode.name = parcel.readString();
            audioEQMode.mBandValues = parcel.readHashMap(AudioEQMode.class.getClassLoader());
            return audioEQMode;
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public AudioEQMode[] newArray(int i) {
            return new AudioEQMode[i];
        }
    };
    private static final String EQ_BAND_KEY_PREFIX = "band";
    private static final String TAG = "AudioEQMode";
    HashMap<String, Integer> mBandValues;
    String name;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public HashMap<String, Integer> getBandValues() {
        return this.mBandValues;
    }

    public void setBandValues(HashMap<String, Integer> map) {
        this.mBandValues = map;
    }

    public static String getBandKey(int i) {
        return "band" + i;
    }

    public static int getBandIndexByName(String str) {
        if (str == null) {
            return -1;
        }
        try {
            return Integer.parseInt(str.substring(str.indexOf("band") + 4));
        } catch (NumberFormatException e) {
            Log.e(TAG, "parse eq band key exception:" + e.getMessage());
            return -1;
        }
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.name);
        parcel.writeMap(this.mBandValues);
    }

    public String toString() {
        return "AudioEQMode{name='" + this.name + "', mBandValues=" + this.mBandValues + '}';
    }
}

// 📁 File: src/main/java/com/qinggan/audiorecord/record/NativeRecord.java
package com.qinggan.audiorecord.record;

import android.util.Log;
import com.qinggan.audiorecord.record.api.IRecord;
import com.qinggan.audiorecord.record.api.IRecordListener;

public class NativeRecord extends IRecord {

    private static final String TAG = "NativeRecord";

    // ===== Static native methods (как в оригинале) =====
    private static native int registerRecordListener(IRecordListener listener);
    private static native int setChannelNum(int channels);
    private static native int startAudioRecord();
    private static native int stopAudioRecord();
    private static native int unRegisterRecordListener(IRecordListener listener);

    // ===== Singleton =====
    private static volatile NativeRecord instance;

    private NativeRecord() {
        // Private constructor for singleton
    }

    public static NativeRecord getInstance() {
        if (instance == null) {
            synchronized (NativeRecord.class) {
                if (instance == null) {
                    instance = new NativeRecord();
                }
            }
        }
        return instance;
    }

    // ===== Library loading =====
    static {
        try {
            //System.loadLibrary("SpeechRecord4Mic");
            Log.i(TAG, "✅ libSpeechRecord4Mic.so loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "❌ Failed to load libSpeechRecord4Mic.so: " + e.getMessage(), e);
            throw e;
        }
    }

    // ===== Implementation of IRecord abstract methods =====

    @Override
    public int getChannelNumb() {
        return 6; // default: 4 mic + 2 reference for AEC
    }

    @Override
    public void setChannelNumb(int i) {
        int result = setChannelNum(i);
        Log.d(TAG, "setChannelNum(" + i + ") = " + result);
    }

    @Override
    public void startRecord() {
        java.util.ArrayList<IRecordListener> listeners = getRecordListeners();
        if (listeners.isEmpty()) {
            Log.w(TAG, "startRecord() called but no listeners registered");
            return;
        }

        for (IRecordListener listener : listeners) {
            int result = registerRecordListener(listener);
            Log.d(TAG, "registerRecordListener() = " + result);
            if (result != 0) {
                Log.w(TAG, "registerRecordListener failed with code " + result);
            }
        }

        int startResult = startAudioRecord();
        Log.d(TAG, "startAudioRecord() = " + startResult);
    }

    @Override
    public void stopRecord() {
        int result = stopAudioRecord();
        Log.d(TAG, "stopAudioRecord() = " + result);
    }

    @Override
    public void release() {
        for (IRecordListener listener : getRecordListeners()) {
            try {
                unRegisterRecordListener(listener);
            } catch (Exception e) {
                Log.w(TAG, "Failed to unregister listener", e);
            }
        }
        Log.d(TAG, "release() done");
    }

    // ===== Helper methods =====

    public boolean initialize(int channels) {
        try {
            setChannelNum(channels);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize", e);
            return false;
        }
    }

    public boolean initialize() {
        return initialize(6);
    }

    public boolean startWithListener(IRecordListener listener) {
        try {
            addRecordListener(listener);
            startRecord();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to start with listener", e);
            return false;
        }
    }
}
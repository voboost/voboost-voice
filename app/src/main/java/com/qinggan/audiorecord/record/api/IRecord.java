package com.qinggan.audiorecord.record.api;

import java.util.ArrayList;

/* loaded from: classes2.dex */
public abstract class IRecord {
    private ArrayList<IRecordListener> mRecordListeners = new ArrayList<>();

    public abstract int getChannelNumb();

    public abstract void release();

    public abstract void setChannelNumb(int i);

    public abstract void startRecord();

    public abstract void stopRecord();

    public void addRecordListener(IRecordListener iRecordListener) {
        synchronized (this.mRecordListeners) {
            this.mRecordListeners.add(iRecordListener);
        }
    }

    public void removeRecordListener(IRecordListener iRecordListener) {
        synchronized (this.mRecordListeners) {
            this.mRecordListeners.remove(iRecordListener);
        }
    }

    public ArrayList<IRecordListener> getRecordListeners() {
        return this.mRecordListeners;
    }
}

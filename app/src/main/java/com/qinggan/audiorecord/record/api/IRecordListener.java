package com.qinggan.audiorecord.record.api;

/* loaded from: classes2.dex */
public interface IRecordListener {
    void onData(byte[] bArr, int i);

    void onStart();

    void onStop();
}

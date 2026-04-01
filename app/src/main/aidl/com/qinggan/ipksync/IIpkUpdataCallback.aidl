// IUploadProgressCallback.aidl
package com.qinggan.ipksync;

// Declare any non-default types here with import statements

interface IIpkUpdataCallback {
    void onUploadState(int state);
    void onUploadProgress(String path, long size);
    void onReqFile(int index);

    void onLogState(int state);
    void onUpgradeProgress(int present);
}

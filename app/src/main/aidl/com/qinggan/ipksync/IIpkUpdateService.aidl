package com.qinggan.ipksync;
import com.qinggan.ipksync.IIpkUpdataCallback;

interface IIpkUpdateService {

    void startUpload();
    void stopUpload();
    void setCallbackListener(in IIpkUpdataCallback listener);
    boolean isInitUpdate();
}

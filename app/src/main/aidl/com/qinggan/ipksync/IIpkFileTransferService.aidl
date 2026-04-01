package com.qinggan.ipksync;
import com.qinggan.ipksync.IIpkUpdataCallback;

interface IIpkFileTransferService {

    void startUpload(String filePath);
    void stopUpload();
    void setCallbackListener(in IIpkUpdataCallback listener);
    boolean isInitUpdate();
}

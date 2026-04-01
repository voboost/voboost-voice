package com.qinggan.ipksync;

import com.qinggan.ipksync.IIpkLogCallback;

interface IIpkLogService {
    void startExtractIpkLog();
    void exitExtractIpkLog();
    void setIpkLogCallback(in IIpkLogCallback callback);
}

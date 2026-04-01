package com.qinggan.ipksync;

// Declare any non-default types here with import statements

interface IIpkLogCallback {
    void onExtractIpkLogState(int state);
    void onExtractLogProgress(int progress);
}

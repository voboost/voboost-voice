package com.qinggan.qinglink.api.hu;

interface IMicphoneModeListener {
    void onConnect(boolean connect);
    void onSetMicMode(int mode);
    void onGetCurrentMicMode();
    void onGetCurrentAudioSourceAngle();
}

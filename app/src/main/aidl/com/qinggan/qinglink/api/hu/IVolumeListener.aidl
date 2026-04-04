package com.qinggan.qinglink.api.hu;

interface IVolumeListener {
    void onConnect(boolean connect);
    void onGetCurrentMediaVolume();
    void onSetCurrentMediaVolume(int volume);
    void onGetCurrentNavigationVolume();
    void onSetCurrentNavigationVolume(int volume);
    void onGetCurrentPhoneVolume();
    void onSetCurrentPhoneVolume(int volume);
    void onGetCurrentNotificationVolume();
    void onSetCurrentNotificationVolume(int volume);
    void onSetEqualizer(String json);
    void onGetEqualizer();
    void onVolumeUp();
    void onVolumeDown();
    void onGetCurrentA2DPVolume();
    void onSetCurrentA2DPVolume(int volume);
    void onSetBalanceFaderLevel(int position, int balance, int fader);
    void onSetSpeedVolumeModeType(int type);
    void onSetLoudness(boolean open);
    void onGetBalanceFaderLevel();
    void onGetSpeedVolumeModeType();
    void onGetLoudness();
    void onSetMute(boolean isMute);
    void onGetMute();
    void onRequestAudioPolicy(int streamType, String clientId);
    void onAbandonAudioPolicy(String clientId);
    void onGetMDVolumeResponse(int type, int streamType, int volume);
    void onMDVolumeUpdate(int streamType, int volume);
    void onGetCurrentAudioPolicyInfo();
}

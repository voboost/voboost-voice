package com.qinggan.qinglink.api.hu;

import com.qinggan.qinglink.api.hu.IVolumeListener;

interface IVolume {
    boolean registerListener(IVolumeListener listener);
    boolean sendCurrentMediaVolumeResponse(int volume);
    boolean sendCurrentNavigationVolumeResponse(int volume);
    boolean sendCurrentPhoneVolumeResponse(int volume);
    boolean sendCurrentNotificationVolumeResponse(int volume);
    boolean sendEqualizerResponse(String json);
    boolean sendVolumeUp();
    boolean sendVolumeDown();
    boolean sendCurrentA2DPVolumeResponse(int volume);
    boolean sendBalanceFaderLevelResponse(int position, int balance, int fader);
    boolean sendSpeedVolumeModeTypeResponse(int type);
    boolean sendLoudnessResponse(boolean open);
    boolean sendMuteResponse(boolean isMute);
    boolean sendRequestAudioPolicyResult(String clientId, int result);
    boolean sendAbandonAudioPolicyResult(String clientId, int result);
    boolean sendAudioPolicyMessage(String clientId, int message);
    boolean sendGetMDVolume(int type, int streamType);
    boolean sendSetMDVolume(int streamType, int volume);
    boolean sendHUVolumeUpdate(int streamType, int volume);
    boolean sendCurrentAudioPolicyInfoResponse(int streamType, String pkgName);
}

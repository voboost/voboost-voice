package com.qinggan.qinglink.api.hu;

import com.qinggan.qinglink.api.hu.IMicphoneModeListener;

interface IMicphoneMode {
    boolean registerListener(IMicphoneModeListener listener);
    void unregisterListener(IMicphoneModeListener listener);
    boolean sendCurrentMicModeResponse(int mode);
    boolean sendCurrentAudioSourceAngleResponse(int angle);
}

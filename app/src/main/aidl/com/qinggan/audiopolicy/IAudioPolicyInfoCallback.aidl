package com.qinggan.audiopolicy;

import com.qinggan.audiopolicy.AudioPolicyInfo;

oneway interface IAudioPolicyInfoCallback {
	void onAudioPolicyInfoChange(in AudioPolicyInfo topInfo);

	void onStreamVolumeChange(int streamType, int volume, int flags);

    void onMuteStateChange(boolean mute, int flags);

    void onStreamMuteStateChange(int streamType, boolean mute);
}
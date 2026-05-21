package com.qinggan.audiopolicy;

import com.qinggan.audiopolicy.AudioClient;

oneway interface IAudioClientCallback {
	void onAudioClientChange(in AudioClient client);
}

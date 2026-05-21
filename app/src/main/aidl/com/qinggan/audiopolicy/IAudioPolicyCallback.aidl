package com.qinggan.audiopolicy;

interface IAudioPolicyCallback {
	boolean onAudioPolicyMessage(in int msgId, String clientId, int param);
}
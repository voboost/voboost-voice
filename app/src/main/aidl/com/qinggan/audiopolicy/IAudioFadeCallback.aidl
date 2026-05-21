package com.qinggan.audiopolicy;

oneway interface IAudioFadeCallback {
	void onComplete(int streamType, int type);
}
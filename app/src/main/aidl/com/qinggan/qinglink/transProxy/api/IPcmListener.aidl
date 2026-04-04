package com.qinggan.qinglink.transProxy.api;

interface IPcmListener {
	void onDisconnected();
	void onConnected();
	void onPcm(in byte[] pcm);
}

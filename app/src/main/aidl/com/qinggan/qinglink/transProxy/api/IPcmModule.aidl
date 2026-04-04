package com.qinggan.qinglink.transProxy.api;

import com.qinggan.qinglink.transProxy.api.IPcmListener;

interface IPcmModule{
	void registerPcmListener(IPcmListener listener);
	boolean sendPcm(in byte[] buff, int offset, int length);
	void unregisterPcmListener(IPcmListener listener);
}

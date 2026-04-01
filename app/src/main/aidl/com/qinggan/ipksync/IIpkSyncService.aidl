package com.qinggan.ipksync;
import com.qinggan.ipksync.IIpkSyncCallbacks;
import com.qinggan.ipksync.IIpkUpdataCallback;

interface IIpkSyncService {
	
	void syncEhuStaus2IcmAudioReady(int audioReady);
	void syncEhuStaus2IcmNaviReady(int naviReady);
	void syncEhuStaus2IcmStartNaviEvent();
	void syncEhuStaus2IcmNaviEvents();
	void syncEhuStaus2IcmMediaReady(int mediaReady);
	void syncEhuStaus2IcmMediaTypeReady(int mediaReady, int mediaType);
	void syncEhuStaus2IcmMediaEvent();
	void syncEhuStaus2IcmPushNaviVideoEvent();
	void syncEhuStaus2IcmSyncingVideo(int syncing);
	void syncEhuStaus2IcmSyncingVideoType(int syncing, int type);
	void syncEhuStaus2IcmSyncingVideoTypeArea(int syncing, int type, int area);
	
	void syncEhuStaus2IcmPhoneStatus(int phoneState);
	void syncEhuStaus2IcmBTStatus(int btState);
	void syncEhuStaus2IcmBTContactsRecordStatus(int recordState);
	
	void syncEhuStaus2IcmUpdatePkgTransferStatus(int transferState);
	
	void syncEhuSetTheme(int themeId);
	void syncEhuSetIPKMusic(int state);
	void syncEhuSetTimeFormat(int format);
	
	void syncEhuPhone2IcmSyncName(String name, boolean isThirdCall);
	void syncEhuPhone2IcmSyncCallTime(int time);
	void syncEhuPhone2IcmSyncCallTimed(int time, boolean isThirdCall);
	
	void syncEhu2IcmACK(int ack, int forWhat);
	
	void attachIpkSyncCallbacks(IIpkSyncCallbacks callbacks);
	
	void syncNaviStatus(int naviStatus);
	
	void customThreeAreas(int areaA, int areaB, int areaC);
	void mediaRadioFocusChange(int currentFocus, int subType);
	void syncEhuNaviDirection(int direction);
	void syncICMShowModeType(int mode, int style);
	
	void syncEhuStaus2IcmPushNaviByDirection(int direction, int triggerSource);
	void syncEhuReqMediaRadioChange(int toFocus);
	
	int getAreaIndex(int area);
	int getWindowState();

	void startUpload();
	void syncUploadFile(int iscomplete,int index,int total,int lastsize);
	void registerIpkUpdateCallback(IIpkUpdataCallback callbacks);

	void startLogReceive();
	void setLogState(int state);

	void syncEhuMUState(int state,int viewtype);
	void syncEhuFirstIsOK(int state,int viewType);
	int getMUStateformICM();
	void startUploadtype(int type);
	void startFileTransfer(int type);
    void sendLanguageState(String language, int type);
    void switchToRecoveryMode();
    void restoreFactory();
    void factoryUpgrade();
    void syncMenuCardInfo(in Map cardInfo);
    void syncEhuStatus2IcmPushRestByNavi(int eventType);
    void syncEhuStatus2IcmPushCampByNavi(int eventType);
    void syncICMVehicleState(int state);
}

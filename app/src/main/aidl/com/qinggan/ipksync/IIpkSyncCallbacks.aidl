package com.qinggan.ipksync;

interface IIpkSyncCallbacks {
	/** ICM ask **/
	void onIcmAck2EhuStatus(int ack);
	void onIcmAck2EhuPhone(int ack, int frameIndex);
	void onIcmAck2EhuUpdate(int ready);
	/** ICM ask **/

	/** ICM request **/
	void onIcmReq2EhuNaviShow(int windowState);
	void onIcmReq2EhuNaviStopSyncType(int windowState);
	void onIcmReq2EhuNaviStopSync();

	void onIcmReq2EhuMedia();
	void onIcmReq2EhuMediaStopSync();

	void onIcmReq2EhuRadio();
	void onIcmReq2EhuContacts();
	void onIcmReq2EhuTbtInfo(int status);

	void onIcmReq2EhuPhone();
	void onIcmReq2EhuPhoneStopSync();

	void onIcmReq2EhuWarning(int toneId, int toneType);
	void onIcmReq2EhuWarningStopSync();

	void onIcmReq2EhuIpkPageChanged(int ipkPageState);

	void onIcmReq2EhuShowByArea(int area);
	void onIcmReq2EhuPauseByArea(int area);
	/** ICM request **/

	/** ICM status **/
	void onIcmStatus2EhuIpkState(int ipkState);
	void onIcmStatus2EhuReceivePkg(int receivePkgState);
	/** ICM status **/

	/** ICM update setting **/
	void onIcmUpdateSetTheme(int themeId);
	void onIcmUpdateSetIkpMusic(int state);
	void onIcmUpdateSetTimeFormat(int format);
	/** ICM update setting **/

	/** ICM control EHU **/
	void onIcmControlMedia(int action);
	void onIcmControlRadio(int action);

	void onIcmControlMediaType(int type);
	void onIcmControlRadioType(int type);

	void onIcmControlPhoneAction(int action);
	void onIcmControlContactsAction(int action);

	void onIcmReq2EhuFavorate(int action);
	void onIcmReq2EhuMuStatue(int status);
	void onIcmReq2EhuKeyCode(int status);

	void onIcmReq2EhuRest();
	void onIcmReq2EhuRestStopSync();

	void onIcmReq2EhuCamp();
	void onIcmReq2EhuCampStopSync();
	void onIcmReq2EhuLanguageState(int businessType, int state);
}

package com.qinggan.audiopolicy;

import android.os.Bundle;

import com.qinggan.audiopolicy.IAudioPolicyCallback;
import com.qinggan.audiopolicy.IAudioPolicyInfoCallback;
import com.qinggan.audiopolicy.IAudioClientCallback;
import com.qinggan.audiopolicy.IBTSourceInfoCallback;
import com.qinggan.audiopolicy.IBTSourceConnected;
import com.qinggan.audiopolicy.IAudioFadeCallback;
import com.qinggan.audiopolicy.AudioPolicyInfo;
import com.qinggan.audiopolicy.AudioClient;
import com.qinggan.audiopolicy.AudioEQMode;
import com.qinggan.audiopolicy.IAudioExceptionCallback;

interface IAudioPolicyService {
    int requestAudioPolicy(int mainStreamType, int originStreamType,
                           IBinder cb, IAudioPolicyCallback callback, String clientId,
                           String callingPackageName, int flags);

    int abandonAudioPolicy(IAudioPolicyCallback callback, String clientId,
                           boolean resume);
    void unregisterAudioPolicyClient(String clientId);

    boolean registerAudioPolicyInfoCallback(IAudioPolicyInfoCallback callback,
                                            String callingPackageName);
    boolean unregisterAudioPolicyInfoCallback(IAudioPolicyInfoCallback callback);

    AudioPolicyInfo getCurrentAudioPolicyInfo(int type, String callingPackage);
    AudioPolicyInfo getA2dpAudioPolicyInfo(String callingPackage);
    List<AudioPolicyInfo> getAudioPolicyInfo(int streamType);

    boolean isInIVoka();
    boolean isInCall(String callingPackage);

    List<AudioClient> getAudioClients();
    boolean registerAudioClientCallback(IAudioClientCallback cb, String calling);
    boolean unregisterAudioClientCallback(IAudioClientCallback cb);

    AudioPolicyInfo getCurrAudioPolicyInfoByChannel(int channel);
    AudioPolicyInfo getCurrMediaAudioPolicyInfoByChannel(int channel);
    AudioPolicyInfo getLastMediaAudioPolicyInfoByChannel(int channel);

    boolean registerBTSourceInfoCallback(IBTSourceInfoCallback callback, String calling);
    boolean unregisterBTSourceInfoCallback(IBTSourceInfoCallback callback);

    void setStreamMute(int streamType, boolean state);
    boolean isStreamMute(int streamType);
    void setMasterMute(boolean state, int flags);
    boolean isMasterMute();
    void setXCallState(boolean enter);

    void adjustStreamVolume(int streamType, int direction, int step, int flags);
    void adjustVolume(int direction, int flags);

    int getStreamVolume(int streamType, String packageName);
    int getStreamMaxVolume(int streamType);
    void setStreamVolume(int streamType, int index, int flags, String packageName);

    int setSpeedCoefVolume(int volume);
    int getSpeedCoefMode();
    int setSpeedCoefMode(int mode);

    AudioEQMode getEQMode();
    AudioEQMode getEQModeByIndex(int mode);
    void setEQModeByIndex(int mode, in int[] values);
    int getEQModeIndex();
    int setEQMode(int mode);

    int setBandValue(int band, int value);
    int getMaxBandIndex();
    int getMaxBandValue();
    int getBandValue(int band);
    int setBalanceFaderLevel(int balanceLevel, int faderLevel);
    int getMaxBalanceFaderLevel();
    int[] getBalanceFaderLevel();

    int setLoudness(int state);
    int getLoudnessState();

    int setCanDspEffect(int bass, int mid, int treble);
    int setCanDspLoudness(boolean state);
    int getCanDspType();

    int getReversePolicy();
    int setReversePolicy(int policy);

    int getMixPolicy();
    int setMixPolicy(int policy);

    int setWakeUpMode(int mode);

    int volumeFadeIn(int streamType, IAudioFadeCallback callback);
    int volumeFadeOut(int streamType, IAudioFadeCallback callback);
    int setMicMute(boolean state);

    int requestAudioPolicyEx(int mainStreamType, int originStreamType,
                             IBinder cb, IAudioPolicyCallback callback, String clientId,
                             String callingPackageName, int flags, int extraType);

    int getPhoneMixPolicy();
    int setPhoneMixPolicy(int policy);

    int setDtsEnabled(boolean enable);
    boolean isDtsEnabled();

    int setDtsMode(int mode);
    int getDtsMode();

    int setChimeMode(int mode);
    int getChimeMode();

    int getHintToneState();
    int setHintToneState(int state);

    int getScreenSoundState();
    int setScreenSoundState(int state);

    int setChannel(int channel);
    int getChannel();

    int setBabyInCar(boolean babyInCar);
    boolean isBabyInCar();

    int setArkarmysMode(int mode);
    int getArkarmysMode();

    boolean hasAmpVehicel();
    int setAtmos(boolean enable);
    boolean isAtmos();
    int getAtmosMode();
    int setAtmosMode(int mode);

    int setSubwooferMode(int mode);
    int getSubwooferMode();
    boolean isMicMute();
    void playTone();
    void playSound(int id);

    int setSoundFeature(int mode);
    int getSoundFeature();
    int setSoundField(int field);
    int getSoundField();
    int setHighFreq(int h_freq);
    int getHighFreq();
    int setMidFreq(int m_freq);
    int getMidFreq();
    int setLowFreq(int l_freq);
    int getLowFreq();
    int setBassFreq(int l_freq);
    int getBassFreq();
    int[] getEQDefault();

    int getBTSourceVolume();
    int setBTSourceVolume(int index);
    int getVehicleState();
    void setVoiceMute(boolean mute);
    oneway void setEcnr(boolean enable);

    void setNaviMix(boolean mix);
    boolean isNaviMix();

    void setMediaPlaying(boolean play, in Bundle extras);
    boolean isMediaPlaying();

    void registerSoundChannelForPkg(String pkg, int soundChannel);
    void unregisterSoundChannelForPkg(String pkg);

    boolean isBTSourceConnected();
    boolean registerBTSourceConnected(IBTSourceConnected callback, String calling);
    boolean unregisterBTSourceConnected(IBTSourceConnected callback);

    boolean registerAudioExceptionCallback(IAudioExceptionCallback callback);
    boolean unregisterAudioExceptionCallback(IAudioExceptionCallback callback);

    int requestAudioFocus(IAudioPolicyCallback callback, String clientId,
                          int usage, int cont, String pack, int focusGain, int flags);

    int abandonAudioFocus(IAudioPolicyCallback callback, String clientId,
                          String pack, boolean resume);
}


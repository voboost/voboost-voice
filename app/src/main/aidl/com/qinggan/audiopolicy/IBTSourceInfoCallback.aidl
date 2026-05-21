package com.qinggan.audiopolicy;

import com.qinggan.audiopolicy.AudioPolicyInfo;

oneway interface IBTSourceInfoCallback {
    void onBTSourceInfoChange(in AudioPolicyInfo info);
}

/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.soter.core.model;

import android.os.Process;

/**
 * Created by henryye on 15/9/28.
 *
 */
public interface ConstantsSoter {
    String SOTER_PROVIDER_NAME = "SoterKeyStore";
    // You must add this prefix in every key name to compat some bad implements.
    String SOTER_COMMON_KEYNAME_PREFIX = "Wechat";
    String COMMON_SOTER_APP_SECURE_KEY_NAME = SOTER_COMMON_KEYNAME_PREFIX + Process.myUid();
    String SOTER_FINGERPRINT_ERR_FAIL_MAX_MSG = "Too many failed times";
    String SOTER_BIOMETRIC_ERR_FAIL_MAX_MSG = "Too many failed times";
    String SOTER_FACEID_ERR_FAIL_MAX_MSG = "Too many failed times";
    /**
     * The authentication is frozen due to too many failures
     */
    //fingerprint anti brute force. We use 10308 to distinguish some bad implementation which will return 7 in non-predicted situations
    int ERR_FINGERPRINT_FAIL_MAX = 10308; //FingerprintManager.FINGERPRINT_ERROR_LOCKOUT = 7;
    int ERR_BIOMETRIC_FAIL_MAX = 10308; //FingerprintManager.FINGERPRINT_ERROR_LOCKOUT = 7;
    int ERR_BIOMETRIC_FAIL_MAX_PERMANENT = 10309; //FingerprintManager.FINGERPRINT_ERROR_LOCKOUT_PERMANENT = 9;
    int ERR_BIOMETRIC_WAIT_TIMEOUT = 10309;
    int ERR_NEGATIVE_BUTTON = 10310;

    /**biometric auth type, ths first bit means fingerprint, the second bit for faceid, and go on*/
    int FINGERPRINT_AUTH = 0x1;
    int FACEID_AUTH = 0x2;
    long FACEID_AUTH_CHECK_TIME = 3000;
    
    /* report detail error */
    int ERR_SOTER_OK = 0;
    int ERR_SOTER_UNKNOWN = 100;
    int ERR_ANDROID_AIDL_EXCEPTION = 101; //调用android系统服务SoterService的接口时出现异常
    int ERR_ANDROID_AIDL_RESULT = 102; //调用android系统服务SoterService的接口时返回值为失败或空
    int ERR_ANDROID_BEFORE_TREBLE = 103; //采用BeforeTreble方案的手机，系统调用异常
    int ERR_ANDROID_HAREWARE_NOT_SUPPORT = 104; //Android手机系统方法返回不支持指纹/面容
    int ERR_ANDROID_BIND_SERVICE_OUTTIME = 105; //绑定手机系统的SoterService超时(3秒)
    int ERR_SOTER_INNER = 200; //Soter内部异常
    int ERR_SOTER_SRV_CONFIG = 300; //上报SrvDeviceInfo的服务端配置
    int ERR_SOTER_AUTH_ERROR = 401; //上报指纹/面容识别错误
}

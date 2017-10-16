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
    /**
     * The authentication is frozen due to too many failures
     */
    //fingerprint anti brute force. We use 10308 to distinguish some bad implementation which will return 7 in non-predicted situations
    int ERR_FINGERPRINT_FAIL_MAX = 10308; //FingerprintManager.FINGERPRINT_ERROR_LOCKOUT = 7;
}

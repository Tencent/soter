/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.soter.wrapper.wrap_core;

import android.content.Context;

import com.tencent.soter.core.model.SoterErrCode;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessCallback;
import com.tencent.soter.wrapper.wrap_task.InitializeParam;

/**
 * {@inheritDoc}
 * Created by henryye on 2017/4/7.
 * The error codes in SOTER wrap process. It includes error codes from SOTER core as well
 */

public interface SoterProcessErrCode extends SoterErrCode {
    /**
     * Not specify the purpose when generating keys
     */
    int ERR_UNEXPECTED_PURPOSE = 7;
    /**
     * Fails when getting SOTER support from server.
     */
    int ERR_GET_SUPPORT_SOTER_REMOTE_FAILED = 8;
    /**
     * Upload App Secure Key Failed
     */
    int ERR_UPLOAD_ASK_FAILED = 9;
    /**
     * Upload Auth Key Failed
     */
    int ERR_UPLOAD_AUTH_KEY_FAILED = 10;
    /**
     * The Auth Key is already expired.
     */
    int ERR_AUTHKEY_ALREADY_EXPIRED = 11;
    /**
     * Auth Key not found
     */
    int ERR_AUTHKEY_NOT_FOUND = 12;
    /**
     * Failed in preparing the Signature object.
     */
    int ERR_INIT_SIGN_FAILED = 13;
    /**
     * Not calling {@link com.tencent.soter.wrapper.SoterWrapperApi#init(Context, SoterProcessCallback, InitializeParam)} first before calling this method or not initialized yet.
     */
    int ERR_NOT_INIT_WRAPPER = 14;
    /**
     * The business scene code is not registered in {@link com.tencent.soter.wrapper.SoterWrapperApi#init(Context, SoterProcessCallback, InitializeParam)}
     */
    int ERR_AUTH_KEY_NOT_IN_MAP = 15;
    /**
     * Not provide the network wrapper
     */
    int ERR_NO_NET_WRAPPER = 16;
    /**
     * Context object not exist
     */
    int ERR_CONTEXT_INSTANCE_NOT_EXISTS = 17;
    /**
     * No fingerprint enrolled in the system
     */
    int ERR_NO_FINGERPRINT_ENROLLED = 18;
    /**
     * Failed in getting challenge from server
     */
    int ERR_GET_CHALLENGE = 19;
    /**
     * Failed in requesting fingerprint authentication service.
     */
    int ERR_START_AUTHEN_FAILED = 20;
    /**
     * Failed in Fingerprint authentication
     */
    int ERR_FINGERPRINT_AUTHENTICATION_FAILED = 21;
    /**
     * Failed in signing
     */
    int ERR_SIGN_FAILED = 22;
    /**
     * Failed in uploading signature to server
     */
    int ERR_UPLOAD_OR_VERIFY_SIGNATURE_FAILED = 23;
    /**
     * User cancelled the fingerprint authentication process.
     */
    int ERR_USER_CANCELLED = 24;
    /**
     * Already reached the maximum fail times in fingerprint authentication. you should try another
     * authentication method if you see this error code.
     */
    int ERR_FINGERPRINT_LOCKED = 25;

    /**
     * Add the SOTER task to queue failed. Check the logcat for further information
     */
    int ERR_ADD_TASK_FAILED = 26;

    /**
     * Indicate that you do not provide any business scenes in {@link com.tencent.soter.wrapper.SoterWrapperApi#init(Context, SoterProcessCallback, InitializeParam)}
     */
    int ERR_NO_BUSINESS_SCENE_PROVIDED = 27;

    /**
     * Indicated that you provide a long account salt string in {@link com.tencent.soter.wrapper.SoterWrapperApi#init(Context, SoterProcessCallback, InitializeParam)}
     */
    int ERR_ACCOUNT_SALT_LEN_TOO_LONG = 28;

    /**
     * The custom App Secure Key name is too long (larger than 24).
     */
    int ERR_CUSTOM_ASK_NAME_TOO_LONG = 29;

    /**
     * The java.security.SignatureException(perhaps caused by android.security.KeyStoreException:
     * Key user not authenticated,after OTA from N to O and enroll a new fingerprint).
     */
    int ERR_SIGNATURE_INVALID = 30;
}

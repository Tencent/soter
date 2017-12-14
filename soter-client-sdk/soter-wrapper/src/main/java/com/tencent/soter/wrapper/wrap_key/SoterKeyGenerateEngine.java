/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.soter.wrapper.wrap_key;

import com.tencent.soter.core.SoterCore;
import com.tencent.soter.core.model.SoterCoreResult;
import com.tencent.soter.core.model.SoterCoreUtil;
import com.tencent.soter.core.model.SLogger;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessKeyPreparationResult;
import com.tencent.soter.wrapper.wrap_core.SoterProcessErrCode;
import com.tencent.soter.wrapper.wrap_task.SoterTaskThread;

/**
 * Created by henryye on 2017/4/6.
 *
 * The generator to wrap key generating process.
 */
@SuppressWarnings("unused")
public class SoterKeyGenerateEngine {
    private static final String TAG = "Soter.SoterKeyGenerateEngine";

    private static final int FLAG_GEN_ASK = 0x1;
    private static final int FLAG_GEN_AUTH_KEY = 0x2;

    private SoterKeyGenerateEngine(int genKeyFlag, String authKeyName, boolean shouldDeleteAndReGenAskIfExists,
                                   boolean shouldDeleteAndReGenAuthKeyIfExists, ISoterKeyGenerateCallback callback) {
        mGenKeyFlag = genKeyFlag;
        mAuthKeyName = authKeyName;
        mShouldDeleteAndReGenAskIfExists = shouldDeleteAndReGenAskIfExists;
        mShouldDeleteAndReGenAuthKeyIfExists = shouldDeleteAndReGenAuthKeyIfExists;
        mCallback = callback;
    }

    private int mGenKeyFlag = 0x0;
    private String mAuthKeyName = "";
    private boolean mShouldDeleteAndReGenAskIfExists = false;

    private boolean mShouldDeleteAndReGenAuthKeyIfExists = false;
    private ISoterKeyGenerateCallback mCallback = null;

    private boolean mIsCallbacked = false;

    public static class SoterKeyGenerateEngineBuilder {

        private int mGenKeyFlag = 0x0;
        private String mAuthKeyName = "";
        private boolean mShouldDeleteAndReGenAskIfExists = false;

        private boolean mShouldDeleteAndReGenAuthKeyIfExists = false;
        private ISoterKeyGenerateCallback mCallback = null;

        public SoterKeyGenerateEngineBuilder markGenAppSecureKey(boolean shouldDeleteAndReGenAskIfExists) {
            mGenKeyFlag |= FLAG_GEN_ASK;
            mShouldDeleteAndReGenAskIfExists = shouldDeleteAndReGenAskIfExists;
            return this;
        }

        public SoterKeyGenerateEngineBuilder markGenAuthKey(String authKeyName, boolean shouldDeleteAndReGenAuthKeyIfExists) {
            this.mAuthKeyName = authKeyName;
            mShouldDeleteAndReGenAuthKeyIfExists = shouldDeleteAndReGenAuthKeyIfExists;
            this.mGenKeyFlag |= FLAG_GEN_AUTH_KEY;
            return this;
        }

        public SoterKeyGenerateEngineBuilder setKeyGenCallback(ISoterKeyGenerateCallback callback) {
            this.mCallback = callback;
            return this;
        }

        public SoterKeyGenerateEngine build() {
            return new SoterKeyGenerateEngine(mGenKeyFlag, mAuthKeyName,
                    mShouldDeleteAndReGenAskIfExists, mShouldDeleteAndReGenAuthKeyIfExists, mCallback);
        }

    }



    public void generate() {
        SoterTaskThread.getInstance().postToWorker(new Runnable() {
            @Override
            public void run() {
                SoterProcessKeyPreparationResult preCheckResult = checkParams();
                if(!preCheckResult.isSuccess()) {
                    callback(preCheckResult);
                    return;
                }
                if(!SoterCore.isNativeSupportSoter()) {
                    SLogger.w(TAG, "soter: native not support soter");
                    callback(new SoterProcessKeyPreparationResult(SoterProcessErrCode.ERR_SOTER_NOT_SUPPORTED));
                    return;
                }
                if((mGenKeyFlag & FLAG_GEN_ASK) == FLAG_GEN_ASK) {
                    SLogger.d(TAG, "soter: require generate ask. start gen");
                    if(mShouldDeleteAndReGenAskIfExists && SoterCore.hasAppGlobalSecureKey()) {
                        SLogger.d(TAG, "soter: request regen ask. remove former one");
                        SoterCoreResult removeAskResult = SoterCore.removeAppGlobalSecureKey();
                        if(!removeAskResult.isSuccess()) {
                            SLogger.w(TAG, "soter: remove ask failed: %s", removeAskResult.errMsg);
                            callback(removeAskResult);
                            return;
                        }
                    }
                    SoterCoreResult genAskResult = SoterCore.generateAppGlobalSecureKey();
                    if(!genAskResult.isSuccess()) {
                        SLogger.w(TAG, "soter: generate ask failed: %s", genAskResult.errMsg);
                        SoterCore.removeAppGlobalSecureKey(); // once failed, remove it
                        callback(genAskResult);
                        return;
                    } else {
                        SLogger.i(TAG, "soter: generate ask success!");
                        callback(genAskResult);
                    }
                }
                if((mGenKeyFlag & FLAG_GEN_AUTH_KEY) == FLAG_GEN_AUTH_KEY) {
                    SLogger.d(TAG, "soter: require generate auth key. start gen: %s", mAuthKeyName);
                    if(!SoterCore.isAppGlobalSecureKeyValid()) {
                        SLogger.w(TAG, "soter: no ask.");
                        callback(new SoterProcessKeyPreparationResult(SoterProcessErrCode.ERR_ASK_NOT_EXIST, "ASK not exists when generate auth key"));
                        return;
                    }
                    if(mShouldDeleteAndReGenAuthKeyIfExists && SoterCore.hasAuthKey(mAuthKeyName)) {
                        SLogger.d(TAG, "soter: request regen auth key. remove former one");
                        SoterCoreResult removeAutKeyResult = SoterCore.removeAuthKey(mAuthKeyName, false);
                        if(!removeAutKeyResult.isSuccess()) {
                            SLogger.w(TAG, "soter: remove auth key %s, failed: %s", mAuthKeyName, removeAutKeyResult.errMsg);
                            callback(removeAutKeyResult);
                            return;
                        }
                    }
                    SoterCoreResult genAuthKeyResult = SoterCore.generateAuthKey(mAuthKeyName);
                    if(!genAuthKeyResult.isSuccess()) {
                        SLogger.w(TAG, "soter: generate auth key %s failed: %s", mAuthKeyName, genAuthKeyResult.errMsg);
                        SoterCore.removeAuthKey(mAuthKeyName, true); // once failed, remove it. Note: it will remove ask as well
                        callback(genAuthKeyResult);
                    } else {
                        SLogger.i(TAG, "soter: generate auth key success!");
                        callback(genAuthKeyResult);
                    }
                }
            }
        });
    }

    private SoterProcessKeyPreparationResult checkParams() {
        //noinspection StatementWithEmptyBody
        if((mGenKeyFlag & FLAG_GEN_ASK) == FLAG_GEN_ASK) {
            // nothing to check
        } else if((mGenKeyFlag & FLAG_GEN_AUTH_KEY) == FLAG_GEN_AUTH_KEY) {
            if(SoterCoreUtil.isNullOrNil(mAuthKeyName)) {
                SLogger.e(TAG, "soter: not pass auth key name");
                return new SoterProcessKeyPreparationResult(SoterProcessErrCode.ERR_PARAMERROR, "auth key name not specified");
            }
        } else {
            SLogger.e(TAG, "soter: not specified purpose");
            return new SoterProcessKeyPreparationResult(SoterProcessErrCode.ERR_UNEXPECTED_PURPOSE, "not specified purpose. did you for get to call markGenAppSecureKey or/and markGenAuthKey?");
        }
        return new SoterProcessKeyPreparationResult(SoterProcessErrCode.ERR_OK);
    }

    private void callback(SoterCoreResult result) {
        if(mCallback != null && !mIsCallbacked) {
            if(result != null) {
                if(result.isSuccess()) {
                    mCallback.onSuccess();
                } else {
                    mCallback.onError(result.errCode, result.errMsg);
                }
            } else {
                mCallback.onError(SoterProcessErrCode.ERR_UNKNOWN, "unknown");
            }
        }
        mIsCallbacked = true;
    }

}

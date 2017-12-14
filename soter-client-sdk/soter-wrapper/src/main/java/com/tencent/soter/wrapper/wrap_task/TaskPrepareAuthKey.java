/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.soter.wrapper.wrap_task;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import com.tencent.soter.core.SoterCore;
import com.tencent.soter.core.model.SLogger;
import com.tencent.soter.core.model.SoterCoreUtil;
import com.tencent.soter.core.model.SoterErrCode;
import com.tencent.soter.core.model.SoterPubKeyModel;
import com.tencent.soter.wrapper.SoterWrapperApi;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessKeyPreparationResult;
import com.tencent.soter.wrapper.wrap_core.ConstantsSoterProcess;
import com.tencent.soter.wrapper.wrap_core.SoterDataCenter;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessCallback;
import com.tencent.soter.wrapper.wrap_core.SoterProcessErrCode;
import com.tencent.soter.wrapper.wrap_key.ISoterKeyGenerateCallback;
import com.tencent.soter.wrapper.wrap_key.SoterKeyGenerateEngine;
import com.tencent.soter.wrapper.wrap_net.ISoterNetCallback;
import com.tencent.soter.wrapper.wrap_net.IWrapUploadKeyNet;

/**
 * Created by henryye on 2017/4/21.
 * The task to generate ASK
 */

public class TaskPrepareAuthKey extends BaseSoterPrepareKeyTask implements SoterProcessErrCode {
    private static final String TAG = "Soter.TaskPrepareAuthKey";

    private String mAuthKeyName = null;
    private int mScene = -1;
    private IWrapUploadKeyNet mAuthKeyNetWrapper = null;
    private IWrapUploadKeyNet mASKNetWrapper = null;
    private boolean mIsAutoDeleteWhenAlreadyGenerated = false;
    private boolean mIsAutoPrepareASKWhenNotFound = false;

    public TaskPrepareAuthKey(int scene, IWrapUploadKeyNet authKeyNetWrapper, IWrapUploadKeyNet askNetWrapper, boolean isAutoDeleteWhenAlreadyGenerated, boolean isAutoPrepareASKWhenNotFound) {
        this.mScene = scene;
        this.mAuthKeyNetWrapper = authKeyNetWrapper;
        this.mIsAutoDeleteWhenAlreadyGenerated = isAutoDeleteWhenAlreadyGenerated;
        this.mIsAutoPrepareASKWhenNotFound = isAutoPrepareASKWhenNotFound;
        this.mASKNetWrapper = askNetWrapper;
    }

    @SuppressLint("DefaultLocale")
    @Override
    boolean preExecute() {
        if(!SoterDataCenter.getInstance().isInit()) {
            SLogger.w(TAG, "soter: not initialized yet");
            callback(new SoterProcessKeyPreparationResult(ERR_NOT_INIT_WRAPPER));
            return true;
        }
        if(!SoterDataCenter.getInstance().isSupportSoter()) {
            SLogger.w(TAG, "soter: not support soter");
            callback(new SoterProcessKeyPreparationResult(ERR_SOTER_NOT_SUPPORTED));
            return true;
        }
        mAuthKeyName = SoterDataCenter.getInstance().getAuthKeyNames().get(mScene, "");
        if(SoterCoreUtil.isNullOrNil(mAuthKeyName)) {
            SLogger.w(TAG, "soter: request prepare auth key scene: %d, but key name is not registered. Please make sure you register the scene in init");
            callback(new SoterProcessKeyPreparationResult(ERR_AUTH_KEY_NOT_IN_MAP, String.format("auth scene %d not initialized in map", mScene)));
            return true;
        }
        boolean isASKValid = SoterCore.isAppGlobalSecureKeyValid();
        // if there's no ask but has auth key, you should delete the auth key as well
        if(!isASKValid && SoterCore.hasAuthKey(mAuthKeyName)) {
            SLogger.w(TAG, "soter: no ask but has auth key. delete the auth key as well");
            SoterCore.removeAuthKey(mAuthKeyName, false);
        }
        if(!isASKValid && !mIsAutoPrepareASKWhenNotFound) {
            SLogger.w(TAG, "soter: has not generate app secure key yet and not require to generate it");
            callback(new SoterProcessKeyPreparationResult(SoterErrCode.ERR_ASK_NOT_EXIST));
            return true;
        }
        if(SoterCore.hasAuthKey(mAuthKeyName) && !SoterCore.isAuthKeyValid(mAuthKeyName, true)) {
            SLogger.w(TAG, "soter: already has auth key but not valid. delete it already and re-generate");
            return false;
        }
        if(SoterCore.hasAuthKey(mAuthKeyName) && !mIsAutoDeleteWhenAlreadyGenerated) {
            SLogger.i(TAG, "soter: already has key. do not need generate again");
            callback(new SoterProcessKeyPreparationResult(ERR_OK, SoterCore.getAuthKeyModel(mAuthKeyName)));
            return true;
        }
        if(mAuthKeyNetWrapper == null) {
            SLogger.w(TAG, "soter: it is strongly recommended that you provide a net wrapper to check and upload AuthKey validation from server! Please make sure you upload it later");
        }
        return false;
    }

    @Override
    boolean isSingleInstance() {
        return true;
    }

    @Override
    void onRemovedFromTaskPoolActively() {
        SLogger.w(TAG, "soter: cancelled prepare authkey: %s", mAuthKeyName);
        // will remove auth key if cancelled
        SoterCore.removeAuthKey(mAuthKeyName, false);
    }

    @Override
    void execute() {
        if(!SoterCore.isAppGlobalSecureKeyValid() && mIsAutoPrepareASKWhenNotFound) {
            SLogger.d(TAG, "soter: ask not found, but required to generate it. start generate");
            SoterWrapperApi.prepareAppSecureKey(new SoterProcessCallback<SoterProcessKeyPreparationResult>() {

                @Override
                public void onResult(@NonNull SoterProcessKeyPreparationResult result) {
                    SLogger.d(TAG, "soter: prepare ask end: %s", result.toString());
                    if(result.errCode == SoterCore.ERR_OK) {
                        generateAuthKey();
                    } else {
                        callback(result);
                    }
                }
            }, false, mASKNetWrapper);
        } else {
            generateAuthKey();
        }
    }

    private void generateAuthKey() {
        markKeyStatus(mAuthKeyName, ConstantsSoterProcess.KeyStatus.KEY_STATUS_GENERATING);
        final SoterKeyGenerateEngine.SoterKeyGenerateEngineBuilder builder = new SoterKeyGenerateEngine.SoterKeyGenerateEngineBuilder()
                .markGenAuthKey(mAuthKeyName, mIsAutoDeleteWhenAlreadyGenerated).setKeyGenCallback(new ISoterKeyGenerateCallback() {
                    @Override
                    public void onError(int errorCode, String errMsg) {
                        SLogger.w(TAG, "soter: auth key %s generate failed. errcode: %d, errmsg: %s", mAuthKeyName, errorCode, errMsg);
                        markKeyStatus(mAuthKeyName, ConstantsSoterProcess.KeyStatus.KEY_STATUS_NORMAL);
                        callback(new SoterProcessKeyPreparationResult(errorCode, errMsg));
                    }

                    @Override
                    public void onSuccess() {
                        SLogger.i(TAG, "soter: auth key generate successfully. start upload");
                        if(mAuthKeyNetWrapper != null) {
                            markKeyStatus(mAuthKeyName, ConstantsSoterProcess.KeyStatus.KEY_STATUS_GENERATED_BUT_NOT_UPLOADED);
                        } else {
                            markKeyStatus(mAuthKeyName, ConstantsSoterProcess.KeyStatus.KEY_STATUS_NORMAL);
                        }
                        startUploadAuthKeyAfterGenerate();
                    }
                });
        builder.build().generate();
    }

    private void startUploadAuthKeyAfterGenerate() {
        final SoterPubKeyModel authKeyModel = SoterCore.getAuthKeyModel(mAuthKeyName);
        if(authKeyModel == null) {
            SLogger.e(TAG, "soter: auth key model is null even after generation. fatal error");
            SoterCore.removeAuthKey(mAuthKeyName, false);
            callback(new SoterProcessKeyPreparationResult(ERR_AUTHKEY_NOT_FOUND, "auth key model is null even after generation."));
            return;
        }
        if(mAuthKeyNetWrapper != null) {
            mAuthKeyNetWrapper.setRequest(new IWrapUploadKeyNet.UploadRequest(authKeyModel.getSignature(),
                    authKeyModel.getRawJson()));
            mAuthKeyNetWrapper.setCallback(new ISoterNetCallback<IWrapUploadKeyNet.UploadResult>() {
                @Override
                public void onNetEnd(IWrapUploadKeyNet.UploadResult callbackDataModel) {
                    markKeyStatus(mAuthKeyName, ConstantsSoterProcess.KeyStatus.KEY_STATUS_NORMAL);
                    boolean isUploadAndVerified = callbackDataModel.mIsUploadAndVerifiedSuccess;
                    SLogger.i(TAG, "soter: auth key upload result: %b", isUploadAndVerified);
                    if(isUploadAndVerified) {
                        callback(new SoterProcessKeyPreparationResult(ERR_OK, authKeyModel));
                    } else {
                        // alto remove auth key when upload failed
                        SoterCore.removeAuthKey(mAuthKeyName, false);
                        callback(new SoterProcessKeyPreparationResult(SoterProcessErrCode.ERR_UPLOAD_AUTH_KEY_FAILED,
                                String.format("upload auth key: %s failed", mAuthKeyName)));
                    }
                }
            });
            mAuthKeyNetWrapper.execute();
        } else {
            SLogger.d(TAG, "soter: not provide network wrapper instance. please check if it is what you want. we treat it as normal");
            callback(new SoterProcessKeyPreparationResult(ERR_OK, "treat as normal because you do not provide the net wrapper", authKeyModel));
        }
    }
}

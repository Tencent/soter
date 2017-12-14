/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.soter.wrapper.wrap_task;

import com.tencent.soter.core.SoterCore;
import com.tencent.soter.core.model.SLogger;
import com.tencent.soter.core.model.SoterCoreData;
import com.tencent.soter.core.model.SoterPubKeyModel;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessKeyPreparationResult;
import com.tencent.soter.wrapper.wrap_core.ConstantsSoterProcess;
import com.tencent.soter.wrapper.wrap_core.SoterDataCenter;
import com.tencent.soter.wrapper.wrap_core.SoterProcessErrCode;
import com.tencent.soter.wrapper.wrap_key.ISoterKeyGenerateCallback;
import com.tencent.soter.wrapper.wrap_key.SoterKeyGenerateEngine;
import com.tencent.soter.wrapper.wrap_net.ISoterNetCallback;
import com.tencent.soter.wrapper.wrap_net.IWrapUploadKeyNet;

/**
 * Created by henryye on 2017/4/21.
 * The task to generate ASK
 */

public class TaskPrepareAppSecureKey extends BaseSoterPrepareKeyTask implements SoterProcessErrCode {
    private static final String TAG = "Soter.TaskPrepareAppSecureKey";

    private IWrapUploadKeyNet mAppSecureKeyNetWrapper = null;
    private boolean mIsAutoDeleteWhenAlreadyGenerated = false;

    public TaskPrepareAppSecureKey(IWrapUploadKeyNet appSecureKeyNetWrapper, boolean isAutoDeleteWhenAlreadyGenerated) {
        this.mAppSecureKeyNetWrapper = appSecureKeyNetWrapper;
        this.mIsAutoDeleteWhenAlreadyGenerated = isAutoDeleteWhenAlreadyGenerated;
    }

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
        if(SoterCore.isAppGlobalSecureKeyValid() && !mIsAutoDeleteWhenAlreadyGenerated) {
            SLogger.i(TAG, "soter: already has ask. do not need generate again");
            callback(new SoterProcessKeyPreparationResult(ERR_OK, SoterCore.getAppGlobalSecureKeyModel()));
            return true;
        }
        if(mAppSecureKeyNetWrapper == null) {
            SLogger.w(TAG, "soter: it is strongly recommended that you provide a net wrapper to check and upload ASK validation from server! Please make sure you upload it later");
        }
        return false;
    }

    @Override
    boolean isSingleInstance() {
        return true;
    }

    @Override
    void onRemovedFromTaskPoolActively() {
        SLogger.w(TAG, "soter: cancelled prepare ask");
        // will remove ask if cancelled
        SoterCore.removeAppGlobalSecureKey();
    }

    @Override
    void execute() {
        markKeyStatus(SoterCoreData.getInstance().getAskName(), ConstantsSoterProcess.KeyStatus.KEY_STATUS_GENERATING);
        final SoterKeyGenerateEngine.SoterKeyGenerateEngineBuilder builder = new SoterKeyGenerateEngine.SoterKeyGenerateEngineBuilder()
                .markGenAppSecureKey(mIsAutoDeleteWhenAlreadyGenerated).setKeyGenCallback(new ISoterKeyGenerateCallback() {
                    @Override
                    public void onError(int errorCode, String errMsg) {
                        SLogger.w(TAG, "soter: app secure key generate failed. errcode: %d, errmsg: %s", errorCode, errMsg);
                        markKeyStatus(SoterCoreData.getInstance().getAskName(), ConstantsSoterProcess.KeyStatus.KEY_STATUS_NORMAL);
                        callback(new SoterProcessKeyPreparationResult(errorCode, errMsg));
                    }

                    @Override
                    public void onSuccess() {
                        SLogger.i(TAG, "soter: app secure key generate successfully. start upload ask");
                        if(mAppSecureKeyNetWrapper != null) {
                            markKeyStatus(SoterCoreData.getInstance().getAskName(), ConstantsSoterProcess.KeyStatus.KEY_STATUS_GENERATED_BUT_NOT_UPLOADED);
                        } else {
                            markKeyStatus(SoterCoreData.getInstance().getAskName(), ConstantsSoterProcess.KeyStatus.KEY_STATUS_NORMAL);
                        }
                        startUploadASKAfterGenerate();
                    }
                });
        builder.build().generate();
    }

    private void startUploadASKAfterGenerate() {
        final SoterPubKeyModel askModel = SoterCore.getAppGlobalSecureKeyModel();
        if(askModel == null) {
            SLogger.e(TAG, "soter: ask model is null even after generation. fatal error");
            SoterCore.removeAppGlobalSecureKey();
            callback(new SoterProcessKeyPreparationResult(ERR_ASK_NOT_EXIST, "ask model is null even after generation."));
            return;
        }
        if(mAppSecureKeyNetWrapper != null) {
            mAppSecureKeyNetWrapper.setRequest(new IWrapUploadKeyNet.UploadRequest(askModel.getSignature(), askModel.getRawJson()));
            mAppSecureKeyNetWrapper.setCallback(new ISoterNetCallback<IWrapUploadKeyNet.UploadResult>() {
                @Override
                public void onNetEnd(IWrapUploadKeyNet.UploadResult callbackDataModel) {
                    markKeyStatus(SoterCoreData.getInstance().getAskName(), ConstantsSoterProcess.KeyStatus.KEY_STATUS_NORMAL);
                    boolean isUploadAndVerified = callbackDataModel.mIsUploadAndVerifiedSuccess;
                    SLogger.i(TAG, "soter: ask upload result: %b", isUploadAndVerified);
                    if(isUploadAndVerified) {
                        callback(new SoterProcessKeyPreparationResult(ERR_OK, askModel));
                    } else {
                        // alto remove ask when upload failed
                        SoterCore.removeAppGlobalSecureKey();
                        callback(new SoterProcessKeyPreparationResult(ERR_UPLOAD_ASK_FAILED, "upload app secure key failed"));
                    }
                }
            });
            mAppSecureKeyNetWrapper.execute();
        } else {
            SLogger.d(TAG, "soter: not provide network wrapper instance. please check if it is what you want. we treat it as normal");
            callback(new SoterProcessKeyPreparationResult(ERR_OK, "treat as normal because you do not provide the net wrapper", askModel));
        }
    }
}

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
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Process;
import android.support.annotation.NonNull;
import android.util.Log;

import com.tencent.soter.core.SoterCore;
import com.tencent.soter.core.model.ConstantsSoter;
import com.tencent.soter.core.model.ISoterLogger;
import com.tencent.soter.core.model.SLogger;
import com.tencent.soter.core.model.SoterCoreData;
import com.tencent.soter.core.model.SoterCoreUtil;
import com.tencent.soter.core.model.SoterDelegate;
import com.tencent.soter.core.model.SoterErrCode;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessNoExtResult;
import com.tencent.soter.wrapper.wrap_core.ConstantsSoterProcess;
import com.tencent.soter.wrapper.wrap_core.SoterDataCenter;
import com.tencent.soter.wrapper.wrap_core.SoterProcessErrCode;
import com.tencent.soter.wrapper.wrap_net.ISoterNetCallback;
import com.tencent.soter.wrapper.wrap_net.IWrapGetSupportNet;

import java.nio.charset.Charset;

/**
 * Created by henryye on 2017/4/21.
 * The task called in the very beginning of the application.
 */

public class TaskInit extends BaseSoterTask {
    private static final String TAG = "Soter.TaskInit";

    private static final String SOTER_STATUS_SHARED_PREFERENCE_NAME = "soter_status";
    // we add device information digest as the salt, as OEMs may OTA their ROM to avoid OOM.
    private static final String SOTER_TRIGGERED_OOM_FLAG_PREFERENCE_NAME = "soter_triggered_oom"
            + SoterCoreUtil.getMessageDigest(SoterCore.generateRemoteCheckRequestParam().getBytes(Charset.forName("UTF-8")));

    private static final int MAX_SALT_STR_LEN = 16;
    private static final int MAX_CUSTOM_KEY_LEN = 24;

    private boolean isNativeSupport = false;

    private IWrapGetSupportNet getSupportNetWrapper;
    private String distinguishSalt = "";
    private String customAskName = "";
    private int[] scenes;

    @SuppressWarnings("FieldCanBeLocal")
    private SoterDelegate.ISoterDelegate wrapperDelegate = new SoterDelegate.ISoterDelegate() {
        @SuppressLint("ApplySharedPref")
        @Override
        public void onTriggeredOOM() {
            SLogger.w(TAG, "soter: on trigger OOM, using wrapper implement");
            SharedPreferences preferences = SoterDataCenter.getInstance().getStatusSharedPreference();
            if(preferences != null) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(SOTER_TRIGGERED_OOM_FLAG_PREFERENCE_NAME, true);
                editor.commit();
            }
        }

        @Override
        public boolean isTriggeredOOM() {
            SharedPreferences preferences = SoterDataCenter.getInstance().getStatusSharedPreference();
            if(preferences != null) {
                boolean isTriggeredOOM = preferences.getBoolean(SOTER_TRIGGERED_OOM_FLAG_PREFERENCE_NAME, false);
                SLogger.i(TAG, "soter: is triggered OOM: %b", isTriggeredOOM);
                return isTriggeredOOM;
            } else {
                return false;
            }
        }
    };

    public TaskInit(Context context, @NonNull InitializeParam param){
        ISoterLogger loggerImp = param.getSoterLogger();
        // set logger first.
        if(loggerImp != null) {
            SLogger.setLogImp(loggerImp);
        }
        SoterDataCenter.getInstance().setStatusSharedPreference(context.getSharedPreferences(SOTER_STATUS_SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE));
        // set implement to wrapper
        SoterDelegate.setImplement(wrapperDelegate);

        SoterCore.setUp();
        isNativeSupport = SoterCore.isNativeSupportSoter() && SoterCore.isSupportFingerprint(context);
        this.getSupportNetWrapper = param.getGetSupportNetWrapper();
        this.scenes = param.getScenes();
        this.distinguishSalt = param.getDistinguishSalt();
        this.customAskName = param.getCustomAppSecureKeyName();
    }

    @Override
    boolean preExecute() {
        if(SoterCoreUtil.isNullOrNil(scenes)) {
            SLogger.e(TAG, "soter: the salt string used to distinguish is longer than 24");
            callback(new SoterProcessNoExtResult(SoterProcessErrCode.ERR_NO_BUSINESS_SCENE_PROVIDED, "no business scene provided"));
            return true;
        }
        if(SoterCoreUtil.nullAsNil(distinguishSalt).length() > MAX_SALT_STR_LEN) {
            SLogger.w(TAG, "soter: the salt string used to distinguish is longer than 24. soter will try to make it compat");
            String compatMd5 = getCompatDistinguishSalt(distinguishSalt);
            if(SoterCoreUtil.isNullOrNil(compatMd5)) {
                SLogger.w(TAG, "soter: saltlen compat failed!!");
                callback(new SoterProcessNoExtResult(SoterProcessErrCode.ERR_ACCOUNT_SALT_LEN_TOO_LONG, "the account salt length is too long"));
                return true;
            } else {
                distinguishSalt = compatMd5;
                // continue
            }
        }
        if(!SoterCoreUtil.isNullOrNil(customAskName) && customAskName.length() > MAX_CUSTOM_KEY_LEN) {
            SLogger.e(TAG, "soter: the passed ask name is too long (larger than 24).");
            callback(new SoterProcessNoExtResult(SoterProcessErrCode.ERR_CUSTOM_ASK_NAME_TOO_LONG, "the passed ask name is too long (larger than 24)"));
            return true;
        }
        if(getSupportNetWrapper == null) {
            SLogger.w(TAG, "soter: it is strongly recommended to check device support from server, so you'd better provider a net wrapper to check it");
        }
        if(!SoterCoreUtil.isNullOrNil(customAskName)) {
            SLogger.i(TAG, "soter: provided valid ASK name");
            SoterCoreData.getInstance().setAskName(customAskName);
        }
        SoterTaskThread.getInstance().postToWorker(new Runnable() {
            @Override
            public void run() {
                // generate auth key names.
                generateAuthKeyNames(distinguishSalt, scenes);
                removeAbnormalKeys();
            }
        });
        return false;
    }

    private String getCompatDistinguishSalt(@NonNull String previousSalt) {
        String saltMd5 = SoterCoreUtil.getMessageDigest(previousSalt.getBytes(Charset.forName("UTF-8")));
        if(!SoterCoreUtil.isNullOrNil(saltMd5) && saltMd5.length() >= MAX_SALT_STR_LEN) {
            return saltMd5.substring(0, MAX_SALT_STR_LEN);
        }
        Log.e(TAG, "soter: not valid md5 implement!!");
        return null;
    }


    // check if there's any keys invalid and need to be deleted
    private void removeAbnormalKeys() {
        SharedPreferences preferences = SoterDataCenter.getInstance().getStatusSharedPreference();
        int askStatus = preferences.getInt(SoterCoreData.getInstance().getAskName(), ConstantsSoterProcess.KeyStatus.KEY_STATUS_NORMAL);
        SLogger.d(TAG, "soter: ask status: %d", askStatus);
        if(isKeyStatusInvalid(askStatus) && SoterCore.hasAppGlobalSecureKey()) {
            SoterCore.removeAppGlobalSecureKey();
        }
        for (int scene : scenes) {
            String keyName = SoterDataCenter.getInstance().getAuthKeyNames().get(scene, "");
            if(!SoterCoreUtil.isNullOrNil(keyName)) {
                int keyStatus = preferences.getInt(keyName, ConstantsSoterProcess.KeyStatus.KEY_STATUS_NORMAL);
                SLogger.d(TAG, "soter: %s status: %d", keyName, keyStatus);
                if(isKeyStatusInvalid(keyStatus) && SoterCore.hasAuthKey(keyName)) {
                    SoterCore.removeAuthKey(keyName, false);
                }
            }
        }
    }

    // Only one init task instance can be added to
    @Override
    boolean isSingleInstance() {
        return true;
    }

    @Override
    void onRemovedFromTaskPoolActively() {
        // do nothing. cancel will not influence check support stuff.
    }

    @Override
    void execute() {
        if(isNativeSupport) {
            // if do not provide net wrapper, regard it as no need to send request to backend to check support
            if(getSupportNetWrapper == null) {
                SoterDataCenter.getInstance().setSupportSoter(true);
                SoterDataCenter.getInstance().setInit(true);
                callback(new SoterProcessNoExtResult(SoterErrCode.ERR_OK));
            } else {
                String deviceFpKey = SoterCore.generateRemoteCheckRequestParam();
                getSupportNetWrapper.setRequest(new IWrapGetSupportNet.GetSupportRequest(deviceFpKey));
                getSupportNetWrapper.setCallback(new ISoterNetCallback<IWrapGetSupportNet.GetSupportResult>() {
                    @Override
                    public void onNetEnd(IWrapGetSupportNet.GetSupportResult callbackDataModel) {
                        if(callbackDataModel != null) {
                            SLogger.i(TAG ,"soter: got support tag from backend: %b", callbackDataModel.isSupport);
                            synchronized (SoterDataCenter.class) {
                                SoterDataCenter.getInstance().setSupportSoter(callbackDataModel.isSupport);
                                SoterDataCenter.getInstance().setInit(true);
                            }
                            callback(new SoterProcessNoExtResult(ERR_OK));
                        } else {
                            SLogger.w(TAG, "soter: not return data from remote");
                            synchronized (SoterDataCenter.class) {
                                SoterDataCenter.getInstance().setSupportSoter(false);
                                SoterDataCenter.getInstance().setInit(true);
                            }
                            callback(new SoterProcessNoExtResult(ERR_GET_SUPPORT_SOTER_REMOTE_FAILED));
                        }
                    }
                });
                getSupportNetWrapper.execute();
            }
        } else {
            callback(new SoterProcessNoExtResult(SoterErrCode.ERR_SOTER_NOT_SUPPORTED));
            synchronized (SoterDataCenter.class) {
                SoterDataCenter.getInstance().setSupportSoter(false);
                SoterDataCenter.getInstance().setInit(true);
            }
        }
    }

    // Generate auth key names. Keep scene name unique every uid. Make it protected in case you want to generate key names by your self
    @SuppressWarnings("WeakerAccess")
    @SuppressLint("DefaultLocale")
    protected void generateAuthKeyNames(String distinguishSalt, int[] scenes) {
        for (int scene : scenes) {
            String authKeyName = String.format("%suid%d_%s_scene%d", ConstantsSoter.SOTER_COMMON_KEYNAME_PREFIX, Process.myUid(), SoterCoreUtil.nullAsNil(distinguishSalt), scene);
            SoterDataCenter.getInstance().getAuthKeyNames().put(scene, authKeyName);
        }
    }

    private boolean isKeyStatusInvalid(int keyStatus) {
        return keyStatus == ConstantsSoterProcess.KeyStatus.KEY_STATUS_GENERATED_BUT_NOT_UPLOADED
                || keyStatus == ConstantsSoterProcess.KeyStatus.KEY_STATUS_GENERATING;
    }
}

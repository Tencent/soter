/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.soter.wrapper;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.tencent.soter.core.SoterCore;
import com.tencent.soter.core.model.SLogger;
import com.tencent.soter.core.model.SoterCoreResult;
import com.tencent.soter.core.model.SoterCoreUtil;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessAuthenticationResult;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessCallback;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessKeyPreparationResult;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessNoExtResult;
import com.tencent.soter.wrapper.wrap_core.SoterDataCenter;
import com.tencent.soter.wrapper.wrap_core.SoterProcessErrCode;
import com.tencent.soter.wrapper.wrap_net.IWrapUploadKeyNet;
import com.tencent.soter.wrapper.wrap_task.AuthenticationParam;
import com.tencent.soter.wrapper.wrap_task.InitializeParam;
import com.tencent.soter.wrapper.wrap_task.SoterTaskManager;
import com.tencent.soter.wrapper.wrap_task.TaskAuthentication;
import com.tencent.soter.wrapper.wrap_task.TaskInit;
import com.tencent.soter.wrapper.wrap_task.TaskPrepareAppSecureKey;
import com.tencent.soter.wrapper.wrap_task.TaskPrepareAuthKey;

/**
 * Created by henryye on 2017/4/13.
 *
 * The wrapped public interfaces exported to developer
 * For applications not having special needs in SOTER, it will satisfy you if you only focus on these APIs.
 */

@SuppressWarnings({"unused", "WeakerAccess", "SameParameterValue"})
public class SoterWrapperApi implements SoterProcessErrCode {
    private static final String TAG = "Soter.SoterWrapperApi";

    /**
     * It is required to call it before any SOTER related event. This will do the prepare work, including asking whether the device supports SOTER from server. Call it from {@link Application#onCreate()} if
     * your application does not
     * @param context The context.
     * @param callback The callback of the process.
     * @param param The parameter if the initialization operation
     */
    public static void init(Context context, SoterProcessCallback<SoterProcessNoExtResult> callback, @NonNull InitializeParam param) {
        // prepare set up
        TaskInit taskInit = new TaskInit(context, param);
        taskInit.setTaskCallback(callback);
        if(!SoterTaskManager.getInstance().addToTask(taskInit, new SoterProcessNoExtResult())) {
            SLogger.e(TAG, "soter: add init task failed.");
        }
    }

    /**
     * Prepare the App Secure Key. You can call it as soon as you checked the device supports SOTER. If you want to generate ASK when you want to use the Auth Key for the first time, you can call {@link SoterWrapperApi#prepareAuthKey(SoterProcessCallback, boolean, boolean, int, IWrapUploadKeyNet, IWrapUploadKeyNet)}
     * and put your App Secure Key prepare network wrapper in the param.
     * @param callback The callback of the process
     * @param isAutoDeleteIfAlreadyExists If the App Secure Key should be deleted when there's already one. You should not set it to true in most cases
     * @param appSecureKeyNetWrapper The network wrapper of uploading App Secure Key.
     */
    public static void prepareAppSecureKey(SoterProcessCallback<SoterProcessKeyPreparationResult> callback, @SuppressWarnings("SameParameterValue") boolean isAutoDeleteIfAlreadyExists, IWrapUploadKeyNet appSecureKeyNetWrapper) {
        SLogger.i(TAG, "soter: starting prepare ask key. ");
        TaskPrepareAppSecureKey taskPrepareAppSecureKey = new TaskPrepareAppSecureKey(appSecureKeyNetWrapper, isAutoDeleteIfAlreadyExists);
        taskPrepareAppSecureKey.setTaskCallback(callback);
        if(!SoterTaskManager.getInstance().addToTask(taskPrepareAppSecureKey, new SoterProcessKeyPreparationResult())) {
            SLogger.d(TAG, "soter: add prepareAppSecureKey task failed.");
        }
    }

    /**
     * Prepare Auth Key of a specific business scene. Note that you can either prepared App Secure Key before, or you can set isAutoDeleteIfAlreadyExists to true and set your App Secure Key network wrapper to appSecureKeyNetWrapper, we
     * will help you to generate the App Secure Key.
     * @param callback The callback of the result
     * @param isAutoDeleteIfAlreadyExists If the Auth Key should be auto deleted if there's already one.
     * @param isAutoPrepareASKWhenNotFound If we should auto prepare the App Secure Key if it is not found in the device
     * @param scene Business scene. Should be initialized in {@link SoterWrapperApi#init(Context, SoterProcessCallback, InitializeParam)}
     * @param authKeyNetWrapper The network wrapper to upload Auth Key.
     * @param appSecureKeyNetWrapper The network wrapper to upload App Secure Key is isAutoPrepareASKWhenNotFound is true
     */
    public static void prepareAuthKey(SoterProcessCallback<SoterProcessKeyPreparationResult> callback,
                                      boolean isAutoDeleteIfAlreadyExists,
                                      boolean isAutoPrepareASKWhenNotFound,
                                      int scene, IWrapUploadKeyNet authKeyNetWrapper,
                                      IWrapUploadKeyNet appSecureKeyNetWrapper) {
        SLogger.i(TAG, "soter: starting prepare auth key: %d", scene);
        TaskPrepareAuthKey taskPrepareAuthKey = new TaskPrepareAuthKey(scene, authKeyNetWrapper, appSecureKeyNetWrapper, isAutoDeleteIfAlreadyExists, isAutoPrepareASKWhenNotFound);
        taskPrepareAuthKey.setTaskCallback(callback);
        if(!SoterTaskManager.getInstance().addToTask(taskPrepareAuthKey, new SoterProcessKeyPreparationResult())) {
            SLogger.d(TAG, "soter: add prepareAuthKey task failed.");
        }
    }

    /**
     * Wrap the whole authentication process, including request fingerprint authentication, generate the signature and upload the signature to the server.
     * @param param The parameter of the authentication process.
     */
    public static void requestAuthorizeAndSign(SoterProcessCallback<SoterProcessAuthenticationResult> callback, @NonNull AuthenticationParam param) {
        SLogger.i(TAG, "soter: request authorize provide challenge. scene: %d", param.getScene());
        TaskAuthentication taskAuthentication = new TaskAuthentication(param);
        taskAuthentication.setTaskCallback(callback);
        if(!SoterTaskManager.getInstance().addToTask(taskAuthentication, new SoterProcessAuthenticationResult())) {
            SLogger.d(TAG, "soter: add requestAuthorizeAndSign task failed.");
        }
    }

    /**
     * Check whether the device supports SOTER. The only difference between {@link SoterCore#isNativeSupportSoter()} and this method is that it also judge the result combining with server check result.
     * @return True if the device supports SOTER, false otherwise
     */
    public static boolean isSupportSoter() {
        return SoterDataCenter.getInstance().isInit() && SoterDataCenter.getInstance().isSupportSoter();
    }

    /**
     * Check whether SOTER is initialized.
     * @return true if already initialized
     */
    public static boolean isInitialized() {
        return SoterDataCenter.getInstance().isInit();
    }

    /**
     * Remove the Auth Key using business scene code.
     * @param scene Business scene. Should be initialized in {@link SoterWrapperApi#init(Context, SoterProcessCallback, InitializeParam)}
     * @return True if the remove process is successful, false otherwise
     */
    public static boolean removeAuthKeyByScene(int scene) {
        boolean isInit;
        String authKeyName;
        isInit = SoterDataCenter.getInstance().isInit();
        authKeyName = SoterDataCenter.getInstance().getAuthKeyNames().get(scene);
        if (isInit && !SoterCoreUtil.isNullOrNil(authKeyName)) {
            SoterCoreResult result = SoterCore.removeAuthKey(authKeyName, false);
            return result.errCode == ERR_OK;
        } else if (!isInit) {
            SLogger.w(TAG, "soter: not initialized yet");
            return false;
        } else {
            SLogger.w(TAG, "soter: scene not registered in init. please make sure");
            return false;
        }
    }

    /**
     *
     * Try to stop all SOTER processes which have not finished.
     * Note that the keys would be deleted if the key generating process is forced to stop.
     */
    public static void tryStopAllSoterTask() {
        SoterTaskManager.getInstance().cancelAllTask();
    }

    /**
     * Release SOTER initialize state. You should call it when you want to clear the initialize status manually, such as
     * switch the account. Note that it will call {@link SoterWrapperApi#tryStopAllSoterTask()}.
     */
    public static void release() {
        tryStopAllSoterTask();
        SoterDataCenter.getInstance().clearStatus();
    }

}

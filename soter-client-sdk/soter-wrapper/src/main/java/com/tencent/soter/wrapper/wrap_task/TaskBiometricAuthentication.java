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
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import com.tencent.soter.core.SoterCore;
import com.tencent.soter.core.biometric.BiometricManagerCompat;
import com.tencent.soter.core.model.ConstantsSoter;
import com.tencent.soter.core.model.SLogger;
import com.tencent.soter.core.model.SReporter;
import com.tencent.soter.core.model.SoterCoreUtil;
import com.tencent.soter.core.model.SoterSignatureResult;
import com.tencent.soter.soterserver.SoterSessionResult;
import com.tencent.soter.wrapper.SoterWrapperApi;
import com.tencent.soter.wrapper.wrap_biometric.SoterBiometricCanceller;
import com.tencent.soter.wrapper.wrap_biometric.SoterBiometricStateCallback;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessAuthenticationResult;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessResultBase;
import com.tencent.soter.wrapper.wrap_core.RemoveASKStrategy;
import com.tencent.soter.wrapper.wrap_core.SoterDataCenter;
import com.tencent.soter.wrapper.wrap_core.SoterProcessErrCode;
import com.tencent.soter.wrapper.wrap_net.ISoterNetCallback;
import com.tencent.soter.wrapper.wrap_net.IWrapGetChallengeStr;
import com.tencent.soter.wrapper.wrap_net.IWrapUploadSignature;

import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.security.Signature;
import java.security.SignatureException;

/**
 * Created by henryye on 2017/4/24.
 * Task to execute real authentication stuff
 */

public class TaskBiometricAuthentication extends BaseSoterTask implements AuthCancellationCallable {
    private static final String TAG = "Soter.TaskBiometricAuthentication";

    private int mScene = -1;
    private String mAuthKeyName = null;
    private String mChallenge = null;
    private IWrapGetChallengeStr mGetChallengeStrWrapper = null;
    private IWrapUploadSignature mUploadSignatureWrapper = null;
    // which type of biometric hardware used
    private int mBiometricType;
    // just weak reference the context in case it leaks. it's not necessary to strong reference it.
    private WeakReference<Context> mContextWeakReference = null;

    // below for biometric related
    private SoterBiometricCanceller mBiometricCancelSignal = null;
    private SoterBiometricStateCallback mBiometricStateCallback = null;

    // used for biometric prompt
    private String mPromptTitle;
    private String mPromptSubTitle;
    private String mPromptDescription;
    private String mPromptButton;
    private boolean mUseBiometricPrompt;

    private SoterSignatureResult mFinalResult = null;

    private AuthenticationCallbackImpl mAuthenticationCallbackIml = null;

    // below judge compat for low version
    private boolean mShouldOperateCompatWhenHint = Build.VERSION.SDK_INT < Build.VERSION_CODES.M && Build.MANUFACTURER.equalsIgnoreCase("vivo");
//    private boolean mShouldOperateCompatWhenHint = false;
    private boolean mShouldOperateCompatWhenDone = Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
    private boolean mIsAuthenticationAlreadyCancelled = false;

    public TaskBiometricAuthentication(AuthenticationParam param) {
        if(param == null) {
            throw new IllegalArgumentException("param is null!");
        }
        this.mScene = param.getScene();
        this.mGetChallengeStrWrapper = param.getIWrapGetChallengeStr();
        this.mUploadSignatureWrapper = param.getIWrapUploadSignature();
        this.mContextWeakReference = new WeakReference<>(param.getContext());
        this.mBiometricStateCallback = param.getSoterBiometricStateCallback();
        this.mBiometricCancelSignal = param.getSoterBiometricCanceller();
        this.mBiometricType = param.getBiometricType();
        this.mChallenge = param.getChallenge();
        this.mPromptTitle = param.getPromptTitle();
        this.mPromptSubTitle = param.getPromptSubTitle();
        this.mPromptDescription = param.getPromptDescription();
        this.mPromptButton = param.getPromptButton();
        this.mUseBiometricPrompt = param.getUseBiometricPrompt();
    }


    @SuppressLint({"DefaultLocale", "NewApi"})
    @Override
    boolean preExecute() {
        if (!SoterDataCenter.getInstance().isInit()) {
            SLogger.w(TAG, "soter: not initialized yet");
            callback(new SoterProcessAuthenticationResult(ERR_NOT_INIT_WRAPPER));
            return true;
        }
        if (!SoterDataCenter.getInstance().isSupportSoter()) {
            SLogger.w(TAG, "soter: not support soter");
            callback(new SoterProcessAuthenticationResult(ERR_SOTER_NOT_SUPPORTED));
            return true;
        }
        // All SOTER device must be at list Android 5.0, so be easy and free to add this Assert after checking SOTER support
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            callback(new SoterProcessAuthenticationResult(ERR_SOTER_NOT_SUPPORTED));
            return true;
        }
        mAuthKeyName = SoterDataCenter.getInstance().getAuthKeyNames().get(mScene, "");
        if (SoterCoreUtil.isNullOrNil(mAuthKeyName)) {
            SLogger.w(TAG, "soter: request prepare auth key scene: %d, but key name is not registered. Please make sure you register the scene in init");
            callback(new SoterProcessAuthenticationResult(ERR_AUTH_KEY_NOT_IN_MAP, String.format("auth scene %d not initialized in map", mScene)));
            return true;
        }
        /*
        if (!SoterCore.isAppGlobalSecureKeyValid()) {
            SLogger.w(TAG, "soter: app secure key not exists. need re-generate");
            callback(new SoterProcessAuthenticationResult(ERR_ASK_NOT_EXIST));
            return true;
        }
        */
        if (!(SoterCore.hasAuthKey(mAuthKeyName) /*&& SoterCore.getAuthKeyModel(mAuthKeyName) != null*/)) {
            SLogger.w(TAG, "soter: auth key %s not exists. need re-generate", mAuthKeyName);
            callback(new SoterProcessAuthenticationResult(ERR_AUTHKEY_NOT_FOUND, String.format("the auth key to scene %d not exists. it may because you haven't prepare it, or user removed them already in system settings. please prepare the key again", mScene)));
            return true;
        }
        /*
        if (!SoterCore.isAuthKeyValid(mAuthKeyName, true)) {
            SLogger.w(TAG, "soter: auth key %s has already expired, and we've already deleted them. need re-generate", mAuthKeyName);
            callback(new SoterProcessAuthenticationResult(ERR_AUTHKEY_ALREADY_EXPIRED, String.format("the auth key to scene %d has already been expired. in Android versions above 6.0, a key would be expired when user enrolls a new fingerprint. please prepare the key again", mScene)));
            return true;
        }
        */
        // in this process, 2 network wrappers must not be null!
        if (mGetChallengeStrWrapper == null && SoterCoreUtil.isNullOrNil(mChallenge)) {
            SLogger.w(TAG, "soter: challenge wrapper is null!");
            callback(new SoterProcessAuthenticationResult(ERR_NO_NET_WRAPPER, "neither get challenge wrapper nor challenge str is found in request parameter"));
            return true;
        }


        Context context = mContextWeakReference.get();
        if (context == null) {
            SLogger.w(TAG, "soter: context instance released in preExecute");
            callback(new SoterProcessAuthenticationResult(ERR_CONTEXT_INSTANCE_NOT_EXISTS));
            return true;
        }
        // check biometric status
        boolean hasBiometric = BiometricManagerCompat.from(context,mBiometricType).hasEnrolledBiometric();
        if (!hasBiometric) {
            SLogger.w(TAG, "soter: user has not enrolled any biometric in system.");
            callback(new SoterProcessAuthenticationResult(SoterProcessErrCode.ERR_NO_BIOMETRIC_ENROLLED));
            return true;
        }
        if(SoterCore.isCurrentBiometricFrozen(context,mBiometricType)) {
            SLogger.w(TAG, "soter: biometric sensor frozen");
            callback(new SoterProcessAuthenticationResult(SoterProcessErrCode.ERR_FINGERPRINT_LOCKED, ConstantsSoter.SOTER_FINGERPRINT_ERR_FAIL_MAX_MSG));
            return true;
        }
        if(mBiometricCancelSignal == null) {
            SLogger.w(TAG, "soter: did not pass cancellation obj. We suggest you pass one");
            mBiometricCancelSignal = new SoterBiometricCanceller();
            return false;
        }
        if(mUploadSignatureWrapper == null) {
            SLogger.w(TAG, "hy: we strongly recommend you to check the final authentication data in server! Please make sure you upload and check later");
        }
        return false;
    }

    @Override
    boolean isSingleInstance() {
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    void onRemovedFromTaskPoolActively() {
        if (mBiometricCancelSignal != null) {
            mBiometricCancelSignal.asyncCancelBiometricAuthentication();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    void execute() {
        // first get challenge data. If there's already challenge data, just skip it.
        if (SoterCoreUtil.isNullOrNil(mChallenge)) {
            SLogger.i(TAG, "soter: not provide the challenge. we will do the job");
            mGetChallengeStrWrapper.setRequest(new IWrapGetChallengeStr.GetChallengeRequest());
            mGetChallengeStrWrapper.setCallback(new ISoterNetCallback<IWrapGetChallengeStr.GetChallengeResult>() {
                @Override
                public void onNetEnd(IWrapGetChallengeStr.GetChallengeResult callbackDataModel) {
                    if (callbackDataModel != null && !SoterCoreUtil.isNullOrNil(callbackDataModel.challenge)) {
                        mChallenge = callbackDataModel.challenge;
                        startAuthenticate();
                    } else {
                        SLogger.w(TAG, "soter: get challenge failed");
                        callback(new SoterProcessAuthenticationResult(ERR_GET_CHALLENGE));
                    }
                }
            });
            mGetChallengeStrWrapper.execute();
        } else {
            SLogger.i(TAG, "soter: already provided the challenge. directly authenticate");
            startAuthenticate();
        }
    }

    @Override
    void onExecuteCallback(SoterProcessResultBase result) {
        if ((result.getErrCode() == ERR_SIGN_FAILED
                || result.getErrCode() == ERR_INIT_SIGN_FAILED
                || result.getErrCode() == ERR_START_AUTHEN_FAILED)
                && RemoveASKStrategy.shouldRemoveAllKey(this.getClass(), result)) {
            SLogger.i(TAG, "soter: same error happen too much, delete ask");
            SoterWrapperApi.clearAllKey();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void startAuthenticate() {
        if(SoterCore.getSoterCoreType() == SoterCore.IS_TREBLE){
            SoterSessionResult soterSessionResult = SoterCore.initSigh(mAuthKeyName, mChallenge);

            if(soterSessionResult == null ){
                SLogger.w(TAG, "soter: error occurred when init sign soterSessionResult is null");
                callback(new SoterProcessAuthenticationResult(ERR_INIT_SIGN_FAILED));
                return;
            }

            if(soterSessionResult.resultCode != 0) {
                SLogger.w(TAG, "soter: error occurred when init sign resultCode error");
                callback(new SoterProcessAuthenticationResult(ERR_INIT_SIGN_FAILED));
                return;
            }
            SLogger.d(TAG, "soter: session is %d",soterSessionResult.session);

            mAuthenticationCallbackIml = new AuthenticationCallbackImpl(null);
            mAuthenticationCallbackIml.session = soterSessionResult.session;
            performStartBiometricLogic(null);
            SoterTaskThread.getInstance().postToMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mBiometricStateCallback != null) {
                        mBiometricStateCallback.onStartAuthentication();
                    }
                }
            });
        }else {
            final Signature signatureToAuth = SoterCore.getAuthInitAndSign(mAuthKeyName);
            if (signatureToAuth == null) {
                SLogger.w(TAG, "soter: error occurred when init sign");
                callback(new SoterProcessAuthenticationResult(ERR_INIT_SIGN_FAILED));
                return;
            }

            mAuthenticationCallbackIml = new AuthenticationCallbackImpl(signatureToAuth);
            performStartBiometricLogic(signatureToAuth);
            SoterTaskThread.getInstance().postToMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mBiometricStateCallback != null) {
                        mBiometricStateCallback.onStartAuthentication();
                    }
                }
            });
        }
    }

    @SuppressLint("NewApi")
    private void performStartBiometricLogic(Signature signatureToAuth) {
        if(isFinished()) {
            SLogger.w(TAG, "soter: already finished. can not authenticate");
            return;
        }
        Context context = mContextWeakReference.get();
        if (context == null) {
            SLogger.w(TAG, "soter: context instance released in startAuthenticate");
            callback(new SoterProcessAuthenticationResult(ERR_CONTEXT_INSTANCE_NOT_EXISTS));
            return;
        }
        try {
            SLogger.v(TAG, "soter: performing start");
            Bundle extra = new Bundle();
            extra.putString("prompt_title", mPromptTitle);
            extra.putString("prompt_subtitle", mPromptSubTitle);
            extra.putString("prompt_description", mPromptDescription);
            extra.putString("prompt_button", mPromptButton);
            extra.putBoolean("use_biometric_prompt", mUseBiometricPrompt);
            BiometricManagerCompat.from(context, mBiometricType).authenticate(new BiometricManagerCompat.CryptoObject(signatureToAuth), 0,
                    mBiometricCancelSignal != null ? mBiometricCancelSignal.getSignalObj() : null,
                    mAuthenticationCallbackIml, null, extra);
        } catch (Exception e) {
            String cause = e.getMessage();
            SLogger.e(TAG, "soter: caused exception when authenticating: %s", cause);
            SLogger.printErrStackTrace(TAG, e, "soter: caused exception when authenticating");
            SReporter.reportError(ConstantsSoter.ERR_SOTER_INNER, "TaskBiometric, start authentication failed: performStartBiometricLogic().", e);
            callback(new SoterProcessAuthenticationResult(ERR_START_AUTHEN_FAILED, String.format("start authentication failed due to %s", cause)));
        }
    }

    private void executeWhenAuthenticatedWithSession(@NonNull Signature signature, long session) {
        try {

            byte[] rawResult = SoterCore.finishSign(session);
            mFinalResult = SoterCore.convertFromBytesToSignatureResult(rawResult);

            if(mUploadSignatureWrapper != null) {
                uploadSignature();
            } else {
                SLogger.i(TAG, "soter: no upload wrapper, return directly");
                callback(new SoterProcessAuthenticationResult(ERR_OK, mFinalResult));
            }

        } catch (Exception e) {
            SLogger.e(TAG, "soter: finish sign failed due to exception: %s", e.getMessage());
            SLogger.printErrStackTrace(TAG, e, "soter: sign failed due to exception");
            SReporter.reportError(ConstantsSoter.ERR_SOTER_INNER, "TaskBiometric, sign failed: executeWhenAuthenticatedWithSession().", e);
            callback(new SoterProcessAuthenticationResult(ERR_SIGN_FAILED, "sign failed even after user authenticated the key."));
        }
    }

    private void executeWhenAuthenticated(@NonNull Signature signature) {
        try {
            mFinalResult = SoterCore.convertFromBytesToSignatureResult(signature.sign());
            if(mUploadSignatureWrapper != null) {
                uploadSignature();
            } else {
                SLogger.i(TAG, "soter: no upload wrapper, return directly");
                callback(new SoterProcessAuthenticationResult(ERR_OK, mFinalResult));
            }

        } catch (SignatureException e) {
            SLogger.e(TAG, "soter: sign failed due to exception: %s", e.getMessage());
            SLogger.printErrStackTrace(TAG, e, "soter: sign failed due to exception");
            SReporter.reportError(ConstantsSoter.ERR_SOTER_INNER, "TaskBiometric, sign failed: executeWhenAuthenticated().", e);
            callback(new SoterProcessAuthenticationResult(ERR_SIGN_FAILED, "sign failed even after user authenticated the key."));
        }
    }

    private void uploadSignature() {
        if (mFinalResult == null) {
            callback(new SoterProcessAuthenticationResult(ERR_SIGN_FAILED, "sign failed even after user authenticated the key."));
            return;
        }
        mUploadSignatureWrapper.setRequest(new IWrapUploadSignature.UploadSignatureRequest(mFinalResult.getSignature(), mFinalResult.getJsonValue(), mFinalResult.getSaltLen()));
        mUploadSignatureWrapper.setCallback(new ISoterNetCallback<IWrapUploadSignature.UploadSignatureResult>() {
            @Override
            public void onNetEnd(IWrapUploadSignature.UploadSignatureResult callbackDataModel) {
                if (callbackDataModel != null && callbackDataModel.isVerified) {
                    SLogger.i(TAG, "soter: upload and verify succeed");
                    callback(new SoterProcessAuthenticationResult(ERR_OK, mFinalResult));
                } else {
                    SLogger.w(TAG, "soter: upload or verify failed");
                    callback(new SoterProcessAuthenticationResult(ERR_UPLOAD_OR_VERIFY_SIGNATURE_FAILED));
                }
            }
        });
        mUploadSignatureWrapper.execute();
    }

    @Override
    public void callCancellationInternal() {
        SLogger.i(TAG, "soter: called from cancellation signal");
        if(mAuthenticationCallbackIml != null) {
            mAuthenticationCallbackIml.onAuthenticationCancelled();
        }
    }

    @Override
    public boolean isCancelled() {
        return mIsAuthenticationAlreadyCancelled;
    }

    private class AuthenticationCallbackImpl extends BiometricManagerCompat.AuthenticationCallback {

        private Signature mSignatureToAuth = null;
        // The cancellation signal may delay some time even it callbacks, for it uses a remote service and we cannot estimate the accurate delay. This is the experimental value to do that trick
        private static final long MAGIC_CANCELLATION_WAIT = 1000;

        private long session ;

        private AuthenticationCallbackImpl(@NonNull Signature signature) {
            mSignatureToAuth = signature;
        }

        private String charSequenceToStringNullAsNil(CharSequence sequence) {
            return sequence == null ? "unknown error" : sequence.toString();
        }



        @Override
        public void onAuthenticationError(final int errMsgId, final CharSequence errString) {
            SLogger.e(TAG, "soter: on authentication fatal error: %d, %s", errMsgId, errString);
            SoterTaskThread.getInstance().postToMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mBiometricStateCallback != null) {
                        mBiometricStateCallback.onAuthenticationError(errMsgId, errString);
                    }
                }
            });
            // We treat too many fingerprint authentication failures as a special case. It's not a kind of fatal failure.
            // Application should handle this as a normal logic, such as change authentication method
            if(errMsgId == ConstantsSoter.ERR_BIOMETRIC_FAIL_MAX) {
                callback(new SoterProcessAuthenticationResult(SoterProcessErrCode.ERR_BIOMETRIC_LOCKED,  charSequenceToStringNullAsNil(errString)));
            } else if (errMsgId == ConstantsSoter.ERR_BIOMETRIC_FAIL_MAX_PERMANENT) {
                callback(new SoterProcessAuthenticationResult(SoterProcessErrCode.ERR_BIOMETRIC_LOCKED_PERMENANT, charSequenceToStringNullAsNil(errString)));
            } else if (errMsgId == ConstantsSoter.ERR_NEGATIVE_BUTTON) {
                callback(new SoterProcessAuthenticationResult(SoterProcessErrCode.ERR_CLICK_NEGATIVE_BUTTON, charSequenceToStringNullAsNil(errString)));
            } else {
                callback(new SoterProcessAuthenticationResult(SoterProcessErrCode.ERR_BIOMETRIC_AUTHENTICATION_FAILED, charSequenceToStringNullAsNil(errString)));
            }
            authenticationShouldComplete();
            SReporter.reportError(ConstantsSoter.ERR_SOTER_AUTH_ERROR, "on authentication fatal error: " + errMsgId + " " + errString);
        }

        @Override
        public void onAuthenticationHelp(final int helpMsgId, final CharSequence helpString) {
            SLogger.w(TAG, "soter: on authentication help. you do not need to cancel the authentication: %d, %s", helpMsgId, helpString);
            SoterTaskThread.getInstance().postToMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mBiometricStateCallback != null) {
                        mBiometricStateCallback.onAuthenticationHelp(helpMsgId, charSequenceToStringNullAsNil(helpString));
                    }
                }
            });
        }

        @Override
        public void onAuthenticationSucceeded(BiometricManagerCompat.AuthenticationResult result) {
            SLogger.i(TAG, "soter: authentication succeed. start sign and upload upload signature");
            SoterTaskThread.getInstance().postToMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mBiometricStateCallback != null) {
                        mBiometricStateCallback.onAuthenticationSucceed();
                    }
                }
            });
            SoterTaskThread.getInstance().postToWorker(new Runnable() {
                @Override
                public void run() {
                    if(!SoterCoreUtil.isNullOrNil(mChallenge)) {
                        if(SoterCore.getSoterCoreType() == SoterCore.IS_TREBLE){
                            executeWhenAuthenticatedWithSession(mSignatureToAuth, session);
                        }else {
                            try {
                                mSignatureToAuth.update(mChallenge.getBytes(Charset.forName("UTF-8")));
                            } catch (Exception e) {
                                SLogger.e(TAG, "soter: exception in update");
                                SLogger.printErrStackTrace(TAG, e, "soter: exception in update");
                                SReporter.reportError(ConstantsSoter.ERR_SOTER_INNER, "TaskBiometric, update signature failed: onAuthenticationSucceeded().", e);
                                //fix the bug that auth key will be invalid after enroll a new fingerprint after OTA to android O from android N.
                                SLogger.e(TAG, "soter: remove the auth key: %s", mAuthKeyName);
                                SoterCore.removeAuthKey(mAuthKeyName, false);
                                callback(new SoterProcessAuthenticationResult(SoterProcessErrCode.ERR_SIGNATURE_INVALID, "update signature failed. authkey removed after this failure, please check"));
                            }
                            try {
                                executeWhenAuthenticated(mSignatureToAuth);
                            } catch (Exception e) {
                                SLogger.e(TAG, "soter: exception in executeWhenAuthenticated method");
                                SLogger.printErrStackTrace(TAG, e, "soter: exception when execute");
                                onAuthenticationError(-1000, "execute failed");
                            }
                        }
                    } else {
                        SLogger.e(TAG, "soter: challenge is null. should not happen here");
                        onAuthenticationError(-1000, "challenge is null");
                    }
                }
            });

            authenticationShouldComplete();
        }

        @Override
        public void onAuthenticationFailed() {
            SLogger.w(TAG, "soter: authentication failed once");
            SoterTaskThread.getInstance().postToMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mBiometricStateCallback != null) {
                        mBiometricStateCallback.onAuthenticationFailed();
                    }
                }
            });
            authenticationMaybeContinue();
        }

        @Override
        public void onAuthenticationCancelled() {
            SLogger.i(TAG, "soter: called onAuthenticationCancelled");
            if(mIsAuthenticationAlreadyCancelled) {
                SLogger.v(TAG, "soter: during ignore cancel period");
                return;
            }
            SoterTaskThread.getInstance().postToMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mBiometricStateCallback != null) {
                        mBiometricStateCallback.onAuthenticationCancelled();
                    }
                }
            });
            callback(new SoterProcessAuthenticationResult(ERR_USER_CANCELLED, "user cancelled authentication"));
            authenticationShouldComplete();
        }

        @SuppressLint("NewApi")
        private void authenticationMaybeContinue() {
            // in versions below 6.0, you must cancel the authentication and start again when onAuthenticationFailed
            if(mShouldOperateCompatWhenHint) {
                SLogger.i(TAG, "soter: should compat lower android version logic.");
                mBiometricCancelSignal.asyncCancelBiometricAuthenticationInnerImp(false);
                SoterTaskThread.getInstance().postToWorker(new Runnable() {
                    @Override
                    public void run() {
                        mBiometricCancelSignal.refreshCancellationSignal();
                    }
                });
                SoterTaskThread.getInstance().postToWorkerDelayed(new Runnable() {
                    @Override
                    public void run() {
                        performStartBiometricLogic(mSignatureToAuth);
                    }
                }, MAGIC_CANCELLATION_WAIT);

            }

            // in faceid, you must cancel the authentication when onAuthenticationFailed
            if(mBiometricType == ConstantsSoter.FACEID_AUTH){
                SLogger.i(TAG, "soter: should compat faceid logic.");
                callback(new SoterProcessAuthenticationResult(SoterProcessErrCode.ERR_BIOMETRIC_AUTHENTICATION_FAILED,  "faceid not match"));
            }
        }

        @SuppressLint("NewApi")
        private void authenticationShouldComplete() {
            if(mShouldOperateCompatWhenDone || (mBiometricType == ConstantsSoter.FACEID_AUTH)) {
                mBiometricCancelSignal.asyncCancelBiometricAuthenticationInnerImp(false);
                mIsAuthenticationAlreadyCancelled = true;
            }
        }

    }
}

/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.soter.wrapper.wrap_task;

import android.content.Context;

import com.tencent.soter.wrapper.wrap_biometric.SoterBiometricCanceller;
import com.tencent.soter.wrapper.wrap_biometric.SoterBiometricStateCallback;
import com.tencent.soter.wrapper.wrap_fingerprint.SoterFingerprintCanceller;
import com.tencent.soter.wrapper.wrap_fingerprint.SoterFingerprintStateCallback;
import com.tencent.soter.wrapper.wrap_net.IWrapGetChallengeStr;
import com.tencent.soter.wrapper.wrap_net.IWrapUploadSignature;

/**
 * Created by henryye on 2017/5/27.
 * The authentication parameter to be passed to the constructor of {@link TaskAuthentication}.
 */

@SuppressWarnings("WeakerAccess")
public class AuthenticationParam {
    @SuppressWarnings("unused")
    private static final String TAG = "MicroMsg.AuthenticationParam";

    private int mScene;
    private String mChallenge;
    private IWrapGetChallengeStr mIWrapGetChallengeStr;
    private IWrapUploadSignature mIWrapUploadSignature;
    private Context mContext;
    private int mBiometricType;
    private String mPromptTitle; //used for BiometricPrompt
    private String mPromptSubTitle; //used for BiometricPrompt
    private String mPromptDescription; //used for BiometricPrompt
    private String mPromptButton; //used for BiometricPrompt
    private boolean mUseBiometricPrompt; // force use BiometricPrompt when api>=31
    private SoterFingerprintCanceller mFingerprintCanceller;
    private SoterFingerprintStateCallback mSoterFingerprintStateCallback;

    private SoterBiometricCanceller mSoterBiometricCanceller;
    private SoterBiometricStateCallback mSoterBiometricStateCallback;

    private AuthenticationParam() {
        //
    }

    public int getScene() {
        return mScene;
    }

    public String getChallenge() {
        return mChallenge;
    }

    public IWrapGetChallengeStr getIWrapGetChallengeStr() {
        return mIWrapGetChallengeStr;
    }

    public IWrapUploadSignature getIWrapUploadSignature() {
        return mIWrapUploadSignature;
    }

    public Context getContext() {
        return mContext;
    }

    public int getBiometricType() {
        return mBiometricType;
    }

    public SoterFingerprintCanceller getFingerprintCanceller() {
        return mFingerprintCanceller;
    }

    public SoterFingerprintStateCallback getSoterFingerprintStateCallback() {
        return mSoterFingerprintStateCallback;
    }

    public SoterBiometricCanceller getSoterBiometricCanceller() {
        return mSoterBiometricCanceller;
    }

    public SoterBiometricStateCallback getSoterBiometricStateCallback() {
        return mSoterBiometricStateCallback;
    }

    public String getPromptTitle() {
        return mPromptTitle;
    }

    public String getPromptSubTitle() {
        return mPromptSubTitle;
    }

    public String getPromptDescription() {
        return mPromptDescription;
    }

    public String getPromptButton() {
        return mPromptButton;
    }

    public boolean getUseBiometricPrompt() {
        return mUseBiometricPrompt;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public static class AuthenticationParamBuilder {

        private AuthenticationParam mParam = new AuthenticationParam();

        /**
         * Set which business scene you want to authenticate. Must initialized before.
         * @param scene the business scene
         * @return the param model itself
         */
        public AuthenticationParamBuilder setScene(int scene) {
            mParam.mScene = scene;
            return this;
        }

        /**
         * If you previously got the challenge, call this method to tell us. Note that you do not have to call
         * {@link AuthenticationParamBuilder#setIWrapGetChallengeStr(IWrapGetChallengeStr)} if you pass a valid challenge via this method.
         * @param challenge the pre-got challenge
         * @return the param model itself
         */
        public AuthenticationParamBuilder setPrefilledChallenge(String challenge) {
            mParam.mChallenge = challenge;
            return this;
        }

        /**
         * Set the get challenge wrapper so that we can try to fetch the challenge via network. Note that the wrapper would be useless
         * if you pass a valid challenge by calling {@link AuthenticationParamBuilder#setPrefilledChallenge(String)}
         * @param IWrapGetChallengeStr The network wrapper used to fetch challenge from network.
         * @return the param model itself
         */
        public AuthenticationParamBuilder setIWrapGetChallengeStr(IWrapGetChallengeStr IWrapGetChallengeStr) {
            mParam.mIWrapGetChallengeStr = IWrapGetChallengeStr;
            return this;
        }

        /**
         * Set the get upload signature wrapper so that we can try to upload the final result via network.
         * Instead of returning error, we will fill the final result to you in the end.
         * @param IWrapUploadSignature The network wrapper used to upload the final result.
         * @return the param model itself
         */
        public AuthenticationParamBuilder setIWrapUploadSignature(IWrapUploadSignature IWrapUploadSignature) {
            mParam.mIWrapUploadSignature = IWrapUploadSignature;
            return this;
        }

        /**
         * Set the context of your calling component.
         * @param context The context of your calling component
         * @return the param model itself
         */
        public AuthenticationParamBuilder setContext(Context context) {
            mParam.mContext = context;
            return this;
        }

        /**
         * The fingerprint canceller set by the application so that you can control cancellation event from your logic. <br/>
         * use {@link #setSoterBiometricCanceller} instead
         * @param fingerprintCanceller The cancellation controller
         * @return the param model itself
         */
        @Deprecated
        public AuthenticationParamBuilder setFingerprintCanceller(SoterFingerprintCanceller fingerprintCanceller) {
            mParam.mFingerprintCanceller = fingerprintCanceller;
            return this;
        }

        /**
         * The fingerprint status callback so that you can acknowledge the authentication status. Note that it is only the fingerprint authentication callback,
         * do not use it as the process result, just used for updating UI. <br/>
         * use {@link #setSoterBiometricStateCallback} instead
         * @param soterFingerprintStateCallback The fingerprint callback
         * @return the param model itself
         */
        @Deprecated
        public AuthenticationParamBuilder setSoterFingerprintStateCallback(SoterFingerprintStateCallback soterFingerprintStateCallback) {
            mParam.mSoterFingerprintStateCallback = soterFingerprintStateCallback;
            return this;
        }

        /**
         * The biometric canceller set by the application so that you can control cancellation event from your logic
         * @param soterBiometricCanceller The cancellation controller
         * @return the param model itself
         */
        public AuthenticationParamBuilder setSoterBiometricCanceller(SoterBiometricCanceller soterBiometricCanceller) {
            mParam.mSoterBiometricCanceller = soterBiometricCanceller;
            return this;
        }

        /**
         * The biometric status callback so that you can acknowledge the authentication status. Note that it is only the fingerprint authentication callback,
         * do not use it as the process result, just used for updating UI.
         * @param soterBiometricStateCallback The biometric callback
         * @return the param model itself
         */
        public AuthenticationParamBuilder setSoterBiometricStateCallback(SoterBiometricStateCallback soterBiometricStateCallback) {
            mParam.mSoterBiometricStateCallback = soterBiometricStateCallback;
            return this;
        }

        /**
         * The type of biometric authentication.
         * @param biometricType must be {@link com.tencent.soter.core.model.ConstantsSoter#FINGERPRINT_AUTH} or {@link com.tencent.soter.core.model.ConstantsSoter#FACEID_AUTH}
         * @return
         */
        public AuthenticationParamBuilder setBiometricType(int biometricType){
            mParam.mBiometricType = biometricType;
            return this;
        }

        /**
         * used for the BiometricPrompt，will use BiometricPrompt when api>31
         * @param title
         * @return
         */
        public AuthenticationParamBuilder setPromptTitle(String title) {
            mParam.mPromptTitle = title;
            return this;
        }

        /**
         * used for the BiometricPrompt
         * @param subTitle
         * @return
         */
        public AuthenticationParamBuilder setPromptSubTitle(String subTitle) {
            mParam.mPromptSubTitle = subTitle;
            return this;
        }

        /**
         * used for the BiometricPrompt
         * @param description
         * @return
         */
        public AuthenticationParamBuilder setPromptDescription(String description) {
            mParam.mPromptDescription = description;
            return this;
        }

        /**
         * used for the BiometricPrompt
         * @param button
         * @return
         */
        public AuthenticationParamBuilder setPromptButton(String button) {
            mParam.mPromptButton = button;
            return this;
        }

        /**
         * On Android S, the system will force to invoke BiometricPrompt when using FingerprintManager
         * @param useBiometricPrompt
         * @return
         */
        public AuthenticationParamBuilder setUseBiometricPrompt(boolean useBiometricPrompt) {
            mParam.mUseBiometricPrompt = useBiometricPrompt;
            return this;
        }

        public AuthenticationParam build() {
            return mParam;
        }
    }

    @Override
    public String toString() {
        return "AuthenticationParam{" +
                "mScene=" + mScene +
                ", mChallenge='" + mChallenge + '\'' +
                ", mIWrapGetChallengeStr=" + mIWrapGetChallengeStr +
                ", mIWrapUploadSignature=" + mIWrapUploadSignature +
                ", mContext=" + mContext +
                ", mBiometricType=" + mBiometricType +
                '}';
    }
}

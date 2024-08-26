/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.tencent.soter.core.biometric;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;

import com.tencent.soter.core.SoterCore;
import com.tencent.soter.core.model.ConstantsSoter;
import com.tencent.soter.core.model.SLogger;
import com.tencent.soter.core.model.SReporter;

import java.security.Signature;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.Mac;

/**
 * A class that coordinates access to the biometric hardware.
 * <p/>
 * On platforms before {@link Build.VERSION_CODES}, this class behaves as there would
 * be no fingerprint hardware available.
 */

@SuppressWarnings({"SameParameterValue", "WeakerAccess"})
public class BiometricManagerCompat {
    private static final String TAG = "Soter.BiometricManagerCompat";

    private Context mContext;
    private Integer mBiometricType;

    public static BiometricManagerCompat from(Context context, Integer biometricType) {
        return new BiometricManagerCompat(context, biometricType);
    }

    private BiometricManagerCompat(Context context, Integer biometricType) {
        mContext = context;
        mBiometricType = biometricType;
    }

    static final Map<Integer, IBiometricManager> IMPL_PROVIDER = new HashMap<>();

    static {
        IBiometricManager IMPL;
        if (SoterCore.isNativeSupportSoter()) {
            IMPL = new FingerprintManagerImpl();
        } else {
            IMPL = new LegacyFingerprintManagerImpl();
        }
        IMPL_PROVIDER.put(ConstantsSoter.FINGERPRINT_AUTH, IMPL);

        if (SoterCore.isNativeSupportSoter() && isNativeSupportFaceid()) {
            IMPL_PROVIDER.put(ConstantsSoter.FACEID_AUTH, new FaceidManagerImpl());
        }
    }

    public static boolean isNativeSupportFaceid(){
        try {
            Class t = Class.forName(FaceidManagerProxy.FACEMANAGER_FACTORY_CLASS_NAME);
            return  true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Determine if there is at least one fingerprint enrolled.
     *
     * @return true if at least one fingerprint is enrolled, false otherwise
     */
    public boolean hasEnrolledBiometric() {
        IBiometricManager IMPL = IMPL_PROVIDER.get(mBiometricType);
        if (IMPL == null){
            SLogger.i(TAG, "soter: Biometric provider not initialized type["+ mBiometricType +"]");
            return false;
        }
        return IMPL.hasEnrolledBiometric(mContext);
    }

    public String getBiometricName(){
        IBiometricManager IMPL = IMPL_PROVIDER.get(mBiometricType);
        if (IMPL == null){
            SLogger.i(TAG, "soter: Biometric provider not initialized type["+ mBiometricType +"]");
            return null;
        }
        return IMPL.getBiometricName(mContext);
    }

    /**
     * Determine if fingerprint hardware is present and functional.
     *
     * @return true if hardware is present and functional, false otherwise.
     */
    public boolean isHardwareDetected() {
        IBiometricManager IMPL = IMPL_PROVIDER.get(mBiometricType);
        if (IMPL == null){
            SLogger.i(TAG, "soter: Biometric provider not initialized type["+ mBiometricType +"]");
            return false;
        }
        return IMPL.isHardwareDetected(mContext);
    }

    /**
     * Check current fail time is available
     * @return true if fail time less than MAX_FAIL_NUM in {@link SoterBiometricAntiBruteForceStrategy}
     */
    public boolean isCurrentFailTimeAvailable() {
        return SoterBiometricAntiBruteForceStrategy.isCurrentFailTimeAvailable(mContext);
    }

    /**
     * Check current frozen time is released
     * @return true if frozen time more than FREEZE_SECOND in {@link SoterBiometricAntiBruteForceStrategy}
     */
    public boolean isCurrentTweenTimeAvailable(Context context) {
        return SoterBiometricAntiBruteForceStrategy.isCurrentTweenTimeAvailable(mContext);
    }


    /**
     * Request authentication of a crypto object. This call warms up the fingerprint hardware
     * and starts scanning for a fingerprint. It terminates when
     * {@link AuthenticationCallback#onAuthenticationError(int, CharSequence)} or
     * {@link AuthenticationCallback#onAuthenticationSucceeded(AuthenticationResult) is called, at
     * which point the object is no longer valid. The operation can be canceled by using the
     * provided cancel object.
     *
     * @param crypto   object associated with the call or null if none required.
     * @param flags    optional flags; should be 0
     * @param cancel   an object that can be used to cancel authentication
     * @param callback an object to receive authentication events
     * @param handler  an optional handler for events
     */
    public void authenticate(CryptoObject crypto, int flags,
                             CancellationSignal cancel, AuthenticationCallback callback,
                             Handler handler, Bundle extra) {
        IBiometricManager IMPL = IMPL_PROVIDER.get(mBiometricType);
        if (IMPL == null){
            SLogger.i(TAG, "soter: Biometric provider not initialized type["+ mBiometricType +"]");
            callback.onAuthenticationCancelled();
        }
        IMPL.authenticate(mContext, crypto, flags, cancel, callback, handler, extra);
    }

    /**
     * A wrapper class for the crypto objects supported by FingerprintManager. Currently the
     * framework supports {@link Signature} and {@link Cipher} objects.
     */
    public static class CryptoObject {

        private final Signature mSignature;
        private final Cipher mCipher;
        private final Mac mMac;

        public CryptoObject(Signature signature) {
            mSignature = signature;
            mCipher = null;
            mMac = null;

        }

        public CryptoObject(Cipher cipher) {
            mCipher = cipher;
            mSignature = null;
            mMac = null;
        }

        public CryptoObject(Mac mac) {
            mMac = mac;
            mCipher = null;
            mSignature = null;
        }

        /**
         * Get {@link Signature} object.
         *
         * @return {@link Signature} object or null if this doesn't contain one.
         */
        public Signature getSignature() {
            return mSignature;
        }

        /**
         * Get {@link Cipher} object.
         *
         * @return {@link Cipher} object or null if this doesn't contain one.
         */
        public Cipher getCipher() {
            return mCipher;
        }

        /**
         * Get {@link Mac} object.
         *
         * @return {@link Mac} object or null if this doesn't contain one.
         */
        public Mac getMac() {
            return mMac;
        }
    }

    @SuppressWarnings("unused")
    public static final class AuthenticationResult {
        private CryptoObject mCryptoObject;

        public AuthenticationResult(CryptoObject crypto) {
            mCryptoObject = crypto;
        }

        public CryptoObject getCryptoObject() {
            return mCryptoObject;
        }
    }


    public static abstract class AuthenticationCallback {
        /**
         * Called when an unrecoverable error has been encountered and the operation is complete.
         * No further callbacks will be made on this object.
         *
         * @param errMsgId  An integer identifying the error message
         * @param errString A human-readable error string that can be shown in UI
         */
        public void onAuthenticationError(int errMsgId, CharSequence errString) {
        }

        /**
         * Called when a recoverable error has been encountered during authentication. The help
         * string is provided to give the user guidance for what went wrong, such as
         * "Sensor dirty, please clean it."
         *
         * @param helpMsgId  An integer identifying the error message
         * @param helpString A human-readable string that can be shown in UI
         */
        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        }

        /**
         * Called when a fingerprint is recognized.
         *
         * @param result An object containing authentication-related data
         */
        public void onAuthenticationSucceeded(AuthenticationResult result) {
        }

        /**
         * Called when a fingerprint is valid but not recognized.
         */
        public void onAuthenticationFailed() {
        }

        /**
         * Called when the fingerprint is cancelled by user actively.
         */
        public void onAuthenticationCancelled() {
        }
    }

    private interface IBiometricManager {
        boolean hasEnrolledBiometric(Context context);

        boolean isHardwareDetected(Context context);

        String getBiometricName(Context context);

        void authenticate(Context context,
                          CryptoObject crypto, int flags,
                          CancellationSignal cancel,
                          AuthenticationCallback callback, Handler handler, Bundle extra);
    }

    private static class LegacyFingerprintManagerImpl implements IBiometricManager {

        public LegacyFingerprintManagerImpl() {
        }

        @Override
        public boolean hasEnrolledBiometric(Context context) {
            return false;
        }

        @Override
        public boolean isHardwareDetected(Context context) {
            SReporter.reportError(ConstantsSoter.ERR_SOTER_INNER, "LegacyFingerprintManagerImpl.isHardwareDetected return false");
            return false;
        }

        @Override
        public String getBiometricName(Context context) {
            return null;
        }

        @Override
        public void authenticate(Context context,
                                 CryptoObject crypto, int flags,
                                 CancellationSignal cancel,
                                 AuthenticationCallback callback, Handler handler, Bundle extra) {
        }

    }

    private static class FingerprintManagerImpl implements IBiometricManager {

        private static final String TAG = "Soter.BiometricManagerCompat.Fingerprint";

        public FingerprintManagerImpl() {
        }

        @Override
        public boolean hasEnrolledBiometric(Context context) {
            return FingerprintManagerProxy.hasEnrolledFingerprints(context);
        }


        @Override
        public boolean isHardwareDetected(Context context) {
            return FingerprintManagerProxy.isHardwareDetected(context);
        }

        @Override
        public String getBiometricName(Context context) {
            return "fingerprint";
        }

        @Override
        public void authenticate(Context context,
                                 CryptoObject crypto, int flags,
                                 CancellationSignal cancel,
                                 AuthenticationCallback callback, Handler handler, Bundle extra) {

            FingerprintManagerProxy.authenticate(
                    context,
                    wrapCryptoObject(crypto), flags,
                    cancel,
                    wrapCallback(context, callback), handler, extra);
        }

        private static FingerprintManagerProxy.CryptoObject wrapCryptoObject(CryptoObject cryptoObject) {
            if (cryptoObject == null) {
                return null;
            } else if (cryptoObject.getCipher() != null) {
                return new FingerprintManagerProxy.CryptoObject(cryptoObject.getCipher());
            } else if (cryptoObject.getSignature() != null) {
                return new FingerprintManagerProxy.CryptoObject(cryptoObject.getSignature());
            } else if (cryptoObject.getMac() != null) {
                return new FingerprintManagerProxy.CryptoObject(cryptoObject.getMac());
            } else {
                return null;
            }
        }

        private static CryptoObject unwrapCryptoObject(FingerprintManagerProxy.CryptoObject cryptoObject) {
            if (cryptoObject == null) {
                return null;
            } else if (cryptoObject.getCipher() != null) {
                return new CryptoObject(cryptoObject.getCipher());
            } else if (cryptoObject.getSignature() != null) {
                return new CryptoObject(cryptoObject.getSignature());
            } else if (cryptoObject.getMac() != null) {
                return new CryptoObject(cryptoObject.getMac());
            } else {
                return null;
            }
        }

        private static FingerprintManagerProxy.AuthenticationCallback wrapCallback(
                final Context context,
                final AuthenticationCallback callback) {
            return new FingerprintManagerProxy.AuthenticationCallback() {

                private boolean mMarkPermanentlyCallbacked = false;

                @Override
                public void onAuthenticationError(int errMsgId, CharSequence errString) {
                    SLogger.d(TAG, "soter: basic onAuthenticationError");
                    if(mMarkPermanentlyCallbacked) {
                        return;
                    }
                    mMarkPermanentlyCallbacked = true;
                    // filter cases when user has already cancelled the authentication.
                    if(errMsgId == FingerprintManager.FINGERPRINT_ERROR_CANCELED || errMsgId == FingerprintManager.FINGERPRINT_ERROR_USER_CANCELED) {
                        SLogger.i(TAG, "soter: user cancelled fingerprint authen");
                        callback.onAuthenticationCancelled();
                        return;
                    }
                    //sync freeze state
                    if(errMsgId == FingerprintManager.FINGERPRINT_ERROR_LOCKOUT || errMsgId == FingerprintManager.FINGERPRINT_ERROR_LOCKOUT_PERMANENT) {
                        SLogger.i(TAG, "soter: system call too many trial.");
                        if(!SoterBiometricAntiBruteForceStrategy.isCurrentFailTimeAvailable(context)
                                && !SoterBiometricAntiBruteForceStrategy.isCurrentTweenTimeAvailable(context)
                                && !SoterBiometricAntiBruteForceStrategy.isSystemHasAntiBruteForce()) {
                            SoterBiometricAntiBruteForceStrategy.freeze(context);
                        }
                        mMarkPermanentlyCallbacked = false;

                        if (errMsgId == FingerprintManager.FINGERPRINT_ERROR_LOCKOUT) {
                            onAuthenticationError(ConstantsSoter.ERR_BIOMETRIC_FAIL_MAX, ConstantsSoter.SOTER_BIOMETRIC_ERR_FAIL_MAX_MSG);
                        } else {
                            onAuthenticationError(ConstantsSoter.ERR_BIOMETRIC_FAIL_MAX_PERMANENT, ConstantsSoter.SOTER_BIOMETRIC_ERR_FAIL_MAX_MSG);
                        }
                        return;
                    }
                    callback.onAuthenticationError(errMsgId, errString);
                }

                @Override
                public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                    SLogger.d(TAG, "soter: basic onAuthenticationHelp");
                    if(mMarkPermanentlyCallbacked) {
                        return;
                    }
                    if(!shouldInformTooManyTrial(this, context)) {
                        callback.onAuthenticationHelp(helpMsgId, helpString);
                    }
                }

                @Override
                public void onAuthenticationSucceeded(
                        FingerprintManagerProxy.AuthenticationResultInternal result) {
                    SLogger.d(TAG, "soter: basic onAuthenticationSucceeded");
                    if(mMarkPermanentlyCallbacked) {
                        return;
                    }
                    if(!shouldInformTooManyTrial(this, context)) {
                        // unfreeze
                        if(!SoterBiometricAntiBruteForceStrategy.isSystemHasAntiBruteForce()) {
                            SoterBiometricAntiBruteForceStrategy.unFreeze(context);
                        }
                        mMarkPermanentlyCallbacked = true;
                        callback.onAuthenticationSucceeded(new AuthenticationResult(
                                unwrapCryptoObject(result.getCryptoObject())));
                    }
                }

                @Override
                public void onAuthenticationFailed() {
                    SLogger.d(TAG, "soter: basic onAuthenticationFailed");
                    if(mMarkPermanentlyCallbacked) {
                        return;
                    }
                    if(!shouldInformTooManyTrial(this, context)) {
                        if(!SoterBiometricAntiBruteForceStrategy.isSystemHasAntiBruteForce()) {
                            SoterBiometricAntiBruteForceStrategy.addFailTime(context);
                            if(!SoterBiometricAntiBruteForceStrategy.isCurrentFailTimeAvailable(context)) {
                                SLogger.w(TAG, "soter: too many fail trials");
                                SoterBiometricAntiBruteForceStrategy.freeze(context);
                                informTooManyTrial(this);
                                return;
                            }
                        }
                        callback.onAuthenticationFailed();
                    }

                }
            };
        }

        //check brute fore strategy should effect, return true when effected, else return false
        private static boolean shouldInformTooManyTrial(FingerprintManagerProxy.AuthenticationCallback callback, Context context) {
            if(SoterBiometricAntiBruteForceStrategy.isSystemHasAntiBruteForce()) {
                SLogger.v(TAG, "soter: using system anti brute force strategy");
                return false;
            }
            if (SoterBiometricAntiBruteForceStrategy.isCurrentTweenTimeAvailable(context)) {
                if (!SoterBiometricAntiBruteForceStrategy.isCurrentFailTimeAvailable(context)) {
                    // unfreeze
                    SLogger.v(TAG, "soter: unfreeze former frozen status");
                    SoterBiometricAntiBruteForceStrategy.unFreeze(context);
                }
                return false;
            } else if (SoterBiometricAntiBruteForceStrategy.isCurrentFailTimeAvailable(context)) {
                SLogger.v(TAG, "soter: failure time available");
                return false;
            } else {
                informTooManyTrial(callback);
                return true;
            }
        }

        private static void informTooManyTrial(FingerprintManagerProxy.AuthenticationCallback callback) {
            SLogger.w(TAG, "soter: too many fail fingerprint callback. inform it.");
            callback.onAuthenticationError(ConstantsSoter.ERR_BIOMETRIC_FAIL_MAX, ConstantsSoter.SOTER_BIOMETRIC_ERR_FAIL_MAX_MSG);
        }
    }

    private static class FaceidManagerImpl implements IBiometricManager {

        private static final String TAG = "Soter.BiometricManagerCompat.Faceid";

        public FaceidManagerImpl(){
        }

        @Override
        public boolean hasEnrolledBiometric(Context context) {
            return FaceidManagerProxy.hasEnrolledFaceids(context);
        }

        @Override
        public boolean isHardwareDetected(Context context) {
            return FaceidManagerProxy.isHardwareDetected(context);
        }

        @Override
        public String getBiometricName(Context context) {
            return FaceidManagerProxy.getBiometricName(context);
        }

        @Override
        public void authenticate(Context context,
                                 CryptoObject crypto,
                                 int flags,
                                 CancellationSignal cancel,
                                 AuthenticationCallback callback, Handler handler, Bundle extra) {

            FaceidManagerProxy.authenticate(
                    context,
                    wrapCryptoObject(crypto), flags,
                    cancel,
                    wrapCallback(context, callback), handler);

        }

        private static FaceidManagerProxy.CryptoObject wrapCryptoObject(
                CryptoObject cryptoObject) {
            if (cryptoObject == null) {
                return null;
            } else if (cryptoObject.getCipher() != null) {
                return new FaceidManagerProxy.CryptoObject(cryptoObject.getCipher());
            } else if (cryptoObject.getSignature() != null) {
                return new FaceidManagerProxy.CryptoObject(cryptoObject.getSignature());
            } else if (cryptoObject.getMac() != null) {
                return new FaceidManagerProxy.CryptoObject(cryptoObject.getMac());
            } else {
                return null;
            }
        }

        private static CryptoObject unwrapCryptoObject(FaceidManagerProxy.CryptoObject cryptoObject) {
            if (cryptoObject == null) {
                return null;
            } else if (cryptoObject.getCipher() != null) {
                return new CryptoObject(cryptoObject.getCipher());
            } else if (cryptoObject.getSignature() != null) {
                return new CryptoObject(cryptoObject.getSignature());
            } else if (cryptoObject.getMac() != null) {
                return new CryptoObject(cryptoObject.getMac());
            } else {
                return null;
            }
        }

        private static FaceidManagerProxy.AuthenticationCallback wrapCallback(final Context context,
                                                                                         final AuthenticationCallback callback) {
            return new FaceidManagerProxy.AuthenticationCallback() {

                private boolean mMarkPermanentlyCallbacked = false;

                @Override
                public void onAuthenticationError(int errMsgId, CharSequence errString) {
                    SLogger.d(TAG, "soter: basic onAuthenticationError code[%d], msg[%s] entered.", errMsgId, errString);
                    if(mMarkPermanentlyCallbacked) {
                        SLogger.d(TAG, "soter: basic onAuthenticationError code[%d], msg[%s] returned cause permanently callback.", errMsgId, errString);
                        return;
                    }
                    mMarkPermanentlyCallbacked = true;

                    // filter cases when user has already cancelled the authentication.
                    if(errMsgId == FaceManager.FACE_ERROR_CANCELED) {
                        SLogger.i(TAG, "soter: basic onAuthenticationError code[%d], msg[%s] callbacked and returned cause FACE_ERROR_CANCELED got.", errMsgId, errString);
                        callback.onAuthenticationCancelled();
                        return;
                    }

                    //sync freeze state
                    if(errMsgId == FaceManager.FACE_ERROR_LOCKOUT) {
                        SLogger.i(TAG, "soter: basic onAuthenticationError code[%d], msg[%s] callbacked and returned cause FACE_ERROR_LOCKOUT got.", errMsgId, errString);
                        if(!SoterBiometricAntiBruteForceStrategy.isCurrentFailTimeAvailable(context)
                                && !SoterBiometricAntiBruteForceStrategy.isCurrentTweenTimeAvailable(context)
                                && !SoterBiometricAntiBruteForceStrategy.isSystemHasAntiBruteForce()) {
                            SoterBiometricAntiBruteForceStrategy.freeze(context);
                        }
                        callback.onAuthenticationError(ConstantsSoter.ERR_BIOMETRIC_FAIL_MAX, ConstantsSoter.SOTER_BIOMETRIC_ERR_FAIL_MAX_MSG);
                        return;
                    }


                    SLogger.d(TAG, "soter: basic onAuthenticationError code[%d], msg[%s] callbacked and returned.", errMsgId, errString);
                    callback.onAuthenticationError(errMsgId, errString);
                }

                @Override
                public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                    SLogger.d(TAG, "soter: basic onAuthenticationHelp helpMsgId[%d], helpString[%s]", helpMsgId, helpString);
                    long checkTime = System.currentTimeMillis();
                    if(mMarkPermanentlyCallbacked) {
                        return;
                    }
                    if(!shouldInformTooManyTrial(this, context)) {
                        callback.onAuthenticationHelp(helpMsgId, helpString);
                    }
                }

                @Override
                public void onAuthenticationSucceeded(FaceidManagerProxy.AuthenticationResult result) {
                    SLogger.d(TAG, "soter: basic onAuthenticationSucceeded");
                    if(mMarkPermanentlyCallbacked) {
                        return;
                    }
                    mMarkPermanentlyCallbacked = true;
                    if(!shouldInformTooManyTrial(this, context)) {
                        // unfreeze
                        if(!SoterBiometricAntiBruteForceStrategy.isSystemHasAntiBruteForce()) {
                            SoterBiometricAntiBruteForceStrategy.unFreeze(context);
                        }
                        callback.onAuthenticationSucceeded(new AuthenticationResult(unwrapCryptoObject(result.getCryptoObject())));
                    }
                }

                @Override
                public void onAuthenticationFailed() {
                    SLogger.d(TAG, "soter: basic onAuthenticationFailed");
                    if(mMarkPermanentlyCallbacked) {
                        return;
                    }
                    mMarkPermanentlyCallbacked = true;
                    if(!shouldInformTooManyTrial(this, context)) {
                        if(!SoterBiometricAntiBruteForceStrategy.isSystemHasAntiBruteForce()) {
                            SoterBiometricAntiBruteForceStrategy.addFailTime(context);
                            if(!SoterBiometricAntiBruteForceStrategy.isCurrentFailTimeAvailable(context)) {
                                SLogger.w(TAG, "soter: too many fail trials");
                                SoterBiometricAntiBruteForceStrategy.freeze(context);
                                informTooManyTrial(this);
                                return;
                            }
                        }
                    }
                    callback.onAuthenticationFailed();
                }
            };
        }

        private static boolean shouldInformTooManyTrial(FaceidManagerProxy.AuthenticationCallback callback, Context context) {
            if(SoterBiometricAntiBruteForceStrategy.isSystemHasAntiBruteForce()) {
                SLogger.v(TAG, "soter: using system anti brute force strategy");
                return false;
            }
            if (SoterBiometricAntiBruteForceStrategy.isCurrentTweenTimeAvailable(context)) {
                if (!SoterBiometricAntiBruteForceStrategy.isCurrentFailTimeAvailable(context)) {
                    // unfreeze
                    SLogger.v(TAG, "soter: unfreeze former frozen status");
                    SoterBiometricAntiBruteForceStrategy.unFreeze(context);
                }
                return false;
            } else if (SoterBiometricAntiBruteForceStrategy.isCurrentFailTimeAvailable(context)) {
                SLogger.v(TAG, "soter: failure time available");
                return false;
            } else {
                informTooManyTrial(callback);
                return true;
            }
        }

        private static void informTooManyTrial(FaceidManagerProxy.AuthenticationCallback callback) {
            SLogger.w(TAG, "soter: too many fail callback. inform it.");
            callback.onAuthenticationError(ConstantsSoter.ERR_BIOMETRIC_FAIL_MAX, ConstantsSoter.SOTER_BIOMETRIC_ERR_FAIL_MAX_MSG);
        }
    }
}

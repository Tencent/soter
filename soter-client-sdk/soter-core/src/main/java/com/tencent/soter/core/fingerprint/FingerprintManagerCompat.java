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

package com.tencent.soter.core.fingerprint;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;

import com.tencent.soter.core.SoterCore;
import com.tencent.soter.core.model.ConstantsSoter;
import com.tencent.soter.core.model.SLogger;

import java.security.Signature;

import javax.crypto.Cipher;
import javax.crypto.Mac;

/**
 * A class that coordinates access to the fingerprint hardware.
 * <p/>
 * On platforms before {@link Build.VERSION_CODES}, this class behaves as there would
 * be no fingerprint hardware available.
 */

@SuppressWarnings({"SameParameterValue", "WeakerAccess"})
public class FingerprintManagerCompat {
    private static final String TAG = "Soter.FingerprintManagerCompat";

    private Context mContext;

    public static FingerprintManagerCompat from(Context context) {
        return new FingerprintManagerCompat(context);
    }

    private FingerprintManagerCompat(Context context) {
        mContext = context;
    }

    static final FingerprintManagerCompatImpl IMPL;

    static {
        if (SoterCore.isNativeSupportSoter()) {
            IMPL = new Api23FingerprintManagerCompatImpl();
        } else {
            IMPL = new LegacyFingerprintManagerCompatImpl();
        }
    }

    /**
     * Determine if there is at least one fingerprint enrolled.
     *
     * @return true if at least one fingerprint is enrolled, false otherwise
     */
    public boolean hasEnrolledFingerprints() {
        return IMPL.hasEnrolledFingerprints(mContext);
    }

    /**
     * Determine if fingerprint hardware is present and functional.
     *
     * @return true if hardware is present and functional, false otherwise.
     */
    public boolean isHardwareDetected() {
        return IMPL.isHardwareDetected(mContext);
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
                             Handler handler) {
        IMPL.authenticate(mContext, crypto, flags, cancel, callback, handler);
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

    private interface FingerprintManagerCompatImpl {
        boolean hasEnrolledFingerprints(Context context);

        boolean isHardwareDetected(Context context);

        void authenticate(Context context, CryptoObject crypto, int flags,
                          CancellationSignal cancel, AuthenticationCallback callback, Handler handler);
    }

    private static class LegacyFingerprintManagerCompatImpl
            implements FingerprintManagerCompatImpl {

        public LegacyFingerprintManagerCompatImpl() {
        }

        @Override
        public boolean hasEnrolledFingerprints(Context context) {
            return false;
        }

        @Override
        public boolean isHardwareDetected(Context context) {
            return false;
        }

        @Override
        public void authenticate(Context context, CryptoObject crypto, int flags,
                                 CancellationSignal cancel, AuthenticationCallback callback, Handler handler) {
        }
    }

    private static class Api23FingerprintManagerCompatImpl implements FingerprintManagerCompatImpl {

        public Api23FingerprintManagerCompatImpl() {
        }

        @Override
        public boolean hasEnrolledFingerprints(Context context) {
            return FingerprintManagerCompatApi23.hasEnrolledFingerprints(context);
//            return false;
        }


        @Override
        public boolean isHardwareDetected(Context context) {
            return FingerprintManagerCompatApi23.isHardwareDetected(context);
//            return false;
        }

        @Override
        public void authenticate(Context context, CryptoObject crypto, int flags,
                                 CancellationSignal cancel, AuthenticationCallback callback, Handler handler) {
            FingerprintManagerCompatApi23.authenticate(context, wrapCryptoObject(crypto), flags,
                    cancel,
                    wrapCallback(context, callback), handler);
//            return;
        }

        private static FingerprintManagerCompatApi23.CryptoObject wrapCryptoObject(
                CryptoObject cryptoObject) {
            if (cryptoObject == null) {
                return null;
            } else if (cryptoObject.getCipher() != null) {
                return new FingerprintManagerCompatApi23.CryptoObject(cryptoObject.getCipher());
            } else if (cryptoObject.getSignature() != null) {
                return new FingerprintManagerCompatApi23.CryptoObject(cryptoObject.getSignature());
            } else if (cryptoObject.getMac() != null) {
                return new FingerprintManagerCompatApi23.CryptoObject(cryptoObject.getMac());
            } else {
                return null;
            }
        }

        private static CryptoObject unwrapCryptoObject(
                FingerprintManagerCompatApi23.CryptoObject cryptoObject) {
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

        private static FingerprintManagerCompatApi23.AuthenticationCallback wrapCallback(final Context context,
                final AuthenticationCallback callback) {
            return new FingerprintManagerCompatApi23.AuthenticationCallback() {

                private boolean mMarkPermanentlyCallbacked = false;

                @Override
                public void onAuthenticationError(int errMsgId, CharSequence errString) {
                    SLogger.d(TAG, "soter: basic onAuthenticationError");
                    if(mMarkPermanentlyCallbacked) {
                        return;
                    }
                    mMarkPermanentlyCallbacked = true;
                    // filter cases when user has already cancelled the authentication.
                    if(errMsgId == FingerprintManager.FINGERPRINT_ERROR_CANCELED) {
                        SLogger.i(TAG, "soter: user cancelled fingerprint authen");
                        callback.onAuthenticationCancelled();
                        return;
                    }
                    //sync freeze state
                    if(errMsgId == FingerprintManager.FINGERPRINT_ERROR_LOCKOUT) {
                        SLogger.i(TAG, "soter: system call too many trial.");
                        if(!SoterAntiBruteForceStrategy.isCurrentFailTimeAvailable(context)
                                && !SoterAntiBruteForceStrategy.isCurrentTweenTimeAvailable(context)
                                && !SoterAntiBruteForceStrategy.isSystemHasAntiBruteForce()) {
                            SoterAntiBruteForceStrategy.freeze(context);
                        }
                        mMarkPermanentlyCallbacked = false;
                        onAuthenticationError(ConstantsSoter.ERR_FINGERPRINT_FAIL_MAX, ConstantsSoter.SOTER_FINGERPRINT_ERR_FAIL_MAX_MSG);
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
                    if(!checkBruteForce(this, context)) {
                        callback.onAuthenticationHelp(helpMsgId, helpString);
                    }
                }

                @Override
                public void onAuthenticationSucceeded(
                        FingerprintManagerCompatApi23.AuthenticationResultInternal result) {
                    SLogger.d(TAG, "soter: basic onAuthenticationSucceeded");
                    if(mMarkPermanentlyCallbacked) {
                        return;
                    }
                    if(!checkBruteForce(this, context)) {
                        // unfreeze
                        if(!SoterAntiBruteForceStrategy.isSystemHasAntiBruteForce()) {
                            SoterAntiBruteForceStrategy.unFreeze(context);
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
                    if(!checkBruteForce(this, context)) {
                        if(!SoterAntiBruteForceStrategy.isSystemHasAntiBruteForce()) {
                            SoterAntiBruteForceStrategy.addFailTime(context);
                            if(!SoterAntiBruteForceStrategy.isCurrentFailTimeAvailable(context)) {
                                SLogger.w(TAG, "soter: too many fail trials");
                                SoterAntiBruteForceStrategy.freeze(context);
                                informTooManyTrial(this);
                                return;
                            }
                        }
                        callback.onAuthenticationFailed();
                    }

                }
            };
        }

        private static boolean checkBruteForce(FingerprintManagerCompatApi23.AuthenticationCallback callback, Context context) {
            if(SoterAntiBruteForceStrategy.isSystemHasAntiBruteForce()) {
                SLogger.v(TAG, "soter: using system anti brute force strategy");
                return false;
            }
            if (SoterAntiBruteForceStrategy.isCurrentTweenTimeAvailable(context)) {
                if (!SoterAntiBruteForceStrategy.isCurrentFailTimeAvailable(context)) {
                    // unfreeze
                    SLogger.v(TAG, "soter: unfreeze former frozen status");
                    SoterAntiBruteForceStrategy.unFreeze(context);
                }
                return false;
            } else if (SoterAntiBruteForceStrategy.isCurrentFailTimeAvailable(context)) {
                SLogger.v(TAG, "soter: failure time available");
                return false;
            } else {
                informTooManyTrial(callback);
                return true;
            }
        }

        private static void informTooManyTrial(FingerprintManagerCompatApi23.AuthenticationCallback callback) {
            SLogger.w(TAG, "soter: too many fail fingerprint callback. inform it.");
            callback.onAuthenticationError(ConstantsSoter.ERR_FINGERPRINT_FAIL_MAX, ConstantsSoter.SOTER_FINGERPRINT_ERR_FAIL_MAX_MSG);
        }
    }
}

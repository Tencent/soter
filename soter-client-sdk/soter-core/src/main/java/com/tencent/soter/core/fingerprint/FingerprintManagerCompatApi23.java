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

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Handler;

import com.tencent.soter.core.model.SLogger;

import java.security.Signature;

import javax.crypto.Cipher;
import javax.crypto.Mac;

/**
 * Actual FingerprintManagerCompat implementation for API level 23 and later.
 *
 * @author henryye
 */
@SuppressWarnings({"ResourceType", "WeakerAccess"})
@SuppressLint("NewApi")
final class FingerprintManagerCompatApi23 {
    private static final String TAG = "Soter.FingerprintManagerCompatApi23";

    public static final String FINGERPRINT_SERVICE = "fingerprint";

    private static FingerprintManager getFingerprintManager(Context ctx) {
        return (FingerprintManager) ctx.getSystemService(FINGERPRINT_SERVICE);
    }

    public static boolean hasEnrolledFingerprints(Context context) {
        if (checkSelfPermission(Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            SLogger.e(TAG, "soter: permission check failed: hasEnrolledFingerprints");
            return false;
        }
        FingerprintManager mgr = getFingerprintManager(context);
        if(mgr != null) {
            return mgr.hasEnrolledFingerprints();
        } else {
            SLogger.e(TAG, "soter: fingerprint manager is null in hasEnrolledFingerprints! Should never happen");
            return false;
        }
    }

    @SuppressWarnings("UnusedParameters")
    private static int checkSelfPermission(String useFingerprint) {
        return 0;
    }

    public static boolean isHardwareDetected(Context context) {
        if (checkSelfPermission(Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            SLogger.e(TAG, "soter: permission check failed: isHardwareDetected");
            return false;
        }
        FingerprintManager mgr = getFingerprintManager(context);
        if (mgr != null) {
            return mgr.isHardwareDetected();
        } else {
            SLogger.e(TAG, "soter: fingerprint manager is null in isHardwareDetected! Should never happen");
            return false;
        }
    }

    public static void authenticate(Context context, CryptoObject crypto, int flags, Object cancel,
                                    AuthenticationCallback callback, Handler handler) {
        if (checkSelfPermission(Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            SLogger.e(TAG, "soter: permission check failed: authenticate");
            return;
        }
        FingerprintManager mgr = getFingerprintManager(context);
        if(mgr != null) {
            mgr.authenticate(wrapCryptoObject(crypto),
                    (android.os.CancellationSignal) cancel, flags,
                    wrapCallback(callback), handler);
        } else {
            SLogger.e(TAG, "soter: fingerprint manager is null in authenticate! Should never happen");
        }

    }

    private static FingerprintManager.CryptoObject wrapCryptoObject(CryptoObject cryptoObject) {
        if (cryptoObject == null) {
            return null;
        } else if (cryptoObject.getCipher() != null) {
            return new FingerprintManager.CryptoObject(cryptoObject.getCipher());
        } else if (cryptoObject.getSignature() != null) {
            return new FingerprintManager.CryptoObject(cryptoObject.getSignature());
        } else if (cryptoObject.getMac() != null) {
            return new FingerprintManager.CryptoObject(cryptoObject.getMac());
        } else {
            return null;
        }
    }

    private static CryptoObject unwrapCryptoObject(FingerprintManager.CryptoObject cryptoObject) {
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

    private static FingerprintManager.AuthenticationCallback wrapCallback(
            final AuthenticationCallback callback) {
        return new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                SLogger.d(TAG, "hy: lowest level return onAuthenticationError");
                callback.onAuthenticationError(errMsgId, errString);
            }

            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                SLogger.d(TAG, "hy: lowest level return onAuthenticationHelp");
                callback.onAuthenticationHelp(helpMsgId, helpString);
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                SLogger.d(TAG, "hy: lowest level return onAuthenticationSucceeded");
                callback.onAuthenticationSucceeded(new AuthenticationResultInternal(
                        unwrapCryptoObject(result.getCryptoObject())));
            }

            @Override
            public void onAuthenticationFailed() {
                SLogger.d(TAG, "hy: lowest level return onAuthenticationFailed");
                callback.onAuthenticationFailed();
            }
        };
    }

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

        public Signature getSignature() { return mSignature; }
        public Cipher getCipher() { return mCipher; }
        public Mac getMac() { return mMac; }
    }

    public static final class AuthenticationResultInternal {
        private CryptoObject mCryptoObject;

        public AuthenticationResultInternal(CryptoObject crypto) {
            mCryptoObject = crypto;
        }

        public CryptoObject getCryptoObject() { return mCryptoObject; }
    }

    public static abstract class AuthenticationCallback {

        public void onAuthenticationError(int errMsgId, CharSequence errString) { }
        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) { }
        public void onAuthenticationSucceeded(AuthenticationResultInternal result) { }
        public void onAuthenticationFailed() { }
    }
}

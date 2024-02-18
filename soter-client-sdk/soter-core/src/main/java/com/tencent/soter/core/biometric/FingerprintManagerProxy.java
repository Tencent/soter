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

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricManager;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.tencent.soter.core.model.ConstantsSoter;
import com.tencent.soter.core.model.SLogger;
import com.tencent.soter.core.model.SReporter;
import com.tencent.soter.core.model.SoterCoreUtil;

import java.security.Signature;

import javax.crypto.Cipher;
import javax.crypto.Mac;

import static com.tencent.soter.core.model.ConstantsSoter.ERR_ANDROID_HAREWARE_NOT_SUPPORT;

/**
 * Actual BiometricManagerCompat implementation for API level 23 and later.
 *
 * @author henryye
 */
@SuppressWarnings({"ResourceType", "WeakerAccess"})
@SuppressLint("NewApi")
final class FingerprintManagerProxy {
    private static final String TAG = "Soter.FingerprintManagerProxy";

    public static final String FINGERPRINT_SERVICE = "fingerprint";

    public static boolean sCLOSE_API31 = false;

    private static FingerprintManager getFingerprintManager(Context ctx) {
        return (FingerprintManager) ctx.getSystemService(FINGERPRINT_SERVICE);
    }

    public static boolean hasEnrolledFingerprints(Context context) {
        if (checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            SLogger.e(TAG, "soter: permission check failed: hasEnrolledBiometric");
            return false;
        }
        try {
            FingerprintManager mgr = getFingerprintManager(context);
            if(mgr != null) {
                return mgr.hasEnrolledFingerprints();
            } else {
                SLogger.e(TAG, "soter: fingerprint manager is null in hasEnrolledBiometric! Should never happen");
                return false;
            }
        } catch (SecurityException e) {
            SLogger.e(TAG, "soter: triggered SecurityException in hasEnrolledBiometric! Make sure you declared USE_FINGERPRINT in AndroidManifest.xml");
            return false;
        }

    }

    private static int checkSelfPermission(Context context, String permission) {
        if(context == null) {
            SLogger.e(TAG, "soter: check self permission: context is null");
            return -1;
        }
        if(SoterCoreUtil.isNullOrNil(permission)) {
            SLogger.e(TAG, "soter: requested permission is null or nil");
            return -1;
        }
        if (Build.VERSION.SDK_INT < 23) {
            SLogger.d(TAG, "soter: below 23. directly return.");
            return 0;
        }
        return context.checkSelfPermission(permission);
    }

    /**
     * Check whether there's hardware detected in the device
     * @param context The context
     * @return true if there's hardware detected
     */
    public static boolean isHardwareDetected(Context context) {
        if (checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            SLogger.e(TAG, "soter: permission check failed: isHardwareDetected");
            return false;
        }
        try {
            FingerprintManager mgr = getFingerprintManager(context);
            if (mgr != null) {
                boolean ret = mgr.isHardwareDetected();
                if (!ret) {
                    SReporter.reportError(ERR_ANDROID_HAREWARE_NOT_SUPPORT, "FingerprintManager.isHardwareDetected return false");
                }
                return ret;
            } else {
                SLogger.e(TAG, "soter: fingerprint manager is null in isHardwareDetected! Should never happen");
                return false;
            }
        } catch (SecurityException e) {
            SLogger.e(TAG, "soter: triggered SecurityException in isHardwareDetected! Make sure you declared USE_FINGERPRINT in AndroidManifest.xml");
            return false;
        }
    }

    public static void authenticate(Context context, CryptoObject crypto, int flags, Object cancel,
                                    AuthenticationCallback callback, Handler handler, Bundle extra) {
        boolean useBiometricPrompt = extra.getBoolean("use_biometric_prompt");
        SLogger.i(TAG, "use_biometric_prompt: %s, sdk_version: %s", useBiometricPrompt, Build.VERSION.SDK_INT);
        if (useBiometricPrompt && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            authenticateApi28(context, crypto, flags, cancel, callback, handler, extra);
        } else {
            authenticateLegacy(context, crypto, flags, cancel, callback, handler);
        }
    }

    @SuppressLint("MissingPermission")
    private static void authenticateLegacy(Context context, CryptoObject crypto, int flags, Object cancel,
                                           AuthenticationCallback callback, Handler handler) {
        if (checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            SLogger.e(TAG, "soter: permission check failed: authenticate");
            return;
        }
        try {
            FingerprintManager mgr = getFingerprintManager(context);
            if(mgr != null) {
                mgr.authenticate(wrapCryptoObject(crypto),
                        (android.os.CancellationSignal) cancel, flags,
                        wrapCallback(callback), handler);
            } else {
                SLogger.e(TAG, "soter: fingerprint manager is null in authenticate! Should never happen");
            }
        } catch (SecurityException e) {
            SLogger.e(TAG, "soter: triggered SecurityException in authenticate! Make sure you declared USE_FINGERPRINT in AndroidManifest.xml");
        }
    }
    @SuppressLint("MissingPermission")
    private static void authenticateApi28(Context context, CryptoObject crypto, int flags, Object cancel,
                                          final AuthenticationCallback callback, Handler handler, Bundle extra) {
        if (checkSelfPermission(context, Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED) {
            SLogger.e(TAG, "soter: permission check failed: authenticate");
            return;
        }

        BiometricPrompt.Builder builder = new BiometricPrompt.Builder(context);
        builder.setDeviceCredentialAllowed(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG);
        }
        builder.setTitle(extra.getString("prompt_title"));
        builder.setSubtitle(extra.getString("prompt_subtitle"));
        builder.setDescription(extra.getString("prompt_description"));
        String promptButton = extra.getString("prompt_button");
        if (TextUtils.isEmpty(promptButton)) {
            promptButton = context.getString(android.R.string.cancel);
        }
        builder.setNegativeButton(promptButton, context.getMainExecutor(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.onAuthenticationError(ConstantsSoter.ERR_NEGATIVE_BUTTON, "click negative button");
            }
        });
        builder.build().authenticate((android.os.CancellationSignal) cancel, context.getMainExecutor(), wrapCallback2(callback));
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

    private static CryptoObject unwrapCryptoObject(BiometricPrompt.CryptoObject cryptoObject) {
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

    private static BiometricPrompt.AuthenticationCallback wrapCallback2(final AuthenticationCallback callback) {
        return new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                callback.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                callback.onAuthenticationHelp(helpCode, helpString);
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                callback.onAuthenticationSucceeded(new AuthenticationResultInternal(unwrapCryptoObject(result.getCryptoObject())));
            }

            @Override
            public void onAuthenticationFailed() {
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

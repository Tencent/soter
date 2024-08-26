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
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;

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
final class FaceidManagerProxy {
    private static final String TAG = "Soter.FaceidManagerProxy";

    public static final String FACEMANAGER_FACTORY_CLASS_NAME = "com.tencent.soter.core.biometric.SoterFaceManagerFactory";

    private static FaceManager getFaceManager(Context ctx) {
        FaceManager faceManager = null;
        try {
            faceManager = (FaceManager)Class.forName(FACEMANAGER_FACTORY_CLASS_NAME).getDeclaredMethod("getFaceManager", new Class[]{Context.class}).invoke((Object) null, new Object[]{ctx});
        } catch (Exception e) {
            SLogger.e(TAG, "soter: FaceManager init failed, maybe not support." + e.toString());
            e.printStackTrace();
        }
        return faceManager;
    }

    public static boolean hasEnrolledFaceids(Context context) {
        try {
            FaceManager mgr = getFaceManager(context);
            if(mgr != null) {
                return mgr.hasEnrolledFaces();
            } else {
                SLogger.e(TAG, "soter: facemanager is null in hasEnrolledBiometric! Should never happen");
                return false;
            }
        } catch (Exception e) {
            SLogger.e(TAG, "soter: triggered SecurityException in hasEnrolledBiometric! Make sure you declared USE_FACEID in AndroidManifest.xml");
            return false;
        }

    }


    /**
     * Check whether there's hardware detected in the device
     * @param context The context
     * @return true if there's hardware detected
     */
    public static boolean isHardwareDetected(Context context) {
        try {
            FaceManager mgr = getFaceManager(context);
            if (mgr != null) {
                boolean ret = mgr.isHardwareDetected();
                if (!ret) {
                    SReporter.reportError(ERR_ANDROID_HAREWARE_NOT_SUPPORT, "FaceManager.isHardwareDetected return false");
                }
                return ret;
            } else {
                SLogger.e(TAG, "soter: facemanager is null in isHardwareDetected! Should never happen");
                return false;
            }
        } catch (Exception e) {
            SLogger.e(TAG, "soter: triggered SecurityException in isHardwareDetected! Make sure you declared USE_FACEID in AndroidManifest.xml");
            return false;
        }
    }

    public static void authenticate(Context context, CryptoObject crypto, int flags, Object cancel,
                                    AuthenticationCallback callback, Handler handler) {
        try {
            FaceManager mgr = getFaceManager(context);
            if(mgr != null) {
                mgr.authenticate(wrapCryptoObject(crypto),
                        (android.os.CancellationSignal) cancel, flags,
                        wrapCallback(callback), handler);
            } else {
                SLogger.e(TAG, "soter: facemanager is null in authenticate! Should never happen");
            }
        } catch (Exception e) {
            SLogger.e(TAG, "soter: triggered SecurityException in authenticate! Make sure you declared USE_FACEID in AndroidManifest.xml");
        }
    }

    private static FaceManager.CryptoObject wrapCryptoObject(CryptoObject cryptoObject) {
        if (cryptoObject == null) {
            return null;
        } else if (cryptoObject.getCipher() != null) {
            return new FaceManager.CryptoObject(cryptoObject.getCipher());
        } else if (cryptoObject.getSignature() != null) {
            return new FaceManager.CryptoObject(cryptoObject.getSignature());
        } else if (cryptoObject.getMac() != null) {
            return new FaceManager.CryptoObject(cryptoObject.getMac());
        } else {
            return null;
        }
    }

    private static CryptoObject unwrapCryptoObject(FaceManager.CryptoObject cryptoObject) {
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

    private static FaceManager.AuthenticationCallback wrapCallback(
            final AuthenticationCallback callback) {
        return new FaceManager.AuthenticationCallback() {
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
            public void onAuthenticationSucceeded(FaceManager.AuthenticationResult result) {
                SLogger.d(TAG, "hy: lowest level return onAuthenticationSucceeded");
                callback.onAuthenticationSucceeded(new AuthenticationResult(
                        unwrapCryptoObject(result.getCryptoObject())));
            }

            @Override
            public void onAuthenticationFailed() {
                SLogger.d(TAG, "hy: lowest level return onAuthenticationFailed");
                callback.onAuthenticationFailed();
            }
        };
    }

    public static String getBiometricName(Context context) {
        try {
            FaceManager mgr = getFaceManager(context);
            if(mgr != null) {
                return mgr.getBiometricName(context);
            } else {
                SLogger.e(TAG, "soter: faceid manager is null! no biometric name returned.");
                return null;
            }
        } catch (Exception e) {
            SLogger.e(TAG, "soter: triggered SecurityException in getBiometricName! Make sure you declared USE_FACEID in AndroidManifest.xml");
            return null;
        }
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

    public static final class AuthenticationResult {
        private CryptoObject mCryptoObject;

        public AuthenticationResult(CryptoObject crypto) {
            mCryptoObject = crypto;
        }

        public CryptoObject getCryptoObject() { return mCryptoObject; }
    }

    public static abstract class AuthenticationCallback {

        public void onAuthenticationError(int errMsgId, CharSequence errString) { }
        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) { }
        public void onAuthenticationSucceeded(AuthenticationResult result) { }
        public void onAuthenticationFailed() { }
    }
}

package com.tencent.soter.core.biometric;

import android.content.Context;
import android.os.CancellationSignal;
import android.os.Handler;

import java.security.Signature;

import javax.crypto.Cipher;
import javax.crypto.Mac;

/**
 * FaceManager used to unify underlying implementation of face recognition service for SOTER,
 * you can also reference {@link android.hardware.fingerprint.FingerprintManager}.
 *
 * peteryan
 */
public abstract class FaceManager {
    private static final String TAG = "Soter.FaceManager";

    /**Error state returned when the current request has been running too long.*/
    public static final int FACE_ERROR_TIMEOUT = 3;

    /**The operation was canceled. SOTER treat it as onAuthenticationCanceled*/
    public static final int FACE_ERROR_CANCELED = 5;

    /**The operation was canceled because the API is locked out due to too many attempts.*/
    public static final int FACE_ERROR_LOCKOUT = 7;



    public static final int FACE_ERROR_VENDOR_BASE = 1000;
    /**The image acquired was good.*/
    public static final int FACE_ACQUIRED_GOOD = 1101;
    /**The face image was too noisy to process due to a detected condition (i.e. dry skin) or a possibly dirty sensor.*/
    public static final int FACE_ACQUIRED_INSUFFICIENT = 1102;
    /**The face image was too noisy due to suspected or detected dirt on the sensor. */
    public static final int FACE_ACQUIRED_IMAGER_DIRTY = 1103;
    public static final int FACE_ACQUIRED_TOO_SLOW = 1104;
    public static final int FACE_ACQUIRED_TOO_FAST = 1105;
    /**The face image was too close to the phone screen, need to take it off the screen to get a bigger face rect */
    public static final int FACE_ACQUIRED_FAR_FACE = 1106;
    /**The face image was too far from the phone screen, need to take it more close to the screen to get a bigger face rect*/
    public static final int FACE_ACQUIRED_NEAR_FACE = 1107;
    public static final int FACE_ACQUIRED_NO_FACE = 1108;
    public static final int FACE_ACQUIRED_SHIFTING = 1109;
    public static final int FACE_ACQUIRED_DARK = 1110;
    public static final int FACE_ACQUIRED_HACKER = 1111;
    public static final int FACE_ACQUIRED_BRIGHT = 1112;
    public static final int FACE_ACQUIRED_LEFT = 1113;
    public static final int FACE_ACQUIRED_RIGHT = 1114;
    public static final int FACE_ACQUIRED_UP = 1115;
    public static final int FACE_ACQUIRED_DOWN = 1116;
    public static final int FACE_WITH_EYES_CLOSED = 1117;
    public static final int FACE_ACQUIRED_NO_FOCUS = 1118;
    public static final int FACE_ACQUIRED_MOUTH_OCCLUSION = 1119;
    /**used in enroll, if phone is rotate and enroll may cause this message.*/
    public static final int FACE_ACQUIRED_NOT_FRONTAL_FACE = 1120;
    public static final int FACE_ACQUIRED_NOSE_OCCLUSION = 1121;
    /**nose occlusion.*/
    public static final int FACE_ACQUIRED_MULTI_FACE = 1122;
    public static final int FACE_ERROR_CAMERA_UNAVAILABLE = 1001;
    public static final int FACE_ERROR_HW_UNAVAILABLE = 1002;

    public static final class CryptoObject {

        public CryptoObject(Signature signature) {
            mCrypto = signature;
        }

        public CryptoObject(Cipher cipher) {
            mCrypto = cipher;
        }

        public CryptoObject(Mac mac) {
            mCrypto = mac;
        }

        public Signature getSignature() {
            return mCrypto instanceof Signature ? (Signature) mCrypto : null;
        }

        public Cipher getCipher() {
            return mCrypto instanceof Cipher ? (Cipher) mCrypto : null;
        }

        public Mac getMac() {
            return mCrypto instanceof Mac ? (Mac) mCrypto : null;
        }

        private final Object mCrypto;
    };


    public static class AuthenticationResult {

        private CryptoObject mCryptoObject;

        public AuthenticationResult(CryptoObject crypto) {
            mCryptoObject = crypto;
        }

        public CryptoObject getCryptoObject() { return mCryptoObject; }

    };


    public static abstract class AuthenticationCallback {
        /**
         * Called when an unrecoverable error has been encountered and the operation is complete.
         * No further callbacks will be made on this object.
         * @param errorCode An integer identifying the error message
         * @param errString A human-readable error string that can be shown in UI
         */
        public void onAuthenticationError(int errorCode, CharSequence errString) { }

        /**
         * Called when a recoverable error has been encountered during authentication. The help
         * string is provided to give the user guidance for what went wrong, such as
         * "Sensor dirty, please clean it."
         * @param helpCode An integer identifying the error message
         * @param helpString A human-readable string that can be shown in UI
         */
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) { }

        /**
         * Called when a face is recognized and the operation is complete.
         * No further callbacks will be made on this object.
         * @param result An object containing authentication-related data
         */
        public void onAuthenticationSucceeded(AuthenticationResult result) { }

        /**
         * Called when a face is valid but not recognized and the operation is complete.
         * No further callbacks will be made on this object.
         */
        public void onAuthenticationFailed() { }

    };

    /**The faceid alias in system*/
    public abstract String getBiometricName(Context context);

    public abstract boolean hasEnrolledFaces();

    public abstract boolean isHardwareDetected();

    public abstract void authenticate(CryptoObject crypto,
                             CancellationSignal cancel,
                             int flags,
                             AuthenticationCallback callback,
                             Handler handler);
}




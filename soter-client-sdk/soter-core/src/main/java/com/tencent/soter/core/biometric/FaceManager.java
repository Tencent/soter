package com.tencent.soter.core.biometric;

import android.content.Context;
import android.os.CancellationSignal;
import android.os.Handler;

import java.security.Signature;

import javax.crypto.Cipher;
import javax.crypto.Mac;


public abstract class FaceManager {
    private static final String TAG = "FaceManager";
    private static final boolean DEBUG = true;

    private static final int MSG_ENROLL_RESULT = 100;
    private static final int MSG_ACQUIRED = 101;
    private static final int MSG_AUTHENTICATION_SUCCEEDED = 102;
    private static final int MSG_AUTHENTICATION_FAILED = 103;
    private static final int MSG_ERROR = 104;
    private static final int MSG_REMOVED = 105;

    /**The hardware is unavailable. Try again later.*/
    public static final int ERROR_HW_UNAVAILABLE = 1;

    /**Error state returned when the sensor was unable to process the current image.*/
    public static final int ERROR_UNABLE_TO_PROCESS = 2;

    /**Error state returned when the current request has been running too long.*/
    public static final int ERROR_TIMEOUT = 3;

    /**
     * The operation was canceled because the sensor is unavailable. For example,
     * this may happen when the user is switched, the device is locked or another pending operation
     * prevents or disables it.
     */
    public static final int ERROR_CANCELED = 5;

    /**The operation was canceled because the API is locked out due to too many attempts.*/
    public static final int ERROR_LOCKOUT = 7;

    /**
     * Hardware vendors may extend this list if there are conditions that do not fall under one of
     * the above categories. Vendors are responsible for providing error strings for these errors.
     */
    public static final int ERROR_VENDOR_BASE = 1000;
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
         * Called when a fingerprint is recognized.
         * @param result An object containing authentication-related data
         */
        public void onAuthenticationSucceeded(AuthenticationResult result) { }

        /**
         * Called when a fingerprint is valid but not recognized.
         */
        public void onAuthenticationFailed() { }

    };


    public abstract boolean hasEnrolledFaces();


    public abstract boolean isHardwareDetected();

    public abstract void authenticate(CryptoObject crypto,
                             CancellationSignal cancel,
                             int flags,
                             AuthenticationCallback callback,
                             Handler handler);

    /**
     * The image acquired was good.
     */
    public static final int FACE_ACQUIRED_GOOD = 1101;

    /**
     * The face image was too noisy to process due to a detected condition (i.e. dry skin) or
     * a possibly dirty sensor (See {@link #FACE_ACQUIRED_IMAGER_DIRTY}).
     */
    public static final int FACE_ACQUIRED_INSUFFICIENT = 1102;

    /**
     * The face image was too noisy due to suspected or detected dirt on the sensor.
     * For example, it's reasonable return this after multiple
     * {@link #FACE_ACQUIRED_INSUFFICIENT} or actual detection of dirt on the sensor
     * (stuck pixels, swaths, etc.). The user is expected to take action to clean the sensor
     * when this is returned.
     */
    public static final int FACE_ACQUIRED_IMAGER_DIRTY = 1103;

    /**
     * The face image was unreadable due to lack of motion. This is most appropriate for
     * linear array sensors that require a swipe motion.
     */
    public static final int FACE_ACQUIRED_TOO_SLOW = 1104;

    /**
     * The face image was incomplete due to quick motion. While mostly appropriate for
     * linear array sensors,  this could also happen if the face was moved during acquisition.
     * The user should be asked to move the face slower (linear) or leave the face on the sensor
     * longer.
     */
    public static final int FACE_ACQUIRED_TOO_FAST = 1105;

    /**
     * The face image was too close to the phone screen, need to take it off the screen to get a
     * bigger face rect
     */
    public static final int FACE_ACQUIRED_FAR_FACE = 1106;

    /**
     * The face image was too far from the phone screen, need to take it more close to the screen
     * to get a bigger face rect
     */
    public static final int FACE_ACQUIRED_NEAR_FACE = 1107;

    public static final int FACE_ACQUIRED_NO_FACE = 1108;
    public static final int FACE_ACQUIRED_SHIFTING = 1109;
    public static final int FACE_ACQUIRED_DARK = 1110;
    public static final int FACE_ACQUIRED_HACKER = 1111;
    public static final int FACE_ACQUIRED_BRIGHT = 1112;
    //for face angle
    public static final int FACE_ACQUIRED_LEFT = 1113;
    public static final int FACE_ACQUIRED_RIGHT = 1114;
    public static final int FACE_ACQUIRED_UP = 1115;
    public static final int FACE_ACQUIRED_DOWN = 1116;
    public static final int FACE_WITH_EYES_CLOSED = 1117;
    public static final int FACE_ACQUIRED_NO_FOCUS = 1118;
    public static final int FACE_ACQUIRED_MOUTH_OCCLUSION = 1119;
    public static final int FACE_ACQUIRED_NOT_FRONTAL_FACE = 1120; /* used in enroll, if phone is rotate and enroll may cause this message*/
    public static final int FACE_ACQUIRED_NOSE_OCCLUSION = 1121; /* nose occlusion. */
    public static final int FACE_ACQUIRED_MULTI_FACE = 1122;
}




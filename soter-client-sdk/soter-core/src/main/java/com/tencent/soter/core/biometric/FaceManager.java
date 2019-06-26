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

    /**The hardware is unavailable. Try again later.*/
    public static final int FACE_ERROR_HW_UNAVAILABLE = 1;

    /**Error state returned when the sensor was unable to process the current image.*/
    public static final int  FACE_ERROR_UNABLE_TO_PROCESS = 2;

    /**Error state returned when the current request has been running too long.*/
    public static final int  FACE_ERROR_TIMEOUT = 3;

    /**
     * The operation was canceled because the sensor is unavailable. For example,
     * this may happen when the user is switched, the device is locked or another pending operation
     * prevents or disables it.
     */
    public static final int  FACE_ERROR_CANCELED = 5;

    /**The operation was canceled because the API is locked out due to too many attempts.*/
    public static final int  FACE_ERROR_LOCKOUT = 7;

    /**Error state returned when the camera was unable to process the current image.*/
    public static final int  FACE_ERROR_CAMERA_UNAVAILABLE = 8;

    /**
     * Hardware vendors may extend this list if there are conditions that do not fall under one of
     * the above categories. Vendors are responsible for providing error strings for these errors.
     */
    public static final int  FACE_ERROR_VENDOR_BASE = 1000;

    //The following error statuses will be notified to onAuthenticationHelp, they are recoverable error.
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

    /**
     * There is no face in the image
     */
    public static final int FACE_ACQUIRED_NO_FACE = 1108;

    /**
     * Face needs to shift to make a whole face scan. (not use in 3D)
     */
    public static final int FACE_ACQUIRED_SHIFTING = 1109;

    /**
     * The light is too bright, needs darker
     */
    public static final int FACE_ACQUIRED_DARK = 1110;

    /**
     * The image is regarded as a hacker attack
     */
    public static final int FACE_ACQUIRED_HACKER = 1111;

    /**
     * The light is too dim, needs brighter
     */
    public static final int FACE_ACQUIRED_BRIGHT = 1112;

    //for face angle
    /**
     * Should turn the face to left
     */
    public static final int FACE_ACQUIRED_LEFT = 1113;

    /**
     * Should turn the face to right
     */
    public static final int FACE_ACQUIRED_RIGHT = 1114;

    /**
     * Should lift the face
     */
    public static final int FACE_ACQUIRED_UP = 1115;

    /**
     * Should lower the face
     */
    public static final int FACE_ACQUIRED_DOWN = 1116;

    /**
     * Eyes are closed in the image.
     */
    public static final int FACE_WITH_EYES_CLOSED = 1117;

    /**
     * The face has no focus on the phone.
     */
    public static final int FACE_ACQUIRED_NO_FOCUS = 1118;

    /**
     * The mouth is covered
     */
    public static final int FACE_ACQUIRED_MOUTH_OCCLUSION = 1119;

    /**
     * The nose is covered
     */
    public static final int FACE_ACQUIRED_NOSE_OCCLUSION = 1120; /* nose occlusion. */

    /**
     * There are more than one face in the image
     */
    public static final int FACE_ACQUIRED_MULTI_FACE = 1121;
    ///

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




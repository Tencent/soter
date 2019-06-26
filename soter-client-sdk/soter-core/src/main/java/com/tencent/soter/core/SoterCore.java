/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.soter.core;

import android.content.Context;
import android.util.Base64;

import com.tencent.soter.core.biometric.BiometricManagerCompat;
import com.tencent.soter.core.fingerprint.SoterAntiBruteForceStrategy;
import com.tencent.soter.core.model.SLogger;
import com.tencent.soter.core.model.SoterCoreResult;
import com.tencent.soter.core.model.SoterCoreUtil;
import com.tencent.soter.core.model.SoterDelegate;
import com.tencent.soter.core.model.SoterErrCode;
import com.tencent.soter.core.model.SoterPubKeyModel;
import com.tencent.soter.core.model.SoterSignatureResult;
import com.tencent.soter.core.fingerprint.FingerprintManagerCompat;
import com.tencent.soter.core.model.ConstantsSoter;
import com.tencent.soter.core.sotercore.CertSoterCore;
import com.tencent.soter.core.sotercore.SoterCoreBase;
import com.tencent.soter.core.sotercore.SoterCoreBeforeTreble;
import com.tencent.soter.core.sotercore.SoterCoreTreble;
import com.tencent.soter.soterserver.SoterSessionResult;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

/**
 * The SOTER Core APIs for developer to handle keys and other basic stuff. Do not change this file because there're many magic codes in it.
 */
@SuppressWarnings("unused")
public class SoterCore implements ConstantsSoter, SoterErrCode {
    private static final String TAG = "Soter.SoterCore";
    public static final int IS_NOT_TREBLE = 0;
    public static final int IS_TREBLE = 1;

    private static boolean isAlreadyCheckedSetUp = false;
    private static SoterCoreBase IMPL;

    static {
        SLogger.i(TAG,"soter: SoterCore is call static block to init SoterCore IMPL");
        IMPL = getProviderSoterCore();
        SLogger.i(TAG,"soter: SoterCore is call static block to init SoterCore IMPL, IMPL is null[%b]", (IMPL == null) );
    }


    public static void setUp() {
        SoterCoreBeforeTreble.setUp();
    }

    public static void tryToInitSoterTreble(Context context) {
        if(IMPL == null ){
            SLogger.i(TAG,"soter: SoterCore IMPL is null then call tryToInitSoterTreble to init");
            IMPL = new SoterCoreTreble();
            if(!IMPL.initSoter(context)){
                IMPL = null;
                SLogger.i(TAG,"soter: SoterCore IMPL is null after call tryToInitSoterTreble to init");
            }
        }
    }

    public static void tryToInitSoterBeforeTreble() {
        if(IMPL == null ){
            SLogger.i(TAG,"soter: SoterCore IMPL is null then call getProviderSoterCore to init");
            IMPL = getProviderSoterCore();
            SLogger.i(TAG,"soter: SoterCore IMPL is null[%b], after call getProviderSoterCore to init", (IMPL == null) );
        }
    }



    public static int getSoterCoreType(){
        if (IMPL == null){
            return IS_NOT_TREBLE;
        }

        if(IMPL instanceof SoterCoreTreble){
            SLogger.d(TAG, "getSoterCoreType is TREBLE");
            return IS_TREBLE;
        }

        SLogger.d(TAG, "getSoterCoreType is not TREBLE");
        return IS_NOT_TREBLE;
    }


    public static SoterCoreBase getProviderSoterCore(){
        SoterCoreBeforeTreble.setUp();
        if(SoterDelegate.isTriggeredOOM()) {
            return null;
        }
        Provider[] providers = Security.getProviders();
        if (providers == null) {
            return null;
        }
        for (Provider provider : providers) {
            String providerName = provider.getName();
            if (providerName != null && providerName.startsWith(SoterCore.SOTER_PROVIDER_NAME)) {
                if(providerName.split("\\.").length > 1){
                    return new CertSoterCore(providerName);
                }
                return new SoterCoreBeforeTreble(providerName);
            }
        }
        return null;
    }


    /**
     * Check whether this device supports SOTER by checking native interfaces. Remind that you should check the server side as well,
     * instead of trust the return value of this method only
     * @return Whether this device supports SOTER by it's native check result.
     */
    public static boolean isNativeSupportSoter() {
        if (IMPL == null){
            SLogger.e(TAG, "soter: isNativeSupportSoter IMPL is null, not support soter");
            return false;
        }
        boolean isNativeSupportSoter = IMPL.isNativeSupportSoter();
        SLogger.e(TAG, "soter: isNativeSupportSoter return["+isNativeSupportSoter+"]");
        return isNativeSupportSoter;
    }

    /**
     * Generate App Secure Key. Remind not to call it in UI thread
     * @return The result of generating process
     */
    public static SoterCoreResult generateAppGlobalSecureKey() {
        if (IMPL == null){
            SLogger.e(TAG, "soter: generateAppGlobalSecureKey IMPL is null, not support soter");
            return new SoterCoreResult(ERR_SOTER_NOT_SUPPORTED);
        }
        return IMPL.generateAppGlobalSecureKey();
    }

    /**
     * Delete the App Secure Key. Remind that once removed, this key can never be retrieved any more.
     * @return true if you delete the App Secure Key, false otherwise
     */
    public static SoterCoreResult removeAppGlobalSecureKey() {
        if (IMPL == null){
            SLogger.e(TAG, "soter: removeAppGlobalSecureKey IMPL is null, not support soter");
            return new SoterCoreResult(ERR_SOTER_NOT_SUPPORTED);
        }
        return IMPL.removeAppGlobalSecureKey();
    }

    /**
     * Check if there's already a pair of App Secure Key of this application.
     * @return true if there's already App Secure Key
     */
    public static boolean hasAppGlobalSecureKey() {
        if (IMPL == null){
            SLogger.e(TAG, "soter: hasAppGlobalSecureKey IMPL is null, not support soter");
            return false;
        }
        return IMPL.hasAppGlobalSecureKey();
    }

    /**
     * Check if the App Secure Key is valid. Add it because some vivo devices will return true in hasAppGlobalSecureKey
     * but actual model is null.
     * @return true if the App Secure Key is valid
     */
    public static boolean isAppGlobalSecureKeyValid() {
        if (IMPL == null){
            SLogger.e(TAG, "soter: isAppGlobalSecureKeyValid IMPL is null, not support soter");
            return false;
        }
        return IMPL.isAppGlobalSecureKeyValid();
    }

    /**
     * To retrieve the App Secure Key model from device.
     * @return The App Secure Key model.
     */
    public static SoterPubKeyModel getAppGlobalSecureKeyModel() {
        if (IMPL == null){
            SLogger.e(TAG, "soter: getAppGlobalSecureKeyModel IMPL is null, not support soter");
            return null;
        }
        return IMPL.getAppGlobalSecureKeyModel();

    }

    /**
     * Generate Auth Key. Remind not to call it in UI thread
     * @param  authKeyName The alias of the Auth Key to be generated. Keep in mind it should be unique in each business scene, or the key would be overwritten
     * @return The result of key generating process.
     */
    public static SoterCoreResult generateAuthKey(String authKeyName) {
        if (IMPL == null){
            SLogger.e(TAG, "soter: generateAuthKey IMPL is null, not support soter");
            return new SoterCoreResult(ERR_SOTER_NOT_SUPPORTED);
        }
        return IMPL.generateAuthKey(authKeyName);

    }

    /**
     * Delete the Auth Key. Remind that once removed, this key can never be retrieved any more.
     * @param authKeyName The alias of the key to be deleted
     * @param isAutoDeleteASK true if you want to remove the App Secure Key at the same time
     * @return true if the key deleting process is successful
     */
    public static SoterCoreResult removeAuthKey(String authKeyName, boolean isAutoDeleteASK) {
        if (IMPL == null){
            SLogger.e(TAG, "soter: removeAuthKey IMPL is null, not support soter");
            return new SoterCoreResult(ERR_SOTER_NOT_SUPPORTED);
        }
        return IMPL.removeAuthKey(authKeyName, isAutoDeleteASK);
    }

    /**
     * If there's already a pair of auth key by the given key alias
     * @param authKeyName The key alias to check
     * @return true if there's already a pair of auth key
     */
    public static boolean hasAuthKey(String authKeyName) {
        if (IMPL == null){
            SLogger.e(TAG, "soter: hasAuthKey IMPL is null, not support soter");
            return false;
        }
        return IMPL.hasAuthKey(authKeyName);

    }

    /**
     * Check if the Auth Key is valid or not. The check is necessary because from Android M, the Auth Key would be permanently invalid once
     * user enrolled a new fingerprint in the device.
     * @param authKeyName The alias of the auth key to check
     * @param autoDelIfNotValid If the auth key should be deleted when find it invalid
     * @return If the key is valid
     */
    public static boolean isAuthKeyValid(String authKeyName, @SuppressWarnings("SameParameterValue") boolean autoDelIfNotValid) {

        if (IMPL == null){
            SLogger.e(TAG, "soter: isAuthKeyValid IMPL is null, not support soter");
            return false;
        }
        return IMPL.isAuthKeyValid(authKeyName,autoDelIfNotValid);

    }


    /**
     * To retrieve the App Secure Key model from device.
     * @param authKeyName he alias of the auth key
     * @return The public key model of the Auth Key
     */
    public static SoterPubKeyModel getAuthKeyModel(String authKeyName) {

        if (IMPL == null){
            SLogger.e(TAG, "soter: getAuthKeyModel IMPL is null, not support soter");
            return null;
        }
        return IMPL.getAuthKeyModel(authKeyName);

    }

    /**
     * Prepare the {@link Signature} object before authenticating. You should keep the object for later use after user authenticated.
     * More over, this method is used for checking whether the auth key is valid or not.
     * @param useKeyAlias The Auth Key alias of which key you want to prepare
     * @return The prepared Signature. It would be null if the prepare process fails, or the Auth Key is already invalid.
     */
    public static Signature getAuthInitAndSign(String useKeyAlias) {

        if (IMPL == null){
            SLogger.e(TAG, "soter: getAuthInitAndSign IMPL is null, not support soter");
            return null;
        }
        return IMPL.getAuthInitAndSign(useKeyAlias);

    }

    private static Signature initAuthKeySignature(String useKeyAlias) throws InvalidKeyException, NoSuchProviderException,
            NoSuchAlgorithmException,
            KeyStoreException,
            IOException,
            CertificateException,
            UnrecoverableEntryException {

        if (IMPL == null){
            SLogger.e(TAG, "soter: initAuthKeySignature IMPL is null, not support soter");
            return null;
        }
        return IMPL.initAuthKeySignature(useKeyAlias);

    }

    public static SoterSessionResult initSigh(String mAuthKeyName, String mChallenge) {

        if (IMPL == null){
            SLogger.e(TAG, "soter: initSigh IMPL is null, not support soter");
            return null;
        }
        return IMPL.initSigh(mAuthKeyName, mChallenge);
    }

    public static byte[] finishSign(long session) throws Exception {

        if (IMPL == null){
            SLogger.e(TAG, "soter: finishSign IMPL is null, not support soter");
            return new byte[0];
        }
        return IMPL.finishSign(session);
    }


    /**
     * Convert the byte array got from {@link Signature#sign()} to {@link SoterSignatureResult} model.
     * @param origin The return value of {@link Signature#sign()}
     * @return The signature model
     */
    public static SoterSignatureResult convertFromBytesToSignatureResult(byte[] origin) {

        if (SoterCoreUtil.isNullOrNil(origin)) {
            SLogger.e(TAG, "origin is null or nil. abort");
            return null;
        }
        if (origin.length < RAW_LENGTH_PREFIX) {
            SLogger.e(TAG, "soter: length not correct 1");
            return null;
        }
        byte[] lengthBytes = new byte[4];
        System.arraycopy(origin, 0, lengthBytes, 0, 4);
        int rawLength = toInt(lengthBytes);
        SLogger.d("Soter", "parsed raw length: " + rawLength);
        if(rawLength > 1024 * 1024) {
            SLogger.e(TAG, "soter: too large signature result!");
            return null;
        }

        byte[] rawJsonBytes = new byte[rawLength];
        if (origin.length <= RAW_LENGTH_PREFIX + rawLength) {
            SLogger.e(TAG, "soter: length not correct 2");
            return null;
        }
        System.arraycopy(origin, RAW_LENGTH_PREFIX, rawJsonBytes, 0, rawLength);
        SoterSignatureResult result = SoterSignatureResult.convertFromJson(new String(rawJsonBytes));
        int signatureLength = origin.length - (RAW_LENGTH_PREFIX + rawLength);
        SLogger.d(TAG, "soter: signature length: " + signatureLength);
        byte[] signature = new byte[signatureLength];
        System.arraycopy(origin, rawLength + RAW_LENGTH_PREFIX, signature, 0, signatureLength);
        if (result != null) {
            result.setSignature(Base64.encodeToString(signature, Base64.NO_WRAP));
        }
        return result;
    }

    private static final int RAW_LENGTH_PREFIX = 4;

    private static int toInt(byte[] bRefArr) {
        int iOutcome = 0;
        byte bLoop;

        for (int i = 0; i < bRefArr.length; i++) {
            bLoop = bRefArr[i];
            iOutcome += (bLoop & 0xFF) << (8 * i);
        }
        return iOutcome;
    }

    // Magic warning. Do not modify anyway
    private static SoterPubKeyModel retrieveJsonFromExportedData(byte[] origin) {
        if (origin == null) {
            SLogger.e(TAG, "soter: raw data is null");
            return null;
        }
        if (origin.length < RAW_LENGTH_PREFIX) {
            SLogger.e(TAG, "soter: raw data length smaller than RAW_LENGTH_PREFIX");
        }
        byte[] lengthBytes = new byte[4];
        System.arraycopy(origin, 0, lengthBytes, 0, 4);
        int rawLength = toInt(lengthBytes);
        SLogger.d(TAG, "soter: parsed raw length: " + rawLength);
        if(rawLength > 1024 * 1024) {
            SLogger.e(TAG, "soter: too large json result!");
            return null;
        }
        byte[] rawJsonBytes = new byte[rawLength];
        if (origin.length <= RAW_LENGTH_PREFIX + rawLength) {
            SLogger.e(TAG, "length not correct 2");
            return null;
        }
        System.arraycopy(origin, RAW_LENGTH_PREFIX, rawJsonBytes, 0, rawLength);


        String jsonStr = new String(rawJsonBytes);
        SLogger.d(TAG, "soter: to convert json: " + jsonStr);
        SoterPubKeyModel model = new SoterPubKeyModel(jsonStr, "");
        int signatureLength = origin.length - (RAW_LENGTH_PREFIX + rawLength);
        SLogger.d(TAG, "soter: signature length: " + signatureLength);
        byte[] signature = new byte[signatureLength];
        System.arraycopy(origin, rawLength + RAW_LENGTH_PREFIX, signature, 0, signatureLength);
        model.setSignature(Base64.encodeToString(signature, Base64.NO_WRAP));
        return model;
    }

    /**
     * Judge whether there's fingerprint sensor in this device
     * @param context The context
     * @return true if there's fingerprint sensor
     */
    @Deprecated
    public static boolean isSupportFingerprint(Context context) {
        boolean isSupportFingerprint = FingerprintManagerCompat.from(context).isHardwareDetected();
        SLogger.e(TAG, "soter: isSupportFingerprint return["+isSupportFingerprint+"]");
        return isSupportFingerprint;
    }

    public static boolean isSupportBiometric(Context context, int biometricType) {
        boolean isSupportBiometric = BiometricManagerCompat.from(context, biometricType).isHardwareDetected();
        SLogger.e(TAG, "soter: isSupportBiometric type["+biometricType+"] return["+isSupportBiometric+"]");
        return isSupportBiometric;
    }

    /**
     * Judge whether there's any fingerprint enrolled in this device
     * @param context The context
     * @return true if there's fingerprint enrolled
     */
    @Deprecated
    public static boolean isSystemHasFingerprint(Context context) {
        return FingerprintManagerCompat.from(context).hasEnrolledFingerprints();
    }

    public static boolean isSystemHasBiometric(Context context, int biometricType){
        return BiometricManagerCompat.from(context, biometricType).hasEnrolledBiometric();
    }

    /**
     * Judge whether current fingerprint is frozen due to too many failure trials. If true, you should not use fingerprint for authentication in current session.
     * @return True if the fingerprint sensor is frozen now.
     */
    @Deprecated
    public static boolean isCurrentFingerprintFrozen(Context context) {
        return !SoterAntiBruteForceStrategy.isCurrentFailTimeAvailable(context) && !SoterAntiBruteForceStrategy.isCurrentTweenTimeAvailable(context);
    }

    public static boolean isCurrentBiometricFrozen(Context context, int biometricType) {
        return !BiometricManagerCompat.from(context, biometricType).isCurrentFailTimeAvailable()
                && !BiometricManagerCompat.from(context, biometricType).isCurrentTweenTimeAvailable(context);
    }

    /**
     * The parameter represents this device software build. It is for remote SOTER support check use.
     * @return The parameter you should put in the request parameter
     */
    public static String generateRemoteCheckRequestParam() {
        StringBuilder key = new StringBuilder();
        key.append("<deviceinfo>");
        key.append("<MANUFACTURER name=\"");
        key.append(android.os.Build.MANUFACTURER);
        key.append("\">");
        key.append("<MODEL name=\"");
        key.append(android.os.Build.MODEL);
        key.append("\">");
        key.append("<VERSION_RELEASE name=\"");
        key.append(android.os.Build.VERSION.RELEASE);
        key.append("\">");
        key.append("<VERSION_INCREMENTAL name=\"");
        key.append(android.os.Build.VERSION.INCREMENTAL);
        key.append("\">");
        key.append("<DISPLAY name=\"");
        key.append(android.os.Build.DISPLAY);
        key.append("\">");
        key.append("</DISPLAY></VERSION_INCREMENTAL></VERSION_RELEASE></MODEL></MANUFACTURER></deviceinfo>");
        SLogger.d(TAG, "soter: getFingerprint  " + key.toString());
        return key.toString();
    }


}

package com.tencent.soter.core.sotercore;

import android.content.Context;
import android.util.Base64;

import com.tencent.soter.core.model.SLogger;
import com.tencent.soter.core.model.SoterCoreResult;
import com.tencent.soter.core.model.SoterPubKeyModel;
import com.tencent.soter.soterserver.SoterSessionResult;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;


/**
 * The Base SOTER Core APIs
 */
public abstract class SoterCoreBase {

    protected static final String TAG = "Soter.SoterCoreBase";

    public abstract boolean initSoter(Context context);

    /**
     * Check whether this device supports SOTER by checking native interfaces. Remind that you should check the server side as well,
     * instead of trust the return value of this method only
     * @return Whether this device supports SOTER by it's native check result.
     */
    public abstract boolean isNativeSupportSoter() ;

    /**
     * Generate App Secure Key. Remind not to call it in UI thread
     * @return The result of generating process
     */
    public abstract SoterCoreResult generateAppGlobalSecureKey();

    /**
     * Delete the App Secure Key. Remind that once removed, this key can never be retrieved any more.
     * @return true if you delete the App Secure Key, false otherwise
     */
    public abstract SoterCoreResult removeAppGlobalSecureKey();

    /**
     * Check if there's already a pair of App Secure Key of this application.
     * @return true if there's already App Secure Key
     */
    public abstract boolean hasAppGlobalSecureKey();

    /**
     * Check if the App Secure Key is valid. Add it because some vivo devices will return true in hasAppGlobalSecureKey
     * but actual model is null.
     * @return true if the App Secure Key is valid
     */
    public abstract boolean isAppGlobalSecureKeyValid();

    /**
     * To retrieve the App Secure Key model from device.
     * @return The App Secure Key model.
     */
    public abstract SoterPubKeyModel getAppGlobalSecureKeyModel();

    /**
     * Generate Auth Key. Remind not to call it in UI thread
     * @param  authKeyName The alias of the Auth Key to be generated. Keep in mind it should be unique in each business scene, or the key would be overwritten
     * @return The result of key generating process.
     */
    public abstract SoterCoreResult generateAuthKey(String authKeyName);

    /**
     * Delete the Auth Key. Remind that once removed, this key can never be retrieved any more.
     * @param authKeyName The alias of the key to be deleted
     * @param isAutoDeleteASK true if you want to remove the App Secure Key at the same time
     * @return true if the key deleting process is successful
     */
    public abstract SoterCoreResult removeAuthKey(String authKeyName, boolean isAutoDeleteASK);

    /**
     * Check if the Auth Key is valid or not. The check is necessary because from Android M, the Auth Key would be permanently invalid once
     * user enrolled a new fingerprint in the device.
     * @param authKeyName The alias of the auth key to check
     * @param autoDelIfNotValid If the auth key should be deleted when find it invalid
     * @return If the key is valid
     */
    public abstract boolean isAuthKeyValid(String authKeyName,  boolean autoDelIfNotValid);

    /**
     * To retrieve the App Secure Key model from device.
     * @param authKeyName he alias of the auth key
     * @return The public key model of the Auth Key
     */
    public abstract SoterPubKeyModel getAuthKeyModel(String authKeyName);

    /**
     * Prepare the {@link Signature} object before authenticating. You should keep the object for later use after user authenticated.
     * More over, this method is used for checking whether the auth key is valid or not.
     * @param useKeyAlias The Auth Key alias of which key you want to prepare
     * @return The prepared Signature. It would be null if the prepare process fails, or the Auth Key is already invalid.
     */
    public abstract Signature getAuthInitAndSign(String useKeyAlias);

    /**
     * If there's already a pair of auth key by the given key alias
     * @param authKeyName The key alias to check
     * @return true if there's already a pair of auth key
     */
    public abstract boolean hasAuthKey(String authKeyName);

    /**
     * init signature task
     * @param kname The key alias to check
     * @param challenge The key challenge
     * @return long session to ensure signature session is begin
     */
    public abstract SoterSessionResult initSigh(String kname, String challenge) ;

    /**
     * finsh signature task
     * @param signSession the long session to finish the signature task
     * @return signResult to finishSign
     */
    public abstract byte[] finishSign(long signSession) throws Exception;

    /**
     * Only in TrebleCore this method can be meaningful
     * @return weather the Soter Service is connected
     */
    public boolean isTrebleServiceConnected() {
        return true;
    }

    public void triggerTrebleServiceConnecting() {

    }

    public void releaseTrebleServiceConnection() {

    }

    public void setTrebleServiceListener(SoterCoreTrebleServiceListener listener) {

    }
    
    /**
     * update system extra param(such as FingerType, FingerIconPosition) by SoterService
     */
    public void updateExtraParam() {
    
    }

    public abstract Signature initAuthKeySignature(String useKeyAlias) throws InvalidKeyException, NoSuchProviderException,
            NoSuchAlgorithmException,
            KeyStoreException,
            IOException,
            CertificateException,
            UnrecoverableEntryException;


    protected static final int RAW_LENGTH_PREFIX = 4;

    protected static int toInt(byte[] bRefArr) {
        int iOutcome = 0;
        byte bLoop;

        for (int i = 0; i < bRefArr.length; i++) {
            bLoop = bRefArr[i];
            iOutcome += (bLoop & 0xFF) << (8 * i);
        }
        return iOutcome;
    }



    // Magic warning. Do not modify anyway
    protected static SoterPubKeyModel retrieveJsonFromExportedData(byte[] origin) {
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
        if (origin.length < RAW_LENGTH_PREFIX + rawLength) {
            SLogger.e(TAG, "length not correct 2");
            return null;
        }
        System.arraycopy(origin, RAW_LENGTH_PREFIX, rawJsonBytes, 0, rawLength);


        String jsonStr = new String(rawJsonBytes);
        SLogger.d(TAG, "soter: to convert json: " + jsonStr);
        SoterPubKeyModel model = new SoterPubKeyModel(jsonStr, "");
        int signatureLength = origin.length - (RAW_LENGTH_PREFIX + rawLength);
        SLogger.d(TAG, "soter: signature length: " + signatureLength);
        if(signatureLength != 0) {
            byte[] signature = new byte[signatureLength];
            System.arraycopy(origin, rawLength + RAW_LENGTH_PREFIX, signature, 0, signatureLength);
            model.setSignature(Base64.encodeToString(signature, Base64.NO_WRAP));
        }
        return model;
    }

}

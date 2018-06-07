package com.tencent.soter.core.sotercore;

import android.content.Context;
import android.util.Base64;

import com.tencent.soter.core.fingerprint.FingerprintManagerCompat;
import com.tencent.soter.core.fingerprint.SoterAntiBruteForceStrategy;
import com.tencent.soter.core.model.SLogger;
import com.tencent.soter.core.model.SoterCoreResult;
import com.tencent.soter.core.model.SoterCoreUtil;
import com.tencent.soter.core.model.SoterPubKeyModel;
import com.tencent.soter.core.model.SoterSignatureResult;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

public abstract class SoterCoreBase {

    protected static final String TAG = "Soter.SoterCoreBase";

    public abstract boolean initSoter(Context context);

    public abstract boolean isNativeSupportSoter() ;

    public abstract SoterCoreResult generateAppGlobalSecureKey();

    public abstract SoterCoreResult removeAppGlobalSecureKey();

    public abstract boolean hasAppGlobalSecureKey();

    public abstract boolean isAppGlobalSecureKeyValid();

    public abstract SoterPubKeyModel getAppGlobalSecureKeyModel();

    public abstract SoterCoreResult generateAuthKey(String authKeyName);

    public abstract SoterCoreResult removeAuthKey(String authKeyName, boolean isAutoDeleteASK);

    public abstract Signature initAuthKeySignature(String useKeyAlias) throws InvalidKeyException, NoSuchProviderException,
            NoSuchAlgorithmException,
            KeyStoreException,
            IOException,
            CertificateException,
            UnrecoverableEntryException;

    public abstract boolean isAuthKeyValid(String authKeyName,  boolean autoDelIfNotValid);

    public abstract SoterPubKeyModel getAuthKeyModel(String authKeyName);

    public abstract Signature getAuthInitAndSign(String useKeyAlias);

    public abstract boolean hasAuthKey(String authKeyName);

    public abstract long initSigh(String kname, String challenge) ;

    public abstract byte[] finishSign(long signSession) throws Exception;


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


}

package com.tencent.soter.core.sotercore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Base64;

import com.tencent.soter.core.fingerprint.FingerprintManagerCompat;
import com.tencent.soter.core.fingerprint.SoterAntiBruteForceStrategy;
import com.tencent.soter.core.keystore.KeyGenParameterSpecCompatBuilder;
import com.tencent.soter.core.keystore.KeyPropertiesCompact;
import com.tencent.soter.core.model.ConstantsSoter;
import com.tencent.soter.core.model.SLogger;
import com.tencent.soter.core.model.SoterCoreData;
import com.tencent.soter.core.model.SoterCoreResult;
import com.tencent.soter.core.model.SoterCoreUtil;
import com.tencent.soter.core.model.SoterDelegate;
import com.tencent.soter.core.model.SoterErrCode;
import com.tencent.soter.core.model.SoterPubKeyModel;
import com.tencent.soter.core.model.SoterSignatureResult;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.AlgorithmParameterSpec;

public class SoterCoreBeforeTreble extends SoterCoreBase implements ConstantsSoter, SoterErrCode {
    public static final String TAG = "Soter.SoterCore";

    private static boolean isAlreadyCheckedSetUp = false;

    private static final String MAGIC_SOTER_PWD = "from_soter_ui";

    /**
     * The prepare work before using SOTER. Be sure to call this method before SOTER operation
     */
    @SuppressLint("PrivateApi")
    public static void setUp() {
        Class<?> clazz;
        try {
            clazz = Class.forName("android.security.keystore.SoterKeyStoreProvider");
            Method method = clazz.getMethod("install");
            method.setAccessible(true);
            method.invoke(null);
        } catch (ClassNotFoundException e) {
            SLogger.i(TAG, "soter: no SoterProvider found");
        } catch (NoSuchMethodException e) {
            SLogger.i(TAG, "soter: function not found");
        } catch (IllegalAccessException e) {
            SLogger.i(TAG, "soter: cannot access");
        } catch (InvocationTargetException e) {
            SLogger.i(TAG, "soter: InvocationTargetException");
        } finally {
            isAlreadyCheckedSetUp = true;
        }
    }

    @Override
    public boolean initSoter(Context context) {
        return false;
    }

    /**
     * Check whether this device supports SOTER by checking native interfaces. Remind that you should check the server side as well,
     * instead of trust the return value of this method only
     * @return Whether this device supports SOTER by it's native check result.
     */
    public boolean isNativeSupportSoter() {
        if(!isAlreadyCheckedSetUp) {
            setUp();
        }
        if(SoterDelegate.isTriggeredOOM()) {
            SLogger.w(TAG, "hy: the device has already triggered OOM. mark as not support");
            return false;
        }
        Provider[] providers = Security.getProviders();
        if (providers == null) {
            SLogger.e(TAG, "soter: no provider supported");
            return false;
        }
        for (Provider provider : providers) {
            if (SOTER_PROVIDER_NAME.equals(provider.getName())) {
                SLogger.i(TAG, "soter: found soter provider");
                return true;
            }
        }
        SLogger.i(TAG, "soter: soter provider not found");
        return false;
    }

    /**
     * Generate App Secure Key. Remind not to call it in UI thread
     * @return The result of generating process
     */
    public SoterCoreResult generateAppGlobalSecureKey() {
        SLogger.i(TAG, "soter: start generate ask");
        SLogger.i(TAG, "soter: start generate ask for test");
        if (isNativeSupportSoter()) {
            try {
                KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
                keyStore.load(null);
                KeyPairGenerator generator = KeyPairGenerator.getInstance(KeyPropertiesCompact.KEY_ALGORITHM_RSA, SOTER_PROVIDER_NAME);
                AlgorithmParameterSpec spec = KeyGenParameterSpecCompatBuilder.
                        newInstance(SoterCoreData.getInstance().getAskName() +
                                ".addcounter.auto_signed_when_get_pubkey_attk", KeyPropertiesCompact.PURPOSE_SIGN).setDigests(KeyPropertiesCompact.DIGEST_SHA256)
                        .setSignaturePaddings(KeyPropertiesCompact.SIGNATURE_PADDING_RSA_PSS).build();
                generator.initialize(spec);
                long currentTicks = SoterCoreUtil.getCurrentTicks();
                generator.generateKeyPair();
                long cost = SoterCoreUtil.ticksToNowInMs(currentTicks);
                SLogger.i(TAG, "soter: generate successfully. cost: %d ms", cost);
                return new SoterCoreResult(ERR_OK);
            } catch (Exception e) {
                SLogger.e(TAG, "soter: generateAppGlobalSecureKey " + e.toString());
                SLogger.printErrStackTrace(TAG, e, "soter: generateAppGlobalSecureKey error");
                return new SoterCoreResult(ERR_ASK_GEN_FAILED, e.toString());
            } catch (OutOfMemoryError oomError) {
                SLogger.printErrStackTrace(TAG, oomError, "soter: out of memory when generate ASK!! maybe no attk inside");
                SoterDelegate.onTriggerOOM();
            }
        } else {
            SLogger.e(TAG, "soter: not support soter");
        }
        return new SoterCoreResult(ERR_SOTER_NOT_SUPPORTED);
    }

    /**
     * Delete the App Secure Key. Remind that once removed, this key can never be retrieved any more.
     * @return true if you delete the App Secure Key, false otherwise
     */
    public SoterCoreResult removeAppGlobalSecureKey() {
        SLogger.i(TAG, "soter: start remove app global secure key");
        if (isNativeSupportSoter()) {
            try {
                KeyStore keyStore = KeyStore.getInstance("SoterKeyStore");
                keyStore.load(null);
                keyStore.deleteEntry(SoterCoreData.getInstance().getAskName());
                return new SoterCoreResult(ERR_OK);
            } catch (Exception e) {
                SLogger.e(TAG, "soter: removeAppGlobalSecureKey " + e.toString());
                return new SoterCoreResult(ERR_REMOVE_ASK, e.toString());
            }
        } else {
            SLogger.e(TAG, "soter: not support soter");
        }
        return new SoterCoreResult(ERR_SOTER_NOT_SUPPORTED);
    }

    /**
     * Check if there's already a pair of App Secure Key of this application.
     * @return true if there's already App Secure Key
     */
    public boolean hasAppGlobalSecureKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            return keyStore.getCertificate(SoterCoreData.getInstance().getAskName()) != null;
        } catch (Exception e) {
            SLogger.e(TAG, "soter: hasAppGlobalSecureKey exception: " + e.toString());
        }
        return false;
    }

    /**
     * Check if the App Secure Key is valid. Add it because some vivo devices will return true in hasAppGlobalSecureKey
     * but actual model is null.
     * @return true if the App Secure Key is valid
     */
    public boolean isAppGlobalSecureKeyValid() {
        return hasAppGlobalSecureKey() && getAppGlobalSecureKeyModel() != null;
    }

    /**
     * To retrieve the App Secure Key model from device.
     * @return The App Secure Key model.
     */
    public SoterPubKeyModel getAppGlobalSecureKeyModel() {
        SLogger.i(TAG, "soter: start get app global secure key pub");
        if (isNativeSupportSoter()) {
            KeyStore keyStore;
            try {
                keyStore = KeyStore.getInstance("SoterKeyStore");
                keyStore.load(null);
                try {
                    Key key = keyStore.getKey(SoterCoreData.getInstance().getAskName(), "from_soter_ui".toCharArray());
                    if (key != null) {
                        return retrieveJsonFromExportedData(key.getEncoded());
                    }
                    SLogger.e(TAG, "soter: key can not be retrieved");
                    return null;
                } catch (ClassCastException e) {
                    SLogger.e(TAG, "soter: cast error: " + e.toString());
                }
                return null;
            } catch (Exception e) {
                SLogger.printErrStackTrace(TAG, e, "soter: error when get ask");
            } catch (OutOfMemoryError oomError) {
                SLogger.printErrStackTrace(TAG, oomError, "soter: out of memory when getting ask!! maybe no attk inside");
                SoterDelegate.onTriggerOOM();
            }
        } else {
            SLogger.e(TAG, "soter: not support soter");
        }
        return null;
    }

    /**
     * Generate Auth Key. Remind not to call it in UI thread
     * @param  authKeyName The alias of the Auth Key to be generated. Keep in mind it should be unique in each business scene, or the key would be overwritten
     * @return The result of key generating process.
     */
    public SoterCoreResult generateAuthKey(String authKeyName) {
        if (SoterCoreUtil.isNullOrNil(authKeyName)) {
            SLogger.e(TAG, "soter: auth key name is null or nil. abort.");
            return new SoterCoreResult(ERR_PARAMERROR, "no authKeyName");
        }
        if (isNativeSupportSoter()) {
            try {
                if (!hasAppGlobalSecureKey()) {
                    return new SoterCoreResult(ERR_ASK_NOT_EXIST, "app secure key not exist");
                }
                KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
                keyStore.load(null);
                KeyPairGenerator generator = KeyPairGenerator.getInstance(KeyPropertiesCompact.KEY_ALGORITHM_RSA, SOTER_PROVIDER_NAME);
                try {
                    AlgorithmParameterSpec spec = KeyGenParameterSpecCompatBuilder.newInstance(authKeyName +
                            String.format(".addcounter.auto_signed_when_get_pubkey(%s).secmsg_and_counter_signed_when_sign", SoterCoreData.getInstance().getAskName()), KeyPropertiesCompact.PURPOSE_SIGN).
                            setDigests(KeyPropertiesCompact.DIGEST_SHA256).setUserAuthenticationRequired(true)
                            .setSignaturePaddings(KeyPropertiesCompact.SIGNATURE_PADDING_RSA_PSS).build();
                    generator.initialize(spec);
                    long currentTicks = SoterCoreUtil.getCurrentTicks();
                    generator.generateKeyPair();
                    long cost = SoterCoreUtil.ticksToNowInMs(currentTicks);
                    SLogger.i(TAG, "soter: generate successfully, cost: %d ms", cost);
                    return new SoterCoreResult(ERR_OK);
                } catch (Exception e) {
                    SLogger.e(TAG, "soter: cause exception. maybe reflection exception: " + e.toString());
                    return new SoterCoreResult(ERR_AUTH_KEY_GEN_FAILED, e.toString());
                }
            } catch (Exception e) {
                SLogger.e(TAG, "soter: generate auth key failed: " + e.toString());
                return new SoterCoreResult(ERR_AUTH_KEY_GEN_FAILED, e.toString());
            } catch (OutOfMemoryError oomError) {
                SLogger.printErrStackTrace(TAG, oomError, "soter: out of memory when generate AuthKey!! maybe no attk inside");
                SoterDelegate.onTriggerOOM();
            }
        } else {
            SLogger.e(TAG, "soter: not support soter");
        }
        return new SoterCoreResult(ERR_SOTER_NOT_SUPPORTED);
    }

    /**
     * Delete the Auth Key. Remind that once removed, this key can never be retrieved any more.
     * @param authKeyName The alias of the key to be deleted
     * @param isAutoDeleteASK true if you want to remove the App Secure Key at the same time
     * @return true if the key deleting process is successful
     */
    public SoterCoreResult removeAuthKey(String authKeyName, boolean isAutoDeleteASK) {
        if (SoterCoreUtil.isNullOrNil(authKeyName)) {
            SLogger.e(TAG, "soter: auth key name is null or nil. abort.");
            return new SoterCoreResult(ERR_PARAMERROR, "no authKeyName");
        }
        SLogger.i(TAG, "soter: start remove key: " + authKeyName);
        if (isNativeSupportSoter()) {
            try {
                KeyStore keyStore = KeyStore.getInstance("SoterKeyStore");
                keyStore.load(null);
                keyStore.deleteEntry(authKeyName);
                if (isAutoDeleteASK) {
                    SLogger.i(TAG, "soter: auto delete ask");
                    if (hasAppGlobalSecureKey()) {
                        removeAppGlobalSecureKey();
                    }
                }
                return new SoterCoreResult(ERR_OK);
            } catch (Exception e) {
                SLogger.e(TAG, "soter: removeAuthKey " + e.toString());
                return new SoterCoreResult(ERR_REMOVE_AUTH_KEY, e.toString());
            }
        } else {
            SLogger.e(TAG, "soter: not support soter");
        }
        return new SoterCoreResult(ERR_SOTER_NOT_SUPPORTED);
    }

    /**
     * If there's already a pair of auth key by the given key alias
     * @param authKeyName The key alias to check
     * @return true if there's already a pair of auth key
     */
    public boolean hasAuthKey(String authKeyName) {
        if (SoterCoreUtil.isNullOrNil(authKeyName)) {
            SLogger.e(TAG, "soter: authkey name not correct");
            return false;
        }
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            return keyStore.getCertificate(authKeyName) != null;
        } catch (Exception e) {
            SLogger.e(TAG, "soter: hasAppGlobalSecureKey exception: " + e.toString());
        }
        return false;
    }

    @Override
    public long initSigh(String kname, String challenge) {
        return 0;
    }

    @Override
    public byte[] finishSign(long signSession) throws Exception {
        return new byte[0];
    }

    /**
     * Check if the Auth Key is valid or not. The check is necessary because from Android M, the Auth Key would be permanently invalid once
     * user enrolled a new fingerprint in the device.
     * @param authKeyName The alias of the auth key to check
     * @param autoDelIfNotValid If the auth key should be deleted when find it invalid
     * @return If the key is valid
     */
    public boolean isAuthKeyValid(String authKeyName, @SuppressWarnings("SameParameterValue") boolean autoDelIfNotValid) {
        SLogger.i(TAG, String.format("soter: checking key valid: auth key name: %s, autoDelIfNotValid: %b ", authKeyName, autoDelIfNotValid));
        if (SoterCoreUtil.isNullOrNil(authKeyName)) {
            SLogger.e(TAG, "soter: checking key valid: authkey name not correct");
            return false;
        }
        try {
            initAuthKeySignature(authKeyName);
            SLogger.i(TAG, "soter: key valid");
            return true;
        } catch (UnrecoverableEntryException | InvalidKeyException e) {
            SLogger.e(TAG, "soter: key invalid.");
            if (autoDelIfNotValid) {
                removeAuthKey(authKeyName, false);
            }
            return false;
        } catch (Exception e) {
            SLogger.e(TAG, "soter: occurs other exceptions: %s", e.toString());
            SLogger.printErrStackTrace(TAG, e, "soter: occurs other exceptions");
            return false;
        } catch (OutOfMemoryError oomError) {
            SLogger.printErrStackTrace(TAG, oomError, "soter: out of memory when isAuthKeyValid!! maybe no attk inside");
            SoterDelegate.onTriggerOOM();
            return false;
        }
    }


    /**
     * To retrieve the App Secure Key model from device.
     * @param authKeyName he alias of the auth key
     * @return The public key model of the Auth Key
     */
    public SoterPubKeyModel getAuthKeyModel(String authKeyName) {
        if (SoterCoreUtil.isNullOrNil(authKeyName)) {
            SLogger.e(TAG, "soter: auth key name is null or nil. abort.");
            return null;
        }
        if (isNativeSupportSoter()) {
            KeyStore keyStore;
            try {
                keyStore = KeyStore.getInstance("SoterKeyStore");
                keyStore.load(null);
                try {
                    Key key = keyStore.getKey(authKeyName, MAGIC_SOTER_PWD.toCharArray());
                    if (key != null) {
                        return retrieveJsonFromExportedData(key.getEncoded());
                    }
                    SLogger.e(TAG, "soter: key can not be retrieved");
                    return null;
                } catch (ClassCastException e) {
                    SLogger.e(TAG, "soter: cast error: " + e.toString());
                }
                return null;
            } catch (Exception e) {
                SLogger.printErrStackTrace(TAG, e, "soter: error in get auth key model");
            } catch (OutOfMemoryError oomError) {
                SLogger.printErrStackTrace(TAG, oomError, "soter: out of memory when getAuthKeyModel!! maybe no attk inside");
                SoterDelegate.onTriggerOOM();
            }
        } else {
            SLogger.e(TAG, "soter: not support soter " + "AndroidKeyStore");
        }
        return null;
    }

    /**
     * Prepare the {@link Signature} object before authenticating. You should keep the object for later use after user authenticated.
     * More over, this method is used for checking whether the auth key is valid or not.
     * @param useKeyAlias The Auth Key alias of which key you want to prepare
     * @return The prepared Signature. It would be null if the prepare process fails, or the Auth Key is already invalid.
     */
    public Signature getAuthInitAndSign(String useKeyAlias) {
        if (SoterCoreUtil.isNullOrNil(useKeyAlias)) {
            SLogger.e(TAG, "soter: auth key name is null or nil. abort.");
            return null;
        }
        if (isNativeSupportSoter()) {
            try {
                return initAuthKeySignature(useKeyAlias);
            } catch (UnrecoverableEntryException | InvalidKeyException e) {
                SLogger.e(TAG, "soter: key invalid. Advice remove the key");
                return null;
            } catch (Exception e) {
                SLogger.e(TAG, "soter: exception when getSignatureResult: " + e.toString());
                SLogger.printErrStackTrace(TAG, e, "soter: exception when getSignatureResult");
                return null;
            } catch (OutOfMemoryError oomError) {
                SLogger.printErrStackTrace(TAG, oomError, "soter: out of memory when getAuthInitAndSign!! maybe no attk inside");
                SoterDelegate.onTriggerOOM();
                return null;
            }
        } else {
            SLogger.e(TAG, "soter: not support soter" + "AndroidKeyStore");
            return null;
        }

    }

    public Signature initAuthKeySignature(String useKeyAlias) throws InvalidKeyException, NoSuchProviderException,
            NoSuchAlgorithmException,
            KeyStoreException,
            IOException,
            CertificateException,
            UnrecoverableEntryException {
        if (SoterCoreUtil.isNullOrNil(useKeyAlias)) {
            SLogger.e(TAG, "soter: auth key name is null or nil. abort.");
            return null;
        }
        final Signature signature = Signature.getInstance("SHA256withRSA/PSS", "AndroidKeyStoreBCWorkaround");
        KeyStore soterKeyStore = KeyStore.getInstance("SoterKeyStore");
        soterKeyStore.load(null);
        KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) soterKeyStore.getEntry(useKeyAlias, null);
        if (entry != null) {
            signature.initSign(entry.getPrivateKey());
            return signature;
        } else {
            SLogger.e(TAG, "soter: entry not exists");
            return null;
        }
    }


}

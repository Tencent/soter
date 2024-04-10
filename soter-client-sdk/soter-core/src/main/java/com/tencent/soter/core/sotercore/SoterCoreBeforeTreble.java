package com.tencent.soter.core.sotercore;

import android.annotation.SuppressLint;
import android.content.Context;

import com.tencent.soter.core.keystore.KeyGenParameterSpecCompatBuilder;
import com.tencent.soter.core.keystore.KeyPropertiesCompact;
import com.tencent.soter.core.model.ConstantsSoter;
import com.tencent.soter.core.model.SLogger;
import com.tencent.soter.core.model.SReporter;
import com.tencent.soter.core.model.SoterCoreData;
import com.tencent.soter.core.model.SoterCoreResult;
import com.tencent.soter.core.model.SoterCoreUtil;
import com.tencent.soter.core.model.SoterDelegate;
import com.tencent.soter.core.model.SoterErrCode;
import com.tencent.soter.core.model.SoterPubKeyModel;
import com.tencent.soter.soterserver.SoterSessionResult;

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


/**
 * The SOTER Core APIs before Treble project
 */
public class SoterCoreBeforeTreble extends SoterCoreBase implements ConstantsSoter, SoterErrCode {

    private static final String TAG = "Soter.SoterCoreBeforeTreble";
    private static final String MAGIC_SOTER_PWD = "from_soter_ui";

    private static boolean isAlreadyCheckedSetUp = false;

    protected String providerName = SOTER_PROVIDER_NAME;

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

    public SoterCoreBeforeTreble(String providerName){
        this.providerName = providerName;
    }


    @Override
    public boolean initSoter(Context context) {
        setUp();
        return true;
    }

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
            String providerName = provider.getName();
            if (providerName != null && providerName.startsWith(SOTER_PROVIDER_NAME)) {
                SLogger.i(TAG, "soter: found soter provider");
                return true;
            }
        }
        SLogger.i(TAG, "soter: soter provider not found");
        return false;
    }


    public SoterCoreResult generateAppGlobalSecureKey() {
        SLogger.i(TAG, "soter: start generate ask");
        if (isNativeSupportSoter()) {
            try {
                KeyStore keyStore = KeyStore.getInstance(providerName);
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
                SLogger.i(TAG, "soter: generate global successfully. cost: %d ms", cost);
                SoterDelegate.reset();
                return new SoterCoreResult(ERR_OK);
            } catch (Exception e) {
                SLogger.e(TAG, "soter: generateAppGlobalSecureKey " + e.toString());
                SLogger.printErrStackTrace(TAG, e, "soter: generateAppGlobalSecureKey error");
                SReporter.reportError(ERR_ANDROID_BEFORE_TREBLE, "BeforeTreble: generateAppGlobalSecureKey.", e);
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

    public SoterCoreResult removeAppGlobalSecureKey() {
        SLogger.i(TAG, "soter: start remove app global secure key");
        if (isNativeSupportSoter()) {
            try {
                KeyStore keyStore = KeyStore.getInstance(providerName);
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


    public boolean hasAppGlobalSecureKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance(providerName);
            keyStore.load(null);
            return keyStore.getCertificate(SoterCoreData.getInstance().getAskName()) != null;
        } catch (Exception e) {
            SLogger.e(TAG, "soter: hasAppGlobalSecureKey exception: " + e.toString());
            SReporter.reportError(ERR_ANDROID_BEFORE_TREBLE, "BeforeTreble: hasAppGlobalSecureKey.", e);
        }
        return false;
    }


    public boolean isAppGlobalSecureKeyValid() {
        return hasAppGlobalSecureKey() && getAppGlobalSecureKeyModel() != null;
    }


    public SoterPubKeyModel getAppGlobalSecureKeyModel() {
        SLogger.i(TAG, "soter: start get app global secure key pub");
        if (isNativeSupportSoter()) {
            KeyStore keyStore;
            try {
                keyStore = KeyStore.getInstance(providerName);
                keyStore.load(null);
                try {
                    Key key = keyStore.getKey(SoterCoreData.getInstance().getAskName(), "from_soter_ui".toCharArray());
                    if (key != null) {
                        SoterDelegate.reset();
                        return retrieveJsonFromExportedData(key.getEncoded());
                    }
                    SLogger.e(TAG, "soter: key can not be retrieved");
                    SReporter.reportError(ERR_ANDROID_BEFORE_TREBLE, "BeforeTreble: getAppGlobalSecureKeyModel. keyStore.getKey is null");
                    return null;
                } catch (ClassCastException e) {
                    SLogger.e(TAG, "soter: cast error: " + e.toString());
                    SReporter.reportError(ERR_ANDROID_BEFORE_TREBLE, "BeforeTreble: getAppGlobalSecureKeyModel.", e);
                }
                return null;
            } catch (Exception e) {
                SLogger.printErrStackTrace(TAG, e, "soter: error when get ask");
                SReporter.reportError(ERR_ANDROID_BEFORE_TREBLE, "BeforeTreble: getAppGlobalSecureKeyModel.", e);
            } catch (OutOfMemoryError oomError) {
                SLogger.printErrStackTrace(TAG, oomError, "soter: out of memory when getting ask!! maybe no attk inside");
                SoterDelegate.onTriggerOOM();
            }
        } else {
            SLogger.e(TAG, "soter: not support soter");
        }
        return null;
    }


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
                KeyStore keyStore = KeyStore.getInstance(providerName);
                keyStore.load(null);
                KeyPairGenerator generator = KeyPairGenerator.getInstance(KeyPropertiesCompact.KEY_ALGORITHM_RSA, providerName);
                try {
                    AlgorithmParameterSpec spec = KeyGenParameterSpecCompatBuilder.newInstance(authKeyName +
                            String.format(".addcounter.auto_signed_when_get_pubkey(%s).secmsg_and_counter_signed_when_sign", SoterCoreData.getInstance().getAskName()), KeyPropertiesCompact.PURPOSE_SIGN).
                            setDigests(KeyPropertiesCompact.DIGEST_SHA256).setUserAuthenticationRequired(true)
                            .setSignaturePaddings(KeyPropertiesCompact.SIGNATURE_PADDING_RSA_PSS).build();
                    generator.initialize(spec);
                    long currentTicks = SoterCoreUtil.getCurrentTicks();
                    generator.generateKeyPair();
                    long cost = SoterCoreUtil.ticksToNowInMs(currentTicks);
                    SLogger.i(TAG, "soter: generate auth successfully, cost: %d ms", cost);
                    SoterDelegate.reset();
                    return new SoterCoreResult(ERR_OK);
                } catch (Exception e) {
                    SLogger.e(TAG, "soter: cause exception. maybe reflection exception: " + e.toString());
                    SReporter.reportError(ERR_ANDROID_BEFORE_TREBLE, "BeforeTreble: generateAuthKey.", e);
                    return new SoterCoreResult(ERR_AUTH_KEY_GEN_FAILED, e.toString());
                }
            } catch (Exception e) {
                SLogger.e(TAG, "soter: generate auth key failed: " + e.toString());
                SReporter.reportError(ERR_ANDROID_BEFORE_TREBLE, "BeforeTreble: generateAuthKey.", e);
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


    public SoterCoreResult removeAuthKey(String authKeyName, boolean isAutoDeleteASK) {
        if (SoterCoreUtil.isNullOrNil(authKeyName)) {
            SLogger.e(TAG, "soter: auth key name is null or nil. abort.");
            return new SoterCoreResult(ERR_PARAMERROR, "no authKeyName");
        }
        SLogger.i(TAG, "soter: start remove key: " + authKeyName);
        if (isNativeSupportSoter()) {
            try {
                KeyStore keyStore = KeyStore.getInstance(providerName);
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


    public boolean hasAuthKey(String authKeyName) {
        if (SoterCoreUtil.isNullOrNil(authKeyName)) {
            SLogger.e(TAG, "soter: authkey name not correct");
            return false;
        }
        try {
            KeyStore keyStore = KeyStore.getInstance(providerName);
            keyStore.load(null);
            return keyStore.getCertificate(authKeyName) != null;
        } catch (Exception e) {
            SLogger.e(TAG, "soter: hasAppGlobalSecureKey exception: " + e.toString());
            SReporter.reportError(ERR_ANDROID_BEFORE_TREBLE, "BeforeTreble: hasAuthKey.", e);
        }
        return false;
    }

    @Override
    public SoterSessionResult initSigh(String kname, String challenge) {
        return null;
    }

    @Override
    public byte[] finishSign(long signSession) throws Exception {
        return new byte[0];
    }


    public boolean isAuthKeyValid(String authKeyName, @SuppressWarnings("SameParameterValue") boolean autoDelIfNotValid) {
        SLogger.i(TAG, String.format("soter: checking key valid: auth key name: %s, autoDelIfNotValid: %b ", authKeyName, autoDelIfNotValid));
        if (SoterCoreUtil.isNullOrNil(authKeyName)) {
            SLogger.e(TAG, "soter: checking key valid: authkey name not correct");
            return false;
        }
        try {
            initAuthKeySignature(authKeyName);
            SLogger.i(TAG, "soter: key valid");
            SoterDelegate.reset();
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


    public SoterPubKeyModel getAuthKeyModel(String authKeyName) {
        if (SoterCoreUtil.isNullOrNil(authKeyName)) {
            SLogger.e(TAG, "soter: auth key name is null or nil. abort.");
            return null;
        }
        if (isNativeSupportSoter()) {
            KeyStore keyStore;
            try {
                keyStore = KeyStore.getInstance(providerName);
                keyStore.load(null);
                try {
                    Key key = keyStore.getKey(authKeyName, MAGIC_SOTER_PWD.toCharArray());
                    SoterDelegate.reset();
                    if (key != null) {
                        return retrieveJsonFromExportedData(key.getEncoded());
                    }
                    SLogger.e(TAG, "soter: key can not be retrieved");
                    SReporter.reportError(ERR_ANDROID_BEFORE_TREBLE, "BeforeTreble: getAuthKeyModel. keyStore.getKey is null");
                    return null;
                } catch (ClassCastException e) {
                    SLogger.e(TAG, "soter: cast error: " + e.toString());
                    SReporter.reportError(ERR_ANDROID_BEFORE_TREBLE, "BeforeTreble: getAuthKeyModel.", e);
                }
                return null;
            } catch (Exception e) {
                SLogger.printErrStackTrace(TAG, e, "soter: error in get auth key model");
                SReporter.reportError(ERR_ANDROID_BEFORE_TREBLE, "BeforeTreble: getAuthKeyModel.", e);
            } catch (OutOfMemoryError oomError) {
                SLogger.printErrStackTrace(TAG, oomError, "soter: out of memory when getAuthKeyModel!! maybe no attk inside");
                SoterDelegate.onTriggerOOM();
            }
        } else {
            SLogger.e(TAG, "soter: not support soter " + providerName);
        }
        return null;
    }


    public Signature getAuthInitAndSign(String useKeyAlias) {
        if (SoterCoreUtil.isNullOrNil(useKeyAlias)) {
            SLogger.e(TAG, "soter: auth key name is null or nil. abort.");
            return null;
        }
        if (isNativeSupportSoter()) {
            try {
                SoterDelegate.reset();
                return initAuthKeySignature(useKeyAlias);
            } catch (UnrecoverableEntryException | InvalidKeyException e) {
                SLogger.e(TAG, "soter: key invalid. Advice remove the key");
                SReporter.reportError(ERR_ANDROID_BEFORE_TREBLE, "BeforeTreble: getAuthInitAndSign.", e);
                return null;
            } catch (Exception e) {
                SLogger.e(TAG, "soter: exception when getSignatureResult: " + e.toString());
                SLogger.printErrStackTrace(TAG, e, "soter: exception when getSignatureResult");
                SReporter.reportError(ERR_ANDROID_BEFORE_TREBLE, "BeforeTreble: getAuthInitAndSign.", e);
                return null;
            } catch (OutOfMemoryError oomError) {
                SLogger.printErrStackTrace(TAG, oomError, "soter: out of memory when getAuthInitAndSign!! maybe no attk inside");
                SoterDelegate.onTriggerOOM();
                return null;
            }
        } else {
            SLogger.e(TAG, "soter: not support soter" + providerName);
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
        KeyStore soterKeyStore = KeyStore.getInstance(providerName);
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

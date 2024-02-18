package com.tencent.soter.core.sotercore;

import com.tencent.soter.core.keystore.KeyGenParameterSpecCompatBuilder;
import com.tencent.soter.core.keystore.KeyPropertiesCompact;
import com.tencent.soter.core.model.SLogger;
import com.tencent.soter.core.model.SReporter;
import com.tencent.soter.core.model.SoterCoreData;
import com.tencent.soter.core.model.SoterCoreResult;
import com.tencent.soter.core.model.SoterCoreUtil;
import com.tencent.soter.core.model.SoterDelegate;
import com.tencent.soter.core.model.SoterPubKeyModel;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.AlgorithmParameterSpec;

public class CertSoterCore extends SoterCoreBeforeTreble {
    private static final String TAG = "Soter.CertSoterCore";

    public CertSoterCore(String providerName){
        super(providerName);
    }

    @Override
    public SoterCoreResult generateAppGlobalSecureKey() {

        SLogger.i(TAG, "soter: start generate ask");
        if (isNativeSupportSoter()) {
            try {
                KeyPairGenerator generator = KeyPairGenerator.getInstance(KeyPropertiesCompact.KEY_ALGORITHM_RSA, providerName);
                int purpose = KeyPropertiesCompact.PURPOSE_SOTER_ATTEST_KEY;
                AlgorithmParameterSpec spec =
                        KeyGenParameterSpecCompatBuilder.newInstance(SoterCoreData.getInstance().getAskName() + ".addcounter.auto_signed_when_get_pubkey_attk", purpose)
                                .setDigests(KeyPropertiesCompact.DIGEST_SHA256)
                                .setSignaturePaddings(KeyPropertiesCompact.SIGNATURE_PADDING_RSA_PSS).build();
                generator.initialize(spec);
                long currentTicks = SoterCoreUtil.getCurrentTicks();
                generator.generateKeyPair();
                long cost = SoterCoreUtil.ticksToNowInMs(currentTicks);
                SLogger.i(TAG, "soter: generate successfully. cost: %d ms", cost);
                SoterDelegate.reset();
                return new SoterCoreResult(ERR_OK);
            } catch (Exception e) {
                SLogger.e(TAG, "soter: generateAppGlobalSecureKey " + e.toString());
                SLogger.printErrStackTrace(TAG, e, "soter: generateAppGlobalSecureKey error");
                SReporter.reportError(ERR_ANDROID_BEFORE_TREBLE, "CertSoter: generateAppGlobalSecureKey.", e);
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

    @Override
    public SoterPubKeyModel getAppGlobalSecureKeyModel() {
        SLogger.i(TAG, "soter: start get app global secure key pub");
        if (isNativeSupportSoter()) {

            KeyStore keyStore;
            try {
                keyStore = KeyStore.getInstance(providerName);
                keyStore.load(null);
                try {
                    Certificate[] certificates = keyStore.getCertificateChain(SoterCoreData.getInstance().getAskName());
                    if (certificates != null) {
                        SoterDelegate.reset();
                        return new SoterPubKeyModel(certificates);
                    }
                    SLogger.e(TAG, "soter: key can not be retrieved");
                    SReporter.reportError(ERR_ANDROID_BEFORE_TREBLE, "CertSoter: getAppGlobalSecureKeyModel. keyStore.getCertificateChain is null");
                    return null;
                } catch (ClassCastException e) {
                    SLogger.e(TAG, "soter: cast error: " + e.toString());
                    SReporter.reportError(ERR_ANDROID_BEFORE_TREBLE, "CertSoter: getAppGlobalSecureKeyModel.", e);
                }
                return null;
            } catch (Exception e) {
                SLogger.printErrStackTrace(TAG, e, "soter: error when get ask");
                SReporter.reportError(ERR_ANDROID_BEFORE_TREBLE, "CertSoter: getAppGlobalSecureKeyModel.", e);
            } catch (OutOfMemoryError oomError) {
                SLogger.printErrStackTrace(TAG, oomError, "soter: out of memory when getting ask!! maybe no attk inside");
                SoterDelegate.onTriggerOOM();
            }

        } else {
            SLogger.e(TAG, "soter: not support soter");
        }
        return null;
    }

    @Override
    public Signature initAuthKeySignature(String useKeyAlias) throws InvalidKeyException,
            NoSuchAlgorithmException,
            KeyStoreException,
            IOException,
            CertificateException,
            UnrecoverableEntryException {
        SLogger.d("Monday", "CertSoterCore initAuthKeySignature");
        if (SoterCoreUtil.isNullOrNil(useKeyAlias)) {
            SLogger.e(TAG, "soter: auth key name is null or nil. abort.");
            return null;
        }
        final Signature signature = Signature.getInstance("SHA256withRSA/PSS");
        KeyStore soterKeyStore = KeyStore.getInstance(providerName);
        soterKeyStore.load(null);
        Key key = soterKeyStore.getKey(useKeyAlias, null);
        if (key != null) {
            signature.initSign((PrivateKey) key);
            return signature;
        } else {
            SLogger.e(TAG, "soter: entry not exists");
            return null;
        }
    }

}

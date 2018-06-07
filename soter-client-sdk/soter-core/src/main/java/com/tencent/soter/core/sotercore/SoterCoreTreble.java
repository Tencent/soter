package com.tencent.soter.core.sotercore;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.qualcomm.qti.soterserver.ISoterService;
import com.qualcomm.qti.soterserver.SoterExportResult;
import com.qualcomm.qti.soterserver.SoterSessionResult;
import com.qualcomm.qti.soterserver.SoterSignResult;
import com.tencent.soter.core.model.ConstantsSoter;
import com.tencent.soter.core.model.SLogger;
import com.tencent.soter.core.model.SoterCoreResult;
import com.tencent.soter.core.model.SoterDelegate;
import com.tencent.soter.core.model.SoterPubKeyModel;
import com.tencent.soter.core.model.SoterErrCode;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.List;


@SuppressWarnings("unused")
public class SoterCoreTreble extends SoterCoreBase implements ConstantsSoter, SoterErrCode{

    public static final String TAG = "Soter.SoterCoreTreble";

    private static boolean isAlreadyCheckedSetUp = false;

    private Context mContext;

    protected ISoterService mSoterService;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(
                ComponentName className, IBinder service) {
            SLogger.i(TAG, "onServiceConnected");
            mSoterService =
                    ISoterService.Stub.asInterface(service);
            SLogger.i(TAG, "Binding is done - Service connected");

        }

        public void onServiceDisconnected(ComponentName className) {
            mSoterService = null;
        }
    };

    @Override
    public boolean initSoter(Context context) {
        if (mSoterService == null) {
            mContext = context;
            bindService(context);
            isAlreadyCheckedSetUp = true;
        }
        return true;
    }

    public void bindService(Context context){
        Intent intent = new Intent();
        intent.setAction("com.qualcomm.qti.soterserver.ISoterService");
        intent.setPackage("com.qualcomm.qti.soterserver");

        context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindService(Context context){
        context.unbindService(mServiceConnection);
    }

    public boolean isNativeSupportSoter() {
        if (!isAlreadyCheckedSetUp || mSoterService == null) {
            SLogger.w(TAG, "cq: mContext is null bind failed");
            if (mContext != null) {
                bindService(mContext);
            } else {
                SLogger.w(TAG, "cq: mContext is null bind failed");
            }
            return false;
        }
        if(SoterDelegate.isTriggeredOOM()) {
            SLogger.w(TAG, "cq: the device has already triggered OOM. mark as not support");
            return false;
        }

        return true;

    }

    @Override
    public SoterCoreResult generateAppGlobalSecureKey() {
        SLogger.i(TAG,"cq: generateAppSecureKey in");

        if(!isNativeSupportSoter()){
            return new SoterCoreResult(ERR_ASK_GEN_FAILED);
        }

        int uid = android.os.Process.myUid();

        try {
            if(mSoterService.generateAppSecureKey(uid) == ERR_OK) {
                return new SoterCoreResult(ERR_OK);
            }
        } catch (RemoteException e) {
            e.printStackTrace();

        }
        return new SoterCoreResult(ERR_ASK_GEN_FAILED);
    }

    @Override
    public SoterCoreResult removeAppGlobalSecureKey() {
        SLogger.i(TAG, "cq: removeAuthKey in");

        if(!isNativeSupportSoter()){
            return new SoterCoreResult(ERR_REMOVE_ASK);
        }

        int uid = android.os.Process.myUid();

        try {
            if(mSoterService.removeAllAuthKey(uid) == ERR_OK) {
                return new SoterCoreResult(ERR_OK);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return new SoterCoreResult(ERR_REMOVE_ASK);

    }

    @Override
    public boolean hasAppGlobalSecureKey() {
        SLogger.i(TAG, "cq: hasAppGlobalSecureKey in");

        int uid = android.os.Process.myUid();

        if(!isNativeSupportSoter()){
            return false;
        }

        try {
            return mSoterService.hasAskAlready(uid);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }


    }

    @Override
    public boolean isAppGlobalSecureKeyValid() {
        return hasAppGlobalSecureKey() && getAppGlobalSecureKeyModel() != null;
    }

    @Override
    public SoterPubKeyModel getAppGlobalSecureKeyModel() {
        SLogger.i(TAG,"cq: getAppSecureKey in");

        if(!isNativeSupportSoter()){
            return null;
        }

        SoterExportResult soterExportResult;
        int uid = android.os.Process.myUid();

        try {
            soterExportResult =  mSoterService.getAppSecureKey(uid);
            byte[] rawBytes = soterExportResult.exportData;

            if (rawBytes != null && rawBytes.length > 0) {
                return retrieveJsonFromExportedData(rawBytes);
            }else {
                SLogger.e(TAG, "cq: soter: key can not be retrieved");
                return null;
            }
        } catch (RemoteException e) {
            e.printStackTrace();

        }
        return null;

    }

    @Override
    public SoterCoreResult generateAuthKey(String authKeyName) {
        SLogger.i(TAG,"cq: generateAuthKey in");

        if(!isNativeSupportSoter()){
            return new SoterCoreResult(ERR_AUTH_KEY_GEN_FAILED);
        }

        int uid = android.os.Process.myUid();

        try {
            if(mSoterService.generateAuthKey(uid, authKeyName) == ERR_OK) {
                return new SoterCoreResult(ERR_OK);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return new SoterCoreResult(ERR_AUTH_KEY_GEN_FAILED);
    }

    @Override
    public SoterCoreResult removeAuthKey(String authKeyName, boolean isAutoDeleteASK) {
        SLogger.i(TAG,"cq: removeAuthKey in");

        if(!isNativeSupportSoter()){
            return new SoterCoreResult(ERR_REMOVE_AUTH_KEY);
        }

        int uid = android.os.Process.myUid();

        try {
            if(mSoterService.removeAuthKey(uid, authKeyName) == ERR_OK) {
                return new SoterCoreResult(ERR_OK);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return new SoterCoreResult(ERR_REMOVE_AUTH_KEY);
    }

    @Override
    public Signature initAuthKeySignature(String useKeyAlias) throws InvalidKeyException, NoSuchProviderException,
            NoSuchAlgorithmException, KeyStoreException, IOException,
            CertificateException, UnrecoverableEntryException {
        return null;
    }

    @Override
    public boolean isAuthKeyValid(String authKeyName, boolean autoDelIfNotValid) {
        SLogger.i(TAG,"cq: isAuthKeyValid in");
        //todo
        return hasAuthKey(authKeyName) && getAuthKeyModel(authKeyName) != null;
    }

    @Override
    public SoterPubKeyModel getAuthKeyModel(String authKeyName) {
        SLogger.i(TAG,"cq: getAppSecureKey in");

        if(!isNativeSupportSoter()){
            return null;
        }

        SoterExportResult soterExportResult;
        int uid = android.os.Process.myUid();

        try {
            soterExportResult =  mSoterService.getAuthKey(uid, authKeyName);
            byte[] rawBytes = soterExportResult.exportData;
            if (rawBytes != null && rawBytes.length > 0) {
                return retrieveJsonFromExportedData(rawBytes);
            }else {
                SLogger.e(TAG, "soter: key can not be retrieved");
                return null;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    public Signature getAuthInitAndSign(String useKeyAlias) {
        return null;
    }

    @Override
    public boolean hasAuthKey(String authKeyName) {
        int uid = android.os.Process.myUid();

        if(!isNativeSupportSoter()){
            return false;
        }

        try {
            return mSoterService.hasAuthKey(uid,authKeyName);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public long initSigh(String kname, String challenge) {

        if(!isNativeSupportSoter()){
            return 0;
        }

        int uid = android.os.Process.myUid();

        SoterSessionResult result;

        try {
            result =  mSoterService.initSigh(uid, kname, challenge);
            return result.session;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;

    }

    @Override
    public byte[] finishSign(long signSession) throws Exception{

        if(!isNativeSupportSoter()){
            return null;
        }

        int uid = android.os.Process.myUid();
        SoterSignResult soterSignResult;
        byte[] rawBytes = new byte[0];
        try {
            soterSignResult =  mSoterService.finishSign(signSession);
            rawBytes = soterSignResult.exportData;
            if(soterSignResult.resultCode != ERR_OK ){
                throw new Exception("finishSign error");
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return rawBytes;

    }

}

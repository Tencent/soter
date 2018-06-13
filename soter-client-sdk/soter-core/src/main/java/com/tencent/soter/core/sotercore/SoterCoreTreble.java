package com.tencent.soter.core.sotercore;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;

import com.tencent.soter.soterserver.ISoterService;
import com.tencent.soter.soterserver.SoterExportResult;
import com.tencent.soter.soterserver.SoterSessionResult;
import com.tencent.soter.soterserver.SoterSignResult;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/**
 * The SOTER Core APIs Treble project
 */
public class SoterCoreTreble extends SoterCoreBase implements ConstantsSoter, SoterErrCode{

    public static final String TAG = "Soter.SoterCoreTreble";

    private static boolean isAlreadyCheckedSetUp = false;

    private Context mContext;

    protected ISoterService mSoterService;

    private boolean connected = false;

    //同步锁
    private final Object lock = new Object();

    private SyncJob syncJob = new SyncJob();


    protected static final int DEFAULT_BLOCK_TIME = 10 * 1000; // Default synchronize block time


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(
                ComponentName className, IBinder service) {
            SLogger.i(TAG, "onServiceConnected");
            synchronized (lock) {
                connected = true;
                lock.notifyAll();
            }

            mSoterService =
                    ISoterService.Stub.asInterface(service);


            SLogger.i(TAG, "Binding is done - Service connected");

            syncJob.countDown();
        }

        public void onServiceDisconnected(ComponentName className) {
            synchronized (lock) {
                connected = false;
                lock.notifyAll();
            }

            mSoterService = null;

            SLogger.i(TAG, "unBinding is done - Service disconnected");

            syncJob.countDown();
        }
    };

    @Override
    public boolean initSoter(Context context) {

        mContext = context;

        bindServiceIfNeeded();

        return true;
    }

    public void bindServiceIfNeeded() {
        SoterCoreTaskThread.getInstance().postToWorker(new Runnable() {
            @Override
            public void run() {

                if (!connected) {
                    SLogger.i(TAG, "bindServiceIfNeeded try to bind");
                    syncJob.doAsSyncJob(DEFAULT_BLOCK_TIME, new Runnable() {
                        @Override
                        public void run() {
                            bindService(mContext);
                        }
                    });

                }
            }
        });

    }

    public void bindService(Context context){
        Intent intent = new Intent();
        intent.setAction("com.tencent.soter.soterserver.ISoterService");
        intent.setPackage("com.tencent.soter.soterserver");

        context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        SLogger.i(TAG, "Binding is start ");
    }

    public void unbindService(Context context){
        context.unbindService(mServiceConnection);
    }

    public boolean isNativeSupportSoter() {

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

        if(mContext == null) {
            SLogger.w(TAG, "cq: context is null");
            return new SoterCoreResult(ERR_ASK_GEN_FAILED);
        }

        bindServiceIfNeeded();

        if(mSoterService == null) {
            SLogger.w(TAG, "cq: soter service is null");
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

        if(mContext == null) {
            SLogger.w(TAG, "cq: context is null");
            return new SoterCoreResult(ERR_REMOVE_ASK);
        }

        bindServiceIfNeeded();

        if(mSoterService == null) {
            SLogger.w(TAG, "cq: soter service is null");
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

        if(!isNativeSupportSoter()){
            return false;
        }

        if(mContext == null) {
            SLogger.w(TAG, "cq: context is null");
            return false;
        }

        bindServiceIfNeeded();

        if(mSoterService == null) {
            SLogger.w(TAG, "cq: soter service is null");
            return false;
        }

        int uid = android.os.Process.myUid();

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

        if(mContext == null) {
            SLogger.w(TAG, "cq: context is null");
            return null;
        }

        bindServiceIfNeeded();

        if(mSoterService == null) {
            SLogger.w(TAG, "cq: soter service is null");
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

        if(mContext == null) {
            SLogger.w(TAG, "cq: context is null");
            return new SoterCoreResult(ERR_AUTH_KEY_GEN_FAILED);
        }

        bindServiceIfNeeded();

        if(mSoterService == null) {
            SLogger.w(TAG, "cq: soter service is null");
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

        if(mContext == null) {
            SLogger.w(TAG, "cq: context is null");
            return new SoterCoreResult(ERR_REMOVE_AUTH_KEY);
        }

        bindServiceIfNeeded();

        if(mSoterService == null) {
            SLogger.w(TAG, "cq: soter service is null");
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

        if(mContext == null) {
            SLogger.w(TAG, "cq: context is null");
            return null;
        }

        bindServiceIfNeeded();

        if(mSoterService == null) {
            SLogger.w(TAG, "cq: soter service is null");
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

        if(mContext == null) {
            SLogger.w(TAG, "cq: context is null");
            return false;
        }

        bindServiceIfNeeded();

        if(mSoterService == null) {
            SLogger.w(TAG, "cq: soter service is null");
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

        if(mContext == null) {
            SLogger.w(TAG, "cq: context is null");
            return 0;
        }

        bindServiceIfNeeded();

        if(mSoterService == null) {
            SLogger.w(TAG, "cq: soter service is null");
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

        if(mContext == null) {
            SLogger.w(TAG, "cq: context is null");
            return null;
        }

        bindServiceIfNeeded();

        if(mSoterService == null) {
            SLogger.w(TAG, "cq: soter service is null");
            return null;
        }

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

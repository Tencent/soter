package com.tencent.soter.core.sotercore;

import android.os.Handler;
import android.os.Looper;

import com.tencent.soter.core.model.SLogger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SyncJob {
    private static final String TAG = "Soter.SyncJob";
    private CountDownLatch countDownWait = null;

    private static Handler mMainLooperHandler = null;

    // soter在某些机型上初始化，当 SoterCore 的 getProviderSoterCore 为 NULL 时，会在 SoterCoreTreble 中绑定 ISoterService
    // SoterCoreTreble 的 mServiceConnection 连接成功后，在 onServiceConnected 中调用 countDown 该方法会发生必现的死锁现象，导致主线程卡顿 3s
    // 只有等 countDownWait.await 3秒后释放锁后，这里才会继续执行，主线程恢复。
    public synchronized void countDown(){
        if (countDownWait != null) {
            countDownWait.countDown();
            countDownWait = null;
        }
    }

    public synchronized void doAsSyncJob(final long blockTime, final Runnable r) {
        SLogger.i(TAG, "doAsSyncJob");
        if (countDownWait == null) {
            countDownWait = new CountDownLatch(1);
        }

        if (r == null) {
            return;
        }

//        postToMainThread(r);
        r.run();
//        SLogger.i(TAG, "doAsSyncJob postToMainThread");
        if (countDownWait != null) {
            try {
                countDownWait.await(blockTime, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                SLogger.printErrStackTrace(TAG, e, "");
            }
        }
    }

    private static void postToMainThread(final Runnable run) {
        if (run == null) {
            return;
        }

        if (mMainLooperHandler == null) {
            mMainLooperHandler = new Handler(Looper.getMainLooper());
        }

        mMainLooperHandler.post(run);
    }


}

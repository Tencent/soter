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

    public void countDown(){
        if (countDownWait != null) {
            countDownWait.countDown();
        }
    }

    public void doAsSyncJob(final long blockTime, final Runnable r) {
        SLogger.i(TAG, "doAsSyncJob");
        if (r == null) {
            return;
        }
        countDownWait = new CountDownLatch(1);

        r.run();

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

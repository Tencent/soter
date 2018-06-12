package com.tencent.soter.core.sotercore;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SyncJob {
    private static final String TAG = "Soter.SyncJob";
    private CountDownLatch countDownWait = null;

    private static Handler mMainLooperHandler = null;

    public void countDown(){
        if (countDownWait != null) {
            countDownWait.countDown();
            countDownWait = null;
        }
    }

    public void doAsSyncJob(final long blockTime, final Runnable r) {
        Log.i(TAG, "doAsSyncJob");
        if (countDownWait == null) {
            countDownWait = new CountDownLatch(1);
        }

        if (r == null) {
            return;
        }

        postToMainThread(r);

        Log.i(TAG, "doAsSyncJob postToMainThread");
        if (countDownWait != null) {
            try {
                countDownWait.await(blockTime, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Log.w(TAG, e.getMessage());
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

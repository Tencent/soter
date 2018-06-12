package com.tencent.soter.core.sotercore;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.tencent.soter.core.model.SLogger;

public class SoterCoreTaskThread {
    @SuppressWarnings("unused")
    private static final String TAG = "Soter.SoterCoreTaskThread";
    private static final String HANDLER_THREAD_NAME = "SoterCoreThreadName";

    private SoterCoreTaskThread() {
        HandlerThread taskHandlerThread = new HandlerThread(HANDLER_THREAD_NAME);
        taskHandlerThread.start();
        Looper taskLooper = taskHandlerThread.getLooper();
        if(taskLooper != null) {
            mTaskHandler = new Handler(taskHandlerThread.getLooper());
        } else {
            SLogger.e(TAG, "soter: task looper is null! use main looper as the task looper");
            mTaskHandler = new Handler(Looper.getMainLooper());
        }
    }

    private static volatile SoterCoreTaskThread mInstance = null;

    private Handler mTaskHandler = null;

    public static SoterCoreTaskThread getInstance() {
        if (mInstance == null) {
            synchronized (SoterCoreTaskThread.class) {
                if (mInstance == null) {
                    mInstance = new SoterCoreTaskThread();
                }
                return mInstance;
            }
        } else {
            return mInstance;
        }
    }

    public void postToWorker(final Runnable task) {
        mTaskHandler.post(task);
    }

    public void postToWorkerDelayed(final Runnable task, long delayInMs) {
        mTaskHandler.postDelayed(task, delayInMs);
    }

}

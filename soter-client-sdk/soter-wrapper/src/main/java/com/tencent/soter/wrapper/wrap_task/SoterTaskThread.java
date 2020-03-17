/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.soter.wrapper.wrap_task;

import android.graphics.PointF;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.tencent.soter.core.model.SLogger;

/**
 * Created by henryye on 2017/4/6.
 * <p>
 * All soter tasks should be called synchronized
 */

@SuppressWarnings("unused")
public class SoterTaskThread {
    @SuppressWarnings("unused")
    private static final String TAG = "Soter.SoterTaskThread";
    private static final String HANDLER_THREAD_NAME = "SoterGenKeyHandlerThreadName";

    private static volatile SoterTaskThread mInstance = null;

    private HandlerThread mTaskHandlerThread;
    private Handler mTaskHandler = null;
    private Handler mMainLooperHandler = null;

    private SoterTaskThread() {
        if (mTaskHandlerThread == null) {
            mTaskHandlerThread = new HandlerThread(HANDLER_THREAD_NAME);
            mTaskHandlerThread.start();

            Looper taskLooper = mTaskHandlerThread.getLooper();
            if(taskLooper != null) {
                mTaskHandler = new Handler(mTaskHandlerThread.getLooper());
            } else {
                SLogger.e(TAG, "soter: task looper is null! use main looper as the task looper");
                mTaskHandler = new Handler(Looper.getMainLooper());
            }
        }
        mMainLooperHandler = new Handler(Looper.getMainLooper());
    }

    public static SoterTaskThread getInstance() {
        if (mInstance == null) {
            synchronized (SoterTaskThread.class) {
                if (mInstance == null) {
                    mInstance = new SoterTaskThread();
                }
                return mInstance;
            }
        } else {
            return mInstance;
        }
    }

    public void setTaskHandlerThread(HandlerThread handlerThread) {
        if (mTaskHandlerThread != null && mTaskHandlerThread.isAlive()) {
            SLogger.i(TAG, "quit the previous thread");
            mTaskHandlerThread.quit();
        }

        mTaskHandlerThread = handlerThread;
        mTaskHandlerThread.setName(HANDLER_THREAD_NAME);
        if (!handlerThread.isAlive()) {
            handlerThread.start();
        }
        mTaskHandler = new Handler(mTaskHandlerThread.getLooper());
    }

    public void postToWorker(final Runnable task) {
        mTaskHandler.post(task);
    }

    public void postToWorkerDelayed(final Runnable task, long delayInMs) {
        mTaskHandler.postDelayed(task, delayInMs);
    }

    @SuppressWarnings("WeakerAccess")
    public void postToMainThread(final Runnable task) {
        mMainLooperHandler.post(task);
    }

    public void postToMainThreadDelayed(final Runnable task, long delayInMs) {
        mMainLooperHandler.postDelayed(task, delayInMs);
    }
}

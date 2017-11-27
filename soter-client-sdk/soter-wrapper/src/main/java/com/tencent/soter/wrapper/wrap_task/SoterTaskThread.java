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

    private SoterTaskThread() {
        HandlerThread taskHandlerThread = new HandlerThread(HANDLER_THREAD_NAME);
        taskHandlerThread.start();
        Looper taskLooper = taskHandlerThread.getLooper();
        if(taskLooper != null) {
            mTaskHandler = new Handler(taskHandlerThread.getLooper());
        } else {
            SLogger.e(TAG, "soter: task looper is null! use main looper as the task looper");
            mTaskHandler = new Handler(Looper.getMainLooper());
        }
        mMainLooperHandler = new Handler(Looper.getMainLooper());
    }

    private static volatile SoterTaskThread mInstance = null;

    private Handler mTaskHandler = null;
    private Handler mMainLooperHandler = null;

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

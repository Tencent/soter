/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.tencent.soter.demo.net;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by henryye on 2017/4/6.
 *
 */

class DemoNetworkThread {
    private static final String TAG = "SoterDemo.DemoNetworkThread";
    private static final String HANDLER_THREAD_NAME = "DemoHandlerThreadName";

    private DemoNetworkThread() {
        HandlerThread workerHandlerThread = new HandlerThread(HANDLER_THREAD_NAME);
        workerHandlerThread.start();
        mWorkerHandler = new Handler(workerHandlerThread.getLooper());
    }

    private static volatile DemoNetworkThread mInstance = null;

    private Handler mWorkerHandler = null;

    static DemoNetworkThread getInstance() {
        if(mInstance == null) {
            synchronized (DemoNetworkThread.class) {
                if(mInstance == null) {
                    mInstance = new DemoNetworkThread();
                }
                return mInstance;
            }
        } else {
            return mInstance;
        }
    }

    void postTask(final Runnable task) {
        synchronized (DemoNetworkThread.class) {
            mWorkerHandler.post(task);
        }
    }

    void postTaskDelayed(final Runnable task, long delay) {
        synchronized (DemoNetworkThread.class) {
            mWorkerHandler.postDelayed(task, delay);
        }
    }

}

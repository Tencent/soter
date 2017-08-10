/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.soter.wrapper.wrap_task;

import com.tencent.soter.core.model.SLogger;
import com.tencent.soter.wrapper.SoterWrapperApi;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessCallback;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessResultBase;
import com.tencent.soter.wrapper.wrap_core.SoterProcessErrCode;

/**
 * Created by henryye on 2017/4/20.
 *
 */

abstract public class BaseSoterTask implements SoterProcessErrCode {
    private static final String TAG = "Soter.BaseSoterTask";

    private SoterProcessCallback mCallback;
    private boolean mIsCallbacked = false;

    /**
     * Called before this task added to the pool
     * @return If preExecute eat the callback
     */
    abstract boolean preExecute();

    /**
     * Get if this task is single instance, which means same type of task should not been added to the pool
     * if there's already one.
     * @return true if this task is single instance
     */
    abstract boolean isSingleInstance();

    /**
     * Set callback of this task
     * @param callback the callback of the process
     */
    public void setTaskCallback(SoterProcessCallback callback) {
        this.mCallback = callback;
    }

    /**
     * Called when actively called {@link SoterWrapperApi#tryStopAllSoterTask()}
     */
    abstract void onRemovedFromTaskPoolActively();

    /**
     * Execute logic
     */
    abstract void execute();

    synchronized void callback(final SoterProcessResultBase result) {
        if(mIsCallbacked) {
            SLogger.w(TAG, "soter: warning: already removed the task!");
            return;
        }
        SoterTaskManager.getInstance().removeFromTask(this);
        // must callback in main thread
        SoterTaskThread.getInstance().postToMainThread(new Runnable() {
            @Override
            public void run() {
                callbackInternal(result);
            }
        });

    }
    private void callbackInternal(final SoterProcessResultBase result) {
        if(mCallback != null && !mIsCallbacked) {
            //noinspection unchecked as I can guarantee.
            mCallback.onResult(result);
            mIsCallbacked = true;
        }
    }

    public boolean isFinished() {
        return mIsCallbacked;
    }
}

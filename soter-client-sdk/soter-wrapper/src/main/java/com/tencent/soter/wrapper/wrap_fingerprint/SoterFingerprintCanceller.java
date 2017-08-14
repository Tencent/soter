/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.soter.wrapper.wrap_fingerprint;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;

import com.tencent.soter.core.fingerprint.FingerprintManagerCompat;
import com.tencent.soter.core.model.SLogger;
import com.tencent.soter.core.model.SoterCoreUtil;
import com.tencent.soter.wrapper.wrap_task.SoterTaskManager;
import com.tencent.soter.wrapper.wrap_task.SoterTaskThread;

import junit.framework.Assert;

/**
 * Created by henryye on 2017/4/24.
 *
 * The controller to operate cancellation on fingerprint authentication
 *
 * All devices that support SOTER must be at least Android 5.0, so we do not have to check the API-Level
 */
public class SoterFingerprintCanceller {
    private static final String TAG = "Soter.SoterFingerprintCanceller";

    private CancellationSignal mCancellationSignal = null;
    // try to make it sync with worker handler
    private static final long MAX_WAIT_EXECUTION_TIME = 350;

    public SoterFingerprintCanceller() {
        Assert.assertTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN);
        refreshCancellationSignal();
    }

    /**
     * Cancel the fingerprint authentication asynchronously. Note that the cancel callback in called in {@link FingerprintManagerCompat.AuthenticationCallback#onAuthenticationCancelled()}
     * @return False if the cancellation process is already done before.
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean asyncCancelFingerprintAuthentication() {
        return asyncCancelFingerprintAuthenticationInnerImp(true);
    }

    @SuppressLint("NewApi")
    public boolean asyncCancelFingerprintAuthenticationInnerImp(final boolean shouldPublishCancel) {
        SLogger.v(TAG, "soter: publishing cancellation. should publish: %b", shouldPublishCancel);
        if(!mCancellationSignal.isCanceled()) {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                SoterTaskThread.getInstance().postToWorker(new Runnable() {
                    @Override
                    public void run() {
                        SLogger.v(TAG, "soter: enter worker thread. perform cancel");
                        mCancellationSignal.cancel();
                        if(shouldPublishCancel) {
                            publishCancel();
                        }
                    }
                });
            } else {
                SoterTaskThread.getInstance().postToWorker(new Runnable() {
                    @Override
                    public void run() {
                        mCancellationSignal.cancel();
                    }
                });
                // double check in case some weired devices do not callback cancel in system level
                SoterTaskThread.getInstance().postToWorkerDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SLogger.w(TAG, "hy: waiting for %s ms not callback to system callback. cancel manually", MAX_WAIT_EXECUTION_TIME);
                        publishCancel();
                    }
                }, MAX_WAIT_EXECUTION_TIME);
            }

            return true;
        }
        SLogger.i(TAG, "soter: cancellation signal already expired.");
        return false;
    }

    private void publishCancel() {
        SoterTaskManager.getInstance().publishAuthCancellation();
    }

    @SuppressLint("NewApi")
    public void refreshCancellationSignal() {
        this.mCancellationSignal = new CancellationSignal();
    }

    @NonNull
    public CancellationSignal getSignalObj() {
        return mCancellationSignal;
    }

}

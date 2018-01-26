/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.soter.wrapper.wrap_task;

import android.util.SparseArray;

import com.tencent.soter.core.model.SLogger;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessResultBase;
import com.tencent.soter.wrapper.wrap_core.SoterProcessErrCode;

/**
 * Created by henryye on 2017/4/20.
 * The manager to manage all the soter tasks
 */

public class SoterTaskManager implements SoterProcessErrCode {
    private static final String TAG = "Soter.SoterTaskManager";

    private static volatile SoterTaskManager sInstance = null;
    private static volatile SparseArray<BaseSoterTask> sTaskPool = null;

    private final Object mTaskPoolLock = new Object();

    private SoterTaskManager() {
        sTaskPool = new SparseArray<>(5);
    }

    public static SoterTaskManager getInstance() {
        if(sInstance == null) {
            synchronized (SoterTaskManager.class) {
                if(sInstance == null) {
                    sInstance = new SoterTaskManager();
                }
                return sInstance;
            }
        } else {
            return sInstance;
        }
    }

    /**
     * Add the task to the task pool and execute
     * @param task The task to add and
     * @return true if added and executed successfully
     */
    public boolean addToTask(final BaseSoterTask task, SoterProcessResultBase instanceOnError) {
        if(task == null) {
            SLogger.e(TAG, "soter: task is null. should not happen");
            return false;
        }
        if(instanceOnError == null) {
            SLogger.e(TAG, "soter: instanceOnError is null. should not happen");
            return false;
        }
        boolean isEat = task.preExecute();
        if(!isEat) {
            int taskClassIndex = task.hashCode();
            if(!task.isSingleInstance()) {
                SLogger.i(TAG, "soter: not single instance. directly execute");
                synchronized (mTaskPoolLock) {
                    sTaskPool.put(taskClassIndex, task);
                }
                SoterTaskThread.getInstance().postToWorker(new Runnable() {
                    @Override
                    public void run() {
                        task.execute();
                    }
                });
                return true;
            } else {
                // find all instance
                synchronized (mTaskPoolLock) {
                    for (int i = 0; i < sTaskPool.size(); i++) {
                        int key = sTaskPool.keyAt(i);
                        if (sTaskPool.get(key) != null && sTaskPool.get(key).getClass().getName().equals(task.getClass().getName())) {
                            SLogger.w(TAG, "soter: already such type of task. abandon add task");
                            instanceOnError.setErrCode(ERR_ADD_TASK_FAILED);
                            instanceOnError.setErrMsg("add SOTER task to queue failed. check the logcat for further information");
                            task.callback(instanceOnError);
                            return false;
                        }
                    }
                    sTaskPool.put(taskClassIndex, task);
                }
                SoterTaskThread.getInstance().postToWorker(new Runnable() {
                    @Override
                    public void run() {
                        task.execute();
                    }
                });
                return true;
            }
        } else {
            SLogger.d(TAG, "soter: prepare eat execute.");
            return false;
        }
    }

    public void cancelAllTask() {
        synchronized (mTaskPoolLock) {
            SLogger.i(TAG, "soter: request cancel all");
            if(sTaskPool.size() != 0) {
                for(int i = 0; i < sTaskPool.size(); i++) {
                    final int key = sTaskPool.keyAt(i);
                    SoterTaskThread.getInstance().postToWorker(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (mTaskPoolLock) {
                                BaseSoterTask task = sTaskPool.get(key);
                                if(task != null) {
                                    task.onRemovedFromTaskPoolActively();
                                }
                            }
                        }
                    });

                }
            }
            sTaskPool.clear();
        }
    }


    void removeFromTask(BaseSoterTask task) {
        SLogger.i(TAG, "soter: removing task: %d", task != null ? task.hashCode() : "null");
        if(task == null) {
            SLogger.e(TAG, "soter: task is null");
            return;
        }
        synchronized (mTaskPoolLock) {
            if(sTaskPool.get(task.hashCode()) == null) {
                SLogger.i(TAG, "soter: no such task: %d. maybe this task did not pass preExecute", task.hashCode());
            } else {
                sTaskPool.remove(task.hashCode());
            }
        }
    }

    public void publishAuthCancellation() {
        synchronized (mTaskPoolLock) {
            SLogger.i(TAG, "soter: request publish cancellation");
            if(sTaskPool.size() != 0) {
                for(int i = 0; i < sTaskPool.size(); i++) {
                    final int key = sTaskPool.keyAt(i);
                    SoterTaskThread.getInstance().postToWorker(new Runnable() {
                        @Override
                        public void run() {
                            BaseSoterTask task = null;
                            synchronized (mTaskPoolLock) {
                                task = sTaskPool.get(key);
                            }
                            if(task != null && (task instanceof AuthCancellationCallable)) {
                                if(!((AuthCancellationCallable) task).isCancelled()) {
                                    ((AuthCancellationCallable) task).callCancellationInternal();
                                }
                            }
                        }
                    });
                }
            }
        }
    }
}

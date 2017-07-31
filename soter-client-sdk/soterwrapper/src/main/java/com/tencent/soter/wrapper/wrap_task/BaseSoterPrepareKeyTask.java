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
import com.tencent.soter.wrapper.wrap_core.SoterDataCenter;

/**
 * Created by henryye on 2017/4/28.
 *
 */

abstract class BaseSoterPrepareKeyTask extends BaseSoterTask {
    private static final String TAG = "Soter.BaseSoterPrepareKeyTask";

    /**
     * Mark this key pair as generating status and save the status permanently
     * in file system,
     * in case the key is not uploaded or verified but not got callback due to
     * strange situations, such as the application being killed.
     * The values would be extracted in init process, and delete those not uploaded.
     */
    @SuppressWarnings("WeakerAccess")
    protected void markKeyStatus(String keyName, int keyStatus) {
        SLogger.d(TAG, "soter: marking preference. key: %s, status: %d", keyName, keyStatus);
        synchronized (SoterDataCenter.class) {
            if(SoterDataCenter.getInstance().getStatusSharedPreference() != null) {
                SoterDataCenter.getInstance().getStatusSharedPreference().edit().putInt(keyName, keyStatus).apply();
            }
        }
    }

}

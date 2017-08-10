/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.tencent.soter.demo.model;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Created by henryye on 2017/4/25.
 */

public class SoterDemoData {
    private static final String TAG = "SoterDemo.SoterDemoData";

    private static final String DEMO_DISK_CACHE_SP = "DemoDiskCacheSp";

    private static final String KEY_IS_FINGERPRINT_PAY_OPENED = "isFingerprintOpened";

    private static SoterDemoData sInstance = null;
    private boolean isFingerprintPayOpened = false;

    public static SoterDemoData getInstance() {
        if(sInstance == null) {
            synchronized (SoterDemoData.class) {
                if(sInstance == null) {
                    sInstance = new SoterDemoData();
                }
                return sInstance;
            }
        } else {
            return sInstance;
        }
    }

    public void init(@NonNull Context context) {
        isFingerprintPayOpened = context.getSharedPreferences(DEMO_DISK_CACHE_SP,
                Context.MODE_PRIVATE).getBoolean(KEY_IS_FINGERPRINT_PAY_OPENED, false);
    }

    public void setIsFingerprintPayOpened(Context context, boolean isOpened) {
        isFingerprintPayOpened = isOpened;
        context.getSharedPreferences(DEMO_DISK_CACHE_SP, Context.MODE_PRIVATE).edit().
                putBoolean(KEY_IS_FINGERPRINT_PAY_OPENED, isOpened).apply();
    }

    public boolean getIsFingerprintPayOpened() {
        return isFingerprintPayOpened;
    }
}

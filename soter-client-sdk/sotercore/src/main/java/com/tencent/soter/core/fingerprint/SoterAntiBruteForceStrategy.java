/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.soter.core.fingerprint;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.tencent.soter.core.model.SLogger;

/**
 * Created by henryye on 16/3/1.
 * If need any edit necessary, please contact him by RTX
 *
 * The strategy to avoid brute force fingerprint service (as there's 5/10000 chance at most for fingerprint
 * sensor FRR)
 *
 * NOTE: Only for fingerprint
 */
@SuppressWarnings("WeakerAccess")
public class SoterAntiBruteForceStrategy {
    private static final String TAG = "Soter.SoterAntiBruteForceStrategy";
    //constants
    private static final int MAX_FAIL_NUM = 5;
    private static final int FREEZE_SECOND = 30;
    private static final long DEFAULT_FREEZE_TIME = -1L;

    private static final String KEY_FAIL_TIMES = "key_fail_times";
    private static final String KEY_LAST_FREEZE_TIME = "key_last_freeze_time";

    /**
     * Check if system itself has already done the anti brute staff.
     * @return true if system has done it.
     */
    public static boolean isSystemHasAntiBruteForce() {
        return android.os.Build.VERSION.SDK_INT >= 23;
    }

    //variable
    private static int getCurrentFailTime(Context context) {
        // Load from db every time to avoid hackers kill process actively
        Integer currentFailTime = getCurrentFailTimeInDB(context);
        SLogger.i(TAG, "soter: current retry time: " + currentFailTime);
        return currentFailTime;
    }

    private static void setCurrentFailTime(Context context, int currentFailTime) {
        SLogger.i(TAG, "soter: setting to time: " + currentFailTime);
        if(currentFailTime < 0) {
            SLogger.w(TAG, "soter: illegal fail time");
            return;
        }
        setCurrentFailTimeInDB(context, currentFailTime);
    }

    private static long getLastFreezeTime(Context context) {
        // Load from db every time to avoid hackers kill process actively
        Long lastFreezeTime = getLastFreezeTimeInDB(context);
        SLogger.i(TAG, "soter: current last freeze time: " + lastFreezeTime);
        return lastFreezeTime;
    }

    private static void setLastFreezeTime(Context context, long lastFreezeTime) {
        SLogger.i(TAG, "soter: setting last freeze time: " + lastFreezeTime);
        if(lastFreezeTime < -1L) {
            SLogger.w(TAG, "soter: illegal setLastFreezeTime");
            return;
        }
        setLastFreezeTimeInDB(context, lastFreezeTime);
    }



    static void freeze(Context context) {
        //switch current state to freeze
        setCurrentFailTime(context, MAX_FAIL_NUM + 1);
        setLastFreezeTime(context, System.currentTimeMillis());
    }

    static void unFreeze(Context context) {
        setLastFreezeTime(context, DEFAULT_FREEZE_TIME);
        setCurrentFailTime(context, 0);
    }

    static void addFailTime(Context context) {
        Integer currentFailTime = getCurrentFailTime(context);
        setCurrentFailTime(context, ++currentFailTime);
    }

    public static boolean isCurrentTweenTimeAvailable(Context context) {
        int tweenSec = (int)((System.currentTimeMillis()- getLastFreezeTime(context)) / 1000);
        SLogger.i(TAG, "soter: tween sec after last freeze: " + tweenSec);
        if(tweenSec > FREEZE_SECOND) {
            SLogger.d(TAG, "soter: after last freeze");
            return true;
        }
        return false;
    }

    public static boolean isCurrentFailTimeAvailable(Context context) {
        if(getCurrentFailTime(context) < MAX_FAIL_NUM) {
            SLogger.i(TAG, "soter: fail time available");
            return true;
        }
        return false;
    }

    private static void setCurrentFailTimeInDB(Context context, int currentFailTime) {
        if(context == null) {
            SLogger.e(TAG, "soter: context is null");
            return;
        }
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(KEY_FAIL_TIMES, currentFailTime);
        editor.apply();
    }

    private static int getCurrentFailTimeInDB(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_FAIL_TIMES, 0);
    }

    private static long getLastFreezeTimeInDB(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(KEY_LAST_FREEZE_TIME, -1);
    }

    private static void setLastFreezeTimeInDB(Context context, long lastFreezeTime) {
        if(context == null) {
            SLogger.e(TAG, "soter: context is null");
            return;
        }
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putLong(KEY_LAST_FREEZE_TIME, lastFreezeTime);
        editor.apply();
    }


}

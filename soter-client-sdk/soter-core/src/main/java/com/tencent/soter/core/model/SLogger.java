/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.soter.core.model;

import android.util.Log;

import junit.framework.Assert;

/**
 * Created by henryye on 2017/4/13.
 *
 * Wrap all loggers, for later use
 */

@SuppressWarnings("unused")
public class SLogger {

    private static ISoterLogger mLoggerImp = new DefaultSoterLogger();

    public static void setLogImp(ISoterLogger logInstance) {
        Assert.assertTrue(logInstance != null);
        mLoggerImp = logInstance;
    }

    public static void v(String TAG, String msg, Object... args) {
        mLoggerImp.v(TAG, msg, args);
    }

    public static void d(String TAG, String msg, Object... args) {
        mLoggerImp.d(TAG, msg, args);
    }

    public static void i(String TAG, String msg, Object... args) {
        mLoggerImp.i(TAG, msg, args);
    }

    public static void w(String TAG, String msg, Object... args) {
        mLoggerImp.w(TAG, msg, args);
    }

    public static void e(String TAG, String msg, Object... args) {
        mLoggerImp.e(TAG, msg, args);
    }

    public static void printErrStackTrace(String TAG, Throwable e, String errMsg) {
        mLoggerImp.printErrStackTrace(TAG, e, errMsg);
    }

    private static class DefaultSoterLogger implements ISoterLogger{

        @Override
        public void v(String TAG, String msg, Object... args) {
            try {
                Log.v(TAG, String.format(msg, args));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void d(String TAG, String msg, Object... args) {
            try {
                Log.d(TAG, String.format(msg, args));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void i(String TAG, String msg, Object... args) {
            try {
                Log.i(TAG, String.format(msg, args));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void w(String TAG, String msg, Object... args) {
            try {
                Log.w(TAG, String.format(msg, args));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void e(String TAG, String msg, Object... args) {
            try {
                Log.e(TAG, String.format(msg, args));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void printErrStackTrace(String TAG, Throwable e, String errMsg) {
            e.printStackTrace();
        }
    }

}

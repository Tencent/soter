/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.tencent.soter.demo.model;

import android.util.Log;

/**
 * Created by henryye on 2017/4/13.
 */

public class DemoLogger {

    public static void d(String TAG, String msg, Object... args) {
        Log.d(TAG, String.format(msg, args));
    }

    public static void i(String TAG, String msg, Object... args) {
        Log.i(TAG, String.format(msg, args));
    }

    public static void w(String TAG, String msg, Object... args) {
        Log.w(TAG, String.format(msg, args));
    }

    public static void e(String TAG, String msg, Object... args) {
        Log.e(TAG, String.format(msg, args));
    }
}

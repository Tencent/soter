/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.soter.core.model;

import java.security.MessageDigest;

/**
 * Created by henryye on 2017/4/7.
 *
 * Basic utilities for SOTER
 */
public class SoterCoreUtil {

    public static boolean isNullOrNil(final String object) {
        return (object == null) || (object.length() <= 0);
    }

    public static boolean isNullOrNil(final byte[] object) {
        return (object == null) || (object.length <= 0);
    }

    public static boolean isNullOrNil(final int[] object) {
        return (object == null) || (object.length <= 0);
    }

    public static String nullAsNil(String object) {
        return object == null ? "" : object;
    }

    public static long getCurrentTicks() {
        return System.nanoTime();
    }

    public static long ticksToNowInMs(long beforeTicks) {
        return (System.nanoTime() - beforeTicks) / 1000 / 1000;
    }

    public static String getMessageDigest(byte[] buffer) {
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        try {
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(buffer);
            byte[] md = mdTemp.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }

}

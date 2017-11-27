/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.soter.wrapper.wrap_core;

import android.content.SharedPreferences;
import android.util.SparseArray;

/**
 * Created by henryye on 2017/4/21.
 * To cache the universal variables.
 */

public class SoterDataCenter {
    private static volatile SoterDataCenter sInstance = null;

    public static SoterDataCenter getInstance() {
        if(sInstance == null) {
            synchronized (SoterDataCenter.class) {
                if(sInstance == null) {
                    sInstance = new SoterDataCenter();
                }
                return sInstance;
            }
        } else {
            return sInstance;
        }
    }

    private boolean              isInit = false;
    private boolean              isSupportSoter = false;
    private SparseArray<String>  sAuthKeyNames = new SparseArray<>(10);
    private SharedPreferences    sStatusSharedPreference = null;

    public boolean isInit() {
        synchronized (SoterDataCenter.class) {
            return isInit;
        }
    }

    public void setInit(@SuppressWarnings("SameParameterValue") boolean init) {
        synchronized (SoterDataCenter.class) {
            isInit = init;
        }
    }

    public boolean isSupportSoter() {
        synchronized (SoterDataCenter.class) {
            return isSupportSoter;
        }
    }

    public void setSupportSoter(boolean supportSoter) {
        synchronized (SoterDataCenter.class) {
            isSupportSoter = supportSoter;
        }
    }

    public SparseArray<String> getAuthKeyNames() {
        synchronized (SoterDataCenter.class) {
            return sAuthKeyNames;
        }
    }

    public SharedPreferences getStatusSharedPreference() {
        synchronized (SoterDataCenter.class) {
            return sStatusSharedPreference;
        }
    }

    public void setStatusSharedPreference(SharedPreferences statusSharedPreference) {
        synchronized (SoterDataCenter.class) {
            this.sStatusSharedPreference = statusSharedPreference;
        }
    }

    public void clearStatus() {
        synchronized (SoterDataCenter.class) {
            isInit = false;
            isSupportSoter = false;
            sAuthKeyNames = new SparseArray<>(10);
            sStatusSharedPreference = null;
        }
    }
}

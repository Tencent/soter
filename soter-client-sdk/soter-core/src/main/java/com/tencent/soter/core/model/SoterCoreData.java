/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.soter.core.model;

/**
 * Created by henryye on 2017/7/12.
 * The core data storage for soter core
 */

public class SoterCoreData {
    private volatile static SoterCoreData instance = null;

    public static SoterCoreData getInstance() {
        if(instance == null) {
            synchronized (SoterCoreData.class) {
                if(instance == null) {
                    instance = new SoterCoreData();
                }
                return instance;
            }
        } else {
            return instance;
        }
    }
    private String mAskName = ConstantsSoter.COMMON_SOTER_APP_SECURE_KEY_NAME;

    public String getAskName() {
        return mAskName;
    }

    public void setAskName(String AskName) {
        this.mAskName = AskName;
    }

}

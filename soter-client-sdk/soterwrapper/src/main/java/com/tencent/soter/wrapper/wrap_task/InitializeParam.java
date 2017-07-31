/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.soter.wrapper.wrap_task;

import com.tencent.soter.core.model.ISoterLogger;
import com.tencent.soter.wrapper.wrap_net.IWrapGetSupportNet;

/**
 * Created by henryye on 2017/7/12.
 * The initialize parameter and Builder used for initialize
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class InitializeParam {
    private IWrapGetSupportNet getSupportNetWrapper;
    private String distinguishSalt = "";
    private int[] scenes;

    private ISoterLogger mSoterLogger;
    private String customAppSecureKeyName = "";

    private InitializeParam() {
        //
    }

    public IWrapGetSupportNet getGetSupportNetWrapper() {
        return getSupportNetWrapper;
    }

    public String getDistinguishSalt() {
        return distinguishSalt;
    }

    public int[] getScenes() {
        return scenes;
    }

    public ISoterLogger getSoterLogger() {
        return mSoterLogger;
    }

    public String getCustomAppSecureKeyName() {
        return customAppSecureKeyName;
    }

    public static class InitializeParamBuilder {
        private InitializeParam mInitializeParam = new InitializeParam();

        /**
         * Set the network wrapper to query whether the device supports SOTER from server. You can set to null if you do not want to check from server
         * @param getSupportNetWrapper the wrapper
         * @return the builder
         */
        public InitializeParamBuilder setGetSupportNetWrapper(IWrapGetSupportNet getSupportNetWrapper) {
            mInitializeParam.getSupportNetWrapper = getSupportNetWrapper;
            return this;
        }

        /**
         * Set the salt added in authkey names in case you have some scenery to distinguish business scenes. You need it if you want to separate auth keys in different account.
         * @param distinguishSalt the salt. The length of it must be less than 24
         * @return the builder
         */
        public InitializeParamBuilder setDistinguishSalt(String distinguishSalt) {
            mInitializeParam.distinguishSalt = distinguishSalt;
            return this;
        }

        /**
         * Set the business scenes of your application. Each scene represents a pair of Auth Key. Scenes must be unique and non-null. The application should keep the scene values, and use the value instead of Auth Key name to operate on Auth Keys
         * @param scenes the business scenes.
         * @return the builder
         */
        public InitializeParamBuilder setScenes(int... scenes) {
            mInitializeParam.scenes = scenes;
            return this;
        }

        /**
         * Set a custom app secure key name. Usually you do not need to set this. Just for compat former versions
         * @param customAppSecureKeyName the custom key name
         * @return the builder
         */
        public InitializeParamBuilder setCustomAppSecureKeyName(String customAppSecureKeyName) {
            mInitializeParam.customAppSecureKeyName = customAppSecureKeyName;
            return this;
        }

        /**
         * Set a log implement. You do not have to set it if you just use normal logcat for log printing
         * @param soterLogger The log implement
         * @return the builder
         */
        public InitializeParamBuilder setSoterLogger(ISoterLogger soterLogger) {
            mInitializeParam.mSoterLogger = soterLogger;
            return this;
        }

        public InitializeParam build() {
            return mInitializeParam;
        }
    }
}

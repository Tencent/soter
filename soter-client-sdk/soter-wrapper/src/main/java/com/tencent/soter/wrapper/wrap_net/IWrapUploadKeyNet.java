/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.tencent.soter.wrapper.wrap_net;

/**
 * Created by henryye on 2017/4/13.
 * Net wrapper for upload public keys
 */

public interface IWrapUploadKeyNet extends ISoterNetBaseWrapper<IWrapUploadKeyNet.UploadRequest,
        IWrapUploadKeyNet.UploadResult> {

    @SuppressWarnings("WeakerAccess")
    class UploadRequest {
        public UploadRequest(String signature, String keyJson) {
            mKeyJsonSignature = signature;
            mKeyJson = keyJson;
        }

        public String mKeyJson;
        public String mKeyJsonSignature;
    }

    class UploadResult {
        public UploadResult(boolean isUploadAndVerifiedSuccess) {
            mIsUploadAndVerifiedSuccess = isUploadAndVerifiedSuccess;
        }

        public boolean mIsUploadAndVerifiedSuccess;
    }
}

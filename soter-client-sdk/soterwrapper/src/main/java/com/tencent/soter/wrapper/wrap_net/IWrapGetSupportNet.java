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
 * The network wrapper to check whether device supports SOTER from server
 */

public interface IWrapGetSupportNet extends ISoterNetBaseWrapper<IWrapGetSupportNet.GetSupportRequest, IWrapGetSupportNet.GetSupportResult> {
    @SuppressWarnings("WeakerAccess")
    class GetSupportRequest {
        public String requestJson;
        public GetSupportRequest(String requestJson) {
            this.requestJson = requestJson;
        }
    }

    class GetSupportResult {
        public boolean isSupport = false;

        public GetSupportResult(boolean isSupport) {
            this.isSupport = isSupport;
        }
    }

}

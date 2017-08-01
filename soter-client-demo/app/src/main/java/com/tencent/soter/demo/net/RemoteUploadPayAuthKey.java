/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.tencent.soter.demo.net;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by henryye on 2017/4/27.
 */

public class RemoteUploadPayAuthKey extends RemoteUploadAuthKeyBase {
    private static final String TAG = "SoterDemo.RemoteUploadPayAuthKey";

    private static final String KEY_PAY_PWD_DIGEST = "pwdDigest";

    private String mPayPwdDigest;

    public RemoteUploadPayAuthKey(String payPwdDigest) {
        this.mPayPwdDigest = payPwdDigest;
    }

    @Override
    protected String getNetUrl() {
        return "http://qcloud.simulate.soter_demo/upload_pay_auth_key";
    }

    @Override
    void setExtraJson(JSONObject requestJson) {
        if(requestJson != null) {
            try {
                requestJson.put(KEY_PAY_PWD_DIGEST, mPayPwdDigest);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

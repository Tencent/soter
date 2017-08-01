/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.tencent.soter.demo.net;

import android.support.annotation.NonNull;

import com.tencent.soter.wrapper.wrap_net.ISoterNetCallback;
import com.tencent.soter.wrapper.wrap_net.IWrapUploadSignature;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by henryye on 2017/4/27.
 *
 */

public class RemoteOpenFingerprintPay extends RemoteBase implements IWrapUploadSignature {
    private static final String TAG = "SoterDemo.RemoteOpenFingerprintPay";

    private static final String KEY_REQUEST_SIGNATURE_JSON = "signatureJson";
    private static final String KEY_REQUEST_VERIFY_SALT_LENGTH = "saltlen";
    private static final String KEY_REQUEST_PWD_DIGEST = "pwdDigest";
    private static final String KEY_RESULT_IS_OPEN_SUCCESS = "isOpenSuccess";

    private ISoterNetCallback<UploadSignatureResult> mCallback = null;
    private String mPwdDigest = null;

    public RemoteOpenFingerprintPay(String pwdDigest) {
        this.mPwdDigest = pwdDigest;
    }

    @Override
    public void setRequest(@NonNull UploadSignatureRequest requestDataModel) {
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put(KEY_REQUEST_SIGNATURE_JSON, requestDataModel.signatureJson);
            requestJson.put(KEY_REQUEST_VERIFY_SALT_LENGTH, requestDataModel.signatureSaltLength);
            requestJson.put(KEY_REQUEST_PWD_DIGEST, mPwdDigest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setRequestJson(requestJson);
    }

    @Override
    public void setCallback(ISoterNetCallback<UploadSignatureResult> callback) {
        this.mCallback = callback;
    }

    @Override
    public void execute() {
        super.execute();
    }

    @Override
    JSONObject getSimulateJsonResult(JSONObject requestJson) {
        JSONObject resultJson = new JSONObject();
        try {
            resultJson.put(KEY_RESULT_IS_OPEN_SUCCESS, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultJson;
    }

    @Override
    void onNetworkEnd(JSONObject resultJson) {
        if(mCallback != null) {
            if(resultJson != null) {
                boolean isOpenSuccess = resultJson.optBoolean(KEY_RESULT_IS_OPEN_SUCCESS, false);
                mCallback.onNetEnd(new UploadSignatureResult(isOpenSuccess));
            } else {
                mCallback.onNetEnd(null);
            }
        }
    }

    @Override
    protected String getNetUrl() {
        return null;
    }
}

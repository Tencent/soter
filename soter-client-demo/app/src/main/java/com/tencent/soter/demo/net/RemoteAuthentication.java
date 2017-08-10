/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.tencent.soter.demo.net;

import android.support.annotation.NonNull;
import android.util.Base64;

import com.tencent.soter.demo.model.ConstantsSoterDemo;
import com.tencent.soter.demo.model.DemoUtil;
import com.tencent.soter.wrapper.wrap_net.ISoterNetCallback;
import com.tencent.soter.wrapper.wrap_net.IWrapUploadSignature;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by henryye on 2017/5/2.
 * To wrap normal payment or fingerprint payment
 */

public class RemoteAuthentication extends RemoteBase implements IWrapUploadSignature {
    private static final String TAG = "SoterDemo.RemoteAuthentication";

    private static final String SAMPLE_FINAL_JSON_PATH = ConstantsSoterDemo.SAMPLE_EXTERNAL_PATH + "final_json.txt";
    private static final String SAMPLE_FINAL_SIGNATURE_PATH = ConstantsSoterDemo.SAMPLE_EXTERNAL_PATH + "final_signature.bin";
    private static final String SAMPLE_FINAL_SALTLEN_PATH = ConstantsSoterDemo.SAMPLE_EXTERNAL_PATH + "final_salt_len.txt";

    private static final String KEY_REQUEST_SIGNATURE_JSON = "signature_json";
    private static final String KEY_REQUEST_SIGNATURE_DATA = "signature_data";
    private static final String KEY_REQUEST_SIGNATURE_SALT_LEN = "signature_salt_len";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_RESULT_IS_AUTHENTICATED = "is_authenticated";

    private ISoterNetCallback<UploadSignatureResult> mFingerprintPayCallback;
    private IOnNormalPaymentCallback mNormalCallback;

    public RemoteAuthentication() {
        // fingerprint pay. do not provide password
    }

    public RemoteAuthentication(String password, IOnNormalPaymentCallback callback) {
        this.mNormalCallback = callback;
        if (DemoUtil.isNullOrNil(password)) {
            JSONObject requestJson = new JSONObject();
            try {
                requestJson.put(KEY_PASSWORD, password);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            setRequestJson(requestJson);
        }
    }

    @Override
    public void setRequest(@NonNull UploadSignatureRequest requestDataModel) {
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put(KEY_REQUEST_SIGNATURE_JSON, requestDataModel.signatureJson);
            requestJson.put(KEY_REQUEST_SIGNATURE_DATA, requestDataModel.signatureData);
            requestJson.put(KEY_REQUEST_SIGNATURE_SALT_LEN, requestDataModel.signatureSaltLength);
            // save to file as sample. In real projects, you do not have to do it, just as a sample
            if(ConstantsSoterDemo.IS_DEBUG_SAVE_DATA) {
                DemoUtil.saveTextToFile(requestDataModel.signatureJson, SAMPLE_FINAL_JSON_PATH);
                DemoUtil.saveBinaryToFile(Base64.decode(requestDataModel.signatureData, Base64.DEFAULT), SAMPLE_FINAL_SIGNATURE_PATH);
                DemoUtil.saveTextToFile("" + requestDataModel.signatureSaltLength, SAMPLE_FINAL_SALTLEN_PATH);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setRequestJson(requestJson);
    }

    @Override
    public void setCallback(ISoterNetCallback<UploadSignatureResult> callback) {
        this.mFingerprintPayCallback = callback;
    }

    @Override
    JSONObject getSimulateJsonResult(JSONObject requestJson) {
        JSONObject resultJson = new JSONObject();
        try {
            resultJson.put(KEY_RESULT_IS_AUTHENTICATED, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultJson;
    }

    @Override
    void onNetworkEnd(JSONObject resultJson) {
        if (resultJson != null) {
            boolean isVerified = resultJson.optBoolean(KEY_RESULT_IS_AUTHENTICATED, false);
            if(mFingerprintPayCallback != null) {
                mFingerprintPayCallback.onNetEnd(new UploadSignatureResult(isVerified));
            }
            if(mNormalCallback != null) {
                mNormalCallback.onPayEnd(isVerified);
            }
        } else {
            if(mFingerprintPayCallback != null) {
                mFingerprintPayCallback.onNetEnd(null);
            }
            if(mNormalCallback != null) {
                mNormalCallback.onPayEnd(false);
            }
        }
    }

    @Override
    protected String getNetUrl() {
        return "http://simulate.soter_demo/authentication";
    }

    /**
     * Used in non-fingerprint payment
     */
    public interface IOnNormalPaymentCallback {
        void onPayEnd(boolean isSuccess);
    }

}

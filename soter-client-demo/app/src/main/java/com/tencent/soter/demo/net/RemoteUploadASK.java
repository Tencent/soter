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
import com.tencent.soter.wrapper.wrap_net.IWrapUploadKeyNet;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by henryye on 2017/4/27.
 *
 */

public class RemoteUploadASK extends RemoteBase implements IWrapUploadKeyNet {
    private static final String TAG = "SoterDemo.RemoteUploadASK";

    private static final String SAMPLE_ASK_JSON_PATH = ConstantsSoterDemo.SAMPLE_EXTERNAL_PATH + "ask_json.txt";
    private static final String SAMPLE_ASK_SIGNATURE_PATH = ConstantsSoterDemo.SAMPLE_EXTERNAL_PATH + "ask_signature.bin";
    private static final String SAMPLE_ASK_PUBLIC_KEY_PEM_PATH = ConstantsSoterDemo.SAMPLE_EXTERNAL_PATH + "ask_key.pem";

    private static final String KEY_REQUEST_KEY_JSON = "keyJson";
    private static final String KEY_REQUEST_SIGNATURE = "keySignature";
    private static final String KEY_RESULT = "result";

    private ISoterNetCallback<UploadResult> mCallback = null;

    @Override
    JSONObject getSimulateJsonResult(JSONObject requestJson) {
        JSONObject resultJson = new JSONObject();
        try {
            resultJson.put(KEY_RESULT, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultJson;
    }

    @Override
    public void execute() {
        super.execute();
    }

    @Override
    void onNetworkEnd(JSONObject resultJson) {
        if(mCallback != null) {
            if(resultJson == null) {
                mCallback.onNetEnd(null);
            } else {
                boolean isUploadAndVerified = resultJson.optBoolean(KEY_RESULT, false);
                mCallback.onNetEnd(new UploadResult(isUploadAndVerified));
            }
        }

    }

    @Override
    public void setRequest(@NonNull UploadRequest requestDataModel) {
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put(KEY_REQUEST_KEY_JSON, requestDataModel.mKeyJson);
            requestJson.put(KEY_REQUEST_SIGNATURE, requestDataModel.mKeyJsonSignature);
            // save to file as sample. In real projects, you do not have to do it, just as a sample
            if(ConstantsSoterDemo.IS_DEBUG_SAVE_DATA) {
                DemoUtil.saveTextToFile(requestDataModel.mKeyJson, SAMPLE_ASK_JSON_PATH);
                DemoUtil.saveTextToFile(retrievePublicKeyFromJson(requestDataModel.mKeyJson), SAMPLE_ASK_PUBLIC_KEY_PEM_PATH);
                DemoUtil.saveBinaryToFile(Base64.decode(requestDataModel.mKeyJsonSignature, Base64.DEFAULT), SAMPLE_ASK_SIGNATURE_PATH);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setRequestJson(requestJson);
    }

    private String retrievePublicKeyFromJson(String jsonStr) {
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            return jsonObject.getString("pub_key");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setCallback(ISoterNetCallback<UploadResult> callback) {
        this.mCallback = callback;
    }

    @Override
    protected String getNetUrl() {
        return "http://qcloud.simulate.soter_demo/upload_ask_key";
    }
}

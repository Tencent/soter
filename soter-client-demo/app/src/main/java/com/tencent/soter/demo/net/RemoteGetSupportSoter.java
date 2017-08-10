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
import com.tencent.soter.wrapper.wrap_net.IWrapGetSupportNet;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by henryye on 2017/4/25.
 *
 */

public class RemoteGetSupportSoter extends RemoteBase implements IWrapGetSupportNet {
    private static final String TAG = "SoterDemo.RemoteGetSupportSoter";

    private static final String KEY_REQUEST_DEVICE_REQUEST_JSON = "request";
    private static final String KEY_RESULT_IS_SUPPORT = "isSupport";
    private ISoterNetCallback<GetSupportResult> mCallback = null;

    @Override
    public void setRequest(@NonNull GetSupportRequest requestDataModel) {
        JSONObject request = new JSONObject();
        try {
            request.put(KEY_REQUEST_DEVICE_REQUEST_JSON, requestDataModel.requestJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setRequestJson(request);
    }

    @Override
    public void setCallback(ISoterNetCallback<GetSupportResult> callback) {
        mCallback = callback;
    }

    @Override
    JSONObject getSimulateJsonResult(JSONObject requestJson) {
        JSONObject result = new JSONObject();
        try {
            result.put(KEY_RESULT_IS_SUPPORT, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
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
                mCallback.onNetEnd(new GetSupportResult(resultJson.optBoolean(KEY_RESULT_IS_SUPPORT, false)));
            }
        }
    }

    @Override
    protected String getNetUrl() {
        return "http://simulate.soter_demo/get_is_support";
    }
}

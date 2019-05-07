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
import com.tencent.soter.wrapper.wrap_net.IWrapGetChallengeStr;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by henryye on 2017/4/27.
 *
 */

public class RemoteGetChallengeStr extends RemoteBase implements IWrapGetChallengeStr{
    private static final String TAG = "SoterDemo.RemoteGetChallengeStr";

    private static final String KEY_RESULT_CHALLENGE = "challengeStr";
    private static final String DEMO_CHALLENGE = "I'm a demo challenge string";

    private ISoterNetCallback<GetChallengeResult> mCallback = null;

    @Override
    public void setRequest(@NonNull GetChallengeRequest requestDataModel) {
        JSONObject requestJson = new JSONObject();
        // nothing to set
        setRequestJson(requestJson);
    }

    @Override
    public void setCallback(ISoterNetCallback<GetChallengeResult> callback) {
        this.mCallback = callback;
    }

    @Override
    JSONObject getSimulateJsonResult(JSONObject requestJson) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(KEY_RESULT_CHALLENGE, DEMO_CHALLENGE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    public void execute() {
        super.execute();
    }

    @Override
    void onNetworkEnd(JSONObject resultJson) {
        if(mCallback != null) {
            if(resultJson != null) {
                String challenge = resultJson.optString(KEY_RESULT_CHALLENGE);
                this.mCallback.onNetEnd(new GetChallengeResult(challenge));
            } else {
                this.mCallback.onNetEnd(null);
            }
        }

    }

    @Override
    protected String getNetUrl() {
        return "http://simulate.soter_demo/get_challenge";
    }
}

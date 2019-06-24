/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.tencent.soter.demo.net;

import com.tencent.soter.demo.model.DemoLogger;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by henryye on 2017/4/25.
 * 网络封装结构体。注意，真实项目中，请使用真正的网络实现
 */

abstract class RemoteBase {
    private static final String TAG = "SoterDemo.RemoteBase";

    private JSONObject mRequestJson = null;
    private JSONObject mResultJson = null;

    private static final long SIMULATE_NETWORK_DELAY = 1000;
    protected static final String BASE_URL = "http://simulate.soter_demo";
    public void execute() {
        DemoLogger.i(TAG, "soterdemo: simulate execute");
        JSONObject result = getSimulateJsonResult(mRequestJson);
        if(result == null) {
            DemoLogger.w(TAG, "soterdemo: %s no result. regard as network error", getClass().getSimpleName());
            mResultJson = null;
        } else {
            mResultJson = getSimulateJsonResult(mRequestJson);
        }
        String url = getNetUrl();
        DemoLogger.i(TAG, "soterdemo: url is: %s, request: %s", url, mRequestJson);
        // 模拟网络操作
        DemoNetworkThread.getInstance().postTaskDelayed(new Runnable() {
            @Override
            public void run() {
                onNetworkEnd(mResultJson);
            }
        }, SIMULATE_NETWORK_DELAY);
    }

    protected void setRequestJson(JSONObject requestJson) {
        if(requestJson != null) {
            mRequestJson = requestJson;
        } else {
            DemoLogger.w(TAG, "%s invalid request", getClass().getSimpleName());
            mRequestJson = null;
        }
    }

    // 目前使用的模拟网络回包模式，因此需要每一个网络类型指定本次请求的模拟回包。真正的项目中，请使用真实的网络回包数据
    abstract JSONObject getSimulateJsonResult(JSONObject requestJson);

    abstract void onNetworkEnd(JSONObject resultJson);

    abstract protected String getNetUrl();
}

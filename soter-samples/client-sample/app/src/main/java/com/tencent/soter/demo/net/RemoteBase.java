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
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

/**
 * Created by henryye on 2017/4/25.
 * 网络封装结构体。注意，真实项目中，请使用真正的网络实现
 */

abstract class RemoteBase {
    private static final String TAG = "SoterDemo.RemoteBase";

    private JSONObject mRequestJson = null;
    private JSONObject mResultJson = null;

    private static final long SIMULATE_NETWORK_DELAY = 1000;
    protected static final String BASE_URL = "https://www.grouppic.cn/soter";
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
                //post的方式提交
                try {
                    String path = getNetUrl();
                    URL url = new URL(path);

                    HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });
                    SSLContext context = SSLContext.getInstance("TLS");
                    context.init(null, new X509TrustManager[]{new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] chain,
                                                       String authType) throws CertificateException {
                        }

                        public void checkServerTrusted(X509Certificate[] chain,
                                                       String authType) throws CertificateException {
                        }

                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }}, new SecureRandom());
                    HttpsURLConnection.setDefaultSSLSocketFactory(
                            context.getSocketFactory());

                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    connection.setConnectTimeout(5000);
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.connect();
                    connection.getOutputStream().write((mRequestJson.toString()).getBytes("UTF-8"));
                    //获得结果码
                    int responseCode = connection.getResponseCode();
                    String response = null;
                    if(responseCode ==200){
                        //请求成功 获得返回的流
                        InputStream is = connection.getInputStream();
                        response = IOUtils.toString(is, "UTF-8");
                        IOUtils.closeQuietly(is);
                    }else {
                        //请求失败 获得返回的流
                        InputStream is = connection.getInputStream();
                        response = IOUtils.toString(is, "UTF-8");
                        DemoLogger.i(TAG, "soterdemo: remote request returned. url is: %s, response is: %s", path, response);
                        IOUtils.closeQuietly(is);
                    }
                    DemoLogger.i(TAG, "soterdemo: remote request returned. url is: %s, response is: %s", path, response);
                    JSONObject resultJson = new JSONObject(response);
                    mResultJson = resultJson.getJSONObject("data");
                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

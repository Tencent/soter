/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.tencent.soter.demo;

import android.app.Application;
import android.support.annotation.NonNull;

import com.tencent.soter.demo.model.ConstantsSoterDemo;
import com.tencent.soter.demo.model.DemoLogger;
import com.tencent.soter.demo.model.SoterDemoData;
import com.tencent.soter.demo.net.RemoteGetSupportSoter;
import com.tencent.soter.wrapper.SoterWrapperApi;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessCallback;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessNoExtResult;
import com.tencent.soter.wrapper.wrap_task.InitializeParam;

/**
 * Created by henryye on 2017/4/25.
 *
 */

public class SoterDemoApplication extends Application{
    private static final String TAG = "SoterDemo.SoterDemoApplication";

    private SoterProcessCallback<SoterProcessNoExtResult> mGetIsSupportCallback = new SoterProcessCallback<SoterProcessNoExtResult>() {
        @Override
        public void onResult(@NonNull SoterProcessNoExtResult result) {
            DemoLogger.d(TAG, "soterdemo: get is support soter done. result: %s", result.toString());
            // 建议尽早准备ASK。主要有两个时机：1. 进程初始化时 2. 第一次使用业务任何一个业务时。这里在程序进程初始化的时候准备 ASK
//            if(result.errCode == SoterProcessErrCode.ERR_OK && SoterWrapperApi.isSupportSoter()) {
//                prepareASK();
//            }
            // Edit 2017.11.27
            // 不再建议提前生成ASK，可能会拖慢启动。同时极少量机型有兼容性问题，提前生成ASK可能会导致不可预见错误
        }
    };

    // 建议在 SoterDemoApplication 中获取应用是否支持 SOTER
    // 如果不立刻进行网络操作，可以暂时不设置网络回调，这样 SOTER将不会联网检查是否支持SOTER
    @Override
    public void onCreate() {
        super.onCreate();
        initSoterSupport();
        // 模拟获取开通状态
        SoterDemoData.getInstance().init(getApplicationContext());
    }

    private void initSoterSupport() {
        // init 的时机有多种情况，应用方可以自己选择。如果多账号共享同一个密钥的话，则只需要在 Application 的 onCreate 中初始化；如果需要切换账户，则在账户登录成功之后初始化。如果需要
        // 不同账户不共享密钥，则需要填入区分不同账户的字符串 setDistinguishSalt，比如账户名。正常情况下，没有特殊需求，不用调用 setCustomAppSecureKeyName。此接口仅仅作为前期已经接入
        // 如果有自己的log实现，可以将实现通过 setSoterLogger 接口写入
        InitializeParam param = new InitializeParam.InitializeParamBuilder().setGetSupportNetWrapper(new RemoteGetSupportSoter()).setScenes(ConstantsSoterDemo.SCENE_PAYMENT)

//                .setCustomAppSecureKeyName("Wechat_demo_ask")
//                .setDistinguishSalt("demo_salt_account_1")
//                .setSoterLogger(new ISoterLogger() {...})
                .build();
        SoterWrapperApi.init(getApplicationContext(), mGetIsSupportCallback, param);
    }

    // 虚拟机专用，不要过度依赖
    @Override
    public void onTerminate() {
        super.onTerminate();
        SoterWrapperApi.tryStopAllSoterTask();
    }
}

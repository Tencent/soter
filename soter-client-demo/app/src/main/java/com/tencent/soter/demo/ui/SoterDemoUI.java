/*
 * Tencent is pleased to support the open source community by making TENCENT SOTER available.
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * https://opensource.org/licenses/BSD-3-Clause
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.tencent.soter.demo.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.soter.core.SoterCore;
import com.tencent.soter.demo.R;
import com.tencent.soter.demo.model.ConstantsSoterDemo;
import com.tencent.soter.demo.model.DemoLogger;
import com.tencent.soter.demo.model.DemoUtil;
import com.tencent.soter.demo.model.SoterDemoData;
import com.tencent.soter.demo.net.RemoteAuthentication;
import com.tencent.soter.demo.net.RemoteGetChallengeStr;
import com.tencent.soter.demo.net.RemoteOpenFingerprintPay;
import com.tencent.soter.demo.net.RemoteUploadASK;
import com.tencent.soter.demo.net.RemoteUploadPayAuthKey;
import com.tencent.soter.wrapper.SoterWrapperApi;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessAuthenticationResult;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessCallback;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessKeyPreparationResult;
import com.tencent.soter.wrapper.wrap_core.SoterProcessErrCode;
import com.tencent.soter.wrapper.wrap_fingerprint.SoterFingerprintCanceller;
import com.tencent.soter.wrapper.wrap_fingerprint.SoterFingerprintStateCallback;
import com.tencent.soter.wrapper.wrap_net.IWrapUploadKeyNet;
import com.tencent.soter.wrapper.wrap_net.IWrapUploadSignature;
import com.tencent.soter.wrapper.wrap_task.AuthenticationParam;
import com.tencent.soter.wrapper.wrap_task.InitializeParam;

/**
 * Sample中重点演示了一个场景：指纹支付。如果需要添加其他场景，请在
 * {@link com.tencent.soter.wrapper.SoterWrapperApi#init(Context, SoterProcessCallback, InitializeParam)}
 * 中注册更多场景值
 * 注意，演示功能仅仅用于示范SOTER接口使用方法，涉及到的网络操作等
 * 均未使用真实网络环境，具体业务细节、视觉交互细节也做了大量简化，除了SOTER相关流程之外，其余流程切勿直接模仿
 */
public class SoterDemoUI extends AppCompatActivity {
    private static final String TAG = "SoterDemo.SoterDemoUI";

    private Button mOpenOrCloseFingerprintPayment = null;
    private Button mUseFingerprintPay = null;

    private Dialog mPasswordDialog = null;
    private Dialog mFingerprintDialog = null;
    private ProgressDialog mLoadingDialog = null;
    private View mCustomFingerprintView = null;
    private TextView mFingerprintStatusHintView = null;

    private SoterFingerprintCanceller mCanceller = null;
    private Animation mFlashAnimation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        configLogic();
        prepareData();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    private void prepareData() {
        mFlashAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_flash);
    }

    private void configLogic() {
        mOpenOrCloseFingerprintPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SoterDemoData.getInstance().getIsFingerprintPayOpened()) {
                    doCloseFingerprintPayment();
                } else {
                    doOpenFingerprintPayment();
                }
            }
        });

        mUseFingerprintPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doUseFingerprintPayment();
            }
        });
    }

    /**
     * 关闭一项业务的时候，除了业务状态之外，切记删除掉本机密钥，以及后台将原本密钥删除或者标记为不可用
     */
    private void doCloseFingerprintPayment() {
        DemoLogger.i(TAG, "soterdemo: start close fingerprint pay");
        new AlertDialog.Builder(this).setTitle("").setMessage(getString(R.string.app_confirm_close)).setCancelable(true)
                .setOnCancelListener(null).setPositiveButton(getString(R.string.app_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SoterWrapperApi.removeAuthKeyByScene(ConstantsSoterDemo.SCENE_PAYMENT);
                SoterDemoData.getInstance().setIsFingerprintPayOpened(SoterDemoUI.this, false);
                updateUseFingerprintPayBtnStatus();
            }
        }).setNegativeButton(getString(R.string.app_cancel), null).show();
    }

    /**
     * 这里介绍了开启指纹业务的一个典型场景：开启指纹支付。共分为如下两个流程：
     * 1. 准备密钥（authKey，如果ASK也没有的话需要重新生成ASK）。对应{@link SoterWrapperApi#prepareAuthKey(SoterProcessCallback, boolean, boolean, int, IWrapUploadKeyNet, IWrapUploadKeyNet)}
     * 2. 指纹识别。对应{@link SoterWrapperApi#requestAuthorizeAndSign(SoterProcessCallback, AuthenticationParam)} (Context, SoterProcessCallback, int, IWrapGetChallengeStr, IWrapUploadSignature, SoterFingerprintStateCallback, SoterFingerprintCanceller)
     * 建议直接使用SoterWrapper实现逻辑，避免直接使用SoterCore接口。
     */
    private void doOpenFingerprintPayment() {
        doPrepareAuthKey(new IOnAuthKeyPrepared() {
            @Override
            public void onResult(String pwdDigestUsed, boolean isSuccess) {
                if (isSuccess) {

                    startFingerprintAuthentication(new SoterProcessCallback<SoterProcessAuthenticationResult>() {
                        @Override
                        public void onResult(@NonNull SoterProcessAuthenticationResult result) {
                            DemoLogger.i(TAG, "soterdemo: open finished: result: %s, signature data is: %s", result.toString(), result.getExtData() != null ? result.getExtData().toString() : null);
                            dismissCurrentDialog();
                            dismissLoading();
                            if (result.isSuccess()) {
                                SoterDemoData.getInstance().setIsFingerprintPayOpened(SoterDemoUI.this, true);
                                updateUseFingerprintPayBtnStatus();
                                Toast.makeText(SoterDemoUI.this, "open success", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SoterDemoUI.this, String.format("open failed, reason: %s", result.toString()), Toast.LENGTH_LONG).show();
                            }
                        }
                    }, getString(R.string.app_open_fingerprint_pay), new RemoteOpenFingerprintPay(pwdDigestUsed));
                } else {
                    DemoLogger.w(TAG, "soterdemo: generate auth key failed!");
                    dismissLoading();
                    Toast.makeText(SoterDemoUI.this, getString(R.string.app_auth_key_prepare_failed), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void doPrepareAuthKey(final IOnAuthKeyPrepared onAuthKeyPreparedCallback) {
        showPasswordInputDialog(new IOnConfirmedPassword() {
            @Override
            public void onConfirmPassword(final String pwdDigest) {
                // We strongly recommend you to use password to open any fingerprint related business scene.
                // e.g., payment password/pin to open fingerprint
                showLoading(getString(R.string.app_loading_preparing_open_keys));
                prepareAuthKey(pwdDigest, onAuthKeyPreparedCallback);
            }
        }, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                DemoLogger.i(TAG, "soterdemo: user cancelled open in input");
            }
        });
    }

    private void prepareAuthKey(final String pwdDigest, final IOnAuthKeyPrepared callback) {
        SoterWrapperApi.prepareAuthKey(new SoterProcessCallback<SoterProcessKeyPreparationResult>() {
            @Override
            public void onResult(@NonNull SoterProcessKeyPreparationResult result) {
                DemoLogger.i(TAG, "soterdemo: prepare result: %s, auth key result: %s", result, result.getExtData() != null ? result.getExtData().toString() : null);
                if (result.errCode == SoterProcessErrCode.ERR_OK) {
                    if (callback != null) {
                        callback.onResult(pwdDigest, true);
                    }
                } else {
                    if (callback != null) {
                        callback.onResult(pwdDigest, false);
                    }
                }
            }
        }, false, true, ConstantsSoterDemo.SCENE_PAYMENT, new RemoteUploadPayAuthKey(pwdDigest), new RemoteUploadASK());
    }

    /**
     * 进行指纹业务使用的时候，有一个必须要注意的点，即密钥失效的问题。在Android 6.0以及以上的设备中，如果用户录入了一个新指纹，
     * 则原本用于指纹验证的auth key会失效，此时需要将原本的authkey清除，并重新生成、上传authkey。同时，强烈建议本次认证业务（支付）直接使用密码即可，不用取消认证或者重新使用指纹认证。
     *
     * 同时，需要特殊处理指纹认证失败次数过多的情况。在SOTER中，指纹失败次数过多（一般来说是5次以上）的情况下，指纹传感器会暂时冻结。此时需要及时转换为降级方案进行认证，如支付时转化为密码认证。
     */
    private void doUseFingerprintPayment() {
        DemoLogger.i(TAG, "soterdemo: user request use fingerprint payment");
        startFingerprintAuthentication(new SoterProcessCallback<SoterProcessAuthenticationResult>() {
            @Override
            public void onResult(@NonNull SoterProcessAuthenticationResult result) {
                DemoLogger.d(TAG, "soterdemo: use fingerprint payment result: %s, signature data is: %s", result.toString(), result.getExtData() != null ? result.getExtData().toString() : null);
                dismissLoading();
                if (result.isSuccess()) {
                    Toast.makeText(SoterDemoUI.this, "authenticate success!", Toast.LENGTH_SHORT).show();
                } else {
                    // 先判断是否是指纹密钥失效。如果指纹失效，则重新生成并上传authkey，然后直接使用密码支付
                    if (result.errCode == SoterProcessErrCode.ERR_AUTHKEY_NOT_FOUND
                            || result.errCode == SoterProcessErrCode.ERR_AUTHKEY_ALREADY_EXPIRED || result.errCode == SoterProcessErrCode.ERR_ASK_NOT_EXIST
                            || result.errCode == SoterProcessErrCode.ERR_SIGNATURE_INVALID) {
                        DemoLogger.w(TAG, "soterdemo: auth key expired or keys not found. regen and upload");
                        Toast.makeText(SoterDemoUI.this, "authkey expired or not found. start re-generate",
                                Toast.LENGTH_SHORT).show();
                        startPrepareAuthKeyAndAuthenticate();
                    } else if (result.errCode == SoterProcessErrCode.ERR_USER_CANCELLED) {
                        DemoLogger.i(TAG, "soterdemo: user cancelled the authentication");
                        Toast.makeText(SoterDemoUI.this, "user cancelled the payment",
                                Toast.LENGTH_SHORT).show();
                    } else if (result.errCode == SoterProcessErrCode.ERR_FINGERPRINT_LOCKED) {
                        DemoLogger.i(TAG, "soterdemo: fingerprint sensor is locked because of too many failed trials. fall back to password payment");
                        Toast.makeText(SoterDemoUI.this, "fingerprint sensor is locked because of too many failed trials. fall back to password payment",
                                Toast.LENGTH_SHORT).show();
                        startNormalPasswordAuthentication();
                    } else {
                        DemoLogger.w(TAG, "soterdemo: unknown error in doUseFingerprintPayment : %d", result.errCode);
                        Toast.makeText(SoterDemoUI.this, "payment error. check log for more information. fallback to normal",
                                Toast.LENGTH_SHORT).show();
                        startNormalPasswordAuthentication();
                    }
                }
            }
        }, getString(R.string.app_use_fingerprint_pay), new RemoteAuthentication());
    }

    private void startPrepareAuthKeyAndAuthenticate() {
        doPrepareAuthKey(new IOnAuthKeyPrepared() {
            @Override
            public void onResult(String passwordDigestUsed, boolean isSuccess) {
                if (isSuccess) {
                    DemoLogger.i(TAG, "soterdemo: prepare authkey success! do authentication directly by password");
                    // 重新生成并上传authkey成功，直接使用密码认证
                    RemoteAuthentication normalPasswordAuthentication = new RemoteAuthentication(passwordDigestUsed, new RemoteAuthentication.IOnNormalPaymentCallback() {
                        @Override
                        public void onPayEnd(boolean isSuccess) {
                            dismissLoading();
                            if (isSuccess) {
                                Toast.makeText(SoterDemoUI.this, "authenticate success with password!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SoterDemoUI.this, "payment error. check log for more information",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    normalPasswordAuthentication.execute();
                } else {
                    DemoLogger.w(TAG, "soterdemo: prepare authkey failed. check log to find more information");
                }
            }
        });
    }

    private void startNormalPasswordAuthentication() {
        showPasswordInputDialog(new IOnConfirmedPassword() {
            @Override
            public void onConfirmPassword(final String pwdDigest) {
                showLoading(getString(R.string.app_verifying));
                RemoteAuthentication normalPasswordAuthentication = new RemoteAuthentication(pwdDigest, new RemoteAuthentication.IOnNormalPaymentCallback() {
                    @Override
                    public void onPayEnd(boolean isSuccess) {
                        dismissLoading();
                        if (isSuccess) {
                            Toast.makeText(SoterDemoUI.this, "authenticate success with password!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SoterDemoUI.this, "payment error. check log for more information",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                normalPasswordAuthentication.execute();
            }
        }, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                DemoLogger.i(TAG, "soterdemo: user cancelled open in input");
            }
        });
    }


    // Simulate only. In real business scenes, you should check password format first
    private void showPasswordInputDialog(final IOnConfirmedPassword onConfirm, DialogInterface.OnCancelListener onCancel) {
        dismissCurrentDialog();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.input_pay_password));
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        builder.setView(input);
        builder.setPositiveButton(getString(R.string.app_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (onConfirm != null) {
                    String pwdText = input.getText().toString();
                    onConfirm.onConfirmPassword(!DemoUtil.isNullOrNil(pwdText)
                            ? DemoUtil.calcPwdDigest(pwdText) : null);
                }
            }
        });
        builder.setOnCancelListener(onCancel);
        mPasswordDialog = builder.show();
    }

    private void startFingerprintAuthentication(SoterProcessCallback<SoterProcessAuthenticationResult> processCallback,
                                                final String title, IWrapUploadSignature uploadSignatureWrapper) {
        DemoLogger.i(TAG, "soterdemo: start authentication: title: %s", title);
        dismissCurrentDialog();
        if (mCanceller != null) {
            DemoLogger.w(TAG, "soterdemo: last canceller is not null. should not happen because we will set it to null every time we finished the process");
            mCanceller = null;
        }
        mCanceller = new SoterFingerprintCanceller();
        // 认证逻辑部分
        showLoading(getString(R.string.app_request_challenge));
        // Prepare authentication parameters
        AuthenticationParam param = new AuthenticationParam.AuthenticationParamBuilder() // 通过Builder来构建认证请求
                .setScene(ConstantsSoterDemo.SCENE_PAYMENT) // 指定需要认证的场景。必须在init中初始化。必填
                .setContext(this) // 指定当前上下文。必填。
                .setFingerprintCanceller(mCanceller) // 指定当前用于控制指纹取消的控制器。当因为用户退出界面或者进行其他可能引起取消的操作时，需要开发者通过该控制器取消指纹授权。建议必填。
                .setIWrapGetChallengeStr(new RemoteGetChallengeStr()) // 用于获取挑战因子的网络封装结构体。如果在授权之前已经通过其他模块拿到后台挑战因子，则可以改为调用setPrefilledChallenge。如果两个方法都没有调用，则会引起错误。
//                .setPrefilledChallenge("prefilled challenge") // 如果之前已经通过其他方式获取了挑战因子，则设置此字段。如果设置了该字段，则忽略获取挑战因子网络封装结构体的设置。如果两个方法都没有调用，则会引起错误。
                .setIWrapUploadSignature(uploadSignatureWrapper) // 用于上传最终结果的网络封装结构体。该结构体一般来说不独立存在，而是集成在最终授权网络请求中，该请求实现相关接口即可。选填，如果没有填写该字段，则要求应用方自行上传该请求返回字段。
                .setSoterFingerprintStateCallback(new SoterFingerprintStateCallback() { // 指纹回调仅仅用来更新UI相关，不建议在指纹回调中进行任何业务操作。选填。

                    // 指纹回调仅仅用来更新UI相关，不建议在指纹回调中进行任何业务操作
                    // Fingerprint state callbacks are only used for updating UI. Any logic operation is not welcomed.
                    @Override
                    public void onStartAuthentication() {
                        DemoLogger.d(TAG, "soterdemo: start authentication. dismiss loading");
                        dismissLoading();
                        showFingerprintDialog(title);
                    }

                    @Override
                    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                        DemoLogger.w(TAG, "soterdemo: onAuthenticationHelp: %d, %s", helpCode, helpString);
                        // 由于厂商实现不同，不建议在onAuthenticationHelp中做任何操作。
                    }

                    @Override
                    public void onAuthenticationSucceed() {
                        DemoLogger.d(TAG, "soterdemo: onAuthenticationSucceed");
                        mCanceller = null;
                        // 可以在这里做相应的UI操作
                        showLoading(getString(R.string.app_verifying));
                        dismissCurrentDialog();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        DemoLogger.w(TAG, "soterdemo: onAuthenticationFailed once:");
                        setFingerprintHintMsg(getString(R.string.fingerprint_normal_hint), true);
                    }

                    @Override
                    public void onAuthenticationCancelled() {
                        DemoLogger.d(TAG, "soterdemo: user cancelled authentication");
                        mCanceller = null;
                        dismissCurrentDialog();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errorString) {
                        DemoLogger.w(TAG, "soterdemo: onAuthenticationError: %d, %s", errorCode, errorString);
                        mCanceller = null;
                        Toast.makeText(SoterDemoUI.this, errorString, Toast.LENGTH_LONG).show();
                        dismissCurrentDialog();
                    }
                }).build();
        SoterWrapperApi.requestAuthorizeAndSign(processCallback, param);
    }

    private void showFingerprintDialog(String title) {
        if (mFingerprintDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SoterDemoUI.this).setTitle(title).setCancelable(true)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            cancelFingerprintAuthentication();
                            dismissCurrentDialog();
                        }
                    }).setNegativeButton(getString(R.string.app_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelFingerprintAuthentication();
                            dismissCurrentDialog();
                        }
                    }).setView(mCustomFingerprintView);
            mFingerprintDialog = builder.create();
        } else {
            setFingerprintHintMsg("", false);
            mFingerprintDialog.setTitle(title);
        }
        mFingerprintDialog.show();
    }

    private void setFingerprintHintMsg(String msg, boolean isFlash) {
        if (mCustomFingerprintView != null) {
            mFingerprintStatusHintView.setText(msg);
            if (isFlash) {
                mFingerprintStatusHintView.startAnimation(mFlashAnimation);
            }
        }
    }

    private void dismissCurrentDialog() {
        if (mPasswordDialog != null && mPasswordDialog.isShowing()) {
            mPasswordDialog.dismiss();
        }
        if (mFingerprintDialog != null && mFingerprintDialog.isShowing()) {
            mFingerprintDialog.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 确保在onPause的时候结束指纹监听，以免影响其他模块以及应用
        cancelFingerprintAuthentication();
        // 建议在onPause的时候结束掉SOTER相关事件。当然，也可以选择自己管理，但是会更加复杂
        SoterWrapperApi.tryStopAllSoterTask();
        dismissCurrentDialog();
        dismissLoading();
    }

    private void cancelFingerprintAuthentication() {
        if (mCanceller != null) {
            mCanceller.asyncCancelFingerprintAuthentication();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUseFingerprintPayBtnStatus();
    }

    private void updateUseFingerprintPayBtnStatus() {
        if (SoterDemoData.getInstance().getIsFingerprintPayOpened()) {
            mUseFingerprintPay.setEnabled(true);
        } else {
            mUseFingerprintPay.setEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.del_ask:
                SoterCore.removeAppGlobalSecureKey();
                return true;
            case R.id.del_auth_key:
                SoterWrapperApi.removeAuthKeyByScene(ConstantsSoterDemo.SCENE_PAYMENT);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("InflateParams")
    private void initViews() {
        mOpenOrCloseFingerprintPayment = (Button) findViewById(R.id.action_open_or_close_fp_pay);
        mUseFingerprintPay = (Button) findViewById(R.id.action_use_fp_pay);
        mCustomFingerprintView = LayoutInflater.from(this).inflate(R.layout.fingerprint_layout, null);
        mFingerprintStatusHintView = (TextView) mCustomFingerprintView.findViewById(R.id.error_hint_msg);
    }

    private void showLoading(String wording) {
        if (mLoadingDialog == null) {
            mLoadingDialog = ProgressDialog.show(this, "", wording, true, false, null);
        } else if (!mLoadingDialog.isShowing()) {
            mLoadingDialog.setMessage(wording);
            mLoadingDialog.show();
        } else {
            DemoLogger.d(TAG, "soterdemo: already showing. change title only");
            mLoadingDialog.setMessage(wording);
        }
    }

    private void dismissLoading() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    private interface IOnConfirmedPassword {
        void onConfirmPassword(String pwdDigest);
    }

    private interface IOnAuthKeyPrepared {
        void onResult(String passwordDigestUsed, boolean isSuccess);
    }

}

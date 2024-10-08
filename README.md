# Hello TENCENT SOTER

[![license](http://img.shields.io/badge/license-BSD3-brightgreen.svg?style=flat)](https://github.com/Tencent/soter/blob/master/LICENSE)
[![](https://jitpack.io/v/Tencent/soter.svg)](https://jitpack.io/#Tencent/soter)
[![WeChat Approved](https://img.shields.io/badge/Wechat_Approved-2.0.0-red.svg)](https://github.com/Tencent/soter/wiki) 
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/Tencent/soter/pulls)

For English version, please click [here](#a-quick-look-at-tencent-soter).
## 2.1.8
* 修复一些已知问题
## 2.0.7
* 发布aar到jitpack，新的依赖方法看[这里](https://github.com/Tencent/soter#%E6%B7%BB%E5%8A%A0gradle%E4%BE%9D%E8%B5%96)
## 2.0版本变化
* 增加对华为设备的支持。
* 增加对Android9.0的支持。
* 增加人脸识别功能。
* 后台ASK的解析有少量变动，看[这里](https://github.com/Tencent/soter/wiki/%E5%AE%89%E5%85%A8%E6%8E%A5%E5%85%A5%E2%80%94%E2%80%94%E5%90%8E%E5%8F%B0)。
* 后台is_support接口传参有变更，看[这里](https://github.com/Tencent/soter/wiki/%E5%90%8E%E5%8F%B0%E6%8E%A5%E5%8F%A3%E6%96%87%E6%A1%A3)。

## TENCENT SOTER简介

TENCENT SOTER是腾讯于2015年开始制定的生物认证平台与标准，通过与厂商合作，目前已经在一百余款、数亿部Android设备上得到支持，并且这个数字还在快速增长。

目前，TENCENT SOTER已经在微信指纹支付、微信公众号/小程序指纹授权接口等场景使用，并得到了验证。

接入TENCENT SOTER，你可以在**不获取用户指纹图案的前提下**，在Android设备上实现可信的指纹认证，获得与微信指纹支付一致的安全快捷认证体验。

![SoterFramework](https://github.com/WeMobileDev/article/blob/master/assets/soter/SoterFramework.png)



## 快速接入

可以在几行代码之内快速体验TENCENT SOTER完成指纹授权接口。

在使用之前，请确保所使用的测试机在[支持机型列表](http://mp.weixin.qq.com/s/IRI-RCGsVB2WiPwUCGcytA)中。

### 添加gradle依赖

在项目的`build.gradle`中，添加TENCENT SOTER依赖

```groovy
repositories {
    ...
    maven {
            url "https://jitpack.io"
    }
    ...
}

dependencies {
    ...
    implementation 'com.github.Tencent.soter:soter-wrapper:2.0.7'
    ...
}
```

### 声明权限

在 `AndroidManifest.xml`中添加使用指纹权限

```xml
    <queries>
    <package android:name="com.tencent.soter.soterserver" />
    </queries>

    <uses-permission android:name="android.permission.USE_FINGERPRINT" />
    <uses-permission android:name="android.permission.USE_BIOMETRIC" />
```

### 初始化

初始化过程整个应用声明周期内只需要进行一次，用于生成基本配置和检查设备支持情况。你可以选择在`Application`的onCreate()中，或者在使用TENCENT SOTER之前进行初始化。

```java
InitializeParam param = new InitializeParam.InitializeParamBuilder()
.setScenes(0) // 场景值常量，后续使用该常量进行密钥生成或指纹认证
.build();
SoterWrapperApi.init(context, 
new SoterProcessCallback<SoterProcessNoExtResult>() {...}, 
param);
```

### 准备密钥

需要在使用指纹认证之前生成相关密钥

```java
SoterWrapperApi.prepareAuthKey(new SoterProcessCallback<SoterProcessKeyPreparationResult>() {...},false, true, 0, null, null);
```

### 进行指纹/人脸认证

密钥生成完毕之后，可以使用封装接口调用指纹传感器进行认证。

```java
AuthenticationParam param = new AuthenticationParam.AuthenticationParamBuilder()
                                    .setScene(0)
                                    .setContext(MainActivity.this)
    								// fingerprint
    								.setBiometricType(ConstantsSoter.FINGERPRINT_AUTH)
    								// faceid
    								//.setBiometricType(ConstantsSoter.FACEID_AUTH)
    								.setSoterBiometricCanceller(mSoterBiometricCanceller)
                                    .setPrefilledChallenge("test challenge")
                                    .setSoterBiometricStateCallback(new 			SoterBiometricStateCallback() {...}).build();
SoterWrapperApi.requestAuthorizeAndSign(new SoterProcessCallback<SoterProcessAuthenticationResult>() {...}, param);
```

### 释放

当你不再使用TENCENT SOTER时，可以选择释放所有资源，用于停止所有生成、上传任务以及支持状态等。释放之后再次使用时，需要重新进行初始化。 实际上，TENCENT SOTER本身不会占据过多资源，只需要在确认不会再次使用的前提下（如切换账户之前）释放一次即可。

```java
SoterWrapperApi.release();
```

## 更多文档

* 想了解TENCENT SOTER更多信息与原理？看[这里](https://github.com/Tencent/soter/wiki)。

* 想要更高的安全性，用于登录甚至支付场景中？看[这里](https://github.com/Tencent/soter/wiki/%E5%AE%89%E5%85%A8%E6%8E%A5%E5%85%A5)。

  

## 联系我们

如有相关问题，可以在[issues](https://github.com/Tencent/soter/issues)中提问。

为了方便大家交流，也可以加入下面的QQ群，讨论相关技术问题：

![qqgroup_qrcode](https://github.com/WeMobileDev/article/blob/master/assets/soter/SOTER%E4%BA%A4%E6%B5%81%E7%BE%A4%E7%BE%A4%E4%BA%8C%E7%BB%B4%E7%A0%81.png)

## 贡献代码

我们欢迎开发者贡献代码丰富TENCENT SOTER应用，请参考[这个文档](./CONTRIBUTING.md)。

## 协议

TENCENT SOTER基于BSD协议。请参考[协议文档](./LICENSE)。

## 参与贡献

[腾讯开源激励计划](https://opensource.tencent.com/contribution) 鼓励开发者的参与和贡献，期待你的加入。

## 合规使用指南

[SOTER Client SDK合规使用指南](https://github.com/Tencent/soter/wiki/SOTER-Client-SDK%E5%90%88%E8%A7%84%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97)

## 信息公示

* SDK名称：SOTER Client SDK
* 版本号：2.1.8
* 开发者：深圳市腾讯计算机系统有限公司
* 主要功能：TENCENT SOTER是一套安全、通用、完整的指纹认证方案，通过接入SOTER，开发者可以快速实现安全的指纹认证。
* [SOTER Client SDK使用说明](https://github.com/Tencent/soter/wiki)
* [SOTER Client SDK个人信息保护规则](https://github.com/Tencent/soter/wiki/SOTER-Client-SDK%E4%B8%AA%E4%BA%BA%E4%BF%A1%E6%81%AF%E4%BF%9D%E6%8A%A4%E8%A7%84%E5%88%99?from_wecom=1)

## Changelog
* Adding support for Huawei device
* Adding support for Android P
* Adding FaceId function
* A few changes in backend parsing ASK，Check [this](https://github.com/Tencent/soter/wiki/%E5%AE%89%E5%85%A8%E6%8E%A5%E5%85%A5%E2%80%94%E2%80%94%E5%90%8E%E5%8F%B0).

## A Quick Look at TENCENT SOTER

TENCENT SOTER is a biometric standard as well as a platform held by Tencent. 

There are more than 100 models, hundreds of millions Android devices supporting TENCENT SOTER, and the number is still increasing fast. 

TENCENT SOTER has been already used in scenarios like WeChat fingerprint payment, fingerprint authentication in Official Account Webpages and Mini Programs.

You can get a consistent experience in fingerprint authenticating in your application, like what it is like in WeChat Payment, by getting access to TENCENT SOTER. 

![SoterFramework](https://github.com/WeMobileDev/article/blob/master/assets/soter/SoterFramework.png)



## Quick Start

You can get access to TENCENT SOTER in few lines of code to quick experience.

You should make sure your device for testing is in [support list](http://mp.weixin.qq.com/s/IRI-RCGsVB2WiPwUCGcytA).

### Add Gradle Dependency

Add TENCENT SOTER dependency in your project's `build.gradle`

```groovy

repositories {
    ...
    maven {
            url "https://dl.bintray.com/tencent-soter/maven/"
    }
    ...
}

dependencies {
    ...
    compile 'com.tencent.soter:soter-wrapper:2.0.0'
    ...
}
```

### Declare Permission

Add fingerprint permission declaration in `AndroidManifest.xml`

```xml
    <queries>
    <package android:name="com.tencent.soter.soterserver" />
    </queries>

    <uses-permission android:name="android.permission.USE_FINGERPRINT" />
    <uses-permission android:name="android.permission.USE_BIOMETRIC" />
```

### Initialize

You need to initialize only once in application's lifecycle. You can either do it in `Application`'s `onCreate()`, or anywhere before you need to use TENCENT SOTER.

```java
InitializeParam param = new InitializeParam.InitializeParamBuilder()
.setScenes(0) // The senary constant for business index
.build();
SoterWrapperApi.init(context, 
new SoterProcessCallback<SoterProcessNoExtResult>() {...}, 
param);
```

### Prepare Keys

You need to prepare keys before authentication process.

```java
SoterWrapperApi.prepareAuthKey(new SoterProcessCallback<SoterProcessKeyPreparationResult>() {...},false, true, 0, null, null);
```

### Authenticate With Fingerprint

You can use wrapped interface to authenticate when fingerprint.

```java
AuthenticationParam param = new AuthenticationParam.AuthenticationParamBuilder()
                                    .setScene(0)
                                    .setContext(MainActivity.this)
    								// fingerprint
    								.setBiometricType(ConstantsSoter.FINGERPRINT_AUTH)
    								// faceid
    								//.setBiometricType(ConstantsSoter.FACEID_AUTH)
    								.setSoterBiometricCanceller(mSoterBiometricCanceller)
                                    .setPrefilledChallenge("test challenge")
                                    .setSoterBiometricStateCallback(new 			SoterBiometricStateCallback() {...}).build();
SoterWrapperApi.requestAuthorizeAndSign(new SoterProcessCallback<SoterProcessAuthenticationResult>() {...}, param);
```

### Release

You can release all the resource when you do not use TENCENT SOTER again by calling release. It will abort on-going tasks and remove support status. TENCENT SOTER will not occupy too much room actually. You can only do it when you confirm that you did not need to use it, like switch an account.

```java
SoterWrapperApi.release();
```

## More Document

- Want to know more about TENCENT SOTER's mechanism? Check [this](https://github.com/Tencent/soter/wiki).
- Want to use TENCENT SOTER in more sensitive business scenarios like login, or even payment? Check [this](https://github.com/Tencent/soter/wiki).

## Contact Us

You can add your comments in [issues](https://github.com/Tencent/soter/issues) if you have any question.

You can also join in the following QQ Group for more convenient discussing:

![qqgroup_qrcode](https://github.com/WeMobileDev/article/blob/master/assets/soter/SOTER%E4%BA%A4%E6%B5%81%E7%BE%A4%E7%BE%A4%E4%BA%8C%E7%BB%B4%E7%A0%81.png)

## Contributing

For more information about contributing issues or pull requests, check our [CONTRIBUTING document](./CONTRIBUTING.md).

## Compliance usage guide

[SOTER Client SDK compliance usage guide](https://github.com/Tencent/soter/wiki/SOTER-Client-SDK%E5%90%88%E8%A7%84%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97)

## Information Publication

* Name：SOTER Client SDK
* Version Number：2.1.8
* Developer：Shenzhen Tencent Computer Systems Company Limited
* The Main Function：TENCENT SOTER is a safe, universal and complete fingerprint authentication solution. By geting access to SOTER, developers can quickly implement secure fingerprint authentication.
* [Operation Instruction](https://github.com/Tencent/soter/wiki)
* [SOTER Client SDK Personal Information Protection Rules](https://github.com/Tencent/soter/wiki/SOTER-Client-SDK%E4%B8%AA%E4%BA%BA%E4%BF%A1%E6%81%AF%E4%BF%9D%E6%8A%A4%E8%A7%84%E5%88%99?from_wecom=1)

## License

TENCENT SOTER is based on BSD license. Please check our [LICENSE document](./LICENSE).

## Encouraging

[Tencent Open Source Contribution Plan](https://opensource.tencent.com/contribution)  encourages your contributing, and looks forward to your attending。
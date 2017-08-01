# Hello TENCENT SOTER

[![license](http://img.shields.io/badge/license-BSD3-brightgreen.svg?style=flat)](https://github.com/Tencent/soter/blob/master/LICENSE)
[![Release Version](https://img.shields.io/badge/release-1.3.1-red.svg)](https://github.com/Tencent/soter/releases) 
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/Tencent/soter/pulls)

We will give you a brief overview on what TENCENT SOTER is, why we should use TENCENT SOTER, How TENCENT SOTER works, how many devices already supported TENCENT SOTER, and the most important: how you can use it.



## What is TENCENT SOTER

TENCENT SOTER is a biometric stantdard as well as platform held by Tencent. It mainly focuses on biometric authentication security, and provides a solution to use your authentication sensors — for example, fingerprint sensor — securely and easily. It's not easy to build it, but don't worry, Tencent has handled almost all the things, and you only need to do little work to get all things done.



## Why we should use TENCENT SOTER

It's a good question. As there are hundreds of millions Android phones already had fingerprint sensors, and Google even provide a universal fingerprint interface [FingerprintManager](https://developer.android.com/reference/android/hardware/fingerprint/FingerprintManager.htmlFingerprirntManager), why shouldn't we use it in security sensitive scenes? Here are the reasons:

* Legacy FingerprintManger is not safe enough. If you only use the FingerprintManager itself for authenticating, hackers would hack your device quite easily (for example, change your authentication result from 'false ' to 'true', so that everyone can impersonate you). Of course, you can prevent user using fingeprint for authentication when device is rooted, but wait, the judgement itself can be hooked as well, isn't it?
* Even if you use the Crypto interfaces combining with FingerprintManager (see how [googlesample](https://github.com/googlesamples/android-FingerprintDialog) does it), it's still not so safe, because device may not have a root key, which means when you ask the framework to generate a pair of keys that need user to authenticate by fingerprint, the request may be hooked by hackers, replace the keys with theirs, and everything you do later, signing or encrypting or macing, they are all hooked and remain unsafe.
* In Android 7.0, Google require phone manufactures to provide a root key in there phones, but there're still some risks in it. The root keys are not deivce unique, which means other devices may have the same root key as yours. Once a key is cracked, it may influence thousands — or much more — other devices. More over, it needs your phone to be Android 7.0 once launched to market, and has Google Service in it. You now how little share Android 7.0 with Google service is, let alone most of them is not Android 7.0 since they are in there production lines.
* Last but not least, you can never know what fingerprint a user used with legacy FingerprintManager, so when an authentication action is taken, you cannot make sure the fingerprint used to authenticate is the right one, the one who uses it the first time. It is very important to know it in payment or account login scenes.

So the answer why we should use TENCENT SOTER is clear. TENCENT SOTER can solve all the issues listed above.

* If you implements all the client/backend interfaces, then TENCENT SOTER is very safe, even when your device is rooted. Check next chapter you'll find the reason.
* If a device supports TENCENT SOTER, Tencent will guarantee it has a device unique root key, and you can verify it via [WeChat Open Platform](https://open.weixin.qq.com/) server interfaces, which is stable and credible. 
* There're over 200,000,000 Android devices already support TENCENT SOTER, and what's more, **WeChat uses TENCENT SOTER in fingerprint payment in almost all the devices, which proved TENCENT SOTER has the security level of payment.**
* You can know what fingerprint in device scope the user used to authenticate each time, which means you're able to distinguish them from others in sensitive business scenes.
* If you are familiar with legacy FingerprintManager and Crypto interfaces, you can use TENCENT SOTER more like a friend — all the implements are from Android framework, we do not add a single interface. We only enhance the safety with our manufacture collaborators.
* You do not have access to TAM server each time you use TENCENT SOTER to authentication. Your data safety and privacy will be highly protected.
* Tencent has strict test over all TENCENT SOTER devices, which means you do not have to worry about the safety
* Moreover, TENCENT SOTER can support some Android 5.1 devices like vivo X6 and OPPO R7, even they don't have FingerprintManager at all!

## How TENCENT SOTER works



You can scan the QR code below in WeChat to add official TENCENT SOTER WeChat account, where you can find all the detailed introductions and how you can access it. If you just want to know the mechanism of TENCENT SOTER, you can refer to [this link](http://mp.weixin.qq.com/s/4BQulfFgVvanPSGOS92yiw).

![qrcode_for_gh_6410b016e824_258](markdown_res/qrcode_for_gh_6410b016e824_258.jpg)

And there's also [a much simpler version of expaination](http://mp.weixin.qq.com/s/x27CDj0oJPg6gsH-mfq-8g) which you can understand the machinism even if you do not have any cryptography foundation.

Sadly, links above are all in Chinese. To briefly summerize, there are 3 levels of keys in devices that support TENCENT SOTER: ATTK, App Secure Key (ASK) and Auth Key. All the keys are asymmetric using RSA-2048 algorithm.

All the private keys are securely stored in a secure area in device called [TEE](https://en.wikipedia.org/wiki/Trusted_execution_environment). if data is stored secured by TEE, nobody except the one who put it in would get access to it, and if a pair of key is generated in TEE, nobody, including the one who send the generating command, knows the content of the key. In a word, if a pair of keys is generated in TEE, it's so safe that no one can crack the key data even if the device is rooted. 

* ATTK private key is generated in TEE before the phone shipped to market.The public key is securely sent by the manufacture to TAM server, which runs by Tencent, while the private key is securely stored in TEE.
* Application can and only can generate a pair of ASK as the root key of the application. Once ASK is generated, the private key would also be saved in TEE (or more rigorously, secured by [KeyMaster TA](https://source.android.com/security/keystore/) in TEE), and the public key would be exported, along with the signature by ATTK signed in TEE (which means the signing operation itself is safe regardless the device is rooted or not). Application developers can send the key body (formed in json) along with the signature by ATTK to TAM to verify whether the public key is valid or not. If valid, application server should keep the key.
* In each business scene, you should generate a pair of Auth Key as the business key. The generation process is similar as ASK — generated in TEE, private key saved in TEE, and public key exported to application. What's different is application should check the validation of Auth Key public key by yourself (we don't want to know how many payments made by your application, seriously). And you mark the pair of key should be authenticated by user before used (yes, that's defined by Google).
* Before authentication, you should require a challenge string as the content to be signed, and once user authenticated with their fingerprint, you will get the authentication result formed in JSON, with challenge, fingerprint id of the device in it and device information stuff, along with the signature of the result. Then you should send them to the server to verify it with Auth Key public key. Note that the fingerprint id information is also transferred in TEE.

Below is the whole structure

![FD5DC4F4-B49B-4502-B2DE-836BB33B5627](markdown_res/SoterFramework.png)

## How Many Devices Already Supported TENCENT SOTER

By June 2nd 2017, there are over **230,000,000** Android devices already support TENCENT SOTER. You can check [this site][http://mp.weixin.qq.com/s/IRI-RCGsVB2WiPwUCGcytA] to learn what manufactures and what device models supported.

## How to use it

If you're confused about the concept or machinism of TENCENT SOTER, don't worry, you just need to know that if you follow the instruction, your fingerprint authentication process would be very safe. So what you need to know is: how I can use TENCENT SOTER?

### Prepare APPID

Before starting, If you need to use the server interface to check ASK valitdation and device support, you need to prepare APPID in [WeChat Open Platform](https://mp.weixin.qq.com/) or [WeChat Official Accounts Platform](https://mp.weixin.qq.com/). You can skip this and next chapter if you only want to use client APIs to implement a simple and unsafe version. **Only these applications which implement all the client and server interface as well as logics have the most security standard**.

### Server Implements

There are totally 5 network interfaces related to TENCENT SOTER. To simplify all the network process, we provide 5 network wrappers in our `soterwrapper`. You can find how to use them and what are the request and response parameters in our very simple client sample. Here are how you should implement them in your server side:

#### Check support TENCENT SOTER

Although we provide a client interface to check whether the deivce is support TENCENT SOTER or not, we still strongly recommend you to query Tencent to find out whether the device supports TENCENT SOTER or not. We provide a webservice interface defined [here](server_docs/微信开放平台后台接口文档.md). You can call it everytime you want to check it. We provide a network wrapper in client side called `IWrapGetSupportNet` for you to implement.![check_support](markdown_res/check_support.png)



#### Upload ASK public key

We've already introduced what ASK is in the last chapter. **Only TAM server** can verify whether the ASK public key is legal or not, so you should get the public key from the client, then follow the [document](server_docs/微信开放平台后台接口文档.md) to query TAM server whether the key is legal or not, if it's a legal key, save the base64 public key (which is the value of `pub_key` in the JSON you got from the client) corresponding to the device (you should use the value of`cpu_id` in the JSON you got from the client to identify the device). We provide a network wrapper in client side called `IWrapUploadKeyNet` for you to implement (also this wrapper is used for uploading Auth Key, as they are in the same format).

![upload_ask](markdown_res/upload_ask.png)

#### Upload Auth Key public key

Like the ASK, Auth Key is also introduced in the last chapter. However, for privacy reasons, application servers should check Auth Key by their own. When you get the Auth Key JSON and signature from the client, you should use the ASK corresonding to the deivce to check whether the Auth Key as long as all the JSON information is valid or not. We don't care what language you use to verify it. We provide an OpenSSL sample command for the verify Auth Key:

```
openssl dgst -sha256 -sigopt rsa_padding_mode:pss -sigopt rsa_pss_saltlen:20  -verify pubkey.pem -signature sign.bin authkey_json.txt
```

Here, `pubkey.pem`  is the ASK public key you store before,  `sign.bin` is the signature uploaded in binary and `authkey_json` is the JSON uploaded.

To simplify the command above, you can regard it as:

* Algorithm is RSA2048withSha256
* Padding Mode is PSS
* Saltlength is 20

We will sonner give samples in different languages. And any pull request is welcomed if you provide the sample.

We provide a network wrapper in client side called `IWrapUploadKeyNet` like the ASK case for you to implement.

![upload_auth_key](markdown_res/upload_auth_key.png)



#### Get Challenge

Challenge is what you used to be signed in signing operation. It is a random string in most cases. The server should have the logic the generate the string. We provide a network wrapper in client side called `IWrapGetChallengeStr` for you to implement.

![get_challenge](markdown_res/get_challenge.png)



#### Verify the Final Signature

It is the most important server interface to check whether the signature, which can only be genertated when users authenticate with their valid fingerprints.  This interface is always not independent. In most cases, the signature and the JSON stuff are only parts of paramters of the authentication request. You should make that network model implements `IWrapUploadSignature` in the client, and TENCENT SOTER will wrap all things left. You can use the same code like verifying Auth Key. The only difference is you should use the salt length as what you get from the client.

![verify_signature](markdown_res/verify_signature.png)



### Client implements

We provide 2 types of gradle dependancy: `soterwrapp` and `sotercore`. `sotercore` provides some basic and core functions TENCENT SOTER used to comunicate with framework and TEE, such as how to judge the device is support TENCENT SOTER from client, how to generate ASK and Auth Key, how to sign, etc. Althought you can use `sotercore` to complete your TENCENT SOTER support, it is strongly recommended that you should use `soterwrapper` to do it, because we wrapped all the abstract and process to make it easy to implement.

It's very easy to configure TENCENT SOTER support in you Android client application. Just put this line in your app's `build.gradle` file:

```groovy
dependencies {
    ...
      // You should replace the content of compile with 'com.tencent.soter:sotercore:1.3.1'if you only want to use core functions in your application
    compile 'com.tencent.soter:soterwrapper:1.3.1'
    ...
}
```

Of course, don't forget to add fingerprint permission in `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.USE_FINGERPRINT"/>
```



And it's done! Later you need to follow our [sample](soter-client-demo/) to learn what you need to add in your application. 

You should initialize TENCENT SOTER support as soon as possible, like in `Application.onCreate()`

```java
InitializeParam param = new InitializeParam.InitializeParamBuilder().setGetSupportNetWrapper(new RemoteGetSupportSoter()).setScenes(ConstantsSoterDemo.SCENE_PAYMENT)
                /*.setCustomAppSecureKeyName("Wechat_demo_ask").setDistinguishSalt("demo_salt_account_1").setSoterLogger(new ISoterLogger() {
                                                                                                                    ...
                                                                                                           }
                                                                                                        )*/.build();
SoterWrapperApi.init(getApplicationContext(), mGetIsSupportCallback, param);
```

You should prepare the ASK as soon as possible too, either after you checked the device supports TENCENT SOTER, or the first time you use the Auth Key.

```java
SoterWrapperApi.prepareAppSecureKey(mPrepareASKCallback, false, new RemoteUploadASK());
```

You should generate Auth Key in your application. It is highly recommended you pass the ASK network wrapper (If any) to Auth Key as well, in case the ASK is not generated, and our wrapper will help you re-generate and upload ASK and Auth Key all at once.

```java
SoterWrapperApi.prepareAuthKey(mPrepareAuthKeyCallback, false, true, SCENE1, new RemoteUploadPayAuthKey(pwdDigest), new RemoteUploadASK());
```

We recommend you do not use legacy FingerprintManager for authentication (although you can use it). We provide a more friendly process wrapper for you for fingerprint authentication. And what's more, you just need to put the authentication and get challenge network wrapper into the request, everything would be done!

```java
AuthenticationParam param = new AuthenticationParam.AuthenticationParamBuilder()
                .setScene(ConstantsSoterDemo.SCENE_PAYMENT)
                .setContext(this)
                .setFingerprintCanceller(mCanceller)
                .setIWrapGetChallengeStr(new RemoteGetChallengeStr())
                .setIWrapUploadSignature(uploadSignatureWrapper)
                .setSoterFingerprintStateCallback(new SoterFingerprintStateCallback() {
                  ...
                }).build();
        SoterWrapperApi.requestAuthorizeAndSign(processCallback, param);
```



The best way to learn how to use a lib is to see it's [sample](soter-client-demo/). And if our wrapper does not meet your requirement, feel free to fork and modify. Don't forget to send us pull requests if you have any good idea. Try it now!




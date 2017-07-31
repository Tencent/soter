# Hello TENCENT SOTER

For English version, please click [here](README_EN.md).

本文档将简要说明什么是TENCENT SOTER，为什么我们要用TENCENT SOTER，TENCENT SOTER的原理是什么，有多少设备已经支持TENCENT SOTER，以及最重要的：如何接入并使用TENCENT SOTER



## 什么是TENCENT SOTER

TENCENT SOTER是一种生物认证标准，同时也是腾讯生物认证平台。TENCENT SOTER主要着眼于如何安全、高效并简单得使用你设备上的传感器进行鉴权——最重要，也是目前用到最多的就是指纹传感器。构建这样的一个平台和制定标准并不简单，但是不用担心，腾讯已经将几乎所有的准备工作全部完成，你所需的仅仅是少量的接入工作，即可快速完成安全的生物鉴权。



## 为什么要用TENCENT SOTER

这是一个好问题：既然已经有数亿Android设备已经拥有了指纹传感器，甚至Google在Android 6.0中添加了标准的指纹授权接口，为什么我们不能在安全敏感的场景中直接用这个接口呢？原因如下：

* 系统的指纹接口并不安全。如果仅仅使用FingerprintManager进行指纹鉴权，黑客会很容易在设备被root的前提下将整个授权过程hack掉（比如，将指纹认证结果从false直接改为true，那么任何一个人都可以模仿你的指纹进行授权了）。
* 即使你用Crypto接口配合FingerprintManager一起使用（可以参考 [googlesample](https://github.com/googlesamples/android-FingerprintDialog)看下如何实现），整个授权过程依然不安全。因为设备不一定有一个根密钥，这意味着当你要求系统生成一对用户指纹授权才能使用私钥的非对称密钥的时候，这个请求可能会被黑客挂钩子，然后黑客用自己的密钥替换掉你要生成的密钥。这之后你做的所有事情，比如签名或者解密等等，全部都是用的黑客的密钥，整个过程依然形同裸奔。
* 在Android 7.0中，Google要求手机厂商在设备中植入根密钥以避免上述风险。但是，这仍然是有漏洞的：根密钥并非一机一密，也就是说，其他手机可能拥有和你一模一样的密钥。那么，万一其中一台机器的密钥被破解（黑客破解自己的手机即可），那么将影响成千上万的其他机器。另外，这个根密钥同样要求你的手机出厂即是Android 7.0系统或以上，并且在在手机上有可用的Google Service。目前Android 7.0的市占率并不高，更别说大部分手机都是后期升级到7.0的了。
* 最重要的一点，如果使用系统指纹接口，你在授权的时候不知道用户使用了哪根手指进行授权。那么，你将永远不知道是不是你所希望的用户——一般而言，是注册这项服务的用户——授权了这个请求。知晓授权者为谁，这对于高安全性场景（如登录、支付等）非常重要。

因此为什么要用TENCENT SOTER的原因就很明显了：他能解决上述所有问题。

* TENCENT SOTER非常安全，即使你的设备被root了也是如此。下一章将会告诉你为什么。
* 如果该设备支持TENCENT SOTER，那么腾讯将会保证这部设备出厂即有一机一密的根密钥。所有的验证服务都由腾讯开放平台提供，该平台已经被时间验证了是稳定且可信的。
* 目前，已经有超过2亿部Android设备支持了TENCENT SOTER。并且最重要的是，**在几乎所有支持TENCENT SOTER的设备上进行微信指纹支付时，都使用了TENCENT SOTER标准相当长一段时间。这也证明了TENCENT SOTER本身是具备支付级别安全能力的标准和平台**。
* 每次授权之后，你可以知道用户使用了设备上哪根手指进行支付。这对于敏感的业务场景而言非常重要。
* 如果你对于Android系统中的FingerprintManager和Crypto相关接口很熟悉的话，你会发现对TENCENT SOTER非常熟悉：我们所有的实现都是使用的Android Framework中的接口，并且，我们没有增加一个公开接口来做这件事情。我们只是针对这些接口和厂商进行合作，进行了很强的安全加固。
* 你并不需要每次指纹认证都接入腾讯的后台。你的数据安全与隐私将会得到充分保证。
* 腾讯已经针对所有支持TENCENT SOTER的手机进行了严格的测试，也就意味着你不必担心手头这台支持TENCENT SOTER的设备的安全性是否可靠。
* 还有，TENCENT SOTER甚至支持了部分Android 5.1设备，比如vivo X6和OPPO R7，即使这些设备上完全没有Android标准指纹接口！

## TENCENT SOTER的原理是什么



你可以通过微信扫描下面的二维码关注TENCENT SOTER官方公众号，在这里你可以找到详细的原理说明和接入指引。如果你只是想了解TENCENT SOTER的原理，可以参考[这个链接](http://mp.weixin.qq.com/s/4BQulfFgVvanPSGOS92yiw)

![qrcode_for_gh_6410b016e824_258](markdown_res/qrcode_for_gh_6410b016e824_258.jpg)

另外，这里有一个[更简单的解释](http://mp.weixin.qq.com/s/x27CDj0oJPg6gsH-mfq-8g)，即使你没有任何密码学基础也可以看懂。

简要解释下TENCENT SOTER的原理：

TENCENT SOTER中，一共有三个级别的密钥：ATTK，App Secure Key(ASK)以及AuthKey。这些密钥都是RSA-2048的非对称密钥。

所有的密钥都在[TEE](https://en.wikipedia.org/wiki/Trusted_execution_environment)中（或经过TEE安全密钥加密）安全保存。如果在TEE中保存或者安全密钥加密保证，除了数据所有者之外，没有人可以使用它。更重要的是，如果密钥是在TEE中生成的，所有人——包括密钥所有者——都是得不到密钥私钥的（对非对称密钥而言），仅仅可以请求使用它。一句话总结，如果非对称密钥是在TEE内部生成的，即使设备被root，私钥也不会泄露。

* ATTK私钥在设备出厂之前就已经在TEE中生成，公钥被被厂商安全得传输到腾讯的TAM服务器，私钥则在TEE中安全存储。
* 第三方应用能且只能在TEE中生成唯一ASK（二级密钥）。一旦ASK被成功生成，私钥被存储在TEE中（或者更加准确地说，被TEE中安全密钥加密存储在手机sfs中，等同于存储在TEE中，即使手机被root了也是安全的）。公钥结构体（包含公钥信息以及其他辅助信息）导出的时候会自动带上ATTK对公钥数据的签名。应用开发者将公钥结构体以及ATTK对该结构体的签名通过微信开放平台接口（见接口文档）发送到TAM服务器认证公钥结构体合法性。如果合法，则第三方保存该结构体备用。
* 在所有的业务场景中，你应该生成一对AuthKey用于该场景指纹认证。AuthKey的生成过程与ASK类似——在TEE中生成，私钥在TEE中保存，公钥上传到服务器。不同的是，第三方应用应该自己检查AuthKey的合法性（实际上，我们真的不想知道你们的用户做了多少笔支付）。同时，生成AuthKey的时候，需要标记私钥只有在用户指纹授权之后才可以使用（正如Google在标准接口中定义的那样）。
* 在认证之前，应用需要先向自己的服务器请求一个挑战因子（通常是一个随机串）作为用于签名的对象。用户指纹授权之后，你将可以导出一个JSON结果，其中包含了你刚刚请求的挑战因子、用户使用了哪个手指（fid）以及其他设备信息，和这个JSON结果对应AuthKey的签名。之后，将他们发送到自己的服务器，自己使用之前存储的AuthKey公钥验签就好。其中，fid也是在TEE中自动读取并连同结果一起被签名的，也就是说，黑客是无法伪造。

下面是TENCENT SOTER的整体架构

![FD5DC4F4-B49B-4502-B2DE-836BB33B5627](markdown_res/SoterFramework.png)

## 有多少设备已经支持TENCENT SOTER

截止2017年6月2日，已经有超过**2亿**设备支持了TENCENT SOTER。通过[这个网址](http://mp.weixin.qq.com/s/IRI-RCGsVB2WiPwUCGcytA)可以了解目前哪些厂商的哪些机型已经支持了TENCENT SOTER。

## 如何接入并使用TENCENT SOTER

如果你对于TENCENT SOTER的原理感到困惑，不要担心，你只要知道跟着我们一步步做，你就能很简单且安全得实现指纹认证。那么，究竟该如何行动呢？

### 申请接入权限

开始之前，你从之前章节已经了解到你需要将你在[微信公众平台](https://mp.weixin.qq.com/)上的appid加上权限，以便调用后台接口。现在，按照下面的格式给`soter@tencent.com`发送一封邮件：

```
目的: 添加TENCENT SOTER后台接口调用权限
微信公众平台appid: xxxxx
公司: 公司名称或个人 
```

随后，一周之内我们会开通后台接口调用权限。后续，会有更加友好的方式使你能快速申请权限。

### 服务器实现

参考[后台实现流程指引](server-docs/后台实现流程指引.md)以实现后台逻辑。

### 客户端实现

我们提供了两个gradle dependency：`soterwrapp` and `sotercore`。`sotercore`提供了TENCENT SOTER与framework、TEE层交互的核心底层接口，比如从客户端判断设备是否支持TENCENT SOTER，如何生成ASK和AuthKey，如何签名等等。尽管你可以直接使用`sotercore`来完成所有的TENCENT SOTER实现，但是我们依然强烈建议你直接使用另外一个dependency`soterwrapper`，因为我们在这里封装很多TENCENT SOTER相关的复杂逻辑和易错逻辑，让你能更快实现TENCENT SOTER。

在你的客户端中添加TENCENT SOTER支持非常简单：只要在APP的`build.gradle`文件中相应位置添加这一行：

```groovy
dependencies {
    ...
      // You should replace the content of compile with 'com.tencent.soter:sotercore:1.3.0'if you only want to use core functions in your application
    compile 'com.tencent.soter:soterwrapper:1.3.0'
    ...
}
```

这就可以了！然后，建议你参考我们的sample，看下你需要在应用中添加哪些逻辑

你应该尽可能早得初始化TENCENT SOTER支持，比如在`Application.onCreate()`中：

```java
InitializeParam param = new InitializeParam.InitializeParamBuilder().setGetSupportNetWrapper(new RemoteGetSupportSoter()).setScenes(ConstantsSoterDemo.SCENE_PAYMENT)
                /*.setCustomAppSecureKeyName("Wechat_demo_ask").setDistinguishSalt("demo_salt_account_1").setSoterLogger(new ISoterLogger() {
                                                                                                                    ...
                                                                                                           }
                                                                                                        )*/.build();
SoterWrapperApi.init(getApplicationContext(), mGetIsSupportCallback, param);
```

你也应该尽早准备ASK。你可以选择在初始化TENCENT SOTER之后，或者在生成AuthKey之前

```java
SoterWrapperApi.prepareAppSecureKey(mPrepareASKCallback, false, new RemoteUploadASK());
```

你应当生成自己业务需要使用的AuthKey。我们强烈建议在生成AuthKey的请求参数中，将生成ASK的网络封装结构体传进去。这样的话，如果你没有成功生成ASK，或者ASK被用户主动删除，我们会自动帮你重新生成上传新的ASK公钥。

```java
SoterWrapperApi.prepareAuthKey(mPrepareAuthKeyCallback, false, true, SCENE1, new RemoteUploadPayAuthKey(pwdDigest), new RemoteUploadASK());
```

我们不建议你直接使用系统FingerprintManager来进行指纹认证（尽管你可以这么做）。我们提供了一个更加友好的认证流程。你只需要提供认证以及获取挑战因子的网络结构体，所有的事情我们都会替你完成：

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



当然，最好的方式还是直接参考我们的 [sample](soter-client-demo/) 看下怎么实现TENCENT SOTER（请事先确认使用的是支持TENCENT SOTER的设备进行测试，支持设备列表见[这里](http://mp.weixin.qq.com/s/IRI-RCGsVB2WiPwUCGcytA)）。如果`soterwrapper`不能满足你的需求，欢迎fork我们的工程然后自行修改。如果你有更好的实现，不要忘了给我们发pull request。现在就开始尝试吧！




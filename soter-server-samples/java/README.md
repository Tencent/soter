# Soter Server Samples (Java)

## 简介
Soter Server Samples (Java) 提供了Java版本的后台验证签名的实现示例，包括验证AuthKey、验证最终签名数据等。此示例使用开源库[Bouncy Castle](https://www.bouncycastle.org/)实现相关的签名及验证操作。

### RSAUtil
此示例中RSAUtil类封装了以下函数：

- loadPublicKeyFromFile: 从公钥文件加载PublicKey，公钥文件是指使用ssl命令导出的pem文件，在src/example中提供了一些样例数据
- verify：验证签名

### 样例数据
在示例的src/example下提供了一些测试数据：

- ask.pem: ask公钥文件，在实际生产环境中，ask公钥由客户端生成并上传到服务器，用于验证AuthKey
- auth_key_json.txt: 这部分数据是由客户端生成AuthKey时导出的json数据，由客户端上传到服务器，其中json包含的`pub_key`部分是AuthKey公钥
- auth_key_signature.bin: 使用ask对auth_key_json进行签名得到的签名数据，由客户端上传到服务器
- auth_key.pem: AuthKey公钥文件，在实际生产环境中，这部分数据包含在auth_key_json当中，由客户端上传到服务器
- final_json.txt: 在指纹授权之后，对`challenge`作最后签名并导出的json数据
- final_signature.bin: 使用AuthKey对final_json数据进行签名得到的签名数据

## 运行
此示例是一个标准的Java项目，主函数入口为`SoterServerDemo.main`，使用Eclipse导入之后，即可直接运行。

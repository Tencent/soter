package com.tencent.soter.serverdemo;

import com.tencent.soter.serverdemo.utils.FileUtil;
import com.tencent.soter.serverdemo.utils.RSAUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

public class SoterServerDemo {
	public static void main(String[] args) {
		
		Security.addProvider(new BouncyCastleProvider());

		// verify auth key
		RSAPublicKey askPublicKey = RSAUtil.loadPublicKeyFromFile("java/example/ask.pem");
		byte[] authKeyJson = FileUtil.readByteArrayFromFile("java/example/auth_key_json.txt");
		byte[] authKeySignature = FileUtil.readByteArrayFromFile("java/example/auth_key_signature.bin");
		boolean verifyAuthKey = RSAUtil.verify(askPublicKey, authKeyJson, authKeySignature);
		if (verifyAuthKey) {
			System.out.println("Verify AuthKey OK");
		}
		else {
			System.err.println("Verify AuthKey failed");
		}

		// verify final signature
		RSAPublicKey publicKey = RSAUtil.loadPublicKeyFromFile("java/example/auth_key.pem");
		byte[] data = FileUtil.readByteArrayFromFile("java/example/final_json.txt");
		byte[] signature = FileUtil.readByteArrayFromFile("java/example/final_signature.bin");
		boolean verifyFinal = RSAUtil.verify(publicKey, data, signature);
		if (verifyFinal) {
			System.out.println("Verify Final signature OK");
		}
		else {
			System.err.println("Verify Final signature failed");
		}

		// load ask cert from certificate chain(e.g. huawei)
		try {
			String certs = FileUtil.readFromFile("java/example/hw_ask_cert_json.txt");
			JSONObject jsonObject = new JSONObject(certs);
			JSONArray certsJson = jsonObject.optJSONArray("certs");
			CertificateFactory factory = CertificateFactory.getInstance("X.509");
			X509Certificate askCertificate = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certsJson.getString(0).getBytes()));
			SoterPubKeyModel soterPubKeyModel = new SoterPubKeyModel();
			int saltLen = RSAUtil.extractAttestationSequence(askCertificate, soterPubKeyModel);
			System.out.println("cert ask encode: " + Base64.getEncoder().encodeToString(askCertificate.getPublicKey().getEncoded()));
			System.out.println("cert ask model: " + soterPubKeyModel.toString() + " saltLen: " + saltLen);

			// verify auth key
			RSAPublicKey hwAskPublicKey = (RSAPublicKey) askCertificate.getPublicKey();
			//or get ask form file
			//RSAPublicKey hwAskPublicKey = RSAUtil.loadPublicKeyFromFile("java/example/hw_ask.pem");
			System.out.println("publicKey(e.g. huawei): " + hwAskPublicKey);
			byte[] hwAuthKeyJson = FileUtil.readByteArrayFromFile("java/example/hw_auth_key_json.txt");
			String hwSignStr = FileUtil.readFromFile("java/example/hw_auth_key_signature.txt");
			byte[] hwAuthKeySignature = Base64.getDecoder().decode(hwSignStr);
			boolean hwVerifyAuthKey = RSAUtil.verify(hwAskPublicKey, hwAuthKeyJson, hwAuthKeySignature, saltLen);
			if (hwVerifyAuthKey) {
				System.out.println("hwVerifyAuthKey AuthKey OK");
			} else {
				System.err.println("hwVerifyAuthKey AuthKey failed");
			}

			// verify final signature
			RSAPublicKey hwAuthKey = RSAUtil.loadPublicKeyFromFile("java/example/hw_auth_key.pem");
			byte[] hwData = FileUtil.readByteArrayFromFile("java/example/hw_final_json.txt");
			String hwAuthSignStr = FileUtil.readFromFile("java/example/hw_final_signature.txt");
			byte[] hwSign = Base64.getDecoder().decode(hwAuthSignStr);
			boolean hwVerifyFinal = RSAUtil.verify(hwAuthKey, hwData, hwSign, saltLen);
			if (hwVerifyFinal) {
				System.out.println("hwVerifyFinal Final signature OK");
			} else {
				System.err.println("hwVerifyFinal Final signature failed");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

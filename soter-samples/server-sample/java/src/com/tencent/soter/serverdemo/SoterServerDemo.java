package com.tencent.soter.serverdemo;

import com.tencent.soter.serverdemo.utils.FileUtil;
import com.tencent.soter.serverdemo.utils.RSAUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

public class SoterServerDemo {
	public static void main(String[] args) {
		
		Security.addProvider(new BouncyCastleProvider());

		// verify auth key
		RSAPublicKey askPublicKey = RSAUtil.loadPublicKeyFromFile("example/ask.pem");
		byte[] authKeyJson = FileUtil.readByteArrayFromFile("example/auth_key_json.txt");
		byte[] authKeySignature = FileUtil.readByteArrayFromFile("example/auth_key_signature.bin");
		boolean verifyAuthKey = RSAUtil.verify(askPublicKey, authKeyJson, authKeySignature);
		if (verifyAuthKey) {
			System.out.println("Verify AuthKey OK");
		}
		else {
			System.err.println("Verify AuthKey failed");
		}
		
		// verify final signature
		RSAPublicKey publicKey = RSAUtil.loadPublicKeyFromFile("example/auth_key.pem");
		byte[] data = FileUtil.readByteArrayFromFile("example/final_json.txt");
		byte[] signature = FileUtil.readByteArrayFromFile("example/final_signature.bin");
		boolean verifyFinal = RSAUtil.verify(publicKey, data, signature);
		if (verifyFinal) {
			System.out.println("Verify Final signature OK");
		}
		else {
			System.err.println("Verify Final signature failed");
		}

		// load ask cert
		try {
			String certs = FileUtil.readFromFile("example/ask_cert_json.txt");
			JSONObject jsonObject = new JSONObject(certs);
			JSONArray certsJson = jsonObject.optJSONArray("certs");
			CertificateFactory factory = CertificateFactory.getInstance("X.509");
			X509Certificate askCertificate = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certsJson.getString(0).getBytes()));
			SoterPubKeyModel soterPubKeyModel = new SoterPubKeyModel();
			RSAUtil.extractAttestationSequence(askCertificate, soterPubKeyModel);
			System.out.println("cert ask encode: " + Base64.getEncoder().encodeToString(askCertificate.getPublicKey().getEncoded()));
			System.out.println("cert ask model: " + soterPubKeyModel.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

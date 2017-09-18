package com.tencent.soter.serverdemo;

import java.security.Security;
import java.security.interfaces.RSAPublicKey;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.tencent.soter.serverdemo.utils.FileUtil;
import com.tencent.soter.serverdemo.utils.RSAUtil;

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
		
	}
}

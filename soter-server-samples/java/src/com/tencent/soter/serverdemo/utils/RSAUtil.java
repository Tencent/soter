package com.tencent.soter.serverdemo.utils;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.security.spec.X509EncodedKeySpec;

import com.tencent.soter.serverdemo.SoterPubKeyModel;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.json.JSONObject;

/**
 * RSAUtil is the helper class to load keys and verify signature and so on.
 * @author alvinluo
 */
public class RSAUtil {

	private static final String ALGORITHM_NAME = "RSA";

	private static final String KEY_DESCRIPTION_OID = "1.3.6.1.4.1.11129.2.1.17";

	/**
	 * load PublicKey from file
	 */
	public static RSAPublicKey loadPublicKeyFromFile(String fileName) {
		return loadPublicKey(FileUtil.readFromFile(fileName, true));
	}

	public static RSAPublicKey loadPublicKey(String str) {
		if (str == null) {
			return null;
		}

		try {
			byte[] buffer = Base64.decode(str);
			KeyFactory factory = KeyFactory.getInstance(ALGORITHM_NAME);
			X509EncodedKeySpec spec = new X509EncodedKeySpec(buffer);
			return (RSAPublicKey) factory.generatePublic(spec);
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return null;
	} 

	/**
	 * verify signature
	 * @param publicKey the public key used to verify signature
	 * @param data the origin data
	 * @param sign the signature
	 * @return true if verify successfully, false otherwise
	 */
	public static boolean verify(RSAPublicKey publicKey, byte[] data, byte[] sign) {
		try {
			Signature signature = Signature.getInstance("SHA256withRSA/PSS", BouncyCastleProvider.PROVIDER_NAME);
			signature.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 20, 1));
			signature.initVerify(publicKey);
			signature.update(data);
			return signature.verify(sign);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean verify(RSAPublicKey publicKey, String data, byte[] signature) {
		return verify(publicKey, data.getBytes(), signature);
	}

	public static void extractAttestationSequence(X509Certificate attestationCert, SoterPubKeyModel soterPubKeyModel) throws Exception, IOException {
		byte[] attestationExtensionBytes = attestationCert.getExtensionValue(KEY_DESCRIPTION_OID);
		if (attestationExtensionBytes == null || attestationExtensionBytes.length == 0) {
			throw new Exception("Couldn't find the keystore attestation " + "extension data.");
		}

		try {
			int jsonStartOff = 0;
			int jsonEndOff = 0;
			int jsonLength = 0;

			byte jsonStartTag = "{".getBytes()[0];
			byte jsonEndTag = "}".getBytes()[0];

			for (int i = 0; i < attestationExtensionBytes.length; i++) {
				byte b = attestationExtensionBytes[i];
				if (b==jsonStartTag) {
					jsonStartOff = i;
				}else if(b==jsonEndTag){
					jsonEndOff = i;
				}
			}
			if (jsonStartOff > 0 && jsonStartOff < jsonEndOff) {
				assert attestationExtensionBytes[jsonStartOff-1]==(jsonEndOff-jsonStartOff+1);
				jsonLength = (jsonEndOff-jsonStartOff+1);

				byte[] jsonBytes = new byte[jsonLength];
				System.arraycopy(attestationExtensionBytes, jsonStartOff, jsonBytes, 0, jsonLength);
				String jsonString = new String(jsonBytes);
				JSONObject jsonObject = new JSONObject(jsonString);

				System.out.println("soter: challenge json in attestation certificate " + jsonString);



				soterPubKeyModel.setCpu_id(jsonObject.getString("cpu_id"));
				soterPubKeyModel.setUid(jsonObject.getInt("uid"));
				soterPubKeyModel.setCounter(jsonObject.getLong("counter"));
			}
		}catch (Exception e){
			throw new Exception("Couldn't parse challenge json string in the attestation certificate" + e.getStackTrace());
		}
	}
}

package com.tencent.soter.serverdemo.utils;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.RSAPrivateKeyStructure;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

@SuppressWarnings("deprecation")
public class RSAUtil {

	private static final String ALGORITHM_NAME = "RSA";
	
	/**
	 * 从文件加载PublicKey
	 */
	public static RSAPublicKey loadPublicKeyFromFile(String fileName) {
		return loadPublicKey(FileUtil.readFromFile(fileName, true));
	}
	
	/**
	 * 从文件加载 PrivateKey
	 */
	public static RSAPrivateKey loadPrivateKeyFromFile(String fileName) {
		return loadPrivateKey(FileUtil.readFromFile(fileName, true));
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

	public static RSAPrivateKey loadPrivateKey(String str) {
		if (str == null) {
			return null;
		}
		try {
			// RSA��ԿΪPKCS#1
			byte[] buffer = Base64.decode(str);
			RSAPrivateKeyStructure asn1PrivKey = new RSAPrivateKeyStructure(
					(ASN1Sequence) ASN1Sequence.fromByteArray(buffer));
			RSAPrivateKeySpec rsaPrivKeySpec = new RSAPrivateKeySpec(asn1PrivKey.getModulus(),
					asn1PrivKey.getPrivateExponent());

			KeyFactory factory = KeyFactory.getInstance(ALGORITHM_NAME);
			RSAPrivateKey priKey = (RSAPrivateKey) factory.generatePrivate(rsaPrivKeySpec);
			return priKey;
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return null;
	}


	
	public static byte[] sign(RSAPrivateKey privateKey, byte[] data) {
		try {
			Signature signature = Signature.getInstance("SHA256withRSA/PSS", BouncyCastleProvider.PROVIDER_NAME);
			signature.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 20, 1));
			signature.initSign(privateKey);
			signature.update(data);
			return signature.sign();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
		return null;
	}

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
}

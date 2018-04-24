package com.teskalabs.blackswan.gargoyle.crypto;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * A class that encrypts specified data with an RSA public key.
 * @author Premysl Cerny
 */
public class RSAEncryption {
	/**
	 * Obtains the public key from resources.
	 * @return PublicKey
	 */
	protected static PublicKey getPublicKey(InputStream is) {
		byte[] keyBytes = convertStreamToByteArray(is);
		// Obtaining the key in a specified data format
		try {
			X509EncodedKeySpec spec =
					new X509EncodedKeySpec(keyBytes);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			return kf.generatePublic(spec);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Reads a resource and creates a byte array from it.
	 * @param is InputStream
	 * @return byte[]
	 */
	protected static byte[] convertStreamToByteArray(InputStream is) {
		ByteArrayOutputStream b_stream = new ByteArrayOutputStream();
		byte[] buff = new byte[10240];
		int i;
		try {
			while ((i = is.read(buff, 0, buff.length)) > 0) {
				b_stream.write(buff, 0, i);
			}
			return b_stream.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Generates a secret key for the AES.
	 * @return SecretKey
	 */
	protected static SecretKey getSecretKey() {
		try {
			KeyGenerator generator = KeyGenerator.getInstance("AES");
			generator.init(128);
			return generator.generateKey();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Encrypts a specified input with an RSA cipher algorithm.
	 * @param input byte[]
	 * @param public_key InputStream
	 * @return byte[]
	 */
	public static byte[] encrypt(byte[] input, InputStream public_key) {
		// Obtaining and checking the public key for RSA
		PublicKey publicKey = RSAEncryption.getPublicKey(public_key);
		if (publicKey == null)
			return null;
		// Obtaining and checking the secret key for AES
		SecretKey secretKey = getSecretKey();
		if (secretKey == null)
			return null;
		try {
			// Encrypting the input with AES
			byte[] output = encrypt_AES(input, secretKey);
			// Encrypting the key with RSA
			byte[] key = encrypt_RSA(secretKey.getEncoded(), publicKey);
			// Forming the output
			byte[] combined = new byte[key.length + output.length];
			for (int i = 0; i < combined.length; ++i) {
				combined[i] = i < key.length ? key[i] : output[i - key.length];
			}
			// Returning the result
			return combined;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Encrypts input with the AES.
	 * @param input byte[]
	 * @param secretKey SecretKey
	 * @return byte[]
	 * @throws GeneralSecurityException
	 */
	protected static byte[] encrypt_AES(byte[] input, SecretKey secretKey) throws GeneralSecurityException {
		// Encrypting the input
		Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		aesCipher.init(Cipher.ENCRYPT_MODE, secretKey);
		return aesCipher.doFinal(input);
	}

	/**
	 * Encrypts input with the RSA.
	 * @param input byte[]
	 * @param publicKey SecretKey
	 * @return byte[]
	 * @throws GeneralSecurityException
	 */
	protected static byte[] encrypt_RSA(byte[] input, PublicKey publicKey) throws GeneralSecurityException {
		// Encrypting the input
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		return cipher.doFinal(input);
	}
}

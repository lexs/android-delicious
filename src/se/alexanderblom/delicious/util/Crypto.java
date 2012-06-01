package se.alexanderblom.delicious.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

public class Crypto {
	private static final String CIPHER_TRANSFORMATION = "AES/ECB/PKCS7Padding";
	private static final String CIPHER_ALGORITHM = "AES";

	private SecretKeySpec keySpec;
	private Cipher cipher;

	public Crypto(String passphrase) {
		byte[] key = getKey(passphrase);

		keySpec = new SecretKeySpec(key, CIPHER_ALGORITHM);

		try {
			cipher = Cipher.getInstance(CIPHER_TRANSFORMATION, "BC");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public byte[] encryptAsBase64(byte[] input) {
		return Base64.encode(encrypt(input), 0);
	}

	public byte[] encrypt(byte[] input) {
		try {
			cipher.init(Cipher.ENCRYPT_MODE, keySpec);

			return cipher.doFinal(input);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public byte[] decryptAsBase64(byte[] input) {
		return decrypt(Base64.decode(input, 0));
	}

	public byte[] decrypt(byte[] input) {
		try {
			cipher.init(Cipher.DECRYPT_MODE, keySpec);

			return cipher.doFinal(input);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private byte[] getKey(String passphrase) {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance(CIPHER_ALGORITHM);
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			
			sr.setSeed(passphrase.getBytes());
			kgen.init(128, sr);
			
			SecretKey skey = kgen.generateKey();
            return skey.getEncoded();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}

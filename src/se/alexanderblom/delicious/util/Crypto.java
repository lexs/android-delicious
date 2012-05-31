package se.alexanderblom.delicious.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

import com.google.common.hash.Hashing;

public class Crypto {
	private static final String CIPHER_TRANSFORMATION = "AES/ECB/PKCS7Padding";
	private static final String CIPHER_ALGORITHM = "AES";

	private SecretKeySpec keySpec;
	private Cipher cipher;
	
	public Crypto(String passphrase) {
		byte[] key = Hashing.md5().hashString(passphrase).asBytes();
		
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
}

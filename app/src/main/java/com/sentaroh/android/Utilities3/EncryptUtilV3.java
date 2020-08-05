package com.sentaroh.android.Utilities3;
/*
The MIT License (MIT)
Copyright (c) 2011-2019 Sentaroh

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
and to permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

*/

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;

public class EncryptUtilV3 {
    private static Logger log= LoggerFactory.getLogger(EncryptUtilV3.class);

	final static public String makeSHA1Hash(String input)
            throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        md.reset();
        byte[] buffer = input.getBytes();
        md.update(buffer);
        byte[] digest = md.digest();

        String hexStr = "";
        for (int i = 0; i < digest.length; i++) {
            hexStr +=  Integer.toString( ( digest[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return hexStr;
    };

    final static public String makeSHA256Hash(byte[] input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.reset();
        md.update(input);
        byte[] digest = md.digest();

        String hexStr = "";
        for (int i = 0; i < digest.length; i++) {
            hexStr +=  Integer.toString( ( digest[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return hexStr;
    }

    final static public String makeSHA256Hash(String input) throws NoSuchAlgorithmException {
        return makeSHA256Hash(input.getBytes());
    }

	public static class CipherParms {
		public IvParameterSpec iv=null;
		public SecretKey key=null;
	}

//	public static CipherParms initEncryptEnv(String passwd) {
//		CipherParms ep=new CipherParms();
//		ep.iv=generateInitializationVector(passwd);
//		ep.key=generateKey(passwd);
//		return ep;
//	}
//
//    public static CipherParms initEncryptEnv(SecretKey skey, String seed) {
//        CipherParms ep=new CipherParms();
//        ep.iv=generateInitializationVector(seed);
//        ep.key=skey;
//        return ep;
//    }
//
    public static CipherParms initCipherEnv(String passwd) {
		CipherParms ep=new CipherParms();
		ep.iv=generateInitializationVector(passwd);
		ep.key=generateKey(passwd);
		return ep;
	}

    public static CipherParms initCipherEnv(SecretKey skey, String seed) {
        CipherParms ep=new CipherParms();
        ep.iv=generateInitializationVector(seed);
        ep.key=skey;
        return ep;
    }

    private static IvParameterSpec generateInitializationVector(String seed) {
		IvParameterSpec iv=null;
		try {
            String sha256=makeSHA256Hash(seed);
	        MessageDigest md = MessageDigest.getInstance("MD5");
	        md.reset();
	        byte[] buffer = sha256.getBytes();
	        md.update(buffer);
	        byte[] digest = md.digest();
	        iv=new IvParameterSpec(digest);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			log.error("generateInitializationVector error.", e);
		}
		
		return iv;
	}
	
	private static SecretKey generateKey(String passWordStr) {
		SecretKey secretKey=null;
		if (passWordStr!=null) {
            byte[] salt=null;
			try {
				char[] password = passWordStr.toCharArray();
				SecretKeyFactory factory;
                try {
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    md.reset();
                    byte[] buffer = passWordStr.getBytes();
                    md.update(buffer);
                    salt = md.digest();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    log.error("Generate salt error.", e);
                    return null;
                }
                KeySpec keySpec = null;
                keySpec = new PBEKeySpec(password, salt, 1024, 256);
//				factory = SecretKeyFactory.getInstance("PBEWITHSHAAND256BITAES-CBC-BC");
                factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
				secretKey = factory.generateSecret(keySpec);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
                log.error("generateKey error.", e);
			} catch (InvalidKeySpecException e) {
				e.printStackTrace();
                log.error("generateKey error.", e);
			}
		}
		return secretKey;
	};

	private static final String ALGORITM = "AES/CBC/PKCS7Padding";

	/**
	 * 暗号化した文字列を取得する
	 */
	public static byte[] encrypt(final String inStr, CipherParms ep){
		byte[] encrypted=null;
		if (inStr!=null && ep!=null) {
			try {
//		        MessageDigest md = MessageDigest.getInstance("MD5");
//		        md.reset();
		        String time= String.valueOf(System.currentTimeMillis()).concat("12345678");
//		        byte[] buffer = time.getBytes();
//		        md.update(buffer);
//		        byte[] digest = md.digest();
		        String md_str=time.substring(0, 8)+inStr;

				Cipher cipher= Cipher.getInstance(ALGORITM);
				cipher.init(Cipher.ENCRYPT_MODE, ep.key,ep.iv);
				encrypted = cipher.doFinal(md_str.getBytes());
			} catch (Exception e) {
				e.printStackTrace();
                log.error("encrypt error.", e);
			}
		}
		return encrypted;
	};

	/** 
	 * 復号した文字列を取得する 
	 */
	public static String decrypt(final byte[] encrypted_array, CipherParms ep){
		String decrypted_data=null;
		if (encrypted_array!=null && ep!=null) { 
			try {
				Cipher cipher= Cipher.getInstance(ALGORITM);
				cipher.init(Cipher.DECRYPT_MODE, ep.key,ep.iv);
				byte[] decrypted_array = cipher.doFinal(encrypted_array);
				if (decrypted_array!=null) {
					decrypted_data = (new String(decrypted_array)).substring(8);
				}
            } catch (Exception e) {
                e.printStackTrace();
                log.error("decrypt error.", e);
			}
		}
		return decrypted_data;
	};
}

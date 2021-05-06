package rsa;

import java.security.*;
import java.util.Scanner;

import javax.crypto.*;

public class SymEncryption {
	public static void main(String[] args) throws Exception {
		String text = "This is a plaintext for symmetric encryption test";
		System.out.println("Plaintext: " + text);

		// 대칭키 생성
		System.out.println("\n\nAES Key Generation ");
		KeyGenerator keyGen2 = KeyGenerator.getInstance("AES");
		keyGen2.init(128);
		Key key2 = keyGen2.generateKey();
		// System.out.print(key2);
		byte[] printKey2 = key2.getEncoded(); // secretkey 생성완료후 16진수화 하는듯
		System.out.print("Secret key generation complete: ");
		for (byte b : printKey2)
			System.out.printf("%02X ", b);
		System.out.print("\nLength of secret key: " + printKey2.length + " byte");

		// 암호화 과정
		System.out.println("\n\nAES Encryption ");
		Cipher cipher2 = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher2.init(Cipher.ENCRYPT_MODE, key2);

		byte[] plaintext = text.getBytes(); // 평문을 해시화함

		System.out.print("Plaintext : ");
		for (byte b : plaintext)
			System.out.printf("%02X ", b);
		System.out.print("\nPlaintext Length: " + plaintext.length + " byte");

		byte[] ciphertext2 = cipher2.doFinal(plaintext); // 평문 암호화
		System.out.print("\nCiphertext :");
		for (byte b : ciphertext2)
			System.out.printf("%02X ", b);
		System.out.print("\nCiphertext Length: " + ciphertext2.length + " byte");

		// 복호화 과정
		cipher2.init(Cipher.DECRYPT_MODE, key2); // 암호문 복호화
		byte[] decrypttext2 = cipher2.doFinal(ciphertext2);
		String output2 = new String(decrypttext2, "UTF8");
		System.out.print("\nDecrypted Text:" + output2);
	}
}

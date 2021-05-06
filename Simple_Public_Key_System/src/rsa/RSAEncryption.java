package rsa;

import java.security.*;
import javax.crypto.*;
import java.util.*;

public class RSAEncryption {
	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        
		// 개인, 공용키 생성 
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048); 
        KeyPair keyPair = generator.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();     
        System.out.print("\n Public Key : " + publicKey);
        
        System.out.println("\n=== RSA Key Generation ===");
        byte[] pubk = publicKey.getEncoded(); //퍼블릭 키 생성후 인코딩
        byte[] prik = privateKey.getEncoded(); // 프라이빗 키 생성후 인코딩
        System.out.print("\n Public Key : ");
        for(byte b: pubk) System.out.printf("%02X ", b);
        System.out.println("\n Public Key Length : "+pubk.length+ " byte" );	
        System.out.print("\n Private Key : ");
        for(byte b: prik) System.out.printf("%02X ", b);
        System.out.println("\n Private Key Length : "+prik.length+ " byte" );
       
        //전송할 입력값 받기 
        System.out.println("\n=== RSA Encryption ===");
        Scanner s = new Scanner(System.in);
        System.out.print("Input the plaintext to be encrypted... = "); //사용자 입력값을 받아 암호문으로 변환
        String text = s.next();  
        byte[] t0 = text.getBytes(); //입력값을 바이트형태로 변환
        System.out.print("\n Plaintext : "+text+"\n");
        for(byte b: t0) System.out.printf("%02x ", b);
        System.out.println("\n Plaintext Length : "+t0.length+ " byte" );	
        // 암호화 과정
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey); //퍼블릭키로 암호화
        byte[] b0 = cipher.doFinal(t0);
        System.out.print("\n\n Ciphertext : ");
        for(byte b: b0) System.out.printf("%02x ", b);
        System.out.println("\n Ciphertext Length : "+b0.length+ " byte" );	

        //복호화 과정
        System.out.println("=== RSA Decryption ==="); // 전달받은 암호문을 privatekey로 복호화
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] b1 = cipher.doFinal(b0);
        System.out.print("\n Recovered Plaintext : "+ new String(b1) +"\n"); 
        for(byte b: b1) System.out.printf("%02x ", b);
        System.out.println("\n Recovered Plaintext Length : "+b1.length+ " byte" );	
        
    }
}

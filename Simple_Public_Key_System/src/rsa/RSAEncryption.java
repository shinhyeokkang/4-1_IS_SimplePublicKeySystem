package rsa;

import java.security.*;
import javax.crypto.*;
import java.util.*;

public class RSAEncryption {
	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        
		// ����, ����Ű ���� 
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048); 
        KeyPair keyPair = generator.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();     
        System.out.print("\n Public Key : " + publicKey);
        
        System.out.println("\n=== RSA Key Generation ===");
        byte[] pubk = publicKey.getEncoded(); //�ۺ� Ű ������ ���ڵ�
        byte[] prik = privateKey.getEncoded(); // �����̺� Ű ������ ���ڵ�
        System.out.print("\n Public Key : ");
        for(byte b: pubk) System.out.printf("%02X ", b);
        System.out.println("\n Public Key Length : "+pubk.length+ " byte" );	
        System.out.print("\n Private Key : ");
        for(byte b: prik) System.out.printf("%02X ", b);
        System.out.println("\n Private Key Length : "+prik.length+ " byte" );
       
        //������ �Է°� �ޱ� 
        System.out.println("\n=== RSA Encryption ===");
        Scanner s = new Scanner(System.in);
        System.out.print("Input the plaintext to be encrypted... = "); //����� �Է°��� �޾� ��ȣ������ ��ȯ
        String text = s.next();  
        byte[] t0 = text.getBytes(); //�Է°��� ����Ʈ���·� ��ȯ
        System.out.print("\n Plaintext : "+text+"\n");
        for(byte b: t0) System.out.printf("%02x ", b);
        System.out.println("\n Plaintext Length : "+t0.length+ " byte" );	
        // ��ȣȭ ����
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey); //�ۺ�Ű�� ��ȣȭ
        byte[] b0 = cipher.doFinal(t0);
        System.out.print("\n\n Ciphertext : ");
        for(byte b: b0) System.out.printf("%02x ", b);
        System.out.println("\n Ciphertext Length : "+b0.length+ " byte" );	

        //��ȣȭ ����
        System.out.println("=== RSA Decryption ==="); // ���޹��� ��ȣ���� privatekey�� ��ȣȭ
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] b1 = cipher.doFinal(b0);
        System.out.print("\n Recovered Plaintext : "+ new String(b1) +"\n"); 
        for(byte b: b1) System.out.printf("%02x ", b);
        System.out.println("\n Recovered Plaintext Length : "+b1.length+ " byte" );	
        
    }
}

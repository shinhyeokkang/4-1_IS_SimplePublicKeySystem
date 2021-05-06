package rsa;

import java.security.*;

public class RSASignature {
	public static void main(String[] args) throws Exception {
		// Ű ����
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    SecureRandom random = new SecureRandom();
    kpg.initialize(1024,random);
    KeyPair keyPair = kpg.genKeyPair();
    PublicKey publicKey = keyPair.getPublic();
    PrivateKey privateKey = keyPair.getPrivate(); 
    byte[] pubk = publicKey.getEncoded();
    byte[] prik = privateKey.getEncoded();
    
    
    System.out.println("\n\nRSA key generation ");
    System.out.print("\nPublic Key : ");
    for(byte b: pubk) System.out.printf("%02X ", b);
    System.out.println("\nPublic Key Length : "+pubk.length+ " byte" );	
    System.out.print("\nPrivate Key : ");
    for(byte b: prik) System.out.printf("%02X ", b);
    System.out.println("\nPrivate Key Length : "+prik.length+ " byte" );
    	
    // ���� ������ �����ϰ� ����Ʈ�� ��ȯ
    String sigData="asdfasdfasdfasdfasdfdf";
    byte[] data = sigData.getBytes("UTF8");
    System.out.print("\nPlaintext : "+data+"\n");
    //----------------------------------------���� ����
    System.out.println("\n\nSHA512WithRSA");
    Signature sig2 = Signature.getInstance("SHA512WithRSA");
    sig2.initSign(keyPair.getPrivate()); //�����̺� Ű �޾ƿ�
    
    System.out.println("Data Length Original: "+ data.length);
    System.out.print("Data original: ");
    for(byte b: data) System.out.printf("%02X ", b);
    sig2.update(data); // �����Ϳ� ������
    System.out.println("\nData Length update: "+ data.length);
    System.out.print("Data updated: ");
    for(byte b: data) System.out.printf("%02X ", b);
    
    byte[] signatureBytes2 = sig2.sign(); // ���� �Ϸ��� ������
    System.out.print("\nSingature length: "+signatureBytes2.length+ " bytes"); //���ڼ����� �������� ������� �׻� 128bytes�� �����Ѵ�
    System.out.print("\nSingature: ");
    for(byte b: signatureBytes2) System.out.printf("%02X ", b);
    System.out.print("\nSingature length: "+signatureBytes2.length*8+ " bits");
    
    // ���� ����
    sig2.initVerify(keyPair.getPublic()); // ���ø� Ű�� ���� ��ü ���� 
    sig2.update(data); // ���� �غ� // ���� ���������ͷ� sig2 �����Ͽ� �ؽ�ȭ�ڵ� ����
    System.out.print("\nVerification: ");
    System.out.print(sig2.verify(signatureBytes2)); //signature �񱳸� ���� Authentication �ϴµ� (��ȣȭ�� �Ұ�)// ��ȣȭ�ؼ� �ؽõ� �������ϰ�, ���� �ؽ�ȭ�� ���� �� 
    
  }
}
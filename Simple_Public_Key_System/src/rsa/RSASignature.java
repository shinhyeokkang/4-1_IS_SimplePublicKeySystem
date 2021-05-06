package rsa;

import java.security.*;

public class RSASignature {
	public static void main(String[] args) throws Exception {
		// 키 생성
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
    	
    // 보낼 데이터 생성하고 바이트로 변환
    String sigData="asdfasdfasdfasdfasdfdf";
    byte[] data = sigData.getBytes("UTF8");
    System.out.print("\nPlaintext : "+data+"\n");
    //----------------------------------------서명 과정
    System.out.println("\n\nSHA512WithRSA");
    Signature sig2 = Signature.getInstance("SHA512WithRSA");
    sig2.initSign(keyPair.getPrivate()); //프라이빗 키 받아옴
    
    System.out.println("Data Length Original: "+ data.length);
    System.out.print("Data original: ");
    for(byte b: data) System.out.printf("%02X ", b);
    sig2.update(data); // 데이터에 서명함
    System.out.println("\nData Length update: "+ data.length);
    System.out.print("Data updated: ");
    for(byte b: data) System.out.printf("%02X ", b);
    
    byte[] signatureBytes2 = sig2.sign(); // 서명 완료한 데이터
    System.out.print("\nSingature length: "+signatureBytes2.length+ " bytes"); //전자서명은 원본값에 상관없이 항상 128bytes를 유지한다
    System.out.print("\nSingature: ");
    for(byte b: signatureBytes2) System.out.printf("%02X ", b);
    System.out.print("\nSingature length: "+signatureBytes2.length*8+ " bits");
    
    // 검증 과정
    sig2.initVerify(keyPair.getPublic()); // 퍼플릭 키로 검증 객체 생성 
    sig2.update(data); // 검증 준비 // 받은 원본데이터로 sig2 갱신하여 해시화코드 저장
    System.out.print("\nVerification: ");
    System.out.print(sig2.verify(signatureBytes2)); //signature 비교를 통해 Authentication 하는듯 (복호화는 불가)// 복호화해서 해시된 원본파일과, 직접 해시화한 값을 비교 
    
  }
}
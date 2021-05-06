package client_server;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import javax.swing.border.EtchedBorder; 


public class GUI extends JFrame{
	  
		public static final int DEFAULT_BUFFER_SIZE = 1024;
		public static final int DEFAULT_AES_SIZE = 16;
		public static final int DEFAULT_SIGN_SIZE = 256;
		public static int port = 10001;

		public static boolean isServer = false;
		// 라디오 버튼 생성
    	JRadioButton rd1 = new JRadioButton("Server");
    	JRadioButton rd2 = new JRadioButton("Client");
    	JTextField portNum;
    	JLabel porttitle;
    	JTextArea modeArea;
    	JTextArea connectArea;
    	static JTextArea chatArea;
    	JTextField chatArea2;
    	JTextArea fileArea;
        JTextArea keyArea;
        JTextArea keystatArea;
        JLabel statetitle;
        JLabel iptitle;
        JLabel state;
        JLabel ipadress;
        
        JButton keygenbtn = new JButton("Generate key");

        JButton loadbtn = new JButton("Load from a file");
        JButton savebtn = new JButton("Save into a file");            
        JButton sendpubbtn = new JButton("Send Public key");
        
        ServerSocket serverSocket;
        Socket socket;

        
    	DataInputStream dis;
    	DataOutputStream dos;	
    	
    	OutputStream os = null;
    	ObjectOutputStream outO = null;
    	InputStream is = null;
    	ObjectInputStream inO =  null;
        
		BufferedOutputStream bout=null;
		BufferedInputStream bin = null;
		FileInputStream fin =null;
		FileOutputStream fos = null;
        
        DataOutputStream dout = null;
        DataInputStream din = null;
        
        // key parameters
		private KeyPair keypair = null;
		PublicKey publickey = null;
		PrivateKey privatekey = null;
		PublicKey recpublickey = null;
		byte[] pubk = null;
	    byte[] prik = null;
        
        private String encoded_pubKey = null;
        private String encoded_priKey = null;
        
        private SecretKey secretkey = null;       
        private String encodedSecretkey = null;
        private byte[] encrypted_AESkey = null;
        private SecretKeySpec skeySpec = null;
        private boolean safeConnect = false;
        
        //database
        HashMap<String,String> keymap; 
        
        // 생성자를 통해 GUI 초기 세팅을 해준다.
        public GUI(){
            
            // 윈도우 제목(Title)을 생성
            setTitle("Simple Public Key System");
 
            // 종료 버튼 생성
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
            // 이 부분부터 원하는 버튼, 레이블, 콤보박스 등등 설정
            
            // GridLayout을 설정
            this.setLayout(new GridLayout(4,1,5,3));

            // 좌상단패널 생성
            JPanel lb1 = new JPanel();
            //lb1.setLayout(new BoxLayout(lb1,BoxLayout.Y_AXIS));
 
            //레이블 생성
            JLabel conMode = new JLabel("Connection Mode");
            conMode.setAlignmentX(CENTER_ALIGNMENT);
            //lb1.add(conMode);
            
            //라디오 패널 생성
            JPanel radioP = new JPanel();
            radioP.setLayout(new BoxLayout(radioP,BoxLayout.X_AXIS));

            // 라디오 버튼 선택여부 체크
            rd1.addItemListener(new MyItemListener());
            rd2.addItemListener(new MyItemListener());
            
            
            radioP.setAlignmentX(CENTER_ALIGNMENT);
            
            // 1번 라디오 버튼 눌러져있도록
            //rd1.setSelected(true);
            
            // 라디오 버튼을 그룹화 하기위한 객체 생성
            ButtonGroup groupRd = new ButtonGroup();
            
            // 그룹에 라디오 버튼 포함시킨다.
            groupRd.add(rd1);
            groupRd.add(rd2);

            radioP.add(rd1);
            radioP.add(rd2);

            //lb1.add(radioP); // 좌상단 패널에 라디오 패널 삽입
            
            // Connect 버튼 생성
            JButton btn1 = new JButton("Connect!");
            btn1.setAlignmentX(CENTER_ALIGNMENT);
            
            //버튼에 리스너를 달아 클릭시 선택한 채널 개설
            btn1.addActionListener(new ActionListener() {
            	@Override
            	public void actionPerformed(ActionEvent e) {
            		portNum.setEditable(false);
            		if(isServer == true) {
            			//상대방이 접속할 수 있도록 서버소켓을 만들고 통신할 수 있는 준비 작업!
            			//네트워크 작업을 Main Thread가 하게하면 다른 작업(키보드 입력, 클릭 등..)들을 
            			//전혀 할 수 없음, 프로그램이 멈춤, 그래서 Main은 UI작업에 전념하도록 하고, 
            			//다른 작업들(오래 걸리는)은  별도의 Thread에게 위임하는 것이 적절함.	
            			ServerThread serverThread = new ServerThread();
            			serverThread.setDaemon(true); //메인 끝나면 같이 종료
            			serverThread.start();

            			}
            		else {

                    	//서버와 연결하는 네트워크 작업 : 스레드 객체 생성 및 실행
                		ClientThread clientThread = new ClientThread();
                		clientThread.setDaemon(true);
                		clientThread.start();


            			}
            		
            	}
            });
            
            //좌상단 박스레이아웃 생성 // ButtonGroup < Box < Panel
            Box leftBox = Box.createVerticalBox();

            leftBox.add(Box.createVerticalStrut(10));
            leftBox.add(conMode);
            leftBox.add(radioP);
            leftBox.add(btn1); // 좌상단패널에 버튼 패널 삽입
            leftBox.add(Box.createVerticalStrut(10));
            //leftBox.add(Box.createHorizontalStrut(160));
            lb1.add(leftBox);
            
            
            
            //////////////////////------------------------------------------우상단
            //Mode Text field
            JPanel righttopPanel = new JPanel();
            
            
            Box stateBox = Box.createHorizontalBox();
            statetitle = new JLabel("State of you:  ");
            state = new JLabel(" Server ");
            stateBox.add(statetitle);
            stateBox.add(state);
            
            Box ipBox = Box.createHorizontalBox();
            iptitle = new JLabel("IP Address:  ");
            ipadress = new JLabel("000.000.0.0");
            ipBox.add(iptitle);
            ipBox.add(ipadress);


            Box portBox = Box.createHorizontalBox();
            porttitle = new JLabel("Port Number:  ");
            portNum = new JTextField(3);
            String sPort = Integer.toString(port);
            portNum.setText(sPort);
            portBox.add(porttitle);
            portBox.add(portNum);
            
            
            //우상단 박스레이아웃 생성 // TextArea < Box < Panel
            Box rightBox = Box.createVerticalBox();
            rightBox.add(Box.createVerticalStrut(10));
            rightBox.add(stateBox);
            rightBox.add(ipBox);
            rightBox.add(portBox);

            //rightBox.add(modeArea);
            righttopPanel.add(rightBox);
            
            // 수평형태의 박스를 생성해 두 패널을 가로로 삽입
            Box top = Box.createHorizontalBox();
            top.add(lb1); //상단 패널에 좌상단 패널 삽입
            top.add(righttopPanel); // 상단 패널에 우상단 패널 삽입
            top.add(Box.createHorizontalStrut(40));

            
            //Connection Text field
            JPanel connectPanel = new JPanel();
            connectArea = new JTextArea(3,50); // connect 정보 출력하는 text field 선언
            
            connectPanel.setBackground(Color.orange);
            connectArea.setEditable(false); // 출력 전용창으로 설정
            JScrollPane connectscrollPane = new JScrollPane(connectArea);
            
            connectPanel.add(connectscrollPane);
            //connectArea.append("Connected with A!!!!!"); // 모드창에 출력은 이런식으로 
            
            
            // connectionmode, servertext, 상태창 삽입할 box
            Box north = Box.createVerticalBox();
            
            north.add(top); // gridlayout에 상단 패널 삽입
            north.add(connectPanel);
            //north.add(Box.createVerticalStrut(5));
            this.add(north);
            
            
            
            // 키 교환 박스 영역        

            //수직 배치 박스 레이아웃을 생성
            Box keyBox = Box.createVerticalBox();


            
            //Box객체 left를 패널에 추가
            keyArea = new JTextArea(4,10); //3  커뮤니케이션 모드 정보 출력하는 text field 선언
            keyArea.setEditable(false); // 출력 전용창으로 설정
            JScrollPane keyscrollPane = new JScrollPane(keyArea);
            // 키 제어 버튼들 넣을 박스 생성
            Box keybtnBox = Box.createHorizontalBox();
    
            
            keybtnBox.add(keygenbtn);
            keybtnBox.add(Box.createHorizontalStrut(30)); 
            keybtnBox.add(loadbtn);
            keybtnBox.add(Box.createHorizontalStrut(30)); 
            keybtnBox.add(savebtn);
            
            
            // 서버는 키 쌍, 클라이언트는 대칭키 생성해서 파일로 저장 
            keygenbtn.addActionListener( new ActionListener() {			


				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub

					keypair = generateRSAKey();
					publickey = keypair.getPublic();
					privatekey = keypair.getPrivate();
					encoded_pubKey = Base64.getEncoder().encodeToString(publickey.getEncoded());
					encoded_priKey = Base64.getEncoder().encodeToString(privatekey.getEncoded());
					
					////////////////키 생성 후 파일로 저장 코드 필요 
					if(isServer == true) {
						savePublickey("Server", encoded_pubKey);
						
					}else {
					
					savePublickey("Client", encoded_pubKey);
					}
					
					
					
					keyArea.append("Keypair Generated!\n");
					
				}


    		});
            loadbtn.addActionListener( new ActionListener() {			


        				@Override
        				public void actionPerformed(ActionEvent e) {
        					// TODO Auto-generated method stub
        					String targetpubkey = loadPublickey();

        					keymap.put(sPort, targetpubkey);
        					
        					keyArea.append("Other's public key has been loaded from a file!\n");
        					keystatArea.append("\n["+ sPort+ "'s public key]: " );
        					keystatArea.append(targetpubkey);
        				}


            		});
            savebtn.addActionListener( new ActionListener() {			


        				@Override
        				public void actionPerformed(ActionEvent e) {
        					// TODO Auto-generated method stub
        					////////////////키맵에서 반복해서 불러온 후 파일로 저장 코드 
        					String encodedpubkey = null;
        					for(String d : keymap.keySet()) {
        						encodedpubkey = keymap.get(d);
            					savePublickey(d, encodedpubkey);
        					}
        					keyArea.append("\nAll keys are saved in file!\n");
        					
        				}


            		});
            
            
            // 3행 생성
            Box keystatBox = Box.createHorizontalBox();

            keystatArea = new JTextArea(2,10); //3  커뮤니케이션 모드 정보 출력하는 text field 선언
            keystatArea.setEditable(false); // 출력 전용창으로 설정
            //keystatArea.setText("Received Key List: ");
            JScrollPane statscrollPane = new JScrollPane(keystatArea);
            
            // 서버일 경우 공개키를 클라이언트에게 전송 / 클라일경우 오류 반환
            sendpubbtn.addActionListener( new ActionListener() {			


				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					 ///////////////키 불러오기 
					if(isServer == false) {
						fileArea.append("<---Please use this AES-safe Send button!");
						return;
					}
					sendPublickey(keypair);


				}


    		});
            
            keystatBox.add(sendpubbtn);
            keystatBox.add(Box.createHorizontalStrut(20)); 
            keystatBox.add(statscrollPane);
            
            
            JPanel keyPanel = new JPanel();
            //패널의 테두리선을 에칭효과로 지정
            keyPanel.setBorder(new EtchedBorder());       
            

            keyBox.add(keyscrollPane); //3 -> 2
            //보기 좋게 배치하기 위해 투명 컴포넌트로 공간 확보
            keyBox.add(Box.createVerticalStrut(6)); 
            keyBox.add(keybtnBox);
            keyBox.add(Box.createVerticalStrut(6));
            keyBox.add(keystatBox);
            keyPanel.add(keyBox); //2 -> 1
            this.add(keyPanel); //1
            //keyArea.append("KEY!!!!!"); // 모드창에 출력은 이런식으로 
     
            
            
            // 채팅 파트 생성
            JPanel chatPanel = new JPanel(); //1
            chatPanel.setBackground(Color.yellow);
            
            Box chatBox = Box.createVerticalBox(); //2
            //chatBox.add(Box.createVerticalStrut(80));
          
            chatArea = new JTextArea(6,50); //3  커뮤니케이션 모드 정보 출력하는 text field 선언
            chatArea.setEditable(false); // 출력 전용창으로 설정
            JScrollPane scrollPane = new JScrollPane(chatArea);
 
            Box downchatBox = Box.createHorizontalBox(); //3
            chatArea2 = new JTextField(); //4  커뮤니케이션 모드 정보 출력하는 text field 선언

            JButton btnSend = new JButton("send"); //4
        	//send 버튼 클릭에 반응하는 리스너 추가
    		
    		btnSend.addActionListener( new ActionListener() {			

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					if(safeConnect == false) {
						chatArea.append("Please exchange key to have safe connection \n");
						return;
					}
					sendMessage();
				}

    		});
    		
    		//엔터키 눌렀을 때 반응하기

    		chatArea2.addKeyListener( new KeyAdapter() {

    			//키보드에서 키 하나를 눌렀을때 자동으로 실행되는 메소드..: 콜백 메소드

    			@Override

    			public void keyPressed(KeyEvent e) {				

    				super.keyPressed(e);

    			
    			//입력받은 키가 엔터인지 알아내기, KeyEvent 객체가 키에대한 정보 갖고있음
    				int keyCode = e.getKeyCode();

    				switch(keyCode) {

    				case KeyEvent.VK_ENTER:
    					if(safeConnect == false) {
    						chatArea.append("Please exchange key to have safe connection \n");
    						return;
    					}
    					sendMessage();
    					break;
    				}
    			}
    		});
            
            //chatArea.append("Hi!!!!!"); // 모드창에 출력은 이런식으로 

           
            
            chatBox.add(scrollPane); //3->2
            downchatBox.add(btnSend);//4->3
            //downchatBox.add(Box.createHorizontalGlue());//4->3
            downchatBox.add(chatArea2); //4->3

            chatBox.add(downchatBox);//3->2
            chatPanel.add(chatBox); //2->1
            

          
            this.add(chatPanel); //1
           
            chatPanel.setBorder(new EtchedBorder());  
            //chatPanel.add(chatBox, BorderLayout.CENTER);

            
            // ---------------------------------------------------------------------------파일 파트 생성
            JPanel filePanel = new JPanel(); //1
            filePanel.setBackground(Color.green);
            Box bottomBox = Box.createHorizontalBox(); //2

            
            fileArea = new JTextArea(8,38); //3  커뮤니케이션 모드 정보 출력하는 text field 선언
            fileArea.setEditable(false); // 출력 전용창으로 설정
            //fileArea.append("File Transfer");
            JScrollPane filescrollPane = new JScrollPane(fileArea);
            // 파일전송 버튼 생성
            JButton btnFileSend = new JButton("send file");

            JPanel filebtnPanel = new JPanel();
            filebtnPanel.setLayout(new BorderLayout(0,0));
            btnFileSend.setPreferredSize(new Dimension(100, 100));
            filebtnPanel.add(btnFileSend);
        	//send 버튼 클릭에 반응하는 리스너 추가
    		//sendfile 버튼 클릭시 file 서버 생성하고 전달
    		btnFileSend.addActionListener( new ActionListener() {			

				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					if(safeConnect == false) {
						fileArea.append("Please exchange key to have safe connection \n");
						return;
					}
				
					JfileChooserUtil file = new JfileChooserUtil();
					String filepath =file.jFileChooserUtil();
					fileArea.append(filepath);
					sendFile(filepath);
				}

    		});
            
            bottomBox.add(filebtnPanel);
            bottomBox.add(Box.createHorizontalStrut(15)); 
            bottomBox.add(filescrollPane); //3->2

            filePanel.add(bottomBox); //2->1
            

            
            filePanel.setBorder(new EtchedBorder());
            this.add(filePanel); //1


            // 윈도우 창 크기 설정(가로, 세로)
            setSize(500, 700);
            
            // 이 메소드를 이용해야 윈도우 창이 나타난다.
            setVisible(true);
        

    		
    		addWindowListener(new WindowAdapter() {

    			@Override //클라이언트 프레임에 window(창) 관련 리스너 추가

    			public void windowClosing(WindowEvent e) {				

    				super.windowClosing(e);

    				try {
    					if(dos != null) dos.close();
    					if(dis != null) dis.close();
    					if(socket != null) socket.close();

    				} catch (IOException e1) {					
    					e1.printStackTrace();
    				}
    			}			

    		});
        
        
        } // 생성자




	//이너클래스 : 서버소켓을 생성하고 클라이언트의 연결을 대기하고,

    	//연결되면 메시지를 지속적으로 받는 역할 수행

    	class ServerThread extends Thread {

    		@Override
    		public void run() {			
    			try {  //서버 소켓 생성 작업
    				//서버의 공개키 데이터베이스
    				MakeDir md = new MakeDir();
    				md.makeDir("Server");
    				keymap = new HashMap<>();//new에서 타입 파라미터 생략가능
    				//유저 입력 port 받아오기
    				port = Integer.parseInt(portNum.getText());
    				serverSocket = new ServerSocket(port);
    				connectArea.append("서버소켓이 준비됐습니다...\n");
    				connectArea.append("클라이언트의 접속을 기다립니다.\n");				
    				socket = serverSocket.accept();//클라이언트가 접속할때까지 커서(스레드)가 대기
    				connectArea.append(socket.getInetAddress().getHostAddress() + "님이 접속하셨습니다.\n");

					
					//통신을 위한 스트림 생성
				 	is = socket.getInputStream();
    				bin = new BufferedInputStream(is);
    				os = socket.getOutputStream();
    				bout = new BufferedOutputStream(os);
    				dis = new DataInputStream(bin);
    				dos = new DataOutputStream(bout);
    				
    				
    				String filename =null;

    
   
    				
    				 while(true) {
    					 if(encrypted_AESkey == null) {
    						 // client로부터 퍼블릭키로 암호화된 대칭키를 받는 코드 // ObjectInputsream 써야겠다

    						 	inO = new ObjectInputStream(is);
    							encrypted_AESkey = (byte[])inO.readObject();
    							String encryted_AESkey_string = new String(Base64.getEncoder().encode(encrypted_AESkey));
    							keyArea.append("\n[Received encrypted AES key from Client!]");
    							keyArea.append(encryted_AESkey_string + "\n");
    						 
    					 }
    					 else {
    						 // privatekey로 복호화해 대칭키 저장하는 코드
    						 skeySpec = new SecretKeySpec(decryptRSA(encrypted_AESkey, privatekey), "AES");
    						 
    						 
    						 encodedSecretkey = Base64.getEncoder().encodeToString(skeySpec.getEncoded());
    						 
    						 // print AES secret key 
    						 keyArea.append("[AES Secretkey ready!]");
    						 keyArea.append(encodedSecretkey);
    						 safeConnect = true;
    						 chatArea.append("Text encryption started\n");
    						 break;
    					 }
    				 }
    				
    				
    				
    				while(true) {
    					byte[] data = new byte[DEFAULT_BUFFER_SIZE];
    					//bin = new BufferedInputStream(dis); // 데이터를 버퍼로 받아옴
    					
        				// AES 암호화된 데이터를 받아 복호화 
        				byte[] encryptedData = new byte[1040]; /////////////////////// 처음 받아오는 암호문의 길이가 일정한 크기 이하이여야함!!! 
        				int initsize = dis.read(encryptedData);
    					byte[] rawdata = new byte[initsize];
    					
       					for(int i=0;i<initsize;i++) {
    						rawdata[i] = encryptedData[i]; // 적합한 길이의 새로운 리스트에 데이터저장 
    					}
    					
    					String isize=Integer.toString(initsize);
    					chatArea.append("Initial byte len: "+ isize);

        				
    					
        				data = decryptAES(rawdata, skeySpec);

        				int size = data.length;
    					String csize=Integer.toString(size);
    					//fileArea.append(csize);
    					String protocol = new String(data, 0, 6); // 헤더만 받아와서 데이터 종류 확인
    					
//    					// 클라이언트가 자신의 퍼블릭키를 AES채널을 통해 보낼 경우
//    					if(protocol.equals("[CPUK]")) {
//
//        					recpublickey = (PublicKey)inO.readObject();
//        					encoded_pubKey = Base64.getEncoder().encodeToString(recpublickey.getEncoded());
//        					
//    					}
//    					
    					
    					
    					if(protocol.equals("[MESG]")) { //헤더가 메시지이면
    					//상대방이 보내온 데이터를 읽기
    					//String msg = dis.readUTF();//상대방이 보낼때까지 대기
    					//chatArea.append(" [Client] : " + msg + "\n");
    					chatArea.append(" [Client] : " + (new String(data, 6,size-6).trim()) + "\n");
    					chatArea.setCaretPosition(chatArea.getText().length());
    					} else if(protocol.equals("[SFNE]")) { // 헤더가 파일명 타입이면
    						try{
    							filename = new String(data, 6, size-6).trim();
    							fileArea.append(filename+" 을 받습니다!\n");
    							if(filename != null) fos = new FileOutputStream("Server//"+filename);
    						} catch(FileNotFoundException ex) {
    							ex.printStackTrace();
    						}
    					} else if(protocol.equals("[SFIL]")) { //헤더가 파일 데이터일 경우 
    						try {
    							fos.write(data, 8, (int)data[6]*100 + data[7]); // 헤더 제외하고, 파일크기를 받아서 그만큼의 바이트를 저장
    						} catch(IOException ex) {
    							ex.printStackTrace();
    						}
    						if((int)data[6]*100 + data[7] < 1016) { // 버퍼의 크기가 헤더제외 최댓값보다 작을 경우 == 마지막 버퍼일 경우
    							try {
    								if(fos != null) fos.close();
    								fileArea.append(filename +" 파일 수신완료!\n");
    							} catch(IOException ex) {
    								ex.printStackTrace();
    								}
    							}
    							
    						}
    					chatArea.setCaretPosition(chatArea.getText().length());
    				} // while end				

    				

    			} catch (IOException e) {
    				connectArea.append("클라이언트가 나갔습니다.\n");
    			} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

    		}

    	}

    	//이너클래스 : 서버와 연결하는 네트워크 작업 스레드
    	class ClientThread extends Thread {
    		@Override
    		public void run() {
    			try {
    				//
    				MakeDir md = new MakeDir();
    				md.makeDir("Client");
    		        keymap = new HashMap<>(); 
    				//유저 입력 port 값 받아오기
    				port = Integer.parseInt(portNum.getText());
    				
    				socket = new Socket(InetAddress.getLocalHost(), port);
    				chatArea.append("서버에 접속됐습니다.\n");

					//통신을 위한 스트림 생성
					is = socket.getInputStream();
					os = socket.getOutputStream();
    				bin = new BufferedInputStream(is);
    				bout = new BufferedOutputStream(os);
    				dis = new DataInputStream(bin);
    				dos = new DataOutputStream(bout);
    				
    				String filename =null;
    				
   
    				while(true) {
    				if(recpublickey == null) {
    					// 파일로 받을지, objctinputstream으로받을지 생각해볼것
    					// getting server's publickey

    					inO = new ObjectInputStream(is);
    					recpublickey = (PublicKey)inO.readObject();
    					encoded_pubKey = Base64.getEncoder().encodeToString(recpublickey.getEncoded());
    					
    					// save server's pubkey
    					keymap.put(portNum.getText(), encoded_pubKey);
    					
    					// print pubkey
    					keystatArea.append("[RSA PublicKey from server]: ");
    					keystatArea.append(encoded_pubKey);
    				}
    				else {
    					// AES 비밀키를 생성 후 공개키로 암호화해 보내는 코드 
    					secretkey = generateAESkey();
    					skeySpec = new SecretKeySpec(secretkey.getEncoded(), "AES");
    					encrypted_AESkey = encryptRSA(secretkey.getEncoded(),recpublickey); 
    					String encodedKey = Base64.getEncoder().encodeToString(secretkey.getEncoded());
    					keyArea.append("\n[Encrypted Secretkey has been sent to server]");
    					keyArea.append(encodedKey);
    					
    					// server 로 비밀키를 전송 
    					sendEncryptedAESkey(encrypted_AESkey);
    					safeConnect = true;
						chatArea.append("Text encryption started\n");
    					break;
    					}
    				}
    				
    				while(true) {
    					
    					////////////////전자서명 받아서 떼어내고 원본이랑 비교하는 코드 
    					
    					byte[] firstdata = new byte[DEFAULT_BUFFER_SIZE+DEFAULT_AES_SIZE+DEFAULT_SIGN_SIZE];
    					byte[] data = new byte[DEFAULT_BUFFER_SIZE];
    					byte[] sign = new byte[DEFAULT_SIGN_SIZE];
        				byte[] encryptedData = new byte[DEFAULT_BUFFER_SIZE+DEFAULT_AES_SIZE];
    					
        				int initsize = dis.read(firstdata); // 1024 + 16 + 256 = 1296
        				
    					String isize=Integer.toString(initsize);
    					chatArea.append("Initial byte len: "+ isize+"\n");

    					if(initsize > 1024) { //데이터 송신의 경우 서명 분리 필요 
						//sign 분리 코드 
						System.arraycopy(firstdata, 0, encryptedData, 0, encryptedData.length); // AES 암호화된 바이트 분리
						System.arraycopy(firstdata, encryptedData.length, sign, 0, sign.length); // sign 분리
	
						fileArea.append("  Get Encrypted Length: "+encryptedData.length); //1040이어야함! (암호화시 반드시 1040으로 전환된다)
						fileArea.append("  Get Sign Length: "+sign.length+"\n"); //256이어야함!
						

						
						// 서명 검증 과정
						verifySign(encryptedData, sign);
						
					    
						data = decryptAES(encryptedData, skeySpec);
					    
    					}
    					else {

        				// AES 암호화된 데이터를 받아 복호화 
    					byte[] rawdata = new byte[initsize];
    					
    					for(int i=0;i<initsize;i++) {
    						rawdata[i] = firstdata[i];// 적합한 길이의 새로운 리스트에 데이터저장 
    					}

    					
        				data = decryptAES(rawdata, skeySpec);
    					}
    					
    					
    					
//    					byte[] encryptedData = new byte[DEFAULT_BUFFER_SIZE]; /////////////////////// 처음 받아오는 암호문의 길이가 일정한 크기 이하이여야함!!! 
//        				//int initsize = dis.read(encryptedData);
//    					data = decryptAES(encryptedData, skeySpec);
        				int size = data.length;
    					String csize=Integer.toString(size);
    					//bin = new BufferedInputStream(dis); // 데이터를 버퍼로 받아옴

    					//int size = dis.read(data); // 받아온 버퍼를 바이트리스트 형태로 저장
    					//fileArea.append(csize);
    					String protocol = new String(data, 0, 6); // 헤더만 받아와서 데이터 종류 확인 
    					
    					if(protocol.equals("[MESG]")) { //헤더가 메시지이면
    					//상대방이 보내온 데이터를 읽기
    					//String msg = dis.readUTF();//상대방이 보낼때까지 대기
    					chatArea.append(" [Server] : " + (new String(data, 6,size-6).trim()) + "\n");
    					chatArea.setCaretPosition(chatArea.getText().length());
    					} else if(protocol.equals("[SFNE]")) { // 헤더가 파일명 타입이면
    						try{
    							filename = new String(data, 6, size-6).trim();
    							fileArea.append("\n"+filename+" 을 받습니다.\n");
    							if(filename != null) fos = new FileOutputStream("Client//"+ filename);
    						} catch(FileNotFoundException ex) {
    							ex.printStackTrace();
    						}
    					} else if(protocol.equals("[SFIL]")) { //헤더가 파일 데이터일 경우 
    						try {
    							fos.write(data, 8, (int)data[6]*100 + data[7]); // 헤더 제외하고, 파일크기를 받아서 그만큼의 바이트를 저장
    						} catch(IOException ex) {
    							ex.printStackTrace();
    						}
    						if((int)data[6]*100 + data[7] < 1016) { // 버퍼의 크기가 헤더제외 최댓값보다 작을 경우 == 마지막 버퍼일 경우
    							try {
    								if(fos != null) fos.close();
    								fileArea.append("\n"+filename +" 파일 수신완료!\n");
    							} catch(IOException ex) {
    								ex.printStackTrace();
    								}
    							}
    							
    						}
    					chatArea.setCaretPosition(chatArea.getText().length());
    				} // while end		



    			} catch (UnknownHostException e) {

    				connectArea.append("서버 주소가 이상합니다.\n");

    			} catch (IOException e) {

    				connectArea.append("서버 연결에 실패했습니다. Port Number를 확인해주세요\n");
    				portNum.setEditable(true);

    			} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 

    		}

    	}
  
    	
    	class MyItemListener implements ItemListener{

            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()==ItemEvent.DESELECTED){
                    return;
                }
                if(rd1.isSelected()){
                	isServer = true;
                	state.setText(" Server ");
                	try {
						String ip = InetAddress.getLocalHost().getHostAddress();
						ipadress.setText(ip);
					} catch (UnknownHostException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                	
                    
                }
        
                else if(rd2.isSelected()){
                	isServer = false;
                	state.setText(" Client ");
                	try {
						String ip = InetAddress.getLocalHost().getHostAddress();
						ipadress.setText(ip);
					} catch (UnknownHostException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                    
                }
            }
            
        }
    	
    	//메시지 전송하는 기능 메소드

		void sendMessage() {	
			//2.상대방(Server)에게 메시지 전송하기
			//아웃풋 스트림을 통해 상대방에 데이터 전송
			//네트워크 작업은 별도의 Thread가 하는 것이 좋음
			Thread t = new Thread() {
				@Override
				public void run() {
			String msg = chatArea2.getText(); //TextField에 써있는 글씨를 얻어오기
			chatArea2.setText(""); //입력 후 빈칸으로
			chatArea.append(" [Me] : " + msg + "\n");//1.TextArea(채팅창)에 표시
			chatArea.setCaretPosition(chatArea.getText().length());

		
			try {
			msg = "[MESG]" + msg;
			//bout = new BufferedOutputStream(socket.getOutputStream());
			//dos = new DataOutputStream(bout);
			//bout = new BufferedOutputStream(dos); //output 관계 확립하고 다시 작성! 
			//dos.write(msg.getBytes());
			
			// 메세지를 받아 AES 암호화를 거쳐 바이트화 시킨다
			byte[] encryptedMsg = encryptAES(msg, skeySpec);
			
			fileArea.append("\nEncrypted MSG Length: "+encryptedMsg.length); //1040이어야함!
			
			dos.write(encryptedMsg);
			//dos.writeUTF(msg);
			dos.flush();
			} catch(IOException ex) {
				ex.printStackTrace();
			}
			}
		};
		t.start();	
			
		}
		
		void sendFile(String fileLocation) {

			//아웃풋 스트림을 통해 상대방에 데이터 전송
			//네트워크 작업은 별도의 Thread가 하는 것이 좋음
			
			Thread t = new Thread() {
				@Override
				public void run() {
			try{

				// 전자서명 추가코드 더하기 //////////////////////////////////////////////////
				File file = new File(fileLocation);

				int n=0;
				long fileSize = file.length();

				String fileName = file.getName();
				if(fileName == "") {
					fileArea.append("Please choose file to send\n");
					return;
				}
		
				fileArea.append(fileName+" 전송을 시작합니다.\n");
				
				//파일 이름 전송
				String str = "[SFNE]" + fileName;
				
				//파일명을 받아 AES 암호화를 거쳐 바이트화 시킨다
				byte[] encryptedFilename = encryptAES(str, skeySpec);
				
				dos.write(encryptedFilename);
				dos.flush();
				
				// 본 파일 데이터 전송
				byte[] strData = "[SFIL]".getBytes(); // 본 파일 데이터 헤더
				byte[] data = new byte[DEFAULT_BUFFER_SIZE];
				
				for(int i=0;i<6;i++) {
					data[i] = strData[i]; // 버퍼에 파일 데이터 헤더 삽입
				}
				String finalLen;
				int testint;
				try {
					fin = new FileInputStream(file);
					while(true) {
						testint = fin.read(data,8,DEFAULT_BUFFER_SIZE-8); // data의 [8] 부터 [1024]까지 file데이터 저장
						if(testint == -1) break; //파일이 다 불러와지면 반복문 종료
						
						//data에 저장한 데이터 길이를 data[6], data[7]에 나눠서 넣는다
						data[6] = (byte)(testint / 100);
						data[7] = (byte)(testint % 100);
		
						fileArea.append("\nData Length: "+testint); //1024 이하이어야함!
						
						// 데이터를 받아 AES 암호화를 거친다
						
						byte[] encryptedData = encryptByteAES(data, skeySpec);
						
						finalLen = Integer.toString(encryptedData.length);
						fileArea.append("\nEncrypted Length: "+finalLen+"  "); //1040이어야함!
						
						if(isServer == true) {
							//전자서명 추가
							byte[] sign = addSigniture(encryptedData);
							
							fileArea.append("\nSigniture Length: "+ sign.length); //256이어야함!
							
							byte[] sendbytes = new byte[DEFAULT_BUFFER_SIZE+DEFAULT_AES_SIZE+DEFAULT_SIGN_SIZE];
							fileArea.append("\nFinal before Length: "+sendbytes.length); //1040이어야함!
							System.arraycopy(encryptedData, 0, sendbytes, 0, encryptedData.length); //최종 송신값에 AES 암호화된 데이터 삽입
							System.arraycopy(sign, 0, sendbytes, encryptedData.length, sign.length); // 최종 송신값에 (AES암호화된데이터)를 서명한 값을 삽입
							fileArea.append("\nFinal Length: "+sendbytes.length); //1040 + 256 이어야함!
							
							dos.write(sendbytes);
							dos.flush();
							
							
						}else {
							dos.write(encryptedData);
							dos.flush();
							
						}
			
						
					}
					fileArea.append("\nfile sent!\n");
			
			}catch(FileNotFoundException fe){
				System.out.println(fe.getMessage());
			}
			}catch(IOException ie){
				System.out.println(ie.getMessage());
			}finally{
				try{

					if(fin != null) fin.close();
					//if(dout != null) dout.close();
					//if(bin != null) bin.close();
					//if(filesocket!=null) filesocket.close(); //socket을 닫아버리나??
				}catch(IOException ie){
					System.out.println(ie.getMessage());
				}
			}
				}
			};
			t.start();	
		}
		
		
		protected byte[] addSigniture(byte[] encryptedData) {
			// TODO Auto-generated method stub
			Signature sig2;
			 byte[] signatureBytes2 = null;
			
			try {
			sig2 = Signature.getInstance("SHA512WithRSA");
		
		    sig2.initSign(keypair.getPrivate()); //프라이빗 키 받아옴
		    
		    System.out.println("Data Length Original: "+ encryptedData.length);
		    System.out.print("Data original: ");
		    for(byte b: encryptedData) System.out.printf("%02X ", b);
		    sig2.update(encryptedData); // 데이터에 서명함
		    System.out.println("\nData Length update: "+ encryptedData.length);
		    System.out.print("Data updated: ");
		    for(byte b: encryptedData) System.out.printf("%02X ", b);
		    
		    signatureBytes2 = sig2.sign(); // 서명 완료한 데이터

			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SignatureException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				//String finalLen = Integer.toString(signatureBytes2.length);
				//fileArea.append("Signiture Length: "+finalLen); //128이어야함!
			    return signatureBytes2;
			
			
			
			
		}
		protected boolean verifySign(byte[] encryptedData, byte[] sign) {
			boolean tf = false;
			  
		    try {
		    	
			Signature sig2 = Signature.getInstance("SHA512WithRSA");
		    sig2.initVerify(recpublickey); // 퍼플릭 키로 검증 객체 생성 
		    sig2.update(encryptedData); // 검증 준비 // 받은 원본데이터로 sig2 갱신하여 해시화코드 저장
		    fileArea.append("Verification: ");
		  
		    	tf = sig2.verify(sign);
		    	
				fileArea.append(String.valueOf(tf));
			} catch (SignatureException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //signature 비교를 통해 Authentication 하는듯 (복호화는 불가)// 복호화해서 해시된 원본파일과, 직접 해시화한 값을 비교 
		      catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return tf;
			
		}

		/////////////////////////////////////////////서버에서 쓰이는 암호화 메소드 
		
		public void sendPublickey(KeyPair pair) {
			
			try {
			if(keypair == null) {
				keyArea.append("Please create keypair first!!");
				return;
			}
			else {
				
				os = socket.getOutputStream();
				outO = new ObjectOutputStream(os);
				outO.writeObject(publickey);
				outO.flush();
				
				keyArea.append("[Send my RSA Publickey to the Other.]");
				keyArea.append("\n Public Key : ");
		        keyArea.append(encoded_pubKey);
		        keyArea.append("\n Public Key Length : "+publickey.getEncoded().length+ " byte" );	
				keyArea.append("\n Public key has sent to the Other!");
				
			}
			
			}catch(Exception e){
				
				
			}
		}
		

		public void savePublickey(String usercode ,String encoded_pubKey2) {
				// TODO Auto-generated method stub
				keymap.put(usercode, encoded_pubKey2); 
				
				if(isServer == true) {
					usercode = "Server//"+usercode+".txt";
				}else {
					usercode = "Client//"+usercode+".txt";
				}
				
				  File makeFolder = new File(usercode);
			        try{
				        if(makeFolder.exists()) {
				        	keyArea.append("This key is already saved...");
				        	return;
				        	}
				        
			            // BufferedWriter 와 FileWriter를 조합하여 사용 (속도 향상)
			            BufferedWriter fw = new BufferedWriter(new FileWriter(usercode, true));
			             
			            // 파일안에 문자열 쓰기
			            fw.write(encoded_pubKey2);
			            fw.flush();
			 
			            // 객체 닫기
			            fw.close();
			             
			             
			        }catch(Exception e){
			            e.printStackTrace();
			        }
			  
				
			}
		public String loadPublickey() {
			// TODO Auto-generated method stub
			//String encodedPubkey = keymap.get(usercode);
			String encodedPubkey = null;
			//파일 불러오는 코드
			JfileChooserUtil file = new JfileChooserUtil();
			String filepath =file.jFileChooserUtil();
			
			try { 
				
				BufferedReader br = new BufferedReader(new FileReader(filepath));
				
				encodedPubkey = br.readLine(); //인코딩된 퍼블릭키 불러오기
				br.close();
				
			}catch(Exception e) {
				e.printStackTrace();
			}
	
			return encodedPubkey;
		}
		
		public void sendEncryptedAESkey(byte[] encrypted_AESkey) {
			try {
				if(encrypted_AESkey == null) {
					keyArea.append("Please create AES key and encrypt it with Publickey!");
					return;
				}
				else {
					os = socket.getOutputStream();
					outO = new ObjectOutputStream(os);
					outO.writeObject(encrypted_AESkey);
					outO.flush();
					String encryted_AESkey_string = new String(Base64.getEncoder().encode(encrypted_AESkey));
					keyArea.append("\n[encrypted AES key]");
					keyArea.append(encryted_AESkey_string + "\n");
					keyArea.append("encrypted AES key has successfully sent to Server!");
				}
			}catch(Exception e) {
				
			}
		}
		
		//서버상에서 초기에 RSA 공개키와 개인키를 생성한다 
		public KeyPair generateRSAKey() {
			
			KeyPair keyPair = null;
			
			try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
	        generator.initialize(2048); 
	        keyPair = generator.generateKeyPair();
//	        PublicKey publicKey = keyPair.getPublic();
//	        PrivateKey privateKey = keyPair.getPrivate();     
//	        System.out.print("\n Public Key : " + publicKey);
			}catch(Exception e) {
				e.printStackTrace();
			}
			
			return keyPair;
		}
		// 공개키로 암호화된 데이터를 개인키로 복호화 
		public byte[] decryptRSA(byte[] encrypted, PrivateKey privateKey) {
			
			byte[] b1 = null;
			
			try {
	        Cipher cipher = Cipher.getInstance("RSA");
	        //복호화 과정
	        System.out.println("=== RSA Decryption ==="); // 전달받은 암호문을 privatekey로 복호화
	        cipher.init(Cipher.DECRYPT_MODE, privateKey);
	        b1 = cipher.doFinal(encrypted);
	        System.out.print("\n Recovered Plaintext : "+ new String(b1) +"\n"); 
	        for(byte b: b1) System.out.printf("%02x ", b);
	        System.out.println("\n Recovered Plaintext Length : "+b1.length+ " byte" );	
			}catch(Exception e) {
				e.printStackTrace();
			}
	        
			return b1;
		}
		
		////////////////////////////////////////공용으로 쓰이는 AES 메소드들 

		public static byte[] encryptAES(String text, SecretKey key)
		{
			byte[] ciphertext2 = null;
			
			try {
			    
				// 암호화 과정
				System.out.println("\n\nAES Encryption ");
				Cipher cipher2 = Cipher.getInstance("AES/ECB/PKCS5Padding");
				cipher2.init(Cipher.ENCRYPT_MODE, key);

				byte[] plaintext = text.getBytes(); // 평문을 해시화함

//				System.out.print("Plaintext : ");
//				for (byte b : plaintext)
//					System.out.printf("%02X ", b);
//				System.out.print("\nPlaintext Length: " + plaintext.length + " byte");

				ciphertext2 = cipher2.doFinal(plaintext); // 평문 암호화
//				System.out.print("Ciphertext : ");
//				for (byte b : ciphertext2)
//					System.out.printf("%02X ", b);
//				chatArea.append("Ciphertext Length: " + ciphertext2.length + " byte\n");
			    
			} catch (Exception e) {
				e.printStackTrace();
			}
			return ciphertext2;
		}
		
		public static byte[] encryptByteAES(byte[] data, SecretKey key)
		{
			byte[] ciphertext2 = null;
			
			try {
			    
				// 암호화 과정
				System.out.println("\n\nAES Encryption ");
				Cipher cipher2 = Cipher.getInstance("AES/ECB/PKCS5Padding");
				cipher2.init(Cipher.ENCRYPT_MODE, key);

				//byte[] plaintext = text.getBytes(); // 평문을 해시화함
//
//				System.out.print("Plaintext : ");
//				for (byte b : plaintext)
//					System.out.printf("%02X ", b);
				System.out.print("\nData Length: " + data.length + " byte");

				ciphertext2 = cipher2.doFinal(data); // 평문 암호화
				System.out.print("\nEncryptedData Length: " + ciphertext2.length + " byte");
			    
			} catch (Exception e) {
				e.printStackTrace();
			}
			return ciphertext2;
		}
		
		
		public static byte[] decryptAES(byte[] ciphertext, SecretKey key)
		{
			String output2 = null;
			byte[] decrypttext2  = new byte[DEFAULT_BUFFER_SIZE];
			
			try {
				chatArea.append("Ciphertext: " + ciphertext.toString() + " ->");
				Cipher cipher2 = Cipher.getInstance("AES/ECB/PKCS5Padding");
				// 복호화 과정
				cipher2.init(Cipher.DECRYPT_MODE, key); // 암호문 복호화
				decrypttext2 = cipher2.doFinal(ciphertext);
				output2 = new String(decrypttext2, "UTF8");
				System.out.print("\nDecrypted Text:" + output2 + "\n");
			 
			    
			} catch (Exception e) {
				e.printStackTrace();
			}
			return decrypttext2;
		}
		
		///////////////////////////////////////////클라이언트 단에서 사용하는 암호화 메소드
		// 받은 공개키로 데이터를 암호화
		public static byte[] encryptRSA(byte[] plaintext, PublicKey publicKey) 
		{
			byte[] b0 = null;
			
			try {
				
				Cipher cipher = Cipher.getInstance("RSA");
			
				cipher.init(Cipher.ENCRYPT_MODE, publicKey);
				b0 = cipher.doFinal(plaintext);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return b0;
		}
		
		// 클라이언트 단에서 대칭키를 생성 (이를 서버의 공개키로 암호화해서 보낼 예정)
		public static SecretKey generateAESkey() {
			
			// 대칭키 생성
			System.out.println("\n\nAES Key Generation ");
			
			SecretKey key2 = null;
			
			try {
				
				KeyGenerator keyGen2 = KeyGenerator.getInstance("AES");
				keyGen2.init(128);
				key2 = keyGen2.generateKey();
			    
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return key2; 
		}
		
		public class MakeDir {
			 
		    public void makeDir(String folderName) {
		        
		        // 폴더를 만들 디렉토리 경로(Window 기반)
		        //String folderPath = "C://";
		        
		        File makeFolder = new File(folderName);
		 
		        // folderPath의 디렉토리가 존재하지 않을경우 디렉토리 생성.
		        if(!makeFolder.exists()) {
		            
		            // 폴더를 생성합니다.
		            makeFolder.mkdir(); 
		            System.out.println("폴더를 생성합니다.");
		            
		            // 정성적으로 폴더 생성시 true를 반환합니다.
		            System.out.println("폴더가 존재하는지 체크 true/false : "+makeFolder.exists());
		            
		        } else {
		            System.out.println("이미 해당 폴더가 존재합니다.");
		        }
		        
		    }
		    
		}

    
    public static void main(String[] args){
        
        GUI gui = new GUI();
    }
}

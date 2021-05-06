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
		// ���� ��ư ����
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
        
        // �����ڸ� ���� GUI �ʱ� ������ ���ش�.
        public GUI(){
            
            // ������ ����(Title)�� ����
            setTitle("Simple Public Key System");
 
            // ���� ��ư ����
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
            // �� �κк��� ���ϴ� ��ư, ���̺�, �޺��ڽ� ��� ����
            
            // GridLayout�� ����
            this.setLayout(new GridLayout(4,1,5,3));

            // �»���г� ����
            JPanel lb1 = new JPanel();
            //lb1.setLayout(new BoxLayout(lb1,BoxLayout.Y_AXIS));
 
            //���̺� ����
            JLabel conMode = new JLabel("Connection Mode");
            conMode.setAlignmentX(CENTER_ALIGNMENT);
            //lb1.add(conMode);
            
            //���� �г� ����
            JPanel radioP = new JPanel();
            radioP.setLayout(new BoxLayout(radioP,BoxLayout.X_AXIS));

            // ���� ��ư ���ÿ��� üũ
            rd1.addItemListener(new MyItemListener());
            rd2.addItemListener(new MyItemListener());
            
            
            radioP.setAlignmentX(CENTER_ALIGNMENT);
            
            // 1�� ���� ��ư �������ֵ���
            //rd1.setSelected(true);
            
            // ���� ��ư�� �׷�ȭ �ϱ����� ��ü ����
            ButtonGroup groupRd = new ButtonGroup();
            
            // �׷쿡 ���� ��ư ���Խ�Ų��.
            groupRd.add(rd1);
            groupRd.add(rd2);

            radioP.add(rd1);
            radioP.add(rd2);

            //lb1.add(radioP); // �»�� �гο� ���� �г� ����
            
            // Connect ��ư ����
            JButton btn1 = new JButton("Connect!");
            btn1.setAlignmentX(CENTER_ALIGNMENT);
            
            //��ư�� �����ʸ� �޾� Ŭ���� ������ ä�� ����
            btn1.addActionListener(new ActionListener() {
            	@Override
            	public void actionPerformed(ActionEvent e) {
            		portNum.setEditable(false);
            		if(isServer == true) {
            			//������ ������ �� �ֵ��� ���������� ����� ����� �� �ִ� �غ� �۾�!
            			//��Ʈ��ũ �۾��� Main Thread�� �ϰ��ϸ� �ٸ� �۾�(Ű���� �Է�, Ŭ�� ��..)���� 
            			//���� �� �� ����, ���α׷��� ����, �׷��� Main�� UI�۾��� �����ϵ��� �ϰ�, 
            			//�ٸ� �۾���(���� �ɸ���)��  ������ Thread���� �����ϴ� ���� ������.	
            			ServerThread serverThread = new ServerThread();
            			serverThread.setDaemon(true); //���� ������ ���� ����
            			serverThread.start();

            			}
            		else {

                    	//������ �����ϴ� ��Ʈ��ũ �۾� : ������ ��ü ���� �� ����
                		ClientThread clientThread = new ClientThread();
                		clientThread.setDaemon(true);
                		clientThread.start();


            			}
            		
            	}
            });
            
            //�»�� �ڽ����̾ƿ� ���� // ButtonGroup < Box < Panel
            Box leftBox = Box.createVerticalBox();

            leftBox.add(Box.createVerticalStrut(10));
            leftBox.add(conMode);
            leftBox.add(radioP);
            leftBox.add(btn1); // �»���гο� ��ư �г� ����
            leftBox.add(Box.createVerticalStrut(10));
            //leftBox.add(Box.createHorizontalStrut(160));
            lb1.add(leftBox);
            
            
            
            //////////////////////------------------------------------------����
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
            
            
            //���� �ڽ����̾ƿ� ���� // TextArea < Box < Panel
            Box rightBox = Box.createVerticalBox();
            rightBox.add(Box.createVerticalStrut(10));
            rightBox.add(stateBox);
            rightBox.add(ipBox);
            rightBox.add(portBox);

            //rightBox.add(modeArea);
            righttopPanel.add(rightBox);
            
            // ���������� �ڽ��� ������ �� �г��� ���η� ����
            Box top = Box.createHorizontalBox();
            top.add(lb1); //��� �гο� �»�� �г� ����
            top.add(righttopPanel); // ��� �гο� ���� �г� ����
            top.add(Box.createHorizontalStrut(40));

            
            //Connection Text field
            JPanel connectPanel = new JPanel();
            connectArea = new JTextArea(3,50); // connect ���� ����ϴ� text field ����
            
            connectPanel.setBackground(Color.orange);
            connectArea.setEditable(false); // ��� ����â���� ����
            JScrollPane connectscrollPane = new JScrollPane(connectArea);
            
            connectPanel.add(connectscrollPane);
            //connectArea.append("Connected with A!!!!!"); // ���â�� ����� �̷������� 
            
            
            // connectionmode, servertext, ����â ������ box
            Box north = Box.createVerticalBox();
            
            north.add(top); // gridlayout�� ��� �г� ����
            north.add(connectPanel);
            //north.add(Box.createVerticalStrut(5));
            this.add(north);
            
            
            
            // Ű ��ȯ �ڽ� ����        

            //���� ��ġ �ڽ� ���̾ƿ��� ����
            Box keyBox = Box.createVerticalBox();


            
            //Box��ü left�� �гο� �߰�
            keyArea = new JTextArea(4,10); //3  Ŀ�´����̼� ��� ���� ����ϴ� text field ����
            keyArea.setEditable(false); // ��� ����â���� ����
            JScrollPane keyscrollPane = new JScrollPane(keyArea);
            // Ű ���� ��ư�� ���� �ڽ� ����
            Box keybtnBox = Box.createHorizontalBox();
    
            
            keybtnBox.add(keygenbtn);
            keybtnBox.add(Box.createHorizontalStrut(30)); 
            keybtnBox.add(loadbtn);
            keybtnBox.add(Box.createHorizontalStrut(30)); 
            keybtnBox.add(savebtn);
            
            
            // ������ Ű ��, Ŭ���̾�Ʈ�� ��ĪŰ �����ؼ� ���Ϸ� ���� 
            keygenbtn.addActionListener( new ActionListener() {			


				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub

					keypair = generateRSAKey();
					publickey = keypair.getPublic();
					privatekey = keypair.getPrivate();
					encoded_pubKey = Base64.getEncoder().encodeToString(publickey.getEncoded());
					encoded_priKey = Base64.getEncoder().encodeToString(privatekey.getEncoded());
					
					////////////////Ű ���� �� ���Ϸ� ���� �ڵ� �ʿ� 
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
        					////////////////Ű�ʿ��� �ݺ��ؼ� �ҷ��� �� ���Ϸ� ���� �ڵ� 
        					String encodedpubkey = null;
        					for(String d : keymap.keySet()) {
        						encodedpubkey = keymap.get(d);
            					savePublickey(d, encodedpubkey);
        					}
        					keyArea.append("\nAll keys are saved in file!\n");
        					
        				}


            		});
            
            
            // 3�� ����
            Box keystatBox = Box.createHorizontalBox();

            keystatArea = new JTextArea(2,10); //3  Ŀ�´����̼� ��� ���� ����ϴ� text field ����
            keystatArea.setEditable(false); // ��� ����â���� ����
            //keystatArea.setText("Received Key List: ");
            JScrollPane statscrollPane = new JScrollPane(keystatArea);
            
            // ������ ��� ����Ű�� Ŭ���̾�Ʈ���� ���� / Ŭ���ϰ�� ���� ��ȯ
            sendpubbtn.addActionListener( new ActionListener() {			


				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					 ///////////////Ű �ҷ����� 
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
            //�г��� �׵θ����� ��Īȿ���� ����
            keyPanel.setBorder(new EtchedBorder());       
            

            keyBox.add(keyscrollPane); //3 -> 2
            //���� ���� ��ġ�ϱ� ���� ���� ������Ʈ�� ���� Ȯ��
            keyBox.add(Box.createVerticalStrut(6)); 
            keyBox.add(keybtnBox);
            keyBox.add(Box.createVerticalStrut(6));
            keyBox.add(keystatBox);
            keyPanel.add(keyBox); //2 -> 1
            this.add(keyPanel); //1
            //keyArea.append("KEY!!!!!"); // ���â�� ����� �̷������� 
     
            
            
            // ä�� ��Ʈ ����
            JPanel chatPanel = new JPanel(); //1
            chatPanel.setBackground(Color.yellow);
            
            Box chatBox = Box.createVerticalBox(); //2
            //chatBox.add(Box.createVerticalStrut(80));
          
            chatArea = new JTextArea(6,50); //3  Ŀ�´����̼� ��� ���� ����ϴ� text field ����
            chatArea.setEditable(false); // ��� ����â���� ����
            JScrollPane scrollPane = new JScrollPane(chatArea);
 
            Box downchatBox = Box.createHorizontalBox(); //3
            chatArea2 = new JTextField(); //4  Ŀ�´����̼� ��� ���� ����ϴ� text field ����

            JButton btnSend = new JButton("send"); //4
        	//send ��ư Ŭ���� �����ϴ� ������ �߰�
    		
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
    		
    		//����Ű ������ �� �����ϱ�

    		chatArea2.addKeyListener( new KeyAdapter() {

    			//Ű���忡�� Ű �ϳ��� �������� �ڵ����� ����Ǵ� �޼ҵ�..: �ݹ� �޼ҵ�

    			@Override

    			public void keyPressed(KeyEvent e) {				

    				super.keyPressed(e);

    			
    			//�Է¹��� Ű�� �������� �˾Ƴ���, KeyEvent ��ü�� Ű������ ���� ��������
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
            
            //chatArea.append("Hi!!!!!"); // ���â�� ����� �̷������� 

           
            
            chatBox.add(scrollPane); //3->2
            downchatBox.add(btnSend);//4->3
            //downchatBox.add(Box.createHorizontalGlue());//4->3
            downchatBox.add(chatArea2); //4->3

            chatBox.add(downchatBox);//3->2
            chatPanel.add(chatBox); //2->1
            

          
            this.add(chatPanel); //1
           
            chatPanel.setBorder(new EtchedBorder());  
            //chatPanel.add(chatBox, BorderLayout.CENTER);

            
            // ---------------------------------------------------------------------------���� ��Ʈ ����
            JPanel filePanel = new JPanel(); //1
            filePanel.setBackground(Color.green);
            Box bottomBox = Box.createHorizontalBox(); //2

            
            fileArea = new JTextArea(8,38); //3  Ŀ�´����̼� ��� ���� ����ϴ� text field ����
            fileArea.setEditable(false); // ��� ����â���� ����
            //fileArea.append("File Transfer");
            JScrollPane filescrollPane = new JScrollPane(fileArea);
            // �������� ��ư ����
            JButton btnFileSend = new JButton("send file");

            JPanel filebtnPanel = new JPanel();
            filebtnPanel.setLayout(new BorderLayout(0,0));
            btnFileSend.setPreferredSize(new Dimension(100, 100));
            filebtnPanel.add(btnFileSend);
        	//send ��ư Ŭ���� �����ϴ� ������ �߰�
    		//sendfile ��ư Ŭ���� file ���� �����ϰ� ����
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


            // ������ â ũ�� ����(����, ����)
            setSize(500, 700);
            
            // �� �޼ҵ带 �̿��ؾ� ������ â�� ��Ÿ����.
            setVisible(true);
        

    		
    		addWindowListener(new WindowAdapter() {

    			@Override //Ŭ���̾�Ʈ �����ӿ� window(â) ���� ������ �߰�

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
        
        
        } // ������




	//�̳�Ŭ���� : ���������� �����ϰ� Ŭ���̾�Ʈ�� ������ ����ϰ�,

    	//����Ǹ� �޽����� ���������� �޴� ���� ����

    	class ServerThread extends Thread {

    		@Override
    		public void run() {			
    			try {  //���� ���� ���� �۾�
    				//������ ����Ű �����ͺ��̽�
    				MakeDir md = new MakeDir();
    				md.makeDir("Server");
    				keymap = new HashMap<>();//new���� Ÿ�� �Ķ���� ��������
    				//���� �Է� port �޾ƿ���
    				port = Integer.parseInt(portNum.getText());
    				serverSocket = new ServerSocket(port);
    				connectArea.append("���������� �غ�ƽ��ϴ�...\n");
    				connectArea.append("Ŭ���̾�Ʈ�� ������ ��ٸ��ϴ�.\n");				
    				socket = serverSocket.accept();//Ŭ���̾�Ʈ�� �����Ҷ����� Ŀ��(������)�� ���
    				connectArea.append(socket.getInetAddress().getHostAddress() + "���� �����ϼ̽��ϴ�.\n");

					
					//����� ���� ��Ʈ�� ����
				 	is = socket.getInputStream();
    				bin = new BufferedInputStream(is);
    				os = socket.getOutputStream();
    				bout = new BufferedOutputStream(os);
    				dis = new DataInputStream(bin);
    				dos = new DataOutputStream(bout);
    				
    				
    				String filename =null;

    
   
    				
    				 while(true) {
    					 if(encrypted_AESkey == null) {
    						 // client�κ��� �ۺ�Ű�� ��ȣȭ�� ��ĪŰ�� �޴� �ڵ� // ObjectInputsream ��߰ڴ�

    						 	inO = new ObjectInputStream(is);
    							encrypted_AESkey = (byte[])inO.readObject();
    							String encryted_AESkey_string = new String(Base64.getEncoder().encode(encrypted_AESkey));
    							keyArea.append("\n[Received encrypted AES key from Client!]");
    							keyArea.append(encryted_AESkey_string + "\n");
    						 
    					 }
    					 else {
    						 // privatekey�� ��ȣȭ�� ��ĪŰ �����ϴ� �ڵ�
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
    					//bin = new BufferedInputStream(dis); // �����͸� ���۷� �޾ƿ�
    					
        				// AES ��ȣȭ�� �����͸� �޾� ��ȣȭ 
        				byte[] encryptedData = new byte[1040]; /////////////////////// ó�� �޾ƿ��� ��ȣ���� ���̰� ������ ũ�� �����̿�����!!! 
        				int initsize = dis.read(encryptedData);
    					byte[] rawdata = new byte[initsize];
    					
       					for(int i=0;i<initsize;i++) {
    						rawdata[i] = encryptedData[i]; // ������ ������ ���ο� ����Ʈ�� ���������� 
    					}
    					
    					String isize=Integer.toString(initsize);
    					chatArea.append("Initial byte len: "+ isize);

        				
    					
        				data = decryptAES(rawdata, skeySpec);

        				int size = data.length;
    					String csize=Integer.toString(size);
    					//fileArea.append(csize);
    					String protocol = new String(data, 0, 6); // ����� �޾ƿͼ� ������ ���� Ȯ��
    					
//    					// Ŭ���̾�Ʈ�� �ڽ��� �ۺ�Ű�� AESä���� ���� ���� ���
//    					if(protocol.equals("[CPUK]")) {
//
//        					recpublickey = (PublicKey)inO.readObject();
//        					encoded_pubKey = Base64.getEncoder().encodeToString(recpublickey.getEncoded());
//        					
//    					}
//    					
    					
    					
    					if(protocol.equals("[MESG]")) { //����� �޽����̸�
    					//������ ������ �����͸� �б�
    					//String msg = dis.readUTF();//������ ���������� ���
    					//chatArea.append(" [Client] : " + msg + "\n");
    					chatArea.append(" [Client] : " + (new String(data, 6,size-6).trim()) + "\n");
    					chatArea.setCaretPosition(chatArea.getText().length());
    					} else if(protocol.equals("[SFNE]")) { // ����� ���ϸ� Ÿ���̸�
    						try{
    							filename = new String(data, 6, size-6).trim();
    							fileArea.append(filename+" �� �޽��ϴ�!\n");
    							if(filename != null) fos = new FileOutputStream("Server//"+filename);
    						} catch(FileNotFoundException ex) {
    							ex.printStackTrace();
    						}
    					} else if(protocol.equals("[SFIL]")) { //����� ���� �������� ��� 
    						try {
    							fos.write(data, 8, (int)data[6]*100 + data[7]); // ��� �����ϰ�, ����ũ�⸦ �޾Ƽ� �׸�ŭ�� ����Ʈ�� ����
    						} catch(IOException ex) {
    							ex.printStackTrace();
    						}
    						if((int)data[6]*100 + data[7] < 1016) { // ������ ũ�Ⱑ ������� �ִ񰪺��� ���� ��� == ������ ������ ���
    							try {
    								if(fos != null) fos.close();
    								fileArea.append(filename +" ���� ���ſϷ�!\n");
    							} catch(IOException ex) {
    								ex.printStackTrace();
    								}
    							}
    							
    						}
    					chatArea.setCaretPosition(chatArea.getText().length());
    				} // while end				

    				

    			} catch (IOException e) {
    				connectArea.append("Ŭ���̾�Ʈ�� �������ϴ�.\n");
    			} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

    		}

    	}

    	//�̳�Ŭ���� : ������ �����ϴ� ��Ʈ��ũ �۾� ������
    	class ClientThread extends Thread {
    		@Override
    		public void run() {
    			try {
    				//
    				MakeDir md = new MakeDir();
    				md.makeDir("Client");
    		        keymap = new HashMap<>(); 
    				//���� �Է� port �� �޾ƿ���
    				port = Integer.parseInt(portNum.getText());
    				
    				socket = new Socket(InetAddress.getLocalHost(), port);
    				chatArea.append("������ ���ӵƽ��ϴ�.\n");

					//����� ���� ��Ʈ�� ����
					is = socket.getInputStream();
					os = socket.getOutputStream();
    				bin = new BufferedInputStream(is);
    				bout = new BufferedOutputStream(os);
    				dis = new DataInputStream(bin);
    				dos = new DataOutputStream(bout);
    				
    				String filename =null;
    				
   
    				while(true) {
    				if(recpublickey == null) {
    					// ���Ϸ� ������, objctinputstream���ι����� �����غ���
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
    					// AES ���Ű�� ���� �� ����Ű�� ��ȣȭ�� ������ �ڵ� 
    					secretkey = generateAESkey();
    					skeySpec = new SecretKeySpec(secretkey.getEncoded(), "AES");
    					encrypted_AESkey = encryptRSA(secretkey.getEncoded(),recpublickey); 
    					String encodedKey = Base64.getEncoder().encodeToString(secretkey.getEncoded());
    					keyArea.append("\n[Encrypted Secretkey has been sent to server]");
    					keyArea.append(encodedKey);
    					
    					// server �� ���Ű�� ���� 
    					sendEncryptedAESkey(encrypted_AESkey);
    					safeConnect = true;
						chatArea.append("Text encryption started\n");
    					break;
    					}
    				}
    				
    				while(true) {
    					
    					////////////////���ڼ��� �޾Ƽ� ����� �����̶� ���ϴ� �ڵ� 
    					
    					byte[] firstdata = new byte[DEFAULT_BUFFER_SIZE+DEFAULT_AES_SIZE+DEFAULT_SIGN_SIZE];
    					byte[] data = new byte[DEFAULT_BUFFER_SIZE];
    					byte[] sign = new byte[DEFAULT_SIGN_SIZE];
        				byte[] encryptedData = new byte[DEFAULT_BUFFER_SIZE+DEFAULT_AES_SIZE];
    					
        				int initsize = dis.read(firstdata); // 1024 + 16 + 256 = 1296
        				
    					String isize=Integer.toString(initsize);
    					chatArea.append("Initial byte len: "+ isize+"\n");

    					if(initsize > 1024) { //������ �۽��� ��� ���� �и� �ʿ� 
						//sign �и� �ڵ� 
						System.arraycopy(firstdata, 0, encryptedData, 0, encryptedData.length); // AES ��ȣȭ�� ����Ʈ �и�
						System.arraycopy(firstdata, encryptedData.length, sign, 0, sign.length); // sign �и�
	
						fileArea.append("  Get Encrypted Length: "+encryptedData.length); //1040�̾����! (��ȣȭ�� �ݵ�� 1040���� ��ȯ�ȴ�)
						fileArea.append("  Get Sign Length: "+sign.length+"\n"); //256�̾����!
						

						
						// ���� ���� ����
						verifySign(encryptedData, sign);
						
					    
						data = decryptAES(encryptedData, skeySpec);
					    
    					}
    					else {

        				// AES ��ȣȭ�� �����͸� �޾� ��ȣȭ 
    					byte[] rawdata = new byte[initsize];
    					
    					for(int i=0;i<initsize;i++) {
    						rawdata[i] = firstdata[i];// ������ ������ ���ο� ����Ʈ�� ���������� 
    					}

    					
        				data = decryptAES(rawdata, skeySpec);
    					}
    					
    					
    					
//    					byte[] encryptedData = new byte[DEFAULT_BUFFER_SIZE]; /////////////////////// ó�� �޾ƿ��� ��ȣ���� ���̰� ������ ũ�� �����̿�����!!! 
//        				//int initsize = dis.read(encryptedData);
//    					data = decryptAES(encryptedData, skeySpec);
        				int size = data.length;
    					String csize=Integer.toString(size);
    					//bin = new BufferedInputStream(dis); // �����͸� ���۷� �޾ƿ�

    					//int size = dis.read(data); // �޾ƿ� ���۸� ����Ʈ����Ʈ ���·� ����
    					//fileArea.append(csize);
    					String protocol = new String(data, 0, 6); // ����� �޾ƿͼ� ������ ���� Ȯ�� 
    					
    					if(protocol.equals("[MESG]")) { //����� �޽����̸�
    					//������ ������ �����͸� �б�
    					//String msg = dis.readUTF();//������ ���������� ���
    					chatArea.append(" [Server] : " + (new String(data, 6,size-6).trim()) + "\n");
    					chatArea.setCaretPosition(chatArea.getText().length());
    					} else if(protocol.equals("[SFNE]")) { // ����� ���ϸ� Ÿ���̸�
    						try{
    							filename = new String(data, 6, size-6).trim();
    							fileArea.append("\n"+filename+" �� �޽��ϴ�.\n");
    							if(filename != null) fos = new FileOutputStream("Client//"+ filename);
    						} catch(FileNotFoundException ex) {
    							ex.printStackTrace();
    						}
    					} else if(protocol.equals("[SFIL]")) { //����� ���� �������� ��� 
    						try {
    							fos.write(data, 8, (int)data[6]*100 + data[7]); // ��� �����ϰ�, ����ũ�⸦ �޾Ƽ� �׸�ŭ�� ����Ʈ�� ����
    						} catch(IOException ex) {
    							ex.printStackTrace();
    						}
    						if((int)data[6]*100 + data[7] < 1016) { // ������ ũ�Ⱑ ������� �ִ񰪺��� ���� ��� == ������ ������ ���
    							try {
    								if(fos != null) fos.close();
    								fileArea.append("\n"+filename +" ���� ���ſϷ�!\n");
    							} catch(IOException ex) {
    								ex.printStackTrace();
    								}
    							}
    							
    						}
    					chatArea.setCaretPosition(chatArea.getText().length());
    				} // while end		



    			} catch (UnknownHostException e) {

    				connectArea.append("���� �ּҰ� �̻��մϴ�.\n");

    			} catch (IOException e) {

    				connectArea.append("���� ���ῡ �����߽��ϴ�. Port Number�� Ȯ�����ּ���\n");
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
    	
    	//�޽��� �����ϴ� ��� �޼ҵ�

		void sendMessage() {	
			//2.����(Server)���� �޽��� �����ϱ�
			//�ƿ�ǲ ��Ʈ���� ���� ���濡 ������ ����
			//��Ʈ��ũ �۾��� ������ Thread�� �ϴ� ���� ����
			Thread t = new Thread() {
				@Override
				public void run() {
			String msg = chatArea2.getText(); //TextField�� ���ִ� �۾��� ������
			chatArea2.setText(""); //�Է� �� ��ĭ����
			chatArea.append(" [Me] : " + msg + "\n");//1.TextArea(ä��â)�� ǥ��
			chatArea.setCaretPosition(chatArea.getText().length());

		
			try {
			msg = "[MESG]" + msg;
			//bout = new BufferedOutputStream(socket.getOutputStream());
			//dos = new DataOutputStream(bout);
			//bout = new BufferedOutputStream(dos); //output ���� Ȯ���ϰ� �ٽ� �ۼ�! 
			//dos.write(msg.getBytes());
			
			// �޼����� �޾� AES ��ȣȭ�� ���� ����Ʈȭ ��Ų��
			byte[] encryptedMsg = encryptAES(msg, skeySpec);
			
			fileArea.append("\nEncrypted MSG Length: "+encryptedMsg.length); //1040�̾����!
			
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

			//�ƿ�ǲ ��Ʈ���� ���� ���濡 ������ ����
			//��Ʈ��ũ �۾��� ������ Thread�� �ϴ� ���� ����
			
			Thread t = new Thread() {
				@Override
				public void run() {
			try{

				// ���ڼ��� �߰��ڵ� ���ϱ� //////////////////////////////////////////////////
				File file = new File(fileLocation);

				int n=0;
				long fileSize = file.length();

				String fileName = file.getName();
				if(fileName == "") {
					fileArea.append("Please choose file to send\n");
					return;
				}
		
				fileArea.append(fileName+" ������ �����մϴ�.\n");
				
				//���� �̸� ����
				String str = "[SFNE]" + fileName;
				
				//���ϸ��� �޾� AES ��ȣȭ�� ���� ����Ʈȭ ��Ų��
				byte[] encryptedFilename = encryptAES(str, skeySpec);
				
				dos.write(encryptedFilename);
				dos.flush();
				
				// �� ���� ������ ����
				byte[] strData = "[SFIL]".getBytes(); // �� ���� ������ ���
				byte[] data = new byte[DEFAULT_BUFFER_SIZE];
				
				for(int i=0;i<6;i++) {
					data[i] = strData[i]; // ���ۿ� ���� ������ ��� ����
				}
				String finalLen;
				int testint;
				try {
					fin = new FileInputStream(file);
					while(true) {
						testint = fin.read(data,8,DEFAULT_BUFFER_SIZE-8); // data�� [8] ���� [1024]���� file������ ����
						if(testint == -1) break; //������ �� �ҷ������� �ݺ��� ����
						
						//data�� ������ ������ ���̸� data[6], data[7]�� ������ �ִ´�
						data[6] = (byte)(testint / 100);
						data[7] = (byte)(testint % 100);
		
						fileArea.append("\nData Length: "+testint); //1024 �����̾����!
						
						// �����͸� �޾� AES ��ȣȭ�� ��ģ��
						
						byte[] encryptedData = encryptByteAES(data, skeySpec);
						
						finalLen = Integer.toString(encryptedData.length);
						fileArea.append("\nEncrypted Length: "+finalLen+"  "); //1040�̾����!
						
						if(isServer == true) {
							//���ڼ��� �߰�
							byte[] sign = addSigniture(encryptedData);
							
							fileArea.append("\nSigniture Length: "+ sign.length); //256�̾����!
							
							byte[] sendbytes = new byte[DEFAULT_BUFFER_SIZE+DEFAULT_AES_SIZE+DEFAULT_SIGN_SIZE];
							fileArea.append("\nFinal before Length: "+sendbytes.length); //1040�̾����!
							System.arraycopy(encryptedData, 0, sendbytes, 0, encryptedData.length); //���� �۽Ű��� AES ��ȣȭ�� ������ ����
							System.arraycopy(sign, 0, sendbytes, encryptedData.length, sign.length); // ���� �۽Ű��� (AES��ȣȭ�ȵ�����)�� ������ ���� ����
							fileArea.append("\nFinal Length: "+sendbytes.length); //1040 + 256 �̾����!
							
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
					//if(filesocket!=null) filesocket.close(); //socket�� �ݾƹ�����??
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
		
		    sig2.initSign(keypair.getPrivate()); //�����̺� Ű �޾ƿ�
		    
		    System.out.println("Data Length Original: "+ encryptedData.length);
		    System.out.print("Data original: ");
		    for(byte b: encryptedData) System.out.printf("%02X ", b);
		    sig2.update(encryptedData); // �����Ϳ� ������
		    System.out.println("\nData Length update: "+ encryptedData.length);
		    System.out.print("Data updated: ");
		    for(byte b: encryptedData) System.out.printf("%02X ", b);
		    
		    signatureBytes2 = sig2.sign(); // ���� �Ϸ��� ������

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
				//fileArea.append("Signiture Length: "+finalLen); //128�̾����!
			    return signatureBytes2;
			
			
			
			
		}
		protected boolean verifySign(byte[] encryptedData, byte[] sign) {
			boolean tf = false;
			  
		    try {
		    	
			Signature sig2 = Signature.getInstance("SHA512WithRSA");
		    sig2.initVerify(recpublickey); // ���ø� Ű�� ���� ��ü ���� 
		    sig2.update(encryptedData); // ���� �غ� // ���� ���������ͷ� sig2 �����Ͽ� �ؽ�ȭ�ڵ� ����
		    fileArea.append("Verification: ");
		  
		    	tf = sig2.verify(sign);
		    	
				fileArea.append(String.valueOf(tf));
			} catch (SignatureException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //signature �񱳸� ���� Authentication �ϴµ� (��ȣȭ�� �Ұ�)// ��ȣȭ�ؼ� �ؽõ� �������ϰ�, ���� �ؽ�ȭ�� ���� �� 
		      catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return tf;
			
		}

		/////////////////////////////////////////////�������� ���̴� ��ȣȭ �޼ҵ� 
		
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
				        
			            // BufferedWriter �� FileWriter�� �����Ͽ� ��� (�ӵ� ���)
			            BufferedWriter fw = new BufferedWriter(new FileWriter(usercode, true));
			             
			            // ���Ͼȿ� ���ڿ� ����
			            fw.write(encoded_pubKey2);
			            fw.flush();
			 
			            // ��ü �ݱ�
			            fw.close();
			             
			             
			        }catch(Exception e){
			            e.printStackTrace();
			        }
			  
				
			}
		public String loadPublickey() {
			// TODO Auto-generated method stub
			//String encodedPubkey = keymap.get(usercode);
			String encodedPubkey = null;
			//���� �ҷ����� �ڵ�
			JfileChooserUtil file = new JfileChooserUtil();
			String filepath =file.jFileChooserUtil();
			
			try { 
				
				BufferedReader br = new BufferedReader(new FileReader(filepath));
				
				encodedPubkey = br.readLine(); //���ڵ��� �ۺ�Ű �ҷ�����
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
		
		//�����󿡼� �ʱ⿡ RSA ����Ű�� ����Ű�� �����Ѵ� 
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
		// ����Ű�� ��ȣȭ�� �����͸� ����Ű�� ��ȣȭ 
		public byte[] decryptRSA(byte[] encrypted, PrivateKey privateKey) {
			
			byte[] b1 = null;
			
			try {
	        Cipher cipher = Cipher.getInstance("RSA");
	        //��ȣȭ ����
	        System.out.println("=== RSA Decryption ==="); // ���޹��� ��ȣ���� privatekey�� ��ȣȭ
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
		
		////////////////////////////////////////�������� ���̴� AES �޼ҵ�� 

		public static byte[] encryptAES(String text, SecretKey key)
		{
			byte[] ciphertext2 = null;
			
			try {
			    
				// ��ȣȭ ����
				System.out.println("\n\nAES Encryption ");
				Cipher cipher2 = Cipher.getInstance("AES/ECB/PKCS5Padding");
				cipher2.init(Cipher.ENCRYPT_MODE, key);

				byte[] plaintext = text.getBytes(); // ���� �ؽ�ȭ��

//				System.out.print("Plaintext : ");
//				for (byte b : plaintext)
//					System.out.printf("%02X ", b);
//				System.out.print("\nPlaintext Length: " + plaintext.length + " byte");

				ciphertext2 = cipher2.doFinal(plaintext); // �� ��ȣȭ
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
			    
				// ��ȣȭ ����
				System.out.println("\n\nAES Encryption ");
				Cipher cipher2 = Cipher.getInstance("AES/ECB/PKCS5Padding");
				cipher2.init(Cipher.ENCRYPT_MODE, key);

				//byte[] plaintext = text.getBytes(); // ���� �ؽ�ȭ��
//
//				System.out.print("Plaintext : ");
//				for (byte b : plaintext)
//					System.out.printf("%02X ", b);
				System.out.print("\nData Length: " + data.length + " byte");

				ciphertext2 = cipher2.doFinal(data); // �� ��ȣȭ
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
				// ��ȣȭ ����
				cipher2.init(Cipher.DECRYPT_MODE, key); // ��ȣ�� ��ȣȭ
				decrypttext2 = cipher2.doFinal(ciphertext);
				output2 = new String(decrypttext2, "UTF8");
				System.out.print("\nDecrypted Text:" + output2 + "\n");
			 
			    
			} catch (Exception e) {
				e.printStackTrace();
			}
			return decrypttext2;
		}
		
		///////////////////////////////////////////Ŭ���̾�Ʈ �ܿ��� ����ϴ� ��ȣȭ �޼ҵ�
		// ���� ����Ű�� �����͸� ��ȣȭ
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
		
		// Ŭ���̾�Ʈ �ܿ��� ��ĪŰ�� ���� (�̸� ������ ����Ű�� ��ȣȭ�ؼ� ���� ����)
		public static SecretKey generateAESkey() {
			
			// ��ĪŰ ����
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
		        
		        // ������ ���� ���丮 ���(Window ���)
		        //String folderPath = "C://";
		        
		        File makeFolder = new File(folderName);
		 
		        // folderPath�� ���丮�� �������� ������� ���丮 ����.
		        if(!makeFolder.exists()) {
		            
		            // ������ �����մϴ�.
		            makeFolder.mkdir(); 
		            System.out.println("������ �����մϴ�.");
		            
		            // ���������� ���� ������ true�� ��ȯ�մϴ�.
		            System.out.println("������ �����ϴ��� üũ true/false : "+makeFolder.exists());
		            
		        } else {
		            System.out.println("�̹� �ش� ������ �����մϴ�.");
		        }
		        
		    }
		    
		}

    
    public static void main(String[] args){
        
        GUI gui = new GUI();
    }
}

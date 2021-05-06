# Simple Public Key System

The purpose of this project is to make simple chat & file transfer program in server-client structure. I added encryption methods to ensure secured communication. 
First, the server sends its public key to client.
Second, the client send back its AES key after encrypt with Server's public key.
Third, Server decrypt the message and get Client's AES key. 

Chat will be encrypted with AES key from now on.
In case of File transfer from server, I added digital signature to prevent malicious deception and to gain enough authenticity.

##	How to run this program 

 1.	Download the repository
 2.	Import the project
 3.	Please run “GUI.java”

##	How to use this program

 1.	Run the same program twice
 2.	Set the first console to server and press “Connect!”
 3.	Set the second console to Client and press “Connect!”
 4.	Now your server and client are connected but didn’t set the secured channel
 5.	Press “Generate key” on the server
 6.	Press “Send Public key” on the server
 7.	You can check that client got public key successfully and automatically send back the AES key to the server
 8.	From now on, all the data send through the channel will be encrypted in AES
 9.	Test chatting with “send” button and Enter button
 10.	You can see both Ciphertext and Decrypted text

## How to send a file

 1.	Press “send file”
 2.	Choose the file you want in File Explorer
 3.	The file will be encrypted and then sent
 4.	When file transmission finished, then “file sent!” message will be popped up
 5.	When file saving finished, then “filename 파일 수신완료!” message will be popped up
 6.	You can find saved file in Project folder

***

# Detailed Description

##	User Interface
 
#### 1.	Connection Area    
 This Area controls connection between Server and Client. You can easily check your connection mode, State, IP Address, and Port Number.  
 You can choose server or Client with radio button, and also can choose Port number which you want to use. This setting cannot be changed in the middle of connection sequence. 
 On the orange area, you can read the data about current connection.

#### 2.	Key Area   
 This area controls most of functions related with the keys(RSA, AES). 
 To simulate the actual sever-client relation, you need to generate and send public key from server to client first. Once you send the server’s public key to client, client will automatically save the public key. And after that, to establish secured channel, client will generate its own AES key and send it to server with RSA public key encryption. 
 If server got the encrypted data from client, it would decrypt it with its own private key. So now both have the same AES key in secured way. From now on, server and client will only accept the AES encrypted data for their own privacy. 

#### 3.	Chat Area   
 On this area you can send the text message with “send” button or just simply press Enter.
 You should mind that this chat will approved only when you built AES-secured channel. Once you got a message from the other, the message is ciphertext. With AES key your program will decrypt and print it. 

#### 4.	File Area   
 This area controls the file transmission and shows status about the sent/received files.
 When you press the “send file” button, you can see the new File Explorer console pops up. Choose any file that you want to send, and press “Open” button. The program will automatically start transmission to the other.

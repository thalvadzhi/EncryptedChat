package com.bearenterprises.ChatClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.bearenterprises.Encryption.AESEncryption;
import com.bearenterprises.Encryption.RSAEncryption;
import com.bearenterprises.Utilities.Constants;
import com.bearenterprises.Utilities.ServerCommands;

public class Client {
   private Socket socket;
   private DataInputStream inputStream;
   private DataOutputStream outputStream;
   private SecretKey AESKey;
   private PublicKey publicKey;
   private PrivateKey privateKey;
   private AESEncryption aesEncryption;
   private RSAEncryption rsaEncryption;

   public Client(String ip, int port) {
      try {
         socket = new Socket(ip, port);
         InputStream input = socket.getInputStream();
         OutputStream output = socket.getOutputStream();
         outputStream = new DataOutputStream(output);
         inputStream = new DataInputStream(input);
         KeyPair pair = RSAEncryption.generateKeys();
         privateKey = pair.getPrivate();
         publicKey = pair.getPublic();
         rsaEncryption = new RSAEncryption(publicKey, privateKey);
      } catch (UnknownHostException e) {
         // TODO Use logger here
         // e.printStackTrace();
      } catch (IOException e) {
         // TODO Use logger here
         // e.printStackTrace();
      }
   }

   public Client(String ip) {
      this(ip, Constants.serverPortNumber);
   }

   private String byteArrayToString(byte[] data) {
      return new String(data, StandardCharsets.UTF_8);
   }

   private String encryptString(String message) {
      byte[] messageEncrypted = null;
      try {
         messageEncrypted = aesEncryption.encrypt(message.getBytes("UTF8"));
      } catch (UnsupportedEncodingException e) {
         // TODO Auto-generated catch block
         // e.printStackTrace();
      }
      return encode(messageEncrypted);
   }

   private String encode(byte[] message) {
      return Base64.getEncoder().encodeToString(message);
   }

   private byte[] decode(String message) {
      return Base64.getDecoder().decode(message);
   }


   public void sendMessage(String message) {
      OutputStream output = null;
      try {
         output = socket.getOutputStream();
         DataOutputStream outputStream = new DataOutputStream(output);
         outputStream.writeUTF(encryptString(message));
      } catch (IOException e) {
         // TODO Use logger here
         // e.printStackTrace();
      }
   }

   public String readMessage() {
      try {
         String message = inputStream.readUTF();
         byte[] decryptedMessage = aesEncryption.decrypt(decode(message));
         return byteArrayToString(decryptedMessage);
      } catch (IOException e) {
         // TODO Auto-generated catch block
         // e.printStackTrace();
         return "";
      }
   }

   public boolean login(String credentials) {
      try {
         outputStream.writeUTF(ServerCommands.LOGIN_USER.name());
         //wait for the server to be ready
         String response = inputStream.readUTF();
         if (response.equals(ServerCommands.GET_CREDENTIALS.name())) {
            outputStream.writeUTF(encryptString(credentials));
         } else {
            return false;
         }
         response = inputStream.readUTF();
         if (response.equals(ServerCommands.RIGHT_CREDENTIALS.name())) {
            return true;
         } else {
            return false;
         }
      } catch (IOException e) {
         // TODO Auto-generated catch block
         // e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   public boolean register(String credentials) {
      try {
         outputStream.writeUTF(ServerCommands.REGISTER_USER.name());
         //wait for the server to be read
         String response = inputStream.readUTF();
         if (response.equals(ServerCommands.GET_CREDENTIALS.name())) {
            outputStream.writeUTF(encryptString(credentials));
         } else {
            return false;
         }
         response = inputStream.readUTF();
         if (response.equals(ServerCommands.USER_REGISTERED.name())) {
            return true;
         } else {
            return false;
         }
      } catch (IOException e) {
         // TODO Auto-generated catch block
         // e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   public boolean exchangeKeys() {
      try {
         String command = inputStream.readUTF();
         if (command.equals(ServerCommands.EXCHANGE_KEYS.name())) {
            byte[] publicKeyRsa = publicKey.getEncoded();
            outputStream.writeInt(publicKeyRsa.length);
            outputStream.write(publicKeyRsa);
            outputStream.flush();
            //receive the encrypted AES key
            int lengthAES = inputStream.readInt();
            byte[] AESKey = new byte[lengthAES];
            inputStream.read(AESKey);
            decryptAESKey(AESKey);
            return true;
         }
         return false;
      } catch (IOException e) {
         // TODO Auto-generated catch block
         // e.printStackTrace();
         return false;
      }
   }

   private void decryptAESKey(byte[] encryptedKey) {
      byte[] AESDecrypted = rsaEncryption.decrypt(encryptedKey);
      AESKey = new SecretKeySpec(AESDecrypted, 0, AESDecrypted.length, "AES");
      aesEncryption = new AESEncryption(AESKey);
   }

   public void kill() {
      try {
         socket.close();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         // e.printStackTrace();
      }
   }


}

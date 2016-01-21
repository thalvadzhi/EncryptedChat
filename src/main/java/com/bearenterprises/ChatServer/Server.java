package com.bearenterprises.ChatServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKey;

import com.bearenterprises.Database.DatabaseAccess;
import com.bearenterprises.Encryption.AESEncryption;
import com.bearenterprises.Encryption.RSAEncryption;
import com.bearenterprises.Utilities.Constants;
import com.bearenterprises.Utilities.ServerCommands;

public class Server {
   private ServerSocket serverSocket;
   private ArrayList<Socket> connections;
   private ArrayList<WorkerThread> workers;
   private SecretKey AESKey;
   private AESEncryption aesEncryption;


   public Server() {
      try {
         serverSocket = new ServerSocket(Constants.serverPortNumber);
      } catch (IOException e) {
         // TODO Use logger here
         e.printStackTrace();
      }
      connections = new ArrayList<>();
      workers = new ArrayList<>();
      AESKey = AESEncryption.generateKey();
      System.out.println(Arrays.toString(AESKey.getEncoded()));
      aesEncryption = new AESEncryption(AESKey);
   }

   private void broadcast(String message) {
      for (WorkerThread worker : workers) {
         if (worker.getIsAuthenticated()) {
            sendMessage(worker.getSocket(), encryptString(message));
         }
      }
   }

   private void sendMessage(Socket socket, String message) {
      OutputStream stream = null;
      try {
         stream = socket.getOutputStream();
      } catch (IOException e) {
         // TODO Use logger here
         e.printStackTrace();
      }
      DataOutputStream outputStream = new DataOutputStream(stream);
      try {
         outputStream.writeUTF(message);
      } catch (IOException e) {
         // TODO Use logger here
         e.printStackTrace();
      }
   }

   private String decryptMessage(byte[] message) {
      byte[] decryptedMessage = aesEncryption.decrypt(message);
      return byteArrayToString(decryptedMessage);
   }

   private String encryptString(String message) {
      byte[] messageEncrypted = null;
      try {
         messageEncrypted = aesEncryption.encrypt(message.getBytes("UTF8"));
      } catch (UnsupportedEncodingException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return encode(messageEncrypted);
   }

   private String byteArrayToString(byte[] data) {
      return new String(data, StandardCharsets.UTF_8);
   }

   private String encode(byte[] message) {
      return Base64.getEncoder().encodeToString(message);
   }


   private byte[] decode(String message) {
      return Base64.getDecoder().decode(message);
   }


   private class WorkerThread extends Thread {
      private Socket socket;
      private boolean isAuthenticated;
      private DataInputStream inputStream;
      private DataOutputStream outputStream;
      private String username;
      private DatabaseAccess databaseAccess;
      private PublicKey workerPublicKey;

      public Socket getSocket() {
         return socket;
      }

      public boolean getIsAuthenticated() {
         return isAuthenticated;
      }


      public WorkerThread(Socket socket) {
         this.socket = socket;
         isAuthenticated = false;
         InputStream input = null;
         OutputStream output = null;
         username = null;
         databaseAccess = new DatabaseAccess("C:\\Users\\thalv\\Desktop\\database.db");
         try {
            input = socket.getInputStream();
            output = socket.getOutputStream();
         } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         inputStream = new DataInputStream(input);
         outputStream = new DataOutputStream(output);
      }

      public boolean parseInput(String message) {
         if (message.equals(ServerCommands.KILL_WORKER.name())) {
            kill();
            return true;
         } else if (message.equals(ServerCommands.LOGIN_USER.name())) {
            authenticate();
            return true;
         } else if (message.equals(ServerCommands.REGISTER_USER.name())) {
            register();
            return true;
         }
         return false;
      }

      private void exchangeKeys() {
         try {
            byte[] AESEncoded = AESKey.getEncoded();
            outputStream.writeUTF(ServerCommands.EXCHANGE_KEYS.name());
            //read client's public rsa
            int lengthRSA = inputStream.readInt();
            byte[] RSAWorker = new byte[lengthRSA];
            inputStream.read(RSAWorker);
            workerPublicKey = KeyFactory.getInstance("RSA")
                  .generatePublic(new X509EncodedKeySpec(RSAWorker));

            //encrypt AES key
            byte[] AESEncrypted = encryptAESKey(AESEncoded);
            //send encrypted key
            outputStream.writeInt(AESEncrypted.length);
            outputStream.write(AESEncrypted);
            outputStream.flush();
         } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } catch (InvalidKeySpecException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }

      private byte[] encryptAESKey(byte[] aesKey) {
         //do this because cipher doesn't support null
         KeyPair pair = RSAEncryption.generateKeys();
         RSAEncryption rsaEncryption =
               new RSAEncryption(workerPublicKey, pair.getPrivate());
         return rsaEncryption.encrypt(aesKey);
      }

      private UserCredentials parseCredentials(String credentials) {
         byte[] decoded = decode(credentials);
         String credentialsDecrypted = decryptMessage(decoded);
         String[] credentialsSplit = credentialsDecrypted.split(";");
         return new UserCredentials(credentialsSplit[0], credentialsSplit[1]);
      }


      private void register() {
         try {
            outputStream.writeUTF(ServerCommands.GET_CREDENTIALS.name());
            String message = inputStream.readUTF();
            UserCredentials credentials = parseCredentials(message);
            boolean credentialsValid = registerIfPossible(credentials);
            if (credentialsValid) {
               outputStream.writeUTF(ServerCommands.USER_REGISTERED.name());
            } else {
               outputStream.writeUTF(ServerCommands.USER_ALREADY_EXIST.name());
            }
         } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }

      }

      private boolean registerIfPossible(UserCredentials credentials) {
         if (databaseAccess.isInDatabase(credentials.getUsername())) {
            return false;
         } else {
            isAuthenticated = true;
            username = credentials.getUsername();
            databaseAccess.addToDatabase(credentials.getUsername(),
                  credentials.getPassword());
            return true;
         }
      }

      public void authenticate() {
         try {
            //first send command to the user to send his credentials
            outputStream.writeUTF(ServerCommands.GET_CREDENTIALS.name());
            String message = inputStream.readUTF();
            //expect credentials in format username;password
            UserCredentials credentials = parseCredentials(message);
            boolean credentialsCorrect = verifyCredentials(credentials);
            if (credentialsCorrect) {
               isAuthenticated = true;
               username = credentials.getUsername();
               outputStream.writeUTF(ServerCommands.RIGHT_CREDENTIALS.name());
            } else {
               outputStream.writeUTF(ServerCommands.WRONG_CREDENTIALS.name());
            }
         } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }

      private boolean verifyCredentials(UserCredentials credentials) {
         return databaseAccess.credentialsMatch(credentials.getUsername(),
               credentials.getPassword());
      }

      public void kill() {
         try {
            this.socket.close();
         } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }

      public void readMessage() {
         while (true) {
            try {
               String message = inputStream.readUTF();
               if (parseInput(message)) {
                  continue;
               }
               if (isAuthenticated) {
                  broadcast(username + ": " + decryptMessage(decode(message)));
               }
            } catch (IOException e) {
               //               try {
               //                  //ocket.close();
               //               } catch (IOException e1) {
               //                  // TODO Auto-generated catch block
               //                  e1.printStackTrace();
               //               }
               //               e.printStackTrace();
            }
         }
      }

      @Override
      public void run() {
         exchangeKeys();
         readMessage();
      }
   }

   public void serve() {
      System.out.println("Starting server...");
      Socket socket = null;
      while (true) {
         try {
            socket = serverSocket.accept();
            connections.add(socket);
            WorkerThread worker = new WorkerThread(socket);
            worker.start();
            workers.add(worker);
         } catch (IOException e) {
            // TODO Use logger here
            e.printStackTrace();
         }
      }
   }

   public void kill() {
      try {
         serverSocket.close();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   public static void main(String[] args) {
      Server server = new Server();
      server.serve();
   }
}

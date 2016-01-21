package com.bearenterprises.ChatClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.bearenterprises.Utilities.Constants;
import com.bearenterprises.Utilities.ServerCommands;

public class Client {
   private Socket socket;
   private DataInputStream inputStream;
   private DataOutputStream outputStream;

   public Client(String ip, int port) {
      try {
         socket = new Socket(ip, port);
         InputStream input = socket.getInputStream();
         OutputStream output = socket.getOutputStream();
         outputStream = new DataOutputStream(output);
         inputStream = new DataInputStream(input);
      } catch (UnknownHostException e) {
         // TODO Use logger here
         e.printStackTrace();
      } catch (IOException e) {
         // TODO Use logger here
         e.printStackTrace();
      }
   }

   public Client(String ip) {
      this(ip, Constants.serverPortNumber);
   }

   public void sendMessage(String message) {
      OutputStream output = null;
      try {
         output = socket.getOutputStream();
         DataOutputStream outputStream = new DataOutputStream(output);
         outputStream.writeUTF(message);
      } catch (IOException e) {
         // TODO Use logger here
         e.printStackTrace();
      }
   }

   public String readMessage() {
      try {
         String message = inputStream.readUTF();
         return message;
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
         return "";
      }
   }

   public boolean login(String credentials) {
      try {
         outputStream.writeUTF(ServerCommands.LOGIN_USER.name());
         //wait for the server to be read
         String response = inputStream.readUTF();
         if (response.equals(ServerCommands.GET_CREDENTIALS.name())) {
            outputStream.writeUTF(credentials);
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
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   public boolean register(String credentials) {
      try {
         outputStream.writeUTF(ServerCommands.REGISTER_USER.name());
         //wait for the server to be read
         String response = inputStream.readUTF();
         if (response.equals(ServerCommands.GET_CREDENTIALS.name())) {
            outputStream.writeUTF(credentials);
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
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   public void kill() {
      try {
         socket.close();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   public static void main(String[] args) {
      Client client = new Client("192.168.0.103");
      Runnable reader = () -> {
         client.readMessage();
      };
      new Thread(reader).start();
      client.sendMessage("Kvo stava");
      client.sendMessage("Mo stava");
      //client.kill();
   }
}

package com.bearenterprises.ChatServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import com.bearenterprises.Database.DatabaseAccess;
import com.bearenterprises.Utilities.Constants;
import com.bearenterprises.Utilities.ServerCommands;

public class Server {
   private ServerSocket serverSocket;
   private ArrayList<Socket> connections;
   private ArrayList<WorkerThread> workers;

   public Server() {
      try {
         serverSocket = new ServerSocket(Constants.serverPortNumber);
      } catch (IOException e) {
         // TODO Use logger here
         e.printStackTrace();
      }
      connections = new ArrayList<>();
      workers = new ArrayList<>();
   }

   private void broadcast(String message) {
      for (WorkerThread worker : workers) {
         if (worker.getIsAuthenticated()) {
            sendMessage(worker.getSocket(), message);
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

   private class WorkerThread extends Thread {
      private Socket socket;
      private boolean isAuthenticated;
      private DataInputStream inputStream;
      private DataOutputStream outputStream;
      private String username;
      private DatabaseAccess databaseAccess;

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

      private UserCredentials parseCredentials(String credentials) {
         String[] credentialsSplit = credentials.split(";");
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
                  broadcast(username + ": " + message);
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

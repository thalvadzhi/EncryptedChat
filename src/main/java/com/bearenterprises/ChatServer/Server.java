package com.bearenterprises.ChatServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import com.bearenterprises.Utilities.Constants;

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
      for (Socket socket : connections) {
         sendMessage(socket, message);
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

      public WorkerThread(Socket socket) {
         this.socket = socket;
      }

      public void readMessage() {
         InputStream input = null;
         try {
            input = socket.getInputStream();
         } catch (IOException e) {
            // TODO use logger here
            e.printStackTrace();
         }
         DataInputStream dataStream = new DataInputStream(input);
         while (true) {
            try {
               String message = dataStream.readUTF();
               System.out.println(message);
               broadcast(message);
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

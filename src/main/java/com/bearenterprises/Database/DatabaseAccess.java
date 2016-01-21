package com.bearenterprises.Database;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DatabaseAccess {
   private String pathToDatabase;

   public DatabaseAccess(String pathToDatabase) {
      this.pathToDatabase = pathToDatabase;
   }

   public void addToDatabase(String username, String password) {
      HashMap<String, String> db = readDatabase();
      db.put(username, password);
      writeDatabase(db);
   }

   public boolean isInDatabase(String username) {
      HashMap<String, String> db = readDatabase();
      return db.keySet().contains(username);
   }

   public boolean credentialsMatch(String username, String password) {
      HashMap<String, String> db = readDatabase();
      String usernameInDatabase = db.get(username);
      if (usernameInDatabase == null) {
         return false;
      }
      return usernameInDatabase.equals(password);
   }

   public void writeDatabase(HashMap<String, String> db) {
      Gson gson = new Gson();
      String json = gson.toJson(db);
      try {
         FileWriter writer = new FileWriter(pathToDatabase);
         writer.write(json);
         writer.close();

      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private HashMap<String, String> readDatabase() {
      try {
         BufferedReader br = new BufferedReader(new FileReader(pathToDatabase));
         Gson gson = new Gson();
         Type stringStringMap = new TypeToken<HashMap<String, String>>() {
         }.getType();
         HashMap<String, String> db = gson.fromJson(br, stringStringMap);
         return db;
      } catch (FileNotFoundException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }
}

package com.bearenterprises.Encryption;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class AESEncryption {
   private Cipher encryptCipher, decryptCipher;
   private SecretKey secretKey;

   public AESEncryption(SecretKey secretKey) {
      this.secretKey = secretKey;
      try {
         encryptCipher = Cipher.getInstance("AES");
         encryptCipher.init(Cipher.ENCRYPT_MODE, this.secretKey);
         decryptCipher = Cipher.getInstance("AES");
         decryptCipher.init(Cipher.DECRYPT_MODE, this.secretKey);
      } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (InvalidKeyException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

   }

   public byte[] encrypt(byte[] message) {
      try {
         return encryptCipher.doFinal(message);
      } catch (IllegalBlockSizeException | BadPaddingException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
         return null;
      }
   }

   public byte[] encrypt(String message) {
      return encrypt(message.getBytes());
   }

   public byte[] decrypt(byte[] encryptedData) {
      try {
         return decryptCipher.doFinal(encryptedData);
      } catch (IllegalBlockSizeException | BadPaddingException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
         return null;
      }
   }

   public static SecretKey generateKey() {
      KeyGenerator kgen = null;
      try {
         kgen = KeyGenerator.getInstance("AES");
      } catch (NoSuchAlgorithmException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      kgen.init(128);
      return kgen.generateKey();
   }
}

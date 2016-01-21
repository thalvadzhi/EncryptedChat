package com.bearenterprises.Encryption;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RSAEncryption {
   private PublicKey publicKey;
   private PrivateKey privateKey;
   private Cipher cipherEncrypt;
   private Cipher cipherDecrypt;

   public RSAEncryption(PublicKey publicKey, PrivateKey privateKey) {
      try {

         this.publicKey = publicKey;
         this.privateKey = privateKey;
         cipherEncrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
         cipherEncrypt.init(Cipher.ENCRYPT_MODE, this.publicKey);
         cipherDecrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
         cipherDecrypt.init(Cipher.DECRYPT_MODE, this.privateKey);
      } catch (NoSuchAlgorithmException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (NoSuchPaddingException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (InvalidKeyException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   public byte[] encrypt(byte[] message) {
      try {
         return cipherEncrypt.doFinal(message);
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
         return cipherDecrypt.doFinal(encryptedData);
      } catch (IllegalBlockSizeException | BadPaddingException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
         return null;
      }
   }

   public static KeyPair generateKeys() {
      KeyPairGenerator kpg = null;
      try {
         kpg = KeyPairGenerator.getInstance("RSA");
      } catch (NoSuchAlgorithmException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      kpg.initialize(2048);
      return kpg.generateKeyPair();
   }

   public byte[] getPublicKey() {
      return publicKey.getEncoded();
   }
}

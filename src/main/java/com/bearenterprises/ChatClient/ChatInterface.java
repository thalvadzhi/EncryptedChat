package com.bearenterprises.ChatClient;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

public class ChatInterface {
   private TextArea chatArea;
   private TextField chatField, username;
   private JButton login, register;
   private JPasswordField password;
   private Client client;
   private JFrame frame;

   private boolean loggedIn;


   public ChatInterface() {
      loggedIn = false;
      frame = new JFrame();
      client = new Client("localhost");
      client.exchangeKeys();
      JPanel loginPanel = getCredentialsPanel();
      frame.add(loginPanel);
      frame.setVisible(true);
      frame.setSize(500, 500);
      frame.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent windowEvent) {
            //client.kill();
            System.exit(0);
         }
      });
      login.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            onLoginClick();

         }
      });

      register.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            onRegisterClick();

         }
      });

      //setUpFrame();
      //      setEnterListener();
      //      Runnable messageListen = () -> {
      //         messageListener();
      //      };
      //      new Thread(messageListen).start();

   }

   private String hashPassword(String password) {
      MessageDigest md = null;
      try {
         md = MessageDigest.getInstance("SHA-512");
      } catch (NoSuchAlgorithmException e1) {
         // TODO Auto-generated catch block
         e1.printStackTrace();
      }
      String text = "This is some text";

      try {
         md.update(password.getBytes("UTF-8"));
      } catch (UnsupportedEncodingException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } // Change this to "UTF-16" if needed
      byte[] digest = md.digest();
      return String.format("%064x", new java.math.BigInteger(1, digest));
   }

   private void startChat() {
      setEnterListener();
      Runnable messageListen = () -> {
         messageListener();
      };
      new Thread(messageListen).start();
   }

   private void messageListener() {
      while (true) {
         String message = client.readMessage();
         chatArea.append("\n");
         chatArea.append(message);
      }
   }

   private void setEnterListener() {
      chatField.addKeyListener(new KeyListener() {

         @Override
         public void keyTyped(KeyEvent e) {
            // TODO Auto-generated method stub

         }

         @Override
         public void keyReleased(KeyEvent e) {
            // TODO Auto-generated method stub

         }

         @Override
         public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
               String message = chatField.getText();
               client.sendMessage(message);
               chatField.setText("");
            }

         }
      });
   }

   private String getInputFromTextFields() {
      return username.getText() + ";"
            + hashPassword(String.valueOf(password.getPassword()));
   }

   private void onLoginClick() {
      String credentials = getInputFromTextFields();
      loggedIn = client.login(credentials);
      //      System.out.println("");
      if (loggedIn) {
         loadPanel(getChatPanel());
         startChat();

      } else {
         JOptionPane.showMessageDialog(frame, "Wrong credentials!");
      }
   }

   private void onRegisterClick() {
      String credentials = getInputFromTextFields();
      boolean registed = client.register(credentials);
      if (registed) {
         loadPanel(getChatPanel());
         startChat();
      } else {
         JOptionPane.showMessageDialog(frame, "User with that username already exist");
      }
   }

   public void loadPanel(JPanel panel) {
      //frame.removeAll();
      frame.getContentPane().removeAll();
      frame.getContentPane().add(panel);
      frame.getContentPane().repaint();
      frame.setSize(500, 500);
      frame.setVisible(true);
      //      frame.revalidate();
      //frame.pack();
   }

   public void setUpFrame() {
      JFrame frame = new JFrame();
      frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
      frame.setSize(500, 500);
      frame.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent windowEvent) {
            client.kill();
            System.exit(0);
         }
      });
      chatArea = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
      chatArea.setEditable(false);
      chatArea.setSize(500, 450);
      frame.add(chatArea);
      chatField = new TextField();
      frame.add(chatField);
      frame.setVisible(true);
   }

   public JPanel getChatPanel() {
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      chatArea = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
      chatArea.setEditable(false);
      chatArea.setSize(500, 450);
      panel.add(chatArea);
      chatField = new TextField();
      panel.add(chatField);
      panel.setVisible(true);
      return panel;
   }

   public JPanel getCredentialsPanel() {
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      password = new JPasswordField();
      password.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, password.getMinimumSize().height));
      username = new TextField();
      username.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, password.getMinimumSize().height));
      login = new JButton("Login");
      login.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, login.getMinimumSize().height));
      login.setAlignmentX(Component.CENTER_ALIGNMENT);
      register = new JButton("Register");
      register.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, register.getMinimumSize().height));
      register.setAlignmentX(Component.CENTER_ALIGNMENT);
      panel.add(username);
      panel.add(password);
      panel.add(login);
      panel.add(register);
      panel.setVisible(true);
      return panel;
   }


   public static void main(String[] args) {
      ChatInterface gui = new ChatInterface();
      //      //      //gui.setUpFrame();
      //      //      Client client = new Client("192.168.0.103");
      //      //      System.out.println(client.login("JohnDoe;weakAssPasswod"));
      //      JFrame frame = new JFrame();
      //      JPanel panel = gui.getChatPanel();
      //      frame.add(panel);
      //      frame.setVisible(true);
      //      frame.setSize(500, 500);

   }
}

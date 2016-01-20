package com.bearenterprises.ChatClient;

import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JFrame;

public class ChatInterface {
   private TextArea area;
   private TextField field;
   private Client client;

   public ChatInterface() {
      client = new Client("87.120.102.118");
      setUpFrame();
      setEnterListener();
      Runnable messageListen = () -> {
         messageListener();
      };
      new Thread(messageListen).start();

   }

   private void messageListener() {
      while (true) {
         String message = client.readMessage();
         area.append("\n");
         area.append(message);
      }
   }

   private void setEnterListener() {
      field.addKeyListener(new KeyListener() {

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
               System.out.println("ENTRAREI");
               String message = field.getText();
               client.sendMessage(message);
               field.setText("");
            }

         }
      });
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
      area = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
      area.setEditable(false);
      area.setSize(500, 450);
      frame.add(area);
      field = new TextField();
      frame.add(field);
      frame.setVisible(true);
   }


   public static void main(String[] args) {
      ChatInterface gui = new ChatInterface();
      //gui.setUpFrame();
   }
}

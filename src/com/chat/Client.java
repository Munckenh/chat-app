package com.chat;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client extends JFrame {
    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    private final String username;
    private JPanel mainPanel;
    private JTextArea textArea;
    private JTextField textField;
    private JButton sendButton;

    public Client(Socket socket) throws IOException {
        this.socket = socket;
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());

        // Get username
        this.username = JOptionPane.showInputDialog("Enter username:");
        if (this.username == null || this.username.trim().isEmpty()) {
            System.exit(0);
        }

        sendButton.addActionListener(_ -> {
            String message = textField.getText();
            if (!message.isEmpty()) {
                try {
                    outputStream.writeUTF(this.username + ": " + message);
                    display(this.username + ": " + message);
                } catch (IOException ex) {
                    display("Client: Error sending message");
                }
                textField.setText("");
            }
        });

        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendButton.doClick();
                }
            }
        });

        // Initialize the frame
        setTitle("Client");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 300);
        setContentPane(mainPanel);

        // Send username to server
        try {
            outputStream.writeUTF(this.username);
        } catch (IOException e) {
            System.exit(1);
        }

        setVisible(true);
        startListening();
    }

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 4000);
        new Client(socket);
    }

    private void startListening() {
        new Thread(() -> {
            while (socket.isConnected()) {
                try {
                    String message = inputStream.readUTF();
                    display(message);
                } catch (IOException e) {
                    close();
                    break;
                }
            }
        }).start();
    }

    private void close() {
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.exit(0);
    }

    private void display(String message) {
        if (!textArea.getText().isEmpty()) {
            textArea.append("\n");
        }
        textArea.append(message);
    }
}

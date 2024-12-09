package com.chat;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends JFrame {
    private final ServerSocket serverSocket;
    private JPanel mainPanel;
    private JTextArea textArea;
    private JTextField textField;
    private JButton sendButton;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;

        sendButton.addActionListener(_ -> {
            String message = textField.getText();
            if (!message.isEmpty()) {
                broadcast("Server: " + message);
                display("Server: " + message);
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
        setTitle("Server");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 300);
        setContentPane(mainPanel);
        setVisible(true);

        display("Server started on port " + serverSocket.getLocalPort());
    }


    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(4000);
        Server server = new Server(serverSocket);
        server.start();
    }

    private void display(String message) {
        if (!textArea.getText().isEmpty()) {
            textArea.append("\n");
        }
        textArea.append(message);
    }

    private void broadcast(String message) {
        for (ClientHandler clientHandler : ClientHandler.clientHandlers) {
            try {
                clientHandler.outputStream.writeUTF(message);
            } catch (Exception e) {
                display("Error broadcasting message to " + clientHandler.getUsername());
            }
        }
    }

    public void start() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                display("New client connected: " + clientHandler.getUsername());
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            close();
        }
    }

    private void close() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

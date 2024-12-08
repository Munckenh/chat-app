package com.chat;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private final Socket socket;
    final DataInputStream inputStream;
    final DataOutputStream outputStream;
    private final String username;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());

        // First message from the client will be their username
        this.username = inputStream.readUTF();
        clientHandlers.add(this);
        broadcast("Server: " + this.username + " has joined the chat");
    }

    public String getUsername() {
        return this.username;
    }

    public void broadcast(String message) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.username.equals(this.username)) {
                    clientHandler.outputStream.writeUTF(message);
                    clientHandler.outputStream.flush();
                }
            } catch (IOException e) {
                clientHandler.close();
            }
        }
    }

    private void close() {
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        clientHandlers.remove(this);
        broadcast("Server: " + username + " has left the chat");
    }

    @Override
    public void run() {
        String message;
        while (socket.isConnected()) {
            try {
                message = inputStream.readUTF();
                broadcast(message);
            } catch (IOException e) {
                break;
            }
        }
        close();
    }
}

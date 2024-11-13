package org.example.host;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Host {
    private static Set<ClientHandler> clients = new HashSet<>();

    public static void host(){
        try (ServerSocket socket = new ServerSocket(1234)){
            while (true){
                ClientHandler client = new ClientHandler(socket.accept());
                clients.add(client);
                new Thread(client).start();
            }
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public static void broadcastMessage(String message){
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public static class ClientHandler implements Runnable{
        private final Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket){
            this.socket = socket;
        }

        public void run(){
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                String message;
                while ((message = in.readLine()) != null){
                    System.out.println(message);
                    broadcastMessage(message);
                }
            } catch (IOException e) {
                System.out.println("Failed to create client handler, skipping: " + e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Failed to close unused socket: " + e);
                }
                clients.remove(this);
            }
        }

        public void sendMessage(String message){
            out.println(message);
        }
    }
}

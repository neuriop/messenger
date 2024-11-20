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
    public static final int PORT = 5000;
    private static Set<ClientHandler> clients = new HashSet<>();

    public static void host() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void broadcast(String input){
        System.out.println(clients.size());
        for (ClientHandler client : clients) {
            client.sendMessage(input);
        }
        System.out.println("Message broadcasted");
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) throws IOException {
            clientSocket = socket;
            out = new PrintWriter(socket.getOutputStream());
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                String input;
                while ((input = in.readLine()) != null){
                    broadcast(input);
                    System.out.println("Message received: "+ input);
                }

                clients.remove(this);
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void sendMessage(String input) {
            out.println(input);
            System.out.println("Message sent");
        }
    }
}

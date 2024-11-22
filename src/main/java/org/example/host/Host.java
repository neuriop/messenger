package org.example.host;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Host {
    public static final int PORT = 5000;
    private static Set<ClientHandler> clients = new HashSet<>();
    private static Map<String, ClientHandler> clients2 = new HashMap<>();

    public static void host() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients2.put(clientHandler.username, clientHandler);
                new Thread(clientHandler).start();
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void broadcast(String input, ClientHandler clientHandler) {
        System.out.println(clients.size());
        for (ClientHandler client : clients) {
            if (client != clientHandler)
                client.sendMessage(input);
        }
        System.out.println("Message broadcast");
    }

    public static void privateMessage(String username, String input){
        if (clients2.containsKey(username)){
            clients2.get(username).sendMessage(input);
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final PrintWriter out;
        private final BufferedReader in;
        private String username;
        private Set<String> friendList = new HashSet<>();
        private String privateMessageUsername;


        public ClientHandler(Socket socket) throws IOException {
            clientSocket = socket;
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        }

        private void addFriend(String username) {
            if (clients2.containsKey(username)) {
                friendList.add(username);
            } else sendMessage("This user does not exist");
        }

        private void removeFriend(String username) {
            friendList.remove(username);
        }

        private void setUsername() throws IOException {
            sendMessage("Enter username: ");
            String s;
            while ((s = in.readLine()) != null) {
                username = s;
            }
        }

        private String getUsername() {
            return username;
        }

        private void setPrivateMessageUsername() throws IOException {
            sendMessage("Enter username: ");
            String s;
            while ((s = in.readLine()) != null) {
                privateMessageUsername = s;
            }
        }

        @Override
        public void run() {
            try {
                String input;
                while ((input = in.readLine()) != null) {
                    if (input.startsWith("/addfr ")) {
                        addFriend(input.replace("/addfr ", ""));
                    } else if (input.startsWith("/remfr ")){
                        removeFriend(input.replace("/remfr ", ""));
                    } else if (input.startsWith("/msg ")){
//                        privateMessage();
                    }else {
                        broadcast(input, this);
                        System.out.println("Message received: " + input);
                    }
                }

                clients.remove(this);
                clients2.remove(username);
                out.close();
                in.close();
                clientSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void sendMessage(String input) {
            out.println("[" + username + "]:" + input);
            System.out.println("Message sent");
        }
    }
}

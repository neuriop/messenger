package org.example.host;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

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
        System.out.println(clients2.size());
        for (ClientHandler client : clients2.values()) {
            if (client != clientHandler){
                client.sendMessage(input);
            }
        }
        System.out.println("Message broadcast");
    }

    private static void groupChat(String input, Set<String> users){
        for (String user : users) {
            if (clients2.containsKey(user)){
                clients2.get(user).sendMessage("Message from group: " + input);
            }
        }
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

        public ClientHandler(Socket socket) throws IOException {
            clientSocket = socket;
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            setUsername();
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
            out.println("Enter username: ");
            System.out.println("Waiting for username...");
            while (true) {
                String input = in.readLine();
                if (input == null) {
                    System.out.println("Client disconnected while setting username.");
                    return;
                }
                username = input.trim();
                if (isValidUsername(username)) {
                    if (clients2.containsKey(username)) {
                        out.println("Username is already taken. Please choose another one.");
                    } else {
                        clients2.put(username, this);
                        System.out.println("Username set " + username);
                        break;
                    }
                } else {
                    out.println("Username must contain only alphanumeric characters.");
                }
            }
        }

//        private void setUsername() throws IOException {
//            out.println("Enter username: ");
//            System.out.println("waiting for username");
//            String input;
//            while (true) {
//                System.out.println("waiting for username");
//                while ((input = in.readLine()) != null) {
//                    username = input;
//                }
//                System.out.println(username);
//                if (isValidUsername(username)){
//                    break;
//                } else {
//                    sendMessage("Username must contain only alphanumeric characters");
//                }
//            }
//        }

        private String getUsername() {
            return username;
        }

        private Set<String> getFriendList(){
            return friendList;
        }

        private String getPrivateMessageUsername(String message) throws IOException {
            message = message.replace("/msg ", "");
            String[] parts = message.split(" ");
            return parts[0];
        }

        private String getMessageFromMsg(String message){
            message = message.replace("/msg ", "");
            String[] parts = message.split(" ");
            parts [0] = "private:" + username + ":";
            return String.join(" ", parts);
        }

        private String[] removeFirstElement(String[] array) {
            if (array.length <= 1) {
                return new String[0];
            }
            String[] newArray = new String[array.length - 1];
            for (int i = 1; i < array.length; i++) {
                newArray[i - 1] = array[i];
            }
            return newArray;
        }

        public static boolean isValidUsername(String username) {
            return username.matches("^[a-zA-Z0-9]+$");
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
                    } else if (input.startsWith("/msg ")) {
                        privateMessage(getPrivateMessageUsername(input), getMessageFromMsg(input));
                    } else if (input.startsWith("/help")) {
                        sendMessage("/help - list commands" +
                                "\n/addfr <username> - add friend" +
                                "\n/remfr <username> - remove friend" +
                                "\n/msg <username> - send private message" +
                                "\n/group - send message to group of friends");
                    } else if (input.startsWith("/group ")){
                        groupChat(input.replace("/group ", ""), getFriendList());
                    } else {
                        broadcast("[" + username + "]:" + input, this);
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
            out.println(input);
            System.out.println("Message sent");
        }
    }
}

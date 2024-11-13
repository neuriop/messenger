package org.example.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    private String serverAddress = "localhost";
    private int serverPort = 1234;

    private String name;

    public Client(String name){
        this.name = name;
    }

    public Client(String name, String serverAddress, int port){
        this.name = name;
        this.serverAddress = serverAddress;
        serverPort = port;
    }

    public void client(){
        try (Socket socket = new Socket(serverAddress, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream());
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))){
            Scanner scanner = new Scanner(System.in);

            new Thread(() -> {
                String message;
                try {
                    while ((message = in.readLine()) != null){
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    System.out.println("Received unknown message: " + e);
                }
            });

            String userInput;
            while (true) {
                userInput = scanner.nextLine();
                out.println(name + userInput);
            }

        } catch (UnknownHostException e) {
            System.out.println("Unknown address or port: " + e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

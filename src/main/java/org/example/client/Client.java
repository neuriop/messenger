package org.example.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    private static final String ADDRESS = "localhost";
    private static final int PORT = 5000;


    public static void client() {
        try (Socket clientSocket = new Socket(ADDRESS, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true );){
            System.out.println("Client started");
            new Thread(() -> {
                System.out.println("Thread started");
                try {
                    String serverResponse;
                    System.out.println("Went here");
                    while ((serverResponse = in.readLine()) != null) {
                        System.out.println("Message" + serverResponse);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            Scanner scanner = new Scanner(System.in);
            while (true){
                System.out.println("Enter message");
                out.println(scanner.nextLine());
                System.out.println("Message sent");
            }

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

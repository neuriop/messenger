package org.example;

import org.example.client.Client;
import org.example.host.Host;

import java.util.Scanner;

public class Main {


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        if ("server".equals(scanner.nextLine())) {
            System.out.println("Starting server");
            Host.host();
        }

        else {
            System.out.println("Starting client");
            Client.client();
        }

        System.out.println("B");
    }
}
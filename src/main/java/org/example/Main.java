package org.example;

import org.example.client.Client;
import org.example.host.Host;

import java.util.Scanner;

public class Main {


    public static void main(String[] args) {
        String s = new Scanner(System.in).nextLine();



        switch (s) {
            case "host":
                Host.host();
                break;
            case "client":
                new Client(new Scanner(System.in).nextLine()).client();
        }
    }
}
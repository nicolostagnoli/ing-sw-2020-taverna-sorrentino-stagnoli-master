package it.polimi.ingsw;

import it.polimi.ingsw.server.MainServer;

import java.io.IOException;

public class ServerMain {

    public static void main(String[] args) {
        try {
            MainServer server = new MainServer();
            System.out.println("Starting server...");
            server.runServer();
        } catch (Exception e) {
            System.out.println("An unexpected exception occurred. Below you find the stack " +
                    "trace, in order to help you find the cause.");
            e.printStackTrace();
        }
    }
}

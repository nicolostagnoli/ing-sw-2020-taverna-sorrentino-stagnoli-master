package it.polimi.ingsw.server;

import it.polimi.ingsw.observer.Observable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//                            To send coming messages (from the client) to the listener (the RemoteView)
public class Connection extends Observable<Object> implements Runnable {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private Lobby lobby;

    private boolean active = true;

    private ExecutorService ex = Executors.newFixedThreadPool(1);

    public Connection(Socket socket, Lobby lobby, ObjectOutputStream o, ObjectInputStream i) {
        this.socket = socket;
        this.lobby = lobby;
        this.out = o;
        this.in = i;
    }

    public Connection(Socket socket, ObjectOutputStream o, ObjectInputStream i){
        this.socket = socket;
        this.out = o;
        this.in = i;
    }

    /**
     * To get a reference to the ObjectOutputStream object
     * @return
     */
    public synchronized ObjectOutputStream getOutputStream(){
        return this.out;
    }

    /**
     * To get a reference to the ObjectInputStream object
     * @return
     */
    public synchronized ObjectInputStream getInputStream(){
        return this.in;
    }

    private synchronized boolean isActive(){
        return active;
    }

    private synchronized void send(Object message) {
        if(isActive()) {
            try {
                out.reset();
                out.writeUnshared(message);
                out.flush();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * Start a new thread to asynchronously send messages through the socket
     * @param message
     */
    public void asyncSend(final Object message){
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                send(message);
            }
        }).start();*/
        ex.execute(
                new Runnable() {
                       @Override
                       public void run() {
                           send(message);
                       }
                });
    }

    /**
     * To close the connection
     */
    public synchronized void closeConnection() {
        if(isActive()) {
            try {
                socket.close();
                ex.shutdown();
            } catch (IOException e) {
                System.err.println("Error when closing socket!");
            }
            active = false;
            System.out.println("Socket " + this.toString() + ": Closing connection");
        }
    }

    /**
     * To listen for coming messages from the socket.
     */
    @Override
    public void run() {
        try {
            while(isActive()){
                //notify the Player View that a message is arrived from the client
                Object received;
                try {
                    received = this.in.readObject();
                } catch (Exception e) {
                    System.out.println("A network error occurred: " + e.getMessage());
                    System.out.println("Closing connection...");
                    //e.printStackTrace();
                    return;
                }
                notify(received);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            //if this connection is running on the server, tell the lobby to close all the connections
        }finally{
            if (lobby != null) {
                lobby.closeConnections();
            }
            closeConnection();
        }
    }
}

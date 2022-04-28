package it.polimi.ingsw.server;

import it.polimi.ingsw.controller.Controller;
import it.polimi.ingsw.model.GameModel;
import it.polimi.ingsw.view.RemotePlayerView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Lobby {

    /*private class PingChecker implements Runnable{
        @Override
        public void run() {
            while(true){
                System.out.println("Pinging " + playersViews.size() + " clients.");
                for(RemotePlayerView view: playersViews){
                    try {
                        List<Object> message = new ArrayList<>();
                        message.add("onPing");
                        view.getClientConnection().getOutputStream().writeObject(message);
                    } catch (IOException e) {
                        //a client has been disconnected
                        //tell other client to disconnect
                        System.out.println("A client has been disconnected, disconnecting other clients...");
                        for(RemotePlayerView other: playersViews){
                            try {
                                List<Object> disconnection = new ArrayList<>();
                                disconnection.add("disconnected");
                                other.getClientConnection().getOutputStream().writeObject(disconnection);
                                other.getClientConnection().closeConnection();
                            } catch (IOException ex) {  }
                        }
                        //stop pinging
                        break;
                    }
                }
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) { break; }
            }
            System.out.println("Closing lobby...");
            closeLobby();
        }
    }*/

    private GameModel model;
    private Controller controller;
    private int numPlayers;

    private List<RemotePlayerView> playersViews;
    private RemotePlayerView challengerView;

    private ExecutorService executor = Executors.newCachedThreadPool();

    private MainServer server;

    private boolean isClosed = false;

    public Lobby(MainServer server, int numPlayers){
        this.server = server;
        try {
            this.model = new GameModel();
        }
        catch (FileNotFoundException e) {
            System.out.println("Couldn't create a GameModel because some files where missing.");
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.out.println("Exiting the application.");
            System.exit(1);
        }
        this.controller = new Controller(this.model);
        this.playersViews = new ArrayList<>();
        this.numPlayers = numPlayers;
        //new Thread(new PingChecker(), "PingChecker").start();
    }

    /**
     * To check if the lobby is full or not.
     * @return
     */
    public synchronized boolean isFull(){
        return this.playersViews.size() == this.numPlayers;
    }

    /**
     * To get all the nicknames already in the lobby
     * @return
     */
    public synchronized List<String> getPlayersNicknames(){
        List<String> result = new ArrayList<>();
        for(RemotePlayerView v: this.playersViews){
            result.add(v.getNickname());
        }
        return result;
    }

    /**
     * To add a player in the lobby
     * @param nickname The nickname of the new player
     * @param socket The socket object coming from the initial connection to the server
     * @param o The ObjectOutputStream related to the socket
     * @param i The ObjectInputStream related to the socket
     * @return True if the player is added to the lobby, false otherwise.
     */
    public synchronized boolean addPlayer(String nickname, Socket socket, ObjectOutputStream o, ObjectInputStream i){

        //if name is null or is already present, return false
        if(nickname == null || nickname.equals("") || this.playersViews.stream().anyMatch(x -> x.getNickname().equals(nickname)) )
            return false;

        if(socket.isClosed())
            return false;

        if(this.isClosed){
            return false;
        }

        if(this.isFull()){
            return false;
        }

        RemotePlayerView playerView = new RemotePlayerView(nickname, new Connection(socket, this, o, i));
        //start a separate thread waiting for client messages
        this.executor.submit(playerView.getClientConnection());
        //add to the list of players
        this.playersViews.add(playerView);
        //pass the controller to make the view to add it as listener
        playerView.addListener(controller);
        //the player view is a listener of the model
        this.model.addListener(playerView);

        //if it is the first player coming, he is the challenger
        if(this.playersViews.size() == 1){
            this.challengerView = playerView;
            this.setNumPlayers(this.numPlayers);
        }

        return true;
    }

    /**
     * To make the controller add the player to the model after the server finished it's initial communication with the new client
     * (after the player is added to the lobby)
     * @param nickname the nickname of the player
     */
    public synchronized void controllerAddPlayer(String nickname){
        RemotePlayerView playerView = this.playersViews.stream().filter(v -> v.getNickname().equals(nickname)).collect(Collectors.toList()).get(0);
        controller.onNicknameChosen(playerView, nickname);
    }

    private  void setNumPlayers(int numPlayers) {
        if(this.challengerView == null){
            throw new RuntimeException("There is no challanger in the lobby");
        }
        //the controller sets the number of players
        this.controller.onNumberOfPlayersChosen(this.challengerView, numPlayers);
    }

    /**
     * To get the number of players currently in the lobby
     * @return
     */
    public synchronized int getNumPlayers(){
        return model.getNumPlayers();
    }

    /**
     * To close the lobby and shutdown all connections to connected clients
     */
    public synchronized void closeConnections(){
        if(!isClosed) {
            System.out.println("A client has been disconnected, disconnecting other clients...");
            for (RemotePlayerView view : playersViews) {
                try {
                    //tell the client to disconnect
                    List<Object> disconnection = new ArrayList<>();
                    disconnection.add("onMessage");
                    disconnection.add("disconnected");
                    view.getClientConnection().getOutputStream().writeObject(disconnection);
                    //close the socket on the server connected to that client
                    view.getClientConnection().closeConnection();
                } catch (IOException ex) { /*do nothing*/ }
            }
            executor.shutdown();
            this.server.removeLobby(this);
            this.isClosed = true;
        }
    }
}

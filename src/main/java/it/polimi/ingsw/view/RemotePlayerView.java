package it.polimi.ingsw.view;

import it.polimi.ingsw.listeners.*;
import it.polimi.ingsw.model.Board;
import it.polimi.ingsw.model.Coord;
import it.polimi.ingsw.model.Level;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.observer.*;
import it.polimi.ingsw.server.Connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RemotePlayerView implements ModelEventListener, EventSource {

    //used for reacting when a message arrives from the client (assuming the client only sends strings)
    private class MessageReceiver implements Observer<Object> {
        @Override
        public void update(Object message) {
            System.out.println("Received: " + message.toString());
            receivedObject = message;
            handleMessageReceived();
        }
    }

    private Connection clientConnection;
    private Object receivedObject;

    private final String nickname;

    protected PlayerViewEventListener controller;

    public RemotePlayerView(String nickname, Connection cc){
        this.clientConnection = cc;
        //when a message arrives from the client, handle with my MessageReceiver
        this.clientConnection.addObserver(new MessageReceiver());

        this.nickname = nickname;
    }

    /**
     * To get reference to the connection object related to this remote view (server side)
     * @return the connection object
     */
    public Connection getClientConnection(){
        return this.clientConnection;
    }

    /**
     * used for adding the controller as a listener for the player view events
     * @param listener the controller
     */
    @Override
    public void addListener(Listener listener) {
        if (!(listener instanceof PlayerViewEventListener)) {
            throw new IllegalArgumentException("Tried to register a non-ViewEventListener to View.");
        }
        this.controller = (PlayerViewEventListener) listener;
    }

    //this method is fired when an object is received from the client
    private void handleMessageReceived() {
        List<Object> objects;
        if(receivedObject instanceof List)
            objects = (List<Object>)receivedObject;
        else {
            System.out.println("Something went wrong in handling received message");
            return;
        }

        //the first object received is always the event
        String event = (String) objects.get(0);
        switch (event) {
            //player events
            case "onGodChosen":
                String god = (String) objects.get(1);
                controller.onGodChosen(this, god);
                break;
            case "onWorkerInitialization":
                Coord choice = (Coord) objects.get(1);
                controller.onWorkerInitialization(this, choice);
                break;
            case "onWorkerChosen":
                Coord workerPos = (Coord) objects.get(1);
                controller.onWorkerChosen(this, workerPos);
                break;
            case "onMoveChosen":
                Coord moveChoice = (Coord) objects.get(1);
                controller.onMoveChosen(this, moveChoice);
                break;
            case "onBuildChosen":
                Coord buildChoice = (Coord) objects.get(1);
                Level level = (Level) objects.get(2);
                controller.onBuildChosen(this, buildChoice, level);
                break;
            case "skipAction":
                controller.skipAction(this);
                break;
            //challenger events
            case "onGodsChosen":
                List<String> gods = (List)objects.get(1);
                ((ChallengerViewEventListener)controller).onGodsChosen(this, gods);
                break;
            case "onStartPlayerChosen":
                String startPlayer = (String)objects.get(1);
                ((ChallengerViewEventListener)controller).onStartPlayerChosen(this, startPlayer);
                break;
            //client ping check
            /*case "onPong":
                //client ping successful
                break;*/
            default:
                System.out.println("Event message not recognized.");
                break;
        }
    }

    /**
     * used to send objects to the client through the Connection
     * @param message the object to send
     */
    public void sendObjectToClient(Object message){
        this.clientConnection.asyncSend(message);
    }

    //Model Events:

    /**
     * To send the onBoardChanged event to the client
     * @param board
     */
    @Override
    public void onBoardChanged(Board board) {
        List<Object> objects = new ArrayList<>();
        objects.add("onBoardChanged");
        objects.add(board);
        sendObjectToClient(objects);
    }

    /**
     * To send the onGameReady event to the client
     * @param players
     */
    @Override
    public void onGameReady(List<Player> players) {
        List<Object> objects = new ArrayList<>();
        objects.add("onGameReady");
        objects.add(players);
        sendObjectToClient(objects);
    }

    /**
     * To send the onGodsChosen event to the client
     * @param gods
     */
    @Override
    public void onGodsChosen(List<String> gods) {
        List<Object> objects = new ArrayList<>();
        objects.add("onGodsChosen");
        objects.add(gods);
        sendObjectToClient(objects);
    }

    /**
     * To send the onPlayerAdded event to the client
     * @param nickname
     * @param numCurr
     * @param numTot
     */
    @Override
    public void onPlayerAdded(String nickname, int numCurr, int numTot) {
        List<Object> objects = new ArrayList<>();
        objects.add("onPlayerAdded");
        objects.add(nickname);
        objects.add(numCurr);
        objects.add(numTot);
        sendObjectToClient(objects);
    }

    /**
     * To send the onMessage event to the client
     * @param message
     */
    @Override
    public void onMessage(String message) {
        List<Object> objects = new ArrayList<>();
        objects.add("onMessage");
        objects.add(message);
        sendObjectToClient(objects);
    }

    /**
     * To send the onGodSelection event to the client
     * @param nickname
     * @param gods
     */
    @Override
    public void onGodSelection(String nickname, List<String> gods) {
        List<Object> objects = new ArrayList<>();
        objects.add("onGodSelection");
        objects.add(nickname);
        objects.add(gods);
        sendObjectToClient(objects);
    }

    /**
     * To send the onGodsSelection event to the client
     * @param gods
     * @param numPlayers
     */
    @Override
    public void onGodsSelection(List<String> gods, int numPlayers) {
        List<Object> objects = new ArrayList<>();
        objects.add("onGodsSelection");
        objects.add(gods);
        objects.add(numPlayers);
        sendObjectToClient(objects);
    }

    /**
     * To send the onStartPlayerSelection event to the client
     * @param players
     */
    @Override
    public void onStartPlayerSelection(List<String> players) {
        List<Object> objects = new ArrayList<>();
        objects.add("onStartPlayerSelection");
        objects.add(players);
        sendObjectToClient(objects);
    }

    /**
     * To send the onMyInitialization event to the client
     * @param nickname
     * @param freeSpaces
     */
    @Override
    public void onMyInitialization(String nickname, List<Coord> freeSpaces) {
        List<Object> objects = new ArrayList<>();
        objects.add("onMyInitialization");
        objects.add(nickname);
        objects.add(freeSpaces);
        sendObjectToClient(objects);
    }

    /**
     * To send the onMyTurn event to the client
     * @param nickname
     * @param selectableWorkers
     */
    @Override
    public void onMyTurn(String nickname, List<Coord> selectableWorkers) {
        List<Object> objects = new ArrayList<>();
        objects.add("onMyTurn");
        objects.add(nickname);
        objects.add(selectableWorkers);
        sendObjectToClient(objects);
    }

    /**
     * To send the onMyAction event to the client
     * @param nickname
     * @param movableSpaces
     * @param buildableSpaces
     * @param canEndTurn
     */
    @Override
    public void onMyAction(String nickname, List<Coord> movableSpaces, Map<Level, List<Coord>> buildableSpaces, boolean canEndTurn) {
        List<Object> objects = new ArrayList<>();
        objects.add("onMyAction");
        objects.add(nickname);
        objects.add(movableSpaces);
        objects.add(buildableSpaces);
        objects.add(canEndTurn);
        sendObjectToClient(objects);
    }

    /**
     * To send the onEnd event to the client
     */
    @Override
    public void onEnd() {
        List<Object> objects = new ArrayList<>();
        objects.add("onEnd");
        sendObjectToClient(objects);
    }

    /**
     * Get the nickname of the client related to this View
     * @return
     */
    @Override
    public String getNickname() {
        return this.nickname;
    }


}


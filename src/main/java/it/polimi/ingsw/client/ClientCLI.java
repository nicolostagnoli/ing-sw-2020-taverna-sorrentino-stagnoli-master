package it.polimi.ingsw.client;

import it.polimi.ingsw.model.Board;
import it.polimi.ingsw.model.Coord;
import it.polimi.ingsw.model.Level;
import it.polimi.ingsw.observer.Observer;
import it.polimi.ingsw.server.Connection;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.*;

public class ClientCLI {

    private PrintStream outputStream;
    private Scanner s;

    //used for reacting when a message arrives from the server
    private class MessageReceiver implements Observer<Object> {
        @Override
        public void update(Object message) {
            //System.out.println("Received: " + message.toString());
            receivedObject = message;
            handleMessageReceived();
        }
    }

    private Object receivedObject;
    private Connection serverConnection;

    private boolean isChallenger;
    private String nickname = "";

    private ExecutorService exec;

    public ClientCLI(Connection serverConnection, boolean isChallenger, String nickname){
        //initialize connection object to send/receive objects from the server
        this.serverConnection = serverConnection;
        this.serverConnection.addObserver(new MessageReceiver());

        this.outputStream = new PrintStream(System.out);
        this.s = new Scanner(System.in);

        this.isChallenger = isChallenger;
        this.nickname = nickname;

        exec = Executors.newFixedThreadPool(1);
    }

    private void handleMessageReceived(){
        List<Object> objs;
        if(receivedObject instanceof List)
            objs = (List<Object>) receivedObject;
        else {
            outputStream.println("Something went wrong in handling received message");
            return;
        }
        String event = (String) objs.get(0);

        switch (event) {
            //MODEL MESSAGES
            case "onBoardChanged":
                Board b = (Board)objs.get(1);
                outputStream.println(b.toString());
                break;

            case "onGameReady":
                outputStream.println("Set up phase is done!");
                break;

            case "onGodsChosen":
                outputStream.println("Challenger has chosen the playable gods");
                List<String> selectedGods = (List<String>) objs.get(1);
                for (String s : selectedGods){
                    outputStream.println("- " + s);
                }
                break;

            case "onPlayerAdded":
                String newPlayer = (String) objs.get(1);
                int numCurr = (int) objs.get(2);
                int numTot = (int) objs.get(3);
                outputStream.println(newPlayer + " has joined the game. Waiting for " + (numTot-numCurr) + " more player(s)");
                break;

            case "onMessage":
                String message = (String)((List<Object>) receivedObject).get(1);
                outputStream.println(message);
                if(message.equals("disconnected")){
                    outputStream.println("A client disconnected from the game, disconnecting...");
                    this.stop();
                }
                break;

            case "onGodSelection":
                String currPlayer = (String) objs.get(1);
                List<String> godsForSelection = (List<String>) objs.get(2);
                if(currPlayer.equals(nickname)){
                    onGodSelection(godsForSelection);
                }else{
                    outputStream.println(currPlayer + " is choosing his god...");
                }

                break;

            case "onGodsSelection":
                if (isChallenger) {
                    List<String> allGods = (List<String>) objs.get(1);
                    int numPlayers = (int) objs.get(2);
                    onGodsSelection(allGods, numPlayers);
                } else
                    outputStream.println("The challenger is choosing the gods");
                break;

            case "onMyInitialization":
                String currPlayerr = (String) objs.get(1);
                List<Coord> freeSpaces = (List<Coord>) objs.get(2);
                if(currPlayerr.equals(nickname)) {
                    onMyInitialization(freeSpaces);
                }else{
                    outputStream.println(currPlayerr + " is placing his workers...");
                }
                break;

            case "onMyTurn":
                String currPlayerrr = (String) objs.get(1);
                List<Coord> selectableWorkers = (List<Coord>) objs.get(2);
                if(currPlayerrr.equals(nickname)) {
                    onMyTurn(selectableWorkers);
                }else{
                    outputStream.println(currPlayerrr + "'s turn.");
                }
                break;

            case "onMyAction":
                String currPlayerrrr = (String) objs.get(1);
                List<Coord> movableSpaces = (List<Coord>) objs.get(2);
                Map<Level, List<Coord>> buildableSpaces = (Map<Level, List<Coord>>) objs.get(3);
                boolean canEndTurn = (boolean) objs.get(4);
                if(currPlayerrrr.equals(nickname)) {
                    onMyAction(movableSpaces, buildableSpaces, canEndTurn);
                }
                break;

            case "onStartPlayerSelection":
                if(isChallenger){
                    List<String> players = (List<String>) objs.get(1);
                    onStartPlayerSelection(players);
                }
                else {
                    System.out.println("Challenger is choosing the starting player");
                }
                break;

            case "onEnd":
                outputStream.println("Game Ended");
                this.onEnd();
                break;

            default:
                outputStream.println("Event message not recognized.");
                break;
        }
    }

    private void onGodSelection(List<String> gods){

        boolean correct = false;

        while (!correct){
            outputStream.println("Choose a God, you can use \"[god_name] help\" to read the power of the God: ");
            for (String g : gods) {
                outputStream.println("- " + g);
            }

            String input = s.nextLine();

            for(String g : gods){
                if (input.toLowerCase().equals(g.toLowerCase())){
                    correct = true;
                    break;
                }
            }

            if (correct){
                List<Object> objects = new ArrayList<>();
                objects.add("onGodChosen");
                objects.add(input);
                serverConnection.asyncSend(objects);
                if(gods.size() != 1) { //if i am the last to choose, do not print
                    outputStream.println("Waiting for other players to choose their god...");
                }
            }
        }
    }

    private void onGodsSelection(List<String> gods, int numPlayers) {
        outputStream.println("Choose the gods to use in this game: ");
        gods.forEach(outputStream::println);
        boolean valid = false;
        boolean godIsOk = false;
        List<Object> objects = new ArrayList<>();
        List<String> choices = new ArrayList<>();

        while (!valid){
            String input = s.nextLine();
            input = input.toLowerCase();

            for (String god : gods) {
                if (god.toLowerCase().equals(input)) {
                    choices.add(god);
                    godIsOk = true;
                    break;
                }
            }

            if (choices.size() == numPlayers){
                valid = true;
                objects.add("onGodsChosen");
                objects.add(choices);
                serverConnection.asyncSend(objects);
            } else
                outputStream.println("Choose another god");

            if(godIsOk)
                godIsOk = false;
            else
                outputStream.println("Invalid input");
        }
    }

    private void onStartPlayerSelection(List<String> players) {
        outputStream.println("Choose the starting player: ");
        List<Object> objects = new ArrayList<>();
        objects.add("onStartPlayerChosen");

        for (int i = 0; i<players.size(); i++) {
            outputStream.println( (i+1) + " " + players.get(i));
        }

        boolean valid = false;

        while (!valid){
            String input = s.nextLine();

            if (input.equals("1") || input.toLowerCase().equals(players.get(0).toLowerCase())){
                valid = true;
                objects.add(players.get(0));
                serverConnection.asyncSend(objects);
            }else if (input.equals("2") || input.toLowerCase().equals(players.get(1).toLowerCase())){
                valid = true;
                objects.add(players.get(1));
                serverConnection.asyncSend(objects);
            }else if (input.equals("3") || input.toLowerCase().equals(players.get(2).toLowerCase())){
                valid = true;
                objects.add(players.get(2));
                serverConnection.asyncSend(objects);
            } else {
                outputStream.println("Invalid nickname");
            }
        }

    }

    private void onMyInitialization(List<Coord> freeSpaces) {
        boolean valid = false;
        List<Object> objects = new ArrayList<>();
        objects.add("onWorkerInitialization");

        outputStream.println("Where do you want to place your worker (insert coordinates like: A1, B2... from A to E, from 1 to 5)?");
        while (!valid) {
            String input  = s.nextLine();
            try{
                Coord c = Coord.convertStringToCoord(input);
                if (freeSpaces.contains(c)){
                    try{
                        valid = true;
                        objects.add(c);
                        serverConnection.asyncSend(objects);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else{
                    outputStream.println("Invalid input");
                }
            } catch (Exception e) {
                outputStream.println("Invalid input");
            }
        }

    }

    private void onMyTurn(List<Coord> selectableWorkers) {

        boolean correct = false;
        List<Object> objects = new ArrayList<>();
        objects.add("onWorkerChosen");

        while (!correct){
            outputStream.println("Please select the worker to use in this turn, using its position: ");
            String input = s.nextLine();
            try {
                Coord c = Coord.convertStringToCoord(input);

                if (selectableWorkers.contains(c))
                    correct = true;

                if (correct){
                    try{
                        objects.add(c);
                        serverConnection.asyncSend(objects);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                outputStream.println("Invalid input");
            }
        }
    }

    private void onMyAction(List<Coord> movableSpaces, Map<Level, List<Coord>> buildableSpaces, boolean canEndTurn) {

        // String colors
        //String selectable = "\u001B[97m"; // Bright white
        // It doesn't print on IntelliJ white console...
        String selectable = "\u001B[0m"; // Standard
        String nonSelectable = "\u001B[90m"; // Grey
        String reset = "\u001B[0m"; // Standard

        //booleans for the menu
        boolean[] valid = new boolean[3];
        boolean correctInput = false;
        boolean isBuild = false;

        // Menu

        outputStream.println("What would you like to do?");

        if (movableSpaces.isEmpty())
            outputStream.println(nonSelectable + "1. Move" + reset);
        else {
            outputStream.println(selectable + "1. Move" + reset);
            valid[0] = true;
        }

        for (List<Coord> list : buildableSpaces.values()){
            if (!list.isEmpty()) {
                isBuild = true;
                break;
            }
        }

        if (!isBuild)
            outputStream.println(nonSelectable + "2. Build" + reset);
        else {
            outputStream.println(selectable + "2. Build" + reset);
            valid[1] = true;
        }

        if (!canEndTurn)
            outputStream.println(nonSelectable + "3. End" + reset);
        else {
            outputStream.println(selectable + "3. End" + reset);
            valid[2] = true;
        }

        String input;

        while (!correctInput){
            input = s.nextLine();

            if ((input.toLowerCase().equals("move") || input.equals("1")) && valid[0]) {
                correctInput = true;
                this.askToMove(movableSpaces);
            }
            if ((input.toLowerCase().equals("build") || input.equals("2")) && valid[1]) {
                correctInput = true;
                this.askToBuild(buildableSpaces);
            }
            if ((input.toLowerCase().equals("end") || input.equals("3")) && valid[2]) {
                correctInput = true;
                List<Object> objects = new ArrayList<>();
                objects.add("skipAction");
                serverConnection.asyncSend(objects);
            } else
                outputStream.println("Please enter a valid action");

        }

        //End Menu
    }

    private void askToBuild(Map<Level, List<Coord>> buildableSpaces){
        outputStream.println("Where do you want to build? ");
        boolean validCoord = false;
        boolean validLevel = false;
        int choice = 0;
        List<Level> possibleLevels= new ArrayList<>();

        while (!validCoord){
            String input = s.nextLine();
            try{
                Coord c = Coord.convertStringToCoord(input);
                for (Level key : buildableSpaces.keySet()){
                    List<Coord> list = buildableSpaces.get(key);
                    if (list.contains(c)) {
                        validCoord = true;
                        choice++;
                        possibleLevels.add(key);
                    }
                }

                if(validCoord){
                    if(choice == 1) {
                        List<Object> objects = new ArrayList<>();
                        objects.add("onBuildChosen");
                        objects.add(c);
                        objects.add(possibleLevels.get(0));
                        serverConnection.asyncSend(objects);
                    } else{
                        outputStream.println("What level would you like to build?");
                        for (Level l : possibleLevels){
                            outputStream.println("- " + l);
                        }
                        while (!validLevel){
                            String inputLvl = s.nextLine();

                            for (Level l : possibleLevels){
                                if (l.equals(Level.valueOf(inputLvl.toUpperCase()))){
                                    validLevel = true;
                                    break;
                                }
                            }

                            if(validLevel) {
                                List<Object> objects = new ArrayList<>();
                                objects.add("onBuildChosen");
                                objects.add(c);
                                objects.add(Level.valueOf(inputLvl.toUpperCase()));
                                serverConnection.asyncSend(objects);
                            } else
                                outputStream.println("Please enter a valid level");
                        }
                    }
                }
                else
                    outputStream.println("Please enter a valid coordinate");
            } catch (Exception e) {
                outputStream.println("Invalid input");
            }
        }
    }

    private void askToMove(List<Coord> movableSpaces){
        outputStream.println("Where do you want to move? ");
        boolean valid = false;

        while (!valid){
            String input = s.nextLine();
            try{
                Coord c = Coord.convertStringToCoord(input);
                if (movableSpaces.contains(c))
                    valid = true;

                if(valid) {
                    List<Object> objects = new ArrayList<>();
                    objects.add("onMoveChosen");
                    objects.add(c);
                    serverConnection.asyncSend(objects);
                } else
                    outputStream.println("Please enter a valid coordinate");
            } catch (Exception e) {
                outputStream.println("Invalid input");
            }

        }
    }

    private void onEnd(){
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.serverConnection.closeConnection();
        this.stop();
    }

    /**
     * To start a thread listening for incoming messages from the server
     */
    public void run(){
        outputStream.println("You have entered the lobby.");

        Future<?> future = exec.submit(serverConnection);

        //wait for the thread to finish
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            //e.printStackTrace();
        }
    }

    /**
     * To stop the thread listening for incoming messages from the server
     */
    public void stop(){
        this.exec.shutdown();
    }

    private String decorateBoard(Board board, List<String> toBeAdded, int offset) {
        String[] splitBoard = board.toString().split("\n");

        for (String s : toBeAdded) {
            splitBoard[toBeAdded.indexOf(s) + offset] += "\t\t\t" + s;
        }

        String newBoard = "";
        for (String s : splitBoard) {
            newBoard += s + "\n";
        }

        return newBoard;
    }
}
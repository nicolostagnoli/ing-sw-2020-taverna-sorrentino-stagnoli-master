package it.polimi.ingsw;

import it.polimi.ingsw.client.ClientCLI;
import it.polimi.ingsw.client.ClientMainGUI;
import it.polimi.ingsw.server.Connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientMain
{
    public static void main( String[] args ) {

        String ip = "";
        int port = 12345;

        Scanner s = new Scanner(System.in);
        Scanner ss = new Scanner(System.in);
        Socket socket = null;
        ObjectInputStream in;
        ObjectOutputStream out;

        int gui = -1;
        while(gui != 1 && gui != 2){
            System.out.println("Which client do you want to run?");
            System.out.println("1. CLI");
            System.out.println("2. GUI");
            try {
                gui = Integer.parseInt(s.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Insert a valid input.");
            }
        }
        //s.skip(".*\n");

        boolean ipCorrect = false;
        Pattern ipPattern = Pattern.compile("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})");
        while (!ipCorrect) {
            System.out.println("IP Address to connect to: ");
            ip = s.nextLine();
            Matcher m = ipPattern.matcher(ip);
            if (m.matches()) {
                ipCorrect = true;
                for (int i = 1; i <= 4; i++) {
                    if (!(Integer.parseInt(m.group(i)) <= 255 && Integer.parseInt(m.group(i)) >= 0)) {
                        ipCorrect = false;
                    }
                }
            }
            else if (ip.equals("localhost")) {
                ip = "127.0.0.1";
                ipCorrect = true;
            }
        }

        if(gui == 1) {

            ClientCLI cli = null;

            try {
                socket = new Socket(ip, port);
                socket.setKeepAlive(true);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                System.out.println("Connection to the server failed.");
                return;
            }

            boolean finished = false;

            try {

                while (!finished) {

                    //lobby selection
                    Map<Integer, List<String>> availableLobbies = new HashMap<>();
                    Map<Integer, Integer> availableLobbiesMaxPlayers = new HashMap<>();

                    //entering a lobby
                    boolean challenger = false;
                    int numPlayers = 0;
                    String nickname = "";

                    //lobby selection
                    boolean lobbyOK = false;
                    int lobbyChoice = -1;
                    while (!lobbyOK) {
                        out.writeObject("?lobbies");
                        out.flush();
                        int numLobbies = (int) in.readObject();
                        for (int i = 0; i < numLobbies; i++) {
                            List<String> playersInLobby = (List<String>) in.readObject();
                            int maxPlayers = (int) in.readObject();
                            int lobbyNum = (int) in.readObject();
                            availableLobbies.put(lobbyNum, playersInLobby);
                            availableLobbiesMaxPlayers.put(lobbyNum, maxPlayers);
                        }

                        //print lobbies info
                        System.out.println("Choose the lobby to join (insert a number) :");
                        System.out.println("0 - Create new lobby");
                        for (Integer i : availableLobbies.keySet()) {
                            int maxP, currentP;
                            maxP = availableLobbiesMaxPlayers.get(i);
                            currentP = availableLobbies.get(i).size();
                            System.out.print((i + 1) + " - Players (" + currentP + "/" + maxP + "): ");
                            for (String name : availableLobbies.get(i)) {
                                System.out.print("\"" + name + "\" ");
                            }
                            System.out.println("");
                        }
                        //user choice
                        lobbyChoice = -1;
                        while (lobbyChoice < 0) {
                            try {
                                lobbyChoice = s.nextInt();
                            } catch (Exception e) {
                                System.out.println("Insert a digit.");
                            }
                            if (lobbyChoice == 0) {
                                break;
                            }
                            //if input invalid or lobby full
                            if (lobbyChoice < 0 || lobbyChoice > availableLobbies.keySet().size() || availableLobbiesMaxPlayers.get(lobbyChoice - 1) == availableLobbies.get(lobbyChoice - 1).size()) {
                                System.out.println("Invalid input, try again");
                                lobbyChoice = -1;
                            }
                        }
                        out.writeObject("lobbySelected");
                        out.flush();
                        out.writeObject(lobbyChoice);
                        out.flush();

                        String result = (String) in.readObject();
                        if (result.equals("lobbySelectedOK")) {
                            lobbyOK = true;
                            result = (String) in.readObject();
                            if (result.equals("challenger")) {
                                challenger = true;
                                System.out.println("You are the Challenger.");
                            } else {
                                challenger = false;
                            }
                        }

                    }//lobby selection loop

                    //entering lobby

                    if (challenger) { //create lobby
                        List<String> nicknamesInLobby = availableLobbies.get(lobbyChoice - 1);
                        nickname = askForNickname(nicknamesInLobby);
                        out.writeObject("nicknameSelected");
                        out.flush();
                        out.writeObject(nickname);
                        out.flush();

                        System.out.println("Insert the number of opponents (1 or 2):");
                        while (numPlayers != 1 && numPlayers != 2) {
                            try {
                                numPlayers = s.nextInt();
                            } catch (Exception e) {
                                System.out.println("Insert a digit.");
                                s.next();
                            }
                            if (numPlayers != 1 && numPlayers != 2)
                                System.out.println("Invalid input, try again");
                        }
                        numPlayers++;
                        out.writeObject("numPlayersSelected");
                        out.flush();
                        out.writeObject(numPlayers);
                        out.flush();

                        String result = (String) in.readObject();
                        if (result.equals("ok")) {
                            finished = true;
                            cli = new ClientCLI(new Connection(socket, out, in), challenger, nickname);
                            cli.run();
                            break;
                        }
                    } else { //join lobby
                        List<String> nicknamesInLobby = availableLobbies.get(lobbyChoice - 1);
                        nickname = askForNickname(nicknamesInLobby);
                        out.writeObject("nicknameSelected");
                        out.flush();
                        out.writeObject(nickname);
                        out.flush();

                        String result = (String) in.readObject();
                        if (result.equals("ok")) {
                            finished = true;
                            cli = new ClientCLI(new Connection(socket, out, in), challenger, nickname);
                            cli.run();
                            break;
                        }
                    }

                }//main loop
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Connection went down while trying to connect.");
            } finally {
                try {
                    in.close();
                    out.close();
                    socket.close();
                } catch (IOException e) {
                }
            }

            System.out.println("Game finished. Closing application...");
            if (cli != null) {
                cli.stop();
            }
        }

        else{//if gui chosen
            String[] argss = new String[1];
            argss[0] = ip;
            ClientMainGUI.main(argss);
        }
    }

    private static String askForNickname(List<String> nicknamesInLobby){
        String nickname = "";
        Scanner ss = new Scanner(System.in);
        System.out.println("What's your nickname?");
        if (nicknamesInLobby != null && nicknamesInLobby.size() > 0) {
            System.out.println("Players already in the lobby: ");
            nicknamesInLobby.forEach(System.out::println);
        }
        while (nicknamesInLobby != null && nicknamesInLobby.contains(nickname) || nickname.equals("") ) {
            nickname = ss.nextLine();
            if (nicknamesInLobby != null && nicknamesInLobby.contains(nickname))
                System.out.println("Invalid input: nickname already in the lobby.");
            else if (nickname.equals(""))
                System.out.println("Invalid input: nickname can't be an empty string.");
        }

        return nickname;
    }
}

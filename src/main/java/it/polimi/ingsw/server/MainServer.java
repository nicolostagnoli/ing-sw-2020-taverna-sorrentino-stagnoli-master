package it.polimi.ingsw.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainServer {

    private static final int PORT = 12345;
    private ServerSocket serverSocket;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private List<Socket> pendingSockets;

    private List<Lobby> lobbies;

    public MainServer() throws IOException {
        this.serverSocket = new ServerSocket(PORT);
        this.lobbies = new ArrayList<>();
        this.pendingSockets = new ArrayList<>();
    }

    private class ClientInitializer implements Runnable {

        Socket socket;
        ObjectOutputStream out;
        ObjectInputStream in;

        MainServer server;

        boolean valid = true;


        public ClientInitializer(Socket s, MainServer server) {
            this.server = server;
            this.socket = s;
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            }catch (IOException e){
                valid = false;
            }
        }

        @Override
        public void run() {

            int selectedLobbyIndex = -1;
            Lobby selectedLobby = null;
            String nickname = "";
            int numPlayers = -1;
            boolean finished = false;

            String request = "";
            while (!finished) {
                try {
                    request = (String) in.readObject();

                    switch (request) {

                        case "?lobbies":
                            //send available lobbies
                            selectedLobbyIndex = -1;
                            selectedLobby = null;
                            int counter = 0;
                            synchronized (lobbies) {
                                out.writeObject(lobbies.size()); //num lobbies
                                out.flush();
                                for (Lobby l : lobbies) {
                                    out.writeObject(lobbies.get(counter).getPlayersNicknames());
                                    out.flush();
                                    out.writeObject(lobbies.get(counter).getNumPlayers());
                                    out.flush();
                                    out.writeObject(counter);
                                    out.flush();
                                    counter++;
                                }
                            }
                            break;

                        case "lobbySelected":
                            //user is selecting an available lobby (0 to create one)
                            selectedLobbyIndex = -1;
                            selectedLobby = null;
                            selectedLobbyIndex = (int) in.readObject();
                            if (selectedLobbyIndex == 0) {
                                //create new lobby
                                out.writeObject("lobbySelectedOK");
                                out.flush();
                                out.writeObject("challenger");
                                out.flush();
                                break;
                            }
                            synchronized (lobbies) {
                                if ((selectedLobbyIndex - 1) >= 0 && (selectedLobbyIndex - 1) < lobbies.size()) {
                                    //join a lobby
                                    selectedLobby = lobbies.get(selectedLobbyIndex - 1);
                                    out.writeObject("lobbySelectedOK");
                                    out.flush();
                                    out.writeObject("!challenger");
                                    out.flush();
                                    break;
                                }
                            }
                            out.writeObject("lobbySelectedKO"); //lobby not ok
                            out.flush();
                            break;

                        case "?nicknames":
                        /*if(selectedLobbyIndex != 0 && selectedLobby != null) {
                            List<String> nicknames = selectedLobby.getPlayersNicknames();
                            out.writeObject(nicknames);
                            out.flush();
                        }
                        else{
                            List<String> nicknames = new ArrayList<>();
                            out.writeObject(nicknames);
                            out.flush();
                        }*/
                            break;

                        case "nicknameSelected":
                            nickname = (String) in.readObject();

                            //if entering a lobby
                            if (selectedLobbyIndex != 0 && selectedLobby != null) {
                                //check if lobby is full
                                if (selectedLobby.isFull()) {
                                    out.writeObject("fullLobby");
                                    out.flush();
                                    break;
                                }
                                boolean validNickname = selectedLobby.addPlayer(nickname, socket, out, in);
                                if (validNickname) {
                                    //add player to the lobby
                                    pendingSockets.remove(this.socket);
                                    out.writeObject("ok");
                                    out.flush();
                                    finished = true;
                                    selectedLobby.controllerAddPlayer(nickname); //when last player arrives, game loop starts here
                                } else { //lobby is full
                                    out.writeObject("fullLobby");
                                    out.flush();
                                    break;
                                }
                            }
                            break;

                        case "numPlayersSelected":
                            numPlayers = (int) in.readObject();

                            //create new lobby
                            if (selectedLobbyIndex == 0 && selectedLobby == null) {

                                //if valid name and num players
                                if ((numPlayers == 2 || numPlayers == 3) && !nickname.equals("")) {

                                    //valid name and numPlayers, create the lobby
                                    Lobby newLobby = new Lobby(server, numPlayers);
                                    synchronized (lobbies) {
                                        lobbies.add(newLobby);
                                    }
                                    //the player is the challanger
                                    newLobby.addPlayer(nickname, socket, out, in);
                                    newLobby.controllerAddPlayer(nickname);

                                    out.writeObject("ok");
                                    out.flush();

                                    finished = true;
                                    pendingSockets.remove(this.socket);
                                } /*else { //send error message
                                    out.writeObject("fullLobby");
                                    out.flush();
                                }*/
                            }
                            break;

                        default:
                            System.out.println("Message from client not recognized.");
                            break;

                    }//switch request
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Client disconnected while trying entering a lobby. Not registered.");
                    pendingSockets.remove(this.socket);
                    break;
                }
            }

        }//void run

    }//class

    /**
     * To run the server and make it listen for clients
     * @throws IOException
     */
    public void runServer() throws IOException {
        while(true) {
            try {
                Socket newSocket = serverSocket.accept();
                newSocket.setKeepAlive(true);
                pendingSockets.add(newSocket);
                executor.submit(new ClientInitializer(newSocket, this));
            } catch (Exception e) {
                System.out.println("A network problem occurred.");
                System.out.println("Closing server...");
                break;
            }
        }

        //shutdown all threads
        executor.shutdown();
        serverSocket.close();
        //close and shutdown all lobbies
        synchronized (lobbies) {
            for (Lobby l : lobbies) {
                l.closeConnections();
            }
            //close all pending sockets
            for (Socket s: pendingSockets){
                s.close();
            }
        }
    }

    /**
     * To remove a lobby from the list
     * @param lobby The lobby to remove
     */
    public void removeLobby(Lobby lobby){
        System.out.println("Server: Closing lobby " + lobby.toString());
        synchronized (lobbies) {
            this.lobbies.remove(lobby);
        }
    }
}

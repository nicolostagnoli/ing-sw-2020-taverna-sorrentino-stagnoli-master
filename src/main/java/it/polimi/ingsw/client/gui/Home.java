package it.polimi.ingsw.client.gui;

import it.polimi.ingsw.server.Connection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class Home implements Initializable {

    public AnchorPane homePane, numPlayersPane, nicknamePane, lobbyListPane;
    public Label numPlayersError;
    public TextField numPlayersTextField, nicknameTextField;
    public ListView<String> lobbyList;
    public ImageView lobbyListNext, numPlayersNextBtn, backBtn, refreshBtn;

    private boolean challenger = false;
    private Map<Integer, List<String>> availableLobbies = new HashMap<>(); //lobby index and list of players
    private Map<Integer, Integer> availableLobbiesMaxPlayers = new HashMap<>(); //lobby index and max num players
    private String nickname;
    private int numPlayers;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean isConnected;
    private String ip;

    private Parent root;
    private FXMLLoader loader;

    /**
     * Intialize the window
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/board.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * To establish a connection to the server
     * @param ip the ip address of the server
     */
    public void connectToServer(String ip){
        try {
            this.ip = ip;
            socket = new Socket(ip, 12345);
            socket.setKeepAlive(true);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            isConnected = true;
        } catch (IOException e) {
            System.out.println("Connection to the server failed.");
            System.exit(0);
            return;
        }
    }

    /**
     * To check if the connection to the server is still alive
     * @return
     */
    public boolean isConnected(){ return this.isConnected; }

    /**
     * To close the connection and quit the application
     */
    public void close(){
        try {
            System.out.println("Connection Lost, closing...");
            this.socket.close();
            this.in.close();
            this.out.close();
            ((Stage)homePane.getScene().getWindow()).close();
            Platform.exit();
            System.exit(0);
        } catch (IOException e) { }
    }

    /**
     * Called when enter is pressed in opponents pane
     */
    @FXML
    public void opponentsKeyPressed(){
        opponentsChosen();
    }

    /**
     * Called when enter is pressed in nickname pane
     */
    @FXML
    public void nicknameKeyPressed(){
        nicknameChosen();
    }

    /**
     * Called when enter is pressed in lobby list pane
     */
    @FXML
    public void lobbylistKeyPressed(){
        lobbyListNext();
    }

    /**
     * Called when createGame button is clicked
     */
    @FXML
    public void createGame(){
        this.challenger = true;
        this.homePane.setVisible(false);
        this.nicknamePane.setVisible(true);
        this.backBtn.setVisible(true);
    }

    /**
     * Called when joinGame button is clicked
     */
    @FXML
    public void joinGame(){
        this.availableLobbies.clear();
        this.availableLobbiesMaxPlayers.clear();
        this.lobbyList.getItems().clear();

        this.challenger = false;
        this.homePane.setVisible(false);
        this.lobbyListPane.setVisible(true);
        this.lobbyList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        this.lobbyList.setEditable(false);
        this.lobbyListNext.setVisible(false);

        try {
            out.writeObject("?lobbies");
            out.flush();
            int numLobbies = (int)in.readObject();
            for (int i = 0; i < numLobbies; i++) {
                List<String> playersInLobby = (List<String>) in.readObject();
                int maxPlayers = (int) in.readObject();
                int lobbyNum = (int) in.readObject();
                this.availableLobbies.put(lobbyNum, playersInLobby);
                this.availableLobbiesMaxPlayers.put(lobbyNum, maxPlayers);
            }
            for (Integer i : availableLobbies.keySet()) {
                int maxP;
                maxP = availableLobbiesMaxPlayers.get(i);
                this.lobbyList.getItems().add(lobbyToString(maxP, availableLobbies.get(i), i+1));
                this.lobbyList.getSelectionModel().selectFirst();
                this.lobbyListNext.setVisible(true);
            }
        } catch (IOException | ClassNotFoundException e) {
            close();
        }
        this.backBtn.setVisible(true);
    }

    /**
     * Called when back button is clicked
     */
    @FXML
    public void backBtnClick(){
        this.backBtn.setVisible(false);
        this.challenger = false;
        this.homePane.setVisible(true);
        this.nicknamePane.setVisible(false);
        this.numPlayersPane.setVisible(false);
        this.lobbyListPane.setVisible(false);
    }

    /**
     * Called when next button in the lobby list pane is clicked
     */
    @FXML
    public void lobbyListNext(){
        if(availableLobbies.size() > 0) {

            int lobbyIndex = this.lobbyList.getSelectionModel().getSelectedIndex();
            if(availableLobbiesMaxPlayers.get(lobbyIndex) > availableLobbies.get(lobbyIndex).size()) {
                lobbyIndex++;
                try {
                    out.writeObject("lobbySelected");
                    out.flush();
                    out.writeObject(lobbyIndex);
                    out.flush();
                    String res = (String) in.readObject();
                    if (res.equals("lobbySelectedOK")) {
                        //go to nickname selection
                        this.challenger = false;
                        this.homePane.setVisible(false);
                        this.lobbyListPane.setVisible(false);
                        this.nicknamePane.setVisible(true);
                    }
                    //only the confirm (!challenger)
                    res = (String) in.readObject();

                } catch (IOException | ClassNotFoundException e) {
                    close();
                }
            }
        }
    }

    /**
     * Called when next button in nickname pane is clicked
     */
    @FXML
    public void nicknameChosen(){
        nickname = this.nicknameTextField.getText();
        if(!nickname.equals("")){
            this.nicknamePane.setVisible(false);

            //if challenger, ask for num players
            if(this.challenger){
                this.numPlayersPane.setVisible(true);
                return;
            }

            //if not challenger, join the game
            try {
                out.writeObject("nicknameSelected");
                out.flush();
                out.writeObject(nickname);
                out.flush();

                String res = (String)in.readObject();
                if(res.equals("ok")){
                    System.out.println("lobby joined from GUI");
                    showBoardWindow();
                }
                else{ //lobby is full, return to main menu
                    this.challenger = false;
                    this.homePane.setVisible(true);
                    this.nicknamePane.setVisible(false);
                }
            } catch (IOException | ClassNotFoundException e) {
                close();
            }
        } else {
            //TODO Show error (no empty nickname)
        }

    }

    /**
     * Called when next button in opponents number pane is clicked
     */
    @FXML
    public void opponentsChosen(){
        int num = -1;
        try{
            num = Integer.parseInt(numPlayersTextField.getText());
        }catch (Exception e){
            numPlayersError.setVisible(true);
        }
        if(num == 1 || num == 2){
            numPlayersError.setVisible(false);
            num++;
            numPlayers = num;

            //create the lobby
            numPlayersNextBtn.setVisible(false);
            try {
                String res;
                out.writeObject("lobbySelected");
                out.flush();
                out.writeObject(0);
                out.flush();
                res = (String)in.readObject(); //lobbySelectedOk
                assert res.equals("lobbySelectedOK");
                res = (String)in.readObject(); //challenger
                assert res.equals("challenger");
                out.writeObject("nicknameSelected");
                out.flush();
                out.writeObject(nickname);
                out.flush();
                out.writeObject("numPlayersSelected");
                out.flush();
                out.writeObject(numPlayers);
                out.flush();
                res = (String)in.readObject(); //ok
                //res = (String)in.readObject(); //ok
                if(res.equals("ok")){
                    System.out.println("lobby created from GUI");
                    showBoardWindow();
                }
            } catch (IOException | ClassNotFoundException e) {
                close();
            }

        }

    }

    private String lobbyToString(int maxNumPlayers, List<String> players, int number){
        String rep = number + " - ";
        for (String p: players){
            rep += "\"" + p + "\" ";
        }
        rep += "(" + players.size() + "/" + maxNumPlayers + ")";
        return rep;
    }

    /**
     * To close this window and show the board window
     */
    public void showBoardWindow() {
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.sizeToScene();
        stage.setResizable(false);
        stage.setTitle("Santorini Game");

        //set close event for board window: (close socket and streams, exit application)
        stage.setOnCloseRequest(windowEvent -> {
            ((Board)loader.getController()).close();
        });
        ((Board)loader.getController()).setParameters(new Connection(socket, out, in), challenger, nickname, ip);
        stage.show();

        ((Stage) homePane.getScene().getWindow()).close();
    }
}

package it.polimi.ingsw.client.gui;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.observer.Observer;
import it.polimi.ingsw.server.Connection;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Board implements Initializable {

    //used for reacting when a message arrives from the server
    private class MessageReceiver implements Observer<Object> {
        @Override
        public void update(Object message) {
            synchronized (receivedObject) {
                System.out.println("Received: " + message.toString());
                receivedObject = message;
                handleMessageReceived();
            }
        }
    }

    //used to store the graphics and the coordinates for the board cells
    private class Cell{
        private ImageView level, dome, worker;
        private boolean enabled;
        private Coord coord;
        public Cell(int x, int y){
            this.level = new ImageView();
            this.dome = new ImageView();
            this.worker = new ImageView();
            this.level.setX(0);
            this.level.setY(0);
            this.dome.setX(0);
            this.dome.setY(0);
            this.worker.setX(0);
            this.worker.setY(0);
            this.level.setFitWidth(cellDim);
            this.level.setFitHeight(cellDim);
            this.dome.setFitWidth(cellDim/1.30);
            this.dome.setFitHeight(cellDim/1.30);
            this.worker.setFitWidth(cellDim/1.50);
            this.worker.setFitHeight(cellDim/1.50);
            this.coord = new Coord(x, y);
            this.enabled = false;

            this.level.setOnMouseClicked(mouseEvent -> { if(this.enabled){ onCellClicked(this.coord); } } );
            this.worker.setOnMouseClicked(mouseEvent -> { if(this.enabled){ onCellClicked(this.coord); } } );
        }
        public void setPosition(int x, int y){
            this.level.setX(x);
            this.level.setY(y);
            this.dome.setX(x + domeOffset);
            this.dome.setY(y + domeOffset);
            this.worker.setX(x + domeOffset);
            this.worker.setY(y + domeOffset);
        }
        public void setLevelImage(Image img){
            this.level.setImage(img);
        }
        public void setDomeImage(){
            this.dome.setImage(domeImage);
        }
        public void setWorkerImage(Image worker){
            this.worker.setImage(worker);
        }
        public void resetImages(){
            this.level.setImage(null);
            this.dome.setImage(null);
            this.worker.setImage(null);
        }
        public void enable(){
            this.enabled = true;
            this.level.setCursor(Cursor.HAND);
            this.worker.setCursor(Cursor.HAND);
        }
        public void disable(){
            this.enabled = false;
            this.level.setCursor(Cursor.DEFAULT);
            this.worker.setCursor(Cursor.DEFAULT);
        }
        public ImageView getLevelImage(){
            return this.level;
        }
        public ImageView getDomeImage(){
            return this.dome;
        }
        public ImageView getWorkerImage(){
            return this.worker;
        }
    }

    //used to store the state of the gui
    private enum State{
        NONE, MOVE, BUILD, INITIALIZINGWORKERS, SELECTINGWORKER
    }

    //connection variables
    private Object receivedObject = new Object();
    private Connection serverConnection;
    private ExecutorService exec; //to listen for server messages on a separate thread
    private String ip;

    //game variables
    private boolean isChallenger;
    private String nickname;
    private Map<String, Color> playersColors;
    private Map<String, Image> playersGodsImages;
    private Map<String, God> playersGods;
    private Cell[][] board;
    private State state = State.NONE;
    private Map<Level, List<Coord>> buildableSpaces;

    private List<Coord> clickableCells;
    private boolean canMove, canBuild, canSkip;

    //resources and window components
    public AnchorPane boardWindow;
    public ImageView moveBtnOn, moveBtnOff, buildBtnOn, buildBtnOff, skipBtnOn, skipBtnOff, backBtn;
    public ImageView redBanner, blueBanner, yellowBanner, currentGod;
    public Label redLabel, blueLabel, yellowLabel;
    public TextArea messageBox;
    private Image lvl1Image, lvl2Image, lvl3Image, domeImage, emptyImage;
    private Map<Color, Image> workersTokens;
    private int cellStep = 105, intialX = 388, initialY = 112, cellDim = 90, domeOffset = 10;

    public Board(){
        //initialize board
        this.board = new Cell[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                this.board[i][j] = new Cell(i, j);
                this.board[i][j].setPosition(i*cellStep + intialX, j*cellStep + initialY);
            }
        }
        //initialize resources
        this.lvl1Image = new Image("Board/lvl_1.png");
        this.lvl2Image = new Image("Board/lvl_2.png");
        this.lvl3Image = new Image("Board/lvl_3.png");
        this.domeImage = new Image("Board/dome.png");
        this.emptyImage = new Image("Board/empty.png");
        this.workersTokens = new HashMap<>();
        this.workersTokens.put(Color.RED, new Image("Board/token_red.png"));
        this.workersTokens.put(Color.BLUE, new Image("Board/token_blue.png"));
        this.workersTokens.put(Color.YELLOW, new Image("Board/token_yellow.png"));

        this.playersColors = new HashMap<>();
        this.playersGodsImages = new HashMap<>();
        this.playersGods = new HashMap<>();

        this.clickableCells = new ArrayList<>();
    }

    /**
     * To pass connection and game parameters from the previous window (main menu)
     *
     * @param serverConnection the connection object used to communicate with the server
     * @param isChallenger flag to tell the gui if it is challenger or not
     * @param nickname the nickname chosen in the main menu
     * @param ip the ip of the server for further reconnection
     */
    public void setParameters(Connection serverConnection, boolean isChallenger, String nickname, String ip){
        this.serverConnection = serverConnection;
        this.serverConnection.addObserver(new MessageReceiver());
        this.ip = ip;

        this.isChallenger = isChallenger;
        this.nickname = nickname;

        exec = Executors.newFixedThreadPool(1);
        exec.submit(serverConnection);
        System.out.println("You have entered the lobby.");
    }

    /**
     * Initializes the board
     *
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //add each ImageView the matrix board as children of the main pane
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                this.board[i][j].setLevelImage(emptyImage); //to make cell clickable even if there is no image (for setup phase, placing workers)
                boardWindow.getChildren().add(board[i][j].getLevelImage());
                boardWindow.getChildren().add(board[i][j].getDomeImage());
                boardWindow.getChildren().add(board[i][j].getWorkerImage());
            }
        }
        this.blueBanner.setVisible(false);
        this.redBanner.setVisible(false);
        this.yellowBanner.setVisible(false);
    }

    /**
     * called when clicking on a cell (can be clicked only if the cell was enabled)
     * @param clickedCell the coordinates of the clicked cell
     */
    private void onCellClicked(Coord clickedCell){
        switch (this.state){
            case MOVE:
                //player has chose to move in clickedCell
                if(clickableCells.contains(clickedCell)){
                    List<Object> action = new ArrayList<>();
                    action.add("onMoveChosen");
                    action.add(clickedCell);
                    serverConnection.asyncSend(action);
                    this.state=State.NONE;
                    this.disableAll();
                }else{
                    this.showMessage("Invalid move.");
                }
                break;

            case BUILD:
                //player has chose to build in clickedCell
                if(clickableCells.contains(clickedCell)){
                    Level buildLevel;
                    List<Level> possibleLevels = new ArrayList<>();
                    for (Level key : buildableSpaces.keySet()){
                        List<Coord> list = buildableSpaces.get(key);
                        if (list.contains(clickedCell)) {
                            possibleLevels.add(key);
                        }
                    }
                    if(possibleLevels.size() > 1){
                        buildLevel = levelSelectionPopup(possibleLevels);
                    }else{
                        buildLevel = possibleLevels.get(0);
                    }
                    List<Object> action = new ArrayList<>();
                    action.add("onBuildChosen");
                    action.add(clickedCell);
                    action.add(buildLevel);
                    serverConnection.asyncSend(action);
                    this.state=State.NONE;
                    this.disableAll();
                }else{
                    this.showMessage("Invalid build.");
                }
                break;

            case INITIALIZINGWORKERS:
                //worker initialization
                if(clickableCells.contains(clickedCell)){
                    List<Object> action = new ArrayList<>();
                    action.add("onWorkerInitialization");
                    action.add(clickedCell);
                    serverConnection.asyncSend(action);
                    this.state=State.NONE;
                    this.disableAll();
                }else{
                    this.showMessage("Invalid selection.");
                }
                break;

            case SELECTINGWORKER:
                //player has chose where to place his worker
                if(clickableCells.contains(clickedCell)){
                    List<Object> action = new ArrayList<>();
                    action.add("onWorkerChosen");
                    action.add(clickedCell);
                    serverConnection.asyncSend(action);
                    this.state=State.NONE;
                    this.disableAll();
                }else{
                    this.showMessage("Invalid selection.");
                }
                break;

            default:
                break;
        }
    }

    //enable board, means the cell are clickable
    private void enableBoard(boolean enable){
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if(enable){
                    board[i][j].enable();
                }else{
                    board[i][j].disable();
                }
            }
        }
    }

    //used to update the graphics when the onBoardChanged event arrives
    private void updateBoard(it.polimi.ingsw.model.Board b){
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                //reset the board
                board[i][j].resetImages();
                //draw the board
                switch (b.getSpace(new Coord(i, j)).getHeight()){
                    case GROUND:
                        this.board[i][j].setLevelImage(this.emptyImage);
                        break;
                    case LVL1:
                        this.board[i][j].setLevelImage(this.lvl1Image);
                        break;
                    case LVL2:
                        this.board[i][j].setLevelImage(this.lvl2Image);
                        break;
                    case LVL3:
                        this.board[i][j].setLevelImage(this.lvl3Image);
                        break;
                }
                if(b.getSpace(new Coord(i, j)).isDome()){
                    this.board[i][j].setDomeImage();
                }
            }
        }
        //draw worker
        for(Worker w: b.getAllWorkersCopy()){
            if(w.getPosition() != null) {
                board[w.getPosition().x][w.getPosition().y].setWorkerImage(workersTokens.get(w.getColor()));
            }
        }
    }

    //used to show a message in the text area
    private void showMessage(String message){
        this.messageBox.appendText(message + "\n");
    }

    //enable move button
    private void enableMove(boolean enable){
        this.moveBtnOff.setVisible(!enable);
        this.moveBtnOn.setVisible(enable);
    }

    //enable build button
    private void enableBuild(boolean enable){
        this.buildBtnOff.setVisible(!enable);
        this.buildBtnOn.setVisible(enable);
    }

    //enable skip button
    private void enableSkip(boolean enable){
        this.skipBtnOff.setVisible(!enable);
        this.skipBtnOn.setVisible(enable);
    }

    //enable back button
    private void enableBack(boolean enable){
        this.backBtn.setVisible(enable);
    }

    //disable board and all buttons
    private void disableAll(){
        this.enableBoard(false);
        this.enableMove(false);
        this.enableBuild(false);
        this.enableSkip(false);
        this.enableBack(false);
    }

    /**
     * called when the move button is clicked
     */
    @FXML
    public void moveAction(){
        this.disableAll();
        this.enableBoard(true);
        this.state = State.MOVE;
        this.enableBack(true);
    }

    /**
     *called when the move button is clicked
     */
    @FXML
    public void buildAction(){
        this.disableAll();
        this.enableBoard(true);
        this.state = State.BUILD;
        this.enableBack(true);
    }

    /**
     *called when the move button is clicked
     */
    @FXML
    public void skipAction(){
        this.disableAll();
        this.state = State.NONE;
        //send a message to the server, player wants to skip
        List<Object> objects = new ArrayList<>();
        objects.add("skipAction");
        serverConnection.asyncSend(objects);
    }

    /**
     * called when the back button is clicked
     */
    @FXML
    public void goBack(){
        disableAll();
        this.state = State.NONE;
        if(canSkip)
            enableSkip(true);
        if(canMove)
            enableMove(true);
        if(canBuild)
            enableBuild(true);
    }

    //to add player banner (when game is ready)
    private void addPlayerBanner(String player, Color color){
        this.playersColors.put(player, color);
        switch (color){
            case BLUE:
                this.blueBanner.setVisible(true);
                this.blueLabel.setVisible(true);
                this.blueLabel.setText(player);
                break;
            case RED:
                this.redBanner.setVisible(true);
                this.redLabel.setVisible(true);
                this.redLabel.setText(player);
                break;
            case YELLOW:
                this.yellowBanner.setVisible(true);
                this.yellowLabel.setVisible(true);
                this.yellowLabel.setText(player);
                break;
        }
    }

    //to remove a player banner (when he looses)
    private void removePlayerBanner(String nickname){
        Color c = this.playersColors.get(nickname);
        switch (c){
            case BLUE:
                this.blueBanner.setVisible(false);
                this.blueLabel.setVisible(false);
                break;
            case RED:
                this.redBanner.setVisible(false);
                this.redLabel.setVisible(false);
                break;
            case YELLOW:
                this.yellowBanner.setVisible(false);
                this.yellowLabel.setVisible(false);
                break;
        }
    }

    //to highlight a player banner during it's turn
    private void highlightPlayerBanner(String nickname){
        Color c = this.playersColors.get(nickname);
        this.blueBanner.setScaleX(1);
        this.redBanner.setScaleX(1);
        this.yellowBanner.setScaleX(1);
        switch (c){
            case BLUE:
                this.blueBanner.setScaleX(1.5);
                break;
            case RED:
                this.redBanner.setScaleX(1.5);
                break;
            case YELLOW:
                this.yellowBanner.setScaleX(1.5);
                break;
        }
    }

    //to ask challenger to choose gods for the game
    private List<String> godsSelectionPopup(int numPlayers){

        final List<String>[] gods = new List[]{new ArrayList<>()};

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/GodsPopup.fxml"));
        Parent root = null;
        try {
            root = loader.load();
            Stage stage = new Stage();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setTitle("Choose the playable gods");
            stage.initOwner(boardWindow.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            ((GodsPopup) loader.getController()).setNumPlayers(numPlayers);
            stage.showAndWait();
            gods[0] = ((GodsPopup) loader.getController()).getChoices();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return gods[0];

    }

    //to ask a player for his god
    private String godSelectionPopup(List<String> availableGods){

        final String[] god = new String[1];

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/GodPopup.fxml"));
        Parent root = null;
        try {
            root = loader.load();
            Stage stage = new Stage();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setTitle("Choose the playable gods");
            stage.initOwner(boardWindow.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            ((GodPopup) loader.getController()).setGods(availableGods);
            stage.showAndWait();
            god[0] = ((GodPopup) loader.getController()).getChoice();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return god[0];
    }

    //to ask challenger for starting player
    private String startPlayerSelectionPopup(List<String> players){
        ChoiceDialog<String> dialog = new ChoiceDialog<>(players.get(0), players);
        dialog.setTitle("Choose Start Player");
        dialog.setHeaderText("Choose Start Player");

        dialog.initModality(Modality.WINDOW_MODAL);
        Optional<String> result = dialog.showAndWait();
        return result.orElse("");
    }

    //to ask which level to build in case of multiple options (atlas)
    private Level levelSelectionPopup(List<Level> levels){
        ChoiceDialog<Level> dialog = new ChoiceDialog<>(levels.get(0), levels);
        dialog.setTitle("Choose Level to Build");
        dialog.setHeaderText("Choose Level to Build");

        dialog.initModality(Modality.WINDOW_MODAL);
        Optional<Level> result = dialog.showAndWait();
        return result.orElse(levels.get(0));
    }

    private void messagePopup(String header, String body){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Santorini");
        alert.setHeaderText(header);
        alert.setContentText(body);
        alert.showAndWait();
        return;
    }

    //called when receiving a message from the server
    private void handleMessageReceived() {
        List<Object> objs;
        if (receivedObject instanceof List)
            objs = (List<Object>) receivedObject;
        else {
            System.out.println("Something went wrong in handling received message");
            return;
        }
        String event = (String) objs.get(0);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                synchronized (receivedObject) {
                    List<Object> objects;
                    switch (event) {
                        //MODEL MESSAGES
                        case "onBoardChanged":
                            //disableAll();
                            it.polimi.ingsw.model.Board b = (it.polimi.ingsw.model.Board) objs.get(1);
                            System.out.println(b.toString());
                            updateBoard(b);
                            break;

                        case "onGameReady":
                            disableAll();
                            System.out.println("Set up phase is done!");
                            showMessage("Set up phase is done!");
                            List<Player> players = (List<Player>) objs.get(1);
                            players.forEach(p -> addPlayerBanner(p.getNickname(), p.getWorkerColor()));
                            players.forEach(p -> playersGodsImages.put(p.getNickname(), new Image("GodCard/"+p.getGod().getName()+".png")));
                            players.forEach(p -> playersGods.put(p.getNickname(), p.getGod()));
                            break;

                        case "onGodsChosen":
                            disableAll();
                            List<String> selectedGods = (List<String>) objs.get(1);
                            System.out.println("Challenger has chosen the playable gods");
                            showMessage("Challenger has chosen the playable gods");
                            break;

                        case "onPlayerAdded":
                            disableAll();
                            String newPlayer = (String) objs.get(1);
                            int numCurr = (int) objs.get(2);
                            int numTot = (int) objs.get(3);
                            System.out.println(newPlayer + " has joined the game. Waiting for " + (numTot - numCurr) + " more player(s)");
                            showMessage(newPlayer + " has joined the game. Waiting for " + (numTot - numCurr) + " more player(s)");
                            break;

                        case "onGodSelection":
                            disableAll();
                            String currPlayer = (String) objs.get(1);
                            List<String> godsForSelection = (List<String>) objs.get(2);
                            if (currPlayer.equals(nickname)) {
                                String godSelected = "";
                                while (godSelected.equals("")) {
                                    godSelected = godSelectionPopup(godsForSelection);
                                }
                                showMessage("Selected god: " + godSelected);
                                objects = new ArrayList<>();
                                objects.add("onGodChosen");
                                objects.add(godSelected);
                                serverConnection.asyncSend(objects);
                            } else {
                                showMessage(currPlayer + " is choosing his god...");
                            }
                            break;

                        case "onGodsSelection":
                            disableAll();
                            if (isChallenger) {
                                List<String> allGods = (List<String>) objs.get(1);
                                int numPlayers = (int) objs.get(2);
                                List<String> godsSelected = new ArrayList<>();
                                while (godsSelected.size() < numPlayers) {
                                    godsSelected = godsSelectionPopup(numPlayers);
                                }
                                objects = new ArrayList<>();
                                objects.add("onGodsChosen");
                                objects.add(godsSelected);
                                serverConnection.asyncSend(objects);
                            } else {
                                System.out.println("The challenger is choosing the gods...");
                                showMessage("The challenger is choosing the gods...");
                            }
                            break;

                        case "onStartPlayerSelection":
                            disableAll();
                            if (isChallenger) {
                                List<String> playerss = (List<String>) objs.get(1);
                                String startPlayer = "";
                                while (startPlayer.equals("")) {
                                    startPlayer = startPlayerSelectionPopup(playerss);
                                }
                                objects = new ArrayList<>();
                                objects.add("onStartPlayerChosen");
                                objects.add(startPlayer);
                                serverConnection.asyncSend(objects);
                            } else {
                                System.out.println("Challenger is choosing the starting player...");
                                showMessage("Challenger is choosing the starting player...");
                            }
                            break;

                        case "onMyInitialization":
                            disableAll();
                            String currPlayerr = (String) objs.get(1);
                            List<Coord> freeSpaces = (List<Coord>) objs.get(2);
                            if (currPlayerr.equals(nickname)) {
                                showMessage("Your turn: place your workers.");
                                clickableCells.clear();
                                clickableCells = freeSpaces;
                                state = State.INITIALIZINGWORKERS;
                                enableBoard(true);
                                //a questo punto la board è abilitata e verrà selezionato lo spazio dove posizionare il worker
                            } else {
                                showMessage(currPlayerr + " is placing his workers...");
                            }
                            break;

                        case "onMyTurn":
                            disableAll();
                            String currPlayerrr = (String) objs.get(1);
                            List<Coord> selectableWorkers = (List<Coord>) objs.get(2);
                            highlightPlayerBanner(currPlayerrr);
                            currentGod.setImage(playersGodsImages.get(currPlayerrr));
                            currentGod.setOnMouseClicked(mouseEvent -> { messagePopup(playersGods.get(currPlayerrr).getName(), playersGods.get(currPlayerrr).getDescription());});
                            if (currPlayerrr.equals(nickname)) {
                                showMessage("Your turn: Select the worker for this turn");
                                clickableCells.clear();
                                clickableCells.addAll(selectableWorkers);
                                state = State.SELECTINGWORKER;
                                enableBoard(true);
                                //a questo punto la board è abilitata e verrà selezionato il worker per il turno
                            } else {
                                showMessage(currPlayerrr + "'s turn...");
                            }
                            break;

                        case "onMyAction":
                            disableAll();
                            canMove = false;
                            canBuild = false;
                            canSkip = false;
                            String currPlayerrrr = (String) objs.get(1);
                            //movable spaces
                            List<Coord> movableSpaces = (List<Coord>) objs.get(2);
                            buildableSpaces = (Map<Level, List<Coord>>) objs.get(3);
                            boolean canEndTurn = (boolean) objs.get(4);
                            if (currPlayerrrr.equals(nickname)) {
                                clickableCells.clear();
                                clickableCells.addAll(movableSpaces);
                                //buildable spaces
                                boolean thereAreBuilds = false;
                                for (Level l : buildableSpaces.keySet()) {
                                    //clickableCells.addAll(buildableSpaces.get(l));
                                    for (Coord c : buildableSpaces.get(l)) {
                                        if (!clickableCells.contains(c)) {
                                            clickableCells.add(c);
                                        }
                                    }
                                    if (buildableSpaces.get(l).size() > 0)
                                        thereAreBuilds = true;
                                }
                                if (canEndTurn) {
                                    enableSkip(true);
                                    canSkip = true;
                                }
                                if (movableSpaces.size() > 0) {
                                    enableMove(true);
                                    canMove = true;
                                }
                                if (thereAreBuilds) {
                                    enableBuild(true);
                                    canBuild = true;
                                }
                                state = State.NONE;
                                //a questo punto un button tra move e build verrà premuto, e la board verrà abilitata
                            }
                            break;

                        case "onEnd":
                            disableAll();
                            messagePopup("Game ended", "Game ended.");
                            System.out.println("Game Ended");

                            break;

                        case "onMessage":
                            disableAll();
                            String message = (String) ((List<Object>) receivedObject).get(1);
                            if (!message.equals(("disconnected"))) {
                                System.out.println(message);
                                showMessage(message);
                                messagePopup(message, message);
                            }
                            if (message.equals(("disconnected"))) {
                                disableAll();
                                // the game is no more valid, client must disconnect
                                System.out.println("A client disconnected from the game, disconnecting...");
                                showMessage("A client disconnected from the game, disconnecting...");

                                messagePopup("Game ended.", "Another player disconnected from the game.");

                                //Close socket, streams, return to main menu
                                close();
                            }
                            break;

                        default:
                            disableAll();
                            System.out.println("Event message not recognized.");
                            break;
                    }
                }
            }
        });
    }

    /**
     * To close the board window and return to the main menu,
     * connecting to the server as a new client
     */
    public void close(){
        this.exec.shutdown();
        this.serverConnection.closeConnection();
        //Close this windows and show main menu window
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/home.fxml"));
        Parent root = null;
        try {
            root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.sizeToScene();
            stage.setTitle("Santorini Game");
            ((Home)loader.getController()).connectToServer(ip);
            stage.show();
            ((Stage) boardWindow.getScene().getWindow()).close();
        } catch (IOException e) { e.printStackTrace(); }
    }

}

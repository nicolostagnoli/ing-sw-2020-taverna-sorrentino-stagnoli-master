package it.polimi.ingsw.model;

import com.google.gson.Gson;
import it.polimi.ingsw.exceptions.model.AlreadyExistingPlayerException;
import it.polimi.ingsw.exceptions.model.GameFullException;
import it.polimi.ingsw.exceptions.model.IllegalWorkerChoiceException;
import it.polimi.ingsw.listeners.EventSource;
import it.polimi.ingsw.listeners.Listener;
import it.polimi.ingsw.listeners.ModelEventListener;
import it.polimi.ingsw.model.handler.ActionType;
import it.polimi.ingsw.model.handler.EnhancedRequestHandlerCreator;
import it.polimi.ingsw.model.handler.RequestHandler;
import it.polimi.ingsw.model.handler.RequestHandlerCreator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class GameModel implements EventSource {

    private ModelState state;
    private int numPlayers;
    private final List<Player> queue;
    private List<God> godsList;
    private final List<Color> colors;
    private Board board;
    private Player currentPlayer;
    private Worker currentWorker;
    private Turn turn;
    private final Map<Player, RequestHandler> oldHandlers;
    private final Map<Player, RequestHandler> handlers;
    private final List<ModelEventListener> modelListeners;


    public GameModel() throws FileNotFoundException {
        this.state = new LobbyState(this);
        this.queue = new ArrayList<>();
        this.colors = new ArrayList<>();
        this.colors.add(Color.YELLOW);
        this.colors.add(Color.RED);
        this.colors.add(Color.BLUE);
        this.board = new Board();
        this.godsList = new ArrayList<>();
        this.currentPlayer = null;
        this.currentWorker = null;
        this.turn = new Turn();
        this.oldHandlers = new HashMap<>();
        this.handlers = new HashMap<>();
        this.modelListeners = new ArrayList<>();
        loadAvailableGods();

    }


    // INIT FUNCTIONS

    // Reads from file all available gods
    private void loadAvailableGods() throws FileNotFoundException {

        InputStream inputStream = this.getClass()
                .getClassLoader().getResourceAsStream("gods");
        if (inputStream == null) {
            throw new FileNotFoundException("\"gods\" file wasn't found.");
        }
        Scanner sc = new Scanner(inputStream);
        Gson gson = new Gson();
        while (sc.hasNext()) {
            godsList.add(gson.fromJson(sc.nextLine(), God.class));
        }
        sc.close();
        try {
            inputStream.close();
        } catch (IOException e) {
            System.out.println("Couldn't close the inputStream from \"gods\" file.");
            e.printStackTrace();
        }

        System.out.println("Gods loaded: ");
        godsList.forEach(g -> System.out.println(g.getName() + ": " + g.getDescription()));
        System.out.println("Total number of gods: " + godsList.size());
    }

    void initRequestHandlers() {
        queue.forEach(p ->
                oldHandlers.put(p,
                        new RequestHandlerCreator(p.getGod().getName())
                                .createHandler()
                )
        );

        queue.forEach(p ->
                handlers.put(p,
                        new EnhancedRequestHandlerCreator(p.getGod().getName())
                                .createHandler()
                )
        );
    }


    // STATE FUNCTIONS

    /**
     * To change the game state
     * @param state the new state
     */
    public void changeState(ModelState state) {
        this.state = state;
    }

    /**
     * To do the next step of the current state
     */
    public void nextStep() {
        state.nextStep();
    }


    // SETUP FUNCTIONS

    /**
     * To set the number of players for the game
     * @param numPlayers the number of players
     */
    public void setNumPlayers(int numPlayers) {
        this.numPlayers = numPlayers;
    }

    /**
     * To get the number of players for this game
     * @return the number of players for this game
     */
    public int getNumPlayers() {
        return numPlayers;
    }

    /**
     * To check if all players arrived
     * @return true if all players arrived
     */
    public boolean allPlayersArrived() {
        return this.queue.size() == this.numPlayers;
    }

    /**
     * To add a new player in the game
     * @param player the new player
     */
    public void addNewPlayer(Player player) {
        if (queue.size() == this.numPlayers) {
            throw new GameFullException("Game is full.");
        } else if (this.queue.stream().anyMatch(p -> p.getNickname().equals(player.getNickname()))) {
            throw new AlreadyExistingPlayerException("Player with nickname " +
                    player.getNickname() + " already exists.");
        }

        if (queue.size() == 0) {
            currentPlayer = player; // Set Challenger as currentPlayer
        }

        queue.add(player);
        this.board.addWorker(player.getWorker(0));
        this.board.addWorker(player.getWorker(1));

        Random r = new Random();
        int i = r.nextInt(colors.size());
        Color randomColor = colors.get(i);
        setPlayerColor(player, randomColor);

        modelListeners.forEach(l -> l.onPlayerAdded(player.getNickname(), queue.size(), numPlayers));
    }

    /**
     * To get all the available gods
     * @return a list of available gods
     */
    public List<God> getAvailableGods() {
        return new ArrayList<>(godsList);
    }

    /**
     * To get all the nicknames already in the game
     * @return a list of nicknames already in the game
     */
    public List<String> getPlayersNicknames() {
        return queue.stream()
                .map(Player::getNickname)
                .collect(Collectors.toList());
    }

    /**
     * To set the gods for the game
     * @param gods the list of gods
     */
    public void setGods(List<String> gods) {
        this.godsList =
                godsList.stream()
                        .filter(god -> gods.contains(god.getName()))
                        .collect(Collectors.toList());

        modelListeners.forEach(l -> l.onGodsChosen(gods));

        nextPlayer();
    }

    /**
     * to set a color to a player of the game
     * @param p the player
     * @param c the color
     * @throws IllegalArgumentException when player is not in the game or the color has already been assigned
     */
    public void setPlayerColor(Player p, Color c) throws IllegalArgumentException {
        //Check that player p is part of the game
        if (!this.queue.contains(p)) {
            throw new IllegalArgumentException("Given player is not part of the game.");
        }

        //If color has been taken by another player, throw exception
        if (!this.colors.contains(c))
            throw new IllegalArgumentException("Chosen color is not available any longer.");

        p.setWorkerColor(c);
        this.colors.remove(c);
    }

    /**
     * To assign a god to a player
     * @param p the player
     * @param g the gdd
     * @throws IllegalArgumentException when the player is not in the game or the god has already been assigned
     */
    public void assignGodToPlayer(Player p, God g) throws IllegalArgumentException {
        //Check that player p is part of the game
        if (!this.queue.contains(p)) {
            throw new IllegalArgumentException("Given player is not part of the game.");
        }

        //If god has been chosen by another player, throw exception
        if (!this.godsList.contains(g))
            throw new IllegalArgumentException("Chosen god has been previously chosen by another " +
                    "player or has never been selected by Challenger.");

        p.setGod(g);
        this.godsList.remove(g);
        if (!godsList.isEmpty()) { // If all players have chosen, keep the Challenger as currentPlayer
            nextPlayer();
        }
    }

    /**
     * To set a player as the starting player
     * @param startPlayer the player to set
     * @throws IllegalArgumentException when the player is not part of the game
     */
    public void setStartPlayer(Player startPlayer) throws IllegalArgumentException {

        if (!queue.contains(startPlayer))
            throw new IllegalArgumentException("Chosen player is not in the game.");

        boolean ordered = false;
        if (startPlayer.equals(queue.get(0))) ordered = true;
        while (!ordered) {
            Player moved = queue.remove(0);
            queue.add(moved);
            if (startPlayer.equals(queue.get(0))) ordered = true;
        }

        currentPlayer = startPlayer;
        currentPlayer.setAsStartPlayer();
    }

    /**
     * To initialize workers position of the current player on the board
     * @param c the coordinates to place the worker
     */
    public void initializeWorker(Coord c) {

        board.initializeWorker(currentPlayer, c);
        notifyBoardChanged();

        //If player has initialized both his workers...
        if (currentPlayer.getWorkersList().stream()
                .noneMatch(worker -> worker.getPosition() == null)
        ) {
            nextPlayer(); // ... go to next player.
        }
    }


    // GAME FUNCTIONS

    /**
     * To get a reference to a player by his nickname
     * @param nick the nickname
     * @return a reference to the Player object of the game with the given nickname
     * @throws IllegalArgumentException when the nickname is not in the game
     */
    public Player getPlayerByNickname(String nick) throws IllegalArgumentException {

        Player res = queue.stream()
                .filter(p -> p.getNickname().equals(nick)).findFirst().orElse(null);

        if (res != null) {
            return res;
        } else {
            throw new IllegalArgumentException("There is no player with the provided name.");
        }
    }

    /**
     * To get a reference to the current player
     * @return reference to the current player
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Get a copy of the list of players of the game
     * @return a copy of the list of players of the game
     */
    public List<Player> getPlayers() {
        List<Player> result = new ArrayList<>();
        queue.forEach(p -> result.add(p.clone()));
        return result;
    }

    /**
     * To get a copy of the board of the game
     * @return a copy of the board of the game
     */
    public Board getBoard() {
        return this.board.clone();
    }

    /**
     * To set the worker choice of the current player
     * @param workerPos the coordinates of the chosen worker
     */
    public void setWorkerChoice(Coord workerPos) {
        Worker selected = board.getWorkerByPosition(workerPos);

        // Worker must belong to currentPlayer
        if (!selected.getPlayerNickname().equals(currentPlayer.getNickname())) {
            throw new IllegalWorkerChoiceException("Selected worker does not belong to the current player");
        }

        // Worker must be selectable (= able to move)
        if (!turn.getSelectableWorkers().contains(workerPos)) {
            nextStep();
            return;
        }
        currentWorker = selected;
    }

    /**
     * To make the selected move for the selected worker
     * @param moveChoice the coordinates for the move
     */
    public void setMove(Coord moveChoice) {
        if (!turn.getMovableSpacesCopy().contains(moveChoice)) {
            notifyAction();
            return;
        }

        if (turn.getForcesCopy().containsKey(moveChoice)) {
            Coord forceDest = turn.getForcesCopy().get(moveChoice);
            board.workerForceMove(currentWorker, moveChoice, forceDest);
        } else {
            board.workerMove(currentWorker, moveChoice);
        }
        notifyBoardChanged();

        turn.hasMoved();

        //Check if this action caused the current player to win
        RequestHandler oldHandler = oldHandlers.get(currentPlayer);
        RequestHandler handler = handlers.get(currentPlayer);
        assert (oldHandler.checkForWin(moveChoice, ActionType.MOVE) ==
                handler.checkForWin(moveChoice, ActionType.MOVE));
        if (handler.checkForWin(moveChoice, ActionType.MOVE)) {
            state = new EndState(this);
            return;
        }

        oldHandler.generate(moveChoice, ActionType.MOVE);
        handler.generate(moveChoice, ActionType.MOVE);

    }

    /**
     * To make the selected build for the selected worker
     * @param buildChoice the coordinates for the build
     */
    public void setBuild(Coord buildChoice, Level level) {
        if (!turn.getBuildableSpacesCopy().get(level).contains(buildChoice)) {
            notifyAction();
            return;
        }
        board.workerBuild(currentWorker, buildChoice, level);
        notifyBoardChanged();

        turn.hasBuilt();

        //Check if this action caused the current player to win
        RequestHandler oldHandler = oldHandlers.get(currentPlayer);
        RequestHandler handler = handlers.get(currentPlayer);

        assert (oldHandler.checkForWin(buildChoice, ActionType.BUILD) ==
                handler.checkForWin(buildChoice, ActionType.BUILD));

        if (handler.checkForWin(buildChoice, ActionType.BUILD)) {
            state = new EndState(this);
            return;
        }

        oldHandler.generate(buildChoice, ActionType.BUILD);
        handler.generate(buildChoice, ActionType.BUILD);

    }

    /**
     * To end the turn of the current player
     */
    public void setEnd() {

        if (turn.hasEnded()) {
            turn.reset();
            oldHandlers.get(currentPlayer).reset();
            handlers.get(currentPlayer).reset();
            currentWorker = null;
            nextPlayer();
        } else {
            turn.setEnded();
            Coord currPosition = currentWorker.getPosition();

            //Check if this action caused the current player to win
            RequestHandler oldHandler = oldHandlers.get(currentPlayer);
            RequestHandler handler = handlers.get(currentPlayer);

            assert (oldHandler.checkForWin(currPosition, ActionType.END) ==
                    handler.checkForWin(currPosition, ActionType.END));

            if (handler.checkForWin(currPosition, ActionType.END)) {
                state = new EndState(this);
                return;
            }

            oldHandler.generate(currPosition, ActionType.END);
            handler.generate(currPosition, ActionType.END);
        }
    }

    /**
     * To set the next player in the queue as the current player, and to put the current at the end of the queue
     */
    public void nextPlayer() {
        //Check the game is ready
        if (allPlayersArrived()) {
            queue.remove(currentPlayer);
            queue.add(currentPlayer);
            currentPlayer = queue.get(0);
        } else {
            throw new IllegalStateException("Cannot go to next player because players' queue " +
                    "is not complete.");
        }
    }

    /**
     * Returns true when the startPlayer is playing
     * @return true when the startPlayer is playing
     */
    public boolean hasNewCycleBegun() {
        return currentPlayer.isStartPlayer();
    }

    /**
     * Returns true when a player has just begun his turn
     * @return true when a player has just begun his turn
     */
    public boolean hasNewTurnBegun() {
        return currentWorker == null;
    }

    void nextAction() {
        RequestHandler oldCurrHandler = oldHandlers.get(currentPlayer);
        RequestHandler currHandler = handlers.get(currentPlayer);
        Coord currentPosition = currentWorker.getPosition();
        turn.clear();
        currHandler.getValidSpaces(currentPosition, board.clone(),
                turn.getMovableSpacesReference(), turn.getBuildableSpacesReference(),
                turn.getForcesReference());

        /*START: New handler test*/
        if (!(currentPlayer.getGod().getName().equals("Hera") ||
                currentPlayer.getGod().getName().equals("Hestia") ||
                currentPlayer.getGod().getName().equals("Limus") ||
                currentPlayer.getGod().getName().equals("Triton") ||
                currentPlayer.getGod().getName().equals("Zeus")
        )) {
            List<Coord> movableSpaces = new ArrayList<>();
            Map<Level, List<Coord>> buildableSpaces = new HashMap<>();
            Map<Coord, Coord> forces = new HashMap<>();
            oldCurrHandler.getValidSpaces(currentPosition, board.clone(),
                    movableSpaces, buildableSpaces, forces);
            assert turn.getMovableSpacesReference().size() == movableSpaces.size();
            assert turn.getMovableSpacesReference().containsAll(movableSpaces);
            for (Level level : buildableSpaces.keySet()) {
                assert buildableSpaces.get(level).size() ==
                        turn.getBuildableSpacesReference().get(level).size();
                assert turn.getBuildableSpacesReference().get(level).containsAll(
                        buildableSpaces.get(level));
            }
            for (Coord coord : forces.keySet()) {
                assert turn.getForcesReference().containsKey(coord);
                assert turn.getForcesReference().get(coord).equals(forces.get(coord));
            }
        }
        /*END: New handler test*/

        if (turn.getMovableSpacesCopy().isEmpty() && turn.getBuildableSpacesCopy().values()
                .stream().flatMap(Collection::stream).count() == 0) {
            if (!turn.canEndTurn()) {
                removeCurrentPlayer(); // PLayer lost
            } else {
                setEnd();
                nextStep();
            }
            return;
        }

        notifyAction();
    }

    List<Coord> getSelectableWorkers() {
        RequestHandler oldCurrHandler = oldHandlers.get(currentPlayer);
        RequestHandler currHandler = handlers.get(currentPlayer);
        List<Coord> selectableWorkers = new ArrayList<>();

        for (Worker worker : currentPlayer.getWorkersList()) {
            Coord position = worker.getPosition();
            turn.clear();
            currHandler.getValidSpaces(position, board.clone(),
                    turn.getMovableSpacesReference(), turn.getBuildableSpacesReference(),
                    turn.getForcesReference());

            /*START: New handler test*/
            List<Coord> movableSpaces = new ArrayList<>();
            Map<Level, List<Coord>> buildableSpaces = new HashMap<>();
            Map<Coord, Coord> forces = new HashMap<>();
            oldCurrHandler.getValidSpaces(position, board.clone(),
                    movableSpaces, buildableSpaces, forces);
            assert turn.getMovableSpacesReference().size() == movableSpaces.size();
            assert turn.getMovableSpacesReference().containsAll(movableSpaces);
            for (Level level : buildableSpaces.keySet()) {
                assert buildableSpaces.get(level).size() ==
                        turn.getBuildableSpacesReference().get(level).size();
                assert turn.getBuildableSpacesReference().get(level).containsAll(
                        buildableSpaces.get(level));
            }
            for (Coord coord : forces.keySet()) {
                assert turn.getForcesReference().containsKey(coord);
                assert turn.getForcesReference().get(coord).equals(forces.get(coord));
            }
            /*END: New handler test*/

            if (!turn.getMovableSpacesCopy().isEmpty()) {
                selectableWorkers.add(worker.getPosition());
            }
        }
        turn.setSelectableWorkers(selectableWorkers);
        return selectableWorkers;
    }

    void removeCurrentPlayer() {
        Player loser = currentPlayer;
        notifyMessage(loser.getNickname() + " lost.");
        nextPlayer();

        board.remove(loser);
        queue.remove(loser);
        oldHandlers.remove(loser);
        handlers.remove(loser);
        notifyBoardChanged();

        if (numPlayers == 2) {
            modelListeners.forEach(l -> l.onMessage(currentPlayer.getNickname() + " won!"));
            modelListeners.forEach(ModelEventListener::onEnd);
            return;
        }
        numPlayers = numPlayers - 1;
        currentWorker = null;
        nextStep(); // Recently added to avoid blocking on player's defeat
        //Following line is commented because the loser may still want to see the game
        //modelListeners.remove(getListenerByNickname(currentPlayer.getNickname()));
    }


    // LISTENERS FUNCTIONS

    private void notifyAction() {
        modelListeners.forEach(listener -> listener.onMyAction(currentPlayer.getNickname(), turn.getMovableSpacesCopy(), turn.getBuildableSpacesCopy(),
                turn.canEndTurn()));
    }

    private void notifyBoardChanged() {
        modelListeners.forEach(l -> l.onBoardChanged(board.clone()));
    }

    private void notifyMessage(String message) {
        modelListeners.forEach(l -> l.onMessage(message));
    }

    /**
     * To add a listener to this game model
     * @param listener the listener to add
     */
    @Override
    public void addListener(Listener listener) {
        if (!(listener instanceof ModelEventListener)) {
            throw new IllegalArgumentException("Tried to register a non-ModelEventListener to Model");
        }
        modelListeners.add((ModelEventListener) listener);
    }

    List<ModelEventListener> getAllListeners() {
        return new ArrayList<>(modelListeners);
    }

}
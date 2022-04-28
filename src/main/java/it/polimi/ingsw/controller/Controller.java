package it.polimi.ingsw.controller;

import it.polimi.ingsw.listeners.ChallengerViewEventListener;
import it.polimi.ingsw.listeners.EventSource;
import it.polimi.ingsw.listeners.PlayerViewEventListener;
import it.polimi.ingsw.model.Coord;
import it.polimi.ingsw.model.GameModel;
import it.polimi.ingsw.model.Level;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.view.RemotePlayerView;

import java.util.List;

public class Controller implements PlayerViewEventListener, ChallengerViewEventListener {
    private final GameModel model;
    private final Setup setup;

    public Controller(GameModel model) {
        this.model = model;
        setup = new Setup(this.model);
    }

    /**
     * To make the controller to set the worker choice
     * @param source the view firing the event
     * @param workerPos the coordinates of the chosen worker
     */
    @Override
    public synchronized void onWorkerChosen(EventSource source, Coord workerPos) {
        String nickname = ((RemotePlayerView) source).getNickname();

        if (!isCurrentPlayer(nickname)) {
            throw new IllegalStateException("Player " + nickname + " tried to choose a " +
                    "worker not in his turn.");
        }

        model.setWorkerChoice(workerPos);
        model.nextStep();
    }

    /**
     * To make the controller to set the worker move
     * @param source the view firing the event
     * @param moveChoice the coordinates to move the worker
     */
    @Override
    public synchronized void onMoveChosen(EventSource source, Coord moveChoice) {
        String nickname = ((RemotePlayerView) source).getNickname();

        if (!isCurrentPlayer(nickname)) {
            throw new IllegalStateException("Player " + nickname + "tried to move not in his turn.");
        }

        model.setMove(moveChoice);
        model.nextStep();
    }

    /**
     * To make the controller to set the worker build
     * @param source the view firing the event
     * @param buildChoice the coordinates to move the worker
     * @param level the level to build
     */
    @Override
    public synchronized void onBuildChosen(EventSource source, Coord buildChoice, Level level) {
        String nickname = ((RemotePlayerView) source).getNickname();

        if (!isCurrentPlayer(nickname)) {
            throw new IllegalStateException("Player " + nickname + "tried to build not in his turn.");
        }

        model.setBuild(buildChoice, level);
        model.nextStep();
    }

    private /*helper*/ boolean isCurrentPlayer(String nickname) {
        Player currentPlayer = model.getCurrentPlayer();
        return currentPlayer.getNickname().equals(nickname);
    }

    /**
     * To make the controller to skip the action
     * @param source source the view firing the event
     */
    public void skipAction(EventSource source) {
        String nickname = ((RemotePlayerView) source).getNickname();

        if (!isCurrentPlayer(nickname)) {
            throw new IllegalStateException("Player " + nickname + "tried to skip action not in his turn.");
        }

        model.setEnd();
        model.nextStep();
    }



    // --------------------------------------------------------------------------------
    /*
    SETUP SECTION
        Here event handling is delegated to Setup class.
    */

    /**
     * To make the controller to add a new player
     * @param source the view firing the event
     * @param nickname the nickname for the new player
     */
    @Override
    public synchronized void onNicknameChosen(EventSource source, String nickname) {
        setup.onNicknameChosen(source, nickname);
    }

    /**
     * To make the controller to set the number of players
     * @param source the view firing the event
     * @param num the number of players
     */
    @Override
    public synchronized void onNumberOfPlayersChosen(EventSource source, int num) {
        setup.onNumberOfPlayersChosen(source, num);
    }

    /**
     * To make the controller to set the gods for the game
     * @param source the view firing the event
     * @param gods the list of gods
     */
    @Override
    public synchronized void onGodsChosen(EventSource source, List<String> gods) {
        setup.onGodsChosen(source, gods);
    }

    /**
     * To make the controller to assign a god for the current player
     * @param source the view firing the event
     * @param god the god to assign
     */
    @Override
    public synchronized void onGodChosen(EventSource source, String god) {
        setup.onGodChosen(source, god);
    }

    /**
     * To make the controller to set the starting player
     * @param source the view firing the event
     * @param startPlayerNickname
     */
    @Override
    public synchronized void onStartPlayerChosen(EventSource source, String startPlayerNickname) {
        setup.onStartPlayerChosen(source, startPlayerNickname);
    }

    /**
     * To make the controller to initialize the workers position for the current player
     * @param source the view firing the event
     * @param choice the coordinates to place the worker
     */
    @Override
    public synchronized void onWorkerInitialization(EventSource source, Coord choice) {
        setup.onWorkerInitialization(source, choice);
    }

}

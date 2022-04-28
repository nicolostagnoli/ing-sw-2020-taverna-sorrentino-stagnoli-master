package it.polimi.ingsw.listeners;

import it.polimi.ingsw.model.*;

import java.util.List;
import java.util.Map;

public interface ModelEventListener extends Listener {

    public void onBoardChanged(Board board);
    public void onGameReady(List<Player> players);
    public void onGodsChosen(List<String> gods);
    public void onPlayerAdded(String nickname, int numCurr, int numTot);
    public void onMessage(String message);

    public void onGodSelection(String nickname, List<String> gods);
    public void onGodsSelection(List<String> gods, int numPlayers);
    public void onStartPlayerSelection(List<String> players);
    public void onMyInitialization(String nickname, List<Coord> freeSpaces);

    public void onMyTurn(String nickname, List<Coord> selectableWorkers);
    public void onMyAction(String nickname, List<Coord> movableSpaces,
                           Map<Level, List<Coord>> buildableSpaces, boolean canEndTurn);
    public void onEnd();

    public String getNickname();
}

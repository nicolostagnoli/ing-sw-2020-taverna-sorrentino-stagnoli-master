package it.polimi.ingsw.listeners;

import java.util.List;

public interface ChallengerViewEventListener {
    public void onGodsChosen(EventSource source, List<String> gods);
    public void onStartPlayerChosen(EventSource source, String startPlayerNickname);
    public void onNumberOfPlayersChosen(EventSource source, int num);
}

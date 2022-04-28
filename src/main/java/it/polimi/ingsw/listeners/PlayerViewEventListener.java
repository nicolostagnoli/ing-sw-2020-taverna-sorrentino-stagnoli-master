package it.polimi.ingsw.listeners;


import it.polimi.ingsw.exceptions.model.IllegalWorkerChoiceException;
import it.polimi.ingsw.exceptions.model.WorkerNotFoundException;
import it.polimi.ingsw.model.Coord;
import it.polimi.ingsw.model.Level;


public interface PlayerViewEventListener extends Listener {

    public void onNicknameChosen(EventSource source, String nickname);
    public void onGodChosen(EventSource source, String god);
    public void onWorkerInitialization(EventSource source, Coord choice) throws WorkerNotFoundException, IllegalWorkerChoiceException;
    public void onWorkerChosen(EventSource source, Coord workerPos) throws IllegalWorkerChoiceException, WorkerNotFoundException;
    public void onMoveChosen(EventSource source, Coord moveChoice);
    public void onBuildChosen(EventSource source, Coord buildChoice, Level level);
    public void skipAction(EventSource source);
}

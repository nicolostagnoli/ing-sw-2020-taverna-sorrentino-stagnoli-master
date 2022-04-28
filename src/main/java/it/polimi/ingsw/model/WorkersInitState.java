package it.polimi.ingsw.model;

import java.util.List;

public class WorkersInitState extends ModelState {

    private boolean startPlayerChosen = false;
    public WorkersInitState(GameModel model) {
        super(model);
    }

    @Override
    public void nextStep() {
        if (!startPlayerChosen) {
            startPlayerChosen = true;

            model.getAllListeners().forEach(l -> l.onStartPlayerSelection(model.getPlayersNicknames()));
            return;
        }

        List<Coord> freeSpaces = model.getBoard().getUnoccupiedPositions();
        String currPlayer = model.getCurrentPlayer().getNickname();
        model.getAllListeners().forEach(l -> l.onMyInitialization(currPlayer, freeSpaces));
    }
}

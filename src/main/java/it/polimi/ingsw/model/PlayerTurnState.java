package it.polimi.ingsw.model;

import java.util.List;

public class PlayerTurnState extends ModelState {

    public PlayerTurnState(GameModel model) {
        super(model);
    }

    @Override
    public void nextStep() {
        if (model.hasNewTurnBegun()) {
            Player currPlayer = model.getCurrentPlayer();
            String nickname = currPlayer.getNickname();
            List<Coord> selectableWorkers = model.getSelectableWorkers();

            // Caught by tests
            if (model.getAllListeners().size() == 0) {
                throw new RuntimeException("No listeners found.");
            }

            if (!selectableWorkers.isEmpty()) {
                model.getAllListeners().forEach(l -> l.onMyTurn(nickname, selectableWorkers));
            } else { // If player cannot select any worker...
                model.removeCurrentPlayer(); //... he lost
            }
        } else {
            model.nextAction();
        }
    }
}

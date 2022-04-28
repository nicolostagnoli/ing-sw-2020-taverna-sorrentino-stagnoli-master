package it.polimi.ingsw.model;

import it.polimi.ingsw.listeners.ModelEventListener;

public class EndState extends ModelState {

    public EndState(GameModel model) {
        super(model);
    }

    @Override
    public void nextStep() {
        String winner = model.getCurrentPlayer().getNickname();
        model.getAllListeners().forEach(l -> l.onMessage(winner + " has won!\nThe game is finished.\n\nThanks for playing!"));
        model.getAllListeners().forEach(ModelEventListener::onEnd);
    }
}

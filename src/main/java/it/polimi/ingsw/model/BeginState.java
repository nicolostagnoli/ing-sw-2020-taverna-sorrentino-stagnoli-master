package it.polimi.ingsw.model;

public class BeginState extends ModelState {

    public BeginState(GameModel model) {
        super(model);
    }

    @Override
    public void nextStep() {
        model.initRequestHandlers();
        model.getAllListeners().forEach(l -> l.onGameReady(model.getPlayers()));
        model.changeState(new PlayerTurnState(model));
        model.nextStep();
    }
}

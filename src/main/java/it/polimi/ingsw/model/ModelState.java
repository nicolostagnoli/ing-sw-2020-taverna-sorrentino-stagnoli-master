package it.polimi.ingsw.model;

public abstract class ModelState {
    GameModel model;

    public ModelState(GameModel model) {
        this.model = model;
    }

    public abstract void nextStep();
}

package it.polimi.ingsw.model.handler;

import it.polimi.ingsw.model.Board;
import it.polimi.ingsw.model.Coord;
import it.polimi.ingsw.model.Level;

import java.util.List;
import java.util.Map;

class HandlerAdapter implements RequestHandler {

    private final RuleHandler ruleHandler;
    private ValidationContainer validationContainer;

    HandlerAdapter(RuleHandler ruleHandler) {
        this.ruleHandler = ruleHandler;
    }

    @Override
    public void getValidSpaces(Coord current, Board board,
                               List<Coord> movableSpaces, Map<Level, List<Coord>> buildableSpaces,
                               Map<Coord, Coord> forces) {

        validationContainer = new ValidationContainer(current, board);

        ruleHandler.handleValidationRequest(validationContainer);


        movableSpaces.addAll(validationContainer.getMovableSpaces());
        buildableSpaces.putAll(validationContainer.getBuildableSpaces());
        forces.putAll(validationContainer.getForces());

    }

    @Override
    public void generate(Coord after, ActionType at) {
        ruleHandler.generate(validationContainer, after, at);
    }

    @Override
    public boolean checkForWin(Coord after, ActionType at) {
        return ruleHandler.handleWinCheckRequest(validationContainer, after, at);
    }

    @Override
    public void reset() {
        ruleHandler.reset();
    }
}

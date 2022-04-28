package it.polimi.ingsw.model.handler;

import it.polimi.ingsw.model.Coord;

interface RuleHandler {
    void handleValidationRequest(ValidationContainer vc);
    void generate(ValidationContainer vc, Coord after, ActionType at);
    boolean handleWinCheckRequest(ValidationContainer vc, Coord after, ActionType at);
    void reset();
}

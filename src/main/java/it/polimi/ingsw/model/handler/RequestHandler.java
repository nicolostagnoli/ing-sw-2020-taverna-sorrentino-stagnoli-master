package it.polimi.ingsw.model.handler;

import it.polimi.ingsw.model.Board;
import it.polimi.ingsw.model.Coord;
import it.polimi.ingsw.model.Level;

import java.util.List;
import java.util.Map;

/**
 * Handles requests about the possibilities of a player during his turn, based on the rules of the game.
 * This is the only interface the user can have on the whole handling of the game rules, thus allowing
 * a strong decoupling between the "game logic" and the "rules logic".
 */
public interface RequestHandler {

    /**
     * Populates the collections passed by the caller with data about what actions can be
     * performed and what actions can't
     * @param current the position of the current worker, i.e. the worker that will perform the action
     * @param board the board to evaluate for all possible actions
     * @param movableSpaces the list in which the coordinates reachable by a move will be put
     * @param buildableSpaces the map in which the coordinates where a build (for each level)
     *                        can be performed will be put
     * @param forces the map in which the "force" destination will be put, for each space that
     *               requires a forcing in order to move there.
     */
    void getValidSpaces(Coord current, Board board,
                               List<Coord> movableSpaces, Map<Level, List<Coord>> buildableSpaces,
                               Map<Coord, Coord> forces);

    /**
     * Updates the rules of the handler with the rules to be used for the next action.
     * @param after the target position of the last performed action
     * @param at the {@code ActionType} of the last performed action
     */
    void generate(Coord after, ActionType at);

    /**
     * Returns true when the last performed action caused the player to win
     * @param after the target position of the last performed action
     * @param at the {@code ActionType} of the last performed action
     * @return true when the last performed action caused the player to win
     */
    boolean checkForWin(Coord after, ActionType at);

    /**
     * Resets the rules of this handler to its initial rules.
     * When ending a turn (i.e. a sequence of actions) the initial rules must be
     * restored, in order to be used for the next turn.
     * Thus this method has to be called when the corresponding player's turn ends.
     */
    void reset();

}

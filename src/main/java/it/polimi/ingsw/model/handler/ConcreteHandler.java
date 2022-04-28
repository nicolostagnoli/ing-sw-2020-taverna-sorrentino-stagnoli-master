package it.polimi.ingsw.model.handler;

import it.polimi.ingsw.exceptions.model.handler.UndeterminedSpaceException;
import it.polimi.ingsw.model.Board;
import it.polimi.ingsw.model.Coord;
import it.polimi.ingsw.model.handler.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles requests about the possibilities of a player during his turn, based on the rules of the game.
 * This class is at the heart of the application logic. It determines what actions a player can
 * do, whether he has won or not, and updates the current rules used in this turn with the rules
 * to be used in the next turn.
 */
class ConcreteHandler implements RuleHandler {

    private static final List<ConcreteHandler> handlers = new ArrayList<>();
    private List<Rule> rules;
    private final List<Rule> initialRules;


    public ConcreteHandler(List<Rule> rules) {
        this.rules = new ArrayList<>(rules);
        this.initialRules = rules;
        handlers.add(this);
    }


    //---------------------------------------------------------------------------------------

    /**
     * This methods determines, for each coordinate, what actions can be performed and what actions can't.
     * It takes all the {@code VALIDATION} rules and, for each {@code Rule r}:
     * <ul>
     *     <li>Checks if the rule applies to a coordinate
     *     <li>If so, marks the coordinate as valid for the action {@code r.getActionType}
     *          or invalid (it depends on {@code r.getDecision})
     *     <li>Repeats for next coordinates, until all coordinates have been examined
     *     </li>
     * </ul>
     * @param vc the container of the data this method will populate
     * @throws UndeterminedSpaceException when the rules aren't enough to determine the validity
     * of all the coordinates/spaces
     */
    @Override
    public void handleValidationRequest(ValidationContainer vc) throws UndeterminedSpaceException {

        //Do your work

        Coord current = vc.getCurrentPosition();
        Board board = vc.getBoard();

        rules.stream()
                .filter(r -> r.getPurpose() == Purpose.VALIDATION)
                .forEach(r ->
                        vc.getAllSpaces().stream()
                        .filter(s -> r.getCondition().test(new Pair<>(current, s), board))
                        .forEach(s -> vc.validateSpace(
                                s,
                                r.getActionType(),
                                r.getDecision(),
                                r.getForceSpaceFunction().apply(current, s),
                                r.getBuildLevel())
                        )
                );


        //Check if validation is completed
        if (!vc.allSpacesValidated()) {
            String exceptionalMessage = "Cannot determine validity of the followings:\n";
            exceptionalMessage += vc.getNotValidatedSpaces().toString();

            throw new UndeterminedSpaceException(exceptionalMessage);
        }

    }

    @Override
    public void generate(ValidationContainer vc, Coord after, ActionType at) {
        Pair<Coord> pair = new Pair<>(vc.getCurrentPosition(), after);
        Board board = vc.getBoard();


        List<Rule> rulesOnOpponents = rules.stream()
                .filter(r -> r.getPurpose()==Purpose.GENERATION)
                .filter(r -> r.getTarget() == Target.OPPONENTS)
                .filter(r -> r.getActionType() == at)
                .filter(r -> r.getCondition().test(pair, board))
                .map(r -> r.getGeneratedRules(pair))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        handlers.stream()
                .filter(h -> h!=this)
                .forEach(h ->
                        h.rules.addAll(0, rulesOnOpponents)
                );

        rules = rules.stream()
                .filter(r -> r.getPurpose() == Purpose.GENERATION)
                .filter(r -> r.getTarget() == Target.MYSELF)
                .filter(r -> r.getActionType() == at)
                .filter(r -> r.getCondition().test(pair, board))
                .map(r -> r.getGeneratedRules(pair))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

    }

    @Override
    public boolean handleWinCheckRequest(ValidationContainer vc, Coord after, ActionType at) {
        Pair<Coord> pair = new Pair<>(vc.getCurrentPosition(), after);
        Board board = vc.getBoard();

        Rule winRule = rules.stream()
                .filter(r -> r.getPurpose() == Purpose.WIN)
                .filter(r -> r.getActionType() == at)
                .filter(r -> r.getCondition().test(pair, board))
                .findFirst().orElse(null);
        return winRule != null && winRule.getDecision() == Decision.GRANT;
    }

    @Override
    public void reset() {
        rules = new ArrayList<>(initialRules);
    }
}

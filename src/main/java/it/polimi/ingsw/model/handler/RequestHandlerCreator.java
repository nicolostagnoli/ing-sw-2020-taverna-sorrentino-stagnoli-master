package it.polimi.ingsw.model.handler;

import it.polimi.ingsw.model.Board;
import it.polimi.ingsw.model.Coord;
import it.polimi.ingsw.model.Level;
import it.polimi.ingsw.model.handler.util.Pair;
import it.polimi.ingsw.model.handler.util.TriPredicate;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class RequestHandlerCreator {

    private final String god;
    private static final List<Rule> standardRules = new ArrayList<>();
    private static final Map<String, List<Rule>> godRules = new HashMap<>();
    private static final List<Rule> standardBuildUpRules = new ArrayList<>();

    static {
        initStandardBuildUpRules();
        initStandardRules();
    }

    public RequestHandlerCreator() {
        this.god = null;
    }
    public RequestHandlerCreator(String god) {
        this.god = god;
    }


    public RequestHandler createHandler() {
        List<Rule> rules = createRulesList();
        RuleHandler ruleHandler = new ConcreteHandler(rules);
        RequestHandler requestHandler = new HandlerAdapter(ruleHandler);

        return requestHandler;
    }

    private List<Rule> createRulesList() {
        List<Rule> result = new ArrayList<>();
        if (god != null) {
            result.addAll(getGodRules());
        }
        result.addAll(standardRules);

        return result;
    }

    private List<Rule> getGodRules() {

        if (!godRules.containsKey(god)) {
            initGodRules(god);
        }

        return new ArrayList<>(godRules.get(god));
    }



    // Coord before --> cPair.get(0)
    // Coord after --> cPair.get(1)

    private static void initStandardRules() {

        //--------------------------STANDARD MOVE--------------------------
        Rule r = new Rule();
        r.setPurpose(Purpose.VALIDATION);
        r.setDecision(Decision.GRANT);
        r.setActionType(ActionType.MOVE);
        BiPredicate<Pair<Coord>, Board> condition = (cPair, board) ->
        cPair.get(0).isNear(cPair.get(1)) && !board.getSpace(cPair.get(1)).isOccupied() &&
                !board.getSpace(cPair.get(1)).isDome() &&
                board.getSpace(cPair.get(1)).getHeight().ordinal() -
                        board.getSpace(cPair.get(0)).getHeight().ordinal() <= 1;
        r.setCondition(condition);
        standardRules.add(r);

        r = new Rule();
        r.setPurpose(Purpose.VALIDATION);
        r.setDecision(Decision.DENY);
        r.setActionType(ActionType.MOVE);
        condition = (cPair, board) ->
                !cPair.get(0).isNear(cPair.get(1)) || board.getSpace(cPair.get(1)).isOccupied() ||
                        board.getSpace(cPair.get(1)).isDome() ||
                        board.getSpace(cPair.get(1)).getHeight().ordinal() -
                                board.getSpace(cPair.get(0)).getHeight().ordinal() > 1;
        r.setCondition(condition);
        standardRules.add(r);

        //-----------------DENY BUILD on every level------------------
        r = new Rule();
        r.setPurpose(Purpose.VALIDATION);
        r.setDecision(Decision.DENY);
        r.setActionType(ActionType.BUILD);
        r.setBuildLevel(Level.GROUND);
        condition = (cPair, board) -> true;
        r.setCondition(condition);
        standardRules.add(r);

        r = new Rule();
        r.setPurpose(Purpose.VALIDATION);
        r.setDecision(Decision.DENY);
        r.setActionType(ActionType.BUILD);
        r.setBuildLevel(Level.LVL1);
        condition = (cPair, board) -> true;
        r.setCondition(condition);
        standardRules.add(r);

        r = new Rule();
        r.setPurpose(Purpose.VALIDATION);
        r.setDecision(Decision.DENY);
        r.setActionType(ActionType.BUILD);
        r.setBuildLevel(Level.LVL2);
        condition = (cPair, board) -> true;
        r.setCondition(condition);
        standardRules.add(r);

        r = new Rule();
        r.setPurpose(Purpose.VALIDATION);
        r.setDecision(Decision.DENY);
        r.setActionType(ActionType.BUILD);
        r.setBuildLevel(Level.LVL3);
        condition = (cPair, board) -> true;
        r.setCondition(condition);
        standardRules.add(r);

        r = new Rule();
        r.setPurpose(Purpose.VALIDATION);
        r.setDecision(Decision.DENY);
        r.setActionType(ActionType.BUILD);
        r.setBuildLevel(Level.DOME);
        condition = (cPair, board) -> true;
        r.setCondition(condition);
        standardRules.add(r);

        //-------------------------WIN RULES---------------------------
        standardRules.addAll(getStandardWinRules());

        //----------------------BUILD GENERATION-----------------------
        r = new Rule();
        r.setPurpose(Purpose.GENERATION);
        r.setTarget(Target.MYSELF);
        r.setActionType(ActionType.MOVE);
        condition = (cPair, board) -> true;
        r.setCondition(condition);
        standardRules.add(r);

        List<Rule> generatedRules = new ArrayList<>();
        r.setGeneratedRules(generatedRules);

        if (standardBuildUpRules.size() == 0) {
            throw new IllegalStateException("Trying to initialize standard rules " +
                    "without initializing standard build-up rules before.");
        }
        generatedRules.addAll(standardBuildUpRules);

        r = new Rule();
        r.setPurpose(Purpose.VALIDATION);
        r.setDecision(Decision.DENY);
        r.setActionType(ActionType.MOVE);
        condition = (cPair, board) -> true;
        r.setCondition(condition);
        generatedRules.add(r);

        //-----------------------END GENERATION-----------------------
        r = new Rule();
        r.setPurpose(Purpose.GENERATION);
        r.setTarget(Target.MYSELF);
        r.setActionType(ActionType.BUILD);
        condition = (cPair, board) -> true;
        r.setCondition(condition);
        generatedRules.add(r);

        generatedRules = new ArrayList<>();
        r.setGeneratedRules(generatedRules);

        generatedRules.addAll(denyAll());
        generatedRules.add(doNothingOnEnd());

    }

    private static void initGodRules(String god) {
        List<Rule> result = new ArrayList<>();

        if (god == null) {
            throw new IllegalStateException("Tried to get god's rules without specifying god's name " +
                    "at construction-time.");
        }

        if (god.equals("Apollo")) { //COMPLETE&TESTED
            Rule r = new Rule();
            r.setPurpose(Purpose.VALIDATION);
            r.setActionType(ActionType.MOVE);
            r.setDecision(Decision.GRANT);
            BiPredicate<Pair<Coord>, Board> condition = (cPair, board) ->
                    board.getSpace(cPair.get(1)).isOccupied() &&
                            cPair.get(0).isNear(cPair.get(1)) &&
                            !board.getWorkerCopy(cPair.get(1)).getPlayerNickname().equals(
                                    board.getWorkerCopy(cPair.get(0)).getPlayerNickname()
                            );
            r.setCondition(condition);
            BiFunction<Coord, Coord, Coord> forceSpaceFunction = (before, after) ->
                    before;
            r.setForceSpaceFunction(forceSpaceFunction);
            result.add(r);
        }

        if (god.equals("Artemis")) { //COMPLETE&TESTED
            Rule r = new Rule();
            r.setPurpose(Purpose.GENERATION);
            r.setTarget(Target.MYSELF);
            r.setActionType(ActionType.MOVE);
            BiPredicate<Pair<Coord>, Board> condition = (cPair, board) -> true;
            r.setCondition(condition);
            result.add(r);

            List<Rule> generatedRules = new ArrayList<>();
            r.setGeneratedRules(generatedRules);

            r = new Rule();
            r.setPurpose(Purpose.VALIDATION);
            r.setDecision(Decision.DENY);
            r.setActionType(ActionType.MOVE);
            TriPredicate<Pair<Coord>, Pair<Coord>, Board> symbolicCondition = (oldPair, pair, board) ->
                    oldPair.get(0).equals(pair.get(1));
            r.setSymbolicCondition(symbolicCondition);
            generatedRules.add(r);

            generatedRules.add(standardRules.get(0));
            generatedRules.add(standardRules.get(standardRules.size() - 1));
            generatedRules.addAll(getStandardWinRules());
        }

        if (god.equals("Athena")) { //COMPLETE&TESTED
            Rule r = new Rule();
            r.setPurpose(Purpose.GENERATION);
            r.setActionType(ActionType.MOVE);
            r.setTarget(Target.OPPONENTS);
            BiPredicate<Pair<Coord>, Board> condition = (cPair, board) ->
                    board.getSpace(cPair.get(1)).getHeight().ordinal() >
                            board.getSpace(cPair.get(0)).getHeight().ordinal();
            r.setCondition(condition);
            result.add(r);

            List<Rule> generatedRules = new ArrayList<>();
            r.setGeneratedRules(generatedRules);

            r = new Rule();
            r.setPurpose(Purpose.VALIDATION);
            r.setActionType(ActionType.MOVE);
            r.setDecision(Decision.DENY);
            condition = (cPair, board) ->
                    board.getSpace(cPair.get(1)).getHeight().ordinal() >
                            board.getSpace(cPair.get(0)).getHeight().ordinal();
            r.setCondition(condition);
            generatedRules.add(r);

            r = new Rule();
            r.setPurpose(Purpose.GENERATION);
            r.setActionType(ActionType.MOVE);
            r.setTarget(Target.MYSELF);
            condition = (cPair, board) -> true;
            r.setCondition(condition);
            generatedRules.add(r);
            r.setGeneratedRules(generatedRules);

            r = new Rule();
            r.setPurpose(Purpose.GENERATION);
            r.setActionType(ActionType.BUILD);
            r.setTarget(Target.MYSELF);
            condition = (cPair, board) -> true;
            r.setCondition(condition);
            generatedRules.add(r);
            r.setGeneratedRules(generatedRules);

            r = new Rule();
            r.setPurpose(Purpose.GENERATION);
            r.setActionType(ActionType.END);
            r.setTarget(Target.MYSELF);
            condition = (cPair, board) -> true;
            r.setCondition(condition);
            generatedRules.add(r);
            r.setGeneratedRules(generatedRules);
        }

        if (god.equals("Atlas")) { // COMPLETE&TESTED
            Rule r = new Rule();
            r.setPurpose(Purpose.GENERATION);
            r.setActionType(ActionType.MOVE);
            r.setTarget(Target.MYSELF);
            BiPredicate<Pair<Coord>, Board> condition = (cPair, board) -> true;
            r.setCondition(condition);
            result.add(r);

            List<Rule> generatedRules = new ArrayList<>();
            r.setGeneratedRules(generatedRules);

            r = new Rule();
            r.setPurpose(Purpose.VALIDATION);
            r.setActionType(ActionType.BUILD);
            r.setDecision(Decision.GRANT);
            r.setBuildLevel(Level.DOME);
            condition = (cPair, board) ->
                    !board.getSpace(cPair.get(1)).isOccupied() && cPair.get(0).isNear(cPair.get(1)) &&
                            !board.getSpace(cPair.get(1)).isDome();
            r.setCondition(condition);
            generatedRules.add(r);
        }

        if (god.equals("Demeter")) {
            Rule r = new Rule();
            r.setPurpose(Purpose.GENERATION);
            r.setActionType(ActionType.MOVE);
            r.setTarget(Target.MYSELF);
            BiPredicate<Pair<Coord>, Board> condition = (cPair, board) -> true;
            r.setCondition(condition);
            result.add(r);

            List<Rule> generatedRules = new ArrayList<>();
            r.setGeneratedRules(generatedRules);
            generatedRules.add(r);

            r = new Rule();
            r.setPurpose(Purpose.GENERATION);
            r.setActionType(ActionType.BUILD);
            r.setTarget(Target.MYSELF);
            condition = (cPair, board) -> true;
            r.setCondition(condition);
            generatedRules.add(r);

            generatedRules = new ArrayList<>();
            r.setGeneratedRules(generatedRules);

            r = new Rule();
            r.setPurpose(Purpose.VALIDATION);
            r.setActionType(ActionType.BUILD);
            r.setDecision(Decision.DENY);
            r.setBuildLevel(Level.GROUND);
            TriPredicate<Pair<Coord>, Pair<Coord>, Board> symbolicCondition =
                    (oldPair, pair, board) -> oldPair.get(1).equals(pair.get(1));
            r.setSymbolicCondition(symbolicCondition);
            generatedRules.add(r);

            r = new Rule();
            r.setPurpose(Purpose.VALIDATION);
            r.setActionType(ActionType.BUILD);
            r.setDecision(Decision.DENY);
            r.setBuildLevel(Level.LVL1);
            symbolicCondition = (oldPair, pair, board) -> oldPair.get(1).equals(pair.get(1));
            r.setSymbolicCondition(symbolicCondition);
            generatedRules.add(r);

            r = new Rule();
            r.setPurpose(Purpose.VALIDATION);
            r.setActionType(ActionType.BUILD);
            r.setDecision(Decision.DENY);
            r.setBuildLevel(Level.LVL2);
            symbolicCondition = (oldPair, pair, board) -> oldPair.get(1).equals(pair.get(1));
            r.setSymbolicCondition(symbolicCondition);
            generatedRules.add(r);

            r = new Rule();
            r.setPurpose(Purpose.VALIDATION);
            r.setActionType(ActionType.BUILD);
            r.setDecision(Decision.DENY);
            r.setBuildLevel(Level.LVL3);
            symbolicCondition = (oldPair, pair, board) -> oldPair.get(1).equals(pair.get(1));
            r.setSymbolicCondition(symbolicCondition);
            generatedRules.add(r);

            r = new Rule();
            r.setPurpose(Purpose.VALIDATION);
            r.setActionType(ActionType.BUILD);
            r.setDecision(Decision.DENY);
            r.setBuildLevel(Level.DOME);
            symbolicCondition = (oldPair, pair, board) -> oldPair.get(1).equals(pair.get(1));
            r.setSymbolicCondition(symbolicCondition);
            generatedRules.add(r);

            generatedRules.addAll(standardBuildUpRules);

            r = new Rule();
            r.setPurpose(Purpose.GENERATION);
            r.setActionType(ActionType.BUILD);
            r.setTarget(Target.MYSELF);
            condition = (cPair, board) -> true;
            r.setCondition(condition);
            generatedRules.add(r);

            generatedRules = new ArrayList<>();
            r.setGeneratedRules(generatedRules);
            generatedRules.addAll(denyAll());
            generatedRules.add(doNothingOnEnd());
        }

        if (god.equals("Hephaestus")) { //COMPLETE&TESTED
            Rule r = new Rule();
            r.setPurpose(Purpose.GENERATION);
            r.setActionType(ActionType.MOVE);
            r.setTarget(Target.MYSELF);
            BiPredicate<Pair<Coord>, Board> condition = (cPair, board) -> true;
            r.setCondition(condition);

            List<Rule> generatedRules = new ArrayList<>();
            r.setGeneratedRules(generatedRules);
            generatedRules.add(r);
            result.add(r);

            r = new Rule();
            r.setPurpose(Purpose.GENERATION);
            r.setActionType(ActionType.BUILD);
            r.setTarget(Target.MYSELF);
            condition = (cPair, board) -> true;
            r.setCondition(condition);
            generatedRules.add(r);

            generatedRules = new ArrayList<>();
            r.setGeneratedRules(generatedRules);

            r = new Rule();
            r.setPurpose(Purpose.VALIDATION);
            r.setActionType(ActionType.BUILD);
            r.setDecision(Decision.DENY);
            r.setBuildLevel(Level.GROUND);
            TriPredicate<Pair<Coord>, Pair<Coord>, Board> symbolicCondition =
                    (oldPair, pair, board) -> !pair.get(1).equals(oldPair.get(1));
            r.setSymbolicCondition(symbolicCondition);
            generatedRules.add(r);

            r = new Rule();
            r.setPurpose(Purpose.VALIDATION);
            r.setActionType(ActionType.BUILD);
            r.setDecision(Decision.DENY);
            r.setBuildLevel(Level.LVL1);
            symbolicCondition = (oldPair, pair, board) -> !pair.get(1).equals(oldPair.get(1));
            r.setSymbolicCondition(symbolicCondition);
            generatedRules.add(r);

            r = new Rule();
            r.setPurpose(Purpose.VALIDATION);
            r.setActionType(ActionType.BUILD);
            r.setDecision(Decision.DENY);
            r.setBuildLevel(Level.LVL2);
            symbolicCondition = (oldPair, pair, board) -> !pair.get(1).equals(oldPair.get(1));
            r.setSymbolicCondition(symbolicCondition);
            generatedRules.add(r);

            r = new Rule();
            r.setPurpose(Purpose.VALIDATION);
            r.setActionType(ActionType.BUILD);
            r.setDecision(Decision.DENY);
            r.setBuildLevel(Level.LVL3);
            symbolicCondition = (oldPair, pair, board) -> !pair.get(1).equals(oldPair.get(1));
            r.setSymbolicCondition(symbolicCondition);
            generatedRules.add(r);

            r = new Rule();
            r.setPurpose(Purpose.VALIDATION);
            r.setActionType(ActionType.BUILD);
            r.setDecision(Decision.DENY);
            r.setBuildLevel(Level.DOME);
            condition = (cPair, board) -> true;
            r.setCondition(condition);
            generatedRules.add(r);

            generatedRules.addAll(standardBuildUpRules);

            r = new Rule();
            r.setPurpose(Purpose.GENERATION);
            r.setTarget(Target.MYSELF);
            r.setActionType(ActionType.BUILD);
            condition = (cPair, board) -> true;
            r.setCondition(condition);
            generatedRules.add(r);

            generatedRules = new ArrayList<>();
            r.setGeneratedRules(generatedRules);
            generatedRules.addAll(denyAll());
            generatedRules.add(doNothingOnEnd());
        }

        if (god.equals("Minotaur")) { //COMPLETE&TESTED
            Rule r = new Rule();
            r.setPurpose(Purpose.VALIDATION);
            r.setActionType(ActionType.MOVE);
            r.setDecision(Decision.GRANT);
            BiFunction<Coord, Coord, Coord> getDirection = (before, after) ->
                    new Coord(after.x - before.x, after.y - before.y);
            BiPredicate<Pair<Coord>, Board> condition = (cPair, board) ->
                    board.getSpace(cPair.get(1)).isOccupied() &&
                            cPair.get(0).isNear(cPair.get(1)) &&
                            !board.getWorkerCopy(cPair.get(1)).getPlayerNickname().equals(
                                    board.getWorkerCopy(cPair.get(0)).getPlayerNickname()
                            ) &&
                            Coord.validCoord(cPair.get(1).sum(getDirection.apply(cPair.get(0), cPair.get(1)))) &&
                            !board.getSpace(cPair.get(1).sum(getDirection.apply(cPair.get(0), cPair.get(1))))
                                    .isOccupied();
            r.setCondition(condition);
            BiFunction<Coord, Coord, Coord> forceSpaceFunction = (before, after) ->
                    after.sum(getDirection.apply(before, after));
            r.setForceSpaceFunction(forceSpaceFunction);
            result.add(r);
        }

        if (god.equals("Pan")) { //COMPLETE&TESTED
            Rule r = new Rule();
            r.setPurpose(Purpose.WIN);
            r.setActionType(ActionType.MOVE);
            r.setDecision(Decision.GRANT);
            BiPredicate<Pair<Coord>, Board> condition = (cPair, board) ->
                    board.getSpace(cPair.get(0)).getHeight().ordinal() -
                            board.getSpace(cPair.get(1)).getHeight().ordinal()
                            >= 2;
            r.setCondition(condition);
            result.add(r);
        }

        if (god.equals("Prometheus")) { //COMPLETE&TESTED
            result.addAll(standardBuildUpRules);

            Rule r = new Rule();
            r.setPurpose(Purpose.GENERATION);
            r.setActionType(ActionType.BUILD);
            r.setTarget(Target.MYSELF);
            BiPredicate<Pair<Coord>, Board> condition = (cPair, board) -> true;
            r.setCondition(condition);
            result.add(r);

            List<Rule> generatedRules = new ArrayList<>();
            r.setGeneratedRules(generatedRules);

            r = new Rule();
            r.setPurpose(Purpose.VALIDATION);
            r.setActionType(ActionType.MOVE);
            r.setDecision(Decision.DENY);
            condition = (cPair, board) ->
                    board.getSpace(cPair.get(1)).getHeight().ordinal() >
                            board.getSpace(cPair.get(0)).getHeight().ordinal();
            r.setCondition(condition);
            generatedRules.add(r);
            generatedRules.addAll(standardRules);
        }

        godRules.put(god, result);
    }


    private static void initStandardBuildUpRules() {
        if (standardBuildUpRules.size() != 0) {
            throw new IllegalStateException("Trying to initialize standard build-up " +
                    "rules when already initialized.");
        }

        List<Rule> result = new ArrayList<>();

        BiPredicate<Pair<Coord>, Board> conditionOnProximity = (cPair, board) ->
                cPair.get(0).isNear((cPair.get(1)));
        BiPredicate<Pair<Coord>, Board> conditionOnFreeSpace = (cPair, board) ->
                !board.getSpace(cPair.get(1)).isOccupied() &&
                        !board.getSpace(cPair.get(1)).isDome();
        BiPredicate<Pair<Coord>, Board> conditionOnLevelUp;

        BiPredicate<Pair<Coord>, Board> condition;
        Rule r;

        for (Level level : Level.values()) {

            if (level == Level.GROUND) {
                r = new Rule();
                r.setPurpose(Purpose.VALIDATION);
                r.setActionType(ActionType.BUILD);
                r.setDecision(Decision.DENY);
                r.setBuildLevel(level);
                r.setCondition((a, b) -> true);
                result.add(r);
                continue;
            }

            r = new Rule();
            r.setPurpose(Purpose.VALIDATION);
            r.setActionType(ActionType.BUILD);
            r.setDecision(Decision.GRANT);
            r.setBuildLevel(level);
            conditionOnLevelUp = (cPair, board) ->
                    board.getSpace(cPair.get(1)).getHeight().ordinal() == level.ordinal() - 1;
            condition =
                    conditionOnProximity
                            .and(conditionOnFreeSpace
                                    .and(conditionOnLevelUp));
            r.setCondition(condition);
            result.add(r);

            r = new Rule();
            r.setPurpose(Purpose.VALIDATION);
            r.setActionType(ActionType.BUILD);
            r.setDecision(Decision.DENY);
            r.setBuildLevel(level);
            r.setCondition(condition.negate());
            result.add(r);

        }

        standardBuildUpRules.addAll(result);
    }

    private static List<Rule> denyAll() {
        List<Rule> result = new ArrayList<>();
        Rule r;
        BiPredicate<Pair<Coord>, Board> condition;

        r = new Rule();
        r.setPurpose(Purpose.VALIDATION);
        r.setDecision(Decision.DENY);
        r.setActionType(ActionType.MOVE);
        condition = (cPair, board) -> true;
        r.setCondition(condition);
        result.add(r);

        r = new Rule();
        r.setPurpose(Purpose.VALIDATION);
        r.setDecision(Decision.DENY);
        r.setActionType(ActionType.BUILD);
        r.setBuildLevel(Level.GROUND);
        condition = (cPair, board) -> true;
        r.setCondition(condition);
        result.add(r);

        r = new Rule();
        r.setPurpose(Purpose.VALIDATION);
        r.setDecision(Decision.DENY);
        r.setActionType(ActionType.BUILD);
        r.setBuildLevel(Level.LVL1);
        condition = (cPair, board) -> true;
        r.setCondition(condition);
        result.add(r);

        r = new Rule();
        r.setPurpose(Purpose.VALIDATION);
        r.setDecision(Decision.DENY);
        r.setActionType(ActionType.BUILD);
        r.setBuildLevel(Level.LVL2);
        condition = (cPair, board) -> true;
        r.setCondition(condition);
        result.add(r);

        r = new Rule();
        r.setPurpose(Purpose.VALIDATION);
        r.setDecision(Decision.DENY);
        r.setActionType(ActionType.BUILD);
        r.setBuildLevel(Level.LVL3);
        condition = (cPair, board) -> true;
        r.setCondition(condition);
        result.add(r);

        r = new Rule();
        r.setPurpose(Purpose.VALIDATION);
        r.setDecision(Decision.DENY);
        r.setActionType(ActionType.BUILD);
        r.setBuildLevel(Level.DOME);
        condition = (cPair, board) -> true;
        r.setCondition(condition);
        result.add(r);

        return result;
    }

    private static Rule doNothingOnEnd() {
        Rule r = new Rule();
        r.setPurpose(Purpose.GENERATION);
        r.setTarget(Target.MYSELF);
        r.setActionType(ActionType.END);
        BiPredicate<Pair<Coord>, Board> condition = (cPair, board) -> true;
        r.setCondition(condition);

        List<Rule> generatedRules = new ArrayList<>();
        r.setGeneratedRules(generatedRules);

        generatedRules.addAll(denyAll());

        return r;
    }

    public static List<Rule> getStandardWinRules() {

        Rule r = new Rule();
        r.setPurpose(Purpose.WIN);
        r.setActionType(ActionType.MOVE);
        r.setDecision(Decision.GRANT);
        BiPredicate<Pair<Coord>, Board> condition = (cPair, board) ->
                board.getSpace(cPair.get(0)).getHeight() == Level.LVL2 &&
                        board.getSpace(cPair.get(1)).getHeight() == Level.LVL3;
        r.setCondition(condition);

        List<Rule> newResult = new ArrayList<>();
        newResult.add(r);
        return newResult;

        /*
        List<Rule> rulesForPropagation = Arrays.stream(ActionType.values())
                .map(at -> {
                    Rule g = new Rule();
                    g.setPurpose(Purpose.GENERATION);
                    g.setActionType(at);
                    g.setTarget(Target.MYSELF);
                    g.setCondition((cPair, board) -> true);
                    return g;
                }
                ).collect(Collectors.toList());
        List<Rule> generatedRules = new ArrayList<>(rulesForPropagation);
        generatedRules.add(r);
        rulesForPropagation.forEach(p -> p.setGeneratedRules(generatedRules));
        return generatedRules;
         */
    }
}

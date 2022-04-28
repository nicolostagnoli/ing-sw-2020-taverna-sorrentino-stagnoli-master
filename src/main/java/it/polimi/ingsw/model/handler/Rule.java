/*
Fundamental principle of the class:
IMMUTABILITY
Every rule is created just once and then all its attributes are NEVER modified.
Rules are read, discarded and re-added in RuleHandlers, but never modified.
When a rule is generated and its symbolic condition needs to be transformed into
a condition, a new rule is generated in place of the original one, preventing the
original one to be changed. Therefore, other handlers can still use it and use its
symbolic condition.
 */

package it.polimi.ingsw.model.handler;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.handler.util.Pair;
import it.polimi.ingsw.model.handler.util.TriPredicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/**
 * Represents a rule of the game.
 * This class is at the heart of the representation of all standard rules and
 * God Effects in the game.
 * It achieves its aim thanks to a high level of detail, provided by
 * several attributes that can be accessed through the getters/setters of the class.
 * A fundamental principle of the class is its <strong>immutability</strong>. Attributes can be
 * set only once and then the rule can be used by multiple users without interfering with
 * one another leading to undesired changes in behaviors.
 */
class Rule {
    private Purpose purpose;
    private ActionType actionType;
    private Decision decision;
    private BiPredicate<Pair<Coord>, Board> condition;
    private BiFunction<Coord, Coord, Coord> forceSpaceFunction;
    private Level buildLevel;
    private List<Rule> generatedRules;
    private Target target;
    private TriPredicate<Pair<Coord>, Pair<Coord>, Board> symbolicCondition;

    public Rule() {}

    public Rule(Rule rule) {
        this.purpose = rule.purpose;
        this.actionType = rule.actionType;
        this.decision = rule.decision;
        this.condition = rule.condition;
        this.forceSpaceFunction = rule.forceSpaceFunction;
        this.buildLevel = rule.buildLevel;
        this.generatedRules = rule.generatedRules;
        this.target = rule.target;
        this.symbolicCondition = rule.symbolicCondition;
    }


    public Purpose getPurpose() {
        assert repOk();
        return this.purpose;
    }

    ActionType getActionType() {
        assert repOk();
        return actionType;
    }

    Decision getDecision() {
        assert repOk();
        return decision;
    }

    BiPredicate<Pair<Coord>, Board> getCondition() {
        assert repOk();
        return condition;
    }

    BiFunction<Coord, Coord, Coord> getForceSpaceFunction() {
        assert repOk();
        if (forceSpaceFunction != null) {
            return forceSpaceFunction;
        }
        else {
            return (a, b) -> null;
        }
    }

    Level getBuildLevel() {
        assert repOk();
        return buildLevel;
    }

    List<Rule> getGeneratedRules(Pair<Coord> oldAction) {
        assert repOk();
        List<Rule> result = new ArrayList<>();
        for (Rule rule : generatedRules) {
            Rule g = rule;
            if (rule.symbolicCondition != null) {
                g = new Rule(rule);
                g.condition = (cPair, board) ->
                        rule.symbolicCondition.test(oldAction, cPair, board);
                g.symbolicCondition = null;
            }
            g.repOk();
            result.add(g);
        }
        return result;
    }

    Target getTarget() {
        assert repOk();
        return target;
    }

    void setPurpose(Purpose purpose) {
        assert this.purpose == null;
        this.purpose = purpose;
    }

    void setActionType(ActionType actionType) {
        assert this.actionType == null;
        this.actionType = actionType;
    }

    void setDecision(Decision decision) {
        assert this.decision == null;
        this.decision = decision;
    }

    void setCondition(BiPredicate<Pair<Coord>, Board> condition) {
        assert this.condition == null;
        assert this.symbolicCondition == null;
        this.condition = condition;
    }

    void setForceSpaceFunction(BiFunction<Coord, Coord, Coord> f) {
        assert this.forceSpaceFunction == null;
        this.forceSpaceFunction = f;
    }

    void setBuildLevel(Level buildLevel) {
        assert this.buildLevel == null;
        this.buildLevel = buildLevel;
    }

    void setGeneratedRules(List<Rule> generatedRules) {
        assert this.generatedRules == null;
        this.generatedRules = generatedRules;
    }

    void setSymbolicCondition(TriPredicate<Pair<Coord>, Pair<Coord>, Board> symbolicCondition) {
        assert this.symbolicCondition == null;
        assert this.condition == null;
        this.symbolicCondition = symbolicCondition;
    }

    void setTarget(Target target) {
        assert this.target == null;
        this.target = target;
    }

    @Override
    public String toString() {
        String result;

        result = purpose.name() + "_" + actionType.name() + " Rule @ " + Integer.toHexString(hashCode());
        return result;
    }

    private boolean repOk() {
        boolean repOk = actionType != null &&
        condition != null &&
        symbolicCondition == null &&
        iff(decision == null, purpose == Purpose.GENERATION) &&
        iff(actionType==ActionType.BUILD && purpose == Purpose.VALIDATION, buildLevel != null) &&
        ifThen(forceSpaceFunction != null, purpose == Purpose.VALIDATION) &&
        ifThen(forceSpaceFunction != null, actionType == ActionType.MOVE) &&
        ifThen(forceSpaceFunction != null, decision == Decision.GRANT) &&
        iff(generatedRules == null,purpose != Purpose.GENERATION) &&
        ifThen(purpose == Purpose.VALIDATION, actionType != ActionType.END) &&
        iff(purpose == Purpose.GENERATION, target != null);

        if (!repOk) {
            System.out.println("Rep for the rule is invalid.");
        }
        return repOk;
    }

    private boolean ifThen(boolean a, boolean b) {
        return (!a||b);
    }

    private boolean iff(boolean a, boolean b) {
        return ifThen(a, b) && ifThen(b, a);
    }
}
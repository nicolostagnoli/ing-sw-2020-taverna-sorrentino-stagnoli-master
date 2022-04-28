package it.polimi.ingsw.exceptions.model.handler;

public class RuleParserException extends Exception {
    public RuleParserException(String message, String errorLine, int numLine) {
        super(message + "\nLine " + numLine + ": " + errorLine);
    }
    public RuleParserException(String message) {
        super(message);
    }
}

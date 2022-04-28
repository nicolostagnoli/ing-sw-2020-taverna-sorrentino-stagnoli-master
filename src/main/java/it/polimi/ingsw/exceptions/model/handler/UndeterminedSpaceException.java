package it.polimi.ingsw.exceptions.model.handler;

public class UndeterminedSpaceException extends RuntimeException {
    public UndeterminedSpaceException() {};
    public UndeterminedSpaceException(String message) {
        super(message);
    }
}

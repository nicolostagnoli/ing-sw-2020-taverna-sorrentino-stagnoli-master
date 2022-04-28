package it.polimi.ingsw.exceptions.model;

public class AlreadyExistingPlayerException extends RuntimeException {
    public AlreadyExistingPlayerException(String msg){
        super(msg);
    }
}

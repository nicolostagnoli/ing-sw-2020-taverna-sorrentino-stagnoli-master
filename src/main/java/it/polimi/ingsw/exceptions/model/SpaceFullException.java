package it.polimi.ingsw.exceptions.model;

public class SpaceFullException extends IllegalArgumentException {
    public SpaceFullException(String msg){
        super(msg);
    }
}

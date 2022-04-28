package it.polimi.ingsw.exceptions.model;

public class SpaceOccupiedException extends IllegalArgumentException {
    public SpaceOccupiedException(String msg){
        super(msg);
    }
}

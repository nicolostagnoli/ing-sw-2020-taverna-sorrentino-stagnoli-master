package it.polimi.ingsw.exceptions.model;

public class InvalidCoordinatesException extends IllegalArgumentException {
    public InvalidCoordinatesException(String msg){
        super(msg);
    }
}

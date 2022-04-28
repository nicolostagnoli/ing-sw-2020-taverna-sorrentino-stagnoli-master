package it.polimi.ingsw.exceptions.model;

import java.util.NoSuchElementException;

public class GameFullException extends RuntimeException {
    public GameFullException(String msg){
        super(msg);
    }
}

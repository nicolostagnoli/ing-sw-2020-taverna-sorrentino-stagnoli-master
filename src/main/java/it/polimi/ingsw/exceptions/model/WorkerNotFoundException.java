package it.polimi.ingsw.exceptions.model;

import java.util.NoSuchElementException;

public class WorkerNotFoundException extends NoSuchElementException {
    public WorkerNotFoundException(String msg){
        super(msg);
    }
}

package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.PrepareModelException;
import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GameRulesTest {
    protected GameModel model;
    protected Board before;
    protected Coord src;
    protected Coord dest;


    protected void nextStep() {
        try {
            model.nextStep();
        }
            catch (RuntimeException e) {
            // Exception is thrown if there is no listeners for the player.
            // This is ok when testing the model, because Views do not exist.
        }
    }

    protected void setMove(Coord c) {
        try {
            model.setMove(c);
        }
        catch (NullPointerException e) {
            // Exception is thrown if there is no listeners for the player.
            // This is ok when testing the model, because Views do not exist.
        }
    }

    protected void setBuild(Coord c, Level l) {
        try {
            model.setBuild(c, l);
        }
        catch (NullPointerException e) {
            // Exception is thrown if there is no listeners for the player.
            // This is ok when testing the model, because Views do not exist.
        }
    }



    protected void assertCannotBuildOnAnyLevel(Coord buildChoice) {
        Board after;
        for (Level level : Level.values()) {
            setBuild(buildChoice, level);
            after = model.getBoard();
            assertEquals(before, after);
        }
    }

    protected void cannotMove() {
        nextStep();
        setMove(dest);
        Board after = model.getBoard();
        assertEquals(before, after);
    }
}

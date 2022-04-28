package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.PrepareModelException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class MinotaurTest extends GameRulesTest {

    @Before
    public void setInitialSituation() {
        try {
            model = TestUtils.prepareModel("minotaur_test");
            before = model.getBoard();
        }
        catch (PrepareModelException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            fail("Couldn't prepare model as specified in configuration file.");
        }
    }


    //-------------------------------TEST SECTION---------------------------------

    @Test
    public void effect() {
        /*
        Test he can force a player in his move direction
        */

        src = Coord.convertStringToCoord("A1");
        dest = Coord.convertStringToCoord("B1");

        //Do the move
        nextStep();
        model.setWorkerChoice(src);
        nextStep();
        setMove(dest);

        //Check what changed
        Board after = model.getBoard();
        assertFalse(after.getSpace(src).isOccupied());
        assertTrue(after.getSpace(dest).isOccupied());
        assertEquals("Player1", after.getWorkerByPosition(dest).getPlayerNickname());
        Coord forceDest = Coord.convertStringToCoord("C1");
        assertTrue(after.getSpace(forceDest).isOccupied());
        assertEquals(before.getWorkerByPosition(dest).getPlayerNickname(),
                after.getWorkerByPosition(forceDest).getPlayerNickname());
    }
}

package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.PrepareModelException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AthenaTest extends GameRulesTest {

    @Before
    public void setInitialSituation() {
        try {
            model = TestUtils.prepareModel("athena_test");
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
        Test that: if she moves up, others cannot move up
        */

        //Cause
        src = Coord.convertStringToCoord("A1");
        dest = Coord.convertStringToCoord("B2");
        nextStep();
        model.setWorkerChoice(src);
        nextStep();
        setMove(dest);
        assertEquals(before.getWorkerByPosition(src).getPlayerNickname(),
                model.getBoard().getWorkerByPosition(dest).getPlayerNickname());
        assertFalse(model.getBoard().getSpace(src).isOccupied());
        nextStep();
        setBuild(src, Level.LVL1);
        before = model.getBoard();
        nextStep();

        //Effect
        //(on second player)
        assertEquals("Player2", model.getCurrentPlayer().getNickname());

        src = Coord.convertStringToCoord("B1");
        dest = Coord.convertStringToCoord("C1");

        model.setWorkerChoice(src);
        cannotMove();

        dest = Coord.convertStringToCoord("A1");
        setMove(dest);
        assertEquals(before.getWorkerByPosition(src).getPlayerNickname(),
                model.getBoard().getWorkerByPosition(dest).getPlayerNickname());
        assertFalse(model.getBoard().getSpace(src).isOccupied());
        nextStep();
        setBuild(src, Level.LVL2);
        assertEquals(Level.LVL2, model.getBoard().getSpace(src).getHeight());
        before = model.getBoard();
        nextStep();

        //if the player hasn't changed yet, he needs to end his turn explicitly:
        if (model.getCurrentPlayer().getNickname().equals("Player2")) {
            model.setEnd();
            nextStep();
        }

        //(on third player)
        assertEquals("Player3", model.getCurrentPlayer().getNickname());
        src = Coord.convertStringToCoord("E1");
        dest = Coord.convertStringToCoord("D2");
        cannotMove();
    }
}

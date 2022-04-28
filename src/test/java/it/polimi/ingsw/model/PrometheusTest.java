package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.PrepareModelException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class PrometheusTest extends GameRulesTest {

    @Before
    public void setInitialSituation() {
        try {
            model = TestUtils.prepareModel("prometheus_test");
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
        Test he can build, move (not up), build
        */

        src = Coord.convertStringToCoord("B1");
        dest = Coord.convertStringToCoord("C1");
        nextStep();
        model.setWorkerChoice(src);
        nextStep();

        //First build
        setBuild(dest, Level.LVL2);
        nextStep();
        Board after = model.getBoard();
        assertEquals(Level.LVL2, after.getSpace(dest).getHeight());

        //Move
        dest = Coord.convertStringToCoord("B2");
        setMove(dest);
        nextStep();
        after = model.getBoard();
        assertFalse(after.getSpace(src).isOccupied());
        assertTrue(after.getSpace(dest).isOccupied());

        //Second build
        dest = Coord.convertStringToCoord("B3");
        setBuild(dest, Level.LVL1);
        after = model.getBoard();
        assertEquals(Level.LVL1, after.getSpace(dest).getHeight());
    }
}

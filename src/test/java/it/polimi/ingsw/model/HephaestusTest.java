package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.PrepareModelException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class HephaestusTest extends GameRulesTest {

    @Before
    public void setInitialSituation() {
        try {
            model = TestUtils.prepareModel("hephaestus_test");
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
        Test he can build twice (but on same space)
        */

        src = Coord.convertStringToCoord("B1");
        dest = Coord.convertStringToCoord("B2");

        //Pre-action
        nextStep();
        model.setWorkerChoice(src);
        nextStep();
        setMove(dest);

        //Do the first build
        dest = Coord.convertStringToCoord("B3");
        nextStep();
        setBuild(dest, Level.LVL1);

        //Check what changed
        Board after = model.getBoard();
        assertEquals(Level.LVL1, after.getSpace(dest).getHeight());

        //Do the second build on a different space
        dest = Coord.convertStringToCoord("C3");
        nextStep();
        setBuild(dest, Level.LVL2);

        //Check it didn't work
        after = model.getBoard();
        assertEquals(Level.GROUND, after.getSpace(dest).getHeight());

        //Do the second build on the same space
        dest = Coord.convertStringToCoord("B3");
        nextStep();
        setBuild(dest, Level.LVL2);

        //Check what changed
        after = model.getBoard();
        assertEquals(Level.LVL2, after.getSpace(dest).getHeight());

    }
}

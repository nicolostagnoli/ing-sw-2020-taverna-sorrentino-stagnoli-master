package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.PrepareModelException;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class PanTest extends GameRulesTest {

    @Before
    public void setInitialSituation() {
        try {
            model = TestUtils.prepareModel("pan_test");
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
        Test he can win by moving down two or more levels
        */

        src = Coord.convertStringToCoord("B1");
        dest = Coord.convertStringToCoord("C1");

        //Do the move
        nextStep();
        model.setWorkerChoice(src);
        nextStep();
        setMove(dest);

        //Check he won (the game is finished)
        Field stateField;
        ModelState modelState;
        try {
            stateField = GameModel.class.getDeclaredField("state");
        }
        catch (NoSuchFieldException e) {
            fail();
            return;
        }
        stateField.setAccessible(true);
        try {
            modelState = (ModelState) stateField.get(model);
        }
        catch (IllegalAccessException e) {
            fail();
            return;
        }

        assertTrue(modelState instanceof EndState);
    }
}

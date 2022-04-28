package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.PrepareModelException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class ArtemisTest extends GameRulesTest {

    @Before
    public void setInitialSituation() {
        try {
            model = TestUtils.prepareModel("artemis_test");
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
        Test she is allowed to move twice
        */

        //Pre-action
        nextStep();
        model.setWorkerChoice(Coord.convertStringToCoord("A1"));
        nextStep();
        setMove(Coord.convertStringToCoord("B2"));
        before = model.getBoard();
        nextStep();

        //Do the move
        src = Coord.convertStringToCoord("B2");
        dest = Coord.convertStringToCoord("C1");
        setMove(dest);

        //Check what changed
        Board after = model.getBoard();
        assertFalse(after.getSpace(src).isOccupied());
        assertTrue(after.getSpace(dest).isOccupied());
        assertFalse(after.getSpace(src).isDome());
        assertFalse(after.getSpace(dest).isDome());
        assertSame(before.getSpace(src).getHeight(), after.getSpace(src).getHeight());
        assertSame(before.getSpace(dest).getHeight(), after.getSpace(dest).getHeight());
        Worker myWorkerBefore = before.getWorkerByPosition(src);
        Worker myWorkerAfter = after.getWorkerByPosition(dest);
        assertEquals(myWorkerBefore.getPlayerNickname(), myWorkerAfter.getPlayerNickname());
        assertSame(myWorkerBefore.getColor(), myWorkerAfter.getColor());
        assertEquals(myWorkerAfter.getPosition(), dest);

        //Check what didn't change
        List<Coord> allCoord = after.getAllCoord();
        boolean removed = allCoord.remove(src);
        assertTrue(removed);
        removed = allCoord.remove(dest);
        assertTrue(removed);
        List<Space> allBeforeSpaces = allCoord.stream()
                .map(before::getSpace)
                .collect(Collectors.toList());
        List<Space> allAfterSpaces = allCoord.stream()
                .map(after::getSpace)
                .collect(Collectors.toList());
        assertEquals(allBeforeSpaces, allAfterSpaces);
        List<Worker> beforeWorkers = Arrays.asList(before.getAllWorkers());
        beforeWorkers = new ArrayList<>(beforeWorkers);
        List<Worker> afterWorkers = Arrays.asList(after.getAllWorkers());
        afterWorkers = new ArrayList<>(afterWorkers);
        removed = beforeWorkers.remove(myWorkerBefore);
        assertTrue(removed);
        removed = afterWorkers.remove(myWorkerAfter);
        assertTrue(removed);
        assertEquals(beforeWorkers, afterWorkers);
    }

    @Test public void cannotGoBack() {
        /*
        Test she cannot move back to her initial position
        */

        //Pre-action
        nextStep();
        model.setWorkerChoice(Coord.convertStringToCoord("B1"));
        nextStep();
        setMove(Coord.convertStringToCoord("B2"));
        assertEquals(
                before.getWorkerByPosition(Coord.convertStringToCoord("B1")).getPlayerNickname(),
                model.getBoard().getWorkerByPosition(Coord.convertStringToCoord("B2"))
                        .getPlayerNickname()
        );
        assertFalse(model.getBoard().getSpace(Coord.convertStringToCoord("B1")).isOccupied());
        before = model.getBoard();
        nextStep();

        //Do the move
        src = Coord.convertStringToCoord("B2");
        dest = Coord.convertStringToCoord("B1");

        cannotMove();
    }

    @Test
    public void cannotMoveThreeTimes() {
        /*
        Test she cannot move three times
        */

        //Pre-action
        nextStep();
        model.setWorkerChoice(Coord.convertStringToCoord("A1"));
        nextStep();
        setMove(Coord.convertStringToCoord("B2"));
        nextStep();
        setMove(Coord.convertStringToCoord("C1"));
        before = model.getBoard();
        nextStep();

        //Do the move
        src = Coord.convertStringToCoord("C1");
        dest = Coord.convertStringToCoord("C2");

        cannotMove();
    }
}

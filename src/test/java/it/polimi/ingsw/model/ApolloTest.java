package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.PrepareModelException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class ApolloTest extends GameRulesTest {

    @Before
    public void setInitialSituation() {
        try {
            model = TestUtils.prepareModel("apollo_test");
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
        Test he is allowed to move to occupied spaces
        and switch his position with the other worker
        */

        src = Coord.convertStringToCoord("E5");
        dest = Coord.convertStringToCoord("D4");

        //Do the move
        nextStep();
        model.setWorkerChoice(src);
        nextStep();
        setMove(dest);

        //Check what changed
        Board after = model.getBoard();
        assertTrue(after.getSpace(src).isOccupied());
        assertTrue(after.getSpace(dest).isOccupied());
        assertFalse(after.getSpace(src).isDome());
        assertFalse(after.getSpace(dest).isDome());
        assertSame(before.getSpace(src).getHeight(), after.getSpace(src).getHeight());
        assertSame(before.getSpace(dest).getHeight(), after.getSpace(dest).getHeight());
        Worker myWorkerBefore = before.getWorkerByPosition(src);
        Worker myWorkerAfter = after.getWorkerByPosition(dest);
        Worker otherWorkerBefore = before.getWorkerByPosition(dest);
        Worker otherWorkerAfter = after.getWorkerByPosition(src);
        assertEquals(myWorkerBefore.getPosition(), otherWorkerAfter.getPosition());
        assertEquals(myWorkerAfter.getPosition(), otherWorkerBefore.getPosition());
        assertEquals(myWorkerBefore.getPlayerNickname(), myWorkerAfter.getPlayerNickname());
        assertEquals(otherWorkerBefore.getPlayerNickname(), otherWorkerAfter.getPlayerNickname());
        assertNotEquals(myWorkerAfter.getPlayerNickname(), otherWorkerAfter.getPlayerNickname());
        assertSame(myWorkerBefore.getColor(), myWorkerAfter.getColor());
        assertSame(otherWorkerBefore.getColor(), otherWorkerAfter.getColor());

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
        removed = beforeWorkers.remove(otherWorkerBefore);
        assertTrue(removed);
        removed = afterWorkers.remove(myWorkerAfter);
        assertTrue(removed);
        removed = afterWorkers.remove(otherWorkerAfter);
        assertTrue(removed);
        assertEquals(beforeWorkers, afterWorkers);
    }
}

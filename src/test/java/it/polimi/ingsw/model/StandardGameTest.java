package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.PrepareModelException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class StandardGameTest extends GameRulesTest {

    @Before
    public void setInitialSituation() {
        try {
            model = TestUtils.prepareModel("base_test");
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
    public void cannotMoveInOccupiedSpace() {
        src = Coord.convertStringToCoord("B1");
        dest = Coord.convertStringToCoord("A1");
        nextStep();
        model.setWorkerChoice(src);
        super.cannotMove();
    }

    @Test
    public void cannotMoveToDome() {
        src = Coord.convertStringToCoord("B1");
        dest = Coord.convertStringToCoord("C2");
        nextStep();
        model.setWorkerChoice(src);
        super.cannotMove();
    }

    @Test
    public void cannotMoveUpMoreThanOneLevel() {
        src = Coord.convertStringToCoord("A1");
        dest = Coord.convertStringToCoord("A2");
        nextStep();
        model.setWorkerChoice(src);
        super.cannotMove();
    }

    @Test
    public void cannotMoveFarAway() {
        //Playing with second player
        model.nextPlayer();

        src = Coord.convertStringToCoord("A3");
        dest = Coord.convertStringToCoord("C3");
        nextStep();
        model.setWorkerChoice(src);
        super.cannotMove();
    }

    @Test
    public void canMoveDownOneLevel() {
        //Do the move
        nextStep();
        model.setWorkerChoice(Coord.convertStringToCoord("B1"));
        nextStep();
        setMove(Coord.convertStringToCoord("B2"));

        //Check what changed
        Board after = model.getBoard();
        assertTrue(after.getSpace(Coord.convertStringToCoord("B2")).isOccupied());
        assertFalse(after.getSpace(Coord.convertStringToCoord("B1")).isOccupied());
        assertFalse(after.getSpace(Coord.convertStringToCoord("B2")).isDome());
        assertFalse(after.getSpace(Coord.convertStringToCoord("B1")).isDome());
        assertSame(after.getSpace(Coord.convertStringToCoord("B2")).getHeight(),
                before.getSpace(Coord.convertStringToCoord("B2")).getHeight());
        assertSame(after.getSpace(Coord.convertStringToCoord("B1")).getHeight(),
                before.getSpace(Coord.convertStringToCoord("B1")).getHeight());
        Worker beforeWorker = before.getWorkerByPosition(Coord.convertStringToCoord("B1"));
        Worker afterWorker = after.getWorkerByPosition(Coord.convertStringToCoord("B2"));
        assertEquals(beforeWorker.getPlayerNickname(), afterWorker.getPlayerNickname());
        assertEquals(beforeWorker.getColor(), afterWorker.getColor());
        assertEquals(afterWorker.getPosition(), Coord.convertStringToCoord("B2"));

        //Check what didn't change
        List<Coord> allCoord = after.getAllCoord();
        boolean removed = allCoord.remove(Coord.convertStringToCoord("B2"));
        assertTrue(removed);
        removed = allCoord.remove(Coord.convertStringToCoord("B1"));
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
        removed = beforeWorkers.remove(beforeWorker);
        assertTrue(removed);
        removed = afterWorkers.remove(afterWorker);
        assertTrue(removed);
        assertEquals(beforeWorkers, afterWorkers);


    }

    @Test
    public void canMoveDownTwoLevels() {
        //Do the move
        nextStep();
        model.setWorkerChoice(Coord.convertStringToCoord("B1"));
        nextStep();
        setMove(Coord.convertStringToCoord("C1"));

        //Check what changed
        Board after = model.getBoard();
        assertTrue(after.getSpace(Coord.convertStringToCoord("C1")).isOccupied());
        assertFalse(after.getSpace(Coord.convertStringToCoord("B1")).isOccupied());
        assertFalse(after.getSpace(Coord.convertStringToCoord("C1")).isDome());
        assertFalse(after.getSpace(Coord.convertStringToCoord("B1")).isDome());
        assertSame(after.getSpace(Coord.convertStringToCoord("C1")).getHeight(),
                before.getSpace(Coord.convertStringToCoord("C1")).getHeight());
        assertSame(after.getSpace(Coord.convertStringToCoord("B1")).getHeight(),
                before.getSpace(Coord.convertStringToCoord("B1")).getHeight());
        Worker beforeWorker = before.getWorkerByPosition(Coord.convertStringToCoord("B1"));
        Worker afterWorker = after.getWorkerByPosition(Coord.convertStringToCoord("C1"));
        assertEquals(beforeWorker.getPlayerNickname(), afterWorker.getPlayerNickname());
        assertEquals(beforeWorker.getColor(), afterWorker.getColor());
        assertEquals(afterWorker.getPosition(), Coord.convertStringToCoord("C1"));

        //Check what didn't change
        List<Coord> allCoord = after.getAllCoord();
        boolean removed = allCoord.remove(Coord.convertStringToCoord("C1"));
        assertTrue(removed);
        removed = allCoord.remove(Coord.convertStringToCoord("B1"));
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
        removed = beforeWorkers.remove(beforeWorker);
        assertTrue(removed);
        removed = afterWorkers.remove(afterWorker);
        assertTrue(removed);
        assertEquals(beforeWorkers, afterWorkers);
    }

    @Test
    public void canMoveOnSameLevel() {
        //Do the move
        nextStep();
        model.setWorkerChoice(Coord.convertStringToCoord("B1"));
        nextStep();
        setMove(Coord.convertStringToCoord("A2"));

        //Check what changed
        Board after = model.getBoard();
        assertTrue(after.getSpace(Coord.convertStringToCoord("A2")).isOccupied());
        assertFalse(after.getSpace(Coord.convertStringToCoord("B1")).isOccupied());
        assertFalse(after.getSpace(Coord.convertStringToCoord("A2")).isDome());
        assertFalse(after.getSpace(Coord.convertStringToCoord("B1")).isDome());
        assertSame(after.getSpace(Coord.convertStringToCoord("A2")).getHeight(),
                before.getSpace(Coord.convertStringToCoord("A2")).getHeight());
        assertSame(after.getSpace(Coord.convertStringToCoord("B1")).getHeight(),
                before.getSpace(Coord.convertStringToCoord("B1")).getHeight());
        Worker beforeWorker = before.getWorkerByPosition(Coord.convertStringToCoord("B1"));
        Worker afterWorker = after.getWorkerByPosition(Coord.convertStringToCoord("A2"));
        assertEquals(beforeWorker.getPlayerNickname(), afterWorker.getPlayerNickname());
        assertEquals(beforeWorker.getColor(), afterWorker.getColor());
        assertEquals(afterWorker.getPosition(), Coord.convertStringToCoord("A2"));

        //Check what didn't change
        List<Coord> allCoord = after.getAllCoord();
        boolean removed = allCoord.remove(Coord.convertStringToCoord("A2"));
        assertTrue(removed);
        removed = allCoord.remove(Coord.convertStringToCoord("B1"));
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
        removed = beforeWorkers.remove(beforeWorker);
        assertTrue(removed);
        removed = afterWorkers.remove(afterWorker);
        assertTrue(removed);
        assertEquals(beforeWorkers, afterWorkers);
    }

    @Test
    public void cannotBuildOnOccupiedSpace() {
        //Pre-action
        nextStep();
        model.setWorkerChoice(Coord.convertStringToCoord("B1"));
        nextStep();
        setMove(Coord.convertStringToCoord("B2"));
        before = model.getBoard();
        nextStep();

        //Try to build
        assertCannotBuildOnAnyLevel(Coord.convertStringToCoord("A1"));
    }

    @Test
    public void cannotBuildOnDome() {
        //Pre-action
        nextStep();
        model.setWorkerChoice(Coord.convertStringToCoord("B1"));
        nextStep();
        setMove(Coord.convertStringToCoord("B2"));
        before = model.getBoard();
        nextStep();

        //Try to build
        assertCannotBuildOnAnyLevel(Coord.convertStringToCoord("C2"));
    }

    @Test
    public void cannotBuildOnMyself() {
        //Pre-action
        nextStep();
        model.setWorkerChoice(Coord.convertStringToCoord("B1"));
        nextStep();
        setMove(Coord.convertStringToCoord("B2"));
        before = model.getBoard();
        nextStep();

        //Try to build
        assertCannotBuildOnAnyLevel(Coord.convertStringToCoord("B2"));
    }

    @Test
    public void CannotBuildFarAway() {
        //Playing with second player
        model.nextPlayer();

        //Pre-action
        nextStep();
        model.setWorkerChoice(Coord.convertStringToCoord("A3"));
        nextStep();
        setMove(Coord.convertStringToCoord("B3"));
        before = model.getBoard();
        nextStep();

        //Try to build
        assertCannotBuildOnAnyLevel(Coord.convertStringToCoord("D3"));
    }

    @Test
    public void canBuildLevel() {
        //Pre-action
        nextStep();
        model.setWorkerChoice(Coord.convertStringToCoord("B1"));
        nextStep();
        setMove(Coord.convertStringToCoord("B2"));
        before = model.getBoard();
        Board after;

        //Try to build the wrong level
        nextStep();
        setBuild(Coord.convertStringToCoord("C1"), Level.GROUND);
        after = model.getBoard();
        assertEquals(before, after);

        //Try to build the wrong level
        nextStep();
        setBuild(Coord.convertStringToCoord("C1"), Level.LVL1);
        after = model.getBoard();
        assertEquals(before, after);

        //Try to build the wrong level
        nextStep();
        setBuild(Coord.convertStringToCoord("C1"), Level.LVL3);
        after = model.getBoard();
        assertEquals(before, after);

        //Try to build the wrong level
        nextStep();
        setBuild(Coord.convertStringToCoord("C1"), Level.DOME);
        after = model.getBoard();
        assertEquals(before, after);

        //Build the correct one
        nextStep();
        setBuild(Coord.convertStringToCoord("C1"), Level.LVL2);

        //Check what changed
        after = model.getBoard();
        assertFalse(after.getSpace(Coord.convertStringToCoord("C1")).isOccupied());
        assertFalse(after.getSpace(Coord.convertStringToCoord("C1")).isDome());
        assertSame(after.getSpace(Coord.convertStringToCoord("C1")).getHeight(), Level.LVL2);

        //Check what didn't change
        List<Coord> allCoord = after.getAllCoord();
        boolean removed = allCoord.remove(Coord.convertStringToCoord("C1"));
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
        assertEquals(beforeWorkers, afterWorkers);
    }

    @Test
    public void canBuildDome() {
        //Pre-action
        nextStep();
        model.setWorkerChoice(Coord.convertStringToCoord("B1"));
        nextStep();
        setMove(Coord.convertStringToCoord("B2"));
        before = model.getBoard();
        Board after;

        //Try to build the wrong level
        nextStep();
        setBuild(Coord.convertStringToCoord("C2"), Level.GROUND);
        after = model.getBoard();
        assertEquals(before, after);

        //Try to build the wrong level
        nextStep();
        setBuild(Coord.convertStringToCoord("C2"), Level.LVL1);
        after = model.getBoard();
        assertEquals(before, after);

        //Try to build the wrong level
        nextStep();
        setBuild(Coord.convertStringToCoord("C2"), Level.LVL2);
        after = model.getBoard();
        assertEquals(before, after);

        //Try to build the wrong level
        nextStep();
        setBuild(Coord.convertStringToCoord("C2"), Level.LVL3);
        after = model.getBoard();
        assertEquals(before, after);

        //Build the correct one
        nextStep();
        setBuild(Coord.convertStringToCoord("C2"), Level.DOME);

        //Check what changed
        after = model.getBoard();
        assertFalse(after.getSpace(Coord.convertStringToCoord("C2")).isOccupied());
        assertTrue(after.getSpace(Coord.convertStringToCoord("C2")).isDome());
        assertSame(after.getSpace(Coord.convertStringToCoord("C2")).getHeight(), Level.LVL3);

        //Check what didn't change
        List<Coord> allCoord = after.getAllCoord();
        boolean removed = allCoord.remove(Coord.convertStringToCoord("C2"));
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
        assertEquals(beforeWorkers, afterWorkers);
    }

}

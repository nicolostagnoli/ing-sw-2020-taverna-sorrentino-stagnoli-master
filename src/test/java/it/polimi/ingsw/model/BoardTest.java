package it.polimi.ingsw.model;

import it.polimi.ingsw.exceptions.model.*;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class BoardTest {


    @Test
    public void getAllCoordTest() {
        List<Coord> coords = new Board().getAllCoord();
        assertEquals(Board.BOARD_SIZE*Board.BOARD_SIZE, coords.size());
        for (int i = 0; i < Board.BOARD_SIZE; i++) {
            for (int j = 0; j < Board.BOARD_SIZE; j++) {
                assertTrue(coords.contains(new Coord(i,j)));
            }
        }

    }
    @Test ( expected = InvalidCoordinatesException.class )
    public void getNonExistingSpace() {
        Coord c = new Coord(0,8);
        Board b = new Board();
        b.getSpace(c);
    }


    //Test getWorkerByPosition() returns the correct workers for each position
    @Test
    public void getWorkerByPositionTest() throws WorkerNotFoundException {
        Board b = new Board();
        Player p1 = new Player("Lucio");
        Player p2 = new Player("Asdrogonio");
        Player p3 = new Player("Timburlaldo");
        b.addWorker(p1.getWorker(0));
        b.addWorker(p1.getWorker(1));
        b.addWorker(p2.getWorker(0));
        b.addWorker(p2.getWorker(1));
        b.addWorker(p3.getWorker(0));
        b.addWorker(p3.getWorker(1));

        b.initializeWorker(p1, new Coord(0, 0));
        b.initializeWorker(p1, new Coord(1, 0));
        b.initializeWorker(p2, new Coord(0, 1));
        b.initializeWorker(p2, new Coord(1, 1));
        b.initializeWorker(p3, new Coord(2, 0));
        b.initializeWorker(p3, new Coord(2, 1));

        //check the returned worker is the same worker on that position
        assertSame(b.getWorkerByPosition(new Coord(0, 0)), p1.getWorker(0));
        assertSame(b.getWorkerByPosition(new Coord(1, 0)), p1.getWorker(1));
        assertSame(b.getWorkerByPosition(new Coord(0, 1)), p2.getWorker(0));
        assertSame(b.getWorkerByPosition(new Coord(1, 1)), p2.getWorker(1));
        assertSame(b.getWorkerByPosition(new Coord(2, 0)), p3.getWorker(0));
        assertSame(b.getWorkerByPosition(new Coord(2, 1)), p3.getWorker(1));

    }

    @Test ( expected = WorkerNotFoundException.class )
    public void getWorkerByInvalidPosition() {
        Coord c = new Coord(6,2);
        Board b = new Board();
        b.getWorkerByPosition(c);
    }

    @Test ( expected = WorkerNotFoundException.class )
    public void getWorkerByFreePosition() {
        Coord c = new Coord(3,3);
        Board b = new Board();
        b.getWorkerByPosition(c);
    }

    @Test
    public void getUnoccupiedSpacesTest() {
        Board b = new Board();
        Player p1 = new Player("Lucio");
        Player p2 = new Player("Asdrogonio");
        Player p3 = new Player("Timburlaldo");
        b.addWorker(p1.getWorker(0));
        b.addWorker(p1.getWorker(1));
        b.addWorker(p2.getWorker(0));
        b.addWorker(p2.getWorker(1));
        b.addWorker(p3.getWorker(0));
        b.addWorker(p3.getWorker(1));

        //when board is created, it is empty and the count of unoccupied spaces should be 25
        assertEquals(b.getUnoccupiedPositions().size(), 25);

        b.initializeWorker(p1, new Coord(0, 0));
        assertEquals(b.getUnoccupiedPositions().size(), 24);
        b.initializeWorker(p1, new Coord(1, 0));
        assertEquals(b.getUnoccupiedPositions().size(), 23);
        b.initializeWorker(p2, new Coord(0, 1));
        assertEquals(b.getUnoccupiedPositions().size(), 22);
        b.initializeWorker(p2, new Coord(1, 1));
        assertEquals(b.getUnoccupiedPositions().size(), 21);
        b.initializeWorker(p3, new Coord(2, 0));
        assertEquals(b.getUnoccupiedPositions().size(), 20);
        b.initializeWorker(p3, new Coord(2, 1));
        assertEquals(b.getUnoccupiedPositions().size(), 19);
    }

    //Test adding an already added worker throws an exception
    @Test ( expected = IllegalStateException.class )
    public void addAlreadyAddedWorker() throws IllegalStateException {
        Board b = new Board();
        Player p1 = new Player("Lucio");
        Player p2 = new Player("Asdrogonio");

        b.addWorker(p1.getWorker(0));
        b.addWorker(p1.getWorker(1));
        b.addWorker(p2.getWorker(0));
        b.addWorker(p2.getWorker(1));

        //Here the exception is thrown:
        b.addWorker(p1.getWorker(0));
    }

    @Test
    public void playerInitializesTooManyTimes() {
        Board b = new Board();
        Player p = new Player("Lucio");
        b.addWorker(p.getWorker(0));
        b.addWorker(p.getWorker(1));
        b.initializeWorker(p, new Coord(0, 0));
        b.initializeWorker(p, new Coord(0, 1));

        try {
            b.initializeWorker(p, new Coord(0, 2));
        } catch (IllegalStateException ignored) {}
    }

    //test that after initialization all workers previously added to the board have valid coordinates
    @Test
    public void noNullPositionsOfWorkersAfterInitialization() throws IllegalArgumentException, IllegalStateException {
        Board b = new Board();
        Player p1 = new Player("Lucio");
        Player p2 = new Player("Asdrogonio");
        Player p3 = new Player("DSSDSD");
        b.addWorker(p1.getWorker(0));
        b.addWorker(p1.getWorker(1));
        b.addWorker(p2.getWorker(0));
        b.addWorker(p2.getWorker(1));
        b.addWorker(p3.getWorker(0));
        b.addWorker(p3.getWorker(1));

        b.initializeWorker(p1, new Coord(0, 0));
        b.initializeWorker(p1, new Coord(1, 0));
        b.initializeWorker(p2, new Coord(1, 1));
        b.initializeWorker(p2, new Coord(2, 0));
        b.initializeWorker(p3, new Coord(3, 0));
        b.initializeWorker(p3, new Coord(3, 4));

        for(Worker w : b.getAllWorkers()){
            assertNotNull(w.getPosition());
            assertTrue(Coord.validCoord(w.getPosition()));
        }
    }

    //test exception is thrown when initializing on already occupied space
    @Test ( expected = IllegalStateException.class )
    public void forceInitializationOnOccupiedSpace() throws IllegalArgumentException, IllegalStateException {
        Board b = new Board();
        Player p = new Player("Lucio");
        b.addWorker(p.getWorker(0));
        b.addWorker(p.getWorker(1));

        b.initializeWorker(p, new Coord(0, 0));
        b.initializeWorker(p, new Coord(0, 0));
    }

    //test exception is thrown when trying to move a worker that has not been initialized
    @Test ( expected = IllegalWorkerActionException.class )
    public void cannotMoveUninitializedWorker() throws InvalidCoordinatesException, SpaceFullException, SpaceOccupiedException, IllegalWorkerActionException{
        Board b = new Board();
        Player p1 = new Player("Lucio");

        b.addWorker(p1.getWorker(0));
        b.addWorker(p1.getWorker(1));


        //initialize workers
        b.initializeWorker(p1, new Coord(0, 0));

        b.workerMove(p1.getWorker(1), new Coord(2, 0));
    }

    @Test
    public void workerMoveTest() throws InvalidCoordinatesException, SpaceFullException,
            SpaceOccupiedException, IllegalWorkerActionException {

        Random rand = new Random();
        Board b = new Board();
        Player p1 = new Player("Lucio");
        Player p2 = new Player("Asdrogonio");
        Player p3 = new Player("Timburlaldo");
        b.addWorker(p1.getWorker(0));
        b.addWorker(p1.getWorker(1));
        b.addWorker(p2.getWorker(0));
        b.addWorker(p2.getWorker(1));
        b.addWorker(p3.getWorker(0));
        b.addWorker(p3.getWorker(1));

        //initialize workers
        b.initializeWorker(p1, new Coord(0, 0));
        b.initializeWorker(p1, new Coord(1, 0));
        b.initializeWorker(p2, new Coord(0, 1));
        b.initializeWorker(p2, new Coord(1, 1));
        b.initializeWorker(p3, new Coord(2, 0));
        b.initializeWorker(p3, new Coord(2, 1));

        for (int j = 0; j < 10000; j++) {
            //pick a random worker
            int i = rand.nextInt(6);
            Worker randomWorker = b.getAllWorkers()[i];

            Coord randomMove = new Coord(rand.nextInt(5), rand.nextInt(5));
            if (b.getSpace(randomMove).isOccupied() || b.getSpace(randomMove).isDome()) {
                continue;
            }
            b.workerMove(randomWorker, randomMove); //This doesn't throw an exception because the
                                                    //destination space is free and not dome
        }
    }

    @Test (expected = SpaceOccupiedException.class)
    public void workerMoveOnOccupiedSpace() {
        Board b = new Board();
        Player p = new Player("Lucio");
        b.addWorker(p.getWorker(0));
        b.addWorker(p.getWorker(1));
        b.initializeWorker(p, new Coord(0, 0));
        b.initializeWorker(p, new Coord(1, 0));

        //Here the exception is thrown:
        b.workerMove(p.getWorker(1), new Coord(0,0));
    }

    @Test (expected = SpaceFullException.class)
    public void workerMoveOnSpaceWithDome() {
        Board b = new Board();
        Player p = new Player("Lucio");
        b.addWorker(p.getWorker(0));
        b.addWorker(p.getWorker(1));
        b.initializeWorker(p, new Coord(0, 0));
        b.initializeWorker(p, new Coord(1, 0));
        b.workerBuild(p.getWorker(1), new Coord(2,0), Level.DOME);
        /* First row:
        | W | W | ^ |   |   |
         */

        //Here the exception is thrown:
        b.workerMove(p.getWorker(1), new Coord(2,0));
    }

    @Test (expected = InvalidCoordinatesException.class)
    public void workerMoveOnInvalidSpace() {
        Board b = new Board();
        Player p = new Player("Lucio");
        b.addWorker(p.getWorker(0));
        b.addWorker(p.getWorker(1));
        b.initializeWorker(p, new Coord(0, 0));
        b.initializeWorker(p, new Coord(1, 0));

        //Here the exception is thrown:
        b.workerMove(p.getWorker(0), new Coord(7,4));
    }

    @Test (expected = IllegalWorkerActionException.class)
    public void workerMove_WorkerNotAdded() {
        Board b = new Board();
        Player p = new Player("Lucio");
        b.addWorker(p.getWorker(0));

        //Here the exception is thrown:
        b.workerMove(p.getWorker(1), new Coord(2,2));
    }

    @Test
    public void workerForceMoveTest() throws InvalidCoordinatesException, SpaceFullException,
            SpaceOccupiedException, IllegalWorkerActionException {

        Random rand = new Random();
        Board b = new Board();
        Player p1 = new Player("Lucio");
        Player p2 = new Player("Asdrogonio");
        Player p3 = new Player("Timburlaldo");
        b.addWorker(p1.getWorker(0));
        b.addWorker(p1.getWorker(1));
        b.addWorker(p2.getWorker(0));
        b.addWorker(p2.getWorker(1));
        b.addWorker(p3.getWorker(0));
        b.addWorker(p3.getWorker(1));

        //initialize workers
        b.initializeWorker(p1, new Coord(0, 0));
        b.initializeWorker(p1, new Coord(1, 0));
        b.initializeWorker(p2, new Coord(0, 1));
        b.initializeWorker(p2, new Coord(1, 1));
        b.initializeWorker(p3, new Coord(2, 0));
        b.initializeWorker(p3, new Coord(2, 1));

        for (int j = 0; j < 10; j++) {
            //pick a random worker
            int i = rand.nextInt(6);
            Worker randomWorker = b.getAllWorkers()[i];

            Coord randomMove = new Coord(rand.nextInt(5), rand.nextInt(5));
            while (!b.getSpace(randomMove).isOccupied() ||
                    randomMove.equals(randomWorker.getPosition())) {
                randomMove = new Coord(rand.nextInt(5), rand.nextInt(5));
            }
            Coord randomForceDest = new Coord(rand.nextInt(5), rand.nextInt(5));
            while (b.getSpace(randomForceDest).isOccupied() ||
                    b.getSpace(randomForceDest).isDome() ||
                    randomForceDest.equals(randomMove)) {
                randomForceDest = new Coord(rand.nextInt(5), rand.nextInt(5));
            }
            b.workerForceMove(randomWorker, randomMove, randomForceDest);
        }
    }

    @Test (expected = InvalidCoordinatesException.class)
    public void workerForceMoveOnInvalidSpace() {
        Board b = new Board();
        Player p = new Player("Lucio");
        b.addWorker(p.getWorker(0));
        b.addWorker(p.getWorker(1));
        b.initializeWorker(p, new Coord(0, 0));
        b.initializeWorker(p, new Coord(1, 0));

        //Here the exception is thrown:
        b.workerForceMove(p.getWorker(0), new Coord(7,4), new Coord(1,1));
    }

    @Test (expected = IllegalWorkerActionException.class)
    public void workerForceMove_WorkerNotAdded() {
        Board b = new Board();
        Player p = new Player("Lucio");
        b.addWorker(p.getWorker(0));

        //Here the exception is thrown:
        b.workerForceMove(p.getWorker(1), new Coord(2,2), new Coord(1,1));
    }

    @Test (expected = IllegalWorkerActionException.class)
    public void workerForceMove_WorkerNotInitialized() {
        Board b = new Board();
        Player p = new Player("Lucio");
        b.addWorker(p.getWorker(0));

        //Here the exception is thrown:
        b.workerForceMove(p.getWorker(0), new Coord(2,2), new Coord(1,1));
    }

    @Test (expected = InvalidCoordinatesException.class)
    public void workerBuildOnInvalidSpace() {
        Board b = new Board();
        Player p = new Player("Lucio");
        b.addWorker(p.getWorker(0));
        b.addWorker(p.getWorker(1));
        b.initializeWorker(p, new Coord(0, 0));
        b.initializeWorker(p, new Coord(1, 0));

        //Here the exception is thrown:
        b.workerBuild(p.getWorker(0), new Coord(7,4), Level.LVL3);
    }

    @Test (expected = IllegalWorkerActionException.class)
    public void workerBuild_WorkerNotAdded() {
        Board b = new Board();
        Player p = new Player("Lucio");
        b.addWorker(p.getWorker(0));

        //Here the exception is thrown:
        b.workerBuild(p.getWorker(1), new Coord(1,1), Level.LVL3);
    }

    @Test (expected = IllegalWorkerActionException.class)
    public void workerBuild_WorkerNotInitialized() {
        Board b = new Board();
        Player p = new Player("Lucio");
        b.addWorker(p.getWorker(0));

        //Here the exception is thrown:
        b.workerBuild(p.getWorker(0), new Coord(1,1), Level.LVL3);
    }



    //Test Board.getBuildableSpaceAround method does not give occupied (there is a worker) or full (there is a Dome) space coordinates
    /*
    @Test
    public void notBuildOnOccupiedOrFullSpace() throws InvalidCoordinatesException, SpaceFullException, SpaceOccupiedException, IllegalWorkerActionException{

        Random rand = new Random();
        Board b = new Board();
        Player p1 = new Player("Lucio");
        Player p2 = new Player("Asdrogonio");
        Player p3 = new Player("MamboLosco");
        b.addWorker(p1.getWorker(0));
        b.addWorker(p1.getWorker(1));
        b.addWorker(p2.getWorker(0));
        b.addWorker(p2.getWorker(1));
        b.addWorker(p3.getWorker(0));
        b.addWorker(p3.getWorker(1));

        //initialize workers
        b.initializeWorker(p1.getWorker(0), new Coord(0, 0));
        b.initializeWorker(p1.getWorker(1), new Coord(1, 0));
        b.initializeWorker(p2.getWorker(0), new Coord(0, 1));
        b.initializeWorker(p2.getWorker(1), new Coord(1, 1));
        b.initializeWorker(p3.getWorker(0), new Coord(2, 0));
        b.initializeWorker(p3.getWorker(1), new Coord(2, 1));

        for (int j = 0; j < 150; j++) {
            //pick a random worker
            int i = rand.nextInt(6);
            Worker randomWorker = b.getAllWorkers()[i];
            List<Coord> builds = b.getBuildableSpacesAround(randomWorker.getPosition());

            //if this worker cannot build, skip
            if(builds.size() == 0){
                continue;
            }

            Coord build = builds.get(rand.nextInt(builds.size()));
            b.workerBuild(randomWorker, build, Level.values()[b.getSpace(build).getHeight().ordinal() + 1]);
        }
    }
     */

    @Test
    public void nonEmptyRepresentation() {
        Board b = new Board();
        Player p1 = new Player("Lucio");
        Player p2 = new Player("Asdrogonio");
        Player p3 = new Player("MamboLosco");
        p1.setWorkerColor(Color.BLUE);
        p2.setWorkerColor(Color.RED);
        p3.setWorkerColor(Color.YELLOW);
        p1.getWorker(0).setColor(Color.BLUE);
        p1.getWorker(1).setColor(Color.BLUE);
        p2.getWorker(0).setColor(Color.RED);
        p2.getWorker(1).setColor(Color.RED);
        p3.getWorker(0).setColor(Color.YELLOW);
        p3.getWorker(1).setColor(Color.YELLOW);
        b.addWorker(p1.getWorker(0));
        b.addWorker(p1.getWorker(1));
        b.addWorker(p2.getWorker(0));
        b.addWorker(p2.getWorker(1));
        b.addWorker(p3.getWorker(0));
        b.addWorker(p3.getWorker(1));

        //initialize workers
        b.initializeWorker(p1, new Coord(0, 0));
        b.initializeWorker(p1, new Coord(1, 0));
        b.initializeWorker(p2, new Coord(0, 1));
        b.initializeWorker(p2, new Coord(1, 1));
        b.initializeWorker(p3, new Coord(2, 0));
        b.initializeWorker(p3, new Coord(2, 1));

        //Populating the board
        b.workerBuild(p1.getWorker(0), new Coord(3,3), Level.LVL1);
        b.workerBuild(p1.getWorker(0), new Coord(3,4), Level.LVL2);
        b.workerBuild(p1.getWorker(0), new Coord(4,4), Level.LVL3);
        b.workerBuild(p1.getWorker(0), new Coord(4,2), Level.DOME);

        assertNotEquals("", b.toString());
    }
}
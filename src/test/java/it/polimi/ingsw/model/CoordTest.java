package it.polimi.ingsw.model;

import it.polimi.ingsw.exceptions.model.InvalidCoordinatesException;
import it.polimi.ingsw.exceptions.model.WorkerNotFoundException;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CoordTest {

    @Test
    public void invalidCoordinatesTest() {
        Coord c1 = new Coord(-1, 0);
        Coord c2 = new Coord(0, -1);
        Coord c3 = new Coord(-1, -1);
        Coord c4 = new Coord(5, 5);
        Coord c5 = new Coord(4, 5);
        Coord c6 = new Coord(5, 4);

        assertFalse(Coord.validCoord(c1));
        assertFalse(Coord.validCoord(c2));
        assertFalse(Coord.validCoord(c3));
        assertFalse(Coord.validCoord(c4));
        assertFalse(Coord.validCoord(c5));
        assertFalse(Coord.validCoord(c6));
    }

    @Test
    public void isNearTest() throws InvalidCoordinatesException {
        Coord center = new Coord(2, 2);
        Coord up = new Coord(2, 3);
        Coord upLeft = new Coord(1, 3);
        Coord upRight = new Coord(3, 3);
        Coord bottom = new Coord(2, 1);
        Coord bottomLeft = new Coord(1, 1);
        Coord bottomRight = new Coord(3, 1);
        Coord left = new Coord(1, 2);
        Coord right = new Coord(3, 2);

        assertTrue(!center.isNear(center) );
        assertTrue( center.isNear(up) );
        assertTrue( center.isNear(upLeft) );
        assertTrue( center.isNear(upRight) );
        assertTrue( center.isNear(bottom) );
        assertTrue( center.isNear(bottomLeft) );
        assertTrue( center.isNear(bottomRight) );
        assertTrue( center.isNear(left) );
        assertTrue( center.isNear(right) );
    }

    @Test
    public void isNearTest_exceptions() {
        Coord c1 = new Coord(-1, 0);
        Coord c2 = new Coord(0, 0);
        try {
            c1.isNear(c2);
        } catch (InvalidCoordinatesException ignored) {}
        try {
            c2.isNear(c1);
        } catch (InvalidCoordinatesException ignored) {}
    }

    @Test
    public void convertStringToCoordTest(){
        String topLeft = "A1";
        String topRight = "A5";
        String bottomLeft = "E1";
        String bottomRight = "E5";
        String center = "C3";

        assertTrue(Coord.convertStringToCoord(topLeft).equals(new Coord(0,0)));
        assertTrue (Coord.convertStringToCoord(topRight).equals(new Coord(4,0)));
        assertTrue (Coord.convertStringToCoord(bottomLeft).equals(new Coord(0,4)));
        assertTrue (Coord.convertStringToCoord(bottomRight).equals(new Coord(4,4)));
        assertTrue (Coord.convertStringToCoord(center).equals(new Coord(2,2)));
    }

    @Test
    public void convertStringToCoordTest_exceptions() {
        try {
            Coord.convertStringToCoord("A5C");
        } catch (IllegalArgumentException ignored) {}
        try {
            Coord.convertStringToCoord("F1");
        } catch (IllegalArgumentException ignored) {}
    }
}

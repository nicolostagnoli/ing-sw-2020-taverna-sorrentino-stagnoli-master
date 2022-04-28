package it.polimi.ingsw.model;

import it.polimi.ingsw.exceptions.model.SpaceFullException;
import it.polimi.ingsw.exceptions.model.SpaceOccupiedException;
import org.junit.Test;

import static org.junit.Assert.*;

public class SpaceTest {

    @Test
    public void correctInitializationTest(){
        Space s = new Space();

        assert (s.getHeight().equals(Level.GROUND));
        assert (!s.isOccupied());
        assert (!s.isDome());
    }

    @Test
    public void setOccupiedTest(){
        Space s = new Space();

        s.setOccupied();

        assert (s.isOccupied());
    }

    @Test
    public void setUnoccupiedTest(){
        Space s = new Space();

        s.setOccupied();
        s.setUnoccupied();

        assert (!s.isOccupied());
    }

    @Test (expected = SpaceFullException.class)
    public void destroyDomeTest(){
        Space s = new Space();
        s.setLevel(Level.DOME);

        s.setLevel(Level.LVL2);
    }

    @Test
    public void setLevelTest(){
        Space s = new Space();
        s.setLevel(Level.LVL1);
        assertEquals(Level.LVL1, s.getHeight());
    }

    @Test
    public void cannotChangeLevelOnDomeSpace() {
        Space s = new Space();
        s.setLevel(Level.DOME);

        for (Level l : Level.values()) {
            try {
                s.setLevel(l);
                fail();
            } catch (SpaceFullException ignored) {}
        }
    }

    @Test
    public void levelUpTest() {
        Space s = new Space();
        s.setLevel(Level.LVL1);
        assertEquals(Level.LVL1, s.getHeight());
        s.setLevel(Level.LVL2);
        assertEquals(Level.LVL2, s.getHeight());
        s.setLevel(Level.LVL3);
        assertEquals(Level.LVL3, s.getHeight());
        s.setLevel(Level.GROUND);
        assertEquals(Level.GROUND, s.getHeight());
        s.setLevel(Level.DOME);
        assertTrue(s.isDome());
    }

    @Test
    public void nonEmptyRepresentation() {
        Space s = new Space();
        assertNotNull(s.toString());
        assertNotEquals("", s.toString());
    }
}

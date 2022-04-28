package it.polimi.ingsw.model;

import org.junit.Test;

public class WorkerTest {

    @Test (expected = IllegalArgumentException.class)
    public void wrongNewPositionTest(){
        Worker w = new Worker(new Player("Pippo"));

        w.setPosition(new Coord(10, 10));
    }

    @Test
    public void setPositionTest(){
        Worker w = new Worker(new Player("Pippo"));
        assert (w.getPosition() == null);
        w.setPosition(new Coord(1, 2));
        assert (w.getPosition().equals(new Coord(1, 2)));
    }

    @Test
    public void setColorTest(){
        Worker w = new Worker(new Player("Pippo"));
        w.setColor(Color.BLUE);
        assert (w.getColor().equals(Color.BLUE));
    }

    @Test
    public void correctInitTest(){
        Worker w = new Worker(new Player("Pippo"));

        assert (w.getPlayerNickname().equals("Pippo"));
        assert (w.getPosition() == null);
    }
}

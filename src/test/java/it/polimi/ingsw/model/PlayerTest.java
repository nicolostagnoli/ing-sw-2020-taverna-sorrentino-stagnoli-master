package it.polimi.ingsw.model;

import org.junit.Test;

import static org.junit.Assert.assertNull;

public class PlayerTest {
    @Test
    public void cloneTest() {
        Player p = new Player("A");
        Player p2 = p.clone();
        p2.setGod(new God("Name", "Description"));
        assertNull(p.getGod());
    }
}

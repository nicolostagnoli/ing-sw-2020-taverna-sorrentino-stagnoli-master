package it.polimi.ingsw.model;

import it.polimi.ingsw.exceptions.model.AlreadyExistingPlayerException;
import it.polimi.ingsw.exceptions.model.GameFullException;
import it.polimi.ingsw.model.exceptions.PrepareModelException;
import it.polimi.ingsw.server.Connection;
import it.polimi.ingsw.view.RemotePlayerView;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class GameModelTest {

    @Test( expected = GameFullException.class )
    public void gameFullTest(){
        GameModel m;
        try {
            m = new GameModel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();
            return;
        }
        m.setNumPlayers(3);

        m.addNewPlayer(new Player("AA"));
        m.addNewPlayer(new Player("BB"));
        m.addNewPlayer(new Player("CC"));

        assertTrue(m.allPlayersArrived());

        m.addNewPlayer(new Player("DD"));
    }

    @Test( expected = AlreadyExistingPlayerException.class )
    public void alreadyExistingPlayerTest(){
        GameModel m;
        try {
            m = new GameModel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();
            return;
        }
        m.setNumPlayers(3);

        m.addNewPlayer(new Player("AA"));
        m.addNewPlayer(new Player("AA"));
    }

    @Test
    public void allPlayersHaveDifferentGodAndColor(){
        GameModel m;
        try {
            m = new GameModel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail();
            return;
        }
        m.setNumPlayers(3);

        m.addNewPlayer(new Player("AA"));
        m.addNewPlayer(new Player("BB"));
        m.addNewPlayer(new Player("CC"));

        assertTrue(m.allPlayersArrived());

        //assert different colors
        ArrayList<Color> colors = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Color c = m.getCurrentPlayer().getWorkerColor();
            assertFalse(colors.contains(c));
            colors.add(c);
            m.nextPlayer();
        }

        //assert challenger is first in queue
        assertEquals("AA", m.getCurrentPlayer().getNickname());

        //assert gods set correctly
        ArrayList<String> gods = new ArrayList<>();
        gods.add("Athena");
        gods.add("Demeter");
        gods.add("Artemis");
        m.setGods(gods);
        assertTrue(gods.containsAll(m.getAvailableGods().stream().map(God::getName).collect(Collectors.toList())));

        //assert current player is the second
        assertEquals("BB", m.getCurrentPlayer().getNickname());

        //assignGod removes selected god from the list
        m.assignGodToPlayer(m.getCurrentPlayer(), m.getAvailableGods().get(0) );
        m.assignGodToPlayer(m.getCurrentPlayer(), m.getAvailableGods().get(0) );
        m.assignGodToPlayer(m.getCurrentPlayer(), m.getAvailableGods().get(0) );

        //assert challenger is still the first in queue
        assertEquals("AA", m.getCurrentPlayer().getNickname());

        //assert different gods
        ArrayList<String> assignedGods = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            String g = m.getCurrentPlayer().getGod().getName();
            assertFalse(assignedGods.contains(g));
            assignedGods.add(g);
            m.nextPlayer();
        }
    }

    @Test (expected = IllegalStateException.class)
    public void cannotAdvanceWithPartialQueue() throws FileNotFoundException {
        GameModel model = new GameModel();
        model.setNumPlayers(2);
        model.addNewPlayer(new Player("A"));
        model.nextPlayer();
    }

    @Test
    public void addListenerTest() throws FileNotFoundException {
        GameModel model = new GameModel();
        model.addListener(new RemotePlayerView("A", new Connection(null, null, null)));
    }

    @Test
    public void playerLost() throws PrepareModelException {
        GameModel model = TestUtils.prepareModel("no_actions_test");
        nextStep(model);
        model.setWorkerChoice(Coord.convertStringToCoord("B2"));
        nextStep(model);
        model.setMove(Coord.convertStringToCoord("A1"));
        nextStep(model);
        assertEquals(2, model.getNumPlayers()); // A player has gone... because he lost
    }

    @Test
    public void newCycleAndNewTurn() throws PrepareModelException {
        GameModel model = TestUtils.prepareModel("base_test");
        assertTrue(model.hasNewCycleBegun());
        assertTrue(model.hasNewTurnBegun());
    }

    @Test
    public void getAllPlayersTest() throws PrepareModelException {
        GameModel model = TestUtils.prepareModel("base_test");
        assertEquals(3, model.getPlayers().size());
        assertEquals(3, model.getPlayersNicknames().size());
    }

    @Test
    public void getPlayerByNicknameTest() throws PrepareModelException {
        GameModel model = TestUtils.prepareModel("base_test");
        assertEquals("Player1", model.getPlayerByNickname("Player1").getNickname());
    }

    private void nextStep(GameModel model) {
        try {
            model.nextStep();
        } catch (RuntimeException ignored) {}
    }
}

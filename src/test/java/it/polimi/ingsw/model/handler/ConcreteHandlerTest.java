package it.polimi.ingsw.model.handler;

import it.polimi.ingsw.exceptions.model.handler.RuleParserException;
import it.polimi.ingsw.exceptions.model.handler.UndeterminedSpaceException;
import it.polimi.ingsw.model.Board;
import it.polimi.ingsw.model.Coord;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

public class ConcreteHandlerTest {

    @Test (expected = UndeterminedSpaceException.class)
    public void notAllSpacesValidated() throws FileNotFoundException, RuleParserException {
        RuleParser rp = new RuleParser("rules/standard/build_up");
                                    // Build-up rules don't validate any space for a "move" action
        rp.parse();

        RequestHandler rh = new HandlerAdapter(new ConcreteHandler(rp.getRules()));
        Board board = new Board();
        rh.getValidSpaces(new Coord(2,2), board, new ArrayList<>(), new HashMap<>(), new HashMap<>());
    }
}

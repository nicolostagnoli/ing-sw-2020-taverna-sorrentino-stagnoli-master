package it.polimi.ingsw.model.handler;

import org.junit.Test;


public class RequestHandlerCreatorTest {

    @Test (expected = NullPointerException.class)
    public void invalidGod() {
        String god = "this_god_doesnt_exist";
        new EnhancedRequestHandlerCreator(god).createHandler();
    }

    @Test
    public void handlerForNoGod() {
        new EnhancedRequestHandlerCreator().createHandler();
    }
}

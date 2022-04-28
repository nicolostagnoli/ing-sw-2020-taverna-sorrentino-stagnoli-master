package it.polimi.ingsw.model.handler.util;

import org.junit.Test;

public class PairTest {

    @Test (expected = IllegalArgumentException.class)
    public void noThirdElement() {
        Pair<Object> pair = new Pair<>(new Object(), new Object());
        pair.get(2);
    }
}

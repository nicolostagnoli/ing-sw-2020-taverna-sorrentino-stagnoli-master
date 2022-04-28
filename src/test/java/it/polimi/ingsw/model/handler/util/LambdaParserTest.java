package it.polimi.ingsw.model.handler.util;

import it.polimi.ingsw.exceptions.model.handler.RuleParserException;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.exceptions.PrepareModelException;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class LambdaParserTest {

    private static Board board;

    @BeforeClass
    public static void loadBoard() throws PrepareModelException {
        GameModel model = TestUtils.prepareModel("base_test");
        board = model.getBoard();
    }


    @Test
    public void parseArgumentTest_basics() throws RuleParserException {
        String s = "one";
        List<String> result = LambdaParser.parseArguments(s);
        assertEquals(Collections.singletonList("one"), result);

        s = "one, two, three";
        result = LambdaParser.parseArguments(s);
        assertEquals(Arrays.asList("one", "two", "three"), result);

        s = " (one  ), (  two) ,(three)  ";
        result = LambdaParser.parseArguments(s);
        assertEquals(Arrays.asList("one", "two", "three"), result);

        s = " funct(one, two) ,(three )";
        result = LambdaParser.parseArguments(s);
        assertEquals(Arrays.asList("funct(one, two)", "three"), result);

        s = "sum(after, coord(-1,0)), sum(before, coord(1,1))";
        result = LambdaParser.parseArguments(s);
        assertEquals(Arrays.asList("sum(after, coord(-1,0))", "sum(before, coord(1,1))"), result);
    }

    @Test (expected = RuleParserException.class)
    public void parseArgumentTest_NoEmptyArgument() throws RuleParserException {
        String s = "one,   ,  two, three";
        LambdaParser.parseArguments(s);
    }

    @Test
    public void reduceParenthesesTest() throws RuleParserException {
        String s = "(f1(g(a), h(b), k(m(c), n(d)))), (f2(e))";
        String result = LambdaParser.reduceParentheses(s);
        assertEquals("(f1(g(a), h(b), k(m(c), n(d)))), (f2(e))", result);
    }

    @Test
    public void getFunctionAndArgumentTest() throws RuleParserException {
        String s = "f(1, 2)";
        assertEquals("f", LambdaParser.getFunction(s));
        assertEquals("(1, 2)", LambdaParser.getArgument(s));
    }

    @Test
    public void fromCoordToSymoblicFunctionTest() throws RuleParserException {
        String s = "coord(3,4)";
        BiFunction<Pair<Coord>, Pair<Coord>, Coord> result =
                LambdaParser.fromCoordToSymbolicFunction(s);
        Random r = new Random();

        for (int i = 0; i < 10; i++) {
            Coord a = new Coord(r.nextInt(5), r.nextInt(5));
            Coord b = new Coord(r.nextInt(5), r.nextInt(5));
            Coord c = new Coord(r.nextInt(5), r.nextInt(5));
            Coord d = new Coord(r.nextInt(5), r.nextInt(5));

            assertEquals(new Coord(3, 4), result.apply(new Pair<>(a, b), new Pair<>(c, d)));
        }

        s = "oldBefore";
        result = LambdaParser.fromCoordToSymbolicFunction(s);
        for (int i = 0; i < 10; i++) {
            Coord a = new Coord(r.nextInt(5), r.nextInt(5));
            Coord b = new Coord(r.nextInt(5), r.nextInt(5));
            Coord c = new Coord(r.nextInt(5), r.nextInt(5));
            Coord d = new Coord(r.nextInt(5), r.nextInt(5));

            assertEquals(a, result.apply(new Pair<>(a, b), new Pair<>(c, d)));
        }
        s = "oldAfter";
        result = LambdaParser.fromCoordToSymbolicFunction(s);
        for (int i = 0; i < 10; i++) {
            Coord a = new Coord(r.nextInt(5), r.nextInt(5));
            Coord b = new Coord(r.nextInt(5), r.nextInt(5));
            Coord c = new Coord(r.nextInt(5), r.nextInt(5));
            Coord d = new Coord(r.nextInt(5), r.nextInt(5));

            assertEquals(b, result.apply(new Pair<>(a, b), new Pair<>(c, d)));
        }

        s = "before";
        result = LambdaParser.fromCoordToSymbolicFunction(s);
        for (int i = 0; i < 10; i++) {
            Coord a = new Coord(r.nextInt(5), r.nextInt(5));
            Coord b = new Coord(r.nextInt(5), r.nextInt(5));
            Coord c = new Coord(r.nextInt(5), r.nextInt(5));
            Coord d = new Coord(r.nextInt(5), r.nextInt(5));

            assertEquals(c, result.apply(new Pair<>(a, b), new Pair<>(c, d)));
        }

        s = "after";
        result = LambdaParser.fromCoordToSymbolicFunction(s);
        for (int i = 0; i < 10; i++) {
            Coord a = new Coord(r.nextInt(5), r.nextInt(5));
            Coord b = new Coord(r.nextInt(5), r.nextInt(5));
            Coord c = new Coord(r.nextInt(5), r.nextInt(5));
            Coord d = new Coord(r.nextInt(5), r.nextInt(5));

            assertEquals(d, result.apply(new Pair<>(a, b), new Pair<>(c, d)));
        }

        s = "sum(coord(1,2), coord(3,4))";
        result = LambdaParser.fromCoordToSymbolicFunction(s);
        for (int i = 0; i < 10; i++) {
            Coord a = new Coord(r.nextInt(5), r.nextInt(5));
            Coord b = new Coord(r.nextInt(5), r.nextInt(5));
            Coord c = new Coord(r.nextInt(5), r.nextInt(5));
            Coord d = new Coord(r.nextInt(5), r.nextInt(5));

            assertEquals(new Coord(4, 6), result.apply(new Pair<>(a, b), new Pair<>(c, d)));
        }

        s = "diff(coord(4,5), coord(1,2))";
        result = LambdaParser.fromCoordToSymbolicFunction(s);
        for (int i = 0; i < 10; i++) {
            Coord a = new Coord(r.nextInt(5), r.nextInt(5));
            Coord b = new Coord(r.nextInt(5), r.nextInt(5));
            Coord c = new Coord(r.nextInt(5), r.nextInt(5));
            Coord d = new Coord(r.nextInt(5), r.nextInt(5));

            assertEquals(new Coord(3, 3), result.apply(new Pair<>(a, b), new Pair<>(c, d)));
        }
    }

    @Test (expected = RuleParserException.class)
    public void fromCoordToSymbolicFunctionTest_InvalidValue_1() throws RuleParserException {
        String s = "before + 1";
        LambdaParser.fromCoordToSymbolicFunction(s);
    }

    @Test (expected = RuleParserException.class)
    public void fromCoordToSymbolicFunctionTest_InvalidValue_2() throws RuleParserException {
        String s = "(1,0)";
        LambdaParser.fromCoordToSymbolicFunction(s);
    }

    @Test (expected = RuleParserException.class)
    public void getSumFunctionTest_TooManyArguments() throws RuleParserException {
        String s = "before, after, oldBefore";
        LambdaParser.getSumFunction(s);
    }

    @Test (expected = RuleParserException.class)
    public void getDiffFunctionTest_TooManyArguments() throws RuleParserException {
        String s = "before, after, oldBefore";
        LambdaParser.getDiffFunction(s);
    }

    @Test
    public void fromLevelToSymoblicFunctionTest() throws RuleParserException{
        Random r = new Random();

        String s = "LVL1";
        TriFunction<Pair<Coord>, Pair<Coord>, Board, Level> result =
                LambdaParser.fromLevelToSymbolicFunction(s);
        for (int i = 0; i < 10; i++) {
            Coord a = new Coord(r.nextInt(5), r.nextInt(5));
            Coord b = new Coord(r.nextInt(5), r.nextInt(5));
            Coord c = new Coord(r.nextInt(5), r.nextInt(5));
            Coord d = new Coord(r.nextInt(5), r.nextInt(5));

            assertEquals(Level.LVL1,
                    result.apply(new Pair<>(a, b), new Pair<>(c, d), board));
        }

        s = "coord(4,2)";
        result = LambdaParser.fromLevelToSymbolicFunction(s);
        for (int i = 0; i < 10; i++) {
            Coord a = new Coord(r.nextInt(5), r.nextInt(5));
            Coord b = new Coord(r.nextInt(5), r.nextInt(5));
            Coord c = new Coord(r.nextInt(5), r.nextInt(5));
            Coord d = new Coord(r.nextInt(5), r.nextInt(5));

            assertEquals(board.getSpace(new Coord(4, 2)).getHeight(),
                    result.apply(new Pair<>(a, b), new Pair<>(c, d), board));
        }

        s = "oldBefore";
        result = LambdaParser.fromLevelToSymbolicFunction(s);
        for (int i = 0; i < 10; i++) {
            Coord a = new Coord(r.nextInt(5), r.nextInt(5));
            Coord b = new Coord(r.nextInt(5), r.nextInt(5));
            Coord c = new Coord(r.nextInt(5), r.nextInt(5));
            Coord d = new Coord(r.nextInt(5), r.nextInt(5));

            assertEquals(board.getSpace(a).getHeight(),
                    result.apply(new Pair<>(a, b), new Pair<>(c, d), board));
        }

        s = "oldAfter";
        result = LambdaParser.fromLevelToSymbolicFunction(s);
        for (int i = 0; i < 10; i++) {
            Coord a = new Coord(r.nextInt(5), r.nextInt(5));
            Coord b = new Coord(r.nextInt(5), r.nextInt(5));
            Coord c = new Coord(r.nextInt(5), r.nextInt(5));
            Coord d = new Coord(r.nextInt(5), r.nextInt(5));

            assertEquals(board.getSpace(b).getHeight(),
                    result.apply(new Pair<>(a, b), new Pair<>(c, d), board));
        }

        s = "before";
        result = LambdaParser.fromLevelToSymbolicFunction(s);
        for (int i = 0; i < 10; i++) {
            Coord a = new Coord(r.nextInt(5), r.nextInt(5));
            Coord b = new Coord(r.nextInt(5), r.nextInt(5));
            Coord c = new Coord(r.nextInt(5), r.nextInt(5));
            Coord d = new Coord(r.nextInt(5), r.nextInt(5));

            assertEquals(board.getSpace(c).getHeight(),
                    result.apply(new Pair<>(a, b), new Pair<>(c, d), board));
        }

        s = "after";
        result = LambdaParser.fromLevelToSymbolicFunction(s);
        for (int i = 0; i < 10; i++) {
            Coord a = new Coord(r.nextInt(5), r.nextInt(5));
            Coord b = new Coord(r.nextInt(5), r.nextInt(5));
            Coord c = new Coord(r.nextInt(5), r.nextInt(5));
            Coord d = new Coord(r.nextInt(5), r.nextInt(5));

            assertEquals(board.getSpace(d).getHeight(),
                    result.apply(new Pair<>(a, b), new Pair<>(c, d), board));
        }
    }

    @Test (expected = RuleParserException.class)
    public void fromLevelToSymbolicFunction_InvalidValue_1() throws RuleParserException {
        String s = "2";
        LambdaParser.fromLevelToSymbolicFunction(s);
    }

    @Test (expected = RuleParserException.class)
    public void fromLevelToSymbolicFunction_InvalidValue_2() throws RuleParserException {
        String s = "after + 1";
        LambdaParser.fromLevelToSymbolicFunction(s);
    }

    //Testing of public methods:

    @Test
    public void extractCoordFunctionTest() throws RuleParserException {
        String s = "coord(3,0)";
        BiFunction<Pair<Coord>, Pair<Coord>, Coord> result = LambdaParser.extractCoordFunction(s);
        Random r = new Random();

        for (int i = 0; i < 10; i++) {
            Coord a = new Coord(r.nextInt(5), r.nextInt(5));
            Coord b = new Coord(r.nextInt(5), r.nextInt(5));
            Coord c = new Coord(r.nextInt(5), r.nextInt(5));
            Coord d = new Coord(r.nextInt(5), r.nextInt(5));

            assertEquals(new Coord(3, 0), result.apply(new Pair<>(a, b), new Pair<>(c, d)));
        }
    }

    @Test
    public void extractPredicateTest() throws RuleParserException {
        Coord a, b, c, d;
        String s = "occupied after";
        TriPredicate<Pair<Coord>, Pair<Coord>, Board> condition;
        Random r = new Random();
        List<Coord> coordList;
        try {
            LambdaParser.extractPredicate(s);
            fail();
        } catch (RuleParserException ignored) {}
        //----------------------------------------------------------

        s = "samePlayer";
        try {
            LambdaParser.extractPredicate(s);
            fail();
        } catch (RuleParserException ignored) {}
        //----------------------------------------------------------

        s = "occupied(after)";
        condition = LambdaParser.extractPredicate(s);
        coordList = board.getAllCoord();
        coordList.removeAll(board.getUnoccupiedPositions());
        d = coordList.get(r.nextInt(coordList.size()));
        c = new Coord(1,0);
        assertTrue(condition.test(null, new Pair<>(c, d), board));
        //----------------------------------------------------------

        s = "dome(before)";
        condition = LambdaParser.extractPredicate(s);
        coordList = board.getAllCoord().stream().filter(co -> board.getSpace(co).isDome())
                .collect(Collectors.toList());
        c = coordList.get(r.nextInt(coordList.size()));
        d = new Coord(3,3);
        assertTrue(condition.test(null, new Pair<>(c, d), board));
        //----------------------------------------------------------

        s = "near(before, after)";
        condition = LambdaParser.extractPredicate(s);
        c = new Coord(1,2);
        d = new Coord(2,3);
        assertTrue(condition.test(null, new Pair<>(c, d), null));
        c = new Coord(0,2);
        d = new Coord(2,1);
        assertFalse(condition.test(null, new Pair<>(c, d), null));
        //----------------------------------------------------------

        s = "near(before, after, oldBefore)";
        try {
            LambdaParser.extractPredicate(s);
            fail();
        } catch (RuleParserException ignored) {}
        //----------------------------------------------------------

        s = "equalCoordinates(before, after)";
        condition = LambdaParser.extractPredicate(s);
        c = new Coord(2,3);
        d = new Coord(2,3);
        assertTrue(condition.test(null, new Pair<>(c, d), null));
        c = new Coord(0,2);
        d = new Coord(2,1);
        assertFalse(condition.test(null, new Pair<>(c, d), null));
        //----------------------------------------------------------

        s = "equalCoordinates(before, after, oldBefore)";
        try {
            LambdaParser.extractPredicate(s);
            fail();
        } catch (RuleParserException ignored) {}
        //----------------------------------------------------------

        s = "samePlayer(oldBefore, after)";
        condition = LambdaParser.extractPredicate(s);
        String randomNickname = board.getAllWorkersCopy().get(r.nextInt(board.getAllWorkersCopy().size()))
                .getPlayerNickname();
        coordList = board.getAllWorkersCopy().stream()
                .filter(w -> w.getPlayerNickname().equals(randomNickname))
                .map(Worker::getPosition)
                .collect(Collectors.toList());
        a = coordList.get(0);
        d = coordList.get(1);
        assertTrue(condition.test(new Pair<>(a, null), new Pair<>(null, d), board));
        //----------------------------------------------------------

        s = "samePlayer(after, bifour, oldAfter)";
        try {
            LambdaParser.extractPredicate(s);
            fail();
        } catch (RuleParserException ignored) {}
        //----------------------------------------------------------

        for (Coord co : board.getAllCoord()) {
            s = "valid(coord" + co.toString() + ")";
            condition = LambdaParser.extractPredicate(s);
            assertTrue(condition.test(null, null, null));
        }
        List<String> lines = Arrays.asList(
                "valid(coord(5,1))", "valid(coord(1,5))", "valid(coord(0, -1))", "valid(coord(-1, 3))");
        for (String str : lines) {
            condition = LambdaParser.extractPredicate(str);
            assertFalse(condition.test(null, null, null));
        }
        //----------------------------------------------------------

        s = "compareLevels(after, before)";
        try {
            LambdaParser.extractPredicate(s);
            fail();
        } catch (RuleParserException ignored) {}
        //----------------------------------------------------------

        s = "compareLevels(after, before, 1)";
        try {
            LambdaParser.extractPredicate(s);
            fail();
        } catch (RuleParserException ignored) {}
        //----------------------------------------------------------

        s = "compareLevels(before, after, =0)";
        condition = LambdaParser.extractPredicate(s);
        Level randomLevel = Level.values()[r.nextInt(Level.values().length - 1)];
            // "-1" is to avoid DOME
        List<Coord> allCoord = board.getAllCoord();
        assertEquals(25, allCoord.size());
        coordList = allCoord.stream()
                .filter(co -> board.getSpace(co).getHeight() == randomLevel)
                .collect(Collectors.toList());
        c = coordList.get(0);
        d = coordList.get(1);
        assertTrue(condition.test(null, new Pair<>(c, d), board));
        //----------------------------------------------------------

        s = "compareLevels(before, after, >1)";
        condition = LambdaParser.extractPredicate(s);
        coordList = board.getAllCoord();
        d = coordList.stream()
                .filter(co -> board.getSpace(co).getHeight() == Level.LVL3)
                .findAny().orElseThrow();
        c = coordList.stream()
                .filter(co -> board.getSpace(co).getHeight().ordinal() < Level.LVL3.ordinal() - 1)
                .findAny().orElseThrow();
        assertTrue(condition.test(null, new Pair<>(c, d), board));
        //----------------------------------------------------------

        s = "compareLevels(before, after, <2)";
        condition = LambdaParser.extractPredicate(s);
        coordList = board.getAllCoord();
        c = coordList.stream()
                .filter(co -> board.getSpace(co).getHeight() == Level.GROUND)
                .findAny().orElseThrow();
        d = coordList.stream()
                .filter(co -> board.getSpace(co).getHeight().ordinal() < Level.GROUND.ordinal() + 2)
                .findAny().orElseThrow();
        assertTrue(condition.test(null, new Pair<>(c, d), board));
        //----------------------------------------------------------

        String god = board.getWorkerCopy(new Coord(3,3)).getGod();
        s = "sameGod(coord(3,3), \"" + god +"\")";
        condition = LambdaParser.extractPredicate(s);
        assertTrue(condition.test(null, null, board));
        //----------------------------------------------------------

        s = "sameGod(\"Apollo\")";
        try {
            LambdaParser.extractPredicate(s);
            fail();
        } catch (RuleParserException ignored) {}
        //----------------------------------------------------------

        lines = Arrays.asList("occupied(after)", "near(before, after)", "dome(oldBefore)",
                "compareLevels(coord(1,0), oldAfter, =0)", "valid(coord(3,1))");
        for (String str : lines) {
            condition = LambdaParser.extractPredicate(str);
            s = "negate(" + str + ")";
            condition = condition.and(LambdaParser.extractPredicate(s));
            a = new Coord(r.nextInt(5), r.nextInt(5));
            b = new Coord(r.nextInt(5), r.nextInt(5));
            c = new Coord(r.nextInt(5), r.nextInt(5));
            d = new Coord(r.nextInt(5), r.nextInt(5));
            assertFalse(condition.test(new Pair<>(a, b), new Pair<>(c, d), board));
        }
        //----------------------------------------------------------

        List<String> truePredicates = Arrays.asList(
                "valid(coord(2,3))",
                "valid(coord(4,4))",
                "near(coord(0,0), coord(1,1))",
                "compareLevels(GROUND, LVL1, =1)",
                "valid(coord(0,4))",
                "compareLevels(GROUND, LVL3, >2)",
                "negate(near(coord(4,2), coord(2,1)))",
                "compareLevels(LVL1, LVL3, =2)"
        );
        List<String> falsePredicates = Arrays.asList(
                "valid(coord(-1,3))",
                "valid(coord(4,5))",
                "near(coord(0,0), coord(1,3))",
                "compareLevels(GROUND, LVL1, =0)",
                "valid(coord(0,6))",
                "compareLevels(GROUND, LVL3, <2)",
                "negate(near(coord(3,2), coord(4,1)))",
                "compareLevels(LVL1, LVL3, >2)"
        );

        for (int i = 0; i < truePredicates.size(); i++) {
            for (int j = i; j < truePredicates.size(); j++) {
                s = "and(" + truePredicates.get(i) + ", " + truePredicates.get(j) + ")";
                condition = LambdaParser.extractPredicate(s);
                assertTrue(condition.test(null, null, null));
            }
        }

        for (int i = 0; i < truePredicates.size(); i++) {
            for (int j = 0; j < falsePredicates.size(); j++) {
                s = "and(" + truePredicates.get(i) + ", " + falsePredicates.get(j) + ")";
                condition = LambdaParser.extractPredicate(s);
                assertFalse(condition.test(null, null, null));
            }
        }

        for (int i = 0; i < falsePredicates.size(); i++) {
            for (int j = i; j < falsePredicates.size(); j++) {
                s = "and(" + falsePredicates.get(i) + ", " + falsePredicates.get(j) + ")";
                condition = LambdaParser.extractPredicate(s);
                assertFalse(condition.test(null, null, null));
            }
        }
        //----------------------------------------------------------

        s = "and(valid(after))";
        try {
            LambdaParser.extractPredicate(s);
            fail();
        } catch (RuleParserException ignored) {}
        //----------------------------------------------------------
        for (int i = 0; i < truePredicates.size(); i++) {
            for (int j = i; j < truePredicates.size(); j++) {
                s = "or(" + truePredicates.get(i) + ", " + truePredicates.get(j) + ")";
                condition = LambdaParser.extractPredicate(s);
                assertTrue(condition.test(null, null, null));
            }
        }

        for (int i = 0; i < truePredicates.size(); i++) {
            for (int j = 0; j < falsePredicates.size(); j++) {
                s = "or(" + truePredicates.get(i) + ", " + falsePredicates.get(j) + ")";
                condition = LambdaParser.extractPredicate(s);
                assertTrue(condition.test(null, null, null));
            }
        }

        for (int i = 0; i < falsePredicates.size(); i++) {
            for (int j = i; j < falsePredicates.size(); j++) {
                s = "or(" + falsePredicates.get(i) + ", " + falsePredicates.get(j) + ")";
                condition = LambdaParser.extractPredicate(s);
                assertFalse(condition.test(null, null, null));
            }
        }
        //----------------------------------------------------------

        s = "or(valid(after), valid(before), valid(coord(3,4)))";
        try {
            LambdaParser.extractPredicate(s);
            fail();
        } catch (RuleParserException ignored) {}
    }

    @Test
    public void complexPredicateTest() throws RuleParserException {
        LambdaParser.extractPredicate("near(sum(after, coord(-1,0)), diff(before, coord(1,1)))");
    }

    @Test (expected = RuleParserException.class)
    public void functionDoesntExistsTest() throws RuleParserException {
        LambdaParser.extractPredicate("nonExistingFunction(after)");
    }


}

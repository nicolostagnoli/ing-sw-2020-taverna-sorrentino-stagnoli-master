package it.polimi.ingsw.model.handler;

import it.polimi.ingsw.exceptions.model.handler.RuleParserException;
import it.polimi.ingsw.model.Board;
import it.polimi.ingsw.model.Coord;
import it.polimi.ingsw.model.handler.util.Pair;
import it.polimi.ingsw.model.handler.util.TriPredicate;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;

public class RuleParserTest {

    @Test
    public void fileDoesntExistTest() {
        try {
            new RuleParser("nonExistingFile");
        } catch (FileNotFoundException ignored) {}
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/load_nonExistingFile");
        } catch (FileNotFoundException e) {
            fail();
        }
        try {
            ruleParser.parse();
            fail();
        }
        catch (RuleParserException e) {
            assert e.getMessage().contains("nonExistingFile");
        }

    }

    @Test (expected = IllegalStateException.class)
    public void cannotParseTwiceTest() throws RuleParserException {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/setParameters");
        } catch (FileNotFoundException e) {
            fail();
        }
        ruleParser.parse();
        ruleParser.parse();
    }

    @Test (expected = IllegalStateException.class)
    public void cannotGetRulesBeforeParsingTest() {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/setParameters");
        } catch (FileNotFoundException e) {
            fail();
        }
        ruleParser.getRules();
    }

    @Test
    public void unexpectedLineTest() {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/unexpected");
        } catch (FileNotFoundException e) {
            fail();
        }
        try {
            ruleParser.parse();
            fail();
        } catch (RuleParserException e) {
            String error = e.getMessage();
            Pattern p = Pattern.compile(".*Line\\s+3.*", Pattern.DOTALL);
            assert p.matcher(error).matches();
            assert error.contains("unexpected line");
        }
    }

    @Test
    public void unexpectedLineTest_2() {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/unexpected2");
        } catch (FileNotFoundException e) {
            fail();
        }
        try {
            ruleParser.parse();
            fail();
        } catch (RuleParserException e) {
            String error = e.getMessage();
            System.out.println(error);
            Pattern p = Pattern.compile(".*Line\\s+7.*", Pattern.DOTALL);
            assert p.matcher(error).matches();
            assert error.contains("unexpected line");
        }
    }

    @Test
    public void invalidParameterTest() {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/invalid_parameter");
        } catch (FileNotFoundException e) {
            fail();
        }
        try {
            ruleParser.parse();
            fail();
        } catch (RuleParserException e) {
            String error = e.getMessage();
            Pattern p = Pattern.compile(".*Line\\s+4.*", Pattern.DOTALL);
            assert p.matcher(error).matches();
            assert error.contains("invalid = value");
        }
    }

    @Test (expected = RuleParserException.class)
    public void incorrectIndentationTest() throws RuleParserException {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/indentation_error");
        } catch (FileNotFoundException e) {
            fail();
        }
        ruleParser.parse();
    }

    @Test
    public void errorInConditionTest() {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/condition_error");
        } catch (FileNotFoundException e) {
            fail();
        }
        try {
            ruleParser.parse();
            fail();
        } catch (RuleParserException e) {
            String error = e.getMessage();
            Pattern p = Pattern.compile(".*Line\\s+4.*", Pattern.DOTALL);
            assert p.matcher(error).matches();
            assert error.contains("compareLevels(error, before, =0)");
        }
    }

    @Test
    public void setParametersTest() throws RuleParserException, NoSuchFieldException,
            IllegalAccessException {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/setParameters");
        } catch (FileNotFoundException e) {
            fail();
        }
        ruleParser.parse();
        List<Rule> rules = ruleParser.getRules();
        assertEquals(1, rules.size());
        Rule r = rules.get(0);
        assertEquals(Purpose.VALIDATION, r.getPurpose());
        assertEquals(ActionType.MOVE, r.getActionType());
        assertEquals(Decision.GRANT, r.getDecision());
        assertNotNull(r.getCondition());
        assertNull(getSymbolicCondition(r));
        assertNotNull(r.getForceSpaceFunction());
    }

    @Test
    public void invalidEnumerationValuesTest() {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/invalid_purpose");
        } catch (FileNotFoundException e) {
            fail();
        }
        try {
            ruleParser.parse();
            fail();
        }
         catch(RuleParserException ignored) {}
        try {
            ruleParser = new RuleParser("rules/invalid_actiontype");
        } catch (FileNotFoundException e) {
            fail();
        }
        try {
            ruleParser.parse();
            fail();
        }
        catch(RuleParserException ignored) {}
        try {
            ruleParser = new RuleParser("rules/invalid_decision");
        } catch (FileNotFoundException e) {
            fail();
        }
        try {
            ruleParser.parse();
            fail();
        }
        catch(RuleParserException ignored) {}
        try {
            ruleParser = new RuleParser("rules/invalid_buildlevel");
        } catch (FileNotFoundException e) {
            fail();
        }
        try {
            ruleParser.parse();
            fail();
        }
        catch(RuleParserException ignored) {}
        try {
            ruleParser = new RuleParser("rules/invalid_target");
        } catch (FileNotFoundException e) {
            fail();
        }
        try {
            ruleParser.parse();
            fail();
        }
        catch(RuleParserException ignored) {}
        try {
            ruleParser = new RuleParser("rules/invalid_forcespacefunction");
        } catch (FileNotFoundException e) {
            fail();
        }
        try {
            ruleParser.parse();
            fail();
        }
        catch(RuleParserException ignored) {}
    }

    @Test
    public void multipleRulesTest() throws RuleParserException {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/multiple");
        } catch (FileNotFoundException e) {
            fail();
        }
        ruleParser.parse();
        List<Rule> rules = ruleParser.getRules();
        assertEquals(2, rules.size());
        assertNotNull(rules.get(0).getCondition());
        assertNotNull(rules.get(1).getCondition());
    }

    @Test
    public void symbolicConditionTest() throws RuleParserException, NoSuchFieldException, IllegalAccessException {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/symbolic");
        } catch (FileNotFoundException e) {
            fail();
        }
        ruleParser.parse();
        List<Rule> rules = ruleParser.getRules();
        assertEquals(1, rules.size());
        Rule r = rules.get(0);
        assertNotNull(getSymbolicCondition(r));
    }

    @Test (expected = RuleParserException.class)
    public void symbolicConditionWithNoSymbolsTest() throws RuleParserException {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/symbolic_no_symbols");
        } catch (FileNotFoundException e) {
            fail();
        }
        ruleParser.parse();
    }

    @Test (expected = RuleParserException.class)
    public void conditionWithSymbolsTest() throws RuleParserException {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/condition_with_symbols");
        } catch (FileNotFoundException e) {
            fail();
        }
        ruleParser.parse();
    }

    @Test
    public void noConditionSpecified() throws RuleParserException {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/no_condition_specified");
        } catch (FileNotFoundException e) {
            fail();
        }
        ruleParser.parse();
        List<Rule> rules = ruleParser.getRules();
        assertEquals(1, rules.size());
        Rule r = rules.get(0);
        BiPredicate<Pair<Coord>, Board> condition = r.getCondition();
        assertNotNull(condition);
        assertTrue(condition.test(null, null));
    }

    @Test (expected = AssertionError.class)
    public void ifSymbolicConditionThenNotUsable() throws RuleParserException {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/symbolic");
        } catch (FileNotFoundException e) {
            fail();
        }
        ruleParser.parse();
        List<Rule> rules = ruleParser.getRules();
        assertEquals(1, rules.size());
        Rule r = rules.get(0);
        r.getCondition(); // Trying to use (=access) the rule
    }

    @Test
    public void generationTest() throws RuleParserException {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/generation");
        } catch (FileNotFoundException e) {
            fail();
        }
        ruleParser.parse();
        List<Rule> rules = ruleParser.getRules();
        assertEquals(1, rules.size());
        Rule r = rules.get(0);
        Pair<Coord> oldAction = new Pair<>(new Coord(0,0), new Coord(0,0));
        rules = r.getGeneratedRules(oldAction);
        assertEquals(2, rules.size());
        r = rules.get(1);
        rules = r.getGeneratedRules(oldAction);
        assertEquals(3, rules.size());
        //Check the order:
        assertEquals(rules.get(0).getActionType(), ActionType.MOVE);
        assertEquals(rules.get(1).getActionType(), ActionType.BUILD);
        assertEquals(rules.get(2).getActionType(), ActionType.END);
    }

    @Test (expected = RuleParserException.class)
    public void generationNoIdTest() throws RuleParserException {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/generation_no_id");
        } catch (FileNotFoundException e) {
            fail();
        }
        ruleParser.parse();
    }

    @Test (expected = RuleParserException.class)
    public void nonGenerationWithIdTest() throws RuleParserException {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/non_generation_with_id");
        } catch (FileNotFoundException e) {
            fail();
        }
        ruleParser.parse();
    }

    @Test (expected = RuleParserException.class)
    public void secondaryRuleNotGeneratedByAnyoneTest() throws RuleParserException {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/secondary_no_generated");
        } catch (FileNotFoundException e) {
            fail();
        }
        ruleParser.parse();
    }

    @Test
    public void crossGenerationTest() throws RuleParserException {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/cross_generation");
        } catch (FileNotFoundException e) {
            fail();
        }
        ruleParser.parse();
        List<Rule> rules = ruleParser.getRules();
        assertEquals(2, rules.size());
        Rule r = rules.get(0);
        Rule q = r.getGeneratedRules(null).get(0).getGeneratedRules(null).get(0);
        assertEquals(r, q);
    }

    @Test
    public void autoGenerationTest() throws RuleParserException {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/auto_generation");
        } catch (FileNotFoundException e) {
            fail();
        }
        ruleParser.parse();
        List<Rule> rules = ruleParser.getRules();
        assertEquals(1, rules.size());
        Rule r = rules.get(0);
        Rule q = r.getGeneratedRules(null).get(0);
        assertEquals(r, q);
    }

    @Test (expected = RuleParserException.class)
    public void duplicateIdTest() throws RuleParserException {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/duplicate_id");
        } catch (FileNotFoundException e) {
            fail();
        }
        ruleParser.parse();
    }

    @Test
    public void secondaryFileRulesNotGeneratedByAnyoneTest() {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/secondary_file_no_generated");
        } catch (FileNotFoundException e) {
            fail();
        }
        try {
            ruleParser.parse();
            fail();
        } catch (RuleParserException e) {
            String error = e.getMessage();
            Pattern p = Pattern.compile(".*Line\\s+3.*", Pattern.DOTALL);
            assert p.matcher(error).matches();
        }
    }

    @Test
    public void secondaryFileThenRulesTest() throws RuleParserException {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/secondary_file_then_rules");
        } catch (FileNotFoundException e) {
            fail();
        }
        ruleParser.parse();
        List<Rule> rules = ruleParser.getRules();
        assertEquals(2, rules.size());
        assertEquals(Purpose.VALIDATION, rules.get(0).getPurpose());
        assertEquals(Purpose.GENERATION, rules.get(1).getPurpose());
        rules = rules.get(1).getGeneratedRules(null);
        assertEquals(1, rules.size());
        Rule r = rules.get(0);
        assertEquals(Purpose.VALIDATION, r.getPurpose());
        assertEquals(ActionType.MOVE, r.getActionType());
        assertEquals(Decision.GRANT, r.getDecision());
        assertNotNull(r.getForceSpaceFunction());
    }

    @Test
    public void ruleThenFileTest() throws RuleParserException {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/rule_then_file");
        } catch (FileNotFoundException e) {
            fail();
        }
        ruleParser.parse();
        List<Rule> rules = ruleParser.getRules();
        assertEquals(2, rules.size());
        Rule r = rules.get(1);
        assertEquals(Purpose.VALIDATION, r.getPurpose());
        assertEquals(ActionType.MOVE, r.getActionType());
        assertEquals(Decision.GRANT, r.getDecision());
        assertNotNull(r.getForceSpaceFunction());
    }

    @Test
    public void ruleThenSecondaryFileTest() throws RuleParserException {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/rule_then_secondary_file");
        } catch (FileNotFoundException e) {
            fail();
        }
        ruleParser.parse();
        List<Rule> rules = ruleParser.getRules();
        assertEquals(1, rules.size());
        rules = rules.get(0).getGeneratedRules(null);
        assertEquals(1, rules.size());
        Rule r = rules.get(0);
        assertEquals(Purpose.VALIDATION, r.getPurpose());
        assertEquals(ActionType.MOVE, r.getActionType());
        assertEquals(Decision.GRANT, r.getDecision());
        assertNotNull(r.getForceSpaceFunction());
    }

    @Test
    public void invalidFileParameterTest() {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/invalid_file_parameter");
        } catch (FileNotFoundException e) {
            fail();
        }
        try {
            ruleParser.parse();
            fail();
        } catch (RuleParserException e) {
            String error = e.getMessage();
            Pattern p = Pattern.compile(".*Line\\s+2.*", Pattern.DOTALL);
            assert p.matcher(error).matches();
            assert error.contains("invalid = value");
        }
    }

    @Test
    public void primaryFileTest() throws RuleParserException {
        RuleParser ruleParser = null;
        try {
            ruleParser = new RuleParser("rules/primary_file");
        } catch (FileNotFoundException e) {
            fail();
        }
        ruleParser.parse();
        List<Rule> rules = ruleParser.getRules();
        assertEquals(1, rules.size());
        Rule r = rules.get(0);
        assertEquals(Purpose.VALIDATION, r.getPurpose());
        assertEquals(ActionType.MOVE, r.getActionType());
        assertEquals(Decision.GRANT, r.getDecision());
        assertNotNull(r.getForceSpaceFunction());
    }

    private /*helper*/ TriPredicate getSymbolicCondition(Rule r)
            throws NoSuchFieldException, IllegalAccessException {
        Field symbolicConditionField = Rule.class.getDeclaredField("symbolicCondition");
        symbolicConditionField.setAccessible(true);
        return (TriPredicate) symbolicConditionField.get(r);
    }
}

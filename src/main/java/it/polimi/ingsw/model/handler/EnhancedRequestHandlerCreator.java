package it.polimi.ingsw.model.handler;

import it.polimi.ingsw.exceptions.model.handler.RuleParserException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is the Factory for RequestHandler objects.
 * @see RequestHandler
 */
public class EnhancedRequestHandlerCreator {
    private final String god;
    private static final List<Rule> standardRules = new ArrayList<>();
    private static final Map<String, List<Rule>> godRules = new HashMap<>();

    static {
        initStandardRules();
    }

    public EnhancedRequestHandlerCreator() {
        this.god = null;
    }

    /**
     * Gets a new instance of this class, useful to get a RequestHandler for the specified god
     * @param god the name of the god whose rules will be used by the RequestHandler
     */
    public EnhancedRequestHandlerCreator(String god) {
        if (!god.equals("None")) {
            this.god = god;
        } else {
            this.god = null;
        }
    }

    /**
     * Factory method
     * @return a RequestHandler for the desired god (or with only the standard rules if no god
     * is specified)
     */
    public RequestHandler createHandler() {
        List<Rule> rules = createRulesList();
        RuleHandler ruleHandler = new ConcreteHandler(rules);
        RequestHandler requestHandler = new HandlerAdapter(ruleHandler);

        return requestHandler;
    }

    private List<Rule> createRulesList() {
        List<Rule> result = new ArrayList<>();
        if (god != null) {
            result.addAll(getGodRules());
        }
        result.addAll(standardRules);

        return result;
    }

    private List<Rule> getGodRules() {
        if (god == null) throw new IllegalArgumentException("Cannot get rules for 'null' god.");
        if (!godRules.containsKey(god)) {
            initGodRules(god);
        }

        return new ArrayList<>(godRules.get(god));
    }

    private static void initStandardRules() {
        try {
            RuleParser ruleParser = new RuleParser("rules/standard/primary");
            ruleParser.parse();
            standardRules.addAll(ruleParser.getRules());
        } catch (FileNotFoundException | RuleParserException e) {
            System.out.println("Couldn't load standard rules.");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void initGodRules(String god) {
        String path = "rules/" + god.toLowerCase();
        try {
            RuleParser ruleParser = new RuleParser(path);
            ruleParser.parse();
            godRules.put(god, ruleParser.getRules());
        } catch (FileNotFoundException | RuleParserException e) {
            System.out.println("Couldn't load rules for god " + god);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }
}

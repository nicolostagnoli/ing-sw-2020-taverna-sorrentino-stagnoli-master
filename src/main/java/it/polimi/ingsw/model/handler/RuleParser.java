package it.polimi.ingsw.model.handler;

import it.polimi.ingsw.exceptions.model.handler.RuleParserException;
import it.polimi.ingsw.model.Board;
import it.polimi.ingsw.model.Coord;
import it.polimi.ingsw.model.Level;
import it.polimi.ingsw.model.handler.util.LambdaParser;
import it.polimi.ingsw.model.handler.util.Pair;
import it.polimi.ingsw.model.handler.util.TriPredicate;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/**
 * A parser for rules defined in a domain-specific language.
 * This class allows the user to get a list of rules based on a file written in the language
 * defined below. The expressive power of the language is enough to represent every rule dictated
 * by any God Effect and the standard rules.
 *
 * <h2>Language specification</h2>
 * <h3>Main tags</h3>
 * The main tags define which kind of object will be described in the following lines.
 * They should be written with a colon at their end. For example:<br>
 * <pre>{@code
 * rule:
 *     ...}</pre>
 * <ul>
 *     <li>rule: the following lines will describe the attributes of a rule, and this rule will
 *     be added to the ones that the user can get by calling the method {@code getRules()}.
 *     <li>secondary rule: the following lines will describe the attributes of a rule,
 *     but this rule won't be added to the ones that the user can get by calling the method
 *     {@code getRules()}. On the contrary, this rule must be generated at least by one other rule
 *     (see parameter "generatedBy" below).
 *     <li>file: the following lines will describe another file (its path and other optional parameters)
 *     that will be parsed using another instance of this class. The rules defined in that file
 *     will be added to the ones that the user can get by calling the method {@code getRules()}.
 *     <li>seocndary file: the following lines will describe another file (its path and other optional
 *     parameters) that will be parsed using another instance of this class. The rules defined in that
 *     file won't be added to the ones that the user can get by calling the method {@code getRules()}.
 *     On the contrary, they must be generated at least by one other rule (see parameter "generatedBy"
 *     below).
 *     </li>
 * </ul>
 * <h3>Rule parameters</h3>
 * These parameters can be specified after a "rule" main tag and need one tab of indentation.
 * They must be written in a notation like:<br>
 *     <pre>{@code
 *     parameter = value}</pre>
 * <ul>
 *     <li>purpose: a value of the enumeration {@link Purpose}
 *     <li>actionType: a value of the enumeration {@link ActionType}
 *     <li>decision: a value of the enumeration {@link Decision}
 *     <li>target: a value of the enumeration {@link Target}
 *     <li>buildLevel: a value of the enumeration {@link Level}
 *     <li>forceSpaceFunction: a coordinate representing the destination of a "force", specified as
 *     described in the documentation of {@link LambdaParser}
 *     <li>id: a user-defined name to reference a rule uniquely (to be used for {@code GENERATION} rules
 *     <li>generatedBy: an ID that specifies which rule will generate this one. This parameter is
 *     compulsory if the rule is secondary.
 *     </li>
 * </ul>
 * <h4>Condition of the rule</h4>
 * The condition of the rule can be specified with the tag {@code condition}, followed by a colon.
 * For example:<br>
 * <pre>{@code
 * condition:
 *     (condition goes here)}</pre>
 * If the condition contains references to positions of the previous move (see {@code oldBefore}
 * and {@code oldAfter} in {@code LambdaParser}'s documentation) then the tag {@code symbolicCondition}
 * must be used instead.<br>
 *     These tags require one tab of indentation, too.
 * <h3>File parameters</h3>
 * These parameters can be specified after a "file" tag and need one tab of indentation. The format
 * is like the one of rule parameters:<br>
 * <pre>{@code
 * parameter = value}</pre>
 * <ul>
 *     <li>source: the path to the file
 *     <li>generatedBy: an ID that specifies which rule will generate the ones contained in this file.
 *     This parameter is compulsory if the file is secondary.
 *     </li>
 * </ul>
 *
 */
public class RuleParser {
    private String line;
    private int numLine;
    private int indentationLevel;

    private List<Rule> rules;
    private Rule rule;
    private TriPredicate<Pair<Coord>, Pair<Coord>, Board> condition;
    private boolean isParsingCondition;
    private boolean hasSymbols;
    private boolean isSymbolic;
    private boolean isGeneration;
    private boolean isGenerated;

    private boolean secondary;
    private boolean isFile;
    private final Map<String, List<Rule>> generationMap;
    private final Set<String> idSet;
    private boolean idAdded;
    private List<Rule> rulesFromFile;
    private final Scanner scanner;


    /**
     * Constructor method
     * @param file the file to be parsed
     * @throws FileNotFoundException when the file doesn't exist
     */
    public RuleParser(String file) throws FileNotFoundException {
        numLine = 0;
        indentationLevel = 0;
        condition = (oldPair, cPair, board) -> true;
        generationMap = new HashMap<>();
        idSet = new HashSet<>();
        InputStream inputStream = this.getClass().getClassLoader()
                .getResourceAsStream(file);
        if (inputStream == null) {
            throw new FileNotFoundException("The file " + file + " does not exist.");
        }
        scanner = new Scanner(inputStream);
    }

    /**
     * Parses the file
     * @throws RuleParserException when the file contains syntax mistakes
     */
    public void parse() throws RuleParserException {
        //State check
        if (rules == null) {
            rules = new ArrayList<>();
        }
        else {
            throw new IllegalStateException("This method has already been called and " +
                    "can be called only once.");
        }

        //Loop for parsing
        while (scanner.hasNextLine()) {
            parseLine();

            if (indentationLevel == 0 && line.equals("rule:")) { // If new rule
                if (rule != null) {
                    setCondition();
                    if (!secondary) {
                        addRule();
                    }
                }
                checkAndReset();
                rule = new Rule();
            }
            else if (indentationLevel == 0 && line.matches("secondary\\x20+rule:")) { // If new rule
                if (rule != null) {
                    setCondition();
                    if (!secondary) {
                        addRule();
                    }
                }
                checkAndReset();
                rule = new Rule();
                secondary = true;
            }
            else if (indentationLevel == 0 && line.equals("file:")) {
                if (rule != null) {
                    setCondition();
                    if (!secondary) {
                        addRule();
                    }
                }
                checkAndReset();
                rule = null;
                isFile = true;
            }
            else if (indentationLevel == 0 && line.equals("secondary file:")) {
                if (rule != null) {
                    setCondition();
                    if (!secondary) {
                        addRule();
                    }
                }
                checkAndReset();
                rule = null;
                isFile = true;
                secondary = true;
            }
            else if (indentationLevel == 1) {
                if (line.contains("=")) { // If setting a parameter
                    String[] lineElements = line.split("=");
                    String parameter = lineElements[0].strip();
                    String value = lineElements[1].strip();
                    if (!isFile) {
                        setParameter(parameter, value);
                    } else {
                        setFileParameter(parameter, value);
                    }
                }
                else if (!isFile && line.matches("(condition|symbolicCondition):")) { // If beginning a condition
                    condition = (oldPair, cPair, board) -> true;
                    isParsingCondition = true;
                    if (line.equals("symbolicCondition:")) isSymbolic = true;
                }
                else {
                    error("Unexpected line.");
                }
            }
            else if (indentationLevel == 2 && isParsingCondition) { // If parsing a condition
                try {
                    condition = condition.and(LambdaParser.extractPredicate(line));
                } catch (RuleParserException e) {
                    error(e.getMessage());
                }
                if (line.contains("oldBefore") || line.contains("oldAfter")) {
                    hasSymbols = true;
                    if (!isSymbolic) {
                        error("Symbols used in a non-symbolic condition.");
                    }
                }
            }
            else {
                error("Unexpected line.");
            }


        }

        if (rule != null) {
            setCondition();
            if (!secondary) {
                addRule();
            }
        }
        checkAndReset();
        scanner.close();

    }

    /**
     * Returns the rules specified in the file
     * @return a list containing the rules specified in the file
     */
    public List<Rule> getRules() {
        if (rules == null) {
            throw new IllegalStateException("A parsing is needed before calling this method.");
        }
        return new ArrayList<>(rules);
    }

    private /*helper*/ void parseLine() throws RuleParserException {
        do {
            line = scanner.nextLine();
            numLine++;
        } while (scanner.hasNextLine() && line.strip().equals(""));

        if (line.matches("\\S+.*")) {
            indentationLevel = 0;
        }
        else if (line.matches("\\t\\S+.*")) {
            indentationLevel = 1;
        }
        else if (line.matches("\\t\\t\\S+.*")) {
            indentationLevel = 2;
        }
        else {
            error("Incorrect indentation. (Maybe using spaces instead of tabs?)");
        }

        line = line.substring(indentationLevel);
        line = line.strip();
    }

    private /*helper*/ void setParameter(String parameter, String value) throws RuleParserException {
        switch (parameter) {
            case "purpose": {
                try {
                    rule.setPurpose(Purpose.valueOf(value.toUpperCase()));
                    if (value.toUpperCase().equals("GENERATION")) {
                        isGeneration = true;
                    }
                }
                catch (IllegalArgumentException e) {
                    error("Value " + value + " is invalid for " + parameter + " parameter.");
                }
                break;
            }
            case "actionType": {
                try {
                    rule.setActionType(ActionType.valueOf(value.toUpperCase()));
                }
                catch (IllegalArgumentException e) {
                    error("Value " + value + " is invalid for " + parameter + " parameter.");
                }
                break;
            }
            case "decision": {
                try {
                    rule.setDecision(Decision.valueOf(value.toUpperCase()));
                }
                catch (IllegalArgumentException e) {
                    error("Value " + value + " is invalid for " + parameter + " parameter.");
                }
                break;
            }
            case "target": {
                try {
                    rule.setTarget(Target.valueOf(value.toUpperCase()));
                }
                catch (IllegalArgumentException e) {
                    error("Value " + value + " is invalid for " + parameter + " parameter.");
                }
                break;
            }
            case "buildLevel": {
                try {
                    rule.setBuildLevel(Level.valueOf(value.toUpperCase()));
                }
                catch (IllegalArgumentException e) {
                    error("Value " + value + " is invalid for " + parameter + " parameter.");
                }
                break;
            }
            case "forceSpaceFunction": {
                try {
                    BiFunction<Pair<Coord>, Pair<Coord>, Coord> coord =
                            LambdaParser.extractCoordFunction(value);
                    BiFunction<Coord, Coord, Coord> forceSpaceFunction = (before, after) ->
                            coord.apply(null, new Pair<>(before, after));
                    rule.setForceSpaceFunction(forceSpaceFunction);
                } catch (RuleParserException e) {
                    error(e.getMessage());
                }
                break;
            }

            /*
            GENERATION ASSOCIATION
            When a new generation rule with id = ID is created:
            if (ID exists) --> rule.setGeneratedRules(generationMap.get(id))
            else --> generationMap.put(id, new ArrayList<>)
                     rule.setGeneratedRules(generationMap.get(id))
                     // This means: CREATE THE ASSOCIATION + SET THE GENERATED RULES FOR THE RULE
            When a rule is 'generatedBy' ID:
            if (ID exists) --> generationMap.get(ID).add(rule)
            else --> generationMap.put(ID, new ArrayList<>)
                     generationMap.get(ID).add(rule)
                     // This means: CREATE THE ASSOCIATION + ADD THE RULE
         */
            case "id": {
                idAdded = true;
                if (idSet.contains(value)) {
                    error("A previously defined rule has the same ID " + value);
                }
                idSet.add(value);
                if (!generationMap.containsKey(value)) {
                    generationMap.put(value, new ArrayList<>());
                }
                rule.setGeneratedRules(generationMap.get(value));
                break;
            }

            case "generatedBy": {
                if (!generationMap.containsKey(value)) {
                    generationMap.put(value, new ArrayList<>());
                }
                generationMap.get(value).add(rule);
                isGenerated = true;
                break;
            }

            default: {
                error("Parameter " + parameter + " is invalid.");
            }
        }
    }

    private /*helper*/ void setFileParameter(String parameter, String value) throws RuleParserException {
        switch (parameter) {
            case "source": {
                RuleParser ruleParser;
                try {
                    ruleParser = new RuleParser(value);
                    ruleParser.parse();
                    rulesFromFile = ruleParser.getRules();
                    if (!secondary) {
                        rules.addAll(rulesFromFile);
                    }
                } catch (FileNotFoundException e) {
                    error("File " + value + " doesn't exist.");
                }
                break;
            }
            case "generatedBy": {
                if (!generationMap.containsKey(value)) {
                    generationMap.put(value, new ArrayList<>());
                }
                generationMap.get(value).addAll(rulesFromFile);
                isGenerated = true;
                break;
            }

            default: {
                error("Parameter " + parameter + " is invalid.");
            }
        }
    }

    private /*helper*/ void setCondition() throws RuleParserException {
        if (!isSymbolic) { // If 'condition'
            TriPredicate<Pair<Coord>, Pair<Coord>, Board> finalCondition = condition;
            BiPredicate<Pair<Coord>, Board> ruleCondition = (cPair, board) -> finalCondition.test(null, cPair, board);
            rule.setCondition(ruleCondition);
        }
        else if (hasSymbols) { // If 'symbolicCondition' with symbols
            rule.setSymbolicCondition(condition);
        }
        else { // If 'symbolicCondition' without symbols
            error("End of a symbolic condition with no symbols.");
        }

        isParsingCondition = false;
        isSymbolic = false;
        hasSymbols = false;
        condition = (oldPair, cPair, board) -> true;
    }

    private /*helper*/ void addRule() throws RuleParserException {
        if (idAdded && !isGeneration) {
            error("End of rule with ID assigned, but rule's purpose is not generation.");
        }
        else if (!idAdded && isGeneration) {
            error("End of generation rule without an ID.");
        }
        rules.add(rule);
    }

    private /*helper*/ void checkAndReset() throws RuleParserException {
        if(secondary && !isGenerated) {
            error("End of secondary rule (or file) that is not generated by any other rule.");
        }
        idAdded = false;
        isGeneration = false;
        secondary = false;
        isGenerated = false;
        isFile = false;
    }

    private /*helper*/ void error(String message) throws RuleParserException {
        throw new RuleParserException(message, line, numLine);
    }
}

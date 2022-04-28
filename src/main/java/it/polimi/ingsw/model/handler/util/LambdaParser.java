package it.polimi.ingsw.model.handler.util;

import it.polimi.ingsw.exceptions.model.handler.RuleParserException;
import it.polimi.ingsw.model.Board;
import it.polimi.ingsw.model.Coord;
import it.polimi.ingsw.model.Level;


import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A parser of conditions written in a domain-specific language.
 * This class allows the user to convert a condition (written in the language defined below)
 * into a lambda expression for implementing the interface {@link TriPredicate}.
 *
 * <h2>The language</h2>
 * A condition can be expressed using the following "functions":
 * <ul>
 *     <li>occupied(coord)
 *     <li>dome(coord)
 *     <li>near(coord1, coord2)
 *     <li>valid(coord)
 *     <li>equalCoordinates(coord1, coord2)
 *     <li>samePlayer(coord1, coord2)
 *     <li>compareLevels(coord1, coord2, comparator_level)
 *     <li>sameGod(coord1, coord2)
 *     <li>negate(condition)
 *     <li>or(condition1, condition2)
 *     <li>and(condition1, condition2)
 *     </li>
 * </ul>
 * Apart from the last three functions, the other ones take coordinates as input.
 * A coordinate can be:
 * <ul>
 *     <li>before: the position of the current worker before making the action
 *     <li>after: the position of the current worker after making the action
 *     <li>oldBefore: the position of the current worker before making the previous action
 *     <li>the position of the current worker after making the previous action
 *     <li>coord(n,m): the coordinate (n,m) (a constant value)
 *     </li>
 * </ul>
 * Furthermore, a coordinate can be expressed as a sum or difference of two other coordinates,
 * by using the functions sum(coord1, coord2) and diff(coord1, coord2)
 */
public class LambdaParser {

    private static final Pattern coordPattern =
            Pattern.compile("coord\\x20*\\(\\x20*(-?\\d)\\x20*,\\x20*(-?\\d)\\x20*\\)");

    /**
     * Parses the condition converting it to a lambda expression
     * @param line the string containing the condition
     * @return the TriPredicate corresponding to the condition
     * @throws RuleParserException when the condition is not written properly
     */
    public static TriPredicate<Pair<Coord>, Pair<Coord>, Board> extractPredicate(String line) throws RuleParserException {

        TriPredicate<Pair<Coord>, Pair<Coord>, Board> condition;
        String function = getFunction(line);
        String argument = getArgument(line);
        argument = reduceParentheses(argument);

        switch (function) {
            case "occupied": {
                BiFunction<Pair<Coord>, Pair<Coord>, Coord> arg = fromCoordToSymbolicFunction(argument);
                condition = (oldPair, cPair, board) -> board.getSpace(arg.apply(oldPair, cPair)).isOccupied();
                break;
            }

            case "dome": {
                BiFunction<Pair<Coord>, Pair<Coord>, Coord> arg = fromCoordToSymbolicFunction(argument);
                condition = (oldPair, cPair, board) -> board.getSpace(arg.apply(oldPair, cPair)).isDome();
                break;
            }

            case "near": {
                List<String> arguments = parseArguments(argument);
                if (arguments.size() != 2) {
                    throw new RuleParserException(function.toUpperCase() + " takes exactly 2 " +
                            "arguments, " + arguments.size() + " passed.");
                }

                List<BiFunction<Pair<Coord>, Pair<Coord>, Coord>> coords = new ArrayList<>();
                for (String arg : arguments) {
                    coords.add(fromCoordToSymbolicFunction(arg));
                }

                condition = (oldPair, cPair, board) -> coords.get(0).apply(oldPair, cPair)
                        .isNear(coords.get(1).apply(oldPair, cPair));
                break;
            }

            case "equalCoordinates": {
                List<String> arguments = parseArguments(argument);
                if (arguments.size() != 2) {
                    throw new RuleParserException(function.toUpperCase() + " takes exactly 2 " +
                            "arguments, " + arguments.size() + " passed.");
                }

                List<BiFunction<Pair<Coord>, Pair<Coord>, Coord>> coords = new ArrayList<>();
                for (String arg : arguments) {
                    coords.add(fromCoordToSymbolicFunction(arg));
                }

                condition = (oldPair, cPair, board) -> coords.get(0).apply(oldPair, cPair)
                        .equals(coords.get(1).apply(oldPair, cPair));
                break;
            }

            case "samePlayer": {
                List<String> arguments = parseArguments(argument);
                if (arguments.size() != 2) {
                    throw new RuleParserException(function.toUpperCase() + " takes exactly 2 " +
                            "arguments, " + arguments.size() + " passed.");
                }

                List<BiFunction<Pair<Coord>, Pair<Coord>, Coord>> coords = new ArrayList<>();
                for (String arg : arguments) {
                    coords.add(fromCoordToSymbolicFunction(arg));
                }

                condition = (oldPair, cPair, board) ->
                        board.getWorkerCopy(coords.get(0).apply(oldPair, cPair)).getPlayerNickname()
                                .equals(board.getWorkerCopy(coords.get(1).apply(oldPair, cPair)).getPlayerNickname());
                // If there is no player in one of the specified positions,
                // then the condition 'samePlayer' must be false:
                TriPredicate<Pair<Coord>, Pair<Coord>, Board> conditionOnOccupation =
                        (oldPair, cPair, board) ->
                                board.getSpace(coords.get(0).apply(oldPair, cPair)).isOccupied() &&
                                board.getSpace(coords.get(1).apply(oldPair, cPair)).isOccupied();
                condition = conditionOnOccupation.and(condition);
                break;
            }

            case "valid": {
                BiFunction<Pair<Coord>, Pair<Coord>, Coord> arg = fromCoordToSymbolicFunction(argument);
                condition = (oldPair, cPair, board) -> Coord.validCoord(arg.apply(oldPair, cPair));
                break;
            }
            case "compareLevels": {
                List<String> arguments = parseArguments(argument);
                if (arguments.size() != 3) {
                    throw new RuleParserException(function.toUpperCase() + " takes exactly 3 " +
                            "arguments, " + arguments.size() + " passed.");
                }

                List<TriFunction<Pair<Coord>, Pair<Coord>, Board, Level>> levels = new ArrayList<>();
                levels.add(fromLevelToSymbolicFunction(arguments.get(0)));
                levels.add(fromLevelToSymbolicFunction(arguments.get(1)));
                String comparator = arguments.get(2);

                if (comparator.matches("=\\d")) {
                    condition = (oldPair, cPair, board) ->
                            levels.get(1).apply(oldPair, cPair, board).ordinal() -
                                    levels.get(0).apply(oldPair, cPair, board).ordinal() ==
                                    Integer.parseInt(arguments.get(2).substring(1));
                } else if (comparator.matches("<\\d")) {
                    condition = (oldPair, cPair, board) ->
                            levels.get(1).apply(oldPair, cPair, board).ordinal() -
                                    levels.get(0).apply(oldPair, cPair, board).ordinal() <
                                    Integer.parseInt(arguments.get(2).substring(1));
                } else if (comparator.matches(">\\d")) {
                    condition = (oldPair, cPair, board) ->
                            levels.get(1).apply(oldPair, cPair, board).ordinal() -
                                    levels.get(0).apply(oldPair, cPair, board).ordinal() >
                                    Integer.parseInt(arguments.get(2).substring(1));
                } else {
                    throw new RuleParserException("Incorrect comparator: " + comparator);
                }
                break;
            }

            case "sameGod": {
                List<String> arguments = parseArguments(argument);
                if (arguments.size() != 2) {
                    throw new RuleParserException(function.toUpperCase() + " takes exactly 2 " +
                            "arguments, " + arguments.size() + " passed.");
                }

                List<BiFunction<Pair<Coord>, Board, String>> gods = new ArrayList<>();
                List<BiFunction<Pair<Coord>, Pair<Coord>, Coord>> coords = new ArrayList<>();
                for (String arg : arguments) {
                    BiFunction<Pair<Coord>, Board, String> god;
                    if (arg.matches("\"(\\w+)\"")) {
                        String godName = arg.substring(1, arg.length()-1).strip().toLowerCase();
                        god = (cPair, board) -> godName;
                    }
                    else {
                        BiFunction<Pair<Coord>, Pair<Coord>, Coord> coord = fromCoordToSymbolicFunction(arg);
                        god = (cPair, board) -> board.getWorkerCopy(coord.apply(null, cPair)).getGod().toLowerCase();
                        coords.add(coord);
                    }
                    gods.add(god);
                }

                condition = (oldPair, cPair, board) ->
                        gods.get(0).apply(cPair, board).equals(gods.get(1).apply(cPair, board));
                // If there is no player in one of the specified positions,
                // then the condition 'sameGod' must be false:
                for (int i = 0; i < coords.size(); i++) {
                    int idx = i;
                    TriPredicate<Pair<Coord>, Pair<Coord>, Board> conditionOnOccupation =
                            (oldPair, cPair, board) ->
                                    board.getSpace(coords.get(idx).apply(oldPair, cPair)).isOccupied();
                    condition = conditionOnOccupation.and(condition);
                }
                break;
            }

            case "negate": {
                TriPredicate<Pair<Coord>, Pair<Coord>, Board> internalPredicate = extractPredicate(argument);
                condition = internalPredicate.negate();
                break;
            }

            case "or":
            case "and": {
                List<String> arguments = parseArguments(argument);
                if (arguments.size() != 2) {
                    throw new RuleParserException(function.toUpperCase() + " takes exactly 2 " +
                            "arguments, " + arguments.size() + " passed.");
                }

                String argumentOne = arguments.get(0);
                String argumentTwo = arguments.get(1);

                if (function.equals("or")) {
                    condition = extractPredicate(argumentOne).or(extractPredicate(argumentTwo));
                } else {
                    condition = extractPredicate(argumentOne).and(extractPredicate(argumentTwo));
                }
                break;
            }

            default:
                throw new RuleParserException(function + " function does not exist.");
        }

        return condition;
    }

    /**
     * Converts a coordinate into a lambda expression
     * @param argument the coordinate expressed in the language defined above
     * @return a function that returns the coordinate
     * @throws RuleParserException when the coordinate is not written properly
     */
    public static BiFunction<Pair<Coord>, Pair<Coord>, Coord> extractCoordFunction(String argument) throws RuleParserException {
        return fromCoordToSymbolicFunction(argument);
    }


    //-----------------------------HELPER METHODS------------------------------

    /*
    Internal contract logic:
    The caller must clean the argument to be passed to the callee (i.e. strip() + reduceParentheses())
    The callee can safely call parseArguments() on the argument passed by the caller
    */

    static List<String> /*helper*/ parseArguments(String source) throws RuleParserException {

        /*COMMAS IDENTIFICATION*/
        List<String> splitsOnComma = Arrays.asList(source.split(","));
        splitsOnComma = new ArrayList<>(splitsOnComma);

        if (splitsOnComma.stream().anyMatch(s -> s.matches("\\s*"))) {
            throw new RuleParserException("Found empty argument(s) (,,) in " + source);
        }

        /*ARGUMENTS IDENTIFICATION*/
        List<String> arguments = new ArrayList<>();
        int count = 0;
        int start = 0;

        for (int i = 0; i < splitsOnComma.size(); i++) {
            String piece = splitsOnComma.get(i);

            count += piece.chars().filter(c -> c == '(').count() -
                    piece.chars().filter(c -> c == ')').count();

            if (count == 0) {
                String arg = splitsOnComma.subList(start, i + 1).stream()
                        .map(s -> s + ",").reduce(String::concat).orElseThrow(); // before: .orElse(piece)
                arg = arg.substring(0, arg.length() - 1);
                arg = arg.strip();
                arg = reduceParentheses(arg);
                arguments.add(arg);
                start = i + 1;
            }
        }
        return arguments;
    }

    static String /*helper*/ getFunction(String line) {
        String function = line.split("\\(", 2)[0];
        function = function.strip();
        return function;
    }

    static String /*helper*/ getArgument(String line) throws RuleParserException {
        if (!line.matches(".*\\(.+\\)")) {
            throw new RuleParserException("Argument format is incorrect.");
        }
        String argument = "(" + line.split("\\(", 2)[1];
        argument = argument.strip();
        return argument;
    }

    static String /*helper*/ reduceParentheses(String source) {
        while (Pattern.matches("\\(.+\\)", source)) {
            boolean canReduce = false;
            int count = 0;
            for (int pos = 0; pos < source.length(); pos++) {
                char c = source.charAt(pos);
                if (c == '(') count++;
                if (c == ')') count--;
                if (count == 0) {
                    if(pos == source.length() - 1) {
                        canReduce = true;
                    }
                    break;
                }
            }
            if (canReduce) {
                source = source.substring(1, source.length() - 1);
                source = source.strip();
            }
            else {
                break;
            }
        }
        return source;
    }

    static /*helper*/ BiFunction<Pair<Coord>, Pair<Coord>, Coord> fromCoordToSymbolicFunction(String c)
            throws RuleParserException {
        BiFunction<Pair<Coord>, Pair<Coord>, Coord> result;
        Matcher m = coordPattern.matcher(c);

        if (c.equals("before")) {
            result = (oldPair, cPair) -> cPair.get(0);
        } else if (c.equals("after")) {
            result = (oldPair, cPair) -> cPair.get(1);
        } else if (c.equals("oldBefore")) {
            result = (oldPair, cPair) -> oldPair.get(0);
        } else if (c.equals("oldAfter")) {
            result = (oldPair, cPair) -> oldPair.get(1);
        } else if (m.matches()) {
            int x = Integer.parseInt(m.group(1));
            int y = Integer.parseInt(m.group(2));
            result = (oldPair, cPair) -> new Coord(x, y);
        } else if (c.startsWith("sum")) {
            String sumArgument = c.split("sum", 2)[1];
            sumArgument = sumArgument.strip();
            sumArgument = reduceParentheses(sumArgument);
            result = getSumFunction(sumArgument);
        } else if (c.startsWith("diff")) {
            String diffArgument = c.split("diff", 2)[1];
            diffArgument = diffArgument.strip();
            diffArgument = reduceParentheses(diffArgument);
            result = getDiffFunction(diffArgument);
        }
        else {
            throw new RuleParserException("A coordinate can only be 'before', 'after', 'oldBefore', 'oldAfter', " +
                    "a constant like coord(n,m), a sum or a diff of two coordinates. Provided: " + c);
        }
        return result;
    }

    static /*helper*/ TriFunction<Pair<Coord>, Pair<Coord>, Board, Level> fromLevelToSymbolicFunction(String level)
            throws RuleParserException {

        TriFunction<Pair<Coord>, Pair<Coord>, Board, Level> result;

        if (level.toUpperCase().matches("GROUND|LVL1|LVL2|LVL3|DOME")) {
            result = (oldPair, cPair, board) -> Level.valueOf(level.toUpperCase());
        } else {
            try {
                BiFunction<Pair<Coord>, Pair<Coord>, Coord> coord = fromCoordToSymbolicFunction(level);
                result = (oldPair, cPair, board) -> board.getSpace(coord.apply(oldPair, cPair)).getHeight();
            }
            catch (RuleParserException e) {
                throw new RuleParserException("A level can only be a constant like GROUND, LVLn, DOME or " +
                        "referenced through its coordinate. Provided: " + level);
            }
        }
        return result;
    }


    static /*helper*/ BiFunction<Pair<Coord>, Pair<Coord>, Coord> getSumFunction(String argument)
            throws RuleParserException {

        List<String> arguments = parseArguments(argument);
        if (arguments.size() != 2) {
            throw new RuleParserException("SUM takes exactly 2 " +
                    "arguments, " + arguments.size() + " passed:\n" + arguments);
        }

        List<BiFunction<Pair<Coord>, Pair<Coord>, Coord>> coords = new ArrayList<>();
        for (String arg : arguments) {
            coords.add(fromCoordToSymbolicFunction(arg));
        }


        BiFunction<Pair<Coord>, Pair<Coord>, Coord> firstAddend = coords.get(0);
        BiFunction<Pair<Coord>, Pair<Coord>, Coord> secondAddend = coords.get(1);
        BiFunction<Pair<Coord>, Pair<Coord>, Coord> sumFunction =
                (oldPair, cPair) -> firstAddend.apply(oldPair, cPair).sum(secondAddend.apply(oldPair, cPair));

        return sumFunction;
    }

    static /*helper*/ BiFunction<Pair<Coord>, Pair<Coord>, Coord> getDiffFunction(String argument)
            throws RuleParserException {

        List<String> arguments = parseArguments(argument);
        if (arguments.size() != 2) {
            throw new RuleParserException("DIFF takes exactly 2 " +
                    "arguments, " + arguments.size() + " passed:\n" + arguments);
        }

        List<BiFunction<Pair<Coord>, Pair<Coord>, Coord>> coords = new ArrayList<>();
        for (String arg : arguments) {
            coords.add(fromCoordToSymbolicFunction(arg));
        }

        BiFunction<Pair<Coord>, Pair<Coord>, Coord> minuend = coords.get(0);
        /*
        BiFunction<Pair<Coord>, Pair<Coord>, Coord> subtrahend =
                (oldPair, cPair) -> new Coord(-coords.get(1).apply(cPair, oldPair).x,
                        -coords.get(1).apply(cPair, oldPair).y);
        BiFunction<Pair<Coord>, Pair<Coord>, Coord> diffFunction =
                (oldPair, cPair) -> minuend.apply(cPair, oldPair).sum(subtrahend.apply(cPair, oldPair));
         */
        BiFunction<Pair<Coord>, Pair<Coord>, Coord> subtrahend =
                (oldPair, cPair) -> new Coord(-coords.get(1).apply(oldPair, cPair).x,
                        -coords.get(1).apply(oldPair, cPair).y);
        BiFunction<Pair<Coord>, Pair<Coord>, Coord> diffFunction =
                (oldPair, cPair) -> minuend.apply(oldPair, cPair).sum(subtrahend.apply(oldPair, cPair));
        return diffFunction;
    }
}

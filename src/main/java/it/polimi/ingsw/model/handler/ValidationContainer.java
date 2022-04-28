/*
Rep:
\forall space in allSpaces (moveGrant.contains(space) <==> !moveDeny.contains(space))
\forall space in allSpaces (buildGrant.contains(space) <==> !buildDeny.contains(space))

forceList.contains(space) ==> moveGrantList.contains(space)

*/


/*
SuperList structure:
0. moveGrant list
1. moveDeny list
2. buildGrant_GROUND list
3. buildDeny_GROUND list
4. buildGrant_LVL1 list
5. buildDeny_LVL1 list
6. buildGrant_LVL2 list
7. buildDeny_LVL2 list
8. buildGrant_LVL3 list
9. buildDeny_LVL3 list
10. buildGrant_DOME list
11. buildDeny_DOME list

Every list is accessed with index = 2*actionType + decision (+ 2*level if level != null)
 */

package it.polimi.ingsw.model.handler;

import it.polimi.ingsw.model.Board;
import it.polimi.ingsw.model.Coord;
import it.polimi.ingsw.model.Level;

import java.util.*;
import java.util.stream.Collectors;


class ValidationContainer {
    private final Coord currentPosition;
    private final Board board;
    private final List<Coord> allSpaces;
    private final List<List<Coord>> superList;
    private final Map<Coord, Coord> forces;

    public ValidationContainer(Coord currentPosition, Board board) {
        this.currentPosition = currentPosition;
        this.board = board;
        this.allSpaces = board.getAllCoord();
        this.superList = new ArrayList<>();
        this.forces = new HashMap<>();
        for (int i = 0; i < 12; i++) {
            this.superList.add(new ArrayList<>());
        }
    }


    public Coord getCurrentPosition() {
        return currentPosition;
    }

    public Board getBoard() {
        return board;
    }

    public List<Coord> getAllSpaces() {
        return allSpaces;
    }

    public void validateSpace(
            Coord s, ActionType at, Decision d, Coord forceDest, Level buildLevel) {

        int index = getSuperListIndex(at, d, buildLevel);

        Decision oppositeDecision = d.getOpposite();
        int oppositeIndex = getSuperListIndex(at, oppositeDecision, buildLevel);

        if (superList.get(oppositeIndex).contains(s)) {
            return;
        }

        superList.get(index).add(s);

        if (forceDest != null) {
            forces.put(s, forceDest);
        }
    }

    public List<Coord> getMovableSpaces() {
        return new ArrayList<>(superList.get(0));
    }

    public Map<Level, List<Coord>> getBuildableSpaces() {
        Map<Level, List<Coord>> result = new HashMap<>();


        Arrays.asList(Level.values())
                .forEach(level ->
                        result.put(
                                level,
                                new ArrayList<>(superList.get(2 + 2*level.ordinal()))
                        )
                );

        return result;
    }

    public Map<Coord, Coord> getForces() {
        return new HashMap<>(forces);
    }

    public boolean allSpacesValidated() {
        for (int i = 0; i < superList.size(); i += 2) {
            final int idx = i;
            if (allSpaces.stream()
                    .filter(s -> !superList.get(idx).contains(s))
                    .filter(s -> !superList.get(idx+1).contains(s))
                    .count()
                != 0) {
                return false;
            }
        }
        return true;
    }

    public List<Coord> getNotValidatedSpaces() {
        List<Coord> result = new ArrayList<>();
        for (int i = 0; i < superList.size(); i += 2) {
            final int idx = i;
            result.addAll(
                    allSpaces.stream()
                            .filter(s -> !superList.get(idx).contains(s))
                            .filter(s -> !superList.get(idx + 1).contains(s))
                            .collect(Collectors.toList())
            );
        }

        return result;
    }

    private /*helper*/ int getSuperListIndex(ActionType at, Decision d, Level buildLevel) {
        int index = 2*at.ordinal() + d.ordinal();
        if (buildLevel != null) {
            index += 2*buildLevel.ordinal();
        }

        return index;
    }
}

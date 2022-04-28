package it.polimi.ingsw.model;

import it.polimi.ingsw.exceptions.model.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Board implements Cloneable, Serializable {

    private static final long serialVersionUID = 2L;

    public static final int BOARD_SIZE = 5;
    private Space[][] board;
    private final List<Worker> workers;

    public Board(){
        this.board = new Space[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                this.board[i][j] = new Space();
            }
        }
        this.workers = new ArrayList<>();
    }

    /**
     * Get a copy of the space in the given coordinates
     * @param c the coordinates
     * @return
     * @throws InvalidCoordinatesException when invalid coordinates
     */
    public Space getSpace(Coord c) throws InvalidCoordinatesException {

        // Coordinates must be valid
        if (Coord.validCoord(c)) {
            return board[c.x][c.y].clone();
        } else {
            throw new InvalidCoordinatesException("Invalid coordinates.");
        }
    }

    void addWorker(Worker w) throws IllegalStateException {

        // Worker must be absent from board
        if(this.workers.contains(w)) {
            throw new IllegalStateException("The worker has already been added.");
        }

        this.workers.add(w);
    }

    Worker[] getAllWorkers() {
        Worker[] allWorkers = new Worker[this.workers.size()];
        for (int i = 0; i < this.workers.size(); i++) {
            allWorkers[i] = this.workers.get(i);
        }
        return allWorkers;
    }

    //Returns the worker of the board that occupies coordinates 'pos'
    Worker getWorkerByPosition(Coord pos) throws WorkerNotFoundException {

        //Coordinates must be valid
        if (!Coord.validCoord(pos)) {
            throw new WorkerNotFoundException("Worker not found: invalid coordinates given.");
        }

        for(Worker w : this.workers){
            if(pos.equals(w.getPosition())) {
                return w;
            }
        }
        throw new WorkerNotFoundException("There is no worker in the selected position.");
   }

    /**
     * Get a clone of the worker in the given coordinates
     * @param pos the coordinates
     * @return
     */
    public Worker getWorkerCopy (Coord pos) {
        return getWorkerByPosition(pos).clone();
   }

    /**
     * Initializes a worker of the given player, in the given coordinates
     * @param player the player whose worker is to be initialized
     * @param coord the coordinates where the worker will be set
     * @throws IllegalArgumentException when coordinates are invalid
     * @throws IllegalStateException when all the workers of the player are already set
     */
    void initializeWorker(Player player, Coord coord) throws IllegalArgumentException, IllegalStateException {

        Worker worker = workers.stream()
                .filter(w -> w.getPlayerNickname().equals(player.getNickname()))
                .filter(w -> w.getPosition() == null)
                .findFirst().orElse(null);

        if (worker == null) {
            throw new IllegalStateException("Workers for player " + player.getNickname() +
                    " have already been initialized.");
        }

        Space dest = board[coord.x][coord.y];
        if (dest.isOccupied()) {
            throw new IllegalStateException("Tried to initialize a worker on an occupied space.");
        }

        worker.setPosition(coord);
        dest.setOccupied();
    }

    /**
     * Get all the unoccupied positions
     * @return a list containing all the coordinates of the unoccupied board's spaces
     */
    public List<Coord> getUnoccupiedPositions() { //Used only in initialization

        List<Coord> unoccupiedSpaces = new ArrayList<>();

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if(!board[i][j].isOccupied()){
                    unoccupiedSpaces.add(new Coord(i, j));
                }
            }
        }

        return unoccupiedSpaces;
    }

    /**
     * Get all the valid coordinates for the board
     * @return a list containing all the possible coordinates for the board
     */
    public List<Coord> getAllCoord() {
        List<Coord> result = new ArrayList<>();

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                result.add(new Coord(i, j));
            }
        }
        return result;
    }

    // Use this method when moving without a force (i.e. to an unoccupied space)
    void workerMove(Worker w, Coord newPos) throws InvalidCoordinatesException, SpaceFullException, SpaceOccupiedException, IllegalWorkerActionException {
        //Coordinates must be valid
        if(!Coord.validCoord(newPos)){
            throw new InvalidCoordinatesException("Invalid coordinates.");
        }

        // Worker must be on board
        if(!this.workers.contains(w)){
            throw new IllegalWorkerActionException("The worker " + w.toString() + " is not part of the board.");
        }

        //Worker must have a position
        if(w.getPosition() == null) {
            throw new IllegalWorkerActionException("The worker is not initialized " +
                    "(doesn't have a position).");
        }

        Space currentSpace, newSpace;
        currentSpace = this.board[w.getPosition().x][w.getPosition().y];
        newSpace = this.board[newPos.x][newPos.y];

        if ( !newSpace.isOccupied() ) {
            if ( !(newSpace.isDome()) ) {

                w.setPosition(newPos);
                currentSpace.setUnoccupied();
                newSpace.setOccupied();
            }
            else { // isDome
                throw new SpaceFullException("Space is DOME.");
            }
        }
        else { // isOccupied
            throw new SpaceOccupiedException("Space occupied by another worker.");
        }

    }

    // Use this method when moving with a force (i.e. to an occupied space)
    void workerForceMove(Worker w, Coord newPos, Coord forcePos) {
        if(!Coord.validCoord(newPos) || !Coord.validCoord(forcePos)){
            throw new InvalidCoordinatesException("Invalid coordinates.");
        }

        // Check that worker w is on board
        if(!this.workers.contains(w)){
            throw new IllegalWorkerActionException("The worker " + w.toString() + " is not part of the board.");
        }

        // Check that there is a worker in newPos
        Worker otherW;
        try {
            otherW = this.getWorkerByPosition(newPos);
        }
        catch (WorkerNotFoundException e) {
            throw new IllegalWorkerActionException(e.getMessage());
        }

        //Worker must have a position
        if(w.getPosition() == null) {
            throw new IllegalWorkerActionException("The worker is not initialized " +
                    "(doesn't have a position).");
        }


        Space currentSpace, newSpace, forceSpace;
        currentSpace = this.board[w.getPosition().x][w.getPosition().y];
        newSpace = this.board[newPos.x][newPos.y];
        forceSpace = this.board[forcePos.x][forcePos.y];

        w.setPosition(newPos);
        otherW.setPosition(forcePos);

        currentSpace.setUnoccupied();
        newSpace.setOccupied();
        forceSpace.setOccupied();
    }


    void workerBuild(Worker w, Coord buildPos, Level level) throws InvalidCoordinatesException, SpaceFullException, SpaceOccupiedException, IllegalWorkerActionException{

        // Coordinates must be valid
        if(!Coord.validCoord(buildPos)){
            throw new InvalidCoordinatesException("Invalid coordinates.");
        }

        // Check that worker w is on board
        if(!this.workers.contains(w)){
            throw new IllegalWorkerActionException("The worker " + w.toString() + " is not part of the game.");
        }

        // Worker must have a position
        if(w.getPosition() == null){
            throw new IllegalWorkerActionException("The worker is not initialized.");
        }

        this.board[buildPos.x][buildPos.y].setLevel(level);
    }

    void remove (Player player) {
        List<Worker> workersToBeRemoved = workers.stream()
                .filter(w -> w.getPlayerNickname().equals(player.getNickname()))
                .collect(Collectors.toList());

        workersToBeRemoved.stream()
                .map(Worker::getPosition)
                .map(c -> board[c.x][c.y])
                .forEach(Space::setUnoccupied);

        workers.removeAll(workersToBeRemoved);
    }

    /**
     *Get a copy of all the workers on the board
     * @return
     */
    public List<Worker> getAllWorkersCopy(){
        List<Worker> res = new ArrayList<>();
        for(Worker w : this.workers){
            res.add(w.clone());
        }
        return res;
    }

    /**
     *ToString
     * @return
     */
    @Override
    public String toString() {

        String numLine =            "       1         2         3         4         5     \n";
        String horizontalBorder =   "  +---------+---------+---------+---------+---------+\n";
        String topSpaceLine =       "  |         |         |         |         |         |\n";

        String stdColor = "\033[39m";

        String boardString = numLine + horizontalBorder;
        String workerLine;
        String lvl3Line;
        String lvl2Line;
        String lvl1Line;

        for (int j = 0; j < 5; j++) {

            workerLine = "  |";
            lvl3Line = (char) ('A'+j) + " |";
            lvl2Line = "  |";
            lvl1Line = "  |";

            for (int i = 0; i < 5; i++) {
                if (board[i][j].isOccupied()) {
                    try {
                        Worker workerInSpace = this.getWorkerByPosition(new Coord(i ,j));
                        String coloredWorker = "";
                        switch (workerInSpace.getColor()) {
                            case RED:
                                coloredWorker = "\033[31mW";
                                break;
                            case BLUE:
                                coloredWorker = "\033[34mW";
                                break;
                            case YELLOW:
                                coloredWorker = "\033[33mW";
                                break;
                        }
                        workerLine = workerLine + "    " + coloredWorker + stdColor + "    |";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if (board[i][j].isDome())
                    // A space cannot be occupied by a worker and have a dome at the same time
                    workerLine = workerLine + "    ^    |";
                else
                    workerLine = workerLine + "         |";


                switch (board[i][j].getHeight()) {
                    case LVL3:
                        lvl3Line = lvl3Line + "   ***   |";
                        lvl2Line = lvl2Line + "  *****  |";
                        lvl1Line = lvl1Line + " ******* |";
                        break;

                    case LVL2:
                        lvl3Line = lvl3Line + "         |";
                        lvl2Line = lvl2Line + "  *****  |";
                        lvl1Line = lvl1Line + " ******* |";
                        break;

                    case LVL1:
                        lvl3Line = lvl3Line + "         |";
                        lvl2Line = lvl2Line + "         |";
                        lvl1Line = lvl1Line + " ******* |";
                        break;

                    case GROUND:
                        lvl1Line = lvl1Line + "         |";
                        lvl2Line = lvl2Line + "         |";
                        lvl3Line = lvl3Line + "         |";
                        break;
                }
            }


            workerLine = workerLine + "\n";
            lvl3Line = lvl3Line + "\n";
            lvl2Line = lvl2Line + "\n";
            lvl1Line = lvl1Line + "\n";

            boardString = boardString  + topSpaceLine + workerLine + lvl3Line + lvl2Line + lvl1Line + horizontalBorder;
        }
        return boardString;
    }

    /**
     *Clone
     * @return
     */
    @Override
    public Board clone() {
        Space[][] board = new Space[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = getSpace(new Coord(i,j));
            }
        }
        Board result = new Board();
        result.board = board;
        for(Worker w: this.workers){
            result.workers.add(w.clone());
        }

        return result;
    }

    /**
     *Equals
     * @param o
     * @return
     */
    @Override
    public boolean equals (Object o) {
        if (!(o instanceof Board)) return false;
        Board that = (Board) o;
        boolean areEqual = true;

        for (int i = 0; i < BOARD_SIZE && areEqual; i++) {
            for (int j = 0; j < BOARD_SIZE && areEqual; j++) {
                areEqual = this.board[i][j].equals(that.board[i][j]);
            }
        }
        if (areEqual) {
            areEqual = this.workers.equals(that.workers);
        }

        return areEqual;
    }
}

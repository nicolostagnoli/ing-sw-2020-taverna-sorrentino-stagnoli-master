package it.polimi.ingsw.model;

import it.polimi.ingsw.exceptions.model.SpaceFullException;

import java.io.Serializable;

public class Space implements Cloneable, Serializable {

    private static final long serialVersionUID = 4L;

    private boolean occupied;
    private boolean hasDome;
    private Level height;

    public Space() {
        occupied = false;
        hasDome = false;
        height = Level.GROUND;
    }

    /**
     * Set this space as occupied
     */
    public void setOccupied(){
        occupied = true;
    }

    /**
     * Set this space as unoccupied
     */
    public void setUnoccupied(){
        occupied = false;
    }

    /**
     * Check if this space is occupied (i.e. a worker is in this space)
     * @return true if this space is occupied.
     */
    public boolean isOccupied() {
        return occupied;
    }

    /**
     * Check if there is a dome on this space
     * @return true if there is a dome on this space
     */
    public boolean isDome(){
        return this.hasDome;
    }

    /**
     * Get the current {@link Level} of this space
     * @return the level of this space
     */
    public Level getHeight(){
        return height;
    }

    /**
     * Set a Level for this space
     * @param level the level to set
     */
    public void setLevel(Level level) {

        //This check is valid as long as no gods can destroy a dome
        if(this.isDome()){
            throw new SpaceFullException("This space contains a dome.");
        }

        if (level != Level.DOME) {
            height = level;
        } else {
            hasDome = true;
        }
    }

    /**
     * Clone this space
     * @return a clone of this space
     */
    @Override
    public Space clone() {
        Space result = new Space();
        result.occupied = occupied;
        result.hasDome = hasDome;
        result.height = height;

        return result;
    }

    /**
     * Returns a string representation of the object
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        String result = height.toString() + (hasDome ? " (DOME)" : "") +
                (occupied ? " " : " un") + "occupied";
        return result;
    }

    /**
     * Equality test
     * @param o
     * @return true if this and o are equal
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Space)) return false;
        Space that = (Space) o;
        return this.height == that.height &&
                this.occupied == that.occupied &&
                this.hasDome == that.hasDome;
    }
}

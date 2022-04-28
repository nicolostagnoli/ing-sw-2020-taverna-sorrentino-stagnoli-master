package it.polimi.ingsw.model;

import java.io.Serializable;

public class Worker implements Cloneable, Serializable {

    private static final long serialVersionUID = 3L;

    private Color color;
    private Coord position;
    private final Player player;

    public Worker(Player p) {
        this.player = p;
        this.position = null;
    }

    /**
     * Set the position of this worker
     * @param newPos the coordinates to set
     * @throws IllegalArgumentException when coordinates are invalid
     */
    public void setPosition (Coord newPos) throws IllegalArgumentException {

        if (!Coord.validCoord(newPos)) {
            throw new IllegalArgumentException("Invalid Coordinates");
        }

        this.position = newPos;
    }

    /**
     * Get the current position of this worker
     * @return the current position of this worker
     */
    public Coord getPosition(){
        return position;
    }

    /**
     * Get the color of this worker
     * @return the color of this worker
     */
    public Color getColor() {
        return color;
    }

    /**
     * Set the color for this worker
     * @param c the color to set
     */
    public void setColor(Color c){
        this.color = c;
    }

    /**
     * Get the nickname of the player to which this worker belongs
     * @return the nickname of the player to which this worker belongs
     */
    public String getPlayerNickname(){
        return this.player.getNickname();
    }

    /**
     * Get the god of the player to which this worker belongs
     * @return the god of the player to which this worker belongs
     */
    public String getGod() {
        return player.getGod().getName();
    }

    /**
     * Clone this worker
     * @return a clone of this worker
     */
    @Override
    public Worker clone() {
        Player owner = new Player(player.getNickname());
        owner.setGod(player.getGod());
        Worker result = new Worker(owner);
        result.color = color;
        if(this.position != null) {
            result.position = new Coord(position.x, position.y);
        }
        return result;
    }

    /**
     * Equality test.
     * The colors, positions and player's nicknames are compared.
     * Note that if this method is called on a worker whose position is null,
     * then the returned value is the same of {@link Object#equals(Object)}
     * @param o the object to compare with this
     * @return true if this and o are equal
     */
    @Override
    public boolean equals (Object o) {
        if (!(o instanceof Worker)) return false;
        Worker that = (Worker) o;
        if (this.position != null) {
            return this.color == that.color &&
                    this.position.equals(that.position) &&
                    this.player.getNickname().equals(that.player.getNickname());
        }

        else {
            return super.equals(o);
        }
    }

    /**
     * Returns a string representation of the worker
     * @return a string representation of the worker
     */
    @Override
    public String toString() {
        return "[" + color + ", " + position + ", " + player.getNickname() + "]";
    }
}

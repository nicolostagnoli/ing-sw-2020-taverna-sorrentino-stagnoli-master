package it.polimi.ingsw.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Player implements Serializable, Cloneable {

    private static final long serialVersionUID = 6L;

    private final String nickname;
    private God god;
    private List<Worker> workersList;
    private Color workerColor;
    private boolean isStartPlayer;

    public Player(String nickname) {
        this.nickname = nickname;
        this.workerColor = null;
        this.workersList = new ArrayList<>();
        this.workersList.add(new Worker(this));
        this.workersList.add(new Worker(this));
        this.isStartPlayer = false;
    }

    /**
     * Set the god for this player
     * @param god the god to set
     */
    public void setGod(God god){
        this.god = god;
    }

    /**
     * Get the god of this player
     * @return the god of this player
     */
    public God getGod() {
        return god;
    }


    /**
     * Get a specific worker of this player
     * @param num the number of the worker to get (0 or 1)
     * @return a reference to the selected worker
     */
    public Worker getWorker(int num){
        return workersList.get(num);
    }

    /**
     * Get all the workers of this player
     * @return a list containing a copy of the player's workers
     */
    public List<Worker> getWorkersList() {
        List<Worker> result = new ArrayList<>();
        for (Worker w : workersList) {
            result.add(w.clone());
        }
        return result;
    }

    /**
     * Get the nickname of this player
     * @return the nickname of this player
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Set the color for the workers of this player
     * @param c the color to be set
     */
    public void setWorkerColor(Color c) {
        this.workerColor = c;
        this.workersList.get(0).setColor(this.workerColor);
        this.workersList.get(1).setColor(this.workerColor);
    }

    /**
     * Get the color of this player
     * @return the color of this player
     */
    public Color getWorkerColor(){
        return this.workerColor;
    }

    /**
     * Check if this player is the starting one
     * @return true if this player is the start player
     */
    public boolean isStartPlayer() {
        return isStartPlayer;
    }

    /**
     * Set this player as the start player
     */
    void setAsStartPlayer() {
        isStartPlayer = true;
    }

    /**
     * Clone the player
     * @return a clone of this player.
     */
    @Override
    public Player clone() {
        Player result = new Player(this.nickname);
        result.god = this.god;
        result.workerColor = this.workerColor;
        result.workersList = new ArrayList<>();
        result.workersList.add(this.workersList.get(0).clone());
        result.workersList.add(this.workersList.get(1).clone());
        result.isStartPlayer = this.isStartPlayer;

        return result;
    }
}

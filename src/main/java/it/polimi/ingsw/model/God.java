package it.polimi.ingsw.model;

import java.io.Serializable;

public class God implements Serializable {

    private static final long serialVersionUID = 2L;

    private final String name;
    private final String description;

    public God(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Get the name of this god
     * @return the name of this god
     */
    public String getName() { return name; }

    /**
     * Get the description of this god
     * @return the description of this god
     */
    public String getDescription() { return description; }

    /**
     * Equality check
     * @param o
     * @return true if this and o have same name
     */
    @Override
    public boolean equals(Object o){
        if(o instanceof God){
            God other = (God)o;
            return other.getName().equals(this.getName());
        }
        return false;
    }

}

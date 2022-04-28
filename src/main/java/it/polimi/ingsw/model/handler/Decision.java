package it.polimi.ingsw.model.handler;

/**
 * Enumeration used to characterize a {@link Rule}
 */
public enum Decision {
    GRANT, DENY;

    public Decision getOpposite() {
        if (this == GRANT) {
            return DENY;
        } else {
            return GRANT;
        }
    }
}

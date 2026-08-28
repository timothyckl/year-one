package com.p1_7.abstractengine.input;

import java.util.Objects;

/**
 * represents a logical input action recognised by the engine, identified
 * by a unique string.
 */
public class ActionId {

    /** unique identifier for this action */
    private final String id;

    /** placeholder action; used when no specific action is needed */
    public static final ActionId NONE = new ActionId("NONE");

    /**
     * constructs an action identifier with the specified name.
     *
     * @param id the unique string identifier for this action
     */
    public ActionId(String id) {
        this.id = id;
    }

    /**
     * returns the string identifier for this action.
     *
     * @return the action identifier
     */
    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ActionId actionId = (ActionId) obj;
        return Objects.equals(id, actionId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ActionId{" + id + "}";
    }
}

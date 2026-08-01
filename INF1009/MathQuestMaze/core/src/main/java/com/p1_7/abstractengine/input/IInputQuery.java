package com.p1_7.abstractengine.input;

/**
 * read-only query interface for the current frame's input state.
 */
public interface IInputQuery {

    /**
     * returns whether the specified action is currently active
     * (either InputState.PRESSED or InputState.HELD).
     *
     * @param actionId the logical action to query
     * @return true if the action is active this frame
     */
    boolean isActionActive(ActionId actionId);

    /**
     * returns the precise input state for the specified action.
     *
     * @param actionId the logical action to query
     * @return the InputState, or null if the action
     *         is not active this frame
     */
    InputState getActionState(ActionId actionId);
}

package com.p1_7.abstractengine.input;

import java.util.List;

/**
 * write-side interface for modifying key/button → action bindings at runtime.
 */
public interface IInputMapping {

    /**
     * binds a keyboard key to a logical action, replacing any existing
     * binding for that key.
     *
     * @param keyCode  the platform key code to bind
     * @param actionId the logical action to associate with the key
     * @throws IllegalArgumentException if actionId is null
     */
    void bindKey(int keyCode, ActionId actionId);

    /**
     * removes the binding for the given keyboard key, if one exists.
     *
     * @param keyCode the platform key code to unbind
     */
    void unbindKey(int keyCode);

    /**
     * returns every key code currently mapped to the supplied action.
     *
     * @param actionId the action to search for
     * @return a list of matching key codes (may be empty, never null)
     * @throws IllegalArgumentException if actionId is null
     */
    List<Integer> getKeysForAction(ActionId actionId);

    /**
     * binds a controller button to a logical action, replacing any existing
     * binding for that button.
     *
     * @param buttonCode the platform button code to bind
     * @param actionId   the logical action to associate with the button
     * @throws IllegalArgumentException if actionId is null
     */
    void bindButton(int buttonCode, ActionId actionId);

    /**
     * removes the binding for the given controller button, if one exists.
     *
     * @param buttonCode the platform button code to unbind
     */
    void unbindButton(int buttonCode);

    /**
     * removes all key and button bindings associated with the given action.
     *
     * @param actionId the action whose bindings should be removed
     * @throws IllegalArgumentException if actionId is null
     */
    void unbindAction(ActionId actionId);

    /**
     * returns every button code currently mapped to the supplied action.
     *
     * @param actionId the action to search for
     * @return a list of matching button codes (may be empty, never null)
     * @throws IllegalArgumentException if actionId is null
     */
    List<Integer> getButtonsForAction(ActionId actionId);
}

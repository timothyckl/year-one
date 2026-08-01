package com.p1_7.abstractengine.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * maintains the mapping between physical input codes (keyboard keys and
 * controller buttons) and logical ActionId values.
 */
public class InputMapping {

    /** maps keyboard key codes to logical actions */
    private final Map<Integer, ActionId> keyBindings = new HashMap<>();

    /** maps controller button codes to logical actions */
    private final Map<Integer, ActionId> buttonBindings = new HashMap<>();

    public InputMapping() {
        clearAllBindings();
    }

    /**
     * clears all key and button bindings.
     */
    public void clearAllBindings() {
        keyBindings.clear();
        buttonBindings.clear();
    }

    /**
     * returns the logical action bound to the given keyboard key code.
     *
     * @param keyCode the key code
     * @return the bound ActionId, or null if unbound
     */
    public ActionId getActionForKey(int keyCode) {
        return keyBindings.get(keyCode);
    }

    /**
     * returns the logical action bound to the given controller button
     * code.
     *
     * @param buttonCode the button code
     * @return the bound ActionId, or null if unbound
     */
    public ActionId getActionForButton(int buttonCode) {
        return buttonBindings.get(buttonCode);
    }

    /**
     * binds a keyboard key to a logical action.
     *
     * @param keyCode  the key code
     * @param actionId the action to associate with the key
     * @throws IllegalArgumentException if actionId is null
     */
    public void bindKey(int keyCode, ActionId actionId) {
        if (actionId == null) {
            throw new IllegalArgumentException("actionId cannot be null");
        }
        keyBindings.put(keyCode, actionId);
    }

    /**
     * removes the binding for the given keyboard key, if one exists.
     *
     * @param keyCode the key code to unbind
     */
    public void unbindKey(int keyCode) {
        keyBindings.remove(keyCode);
    }

    /**
     * binds a controller button to a logical action.
     *
     * @param buttonCode the button code
     * @param actionId   the action to associate with the button
     * @throws IllegalArgumentException if actionId is null
     */
    public void bindButton(int buttonCode, ActionId actionId) {
        if (actionId == null) {
            throw new IllegalArgumentException("actionId cannot be null");
        }
        buttonBindings.put(buttonCode, actionId);
    }

    /**
     * removes the binding for the given controller button, if one exists.
     *
     * @param buttonCode the button code to unbind
     */
    public void unbindButton(int buttonCode) {
        buttonBindings.remove(buttonCode);
    }

    /**
     * removes all key and button bindings associated with the given action.
     *
     * @param actionId the action whose bindings should be removed
     * @throws IllegalArgumentException if actionId is null
     */
    public void unbindAction(ActionId actionId) {
        if (actionId == null) {
            throw new IllegalArgumentException("actionId cannot be null");
        }

        keyBindings.entrySet().removeIf(entry -> entry.getValue().equals(actionId));
        buttonBindings.entrySet().removeIf(entry -> entry.getValue().equals(actionId));
    }

    /**
     * returns every key code mapped to the supplied action.
     *
     * @param actionId the action to search for
     * @return a list of matching key codes (may be empty)
     * @throws IllegalArgumentException if actionId is null
     */
    public List<Integer> getKeysForAction(ActionId actionId) {
        if (actionId == null) {
            throw new IllegalArgumentException("actionId cannot be null");
        }
        List<Integer> keys = new ArrayList<>();
        for (Map.Entry<Integer, ActionId> entry : keyBindings.entrySet()) {
            if (entry.getValue().equals(actionId)) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    /**
     * returns every button code mapped to the supplied action.
     *
     * @param actionId the action to search for
     * @return a list of matching button codes (may be empty)
     * @throws IllegalArgumentException if actionId is null
     */
    public List<Integer> getButtonsForAction(ActionId actionId) {
        if (actionId == null) {
            throw new IllegalArgumentException("actionId cannot be null");
        }
        List<Integer> buttons = new ArrayList<>();
        for (Map.Entry<Integer, ActionId> entry : buttonBindings.entrySet()) {
            if (entry.getValue().equals(actionId)) {
                buttons.add(entry.getKey());
            }
        }
        return buttons;
    }

    /**
     * returns all unique action ids that have at least one binding.
     *
     * @return a set of all bound actions
     */
    public Set<ActionId> getAllActions() {
        Set<ActionId> actions = new HashSet<>();

        for (Map.Entry<Integer, ActionId> entry : keyBindings.entrySet()) {
            actions.add(entry.getValue());
        }

        for (Map.Entry<Integer, ActionId> entry : buttonBindings.entrySet()) {
            actions.add(entry.getValue());
        }

        return actions;
    }
}

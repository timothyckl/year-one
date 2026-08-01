package com.p1_7.abstractengine.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable engine-level definition of the initial physical inputs bound to
 * a logical action.
 */
public final class InputBindingSpec {

    private final ActionId      actionId;
    private final List<Integer> keyCodes;
    private final List<Integer> buttonCodes;

    /**
     * Creates a binding spec for the given action and input codes.
     *
     * @param actionId    the logical action to bind
     * @param keyCodes    keyboard key codes to bind
     * @param buttonCodes controller button codes to bind
     */
    public InputBindingSpec(ActionId actionId, List<Integer> keyCodes, List<Integer> buttonCodes) {
        if (actionId == null) {
            throw new IllegalArgumentException("actionId cannot be null");
        }
        if (keyCodes == null || buttonCodes == null) {
            throw new IllegalArgumentException("keyCodes and buttonCodes cannot be null");
        }
        this.actionId = actionId;
        this.keyCodes = Collections.unmodifiableList(new ArrayList<>(keyCodes));
        this.buttonCodes = Collections.unmodifiableList(new ArrayList<>(buttonCodes));
    }

    /**
     * Convenience factory for a keyboard-only binding spec.
     *
     * @param actionId the logical action to bind
     * @param keyCodes the keyboard key codes to bind
     * @return a keyboard-only binding spec
     */
    public static InputBindingSpec keys(ActionId actionId, int... keyCodes) {
        List<Integer> codes = new ArrayList<>();
        for (int i = 0; i < keyCodes.length; i++) {
            codes.add(keyCodes[i]);
        }
        return new InputBindingSpec(actionId, codes, Collections.<Integer>emptyList());
    }

    public ActionId getActionId() {
        return actionId;
    }

    public List<Integer> getKeyCodes() {
        return keyCodes;
    }

    public List<Integer> getButtonCodes() {
        return buttonCodes;
    }
}

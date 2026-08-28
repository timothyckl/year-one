package com.p1_7.game.input;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Input;
import com.p1_7.abstractengine.input.ActionId;
import com.p1_7.abstractengine.input.InputBindingSpec;

/**
 * Canonical gameplay and UI actions plus their default input bindings.
 * The settings remap table still exposes only the movement subset.
 */
public final class GameActions {

    public static final ActionId MOVE_UP    = new ActionId("MOVE_UP");
    public static final ActionId MOVE_DOWN  = new ActionId("MOVE_DOWN");
    public static final ActionId MOVE_LEFT  = new ActionId("MOVE_LEFT");
    public static final ActionId MOVE_RIGHT = new ActionId("MOVE_RIGHT");
    public static final ActionId MENU_BACK = new ActionId("MENU_BACK");
    public static final ActionId MENU_CONFIRM = new ActionId("MENU_CONFIRM");
    public static final ActionId POINTER_PRIMARY = new ActionId("POINTER_PRIMARY");
    public static final ActionId DEBUG_TOGGLE = new ActionId("DEBUG_TOGGLE");

    private static final List<BindingSpec> MOVEMENT_BINDINGS = createMovementBindings();
    private static final List<UiBindingSpec> UI_BINDINGS = createUiBindings();

    private GameActions() { }

    /**
     * Returns the ordered movement bindings shown in the settings remap table.
     *
     * @return immutable ordered movement binding definitions
     */
    public static List<BindingSpec> getMovementBindings() {
        return MOVEMENT_BINDINGS;
    }

    /**
     * Returns the default gameplay and UI bindings in an engine-level format
     * that can be applied by InputManager during construction.
     *
     * @return immutable engine-level default bindings
     */
    public static List<InputBindingSpec> getDefaultBindings() {
        List<InputBindingSpec> bindings = new ArrayList<>();
        for (int i = 0; i < MOVEMENT_BINDINGS.size(); i++) {
            BindingSpec binding = MOVEMENT_BINDINGS.get(i);
            bindings.add(InputBindingSpec.keys(binding.getActionId(),
                binding.getPrimaryKeyCode(),
                binding.getAlternateKeyCode()));
        }
        for (int i = 0; i < UI_BINDINGS.size(); i++) {
            bindings.add(UI_BINDINGS.get(i).toInputBindingSpec());
        }
        return Collections.unmodifiableList(bindings);
    }

    private static List<BindingSpec> createMovementBindings() {
        List<BindingSpec> bindings = new ArrayList<>();
        bindings.add(new BindingSpec("Move Up", MOVE_UP, Input.Keys.W, Input.Keys.UP));
        bindings.add(new BindingSpec("Move Down", MOVE_DOWN, Input.Keys.S, Input.Keys.DOWN));
        bindings.add(new BindingSpec("Move Left", MOVE_LEFT, Input.Keys.A, Input.Keys.LEFT));
        bindings.add(new BindingSpec("Move Right", MOVE_RIGHT, Input.Keys.D, Input.Keys.RIGHT));
        return Collections.unmodifiableList(bindings);
    }

    private static List<UiBindingSpec> createUiBindings() {
        List<UiBindingSpec> bindings = new ArrayList<>();
        bindings.add(UiBindingSpec.keys("menu back", MENU_BACK, Input.Keys.ESCAPE, Input.Keys.BACKSPACE));
        bindings.add(UiBindingSpec.keys("menu confirm", MENU_CONFIRM, Input.Keys.SPACE));
        bindings.add(UiBindingSpec.buttons("primary click", POINTER_PRIMARY, Input.Buttons.LEFT));
        bindings.add(UiBindingSpec.keys("toggle debug hitboxes", DEBUG_TOGGLE, Input.Keys.F1));
        return Collections.unmodifiableList(bindings);
    }

    /**
     * Definition of one remappable action row.
     */
    public static final class BindingSpec {
        private final String   label;
        private final ActionId actionId;
        private final int      primaryKeyCode;
        private final int      alternateKeyCode;

        BindingSpec(String label, ActionId actionId, int primaryKeyCode, int alternateKeyCode) {
            this.label            = label;
            this.actionId         = actionId;
            this.primaryKeyCode   = primaryKeyCode;
            this.alternateKeyCode = alternateKeyCode;
        }

        public String getLabel() {
            return label;
        }

        public ActionId getActionId() {
            return actionId;
        }

        public int getPrimaryKeyCode() {
            return primaryKeyCode;
        }

        public int getAlternateKeyCode() {
            return alternateKeyCode;
        }
    }

    private static final class UiBindingSpec {
        private final String label;
        private final ActionId actionId;
        private final List<Integer> keyCodes;
        private final List<Integer> buttonCodes;

        private UiBindingSpec(String label, ActionId actionId, List<Integer> keyCodes,
                              List<Integer> buttonCodes) {
            this.label = label;
            this.actionId = actionId;
            this.keyCodes = Collections.unmodifiableList(new ArrayList<>(keyCodes));
            this.buttonCodes = Collections.unmodifiableList(new ArrayList<>(buttonCodes));
        }

        private static UiBindingSpec keys(String label, ActionId actionId, int... keyCodes) {
            List<Integer> codes = new ArrayList<>();
            for (int i = 0; i < keyCodes.length; i++) {
                codes.add(keyCodes[i]);
            }
            return new UiBindingSpec(label, actionId, codes, Collections.<Integer>emptyList());
        }

        private static UiBindingSpec buttons(String label, ActionId actionId, int... buttonCodes) {
            List<Integer> codes = new ArrayList<>();
            for (int i = 0; i < buttonCodes.length; i++) {
                codes.add(buttonCodes[i]);
            }
            return new UiBindingSpec(label, actionId, Collections.<Integer>emptyList(), codes);
        }
        private InputBindingSpec toInputBindingSpec() {
            return new InputBindingSpec(actionId, keyCodes, buttonCodes);
        }
    }
}

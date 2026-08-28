package com.p1_7.abstractengine.input;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates the InputManager's polling loop: action-state derivation from
 * physical key/button presses, multi-key bindings, and the PRESSED/HELD/RELEASED
 * state machine across consecutive frames.
 *
 * Bindings are seeded via the two-arg constructor using InputBindingSpec rather
 * than reflective field access.
 */
public class InputManagerTest {

    private IInputSource mockSource;
    private ActionId jumpAction;
    private ActionId shootAction;

    @BeforeEach
    public void setUp() {
        mockSource  = Mockito.mock(IInputSource.class);
        jumpAction  = new ActionId("JUMP");
        shootAction = new ActionId("SHOOT");
    }

    // --- helper: build a manager pre-seeded with the given bindings ---

    private InputManager managerWithBindings(InputBindingSpec... specs) {
        List<InputBindingSpec> list = new ArrayList<>();
        for (InputBindingSpec s : specs) {
            list.add(s);
        }
        InputManager mgr = new InputManager(mockSource, list);
        mgr.init();
        return mgr;
    }

    // --- isActionActive: keyboard ---

    @Test
    public void testIsActionActive_ViaKeyboard() {
        InputManager mgr = managerWithBindings(InputBindingSpec.keys(jumpAction, 51));

        Mockito.when(mockSource.isKeyPressed(51)).thenReturn(true);
        mgr.update(0.016f);

        assertTrue(mgr.isActionActive(jumpAction),
            "Manager should return true when the mapped key is physically pressed");
        assertFalse(mgr.isActionActive(shootAction),
            "Manager should return false for unbound/unpressed actions");
    }

    @Test
    public void testIsActionActive_keyNotPressed_returnsFalse() {
        InputManager mgr = managerWithBindings(InputBindingSpec.keys(jumpAction, 51));

        Mockito.when(mockSource.isKeyPressed(51)).thenReturn(false);
        mgr.update(0.016f);

        assertFalse(mgr.isActionActive(jumpAction),
            "Manager should return false when the mapped key is not pressed");
    }

    // --- isActionActive: mouse button ---

    @Test
    public void testIsActionActive_ViaMouse() {
        InputBindingSpec spec = new InputBindingSpec(
            shootAction,
            Collections.<Integer>emptyList(),
            Collections.singletonList(0)
        );
        InputManager mgr = managerWithBindings(spec);

        Mockito.when(mockSource.isButtonPressed(0)).thenReturn(true);
        mgr.update(0.016f);

        assertTrue(mgr.isActionActive(shootAction),
            "Manager should return true when the mapped button is physically pressed");
        assertFalse(mgr.isActionActive(jumpAction),
            "Manager should return false for unbound/unpressed actions");
    }

    // --- multiple keys bound to one action ---

    @Test
    public void testIsActionActive_MultipleKeysMapped() {
        // bind key 51 and key 19 to JUMP; only the alternate key 19 is pressed
        InputManager mgr = managerWithBindings(InputBindingSpec.keys(jumpAction, 51, 19));

        Mockito.when(mockSource.isKeyPressed(51)).thenReturn(false);
        Mockito.when(mockSource.isKeyPressed(19)).thenReturn(true);
        mgr.update(0.016f);

        assertTrue(mgr.isActionActive(jumpAction),
            "Action should trigger if ANY of its mapped keys are pressed");
    }

    // --- multi-frame PRESSED → HELD → RELEASED → inactive state transitions ---

    @Test
    public void testStateTransition_pressed_onFirstFrameDown() {
        InputManager mgr = managerWithBindings(InputBindingSpec.keys(jumpAction, 51));

        Mockito.when(mockSource.isKeyPressed(51)).thenReturn(true);
        mgr.update(0.016f);

        assertEquals(InputState.PRESSED, mgr.getActionState(jumpAction),
            "First frame a key is down must report PRESSED");
    }

    @Test
    public void testStateTransition_held_onSecondConsecutiveFrame() {
        InputManager mgr = managerWithBindings(InputBindingSpec.keys(jumpAction, 51));
        Mockito.when(mockSource.isKeyPressed(51)).thenReturn(true);

        mgr.update(0.016f); // PRESSED
        mgr.update(0.016f); // should become HELD

        assertEquals(InputState.HELD, mgr.getActionState(jumpAction),
            "Second consecutive frame a key is down must report HELD");
    }

    @Test
    public void testStateTransition_released_onFrameAfterKeyUp() {
        InputManager mgr = managerWithBindings(InputBindingSpec.keys(jumpAction, 51));

        Mockito.when(mockSource.isKeyPressed(51)).thenReturn(true);
        mgr.update(0.016f); // PRESSED
        mgr.update(0.016f); // HELD

        Mockito.when(mockSource.isKeyPressed(51)).thenReturn(false);
        mgr.update(0.016f); // should become RELEASED

        assertEquals(InputState.RELEASED, mgr.getActionState(jumpAction),
            "Frame after key is released must report RELEASED");
    }

    @Test
    public void testStateTransition_inactive_afterReleasedFrame() {
        InputManager mgr = managerWithBindings(InputBindingSpec.keys(jumpAction, 51));

        Mockito.when(mockSource.isKeyPressed(51)).thenReturn(true);
        mgr.update(0.016f);
        Mockito.when(mockSource.isKeyPressed(51)).thenReturn(false);
        mgr.update(0.016f); // RELEASED

        mgr.update(0.016f); // next frame — should be inactive

        assertNull(mgr.getActionState(jumpAction),
            "Action must become inactive the frame after RELEASED");
        assertFalse(mgr.isActionActive(jumpAction));
    }

    // --- input disabled ---

    @Test
    public void testInputDisabled_noActionsReported() {
        InputManager mgr = managerWithBindings(InputBindingSpec.keys(jumpAction, 51));
        Mockito.when(mockSource.isKeyPressed(51)).thenReturn(true);
        mgr.setInputEnabled(false);
        mgr.update(0.016f);

        assertFalse(mgr.isActionActive(jumpAction),
            "No actions must be active while input is disabled");
    }
}

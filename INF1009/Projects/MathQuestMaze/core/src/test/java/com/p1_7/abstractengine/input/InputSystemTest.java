package com.p1_7.abstractengine.input;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated Unit Tests for the InputMapping class.
 * Ensures that physical key/button codes correctly map to logical ActionIds,
 * and that bindings can be dynamically updated or removed without side-effects.
 */
public class InputSystemTest {

    private InputMapping mapping;
    private ActionId actionUp;
    private ActionId actionDown;

    @BeforeEach
    public void setUp() {
        // Create a fresh InputMapping before every test to ensure state isolation
        mapping = new InputMapping();
        
        // Setup some dummy actions for testing
        actionUp = new ActionId("UP");
        actionDown = new ActionId("DOWN");
    }

    @Test
    public void testBindAndGetKey() {
        // Arrange: Bind key code 51 (W) to UP
        mapping.bindKey(51, actionUp);

        // Act & Assert
        assertEquals(actionUp, mapping.getActionForKey(51), "Key 51 should map to UP action");
        assertNull(mapping.getActionForKey(99), "Unbound key should return null");
    }

    @Test
    public void testBindAndGetButton() {
        // Arrange: Bind button code 0 (Left Click) to DOWN
        mapping.bindButton(0, actionDown);

        // Act & Assert
        assertEquals(actionDown, mapping.getActionForButton(0), "Button 0 should map to DOWN action");
        assertNull(mapping.getActionForButton(1), "Unbound button should return null");
    }

    @Test
    public void testUnbindKey() {
        // Arrange
        mapping.bindKey(51, actionUp);
        assertNotNull(mapping.getActionForKey(51));

        // Act
        mapping.unbindKey(51);

        // Assert
        assertNull(mapping.getActionForKey(51), "Key should be unbound and return null");
    }

    @Test
    public void testUnbindAction_RemovesOnlySpecificAction() {
        // Arrange: Bind Primary and Alternate keys to UP
        mapping.bindKey(51, actionUp);   // W
        mapping.bindKey(19, actionUp);   // Up Arrow
        
        // Bind a key to DOWN to ensure it isn't affected
        mapping.bindKey(47, actionDown); // S

        // Act: Unbind the UP action entirely
        mapping.unbindAction(actionUp);

        // Assert: UP bindings should be gone, but DOWN should remain
        assertNull(mapping.getActionForKey(51), "Primary UP key should be unbound");
        assertNull(mapping.getActionForKey(19), "Alternate UP key should be unbound");
        assertEquals(actionDown, mapping.getActionForKey(47), "DOWN key should remain bound");
    }

    @Test
    public void testGetKeysForAction() {
        // Arrange
        mapping.bindKey(51, actionUp);
        mapping.bindKey(19, actionUp);
        mapping.bindKey(47, actionDown);

        // Act
        List<Integer> upKeys = mapping.getKeysForAction(actionUp);

        // Assert
        assertEquals(2, upKeys.size(), "Should return exactly 2 keys for the UP action");
        assertTrue(upKeys.contains(51));
        assertTrue(upKeys.contains(19));
    }

    @Test
    public void testGetAllActions() {
        // Arrange
        mapping.bindKey(51, actionUp);
        mapping.bindButton(0, actionDown);

        // Act
        Set<ActionId> allActions = mapping.getAllActions();

        // Assert
        assertEquals(2, allActions.size(), "Should track exactly 2 unique actions");
        assertTrue(allActions.contains(actionUp));
        assertTrue(allActions.contains(actionDown));
    }

    @Test
    public void testNullActionIdThrowsException() {
        // Act & Assert: Verify that the fail-safes work
        assertThrows(IllegalArgumentException.class, () -> {
            mapping.bindKey(51, null);
        }, "Binding a null ActionId to a key should throw an exception");

        assertThrows(IllegalArgumentException.class, () -> {
            mapping.bindButton(0, null);
        }, "Binding a null ActionId to a button should throw an exception");

        assertThrows(IllegalArgumentException.class, () -> {
            mapping.unbindAction(null);
        }, "Unbinding a null ActionId should throw an exception");
    }
}
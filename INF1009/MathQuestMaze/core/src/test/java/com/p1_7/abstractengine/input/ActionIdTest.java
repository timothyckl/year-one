package com.p1_7.abstractengine.input;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Baseline unit test to verify the CI/CD pipeline and testing framework setup.
 * Tests the fundamental ActionId class for equality and immutability.
 */
public class ActionIdTest {

    @Test
    public void testActionIdEquality() {
        // Arrange
        ActionId action1 = new ActionId("JUMP");
        ActionId action2 = new ActionId("JUMP");
        ActionId action3 = new ActionId("SHOOT");

        // Act & Assert
        assertEquals(action1, action2, "ActionIds with the same string should be equal");
        assertNotEquals(action1, action3, "ActionIds with different strings should not be equal");
    }

    @Test
    public void testActionIdHashCode() {
        // Arrange
        ActionId action1 = new ActionId("MOVE_LEFT");
        ActionId action2 = new ActionId("MOVE_LEFT");

        // Act & Assert
        assertEquals(action1.hashCode(), action2.hashCode(), "Identical ActionIds must have identical hash codes for Map lookups");
    }

    @Test
    public void testNoneActionConstant() {
        // Assert
        assertNotNull(ActionId.NONE, "The NONE constant should be initialized");
        assertEquals("NONE", ActionId.NONE.getId(), "The NONE constant should have the ID 'NONE'");
    }
}
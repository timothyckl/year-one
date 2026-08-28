package com.p1_7.abstractengine.input;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates the InputManager's IInputExtensionRegistry implementation.
 * Ensures that the engine can safely accept custom input handlers from the game layer.
 */
public class InputExtensionRegistryTest {

    private InputManager inputManager;

    // A dummy extension interface representing something the engine doesn't natively know about (like a Gamepad)
    private interface IGamepadExtension extends IInputExtension {
        float getAxis(int axisCode);
    }

    @BeforeEach
    public void setUp() {
        IInputSource mockSource = Mockito.mock(IInputSource.class);
        inputManager = new InputManager(mockSource);
    }

    @Test
    public void testRegisterAndRetrieveExtension() {
        // Arrange: Create a mock of our custom extension
        IGamepadExtension mockGamepad = Mockito.mock(IGamepadExtension.class);

        // Act
        inputManager.registerExtension(IGamepadExtension.class, mockGamepad);

        // Assert
        assertTrue(inputManager.hasExtension(IGamepadExtension.class), "Manager should report the extension exists");
        
        IGamepadExtension retrieved = inputManager.getExtension(IGamepadExtension.class);
        assertEquals(mockGamepad, retrieved, "Should retrieve the exact instance we registered");
    }

    @Test
    public void testGetMissingExtensionThrowsException() {
        // Assert that asking for something that doesn't exist fails cleanly
        assertFalse(inputManager.hasExtension(IGamepadExtension.class));

        assertThrows(IllegalArgumentException.class, () -> {
            inputManager.getExtension(IGamepadExtension.class);
        }, "Requesting an unregistered extension must throw an IllegalArgumentException");
    }
}
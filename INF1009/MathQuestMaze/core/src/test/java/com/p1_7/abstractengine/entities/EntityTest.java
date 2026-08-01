package com.p1_7.abstractengine.entities;

import org.junit.jupiter.api.Test;
import com.p1_7.abstractengine.entity.Entity;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates the base logic of the abstract Entity class, 
 * ensuring UUID generation and active-state tracking work correctly.
 */
public class EntityTest {

    // Create a minimal concrete implementation to test the abstract base.
    // Notice we do NOT need to override getAssetPath or getTransform, 
    // because those belong to IRenderItem, not the base Entity class!
    private static class DummyEntity extends Entity {
    }

    @Test
    public void testEntityInitialization() {
        DummyEntity entity1 = new DummyEntity();
        DummyEntity entity2 = new DummyEntity();

        // Assert: Entities must have a valid, non-null UUID upon creation
        assertNotNull(entity1.getId(), "Entity ID should be automatically generated");
        assertNotNull(entity2.getId(), "Entity ID should be automatically generated");

        // Assert: UUIDs must be strictly unique
        assertNotEquals(entity1.getId(), entity2.getId(), "Generated Entity UUIDs must be unique");
    }

    @Test
    public void testActiveStateToggle() {
        DummyEntity entity = new DummyEntity();

        // Assert: Entities should be active by default
        assertTrue(entity.isActive(), "Entities must be active by default");

        // Act & Assert: Toggling state
        entity.setActive(false);
        assertFalse(entity.isActive(), "Entity should report inactive after being set");

        entity.setActive(true);
        assertTrue(entity.isActive(), "Entity should report active after being set");
    }
}
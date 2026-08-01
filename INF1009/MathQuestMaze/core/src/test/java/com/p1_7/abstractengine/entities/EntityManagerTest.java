package com.p1_7.abstractengine.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.entity.EntityManager;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated Unit Tests for the EntityManager class.
 * Verifies the lifecycle of entities (Creation, Retrieval, Updating, and Deletion).
 */
public class EntityManagerTest {

    private EntityManager entityManager;

    /**
     * A concrete dummy entity used strictly for testing the abstract Entity class.
     */
    private static class DummyEntity extends Entity {
        public DummyEntity() {
            super();
        }
    }

    @BeforeEach
    public void setUp() {
        // Isolate state before each test
        entityManager = new EntityManager();
    }

    @Test
    public void testCreateEntity() {
        // Act
        Entity entity = entityManager.createEntity(DummyEntity::new);

        // Assert
        assertNotNull(entity, "Created entity should not be null");
        assertNotNull(entity.getId(), "Entity should have a generated UUID");
        assertTrue(entity.isActive(), "Entities should be active by default upon creation");
        
        // Verify it was stored correctly
        assertEquals(entity, entityManager.getEntity(entity.getId()), "EntityManager should store and retrieve the created entity");
    }

    @Test
    public void testUpdateEntity() {
        // Arrange
        Entity entity = entityManager.createEntity(DummyEntity::new);
        UUID id = entity.getId();

        // Act: Deactivate the entity
        entityManager.updateEntity(id, false);

        // Assert
        assertFalse(entity.isActive(), "Entity should be deactivated after updateEntity is called");
    }

    @Test
    public void testRemoveEntity() {
        // Arrange
        Entity entity = entityManager.createEntity(DummyEntity::new);
        UUID id = entity.getId();

        // Act
        entityManager.removeEntity(id);

        // Assert
        assertNull(entityManager.getEntity(id), "Entity should be completely removed from the manager");
    }

    @Test
    public void testGetAllEntityIds() {
        // Arrange
        Entity e1 = entityManager.createEntity(DummyEntity::new);
        Entity e2 = entityManager.createEntity(DummyEntity::new);

        // Act
        List<UUID> ids = entityManager.getAllEntityIds();

        // Assert
        assertEquals(2, ids.size(), "Should return exactly 2 UUIDs");
        assertTrue(ids.contains(e1.getId()), "List must contain the first entity's UUID");
        assertTrue(ids.contains(e2.getId()), "List must contain the second entity's UUID");
    }
}
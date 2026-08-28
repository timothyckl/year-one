package com.p1_7.abstractengine.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.p1_7.abstractengine.engine.Manager;

/**
 * concrete entity manager providing both the read-only repository and
 * write-side mutator contracts.
 */
public class EntityManager extends Manager implements IEntityManager {

    /** the backing store of all live entities */
    private final List<Entity> entities = new ArrayList<>();

    /**
     * creates an entity via the supplied factory, adds it to the internal store, and returns it.
     *
     * @param factory the factory that constructs the concrete entity
     * @return the newly created and registered entity
     */
    @Override
    public Entity createEntity(EntityFactory factory) {
        Entity entity = factory.create();
        entities.add(entity);
        return entity;
    }

    /**
     * sets the active flag on the entity identified by id.
     *
     * @param id     the UUID of the target entity
     * @param active the desired active state
     */
    @Override
    public void updateEntity(UUID id, boolean active) {
        for (int i = 0; i < entities.size(); i++) {
            if (entities.get(i).getId().equals(id)) {
                entities.get(i).setActive(active);
                return;
            }
        }
    }

    /**
     * removes the entity identified by id from the store.
     *
     * @param id the UUID of the entity to remove
     */
    @Override
    public void removeEntity(UUID id) {
        for (int i = 0; i < entities.size(); i++) {
            if (entities.get(i).getId().equals(id)) {
                entities.remove(i);
                return;
            }
        }
    }

    /**
     * retrieves a single entity by its unique identifier.
     *
     * @param id the UUID to look up
     * @return the matching entity, or null if not found
     */
    @Override
    public Entity getEntity(UUID id) {
        for (int i = 0; i < entities.size(); i++) {
            if (entities.get(i).getId().equals(id)) {
                return entities.get(i);
            }
        }
        return null;
    }

    /**
     * returns the UUIDs of every entity in the store.
     *
     * @return a list of all entity identifiers
     */
    @Override
    public List<UUID> getAllEntityIds() {
        List<UUID> ids = new ArrayList<>(entities.size());
        for (int i = 0; i < entities.size(); i++) {
            ids.add(entities.get(i).getId());
        }
        return ids;
    }

    /**
     * returns a debug string listing the UUIDs of all entities.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("EntityManager{");
        for (int i = 0; i < entities.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(entities.get(i).getId());
        }
        sb.append('}');
        return sb.toString();
    }
}

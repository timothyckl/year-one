package com.p1_7.abstractengine.entity;

import java.util.UUID;

/**
 * read-only view of the entity store for querying entities without
 * mutating the store.
 */
public interface IEntityRepository {

    /**
     * retrieves a single entity by its unique identifier.
     *
     * @param id the UUID of the entity to look up
     * @return the matching Entity, or null if none exists
     */
    Entity getEntity(UUID id);

    /**
     * returns an iterable over the UUIDs of every entity currently in
     * the store.
     *
     * @return an iterable of entity identifiers
     */
    Iterable<UUID> getAllEntityIds();
}

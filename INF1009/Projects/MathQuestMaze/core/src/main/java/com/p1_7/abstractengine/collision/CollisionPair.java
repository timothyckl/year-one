package com.p1_7.abstractengine.collision;

/**
 * immutable data holder representing a detected collision between two entities.
 * stores references to both colliding entities for processing in the resolution phase.
 */
public class CollisionPair {
    private final ICollidable entityA;
    private final ICollidable entityB;

    /**
     * creates a collision pair between two entities.
     *
     * @param entityA the first colliding entity
     * @param entityB the second colliding entity
     */
    public CollisionPair(ICollidable entityA, ICollidable entityB) {
        this.entityA = entityA;
        this.entityB = entityB;
    }

    /**
     * gets the first entity in the collision pair.
     *
     * @return the first colliding entity
     */
    public ICollidable getEntityA() {
        return entityA;
    }

    /**
     * gets the second entity in the collision pair.
     *
     * @return the second colliding entity
     */
    public ICollidable getEntityB() {
        return entityB;
    }
}

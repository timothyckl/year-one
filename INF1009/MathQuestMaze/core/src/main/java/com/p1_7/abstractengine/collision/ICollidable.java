package com.p1_7.abstractengine.collision;

/**
 * capability interface for any entity that participates in collision detection.
 */
public interface ICollidable {

    /**
     * returns the collision bounds that represent this entity's shape.
     *
     * @return the bounding volume; must not be null
     */
    IBounds getBounds();

    /**
     * called when this entity has been found to overlap with another.
     *
     * @param other the collidable that this entity collided with
     */
    void onCollision(ICollidable other);
}

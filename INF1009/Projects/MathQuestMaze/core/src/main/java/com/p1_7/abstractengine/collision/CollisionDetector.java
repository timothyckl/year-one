package com.p1_7.abstractengine.collision;

/**
 * stateless utility that tests two ICollidable objects for overlap.
 */
public class CollisionDetector {

    /**
     * determines whether the bounding volumes of the two
     * collidables overlap.
     *
     * @param a the first collidable
     * @param b the second collidable
     * @return true if their bounds overlap
     */
    public boolean checkCollision(ICollidable a, ICollidable b) {
        return a.getBounds().overlaps(b.getBounds());
    }
}

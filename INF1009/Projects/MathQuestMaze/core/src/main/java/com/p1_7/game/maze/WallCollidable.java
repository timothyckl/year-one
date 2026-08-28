package com.p1_7.game.maze;

import com.p1_7.abstractengine.collision.IBounds;
import com.p1_7.abstractengine.collision.ICollidable;
import com.p1_7.game.spatial.Bounds2D;

/**
 * lightweight immutable wrapper for a single wall rectangle from MazeLayout.
 *
 * exposes the wall as an ICollidable so MazeCollisionManager can include it
 * in pairwise overlap detection. walls are static and do not react to collisions.
 */
public final class WallCollidable implements ICollidable {

    /** the bounding box of this wall, built once at construction time */
    private final Bounds2D bounds;

    /**
     * constructs a WallCollidable from a four-element rect array.
     *
     * @param rect the wall dimensions as [x, y, width, height]
     */
    public WallCollidable(float[] rect) {
        this.bounds = new Bounds2D(rect[0], rect[1], rect[2], rect[3]);
    }

    /**
     * returns the bounding box for this wall.
     *
     * @return the immutable bounds of this wall
     */
    @Override
    public IBounds getBounds() {
        return bounds;
    }

    /**
     * no-op — walls are static and do not react to collisions.
     *
     * @param other the collidable that collided with this wall
     */
    @Override
    public void onCollision(ICollidable other) {
        // walls are static; position correction is handled by MazeCollisionManager
    }
}

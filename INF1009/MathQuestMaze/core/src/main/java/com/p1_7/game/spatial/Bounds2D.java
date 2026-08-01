package com.p1_7.game.spatial;

import com.p1_7.abstractengine.collision.IBounds;

/**
 * mutable 2D axis-aligned bounding box implementing IBounds.
 *
 * stores position and extent as float[2] arrays. getters return defensive
 * copies to prevent external mutation, mirroring the pattern in Transform2D.
 */
public class Bounds2D implements IBounds {

    // bottom-left corner of the bounding box [x, y]
    private final float[] minPosition = new float[2];

    // size of the bounding box [width, height]
    private final float[] extent = new float[2];

    /**
     * constructs a 2D bounding box from a position and size.
     *
     * @param x      left edge of the box
     * @param y      bottom edge of the box
     * @param width  width of the box
     * @param height height of the box
     */
    public Bounds2D(float x, float y, float width, float height) {
        this.minPosition[0] = x;
        this.minPosition[1] = y;
        this.extent[0]      = width;
        this.extent[1]      = height;
    }

    /**
     * returns true if this AABB overlaps with another IBounds.
     *
     * @param other the bounds to test against; must not be null
     * @return true if the two boxes intersect on both axes
     */
    @Override
    public boolean overlaps(IBounds other) {
        float[] oMin = other.getMinPosition();
        float[] oExt = other.getExtent();

        // standard AABB overlap test on both axes simultaneously
        return minPosition[0] < oMin[0] + oExt[0] && minPosition[0] + extent[0] > oMin[0]
            && minPosition[1] < oMin[1] + oExt[1] && minPosition[1] + extent[1] > oMin[1];
    }

    /**
     * returns a defensive copy of the minimum corner position.
     *
     * @return a new [x, y] array
     */
    @Override
    public float[] getMinPosition() {
        return minPosition.clone();
    }

    /**
     * returns a defensive copy of the extent vector.
     *
     * @return a new [width, height] array
     */
    @Override
    public float[] getExtent() {
        return extent.clone();
    }

    /**
     * updates the position and extent of this bounding box in place.
     *
     * @param minPosition the new [x, y] minimum corner
     * @param extent      the new [width, height] size
     */
    @Override
    public void set(float[] minPosition, float[] extent) {
        this.minPosition[0] = minPosition[0];
        this.minPosition[1] = minPosition[1];
        this.extent[0]      = extent[0];
        this.extent[1]      = extent[1];
    }

    /**
     * returns the number of spatial dimensions this bounds operates in.
     *
     * @return always 2 for this implementation
     */
    @Override
    public int getDimensions() {
        return 2;
    }
}

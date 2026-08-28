package com.p1_7.game.spatial;

import com.p1_7.abstractengine.transform.ITransform;

/**
 * concrete 2D implementation of ITransform.
 *
 * manages position (x, y) and size (width, height) in 2D space.
 * per-axis indexed accessors prevent external mutation of internal arrays.
 */
public class Transform2D implements ITransform {

    // x, y coordinates
    private float[] position = new float[2];

    // width, height dimensions
    private float[] size = new float[2];

    /**
     * constructs a 2D transform with the specified position and size.
     *
     * @param x      the x coordinate
     * @param y      the y coordinate
     * @param width  the width
     * @param height the height
     */
    public Transform2D(float x, float y, float width, float height) {
        this.position[0] = x;
        this.position[1] = y;
        this.size[0] = width;
        this.size[1] = height;
    }

    @Override
    public float getPosition(int axis) {
        return position[axis];
    }

    @Override
    public void setPosition(int axis, float value) {
        position[axis] = value;
    }

    @Override
    public float getSize(int axis) {
        return size[axis];
    }

    @Override
    public void setSize(int axis, float value) {
        size[axis] = value;
    }

    @Override
    public int getDimensions() {
        return 2;
    }
}

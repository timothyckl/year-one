package com.p1_7.abstractengine.transform;

/**
 * dimension-agnostic spatial state for an entity.
 *
 * uses per-axis indexed accessors to prevent external mutation of
 * internal state through leaked array references.
 */
public interface ITransform {

    /**
     * returns the position component for the given axis.
     *
     * @param axis the axis index (e.g. 0 for x, 1 for y)
     * @return the position value along that axis
     */
    float getPosition(int axis);

    /**
     * sets the position component for the given axis.
     *
     * @param axis  the axis index (e.g. 0 for x, 1 for y)
     * @param value the new position value along that axis
     */
    void setPosition(int axis, float value);

    /**
     * returns the size component for the given axis.
     *
     * @param axis the axis index (e.g. 0 for width, 1 for height)
     * @return the size value along that axis
     */
    float getSize(int axis);

    /**
     * sets the size component for the given axis.
     *
     * @param axis  the axis index (e.g. 0 for width, 1 for height)
     * @param value the new size value along that axis
     */
    void setSize(int axis, float value);

    /**
     * returns the number of spatial dimensions this transform operates in.
     *
     * @return the dimensionality
     */
    int getDimensions();
}

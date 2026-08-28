package com.p1_7.abstractengine.collision;

/**
 * dimension-agnostic collision boundary for an entity; array lengths are
 * determined by the concrete implementation.
 */
public interface IBounds {

    /**
     * determines whether this bounds overlaps with another bounds.
     *
     * @param other the bounds to check against; must not be null
     * @return true if the bounds overlap, false otherwise
     */
    boolean overlaps(IBounds other);

    /**
     * returns the minimum corner position as a float array.
     *
     * @return the minimum position vector; length equals the number of dimensions
     */
    float[] getMinPosition();

    /**
     * returns the extent (size) in each dimension as a float array.
     *
     * @return the size vector; length equals the number of dimensions
     */
    float[] getExtent();

    /**
     * updates the bounds to the specified minimum position and extent.
     *
     * @param minPosition the new minimum corner position
     * @param extent the new size in each dimension
     */
    void set(float[] minPosition, float[] extent);

    /**
     * returns the number of spatial dimensions this bounds operates in.
     *
     * @return the dimensionality (length of position and extent arrays)
     */
    int getDimensions();
}

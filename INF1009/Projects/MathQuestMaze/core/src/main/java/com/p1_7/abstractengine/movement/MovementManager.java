package com.p1_7.abstractengine.movement;

import java.util.ArrayList;
import java.util.List;

import com.p1_7.abstractengine.engine.UpdatableManager;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.abstractengine.transform.ITransformable;

/**
 * per-frame manager that advances all registered IMovable entities and
 * optionally clamps their positions to world-space boundaries.
 */
public class MovementManager extends UpdatableManager {

    /** all movable entities managed by this manager */
    private final List<IMovable> movables = new ArrayList<>();

    /** whether boundary clamping is applied after movement */
    private boolean boundariesEnabled = true;

    /** per-dimension lower bound (inclusive); null until set */
    private float[] boundsMin;

    /** per-dimension upper bound (exclusive before size offset); null until set */
    private float[] boundsMax;

    /**
     * adds an IMovable to the update list.
     *
     * @param movable the movable entity to register
     */
    public void registerMovable(IMovable movable) {
        movables.add(movable);
    }

    /**
     * removes an IMovable from the update list.
     *
     * @param movable the movable entity to unregister
     */
    public void unregisterMovable(IMovable movable) {
        movables.remove(movable);
    }

    /**
     * enables or disables boundary clamping after movement.
     *
     * @param enabled true to clamp positions to world bounds
     */
    public void setBoundariesEnabled(boolean enabled) {
        this.boundariesEnabled = enabled;
    }

    /**
     * sets the per-dimension world-space boundaries used for position clamping.
     *
     * @param min the lower bounds per dimension
     * @param max the upper bounds per dimension
     * @throws IllegalArgumentException if arrays are null, mismatched, empty, or min[i] > max[i]
     */
    public void setWorldBounds(float[] min, float[] max) {
        if (min == null) {
            throw new IllegalArgumentException("min cannot be null");
        }
        if (max == null) {
            throw new IllegalArgumentException("max cannot be null");
        }
        if (min.length != max.length) {
            throw new IllegalArgumentException("min and max must have the same length");
        }
        if (min.length == 0) {
            throw new IllegalArgumentException("min and max cannot be empty");
        }
        for (int i = 0; i < min.length; i++) {
            if (min[i] > max[i]) {
                throw new IllegalArgumentException(
                    "min[" + i + "] (" + min[i] + ") cannot be greater than max[" + i + "] (" + max[i] + ")"
                );
            }
        }
        this.boundsMin = min;
        this.boundsMax = max;
    }

    /**
     * advances every registered movable by one physics step, then
     * clamps positions to the world bounds if enabled.
     *
     * @param deltaTime seconds elapsed since the previous frame
     */
    @Override
    protected void onUpdate(float deltaTime) {
        for (int i = 0; i < movables.size(); i++) {
            IMovable movable = movables.get(i);

            movable.move(deltaTime);

            if (boundariesEnabled && boundsMin != null && boundsMax != null
                    && movable instanceof ITransformable) {
                clampToBounds((ITransformable) movable);
            }
        }
    }

    /**
     * clamps each dimension of the entity's position so that the
     * entire body (position + size) stays within boundsMin
     * and boundsMax.
     *
     * @param transformable the entity whose position will be clamped
     */
    private void clampToBounds(ITransformable transformable) {
        ITransform transform = transformable.getTransform();
        int dimensions = transform.getDimensions();

        for (int i = 0; i < dimensions; i++) {
            float pos = transform.getPosition(i);
            float size = transform.getSize(i);

            if (pos < boundsMin[i]) {
                pos = boundsMin[i];
            }
            if (pos + size > boundsMax[i]) {
                pos = boundsMax[i] - size;
            }

            transform.setPosition(i, pos);
        }
    }
}

package com.p1_7.abstractengine.transform;

/**
 * marker contract for any object that exposes spatial state via an ITransform.
 */
public interface ITransformable {

    /**
     * returns the spatial transform attached to this object.
     *
     * @return the transform; must not be null
     */
    ITransform getTransform();
}

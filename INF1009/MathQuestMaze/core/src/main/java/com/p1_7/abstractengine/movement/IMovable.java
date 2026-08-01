package com.p1_7.abstractengine.movement;

/**
 * capability interface for any entity that moves under the influence of
 * acceleration and velocity; vector lengths match the entity's transform dimensionality.
 */
public interface IMovable {

    /**
     * returns the current acceleration vector.
     *
     * @return the acceleration as a float array
     */
    float[] getAcceleration();

    /**
     * sets the acceleration to the supplied values.
     *
     * @param acceleration the new acceleration vector
     */
    void setAcceleration(float[] acceleration);

    /**
     * returns the current velocity vector.
     *
     * @return the velocity as a float array
     */
    float[] getVelocity();

    /**
     * sets the velocity to the supplied values.
     *
     * @param velocity the new velocity vector
     */
    void setVelocity(float[] velocity);

    /**
     * advances the entity by one physics step.
     *
     * @param deltaTime seconds elapsed since the previous frame
     */
    void move(float deltaTime);
}

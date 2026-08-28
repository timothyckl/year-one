package com.p1_7.abstractengine.engine;

/**
 * contract for any object that must be updated once per frame.
 */
public interface IUpdatable {

    /**
     * advances the object's internal state by one tick.
     *
     * @param deltaTime seconds elapsed since the previous frame
     */
    void update(float deltaTime);
}

package com.p1_7.abstractengine.engine;

/**
 * abstract manager that also participates in the per-frame update loop;
 * subclasses implement onUpdate(float) for their tick logic.
 */
public abstract class UpdatableManager extends Manager implements IUpdatable {

    /**
     * advances the manager by one frame.
     *
     * @param deltaTime seconds elapsed since the previous frame
     */
    @Override
    public final void update(float deltaTime) {
        onUpdate(deltaTime);
    }

    /**
     * hook that subclasses must implement to perform their per-frame logic.
     *
     * @param deltaTime seconds elapsed since the previous frame
     */
    protected abstract void onUpdate(float deltaTime);
}

package com.p1_7.abstractengine.scene;

import com.p1_7.abstractengine.render.IRenderQueue;

/**
 * abstract base class for every scene; update is skipped while paused but
 * submitRenderable is always called.
 */
public abstract class Scene {

    /** the unique key used to register and look up this scene */
    protected String name;

    /** when true the per-frame update hook is skipped */
    protected boolean paused;

    /**
     * called once when this scene becomes the active scene.
     *
     * @param context the current engine context
     */
    public abstract void onEnter(SceneContext context);

    /**
     * called once when this scene is about to be replaced by another.
     *
     * @param context the current engine context
     */
    public abstract void onExit(SceneContext context);

    /**
     * called when this scene is temporarily suspended; default is a no-op.
     *
     * @param context the current engine context
     */
    public void onSuspend(SceneContext context) {
    }

    /**
     * called when this scene resumes after suspension; default is a no-op.
     *
     * @param context the current engine context
     */
    public void onResume(SceneContext context) {
    }

    /**
     * per-frame update hook. not called while the scene is paused.
     *
     * @param deltaTime seconds elapsed since the previous frame
     * @param context   the current engine context
     */
    public abstract void update(float deltaTime, SceneContext context);

    /**
     * per-frame hook where the scene pushes its visible entities into
     * the render queue. always called, even when the scene is paused.
     *
     * @param renderQueue the render queue to submit items to
     */
    public abstract void submitRenderable(IRenderQueue renderQueue);

    /**
     * returns the name (key) of this scene.
     *
     * @return the scene name; never null
     */
    public String getName() {
        return name;
    }

    /**
     * returns whether this scene is currently paused.
     *
     * @return true if the scene's update hook is being skipped
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * sets the paused state of this scene.
     *
     * @param paused true to suspend per-frame updates
     */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}

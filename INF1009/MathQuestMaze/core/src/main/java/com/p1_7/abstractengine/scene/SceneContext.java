package com.p1_7.abstractengine.scene;

/**
 * snapshot of engine state passed into every Scene callback, providing access
 * to registered services and scene transitions.
 */
public interface SceneContext {

    /**
     * looks up a service registered under the given type key.
     *
     * @param type the class key to look up
     * @param <T>  the service type
     * @return the registered service instance; never null
     * @throws IllegalArgumentException if no service is registered for the given type
     */
    <T> T get(Class<T> type);

    /**
     * requests a transition to the scene identified by key.
     *
     * @param key the name of the target scene
     */
    void changeScene(String key);

    /**
     * requests a deferred suspend transition, preserving the current scene's state.
     *
     * @param key the name of the target scene
     */
    void suspendScene(String key);

    /**
     * returns the scene registered under the specified key.
     *
     * @param key the name of the scene to retrieve
     * @return the Scene, or null if not found
     */
    Scene getScene(String key);

    /**
     * returns the key of the currently suspended scene, or null if no scene is suspended.
     *
     * @return the suspended scene key, or null
     */
    String getSuspendedSceneKey();

    /**
     * clears the suspended scene record so it can no longer be resumed.
     */
    void clearSuspendedScene();
}

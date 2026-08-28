package com.p1_7.abstractengine.scene;

import java.util.HashMap;
import java.util.Map;

import com.p1_7.abstractengine.engine.IManager;
import com.p1_7.abstractengine.engine.ManagerResolver;
import com.p1_7.abstractengine.engine.UpdatableManager;
import com.p1_7.abstractengine.entity.EntityManager;
import com.p1_7.abstractengine.entity.IEntityManager;
import com.p1_7.abstractengine.input.IInputExtensionRegistry;
import com.p1_7.abstractengine.input.IInputManager;
import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.abstractengine.input.InputManager;
import com.p1_7.abstractengine.render.IRenderQueue;
import com.p1_7.abstractengine.render.RenderManager;

/**
 * manages the scene registry and drives the active scene's lifecycle and
 * per-frame callbacks.
 */
public class SceneManager extends UpdatableManager {

    /** all registered scenes, keyed by name */
    private final Map<String, Scene> scenes = new HashMap<>();

    /** services available to scenes via SceneContext.get() */
    private final Map<Class<?>, Object> serviceMap = new HashMap<>();

    /** the name of the currently active scene (may be null) */
    private String currentKey;

    /** the name of the scene to transition to next frame (may be null) */
    private String pendingKey;

    /** the name of the scene to suspend-transition to next frame (may be null) */
    private String pendingSuspendKey;

    /** tracks which scene was suspended (for resume detection) */
    private String suspendedSceneKey;

    /** the context object passed into scene callbacks */
    private SceneContext context;

    /**
     * returns the direct dependencies of this manager.
     *
     * @return array of manager types this manager depends on
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends IManager>[] getDependencies() {
        return new Class[] {
            EntityManager.class,
            RenderManager.class,
            InputManager.class
        };
    }

    /**
     * binds a service instance under the given type key for lookup via SceneContext.get().
     * must be called before engine.init().
     *
     * @param type    the class key to register under
     * @param service the service instance
     */
    public <T> void registerService(Class<T> type, T service) {
        serviceMap.put(type, service);
    }

    /**
     * populates the service map with engine-provided services resolved from wiring.
     *
     * @param resolver the resolver used to look up dependency instances
     */
    @Override
    protected void onWire(ManagerResolver resolver) {
        InputManager inputManager = resolver.resolve(InputManager.class);
        serviceMap.put(IEntityManager.class,        resolver.resolve(EntityManager.class));
        serviceMap.put(IRenderQueue.class,           resolver.resolve(RenderManager.class).getRenderQueue());
        serviceMap.put(IInputQuery.class,            inputManager);
        serviceMap.put(IInputManager.class,          inputManager);
        serviceMap.put(IInputExtensionRegistry.class, inputManager);
    }

    /**
     * assembles the SceneContext and enters the initial scene if one has been set.
     */
    @Override
    protected void onInit() {
        context = new SceneContext() {
            @Override
            public <T> T get(Class<T> type) {
                Object svc = serviceMap.get(type);
                if (svc == null) {
                    throw new IllegalArgumentException(
                        "no service registered for " + type.getSimpleName());
                }
                return type.cast(svc);
            }

            @Override
            public void changeScene(String key) {
                SceneManager.this.requestChange(key);
            }

            @Override
            public void suspendScene(String key) {
                SceneManager.this.requestSuspend(key);
            }

            @Override
            public Scene getScene(String key) {
                return SceneManager.this.getScene(key);
            }

            @Override
            public String getSuspendedSceneKey() {
                return SceneManager.this.getSuspendedScene();
            }

            @Override
            public void clearSuspendedScene() {
                SceneManager.this.clearSuspendedScene();
            }
        };

        if (currentKey != null && scenes.containsKey(currentKey)) {
            scenes.get(currentKey).onEnter(context);
        }
    }

    /**
     * exits the current scene (if one is active) and clears the
     * scene registry.
     */
    @Override
    protected void onShutdown() {
        if (currentKey != null && scenes.containsKey(currentKey)) {
            scenes.get(currentKey).onExit(context);
        }
        scenes.clear();
    }

    /**
     * adds a scene to the registry, keyed by its name.
     *
     * @param scene the scene to register
     */
    public void registerScene(Scene scene) {
        scenes.put(scene.getName(), scene);
    }

    /**
     * sets the initial scene key; must be called before the engine is initialised.
     *
     * @param key the name of the scene to start in
     * @throws IllegalArgumentException if key is null or blank
     */
    public void setInitialScene(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        if (key.trim().isEmpty()) {
            throw new IllegalArgumentException("key cannot be blank");
        }
        this.currentKey = key;
    }

    /**
     * schedules a transition to the named scene for the next update tick.
     *
     * @param key the name of the target scene
     */
    public void requestChange(String key) {
        validateSceneKey(key);
        this.pendingKey = key;
    }

    /**
     * schedules a suspend transition to the named scene for the next update tick.
     *
     * @param key the name of the target scene
     */
    public void requestSuspend(String key) {
        validateSceneKey(key);
        this.pendingSuspendKey = key;
    }

    /**
     * validates a scene key for deferred transition requests.
     *
     * @param key the target scene key
     * @throws IllegalArgumentException if key is null, blank, or not registered
     */
    private void validateSceneKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        if (key.trim().isEmpty()) {
            throw new IllegalArgumentException("key cannot be blank");
        }
        if (!scenes.containsKey(key)) {
            throw new IllegalArgumentException("scene not registered: " + key);
        }
    }

    /**
     * returns the currently active scene.
     *
     * @return the active Scene, or null if none is set
     */
    public Scene getCurrentScene() {
        if (currentKey == null) {
            return null;
        }
        return scenes.get(currentKey);
    }

    /**
     * returns the scene registered under the specified key.
     *
     * @param key the name of the scene to retrieve
     * @return the Scene, or null if not found
     */
    public Scene getScene(String key) {
        return scenes.get(key);
    }

    /**
     * returns an iterable over the names of all registered scenes.
     *
     * @return the set of scene keys
     */
    public Iterable<String> getSceneKeys() {
        return scenes.keySet();
    }

    /**
     * stores the key of the scene that was suspended.
     *
     * @param key the scene key
     */
    private void storePreviousScene(String key) {
        this.suspendedSceneKey = key;
    }

    /**
     * returns the key of the suspended scene, if any.
     *
     * @return the suspended scene key, or null
     */
    private String getSuspendedScene() {
        return suspendedSceneKey;
    }

    /**
     * clears the suspended scene tracking.
     */
    private void clearSuspendedScene() {
        this.suspendedSceneKey = null;
    }

    /**
     * resolves any pending scene transition, then drives the active
     * scene's update and render-submission hooks.
     *
     * @param deltaTime seconds elapsed since the previous frame
     */
    @Override
    protected void onUpdate(float deltaTime) {
        if (pendingSuspendKey != null) {
            if (currentKey != null && scenes.containsKey(currentKey)) {
                scenes.get(currentKey).onSuspend(context);
            }
            String previousKey = currentKey;
            currentKey = pendingSuspendKey;
            pendingSuspendKey = null;
            storePreviousScene(previousKey);
            if (scenes.containsKey(currentKey)) {
                scenes.get(currentKey).onEnter(context);
            }
        }

        if (pendingKey != null) {
            boolean isResuming = pendingKey.equals(getSuspendedScene());

            if (isResuming) {
                if (currentKey != null && scenes.containsKey(currentKey)) {
                    scenes.get(currentKey).onExit(context);
                }
                currentKey = pendingKey;
                pendingKey = null;
                clearSuspendedScene();
                if (scenes.containsKey(currentKey)) {
                    scenes.get(currentKey).onResume(context);
                }
            } else {
                if (currentKey != null && scenes.containsKey(currentKey)) {
                    scenes.get(currentKey).onExit(context);
                }
                currentKey = pendingKey;
                pendingKey = null;
                if (scenes.containsKey(currentKey)) {
                    scenes.get(currentKey).onEnter(context);
                }
            }
        }

        Scene current = getCurrentScene();
        if (current == null) {
            return;
        }

        // skipped while paused
        if (!current.isPaused()) {
            current.update(deltaTime, context);
        }

        // resolve queue once and pass directly to avoid scene accessing full context
        IRenderQueue renderQueue = (IRenderQueue) serviceMap.get(IRenderQueue.class);

        // render the suspended scene first so the overlay scene composites on top
        if (suspendedSceneKey != null && scenes.containsKey(suspendedSceneKey)) {
            scenes.get(suspendedSceneKey).submitRenderable(renderQueue);
        }
        current.submitRenderable(renderQueue);
    }
}

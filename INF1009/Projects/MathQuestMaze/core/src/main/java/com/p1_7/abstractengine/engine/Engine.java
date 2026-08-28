package com.p1_7.abstractengine.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.p1_7.abstractengine.render.RenderManager;

/**
 * central orchestrator that manages manager lifecycles, drives the update loop,
 * and delegates the per-frame render call. managers are registered in any order
 * and the engine resolves the correct initialisation sequence via a topological
 * sort of declared dependencies.
 */
public class Engine {

    /** all managers registered with the engine */
    private final List<IManager> managers = new ArrayList<>();

    /** subset of managers (or standalone updatables) that update each frame */
    private final List<IUpdatable> updatables = new ArrayList<>();

    /** type-keyed index of registered managers for dependency lookup */
    private final Map<Class<? extends IManager>, IManager> managerMap = new HashMap<>();

    /** the render manager used for the explicit render step */
    private RenderManager renderManager;

    /** guards against post-init registration */
    private boolean initialised;

    /**
     * registers a manager and indexes it by type for dependency lookup.
     *
     * @param manager the manager to register
     * @throws IllegalArgumentException if manager is null
     * @throws IllegalStateException    if called after init()
     * @throws IllegalStateException    if a type key already maps to a different manager
     */
    public void registerManager(IManager manager) {
        if (manager == null) {
            throw new IllegalArgumentException("manager cannot be null");
        }
        if (initialised) {
            throw new IllegalStateException(
                "cannot register managers after init() has been called");
        }

        managers.add(manager);
        if (manager instanceof IUpdatable) {
            updatables.add((IUpdatable) manager);
        }

        // index by concrete class and superclass chain
        indexSuperclasses(manager);

        // index by IManager-extending interfaces
        indexInterfaces(manager);
    }

    /**
     * indexes the concrete class and superclasses up to the framework bases.
     *
     * @param manager the manager to index
     */
    private void indexSuperclasses(IManager manager) {
        Class<?> cls = manager.getClass();
        while (cls != null
               && cls != Manager.class
               && cls != UpdatableManager.class
               && cls != Object.class) {
            if (IManager.class.isAssignableFrom(cls)) {
                @SuppressWarnings("unchecked")
                Class<? extends IManager> key = (Class<? extends IManager>) cls;
                putMapping(key, manager);
            }
            cls = cls.getSuperclass();
        }
    }

    /**
     * indexes IManager-extending interfaces (excluding IManager itself).
     *
     * @param manager the manager to index
     */
    private void indexInterfaces(IManager manager) {
        Class<?> cls = manager.getClass();
        while (cls != null && cls != Object.class) {
            for (Class<?> iface : cls.getInterfaces()) {
                if (iface == IManager.class || iface == IUpdatable.class) {
                    continue;
                }
                if (IManager.class.isAssignableFrom(iface)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends IManager> key = (Class<? extends IManager>) iface;
                    putMapping(key, manager);
                }
            }
            cls = cls.getSuperclass();
        }
    }

    /**
     * inserts a type mapping; throws on duplicate keys.
     *
     * @param key     the type key
     * @param manager the manager instance
     * @throws IllegalStateException if the key already maps to a different instance
     */
    private void putMapping(Class<? extends IManager> key, IManager manager) {
        IManager existing = managerMap.get(key);
        if (existing != null && existing != manager) {
            throw new IllegalStateException(
                "duplicate manager mapping for " + key.getSimpleName()
                + ": " + existing.getClass().getSimpleName()
                + " and " + manager.getClass().getSimpleName());
        }
        managerMap.put(key, manager);
    }

    /**
     * registers a standalone updatable that is not itself a manager.
     *
     * @param updatable the updatable to add to the per-frame loop
     */
    public void registerUpdatable(IUpdatable updatable) {
        updatables.add(updatable);
    }

    /**
     * returns the manager registered under the given type key.
     *
     * @param <T>  the manager type
     * @param type the class or interface key
     * @return the matching manager instance
     * @throws IllegalArgumentException if no manager is registered for the type
     */
    @SuppressWarnings("unchecked")
    public <T extends IManager> T getManager(Class<T> type) {
        IManager manager = managerMap.get(type);
        if (manager == null) {
            throw new IllegalArgumentException(
                "no manager registered for " + type.getSimpleName());
        }
        return (T) manager;
    }

    /**
     * sorts managers by dependency order, wires them, and initialises each one.
     *
     * @throws IllegalArgumentException if a dependency type is not registered
     * @throws IllegalStateException    if a circular dependency is detected
     * @throws IllegalStateException    if more than one RenderManager is found
     */
    public void init() {
        // build sorted order from dependency graph
        List<IManager> sorted = DependencySorter.sort(managers, managerMap);

        // reorder managers list to sorted result
        managers.clear();
        managers.addAll(sorted);

        // rebuild updatables preserving sorted relative order
        updatables.clear();
        for (int i = 0; i < managers.size(); i++) {
            IManager m = managers.get(i);
            if (m instanceof IUpdatable) {
                updatables.add((IUpdatable) m);
            }
        }

        // auto-detect render manager
        renderManager = null;
        for (int i = 0; i < managers.size(); i++) {
            if (managers.get(i) instanceof RenderManager) {
                if (renderManager != null) {
                    throw new IllegalStateException(
                        "multiple RenderManager instances registered");
                }
                renderManager = (RenderManager) managers.get(i);
            }
        }

        // wiring pass — call setDependencies so each manager can resolve and store its dependencies
        ManagerResolver resolver = new ManagerResolver() {
            @Override
            public <T extends IManager> T resolve(Class<T> type) {
                return getManager(type);
            }
        };
        for (int i = 0; i < managers.size(); i++) {
            managers.get(i).setDependencies(resolver);
        }

        initialised = true;

        // initialise in sorted order
        for (int i = 0; i < managers.size(); i++) {
            managers.get(i).init();
        }
    }

    /**
     * advances all registered updatables by one frame.
     *
     * @param deltaTime seconds elapsed since the previous frame
     */
    public void update(float deltaTime) {
        for (int i = 0; i < updatables.size(); i++) {
            updatables.get(i).update(deltaTime);
        }
    }

    /**
     * delegates the current frame's draw call to the render manager.
     * does nothing if no render manager has been set.
     */
    public void render() {
        if (renderManager != null) {
            renderManager.render();
        }
    }

    /**
     * shuts down every registered manager in reverse dependency order
     * so that dependants are torn down before their dependencies.
     */
    public void shutdown() {
        for (int i = managers.size() - 1; i >= 0; i--) {
            managers.get(i).shutdown();
        }
    }
}

package com.p1_7.abstractengine.engine;

/**
 * lifecycle contract for every manager; implementations allocate resources
 * in init() and release them in shutdown().
 */
public interface IManager {

    /**
     * initialises the manager and allocates any required resources.
     */
    void init();

    /**
     * shuts down the manager and releases all held resources.
     */
    void shutdown();

    /**
     * called by the engine during the wiring pass to give this manager a resolver
     * for looking up its declared dependencies. the default is a no-op suitable
     * for IManager implementations that do not use the wiring mechanism.
     * Manager subclasses must not override this method (it is declared final there);
     * they should override onWire() instead.
     *
     * @param resolver the resolver used to look up dependency instances
     */
    default void setDependencies(ManagerResolver resolver) {
        // no-op — implementations that need wiring should override this method
    }
}

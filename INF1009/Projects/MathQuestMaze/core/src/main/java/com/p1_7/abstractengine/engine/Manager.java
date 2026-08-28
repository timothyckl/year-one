package com.p1_7.abstractengine.engine;

/**
 * abstract base class providing the standard init/shutdown lifecycle template
 * for all managers.
 */
public abstract class Manager implements IManager {

    /** tracks whether this manager has been initialised */
    private boolean initialised;

    /**
     * initialises the manager.
     */
    @Override
    public final void init() {
        initialised = true;
        onInit();
    }

    /**
     * shuts down the manager.
     */
    @Override
    public final void shutdown() {
        onShutdown();
        initialised = false;
    }

    /**
     * override to perform setup work; default is a no-op.
     */
    protected void onInit() {
        // no-op — subclasses may override
    }

    /**
     * override to release resources; default is a no-op.
     */
    protected void onShutdown() {
        // no-op — subclasses may override
    }

    /**
     * returns whether this manager has been initialised.
     *
     * @return true if the manager is currently initialised
     */
    public boolean isInitialised() {
        return initialised;
    }

    /**
     * returns the direct dependencies of this manager; default is empty.
     *
     * @return an array of manager types this manager depends on
     */
    @SuppressWarnings("unchecked")
    public Class<? extends IManager>[] getDependencies() {
        return new Class[0];
    }

    /**
     * resolves and stores dependency references before init(); delegates to
     * onWire(ManagerResolver). called by the engine during the wiring pass.
     *
     * @param resolver the resolver used to look up dependency instances
     */
    public final void setDependencies(ManagerResolver resolver) {
        onWire(resolver);
    }

    /**
     * override to resolve and store references to declared dependencies; use
     * resolver.resolve(Type.class) to look up each dependency by type.
     * called by setDependencies() before init(). default is a no-op.
     *
     * @param resolver the resolver used to look up dependency instances
     * @see #getDependencies()
     */
    protected void onWire(ManagerResolver resolver) {
        // no-op — subclasses may override
    }
}

package com.p1_7.abstractengine.engine;

/**
 * lookup contract for resolving manager dependencies by type.
 */
@FunctionalInterface
public interface ManagerResolver {

    /**
     * resolves a manager instance by its registered type.
     *
     * @param <T>  the manager type to resolve
     * @param type the class or interface key to look up
     * @return the matching manager instance
     * @throws IllegalArgumentException if no manager is registered for the given type
     */
    <T extends IManager> T resolve(Class<T> type);
}

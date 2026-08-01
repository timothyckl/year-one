package com.p1_7.abstractengine.entity;

/**
 * functional interface that decouples the EntityManager from any concrete
 * Entity subclass.
 */
@FunctionalInterface
public interface EntityFactory {

    /**
     * constructs and returns a new entity instance.
     *
     * @return a freshly created Entity; must not be null
     */
    Entity create();
}

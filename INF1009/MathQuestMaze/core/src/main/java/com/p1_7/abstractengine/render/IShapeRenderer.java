package com.p1_7.abstractengine.render;

/**
 * abstraction over a shape renderer for drawing procedural geometry.
 * concrete shape-drawing methods are the responsibility of framework-specific
 * implementations; consumers cast to the concrete type as needed.
 */
public interface IShapeRenderer {

    /**
     * begins drawing filled shapes.
     */
    void begin();

    /**
     * ends the current shape drawing batch.
     */
    void end();

    /**
     * disposes the renderer and releases its resources.
     */
    void dispose();
}

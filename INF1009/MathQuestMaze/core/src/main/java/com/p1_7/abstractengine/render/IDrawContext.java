package com.p1_7.abstractengine.render;

/**
 * lifecycle contract for the per-frame draw context.
 * all drawing methods live on the concrete implementation;
 * this interface is used by RenderManager to close the frame.
 */
public interface IDrawContext {

    /**
     * ends the currently active drawing pass and resets context state.
     * called by the render manager once after all items have been rendered.
     */
    void flush();

    /**
     * releases any draw-context-owned resources.
     * called by the render manager during shutdown.
     */
    void dispose();
}

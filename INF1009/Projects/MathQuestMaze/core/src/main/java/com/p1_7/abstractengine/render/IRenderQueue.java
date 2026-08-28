package com.p1_7.abstractengine.render;

/**
 * single-frame accumulator for render items submitted by scenes each tick.
 */
public interface IRenderQueue {

    /**
     * adds an item to the queue for drawing this frame.
     *
     * @param item the renderable to enqueue
     */
    void queue(IRenderable item);

    /**
     * removes all items from the queue. called by the render manager
     * after the draw pass completes.
     */
    void clear();

    /**
     * returns an iterable over every item currently in the queue.
     *
     * @return an iterable of queued renderables
     */
    Iterable<IRenderable> items();
}

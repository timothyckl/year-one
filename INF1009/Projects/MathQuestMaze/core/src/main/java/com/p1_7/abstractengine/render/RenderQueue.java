package com.p1_7.abstractengine.render;

import java.util.ArrayList;
import java.util.List;

/**
 * list-backed implementation of IRenderQueue.
 */
class RenderQueue implements IRenderQueue {

    /** the backing store for queued renderables */
    private final List<IRenderable> items = new ArrayList<>();

    /**
     * adds a renderable to the queue for drawing this frame.
     *
     * @param item the renderable to enqueue
     */
    @Override
    public void queue(IRenderable item) {
        items.add(item);
    }

    /**
     * removes all items from the queue.
     */
    @Override
    public void clear() {
        items.clear();
    }

    /**
     * returns the queued renderables.
     *
     * @return the list of queued renderables
     */
    @Override
    public List<IRenderable> items() {
        return items;
    }
}

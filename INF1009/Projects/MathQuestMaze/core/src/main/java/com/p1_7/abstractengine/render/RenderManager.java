package com.p1_7.abstractengine.render;

import com.p1_7.abstractengine.engine.Manager;

/**
 * owns the drawing resources and drives the per-frame render pass for all
 * queued items. subclasses provide the platform-specific sprite batch,
 * shape renderer, asset store, and draw context via factory methods.
 */
public abstract class RenderManager extends Manager {

    /** sprite batch used for textured items */
    protected ISpriteBatch batch;

    /** shape renderer used for procedural items */
    protected IShapeRenderer shapeRenderer;

    /** asset store for texture loading and caching */
    protected IAssetStore assetStore;

    /** per-frame draw context; owns all pass transitions */
    protected IDrawContext drawCtx;

    /** single-frame queue of items to draw */
    private final RenderQueue queue = new RenderQueue();

    /**
     * creates a platform-specific sprite batch.
     *
     * @return a new ISpriteBatch instance
     */
    protected abstract ISpriteBatch createSpriteBatch();

    /**
     * creates a platform-specific shape renderer.
     *
     * @return a new IShapeRenderer instance
     */
    protected abstract IShapeRenderer createShapeRenderer();

    /**
     * creates a platform-specific asset store.
     *
     * @return a new IAssetStore instance
     */
    protected abstract IAssetStore createAssetStore();

    /**
     * creates the draw context backed by the drawing resources.
     *
     * @param batch         the sprite batch
     * @param shapeRenderer the shape renderer
     * @param assetStore    the asset store
     * @return a new IDrawContext instance
     */
    protected abstract IDrawContext createDrawContext(ISpriteBatch batch,
                                                      IShapeRenderer shapeRenderer,
                                                      IAssetStore assetStore);

    /**
     * creates the drawing resources via the platform factory methods.
     */
    @Override
    protected void onInit() {
        batch         = createSpriteBatch();
        shapeRenderer = createShapeRenderer();
        assetStore    = createAssetStore();
        drawCtx       = createDrawContext(batch, shapeRenderer, assetStore);
    }

    /**
     * disposes the drawing resources.
     */
    @Override
    protected void onShutdown() {
        if (drawCtx       != null) { drawCtx.dispose(); }
        if (assetStore    != null) { assetStore.dispose(); }
        if (batch         != null) { batch.dispose(); }
        if (shapeRenderer != null) { shapeRenderer.dispose(); }
    }

    /**
     * returns the render queue that scenes use to submit items for
     * drawing.
     *
     * @return the IRenderQueue instance
     */
    public IRenderQueue getRenderQueue() {
        return queue;
    }

    /**
     * executes the full render pass for the current frame, then clears the queue.
     * each item drives its own pass transitions via the draw context; a single
     * flush() call closes the final pass after all items have been drawn.
     */
    public void render() {
        assetStore.finishLoading();

        for (IRenderable item : queue.items()) {
            item.render(drawCtx);
        }

        drawCtx.flush();
        queue.clear();
    }
}

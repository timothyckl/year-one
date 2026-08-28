package com.p1_7.game.collectible;

import com.p1_7.abstractengine.collision.IBounds;
import com.p1_7.abstractengine.collision.ICollidable;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.render.IRenderable;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.spatial.Bounds2D;
import com.p1_7.game.spatial.Transform2D;
import com.p1_7.game.character.Player;
import com.p1_7.game.round.ILevelOrchestrator;

/**
 * generic collectable item with shared spatial state and pickup flow.
 *
 * subclasses define how they render and what effect they apply on collection.
 */
public abstract class Item extends Entity implements IRenderable, ICollidable {

    /** item transform centred on the provided spawn point */
    protected final Transform2D transform;

    /** reusable axis-aligned pickup bounds */
    protected final Bounds2D bounds;

    /** collision/pickup box side length */
    protected final float size;

    /** reusable min-position scratch to avoid per-frame allocation */
    private final float[] boundsPos = new float[2];

    /** fixed extent reused for every getBounds() call */
    private final float[] boundsExtent;

    /** gameplay state owner used to apply pickup effects */
    protected final ILevelOrchestrator orchestrator;

    /** scene-level listener notified when this item is collected; may be null */
    private ItemCollectionListener listener;

    /**
     * constructs an item centred on the given world position.
     *
     * @param centreX x coordinate of the item centre
     * @param centreY y coordinate of the item centre
     * @param size    pickup box side length
     * @param orchestrator gameplay state owner
     */
    protected Item(float centreX, float centreY, float size, ILevelOrchestrator orchestrator) {
        this.size = size;
        this.orchestrator = orchestrator;
        this.transform = new Transform2D(centreX - size / 2f, centreY - size / 2f, size, size);
        this.bounds = new Bounds2D(centreX - size / 2f, centreY - size / 2f, size, size);
        this.boundsExtent = new float[]{ size, size };
    }

    /**
     * registers the listener that will be notified when this item is collected.
     *
     * @param listener the collection callback, or null to remove it
     */
    public void bindListener(ItemCollectionListener listener) {
        this.listener = listener;
    }

    @Override
    public ITransform getTransform() {
        return transform;
    }

    @Override
    public IBounds getBounds() {
        boundsPos[0] = transform.getPosition(0);
        boundsPos[1] = transform.getPosition(1);
        bounds.set(boundsPos, boundsExtent);
        return bounds;
    }

    @Override
    public void onCollision(ICollidable other) {
        if (!isActive() || !(other instanceof Player)) {
            return;
        }
        if (onCollect(orchestrator)) {
            setActive(false);
            if (listener != null) {
                listener.onItemCollected(this);
            }
        }
    }

    /**
     * applies the pickup effect.
     *
     * @param orchestrator gameplay state owner
     * @return true if the item was actually consumed
     */
    protected abstract boolean onCollect(ILevelOrchestrator orchestrator);

    /**
     * returns the sound key to play when this item is collected, or null for no sound.
     *
     * @return sound key string, or null
     */
    public String getCollectSoundKey() {
        return null;
    }
}

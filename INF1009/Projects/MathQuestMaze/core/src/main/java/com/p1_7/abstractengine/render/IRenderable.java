package com.p1_7.abstractengine.render;

import com.p1_7.abstractengine.transform.ITransformable;

/**
 * unified contract for any object the render system can draw.
 * extends ITransformable to provide spatial state, and adds
 * a single render entry point that delegates all pass management
 * to the draw context.
 */
public interface IRenderable extends ITransformable {

    /**
     * returns the asset path for a textured sprite, or null if the
     * entity draws procedurally or manages its own textures.
     *
     * @return the asset path string, or null
     */
    String getAssetPath();

    /**
     * returns true if this entity primarily draws a managed texture.
     * derived from getAssetPath() by default; override only if the
     * inference is incorrect for a given implementation.
     *
     * @return true if getAssetPath() is non-null, false otherwise
     */
    default boolean isTextured() {
        return getAssetPath() != null;
    }

    /**
     * draws this entity using the supplied draw context.
     * all pass transitions are handled internally by the context.
     *
     * @param ctx the draw context for this frame
     */
    void render(IDrawContext ctx);
}

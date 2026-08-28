package com.p1_7.abstractengine.render;

/**
 * abstraction over a sprite batch for drawing textured quads.
 */
public interface ISpriteBatch {

    /**
     * begins a new drawing batch.
     */
    void begin();

    /**
     * ends the current drawing batch and flushes buffered draws.
     */
    void end();

    /**
     * draws a texture at the specified position and size.
     *
     * @param textureHandle an opaque handle to the loaded texture
     * @param x             the x position
     * @param y             the y position
     * @param width         the draw width
     * @param height        the draw height
     */
    void draw(Object textureHandle, float x, float y, float width, float height);

    /**
     * disposes the batch and releases its resources.
     */
    void dispose();
}

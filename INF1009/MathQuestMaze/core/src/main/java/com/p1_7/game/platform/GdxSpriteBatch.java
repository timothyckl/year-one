package com.p1_7.game.platform;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.p1_7.abstractengine.render.ISpriteBatch;

/**
 * libgdx implementation of ISpriteBatch that wraps a SpriteBatch.
 *
 * exposes typed drawing methods so GdxDrawContext can delegate without
 * needing access to the raw libgdx SpriteBatch.
 */
public class GdxSpriteBatch implements ISpriteBatch {

    /** the underlying libgdx sprite batch */
    private final SpriteBatch batch = new SpriteBatch();

    @Override
    public void begin() {
        batch.begin();
    }

    @Override
    public void end() {
        batch.end();
    }

    /**
     * draws a texture at the specified position and size.
     *
     * @param textureHandle the loaded texture (must be a libgdx Texture)
     * @param x             the x position
     * @param y             the y position
     * @param width         the draw width
     * @param height        the draw height
     */
    @Override
    public void draw(Object textureHandle, float x, float y, float width, float height) {
        batch.draw((Texture) textureHandle, x, y, width, height);
    }

    /**
     * draws a texture at the specified position and size.
     *
     * @param texture the libgdx texture
     * @param x       the x position
     * @param y       the y position
     * @param width   the draw width
     * @param height  the draw height
     */
    public void draw(Texture texture, float x, float y, float width, float height) {
        batch.draw(texture, x, y, width, height);
    }

    /**
     * draws a sub-region of a texture with optional horizontal flip.
     *
     * @param texture the libgdx texture
     * @param x       destination x
     * @param y       destination y
     * @param width   destination width
     * @param height  destination height
     * @param srcX    source region x
     * @param srcY    source region y
     * @param srcW    source region width
     * @param srcH    source region height
     * @param flipX   true to mirror horizontally
     * @param flipY   true to mirror vertically
     */
    public void draw(Texture texture, float x, float y, float width, float height,
                     int srcX, int srcY, int srcW, int srcH,
                     boolean flipX, boolean flipY) {
        batch.draw(texture, x, y, width, height, srcX, srcY, srcW, srcH, flipX, flipY);
    }

    /**
     * sets the batch tint colour.
     *
     * @param color the colour to apply to subsequent draws
     */
    public void setColor(Color color) {
        batch.setColor(color);
    }

    /**
     * draws text using a BitmapFont at the specified position.
     *
     * @param font the bitmap font
     * @param text the string to draw
     * @param x    left edge in world coordinates
     * @param y    baseline y in world coordinates
     */
    public void drawFont(BitmapFont font, String text, float x, float y) {
        font.draw(batch, text, x, y);
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}

package com.p1_7.game.platform;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.p1_7.abstractengine.render.IAssetStore;
import com.p1_7.abstractengine.render.IDrawContext;

/**
 * libgdx draw context that manages SpriteBatch / ShapeRenderer pass
 * transitions. entities cast to GdxDrawContext inside their render()
 * implementation to call the drawing methods below.
 *
 * pass transitions are lazy — openBatch() and openShape() are no-ops
 * if the correct pass is already active, preserving batching across
 * consecutive textured items.
 */
public class GdxDrawContext implements IDrawContext {

    /** the active drawing pass for this frame */
    private enum Pass { NONE, BATCH, SHAPE }

    private final GdxSpriteBatch    batch;
    private final GdxShapeRenderer  shapeRenderer;
    private final IAssetStore       assetStore;
    private final Texture           solidPixel;

    private Pass                     currentPass      = Pass.NONE;
    private ShapeRenderer.ShapeType  activeShapeType  = null;

    /**
     * constructs a draw context backed by the provided libgdx wrapper resources.
     *
     * @param batch         the sprite batch wrapper
     * @param shapeRenderer the shape renderer wrapper
     * @param assetStore    the asset store for texture lookup
     */
    public GdxDrawContext(GdxSpriteBatch batch, GdxShapeRenderer shapeRenderer,
                          IAssetStore assetStore) {
        this.batch         = batch;
        this.shapeRenderer = shapeRenderer;
        this.assetStore    = assetStore;
        this.solidPixel    = createSolidPixel();
    }

    /**
     * ends the currently active drawing pass and resets to idle.
     * called by RenderManager after all items have been rendered.
     */
    @Override
    public void flush() {
        if (currentPass == Pass.BATCH) {
            batch.end();
        } else if (currentPass == Pass.SHAPE) {
            shapeRenderer.end();
        }
        currentPass     = Pass.NONE;
        activeShapeType = null;
    }

    /**
     * releases resources owned directly by the draw context.
     */
    @Override
    public void dispose() {
        flush();
        solidPixel.dispose();
    }

    /**
     * draws a managed texture from the asset store at the specified bounds.
     *
     * @param assetPath the path to the texture asset
     * @param x         left edge in world coordinates
     * @param y         bottom edge in world coordinates
     * @param w         draw width
     * @param h         draw height
     */
    public void drawTexture(String assetPath, float x, float y, float w, float h) {
        openBatch();
        Texture texture = (Texture) assetStore.loadTexture(assetPath);
        batch.draw(texture, x, y, w, h);
    }

    /**
     * draws a sub-region of a managed texture, e.g. one frame of a sprite strip.
     *
     * @param assetPath the path to the texture asset
     * @param srcX      left edge of the source region in texture pixels
     * @param srcY      top edge of the source region in texture pixels (libgdx y-down)
     * @param srcW      width of the source region in texture pixels
     * @param srcH      height of the source region in texture pixels
     * @param x         left edge of the destination in world coordinates
     * @param y         bottom edge of the destination in world coordinates
     * @param w         destination draw width
     * @param h         destination draw height
     * @param flipX     true to mirror the region horizontally
     */
    public void drawTextureRegion(String assetPath,
                                  int srcX, int srcY, int srcW, int srcH,
                                  float x, float y, float w, float h,
                                  boolean flipX) {
        openBatch();
        Texture texture = (Texture) assetStore.loadTexture(assetPath);
        batch.draw(texture, x, y, w, h, srcX, srcY, srcW, srcH, flipX, false);
    }

    /**
     * draws a raw libgdx Texture (owned directly by the caller) with a colour tint.
     * resets the batch colour to white after drawing.
     *
     * @param texture the texture to draw
     * @param tint    the colour tint to apply
     * @param x       left edge in world coordinates
     * @param y       bottom edge in world coordinates
     * @param w       draw width
     * @param h       draw height
     */
    public void drawRawTexture(Texture texture, Color tint,
                               float x, float y, float w, float h) {
        openBatch();
        batch.setColor(tint);
        batch.draw(texture, x, y, w, h);
        batch.setColor(Color.WHITE);
    }

    /**
     * draws a tinted quad via SpriteBatch using an internal 1x1 white texture.
     * this is the preferred path for translucent fullscreen overlays.
     *
     * @param color the tint and alpha to apply
     * @param x     left edge in world coordinates
     * @param y     bottom edge in world coordinates
     * @param w     width
     * @param h     height
     */
    public void drawTintedQuad(Color color, float x, float y, float w, float h) {
        drawRawTexture(solidPixel, color, x, y, w, h);
    }

    /**
     * draws a string using the given BitmapFont at the specified position.
     *
     * @param font the bitmap font to draw with
     * @param text the string to draw
     * @param x    left edge in world coordinates
     * @param y    baseline y in world coordinates
     */
    public void drawFont(BitmapFont font, String text, float x, float y) {
        openBatch();
        batch.drawFont(font, text, x, y);
    }

    /**
     * draws a rectangle with the given colour.
     *
     * @param color  the colour
     * @param x      left edge in world coordinates
     * @param y      bottom edge in world coordinates
     * @param w      width
     * @param h      height
     * @param filled true for a solid fill, false for an outline
     */
    public void rect(Color color, float x, float y, float w, float h, boolean filled) {
        openShape(filled ? ShapeRenderer.ShapeType.Filled : ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x, y, w, h);
    }

    /**
     * draws a circle with the given colour.
     *
     * @param color  the colour
     * @param x      centre x in world coordinates
     * @param y      centre y in world coordinates
     * @param radius circle radius
     * @param filled true for a solid fill, false for an outline
     */
    public void circle(Color color, float x, float y, float radius, boolean filled) {
        openShape(filled ? ShapeRenderer.ShapeType.Filled : ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(color);
        shapeRenderer.circle(x, y, radius);
    }

    // private pass management ──────────────────────────────────────

    /**
     * ensures the sprite batch pass is open, closing the shape pass first if needed.
     */
    private void openBatch() {
        if (currentPass == Pass.BATCH) {
            return;
        }
        if (currentPass == Pass.SHAPE) {
            shapeRenderer.end();
        }
        batch.begin();
        currentPass = Pass.BATCH;
    }

    /**
     * ensures the shape renderer pass is open for the given shape type.
     * re-opens the renderer if the type changes mid-frame.
     *
     * @param type the desired ShapeType (Filled or Line)
     */
    private void openShape(ShapeRenderer.ShapeType type) {
        if (currentPass == Pass.SHAPE && activeShapeType == type) return;
        if (currentPass == Pass.SHAPE) shapeRenderer.end();
        else if (currentPass == Pass.BATCH) batch.end();
        shapeRenderer.begin(type);
        currentPass     = Pass.SHAPE;
        activeShapeType = type;
    }

    private Texture createSolidPixel() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
}

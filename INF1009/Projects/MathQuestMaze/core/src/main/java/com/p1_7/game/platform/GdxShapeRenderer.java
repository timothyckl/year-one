package com.p1_7.game.platform;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.p1_7.abstractengine.render.IShapeRenderer;

/**
 * libgdx implementation of IShapeRenderer that wraps a ShapeRenderer.
 *
 * exposes typed shape-drawing methods so GdxDrawContext can delegate
 * without needing access to the raw libgdx ShapeRenderer.
 */
public class GdxShapeRenderer implements IShapeRenderer {

    /** the underlying libgdx shape renderer */
    private final ShapeRenderer renderer = new ShapeRenderer();

    /**
     * begins drawing filled shapes (default shape type).
     */
    @Override
    public void begin() {
        renderer.begin(ShapeType.Filled);
    }

    /**
     * begins drawing shapes of the specified type.
     *
     * @param type the shape type (Filled or Line)
     */
    public void begin(ShapeType type) {
        renderer.begin(type);
    }

    @Override
    public void end() {
        renderer.end();
    }

    /**
     * sets the colour for subsequent shape draws.
     *
     * @param color the colour to apply
     */
    public void setColor(Color color) {
        renderer.setColor(color);
    }

    /**
     * draws a rectangle.
     *
     * @param x left edge
     * @param y bottom edge
     * @param w width
     * @param h height
     */
    public void rect(float x, float y, float w, float h) {
        renderer.rect(x, y, w, h);
    }

    /**
     * draws a circle.
     *
     * @param x      centre x
     * @param y      centre y
     * @param radius the radius
     */
    public void circle(float x, float y, float radius) {
        renderer.circle(x, y, radius);
    }

    @Override
    public void dispose() {
        renderer.dispose();
    }
}

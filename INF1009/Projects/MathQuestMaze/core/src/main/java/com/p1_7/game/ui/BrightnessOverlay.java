package com.p1_7.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.render.IRenderable;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.Settings;
import com.p1_7.game.spatial.Transform2D;
import com.p1_7.game.platform.GdxDrawContext;

/**
 * Full-screen brightness overlay.
 *
 * Draws a translucent black quad whose alpha is the inverse of the current
 * brightness setting, allowing scenes to dim the entire frame when queued last.
 */
public class BrightnessOverlay extends Entity implements IRenderable {

    private final Transform2D transform;
    private final Color       overlayColour = new Color(0f, 0f, 0f, 1f);

    /** Creates a full-screen overlay sized from the current settings. */
    public BrightnessOverlay() {
        this.transform = new Transform2D(
            0f,
            0f,
            Settings.getWindowWidth(),
            Settings.getWindowHeight()
        );
    }

    @Override public String     getAssetPath() { return null; }
    @Override public ITransform getTransform() { return transform; }

    /**
     * Draws the overlay with alpha driven by the current brightness level.
     *
     * @param ctx the draw context for this frame
     */
    @Override
    public void render(IDrawContext ctx) {
        GdxDrawContext gdxCtx = (GdxDrawContext) ctx;
        overlayColour.a = 1.0f - Settings.getBrightnessLevel();
        gdxCtx.drawTintedQuad(overlayColour,
                              transform.getPosition(0), transform.getPosition(1),
                              transform.getSize(0),     transform.getSize(1));
    }

}

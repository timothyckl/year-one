package com.p1_7.game.hud;

import com.badlogic.gdx.graphics.Color;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.render.IRenderable;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.Settings;
import com.p1_7.game.spatial.Transform2D;
import com.p1_7.game.platform.GdxDrawContext;

/**
 * solid top bar reserved for HUD-only content.
 */
public final class HudStrip extends Entity implements IRenderable {

    /** height of the strip in pixels */
    private static final float HEIGHT = 48f;

    /** y coordinate where the strip begins — the upper boundary of the playfield */
    public static final float STRIP_Y = Settings.getWindowHeight() - HEIGHT;

    /** total height available for gameplay below the strip — equal to STRIP_Y by definition */
    public static final float PLAYFIELD_HEIGHT = STRIP_Y;

    // TODO palette: replace hardcoded colour with a shared palette entry
    private static final Color STRIP_COLOUR = new Color(0.11f, 0.14f, 0.20f, 1f);

    private final Transform2D transform = new Transform2D(
        0f,
        STRIP_Y,
        Settings.getWindowWidth(),
        HEIGHT
    );

    @Override
    public String getAssetPath() {
        return null;
    }

    @Override
    public ITransform getTransform() {
        return transform;
    }

    @Override
    public void render(IDrawContext ctx) {
        ((GdxDrawContext) ctx).drawTintedQuad(
            STRIP_COLOUR,
            transform.getPosition(0),
            transform.getPosition(1),
            transform.getSize(0),
            transform.getSize(1)
        );
    }
}

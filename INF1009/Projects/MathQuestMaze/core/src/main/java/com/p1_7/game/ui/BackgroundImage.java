package com.p1_7.game.ui;

import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.render.IRenderable;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.Settings;
import com.p1_7.game.spatial.Transform2D;
import com.p1_7.game.platform.GdxDrawContext;

/**
 * Shared full-screen background renderable that delegates texture ownership
 * to the engine asset pipeline.
 */
public final class BackgroundImage implements IRenderable {

    private final Transform2D transform;
    private final String assetPath;

    public BackgroundImage(String assetPath) {
        this.assetPath = assetPath;
        this.transform = new Transform2D(
            0f,
            0f,
            Settings.getWindowWidth(),
            Settings.getWindowHeight()
        );
    }

    @Override
    public String getAssetPath() {
        return assetPath;
    }

    @Override
    public ITransform getTransform() {
        return transform;
    }

    @Override
    public void render(IDrawContext ctx) {
        GdxDrawContext gdxCtx = (GdxDrawContext) ctx;
        gdxCtx.drawTexture(assetPath,
            transform.getPosition(0), transform.getPosition(1),
            transform.getSize(0), transform.getSize(1));
    }
}

package com.p1_7.game.collectible;

import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.game.round.ILevelOrchestrator;
import com.p1_7.game.platform.GdxDrawContext;

/**
 * healing pickup that restores one player health when consumed.
 */
public final class Heart extends Item {

    private static final String HEART_ASSET = "health-points.png";

    /** visible and pickup size of the heart item */
    public static final float SIZE = 28f;

    /**
     * constructs a heart pickup centred on the given world position.
     *
     * @param centreX x coordinate of the heart centre
     * @param centreY y coordinate of the heart centre
     * @param orchestrator gameplay state owner
     */
    public Heart(float centreX, float centreY, ILevelOrchestrator orchestrator) {
        super(centreX, centreY, SIZE, orchestrator);
    }

    @Override
    public String getCollectSoundKey() {
        return "heart";
    }

    @Override
    public String getAssetPath() {
        return HEART_ASSET;
    }

    @Override
    public void render(IDrawContext ctx) {
        GdxDrawContext gdx = (GdxDrawContext) ctx;
        gdx.drawTexture(
            HEART_ASSET,
            transform.getPosition(0),
            transform.getPosition(1),
            SIZE,
            SIZE
        );
    }

    @Override
    protected boolean onCollect(ILevelOrchestrator orchestrator) {
        return orchestrator.healPlayer(1);
    }
}

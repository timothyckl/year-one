package com.p1_7.game.character;

import com.p1_7.abstractengine.engine.IManager;
import com.p1_7.abstractengine.movement.MovementManager;
import com.p1_7.abstractengine.scene.SceneManager;
import com.p1_7.game.Settings;
import com.p1_7.game.hud.HudStrip;

/**
 * concrete MovementManager that handles player position integration and
 * viewport clamping for the game's fixed 1280 × 672 playfield below the HUD strip.
 *
 * declaring SceneManager as a dependency ensures this manager's onUpdate()
 * runs after SceneManager.onUpdate() — which calls GameScene.update() and
 * sets the player's velocity — so position integration always uses the
 * velocity resolved for the current frame.
 */
public final class GameMovementManager extends MovementManager {

    /**
     * configures viewport boundary clamping for the game's screen dimensions.
     */
    @Override
    protected void onInit() {
        setWorldBounds(
            new float[]{ 0f, 0f },
            new float[]{ Settings.getWindowWidth(), HudStrip.PLAYFIELD_HEIGHT }
        );
    }

    /**
     * declares SceneManager as a dependency so this manager's onUpdate() runs
     * after SceneManager.onUpdate() — which calls GameScene.update() and sets
     * the player's velocity — each frame.
     *
     * @return array containing SceneManager.class
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends IManager>[] getDependencies() {
        return new Class[]{ SceneManager.class };
    }
}

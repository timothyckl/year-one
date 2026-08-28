package com.p1_7.game.round;

import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.game.character.Player;

/**
 * documents and groups the three-step movement sequence that must execute
 * in a fixed order every frame:
 *
 * step 1 — input resolution: Player.update() reads directional input and
 *           commits a velocity vector. executed here.
 * step 2 — position integration: GameMovementManager.onUpdate() calls
 *           Character.move(). executed by the engine's manager lifecycle.
 * step 3 — collision resolution: MazeCollisionManager.onUpdate() detects
 *           wall penetrations and corrects the position. executed by the
 *           engine's manager lifecycle.
 *
 * steps 2 and 3 are enforced by getDependencies() declarations on their
 * respective managers. this class makes the ordering visible at the call
 * site in GameScene.update() so the full pipeline is documented in one place.
 */
public class MovementPipeline {

    /**
     * executes step 1 of the movement pipeline: resolves player input into a velocity.
     *
     * steps 2 (position integration) and 3 (collision resolution) are handled
     * automatically by the engine's dependency-ordered manager lifecycle and
     * do not need to be called here.
     *
     * @param deltaTime  seconds since the last frame
     * @param player     the player entity
     * @param inputQuery the current input state
     * @param phase      the current round phase (movement is locked outside CHOOSING)
     */
    public void step(float deltaTime, Player player, IInputQuery inputQuery,
                     RoundPhase phase) {
        player.update(deltaTime, inputQuery, phase);
    }
}

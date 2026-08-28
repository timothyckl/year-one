package com.p1_7.game.character;

import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.platform.GdxDrawContext;

/**
 * a goblin enemy with three behaviour states driven by proximity to the player.
 *
 * IDLE   — player outside VIEW_RANGE; patrols horizontally using goblin-walk.png.
 * CHASE  — player within VIEW_RANGE; runs directly toward player using goblin-run.png.
 * ATTACK — player within ATTACK_RANGE; stops and plays goblin-attack.png.
 *
 * wall collision is handled reactively by MazeCollisionManager, matching the
 * player's collision model. the sprite is flipped horizontally to match movement direction.
 */
public class Goblin extends HostileCharacter {

    // AI constants ─────────────────────────────────────────────

    /** collision box side length in pixels */
    private static final float SIZE          = 20f;

    /** enlarged contact box used when checking whether the player should take damage */
    private static final float DAMAGE_BOX_SIZE = 140f;

    /** movement speed while patrolling, in pixels per second */
    private static final float PATROL_SPEED  = 40f;

    /** movement speed while chasing, in pixels per second */
    private static final float CHASE_SPEED   = 80f;

    /** distance at which the enemy detects the player and begins chasing */
    private static final float VIEW_RANGE    = 300f;

    /** distance at which the enemy stops chasing and begins attacking */
    private static final float ATTACK_RANGE  = 50f;

    /**
     * distance at which an active attack is released back into chase.
     * this hysteresis prevents rapid ATTACK/CHASE thrashing near the threshold,
     * which otherwise restarts the attack strip and looks like flicker.
     */
    private static final float ATTACK_RELEASE_RANGE = 70f;

    /** seconds the enemy walks in one direction before reversing patrol */
    private static final float PATROL_DURATION = 1.5f;

    // sprite constants ────────────────────────────────────────

    private static final String WALK_ASSET   = "goblin-walk.png";
    private static final String RUN_ASSET    = "goblin-run.png";
    private static final String ATTACK_ASSET = "goblin-attack.png";

    /** frames in the walk and run strips */
    private static final int   WALK_RUN_FRAMES      = 8;
    /** frames in the attack strip */
    private static final int   ATTACK_FRAMES         = 9;

    /** source frame width for walk and run strips, in pixels */
    private static final int   WALK_RUN_FRAME_W      = 96;
    /** source frame width for the attack strip, in pixels */
    private static final int   ATTACK_FRAME_W        = 96;
    /** source frame height shared by all strips, in pixels */
    private static final int   FRAME_H               = 64;

    /** rendered size for walk and run frames */
    private static final float DISPLAY_W             = 168f;
    private static final float DISPLAY_H             = 128f;

    /** rendered size for the attack frame */
    private static final float ATTACK_DISPLAY_W      = 208f;
    private static final float ATTACK_DISPLAY_H      = 128f;

    /** seconds each animation frame is held before advancing */
    private static final float FRAME_DURATION        = 0.1f;

    // state ───────────────────────────────────────────────────

    /** three-way AI state */
    private enum AnimState { IDLE, CHASE, ATTACK }

    private AnimState animState  = AnimState.IDLE;

    /** true when the sprite should be mirrored horizontally */
    private boolean   flipSprite = false;

    /** accumulated patrol time; resets when the enemy reverses direction */
    private float     patrolTimer = 0f;

    /** +1 = patrol right, -1 = patrol left */
    private float     patrolDir   = 1f;

    /**
     * constructs a goblin centred on the given spawn coordinates.
     *
     * @param spawnX world x coordinate of the spawn centre
     * @param spawnY world y coordinate of the spawn centre
     */
    public Goblin(float spawnX, float spawnY) {
        super(spawnX, spawnY, SIZE, DAMAGE_BOX_SIZE);
    }

    // IRenderable ─────────────────────────────────────────────

    /**
     * returns the walk sprite path as the primary asset for pre-loading.
     *
     * @return path to the goblin walk strip
     */
    @Override
    public String getAssetPath() {
        return WALK_ASSET;
    }

    /**
     * draws the current animation frame centred on the collision box.
     * switches strip and display size based on the active AI state.
     *
     * @param ctx the draw context for this frame
     */
    @Override
    public void render(IDrawContext ctx) {
        GdxDrawContext gdx = (GdxDrawContext) ctx;

        if (animState == AnimState.ATTACK) {
            // attack frame is wider — re-centre on the collision box
            float drawX = transform.getPosition(0) + (SIZE - ATTACK_DISPLAY_W) / 2f;
            float drawY = transform.getPosition(1) + (SIZE - ATTACK_DISPLAY_H) / 2f;
            int   srcX  = currentFrame * ATTACK_FRAME_W;
            gdx.drawTextureRegion(ATTACK_ASSET, srcX, 0, ATTACK_FRAME_W, FRAME_H,
                                  drawX, drawY, ATTACK_DISPLAY_W, ATTACK_DISPLAY_H, flipSprite);
        } else {
            String asset = (animState == AnimState.CHASE) ? RUN_ASSET : WALK_ASSET;
            float  drawX = transform.getPosition(0) + (SIZE - DISPLAY_W) / 2f;
            float  drawY = transform.getPosition(1) + (SIZE - DISPLAY_H) / 2f;
            int    srcX  = currentFrame * WALK_RUN_FRAME_W;
            gdx.drawTextureRegion(asset, srcX, 0, WALK_RUN_FRAME_W, FRAME_H,
                                  drawX, drawY, DISPLAY_W, DISPLAY_H, flipSprite);
        }
    }

    // per-frame update ────────────────────────────────────────

    /**
     * evaluates proximity to the player and updates velocity, animation state,
     * and sprite direction accordingly.
     *
     * @param deltaTime       seconds elapsed since the previous frame
     * @param playerTransform the player's current transform for position sampling
     * @param hasLineOfSight  true when no wall blocks vision to the player
     */
    public void update(float deltaTime, ITransform playerTransform, boolean hasLineOfSight) {
        // compute vector and distance from this enemy's centre to the player's centre
        float ex   = transform.getPosition(0) + SIZE / 2f;
        float ey   = transform.getPosition(1) + SIZE / 2f;
        float px   = playerTransform.getPosition(0) + SIZE / 2f;
        float py   = playerTransform.getPosition(1) + SIZE / 2f;
        float dx   = px - ex;
        float dy   = py - ey;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        boolean inAttackRange = hasLineOfSight && (
            dist <= ATTACK_RANGE
            || (animState == AnimState.ATTACK && dist <= ATTACK_RELEASE_RANGE)
        );

        if (inAttackRange) {
            // stop and attack
            setVelocity(new float[]{ 0f, 0f });
            flipSprite = dx < 0f;
            if (animState != AnimState.ATTACK) {
                animState = AnimState.ATTACK;
                resetAnimation();
            }
            advanceAnimation(deltaTime, ATTACK_FRAMES, FRAME_DURATION);

        } else if (hasLineOfSight && dist <= VIEW_RANGE) {
            // chase: move directly toward the player
            float nx = dx / dist;
            float ny = dy / dist;
            setVelocity(new float[]{ nx * CHASE_SPEED, ny * CHASE_SPEED });
            flipSprite = dx < 0f;
            if (animState != AnimState.CHASE) {
                animState = AnimState.CHASE;
                resetAnimation();
            }
            advanceAnimation(deltaTime, WALK_RUN_FRAMES, FRAME_DURATION);

        } else {
            // idle: patrol horizontally
            patrolTimer += deltaTime;
            if (patrolTimer >= PATROL_DURATION) {
                patrolTimer = 0f;
                patrolDir   = -patrolDir;
            }
            setVelocity(new float[]{ patrolDir * PATROL_SPEED, 0f });
            flipSprite = patrolDir < 0f;
            if (animState != AnimState.IDLE) {
                animState = AnimState.IDLE;
                resetAnimation();
            }
            advanceAnimation(deltaTime, WALK_RUN_FRAMES, FRAME_DURATION);
        }
    }

    /**
     * restores the enemy to its original spawn point and clears any transient
     * movement/animation state so the next round starts from a stable baseline.
     */
    @Override
    public void resetToSpawn() {
        super.resetToSpawn();
        setVelocity(new float[]{ 0f, 0f });
        animState   = AnimState.IDLE;
        flipSprite  = false;
        patrolTimer = 0f;
        patrolDir   = 1f;
        resetAnimation();
    }

    /**
     * returns whether this enemy is currently in its attack state.
     *
     * @return true when the active animation/behaviour state is ATTACK
     */
    @Override
    public boolean isAttacking() {
        return animState == AnimState.ATTACK;
    }

    /** frames 5–8 of the 9-frame attack strip are the active hit window */
    @Override
    public boolean isHitActive() {
        return animState == AnimState.ATTACK && currentFrame >= 5 && currentFrame <= 8;
    }
}

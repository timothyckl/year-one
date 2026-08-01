package com.p1_7.game.character;

import com.p1_7.abstractengine.collision.ICollidable;
import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.input.GameActions;
import com.p1_7.game.round.RoundPhase;
import com.p1_7.game.round.ILevelOrchestrator;
import com.p1_7.game.platform.GdxDrawContext;

/**
 * the player character — moves freely around the maze in response to input.
 *
 * extends Character for shared spatial infrastructure. movement is locked
 * during non-interactive phases (QUESTION_INTRO, FEEDBACK, ROUND_RESET).
 * wall collision is handled reactively by MazeCollisionManager, which pushes
 * the player out of any penetrating wall after move() runs.
 *
 * call update() each frame to resolve input and set velocity. position
 * integration is delegated to GameMovementManager, which calls move() after
 * GameScene.update() has set the velocity.
 */
public class Player extends Character {

    /** movement speed in pixels per second */
    private static final float SPEED = 160f;

    /** minimum time between successive enemy-contact hits */
    private static final float ENEMY_HIT_COOLDOWN_SECONDS = 1.0f;

    /** collision box side length in pixels */
    private static final float SIZE = 20f;

    /** path to the walk-cycle sprite strip */
    private static final String SPRITE_ASSET = "player.png";

    /** path to the hurt animation sprite strip */
    private static final String HURT_ASSET = "player-hurt.png";

    /** number of frames in the sprite strip */
    private static final int FRAME_COUNT = 8;

    /** width of one frame in the source texture, in pixels */
    private static final int FRAME_WIDTH = 96;

    /** height of one frame in the source texture, in pixels */
    private static final int FRAME_HEIGHT = 64;

    /** rendered width of the sprite in world units */
    private static final float DISPLAY_W = 192f;

    /** rendered height of the sprite in world units */
    private static final float DISPLAY_H = 128f;

    /** seconds each animation frame is held before advancing */
    private static final float FRAME_DURATION = 0.1f;

    /** total frames in the hurt strip */
    private static final int HURT_FRAME_COUNT = 8;

    /** seconds each hurt frame is held before advancing */
    private static final float HURT_FRAME_DURATION = 0.1f;

    /** true when the sprite should be flipped horizontally (i.e. player is moving left) */
    private boolean flipSprite = false;

    /** index of the current hurt frame while the damage animation is active */
    private int hurtFrame = 0;

    /** accumulated time since the last hurt-frame advance */
    private float hurtFrameTimer = 0f;

    /** true while the hurt animation is being displayed */
    private boolean hurtAnimating = false;

    /** orchestrator reference used to apply enemy-contact damage */
    private ILevelOrchestrator orchestrator;

    /** scene-level listener notified when the player takes damage; may be null */
    private PlayerDamageListener damageListener;

    /** countdown timer that throttles repeated enemy-contact hits */
    private float enemyHitCooldownTimer = 0f;

    /**
     * constructs a player centred on the given spawn coordinates.
     *
     * @param spawnX world x coordinate of the desired spawn centre
     * @param spawnY world y coordinate of the desired spawn centre
     */
    public Player(float spawnX, float spawnY) {
        super(spawnX, spawnY, SIZE);
    }

    // IRenderable ─────────────────────────────────────────────

    /**
     * returns the walk-cycle sprite strip path for asset pre-loading.
     *
     * @return path to the player sprite strip
     */
    @Override
    public String getAssetPath() {
        return SPRITE_ASSET;
    }

    /**
     * draws the current animation frame centred on the player's collision box.
     *
     * @param ctx the draw context for this frame
     */
    @Override
    public void render(IDrawContext ctx) {
        GdxDrawContext gdx   = (GdxDrawContext) ctx;
        // centre the larger sprite display over the smaller collision box
        float drawX = transform.getPosition(0) + (SIZE - DISPLAY_W) / 2f;
        float drawY = transform.getPosition(1) + (SIZE - DISPLAY_H) / 2f;
        String asset = hurtAnimating ? HURT_ASSET : SPRITE_ASSET;
        int   srcX  = (hurtAnimating ? hurtFrame : currentFrame) * FRAME_WIDTH;
        gdx.drawTextureRegion(asset, srcX, 0, FRAME_WIDTH, FRAME_HEIGHT,
                              drawX, drawY, DISPLAY_W, DISPLAY_H, flipSprite);
    }

    // per-frame update ────────────────────────────────────────

    /**
     * resolves input and applies phase locking for this frame.
     *
     * must be called before move(). the resolved velocity is committed via
     * setVelocity(). wall collision is handled reactively by MazeCollisionManager
     * after move() runs — no inline AABB check is needed here.
     *
     * @param deltaTime  seconds elapsed since the previous frame
     * @param inputQuery the logical input query for this frame
     * @param phase      the current round phase; movement is locked unless CHOOSING
     */
    public void update(float deltaTime, IInputQuery inputQuery, RoundPhase phase) {
        updateHurtAnimation(deltaTime);
        enemyHitCooldownTimer = Math.max(0f, enemyHitCooldownTimer - deltaTime);

        // only allow movement during the choosing phase; any other phase locks the player
        if (phase != RoundPhase.CHOOSING) {
            setVelocity(new float[]{ 0f, 0f });
            return;
        }

        // read raw directional input and commit as velocity
        float rawVx = (inputQuery.isActionActive(GameActions.MOVE_RIGHT) ? SPEED : 0f)
                    - (inputQuery.isActionActive(GameActions.MOVE_LEFT)  ? SPEED : 0f);
        float rawVy = (inputQuery.isActionActive(GameActions.MOVE_UP)    ? SPEED : 0f)
                    - (inputQuery.isActionActive(GameActions.MOVE_DOWN)  ? SPEED : 0f);

        setVelocity(new float[]{ rawVx, rawVy });

        // update facing direction on horizontal input only
        if (rawVx > 0f) flipSprite = false;
        else if (rawVx < 0f) flipSprite = true;

        // advance animation while moving; reset to the first frame when idle
        if (rawVx != 0f || rawVy != 0f) {
            advanceAnimation(deltaTime, FRAME_COUNT, FRAME_DURATION);
        } else {
            resetAnimation();
        }
    }

    /**
     * starts the one-shot hurt animation from its first frame.
     */
    public void triggerDamageAnimation() {
        hurtAnimating = true;
        hurtFrame = 0;
        hurtFrameTimer = 0f;
    }

    @Override
    public void resetToSpawn(float[] spawnPoint) {
        super.resetToSpawn(spawnPoint);
        hurtAnimating = false;
        hurtFrame = 0;
        hurtFrameTimer = 0f;
        enemyHitCooldownTimer = 0f;
    }

    /**
     * binds the gameplay orchestrator so collision callbacks can apply health changes.
     *
     * @param orchestrator current gameplay orchestrator
     */
    public void bindGameplay(ILevelOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    /**
     * registers the listener notified when the player takes damage.
     *
     * @param damageListener the damage callback, or null to remove it
     */
    public void bindDamageListener(PlayerDamageListener damageListener) {
        this.damageListener = damageListener;
    }

    @Override
    public void onCollision(ICollidable other) {
        if (!(other instanceof EnemyDamageZone)) {
            return;
        }
        EnemyDamageZone damageZone = (EnemyDamageZone) other;
        if (orchestrator == null || enemyHitCooldownTimer > 0f) {
            return;
        }
        if (!damageZone.isDangerous() || orchestrator.getPhase() != RoundPhase.CHOOSING) {
            return;
        }

        int healthBefore = orchestrator.getHealth();
        orchestrator.applyEnemyDamage();
        if (orchestrator.getHealth() < healthBefore) {
            triggerDamageAnimation();
            enemyHitCooldownTimer = ENEMY_HIT_COOLDOWN_SECONDS;
            if (damageListener != null) {
                damageListener.onPlayerDamaged();
            }
        }
    }

    /**
     * advances the temporary hurt animation independently of movement state.
     *
     * @param deltaTime seconds elapsed since the previous frame
     */
    private void updateHurtAnimation(float deltaTime) {
        if (!hurtAnimating) {
            return;
        }

        hurtFrameTimer += deltaTime;
        while (hurtFrameTimer >= HURT_FRAME_DURATION) {
            hurtFrameTimer -= HURT_FRAME_DURATION;
            hurtFrame++;
            if (hurtFrame >= HURT_FRAME_COUNT) {
                hurtAnimating = false;
                hurtFrame = 0;
                hurtFrameTimer = 0f;
                break;
            }
        }
    }
}

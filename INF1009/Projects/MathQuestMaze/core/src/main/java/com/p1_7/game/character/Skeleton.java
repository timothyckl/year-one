package com.p1_7.game.character;

import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.platform.GdxDrawContext;

/**
 * roaming skeleton enemy that patrols a fixed route around the maze until it
 * spots the player, then switches into the same chase/attack pressure pattern
 * used by other hostiles.
 */
public class Skeleton extends HostileCharacter {

    private static final float SIZE = 20f;
    private static final float DAMAGE_BOX_SIZE = 140f;

    private static final float PATROL_SPEED = 58f;
    private static final float CHASE_SPEED = 76f;
    private static final float VIEW_RANGE = 260f;
    private static final float ATTACK_RANGE = 40f;
    private static final float ATTACK_RELEASE_RANGE = 60f;
    private static final float WAYPOINT_REACHED_DISTANCE = 10f;

    private static final String WALK_ASSET = "skeleton-walk.png";
    private static final String ATTACK_ASSET = "skeleton-attack.png";

    private static final int WALK_FRAMES = 8;
    private static final int ATTACK_FRAMES = 7;
    private static final int FRAME_W = 96;
    private static final int FRAME_H = 64;

    private static final float DISPLAY_W = 168f;
    private static final float DISPLAY_H = 128f;
    private static final float ATTACK_DISPLAY_W = 208f;
    private static final float ATTACK_DISPLAY_H = 128f;

    private static final float WALK_FRAME_DURATION = 0.11f;
    private static final float ATTACK_FRAME_DURATION = 0.1f;

    private enum AnimState { PATROL, CHASE, ATTACK }

    private final float[][] patrolRoute;

    private AnimState animState = AnimState.PATROL;
    private boolean flipSprite = false;
    private int patrolIndex = 0;

    /**
     * constructs a skeleton centred on the given spawn point with a fixed patrol route.
     *
     * @param spawnX      world x coordinate of the spawn centre
     * @param spawnY      world y coordinate of the spawn centre
     * @param patrolRoute non-empty ordered loop of [x, y] patrol waypoints
     */
    public Skeleton(float spawnX, float spawnY, float[][] patrolRoute) {
        super(spawnX, spawnY, SIZE, DAMAGE_BOX_SIZE);
        if (patrolRoute == null || patrolRoute.length == 0) {
            throw new IllegalArgumentException("patrolRoute must contain at least one waypoint");
        }
        this.patrolRoute = copyRoute(patrolRoute);
    }

    @Override
    public String getAssetPath() {
        return WALK_ASSET;
    }

    @Override
    public void render(IDrawContext ctx) {
        GdxDrawContext gdx = (GdxDrawContext) ctx;

        if (animState == AnimState.ATTACK) {
            float drawX = transform.getPosition(0) + (SIZE - ATTACK_DISPLAY_W) / 2f;
            float drawY = transform.getPosition(1) + (SIZE - ATTACK_DISPLAY_H) / 2f;
            int srcX = currentFrame * FRAME_W;
            gdx.drawTextureRegion(ATTACK_ASSET, srcX, 0, FRAME_W, FRAME_H,
                drawX, drawY, ATTACK_DISPLAY_W, ATTACK_DISPLAY_H, flipSprite);
            return;
        }

        float drawX = transform.getPosition(0) + (SIZE - DISPLAY_W) / 2f;
        float drawY = transform.getPosition(1) + (SIZE - DISPLAY_H) / 2f;
        int srcX = currentFrame * FRAME_W;
        gdx.drawTextureRegion(WALK_ASSET, srcX, 0, FRAME_W, FRAME_H,
            drawX, drawY, DISPLAY_W, DISPLAY_H, flipSprite);
    }

    @Override
    public void update(float deltaTime, ITransform playerTransform, boolean hasLineOfSight) {
        float ex = transform.getPosition(0) + SIZE / 2f;
        float ey = transform.getPosition(1) + SIZE / 2f;
        float px = playerTransform.getPosition(0) + SIZE / 2f;
        float py = playerTransform.getPosition(1) + SIZE / 2f;
        float dx = px - ex;
        float dy = py - ey;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        boolean inAttackRange = hasLineOfSight && (
            dist <= ATTACK_RANGE
            || (animState == AnimState.ATTACK && dist <= ATTACK_RELEASE_RANGE)
        );

        if (inAttackRange) {
            setVelocity(new float[]{ 0f, 0f });
            if (Math.abs(dx) > 0.001f) {
                flipSprite = dx < 0f;
            }
            if (animState != AnimState.ATTACK) {
                animState = AnimState.ATTACK;
                resetAnimation();
            }
            advanceAnimation(deltaTime, ATTACK_FRAMES, ATTACK_FRAME_DURATION);
            return;
        }

        if (hasLineOfSight && dist <= VIEW_RANGE && dist > 0.001f) {
            float nx = dx / dist;
            float ny = dy / dist;
            setVelocity(new float[]{ nx * CHASE_SPEED, ny * CHASE_SPEED });
            if (Math.abs(dx) > 0.001f) {
                flipSprite = dx < 0f;
            }
            if (animState != AnimState.CHASE) {
                animState = AnimState.CHASE;
                resetAnimation();
            }
            advanceAnimation(deltaTime, WALK_FRAMES, WALK_FRAME_DURATION);
            return;
        }

        patrol(deltaTime, ex, ey);
    }

    @Override
    public void resetToSpawn() {
        super.resetToSpawn();
        setVelocity(new float[]{ 0f, 0f });
        animState = AnimState.PATROL;
        flipSprite = false;
        patrolIndex = 0;
        resetAnimation();
    }

    @Override
    public boolean isAttacking() {
        return animState == AnimState.ATTACK;
    }

    /** frames 5-6 of the 7-frame attack strip are the active hit window */
    @Override
    public boolean isHitActive() {
        return animState == AnimState.ATTACK && currentFrame >= 5 && currentFrame <= 6;
    }

    private void patrol(float deltaTime, float ex, float ey) {
        float[] target = patrolRoute[patrolIndex];
        float dx = target[0] - ex;
        float dy = target[1] - ey;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist <= WAYPOINT_REACHED_DISTANCE) {
            patrolIndex = (patrolIndex + 1) % patrolRoute.length;
            target = patrolRoute[patrolIndex];
            dx = target[0] - ex;
            dy = target[1] - ey;
            dist = (float) Math.sqrt(dx * dx + dy * dy);
        }

        if (dist > 0.001f) {
            float nx = dx / dist;
            float ny = dy / dist;
            setVelocity(new float[]{ nx * PATROL_SPEED, ny * PATROL_SPEED });
            if (Math.abs(nx) > 0.001f) {
                flipSprite = nx < 0f;
            }
        } else {
            setVelocity(new float[]{ 0f, 0f });
        }

        if (animState != AnimState.PATROL) {
            animState = AnimState.PATROL;
            resetAnimation();
        }
        advanceAnimation(deltaTime, WALK_FRAMES, WALK_FRAME_DURATION);
    }

    private static float[][] copyRoute(float[][] route) {
        float[][] copy = new float[route.length][2];
        for (int i = 0; i < route.length; i++) {
            if (route[i] == null || route[i].length != 2) {
                throw new IllegalArgumentException("patrolRoute[" + i + "] must be a two-element [x, y] point");
            }
            copy[i][0] = route[i][0];
            copy[i][1] = route[i][1];
        }
        return copy;
    }
}

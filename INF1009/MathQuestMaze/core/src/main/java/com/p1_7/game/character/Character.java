package com.p1_7.game.character;

import com.p1_7.abstractengine.collision.IBounds;
import com.p1_7.abstractengine.collision.ICollidable;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.movement.IMovable;
import com.p1_7.abstractengine.render.IRenderable;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.spatial.Bounds2D;
import com.p1_7.game.spatial.Transform2D;

/**
 * abstract base for all moving, collidable, renderable entities in the maze.
 *
 * provides shared spatial infrastructure — transform, bounds, velocity,
 * acceleration — and implements IMovable and ICollidable so concrete subclasses
 * only need to define appearance (getAssetPath, render) and behaviour (update).
 *
 * animation state (currentFrame, frameTimer) and helpers are included for
 * subclasses that use sprite strips.
 */
public abstract class Character extends Entity implements IRenderable, IMovable, ICollidable {

    /** collision box side length supplied at construction */
    protected final float size;

    /** bottom-left origin, size × size */
    protected final Transform2D transform;

    /** AABB synced with transform position on each getBounds() call */
    protected final Bounds2D bounds;

    /** reusable position scratch for getBounds() — avoids per-call allocation */
    private final float[] boundsPos = new float[2];

    /** fixed extent array reused across all getBounds() calls */
    private final float[] boundsExtent;

    /** [vx, vy] — set each frame by the concrete subclass */
    protected float[] velocity;

    /** [ax, ay] — satisfies IMovable; unused by most subclasses */
    protected float[] acceleration;

    /** index of the frame currently displayed (0-based) */
    protected int currentFrame = 0;

    /** accumulated time since the last frame advance, in seconds */
    protected float frameTimer = 0f;

    /**
     * constructs a character centred on the given spawn coordinates.
     *
     * @param spawnX world x coordinate of the spawn centre
     * @param spawnY world y coordinate of the spawn centre
     * @param size   collision box side length in pixels
     */
    protected Character(float spawnX, float spawnY, float size) {
        this.size         = size;
        this.transform    = new Transform2D(spawnX - size / 2f, spawnY - size / 2f, size, size);
        this.bounds       = new Bounds2D(spawnX - size / 2f, spawnY - size / 2f, size, size);
        this.boundsExtent = new float[]{ size, size };
        this.velocity     = new float[]{ 0f, 0f };
        this.acceleration = new float[]{ 0f, 0f };
    }

    // ITransformable ──────────────────────────────────────────

    @Override
    public ITransform getTransform() {
        return transform;
    }

    // IMovable ────────────────────────────────────────────────

    @Override
    public float[] getVelocity() {
        return velocity.clone();
    }

    @Override
    public void setVelocity(float[] velocity) {
        this.velocity = velocity.clone();
    }

    @Override
    public float[] getAcceleration() {
        return acceleration.clone();
    }

    @Override
    public void setAcceleration(float[] accel) {
        this.acceleration = accel.clone();
    }

    /**
     * integrates the current velocity into the entity's position.
     *
     * @param deltaTime seconds elapsed since the previous frame
     */
    @Override
    public void move(float deltaTime) {
        transform.setPosition(0, transform.getPosition(0) + velocity[0] * deltaTime);
        transform.setPosition(1, transform.getPosition(1) + velocity[1] * deltaTime);
    }

    // ICollidable ─────────────────────────────────────────────

    /**
     * returns the bounding box synced to the current transform position.
     *
     * @return the AABB for this frame's position
     */
    @Override
    public IBounds getBounds() {
        boundsPos[0] = transform.getPosition(0);
        boundsPos[1] = transform.getPosition(1);
        bounds.set(boundsPos, boundsExtent);
        return bounds;
    }

    /**
     * no-op — position correction is handled by MazeCollisionManager.
     *
     * @param other the collidable that this entity collided with
     */
    @Override
    public void onCollision(ICollidable other) {
        // wall push is delegated to MazeCollisionManager.resolve()
    }

    // spawn reset ─────────────────────────────────────────────

    /**
     * recentres the character on the given spawn coordinates.
     *
     * @param spawnPoint a two-element [x, y] array; must not be null
     * @throws IllegalArgumentException if spawnPoint is null or not length 2
     */
    public void resetToSpawn(float[] spawnPoint) {
        if (spawnPoint == null) {
            throw new IllegalArgumentException("spawnPoint must not be null");
        }
        if (spawnPoint.length != 2) {
            throw new IllegalArgumentException(
                "spawnPoint must have exactly 2 elements, got: " + spawnPoint.length);
        }
        transform.setPosition(0, spawnPoint[0] - size / 2f);
        transform.setPosition(1, spawnPoint[1] - size / 2f);
    }

    // animation helpers ───────────────────────────────────────

    /**
     * advances the walk-cycle frame counter while the entity is in motion.
     *
     * @param deltaTime     seconds elapsed since the previous frame
     * @param frameCount    total number of frames in the strip
     * @param frameDuration seconds each frame is held before advancing
     */
    protected void advanceAnimation(float deltaTime, int frameCount, float frameDuration) {
        frameTimer += deltaTime;
        if (frameTimer >= frameDuration) {
            frameTimer  -= frameDuration;
            currentFrame = (currentFrame + 1) % frameCount;
        }
    }

    /**
     * resets the animation to the first frame and clears the frame timer.
     */
    protected void resetAnimation() {
        currentFrame = 0;
        frameTimer   = 0f;
    }
}

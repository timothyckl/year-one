package com.p1_7.game.character;

import com.p1_7.abstractengine.collision.IBounds;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.spatial.Bounds2D;

/**
 * shared base for enemy-style characters that can damage the player.
 *
 * owns the spawn point and enlarged damage bounds used by EnemyDamageZone so
 * concrete hostile types only need to implement rendering and behaviour.
 */
public abstract class HostileCharacter extends Character {

    /** original spawn centre retained so round resets can restore this enemy */
    private final float[] spawnPoint;

    /** reusable enlarged damage bounds so contact checks do not allocate each frame */
    private final Bounds2D damageBounds;

    /** reusable min-position scratch for the enlarged damage bounds */
    private final float[] damageBoundsPos = new float[2];

    /** fixed extent for the enlarged damage bounds */
    private final float[] damageBoundsExtent;

    protected HostileCharacter(float spawnX, float spawnY, float size, float damageBoxSize) {
        super(spawnX, spawnY, size);
        this.spawnPoint = new float[]{ spawnX, spawnY };
        this.damageBounds = new Bounds2D(
            spawnX - damageBoxSize / 2f,
            spawnY - damageBoxSize / 2f,
            damageBoxSize,
            damageBoxSize
        );
        this.damageBoundsExtent = new float[]{ damageBoxSize, damageBoxSize };
    }

    /**
     * returns whether this hostile should currently deal damage on contact.
     *
     * @return true when the active behaviour is in an attack state
     */
    public abstract boolean isAttacking();

    /**
     * returns true only during the frames of the attack animation where the hit lands.
     * damage is applied during this window only, not throughout the full attack state.
     */
    public abstract boolean isHitActive();

    /**
     * updates this hostile against the current player transform and visibility state.
     *
     * @param deltaTime       seconds elapsed since the previous frame
     * @param playerTransform the player's current transform
     * @param hasLineOfSight  true when the player is visible from this hostile
     */
    public abstract void update(float deltaTime, ITransform playerTransform, boolean hasLineOfSight);

    /**
     * returns an enlarged contact box centred on the hostile body for player-damage checks.
     *
     * @return enlarged damage bounds for this frame
     */
    public IBounds getDamageBounds() {
        float cx = transform.getPosition(0) + size / 2f;
        float cy = transform.getPosition(1) + size / 2f;
        damageBoundsPos[0] = cx - damageBoundsExtent[0] / 2f;
        damageBoundsPos[1] = cy - damageBoundsExtent[1] / 2f;
        damageBounds.set(damageBoundsPos, damageBoundsExtent);
        return damageBounds;
    }

    /**
     * restores the hostile to its original spawn point.
     */
    public void resetToSpawn() {
        super.resetToSpawn(spawnPoint);
    }
}

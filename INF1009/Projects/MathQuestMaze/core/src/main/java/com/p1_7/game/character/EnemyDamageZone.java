package com.p1_7.game.character;

import com.p1_7.abstractengine.collision.IBounds;
import com.p1_7.abstractengine.collision.ICollidable;

/**
 * collision-only proxy that exposes an enemy's enlarged damage bounds.
 *
 * this allows gameplay damage detection to participate in the collision system
 * without changing the enemy's wall-collision body.
 */
public final class EnemyDamageZone implements ICollidable {

    private final HostileCharacter hostile;

    /**
     * constructs a damage zone bound to the given enemy.
     *
     * @param hostile the owning hostile character
     */
    public EnemyDamageZone(HostileCharacter hostile) {
        this.hostile = hostile;
    }

    /**
     * returns the owning enemy.
     *
     * @return the bound hostile character
     */
    public HostileCharacter getEnemy() {
        return hostile;
    }

    /**
     * returns whether the zone should currently deal damage.
     *
     * @return true when the enemy is in its attack state
     */
    public boolean isDangerous() {
        return hostile.isHitActive();
    }

    @Override
    public IBounds getBounds() {
        return hostile.getDamageBounds();
    }

    @Override
    public void onCollision(ICollidable other) {
        // the player owns the damage response
    }
}

package com.p1_7.game.maze;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import com.p1_7.game.character.Character;
import com.p1_7.game.character.EnemyDamageZone;
import com.p1_7.game.character.GameMovementManager;
import com.p1_7.game.character.HostileCharacter;
import com.p1_7.game.character.Player;
import com.p1_7.game.collectible.Item;

import com.p1_7.abstractengine.collision.CollisionManager;
import com.p1_7.abstractengine.collision.CollisionPair;
import com.p1_7.abstractengine.collision.ICollidable;
import com.p1_7.abstractengine.engine.IManager;
import com.p1_7.abstractengine.transform.ITransform;

/**
 * concrete CollisionManager that resolves character-to-wall collisions in the maze.
 *
 * registered walls and movers are maintained separately so resolve() can identify
 * which side of each collision pair is a wall without instanceof checks. any
 * registered Character (player or enemy) is pushed out of penetrating walls using
 * a minimum translation vector (MTV) computed from axis-aligned overlap distances.
 *
 * declaring GameMovementManager as a dependency ensures this manager's onUpdate()
 * runs after GameMovementManager.onUpdate() — which calls move() and clamps to
 * viewport bounds — so position corrections are applied to the post-integration
 * position rather than the pre-movement position.
 */
public class MazeCollisionManager extends CollisionManager {

    /** set of all registered wall collidables for fast membership tests in resolve() */
    private final Set<ICollidable> registeredWalls = new HashSet<>();

    /** all registered movers (player + enemies) that should be pushed out of walls */
    private final Set<Character> movers = new HashSet<>();

    /** the player reference; retained so unregisterPlayer() can remove the correct entry */
    private Player player;

    /** active pickup items registered for collision dispatch */
    private final Set<Item> items = new HashSet<>();

    /** per-hostile damage-zone collidables used for attack hit detection */
    private final Map<HostileCharacter, EnemyDamageZone> enemyDamageZones = new HashMap<>();

    /**
     * declares GameMovementManager as this manager's sole dependency so the engine
     * schedules this manager's onUpdate() after GameMovementManager.onUpdate() each frame.
     *
     * @return array containing GameMovementManager.class
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends IManager>[] getDependencies() {
        return new Class[]{ GameMovementManager.class };
    }

    // registration helpers ─────────────────────────────────────

    /**
     * registers the player for collision detection and wall-push resolution.
     *
     * @param p the player to register; must not be null
     */
    public void registerPlayer(Player p) {
        this.player = p;
        movers.add(p);
        registerCollidable(p);
    }

    /**
     * registers an enemy or other Character for collision detection and wall-push resolution.
     *
     * @param mover the character to register; must not be null
     */
    public void registerMover(Character mover) {
        movers.add(mover);
        registerCollidable(mover);
        if (mover instanceof HostileCharacter) {
            HostileCharacter hostile = (HostileCharacter) mover;
            EnemyDamageZone damageZone = new EnemyDamageZone(hostile);
            enemyDamageZones.put(hostile, damageZone);
            registerCollidable(damageZone);
        }
    }

    /**
     * registers a wall collidable for collision detection.
     *
     * @param wall the wall to register; must not be null
     */
    public void registerWall(WallCollidable wall) {
        registeredWalls.add(wall);
        registerCollidable(wall);
    }

    /**
     * registers a pickup item for collision detection.
     *
     * @param item the item to register
     */
    public void registerItem(Item item) {
        items.add(item);
        registerCollidable(item);
    }

    /**
     * removes the player from collision detection and clears the player reference.
     */
    public void unregisterPlayer() {
        if (player != null) {
            movers.remove(player);
            unregisterCollidable(player);
        }
        player = null;
    }

    /**
     * removes a character mover from collision detection.
     *
     * @param mover the character to unregister
     */
    public void unregisterMover(Character mover) {
        movers.remove(mover);
        unregisterCollidable(mover);
        if (mover instanceof HostileCharacter) {
            HostileCharacter hostile = (HostileCharacter) mover;
            EnemyDamageZone damageZone = enemyDamageZones.remove(hostile);
            if (damageZone != null) {
                unregisterCollidable(damageZone);
            }
        }
    }

    /**
     * removes a wall from collision detection.
     *
     * @param wall the wall to unregister
     */
    public void unregisterWall(WallCollidable wall) {
        registeredWalls.remove(wall);
        unregisterCollidable(wall);
    }

    /**
     * removes a pickup item from collision detection.
     *
     * @param item the item to unregister
     */
    public void unregisterItem(Item item) {
        items.remove(item);
        unregisterCollidable(item);
    }

    // resolution ──────────────────────────────────────────────

    /**
     * resolves all detected collisions by pushing any registered mover out of
     * penetrating walls using the minimum translation vector.
     *
     * @param collisions the list of detected collision pairs for this frame
     */
    @Override
    protected void resolve(List<CollisionPair> collisions) {
        if (movers.isEmpty()) return;

        for (CollisionPair pair : collisions) {
            // identify which side of the pair is a registered wall
            ICollidable wall;
            ICollidable other;
            if (registeredWalls.contains(pair.getEntityA())) {
                wall  = pair.getEntityA();
                other = pair.getEntityB();
            } else if (registeredWalls.contains(pair.getEntityB())) {
                wall  = pair.getEntityB();
                other = pair.getEntityA();
            } else {
                // neither entity is a known wall — nothing to resolve
                continue;
            }

            // only push out registered movers; ignore wall-to-non-mover pairs
            if (movers.contains(other)) {
                pushMoverOut(wall, (Character) other);
            }

            pair.getEntityA().onCollision(pair.getEntityB());
            pair.getEntityB().onCollision(pair.getEntityA());
            continue;
        }

        for (CollisionPair pair : collisions) {
            if (registeredWalls.contains(pair.getEntityA()) || registeredWalls.contains(pair.getEntityB())) {
                continue;
            }
            pair.getEntityA().onCollision(pair.getEntityB());
            pair.getEntityB().onCollision(pair.getEntityA());
        }
    }

    /**
     * computes the minimum translation vector between a mover and a wall,
     * then applies it to the mover's transform to eliminate penetration.
     *
     * the axis with the smaller overlap is chosen so the correction is minimal
     * and feels natural at corners.
     *
     * @param wall  the wall the mover has penetrated
     * @param mover the character to push out
     */
    private void pushMoverOut(ICollidable wall, Character mover) {
        float[] mMin = mover.getBounds().getMinPosition();
        float[] mExt = mover.getBounds().getExtent();
        float[] wMin = wall.getBounds().getMinPosition();
        float[] wExt = wall.getBounds().getExtent();

        // compute overlap depth on each axis
        float overlapX = Math.min(mMin[0] + mExt[0], wMin[0] + wExt[0]) - Math.max(mMin[0], wMin[0]);
        float overlapY = Math.min(mMin[1] + mExt[1], wMin[1] + wExt[1]) - Math.max(mMin[1], wMin[1]);

        ITransform t = mover.getTransform();

        if (overlapX <= overlapY) {
            // push along x — smaller overlap axis
            float correction = (mMin[0] < wMin[0]) ? -overlapX : overlapX;
            t.setPosition(0, t.getPosition(0) + correction);
        } else {
            // push along y — smaller overlap axis
            float correction = (mMin[1] < wMin[1]) ? -overlapY : overlapY;
            t.setPosition(1, t.getPosition(1) + correction);
        }
    }
}

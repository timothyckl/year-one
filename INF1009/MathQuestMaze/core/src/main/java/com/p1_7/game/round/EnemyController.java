package com.p1_7.game.round;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.p1_7.abstractengine.collision.IBounds;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.character.Goblin;
import com.p1_7.game.character.HostileCharacter;
import com.p1_7.game.character.Player;
import com.p1_7.game.character.Skeleton;
import com.p1_7.game.math.Difficulty;
import com.p1_7.game.maze.MazeLayout;
import com.p1_7.game.maze.WallCollidable;

/**
 * coordinates enemy spawning, AI updates, and line-of-sight checks.
 *
 * extracts all enemy-related logic that was previously inline in GameScene,
 * keeping the scene class focused on lifecycle orchestration.
 */
public class EnemyController {

    /** reusable scratch window for Liang-Barsky clipping */
    private final float[] clipWindow = new float[2];

    /**
     * spawns goblins at corner-room centres and difficulty-scaled skeletons
     * at corridor intersections.
     *
     * @param layout     the maze layout providing room bounds and pathway data
     * @param difficulty the current difficulty level
     * @return a mutable list of all spawned hostile characters
     */
    public List<HostileCharacter> spawnEnemies(MazeLayout layout, Difficulty difficulty) {
        float[][] roomBounds = new float[4][];
        for (int i = 0; i < 4; i++) {
            roomBounds[i] = layout.getRoomBounds(i);
        }

        int skeletonCount = getSkeletonCount(difficulty);
        List<HostileCharacter> enemies = new ArrayList<>(4 + skeletonCount);

        // one goblin at the centre of each corner room
        for (float[] room : roomBounds) {
            float cx = room[0] + room[2] / 2f;
            float cy = room[1] + room[3] / 2f;
            enemies.add(new Goblin(cx, cy));
        }

        // skeletons at distinct corridor intersections sharing a maze-wide patrol loop
        int[] startIndices = chooseSkeletonStartIndices(skeletonCount);
        for (int startIndex : startIndices) {
            float[][] route = createSkeletonPatrolRoute(layout, startIndex);
            enemies.add(new Skeleton(route[0][0], route[0][1], route));
        }

        return enemies;
    }

    /**
     * updates every enemy's AI against the current player state.
     *
     * @param deltaTime seconds elapsed since the previous frame
     * @param enemies   the active enemy list
     * @param player    the player entity
     * @param walls     wall collidables used for line-of-sight testing
     */
    public void updateEnemies(float deltaTime, List<HostileCharacter> enemies,
                              Player player, List<WallCollidable> walls) {
        for (HostileCharacter enemy : enemies) {
            enemy.update(deltaTime, player.getTransform(),
                         hasLineOfSightToPlayer(enemy, player, walls));
        }
    }

    /**
     * zeros the velocity of every enemy so they freeze in place.
     * used during non-interactive phases.
     *
     * @param enemies the active enemy list
     */
    public void freezeEnemies(List<HostileCharacter> enemies) {
        for (HostileCharacter enemy : enemies) {
            enemy.setVelocity(new float[]{ 0f, 0f });
        }
    }

    // line-of-sight ───────────────────────────────────────────────────

    /**
     * returns true when the straight line from enemy centre to player centre
     * is not blocked by any wall rectangle.
     */
    private boolean hasLineOfSightToPlayer(HostileCharacter enemy, Player player,
                                           List<WallCollidable> walls) {
        ITransform enemyTransform  = enemy.getTransform();
        ITransform playerTransform = player.getTransform();

        float ex = enemyTransform.getPosition(0) + enemyTransform.getSize(0) / 2f;
        float ey = enemyTransform.getPosition(1) + enemyTransform.getSize(1) / 2f;
        float px = playerTransform.getPosition(0) + playerTransform.getSize(0) / 2f;
        float py = playerTransform.getPosition(1) + playerTransform.getSize(1) / 2f;

        for (WallCollidable wall : walls) {
            if (segmentIntersectsRect(ex, ey, px, py, wall.getBounds())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Liang-Barsky slab test for segment p0->p1 against an axis-aligned rectangle.
     */
    private boolean segmentIntersectsRect(float x0, float y0, float x1, float y1,
                                          IBounds bounds) {
        float[] min = bounds.getMinPosition();
        float[] ext = bounds.getExtent();

        float minX = min[0];
        float minY = min[1];
        float maxX = min[0] + ext[0];
        float maxY = min[1] + ext[1];

        float dx = x1 - x0;
        float dy = y1 - y0;
        clipWindow[0] = 0f;
        clipWindow[1] = 1f;

        if (!clipTest(-dx, x0 - minX)) return false;
        if (!clipTest( dx, maxX - x0)) return false;
        if (!clipTest(-dy, y0 - minY)) return false;
        return clipTest(dy, maxY - y0);
    }

    /**
     * one Liang-Barsky clipping step that narrows the parametric segment window.
     */
    private boolean clipTest(float p, float q) {
        float tMin = clipWindow[0];
        float tMax = clipWindow[1];

        if (p == 0f) {
            if (q < 0f) return false;
            clipWindow[0] = tMin;
            clipWindow[1] = tMax;
            return true;
        }

        float r = q / p;
        if (p < 0f) {
            if (r > tMax) return false;
            if (r > tMin) tMin = r;
        } else {
            if (r < tMin) return false;
            if (r < tMax) tMax = r;
        }

        clipWindow[0] = tMin;
        clipWindow[1] = tMax;
        return true;
    }

    // skeleton spawning ───────────────────────────────────────────────

    /**
     * builds a map-wide patrol loop and rotates it so the skeleton starts
     * at the given corridor intersection.
     */
    private float[][] createSkeletonPatrolRoute(MazeLayout layout, int startIndex) {
        List<float[]> pathways = layout.getPathwayBounds();
        if (pathways.size() < 8) {
            throw new IllegalStateException(
                "MazeLayout must expose at least eight pathway rectangles for skeleton patrol routing");
        }

        float topY    = centreY(pathways.get(0));
        float bottomY = centreY(pathways.get(1));
        float leftX   = centreX(pathways.get(2));
        float rightX  = centreX(pathways.get(3));
        float middleY = centreY(pathways.get(7));
        float centreX = centreX(pathways.get(4));

        float[][] route = new float[][] {
            { leftX,   topY    },
            { centreX, topY    },
            { rightX,  topY    },
            { rightX,  middleY },
            { rightX,  bottomY },
            { centreX, bottomY },
            { leftX,   bottomY },
            { leftX,   middleY }
        };

        return rotateRoute(route, startIndex);
    }

    /**
     * selects distinct corridor-intersection start indices for the given number of skeletons.
     */
    private int[] chooseSkeletonStartIndices(int skeletonCount) {
        int[] spawnable = { 1, 3, 5, 7 };
        if (skeletonCount < 0 || skeletonCount > spawnable.length) {
            throw new IllegalArgumentException(
                "skeletonCount must be in [0, " + spawnable.length + "], got: " + skeletonCount);
        }

        int[] chosen = spawnable.clone();
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        for (int i = chosen.length - 1; i > 0; i--) {
            int swapIndex = rng.nextInt(i + 1);
            int tmp = chosen[i];
            chosen[i] = chosen[swapIndex];
            chosen[swapIndex] = tmp;
        }

        return Arrays.copyOf(chosen, skeletonCount);
    }

    /**
     * returns the number of skeletons for the given difficulty.
     *
     * @param difficulty the current difficulty level
     * @return 3 for HARD, 2 for MEDIUM, 1 for EASY
     */
    public static int getSkeletonCount(Difficulty difficulty) {
        if (difficulty == Difficulty.HARD)   return 3;
        if (difficulty == Difficulty.MEDIUM) return 2;
        return 1;
    }

    private static float centreX(float[] rect) {
        return rect[0] + rect[2] / 2f;
    }

    private static float centreY(float[] rect) {
        return rect[1] + rect[3] / 2f;
    }

    private static float[][] rotateRoute(float[][] route, int startIndex) {
        float[][] rotated = new float[route.length][2];
        for (int i = 0; i < route.length; i++) {
            float[] point = route[(startIndex + i) % route.length];
            rotated[i][0] = point[0];
            rotated[i][1] = point[1];
        }
        return rotated;
    }
}

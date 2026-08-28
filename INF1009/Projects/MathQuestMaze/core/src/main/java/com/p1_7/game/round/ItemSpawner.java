package com.p1_7.game.round;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.p1_7.game.GameConfig;
import com.p1_7.game.collectible.Heart;
import com.p1_7.game.collectible.Item;
import com.p1_7.game.maze.MazeLayout;

/**
 * spawns heart pickups within corridor/pathway rectangles, with
 * area-weighted random placement and minimum spacing enforcement.
 */
public class ItemSpawner {

    /**
     * spawns a random set of heart pickups in the maze pathways.
     *
     * @param layout       the maze layout providing pathway bounds
     * @param orchestrator the level orchestrator passed to each heart for heal callbacks
     * @return a mutable list of newly spawned items
     */
    public List<Item> spawnItems(MazeLayout layout, ILevelOrchestrator orchestrator) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        List<float[]> pathways = layout.getPathwayBounds();
        List<Item> spawnedItems = new ArrayList<>();
        int heartCount = rng.nextInt(GameConfig.HEART_SPAWN_MIN, GameConfig.HEART_SPAWN_MAX);

        for (int i = 0; i < heartCount; i++) {
            Heart heart = trySpawnHeart(pathways, spawnedItems, rng, orchestrator);
            if (heart != null) {
                spawnedItems.add(heart);
            }
        }

        return spawnedItems;
    }

    /**
     * attempts to place one heart in a random pathway while keeping it away
     * from existing hearts.
     *
     * @param pathways     current walkway rectangles
     * @param existing     already spawned items
     * @param rng          random source
     * @param orchestrator the level orchestrator
     * @return a new heart, or null if placement failed after several attempts
     */
    private Heart trySpawnHeart(List<float[]> pathways, List<Item> existing,
                                ThreadLocalRandom rng,
                                ILevelOrchestrator orchestrator) {
        final float padding    = Heart.SIZE / 2f + 6f;
        final float minSpacing = Heart.SIZE * 1.6f;

        for (int attempt = 0; attempt < GameConfig.HEART_SPAWN_ATTEMPTS; attempt++) {
            float[] rect = chooseRandomPathway(pathways, rng);
            if (rect == null || rect[2] <= padding * 2f || rect[3] <= padding * 2f) {
                continue;
            }

            float cx = rng.nextFloat(rect[0] + padding, rect[0] + rect[2] - padding);
            float cy = rng.nextFloat(rect[1] + padding, rect[1] + rect[3] - padding);

            boolean overlapsExisting = false;
            for (Item item : existing) {
                float ix = item.getTransform().getPosition(0) + item.getTransform().getSize(0) / 2f;
                float iy = item.getTransform().getPosition(1) + item.getTransform().getSize(1) / 2f;
                float dx = cx - ix;
                float dy = cy - iy;
                if (dx * dx + dy * dy < minSpacing * minSpacing) {
                    overlapsExisting = true;
                    break;
                }
            }

            if (!overlapsExisting) {
                return new Heart(cx, cy, orchestrator);
            }
        }

        return null;
    }

    /**
     * chooses one pathway rectangle with probability proportional to its area.
     *
     * @param pathways candidate pathway rectangles
     * @param rng      random source
     * @return chosen rectangle, or null if none are available
     */
    private float[] chooseRandomPathway(List<float[]> pathways, ThreadLocalRandom rng) {
        if (pathways.isEmpty()) {
            return null;
        }

        float totalArea = 0f;
        for (float[] rect : pathways) {
            totalArea += rect[2] * rect[3];
        }

        float pick = rng.nextFloat() * totalArea;
        for (float[] rect : pathways) {
            pick -= rect[2] * rect[3];
            if (pick <= 0f) {
                return rect;
            }
        }

        return pathways.get(pathways.size() - 1);
    }
}

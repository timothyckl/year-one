package com.p1_7.abstractengine.collision;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates the CollisionManager's detection orchestration:
 * brute-force path (no world bounds), spatial-tree path (world bounds set),
 * and that resolve() receives the correct pairs.
 */
public class CollisionManagerTest {

    // --- Minimal IBounds implementation backed by explicit floats ---

    private static class SimpleBounds implements IBounds {
        private float x, y, w, h;

        SimpleBounds(float x, float y, float w, float h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        @Override
        public boolean overlaps(IBounds other) {
            float[] oMin = other.getMinPosition();
            float[] oExt = other.getExtent();
            return x < oMin[0] + oExt[0] && x + w > oMin[0]
                && y < oMin[1] + oExt[1] && y + h > oMin[1];
        }

        @Override public float[] getMinPosition() { return new float[]{x, y}; }
        @Override public float[] getExtent()       { return new float[]{w, h}; }
        @Override public void set(float[] minPos, float[] ext) {
            x = minPos[0]; y = minPos[1]; w = ext[0]; h = ext[1];
        }
        @Override public int getDimensions() { return 2; }
    }

    // --- Minimal ICollidable backed by SimpleBounds ---

    private static class SimpleCollidable implements ICollidable {
        private final IBounds bounds;

        SimpleCollidable(float x, float y, float w, float h) {
            this.bounds = new SimpleBounds(x, y, w, h);
        }

        @Override public IBounds getBounds()            { return bounds; }
        @Override public void onCollision(ICollidable o) {}
    }

    // --- Concrete CollisionManager that records pairs passed to resolve() ---

    private static class RecordingCollisionManager extends CollisionManager {
        final List<CollisionPair> receivedPairs = new ArrayList<>();

        @Override
        protected void resolve(List<CollisionPair> collisions) {
            receivedPairs.addAll(collisions);
        }
    }

    private RecordingCollisionManager manager;

    @BeforeEach
    public void setUp() {
        manager = new RecordingCollisionManager();
    }

    // --- brute-force path (no world bounds) ---

    @Test
    public void testBruteForce_overlappingPair_resolveReceivesPair() {
        // two 10×10 boxes that share the same origin — fully overlapping
        SimpleCollidable a = new SimpleCollidable(0, 0, 10, 10);
        SimpleCollidable b = new SimpleCollidable(5, 5, 10, 10);

        manager.registerCollidable(a);
        manager.registerCollidable(b);
        manager.update(0f);

        assertEquals(1, manager.receivedPairs.size(),
            "One overlapping pair should produce exactly one collision");
        CollisionPair pair = manager.receivedPairs.get(0);
        assertTrue((pair.getEntityA() == a && pair.getEntityB() == b)
                || (pair.getEntityA() == b && pair.getEntityB() == a),
            "The pair must contain the two registered collidables");
    }

    @Test
    public void testBruteForce_disjointPair_resolveReceivesNoPairs() {
        // two boxes placed far apart with no overlap
        SimpleCollidable a = new SimpleCollidable(0,   0,  10, 10);
        SimpleCollidable b = new SimpleCollidable(100, 100, 10, 10);

        manager.registerCollidable(a);
        manager.registerCollidable(b);
        manager.update(0f);

        assertTrue(manager.receivedPairs.isEmpty(),
            "No overlap should produce an empty collision list");
    }

    @Test
    public void testBruteForce_pairsAreNotDuplicated() {
        // three boxes: a overlaps b, b overlaps c, a does not overlap c
        SimpleCollidable a = new SimpleCollidable(0,  0,  20, 10);
        SimpleCollidable b = new SimpleCollidable(15, 0,  20, 10);
        SimpleCollidable c = new SimpleCollidable(50, 0,  10, 10);

        manager.registerCollidable(a);
        manager.registerCollidable(b);
        manager.registerCollidable(c);
        manager.update(0f);

        // only (a,b) overlaps; verify the pair is recorded exactly once
        assertEquals(1, manager.receivedPairs.size(),
            "Each colliding pair should be reported exactly once");
    }

    @Test
    public void testUnregister_removedCollidableProducesNoPair() {
        SimpleCollidable a = new SimpleCollidable(0, 0, 10, 10);
        SimpleCollidable b = new SimpleCollidable(5, 5, 10, 10);

        manager.registerCollidable(a);
        manager.registerCollidable(b);
        manager.unregisterCollidable(b);
        manager.update(0f);

        assertTrue(manager.receivedPairs.isEmpty(),
            "After unregistering b, no collisions should be detected");
    }

    // --- spatial-tree path (world bounds set) ---

    @Test
    public void testSpatialTree_overlappingPair_resolveReceivesPair() {
        // configure world bounds so the spatial-tree path is used
        manager.setWorldBounds(new float[]{0f, 0f}, new float[]{200f, 200f});

        SimpleCollidable a = new SimpleCollidable(0, 0, 10, 10);
        SimpleCollidable b = new SimpleCollidable(5, 5, 10, 10);

        manager.registerCollidable(a);
        manager.registerCollidable(b);
        manager.update(0f);

        assertEquals(1, manager.receivedPairs.size(),
            "Spatial-tree path should detect the same overlapping pair");
    }

    @Test
    public void testSpatialTree_disjointPair_resolveReceivesNoPairs() {
        manager.setWorldBounds(new float[]{0f, 0f}, new float[]{200f, 200f});

        SimpleCollidable a = new SimpleCollidable(0,   0,  10, 10);
        SimpleCollidable b = new SimpleCollidable(150, 150, 10, 10);

        manager.registerCollidable(a);
        manager.registerCollidable(b);
        manager.update(0f);

        assertTrue(manager.receivedPairs.isEmpty(),
            "Spatial-tree path should produce no pairs for disjoint entities");
    }
}

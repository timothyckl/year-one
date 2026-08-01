package com.p1_7.abstractengine.collision;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates the spatial partitioning logic of the SpatialTree.
 */
public class SpatialTreeTest {

    // --- Dummy Classes for Isolated Testing ---
    
    private static class DummyBounds implements IBounds {
        private final float[] minPos;
        private final float[] extent;
        
        public DummyBounds(float x, float y, float w, float h) {
            this.minPos = new float[]{x, y};
            this.extent = new float[]{w, h};
        }
        
        @Override public boolean overlaps(IBounds other) { return false; } // Not needed for pure tree insertion
        @Override public float[] getMinPosition() { return minPos; }
        @Override public float[] getExtent() { return extent; }
        @Override public void set(float[] minPosition, float[] extent) {}
        @Override public int getDimensions() { return 2; }
    }

    private static class DummyCollidable implements ICollidable {
        private final IBounds bounds;
        
        public DummyCollidable(float x, float y, float w, float h) {
            this.bounds = new DummyBounds(x, y, w, h);
        }
        
        @Override public IBounds getBounds() { return bounds; }
        @Override public void onCollision(ICollidable other) {}
    }

    private SpatialTree tree;

    @BeforeEach
    public void setUp() {
        // Create a 100x100 2D world space for the root node
        tree = new SpatialTree(new float[]{0f, 0f}, new float[]{100f, 100f});
    }

    @Test
    public void testInsertAndRetrieveSingle() {
        DummyCollidable c1 = new DummyCollidable(10, 10, 10, 10);
        tree.insert(c1);

        List<ICollidable> result = new ArrayList<>();
        tree.retrieve(result, c1);

        assertEquals(1, result.size(), "Should retrieve the inserted entity");
        assertTrue(result.contains(c1));
    }

    @Test
    public void testSubdivisionIsolatesEntities() {
        // The default max entities per node is 4. Inserting 5 triggers a subdivision!
        DummyCollidable bottomLeft1 = new DummyCollidable(10, 10, 5, 5); 
        DummyCollidable bottomRight = new DummyCollidable(80, 10, 5, 5); 
        DummyCollidable topLeft     = new DummyCollidable(10, 80, 5, 5); 
        DummyCollidable topRight    = new DummyCollidable(80, 80, 5, 5); 
        DummyCollidable bottomLeft2 = new DummyCollidable(12, 12, 5, 5); 
        
        tree.insert(bottomLeft1);
        tree.insert(bottomRight);
        tree.insert(topLeft);
        tree.insert(topRight);
        tree.insert(bottomLeft2); // This 5th insertion forces the tree to split!

        // Act: Query the space around the bottom-right entity
        List<ICollidable> result = new ArrayList<>();
        tree.retrieve(result, bottomRight);

        // Assert: Because the tree subdivided, the bottom-right quadrant should ONLY contain the bottom-right entity!
        assertEquals(1, result.size(), "Subdivision failed to isolate entities into distinct quadrants");
        assertTrue(result.contains(bottomRight));
    }
    
    @Test
    public void testSpanningEntityRetrieval() {
        // Insert 4 small entities to fill capacity
        tree.insert(new DummyCollidable(10, 10, 5, 5));
        tree.insert(new DummyCollidable(80, 10, 5, 5));
        tree.insert(new DummyCollidable(10, 80, 5, 5));
        tree.insert(new DummyCollidable(80, 80, 5, 5));

        // Insert a giant entity that sits dead center and spans across all 4 quadrants (40,40 to 60,60)
        DummyCollidable giant = new DummyCollidable(40, 40, 20, 20);
        tree.insert(giant); // Forces subdivision, but 'giant' is too big to fit in a child, so it stays in the root

        List<ICollidable> result = new ArrayList<>();
        // Querying with the giant should check ALL children and the root itself
        tree.retrieve(result, giant);
        
        assertEquals(5, result.size(), "A spanning entity should retrieve collision candidates from all overlapped child nodes");
    }
}
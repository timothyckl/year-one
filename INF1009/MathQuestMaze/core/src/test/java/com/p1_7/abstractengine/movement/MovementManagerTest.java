package com.p1_7.abstractengine.movement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.abstractengine.transform.ITransformable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates physics ticks and spatial clamping logic.
 */
public class MovementManagerTest {

    private MovementManager movementManager;

    /**
     * A self-contained dummy object that implements both IMovable and ITransformable
     * strictly for testing boundaries without LibGDX dependencies.
     */
    private static class DummyMovable implements IMovable, ITransformable {
        public float[] pos = {0f, 0f};
        public float[] size = {10f, 10f}; // Entity is 10x10 pixels
        public boolean moveCalled = false;

        private final ITransform transform = new ITransform() {
            @Override public float getPosition(int axis) { return pos[axis]; }
            @Override public void setPosition(int axis, float value) { pos[axis] = value; }
            @Override public float getSize(int axis) { return size[axis]; }
            @Override public void setSize(int axis, float value) { size[axis] = value; }
            @Override public int getDimensions() { return 2; }
        };

        @Override public ITransform getTransform() { return transform; }
        @Override public float[] getAcceleration() { return new float[2]; }
        @Override public void setAcceleration(float[] a) {}
        @Override public float[] getVelocity() { return new float[2]; }
        @Override public void setVelocity(float[] v) {}
        
        @Override 
        public void move(float dt) { 
            moveCalled = true; 
        }
    }

    @BeforeEach
    public void setUp() {
        movementManager = new MovementManager();
    }

    @Test
    public void testMoveIsCalled() {
        DummyMovable movable = new DummyMovable();
        movementManager.registerMovable(movable);

        movementManager.update(1.0f); // Fires a tick

        assertTrue(movable.moveCalled, "The move() method should be called on registered entities");
    }

    @Test
    public void testBoundaryClamping() {
        DummyMovable movable = new DummyMovable();
        movementManager.registerMovable(movable);

        // Set limits to a 100x100 box
        movementManager.setWorldBounds(new float[]{0f, 0f}, new float[]{100f, 100f});
        movementManager.setBoundariesEnabled(true);

        // 1. Test Minimum Bounds (Trying to leave via the bottom-left)
        movable.pos[0] = -50f;
        movable.pos[1] = -50f;
        movementManager.update(1.0f);
        
        assertEquals(0f, movable.pos[0], "X position should be clamped to 0");
        assertEquals(0f, movable.pos[1], "Y position should be clamped to 0");

        // 2. Test Maximum Bounds (Trying to leave via the top-right)
        movable.pos[0] = 150f;
        movable.pos[1] = 150f;
        movementManager.update(1.0f);
        
        // Note: It should clamp to 90 because the entity size is 10 (100 - 10 = 90)
        assertEquals(90f, movable.pos[0], "X position should be clamped to boundsMax - size");
        assertEquals(90f, movable.pos[1], "Y position should be clamped to boundsMax - size");
    }
    
    @Test
    public void testBoundariesDisabled() {
        DummyMovable movable = new DummyMovable();
        movementManager.registerMovable(movable);

        movementManager.setWorldBounds(new float[]{0f, 0f}, new float[]{100f, 100f});
        movementManager.setBoundariesEnabled(false); // Explicitly disable

        movable.pos[0] = -50f;
        movementManager.update(1.0f);
        
        assertEquals(-50f, movable.pos[0], "Position should NOT be clamped when boundaries are disabled");
    }
}
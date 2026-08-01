package com.p1_7.game.spatial;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates Bounds2D AABB overlap logic and accessor round-trips.
 */
public class Bounds2DTest {

    // --- overlaps ---

    @Test
    public void testOverlaps_clearlyOverlapping_returnsTrue() {
        Bounds2D a = new Bounds2D(0, 0, 10, 10);
        Bounds2D b = new Bounds2D(5, 5, 10, 10);
        assertTrue(a.overlaps(b), "Partially overlapping boxes must return true");
        assertTrue(b.overlaps(a), "overlaps must be symmetric");
    }

    @Test
    public void testOverlaps_fullyContained_returnsTrue() {
        Bounds2D outer = new Bounds2D(0, 0, 20, 20);
        Bounds2D inner = new Bounds2D(5, 5,  5,  5);
        assertTrue(outer.overlaps(inner), "A box fully inside another must overlap");
        assertTrue(inner.overlaps(outer), "Containment check must be symmetric");
    }

    @Test
    public void testOverlaps_clearlyDisjoint_returnsFalse() {
        Bounds2D a = new Bounds2D(0, 0, 10, 10);
        Bounds2D b = new Bounds2D(50, 50, 10, 10);
        assertFalse(a.overlaps(b), "Non-touching boxes must not overlap");
        assertFalse(b.overlaps(a), "Non-touching check must be symmetric");
    }

    @Test
    public void testOverlaps_adjacentEdge_returnsFalse() {
        // boxes share an edge (touching but not penetrating) — should NOT overlap
        // a occupies [0,0]–[10,10]; b starts at x=10 → no penetration on x-axis
        Bounds2D a = new Bounds2D(0, 0, 10, 10);
        Bounds2D b = new Bounds2D(10, 0, 10, 10);
        assertFalse(a.overlaps(b),
            "Boxes that only touch at an edge must not be considered overlapping");
    }

    @Test
    public void testOverlaps_partialXAxisOnly_returnsFalse() {
        // overlap on x but fully separated on y
        Bounds2D a = new Bounds2D(0, 0,  10, 10);
        Bounds2D b = new Bounds2D(5, 20, 10, 10);
        assertFalse(a.overlaps(b),
            "Overlap on only one axis must not count as a collision");
    }

    // --- getMinPosition ---

    @Test
    public void testGetMinPosition_returnsCorrectCoordinates() {
        Bounds2D b = new Bounds2D(3f, 7f, 20f, 15f);
        float[] min = b.getMinPosition();
        assertEquals(3f,  min[0], 1e-6f, "X min-position should be 3");
        assertEquals(7f,  min[1], 1e-6f, "Y min-position should be 7");
    }

    @Test
    public void testGetMinPosition_returnsDefensiveCopy() {
        Bounds2D b = new Bounds2D(1f, 2f, 5f, 5f);
        float[] copy = b.getMinPosition();
        copy[0] = 999f;
        // mutating the returned array must not affect the bounds
        assertEquals(1f, b.getMinPosition()[0], 1e-6f,
            "getMinPosition must return a defensive copy");
    }

    // --- getExtent ---

    @Test
    public void testGetExtent_returnsCorrectDimensions() {
        Bounds2D b = new Bounds2D(0f, 0f, 30f, 45f);
        float[] ext = b.getExtent();
        assertEquals(30f, ext[0], 1e-6f, "Width should be 30");
        assertEquals(45f, ext[1], 1e-6f, "Height should be 45");
    }

    @Test
    public void testGetExtent_returnsDefensiveCopy() {
        Bounds2D b = new Bounds2D(0f, 0f, 10f, 10f);
        float[] copy = b.getExtent();
        copy[0] = 999f;
        assertEquals(10f, b.getExtent()[0], 1e-6f,
            "getExtent must return a defensive copy");
    }

    // --- set ---

    @Test
    public void testSet_updatesPositionAndExtent() {
        Bounds2D b = new Bounds2D(0f, 0f, 10f, 10f);
        b.set(new float[]{5f, 8f}, new float[]{20f, 30f});

        assertArrayEquals(new float[]{5f, 8f},  b.getMinPosition(), 1e-6f);
        assertArrayEquals(new float[]{20f, 30f}, b.getExtent(),      1e-6f);
    }

    // --- getDimensions ---

    @Test
    public void testGetDimensions_returnsTwo() {
        assertEquals(2, new Bounds2D(0, 0, 1, 1).getDimensions());
    }
}

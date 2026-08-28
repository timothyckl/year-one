package com.p1_7.abstractengine.collision;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Automated Unit Tests for the CollisionDetector.
 * Demonstrates the use of Mockito to simulate interface behaviors without 
 * relying on concrete implementations like Rectangle2D.
 */
public class CollisionSystemTest {

    private CollisionDetector detector;

    @BeforeEach
    public void setUp() {
        detector = new CollisionDetector();
    }

    @Test
    public void testCheckCollision_OverlappingBounds() {
        // Arrange: Mock the interfaces
        ICollidable entityA = mock(ICollidable.class);
        ICollidable entityB = mock(ICollidable.class);
        IBounds boundsA = mock(IBounds.class);
        IBounds boundsB = mock(IBounds.class);

        // Tell the mocked entities to return our mocked bounds
        when(entityA.getBounds()).thenReturn(boundsA);
        when(entityB.getBounds()).thenReturn(boundsB);
        
        // Simulate that boundsA overlapping with boundsB returns TRUE
        when(boundsA.overlaps(boundsB)).thenReturn(true);

        // Act
        boolean result = detector.checkCollision(entityA, entityB);

        // Assert
        assertTrue(result, "Detector should return true when the underlying bounds overlap");
        verify(boundsA).overlaps(boundsB); // Verify the abstract method was actually invoked
    }

    @Test
    public void testCheckCollision_NoOverlap() {
        // Arrange: Mock the interfaces
        ICollidable entityA = mock(ICollidable.class);
        ICollidable entityB = mock(ICollidable.class);
        IBounds boundsA = mock(IBounds.class);
        IBounds boundsB = mock(IBounds.class);

        when(entityA.getBounds()).thenReturn(boundsA);
        when(entityB.getBounds()).thenReturn(boundsB);
        
        // Simulate that boundsA overlapping with boundsB returns FALSE
        when(boundsA.overlaps(boundsB)).thenReturn(false);

        // Act
        boolean result = detector.checkCollision(entityA, entityB);

        // Assert
        assertFalse(result, "Detector should return false when bounds do not overlap");
    }
}
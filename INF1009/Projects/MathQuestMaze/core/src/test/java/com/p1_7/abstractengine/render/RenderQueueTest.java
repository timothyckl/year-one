package com.p1_7.abstractengine.render;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates the RenderQueue's ability to store and flush render items 
 * safely during the game loop.
 */
public class RenderQueueTest {

    private RenderQueue renderQueue;

    @BeforeEach
    public void setUp() {
        renderQueue = new RenderQueue();
    }

    @Test
    public void testQueueAndRetrieveItems() {
        // Arrange: Create mock renderables
        IRenderable item1 = Mockito.mock(IRenderable.class);
        IRenderable item2 = Mockito.mock(IRenderable.class);

        // Act
        renderQueue.queue(item1);
        renderQueue.queue(item2);

        // Convert iterable to list for easy assertion
        List<IRenderable> items = new ArrayList<>();
        renderQueue.items().forEach(items::add);

        // Assert
        assertEquals(2, items.size(), "Queue should contain 2 items");
        assertTrue(items.contains(item1));
        assertTrue(items.contains(item2));
    }

    @Test
    public void testClearQueue() {
        // Arrange
        IRenderable item = Mockito.mock(IRenderable.class);
        renderQueue.queue(item);
        
        // Act
        renderQueue.clear();
        
        // Assert
        List<IRenderable> items = new ArrayList<>();
        renderQueue.items().forEach(items::add);
        
        assertTrue(items.isEmpty(), "Queue should be totally empty after clear() is called");
    }
}
package com.p1_7.game.core;

import com.p1_7.game.spatial.Transform2D;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Transform2DTest {

    @Test
    public void testInitialization() {
        // Use the standard 4-argument constructor: (x, y, width, height)
        Transform2D transform = new Transform2D(10f, 20f, 100f, 100f);
        
        // Assert positions (axis 0 = X, axis 1 = Y)
        assertEquals(10f, transform.getPosition(0), "Initial X should be 10");
        assertEquals(20f, transform.getPosition(1), "Initial Y should be 20");
        
        // Assert sizes
        assertEquals(100f, transform.getSize(0), "Initial Width should be 100");
        assertEquals(100f, transform.getSize(1), "Initial Height should be 100");
    }

    @Test
    public void testSetPosition() {
        Transform2D transform = new Transform2D(0f, 0f, 50f, 50f);
        
        // Update positions using the ITransform interface standard
        transform.setPosition(0, 150.5f); // Set X
        transform.setPosition(1, -42.0f); // Set Y
        
        assertEquals(150.5f, transform.getPosition(0), "X position did not update correctly");
        assertEquals(-42.0f, transform.getPosition(1), "Y position did not update correctly");
    }
    
    @Test
    public void testSetSize() {
        Transform2D transform = new Transform2D(0f, 0f, 50f, 50f);
        
        // Update sizes
        transform.setSize(0, 200f); // Set Width
        transform.setSize(1, 150f); // Set Height
        
        assertEquals(200f, transform.getSize(0), "Width did not update correctly");
        assertEquals(150f, transform.getSize(1), "Height did not update correctly");
    }
}
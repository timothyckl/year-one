package com.p1_7.abstractengine.scene;

import org.junit.jupiter.api.Test;
import com.p1_7.abstractengine.render.IRenderQueue;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates the base logic of the abstract Scene class,
 * specifically testing state variables like 'name' and 'isPaused'.
 */
public class SceneTest {

    // Create a minimal concrete implementation to test the abstract base
    private static class DummyScene extends Scene {
        public DummyScene() {
            this.name = "dummy-scene";
        }
        
        @Override public void onEnter(SceneContext context) {}
        @Override public void onExit(SceneContext context) {}
        @Override public void onSuspend(SceneContext context) {}
        @Override public void onResume(SceneContext context) {}
        @Override public void update(float dt, SceneContext context) {}
        @Override public void submitRenderable(IRenderQueue queue) {}
    }

    @Test
    public void testSceneName() {
        DummyScene scene = new DummyScene();
        assertEquals("dummy-scene", scene.getName(), "Scene should return the correct assigned name");
    }

    @Test
    public void testPauseState() {
        DummyScene scene = new DummyScene();

        // Assert: Scenes should run by default
        assertFalse(scene.isPaused(), "Scenes must not be paused by default");

        // Act & Assert
        scene.setPaused(true);
        assertTrue(scene.isPaused(), "Scene should report paused after being set");

        scene.setPaused(false);
        assertFalse(scene.isPaused(), "Scene should report unpaused after being set");
    }
}
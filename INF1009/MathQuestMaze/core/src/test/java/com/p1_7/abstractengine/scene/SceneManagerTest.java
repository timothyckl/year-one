package com.p1_7.abstractengine.scene;

import com.p1_7.abstractengine.engine.ManagerResolver;
import com.p1_7.abstractengine.entity.EntityManager;
import com.p1_7.abstractengine.input.InputManager;
import com.p1_7.abstractengine.render.IRenderQueue;
import com.p1_7.abstractengine.render.RenderManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Validates the SceneManager's lifecycle transitions.
 * Specifically tests the "Hard Transition" (changeScene) and 
 * "Deferred Suspend Transition" (suspendScene) logic.
 */
public class SceneManagerTest {

    private SceneManager sceneManager;
    private ManagerResolver mockResolver;

    /**
     * A lightweight dummy scene that tracks how many times its lifecycle hooks are called.
     */
    private static class TrackingScene extends Scene {
        int enterCount = 0;
        int exitCount = 0;
        int suspendCount = 0;
        int resumeCount = 0;
        int updateCount = 0;

        TrackingScene(String name) {
            this.name = name;
        }

        @Override public void onEnter(SceneContext context) { enterCount++; }
        @Override public void onExit(SceneContext context) { exitCount++; }
        @Override public void onSuspend(SceneContext context) { suspendCount++; }
        @Override public void onResume(SceneContext context) { resumeCount++; }
        @Override public void update(float dt, SceneContext context) { updateCount++; }
        @Override public void submitRenderable(IRenderQueue queue) {}
    }

    @BeforeEach
    public void setUp() {
        sceneManager = new SceneManager();

        // 1. Mock the heavy Engine dependencies so we can test SceneManager in isolation
        mockResolver = mock(ManagerResolver.class);
        when(mockResolver.resolve(EntityManager.class)).thenReturn(mock(EntityManager.class));
        when(mockResolver.resolve(InputManager.class)).thenReturn(mock(InputManager.class));
        
        RenderManager mockRenderManager = mock(RenderManager.class);
        when(mockRenderManager.getRenderQueue()).thenReturn(mock(IRenderQueue.class));
        when(mockResolver.resolve(RenderManager.class)).thenReturn(mockRenderManager);

        // 2. Wire the manager just like the real Engine would
        sceneManager.onWire(mockResolver);
    }

    @Test
    public void testHardTransition_ChangeScene() {
        // Arrange
        TrackingScene menu = new TrackingScene("menu");
        TrackingScene game = new TrackingScene("game");
        sceneManager.registerScene(menu);
        sceneManager.registerScene(game);
        sceneManager.setInitialScene("menu");

        // Act: Boot up the manager
        sceneManager.init(); 
        
        // Assert: Menu should be entered
        assertEquals(1, menu.enterCount, "Initial scene should have onEnter called during init");

        // Act: Request a hard change to 'game' and step the engine forward
        sceneManager.requestChange("game");
        sceneManager.update(1.0f); // Triggers onUpdate()

        // Assert: Menu exits, Game enters
        assertEquals(1, menu.exitCount, "Old scene should exit on a hard transition");
        assertEquals(1, game.enterCount, "New scene should enter on a hard transition");
        assertEquals(game, sceneManager.getCurrentScene(), "Active scene should be 'game'");
    }

    @Test
    public void testDeferredTransition_Suspend() {
        // Arrange
        TrackingScene game = new TrackingScene("game");
        TrackingScene settings = new TrackingScene("settings");
        sceneManager.registerScene(game);
        sceneManager.registerScene(settings);
        sceneManager.setInitialScene("game");
        sceneManager.init();

        // Act: Suspend 'game' to open 'settings'
        sceneManager.requestSuspend("settings");
        sceneManager.update(1.0f);

        // Assert: Game suspends (does NOT exit), Settings enters
        assertEquals(1, game.suspendCount, "Original scene should suspend");
        assertEquals(0, game.exitCount, "Original scene should NOT exit during a suspend transition");
        assertEquals(1, settings.enterCount, "Settings scene should enter");
        assertEquals(settings, sceneManager.getCurrentScene(), "Active scene should transition to 'settings'");
    }

    @Test
    public void testUpdateNotCalledWhenPaused() {
        TrackingScene menu = new TrackingScene("menu");
        sceneManager.registerScene(menu);
        sceneManager.setInitialScene("menu");
        sceneManager.init();

        // Act: Normal update
        sceneManager.update(1.0f);
        assertEquals(1, menu.updateCount, "Update should fire when not paused");

        // Act: Paused update
        menu.setPaused(true);
        sceneManager.update(1.0f);
        assertEquals(1, menu.updateCount, "Update count should remain 1 because the scene is paused");
    }

    @Test
    public void testResumeTransition_callsOnResumeNotOnEnter() {
        // Arrange: game → suspend → settings → change back to game should call onResume on game
        TrackingScene game     = new TrackingScene("game");
        TrackingScene settings = new TrackingScene("settings");
        sceneManager.registerScene(game);
        sceneManager.registerScene(settings);
        sceneManager.setInitialScene("game");
        sceneManager.init();

        // Act: suspend game, open settings
        sceneManager.requestSuspend("settings");
        sceneManager.update(1.0f);

        assertEquals(1, game.suspendCount,    "game should have been suspended");
        assertEquals(1, settings.enterCount,  "settings should have entered");

        // Act: change back to game (the suspended scene) — should trigger onResume
        sceneManager.requestChange("game");
        sceneManager.update(1.0f);

        // Assert: game is resumed, not re-entered; settings is exited
        assertEquals(1, game.resumeCount,
            "Changing back to the suspended scene must call onResume");
        assertEquals(1, game.enterCount,
            "onEnter must NOT be called again on a resume — it was only called during init");
        assertEquals(1, settings.exitCount,
            "The overlay scene must exit when the suspended scene is resumed");
        assertEquals(game, sceneManager.getCurrentScene(),
            "game must be the active scene after resume");
    }
}
package com.p1_7.game.managers;

import com.p1_7.game.audio.AudioManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates the architectural state of the AudioManager.
 * Ensures it behaves correctly as an instantiable engine Manager.
 */
public class AudioManagerTest {

    private AudioManager manager;

    @BeforeEach
    public void setUp() {
        // Mock the Gdx.app logger so that "track not found" warnings 
        // don't crash our headless CI pipeline!
        Gdx.app = Mockito.mock(Application.class);
        
        manager = new AudioManager();
    }

    @Test
    public void testInstantiation() {
        AudioManager manager2 = new AudioManager();

        // Assert: The Singleton pattern was removed. It is now the Engine's responsibility
        // to instantiate it once and pass it around via Dependency Injection.
        assertNotNull(manager, "AudioManager instance should not be null");
        assertNotSame(manager, manager2, "AudioManager must be independently instantiable");
    }

    @Test
    public void testSafeShutdownWithoutCrashes() {
        // Assert that calling shutdown on an empty cache doesn't throw NullPointerExceptions
        assertDoesNotThrow(() -> {
            manager.shutdown(); // Calls the inherited Manager.shutdown() which triggers onShutdown()
        }, "Shutting down an empty AudioManager should be completely safe");
    }
    
    @Test
    public void testSetMusicVolumeDoesNotCrashWhenEmpty() {
        // Assert that setting the volume when no music is playing doesn't crash
        assertDoesNotThrow(() -> {
            manager.setMusicVolume(0.5f);
        }, "Setting volume with no active track should be ignored safely");
    }

    @Test
    public void testPlaybackControlsDoNotCrashWhenEmpty() {
        // Assert that invoking playback commands on an empty cache doesn't crash the game.
        // Because Gdx.app is mocked, the "track not found" warning will safely do nothing!
        assertDoesNotThrow(() -> {
            manager.playMusic("non_existent_track", true);
            manager.pauseMusic();  
            manager.resumeMusic(); 
        }, "Playing, pausing, or resuming missing/empty music should be handled safely by the manager");
    }

    @Test
    public void testLoadMusicReachesLibGDX() {
        // CLEVER TRICK: Even though Gdx.app is mocked, Gdx.audio is STILL null.
        // If the manager is correctly wired up, calling loadMusic WILL throw a NullPointerException
        // when it tries to touch Gdx.audio.newMusic(). We assert this Exception to definitively 
        // prove the method successfully reaches the LibGDX framework layer!
        assertThrows(NullPointerException.class, () -> {
            manager.loadMusic("test_track", "dummy/path.mp3");
        }, "loadMusic should attempt to access Gdx.audio and throw an NPE in the headless CI pipeline");
    }
}
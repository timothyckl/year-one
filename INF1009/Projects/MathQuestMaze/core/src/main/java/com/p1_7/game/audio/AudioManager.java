package com.p1_7.game.audio;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.TimeUtils;
import com.p1_7.abstractengine.engine.Manager;
import com.p1_7.game.Settings;

import java.util.HashMap;
import java.util.Map;

/**
 * game-level manager for music and sound effect playback.
 *
 * integrates with the engine lifecycle via Manager; assets are loaded
 * explicitly after engine initialisation and disposed automatically on shutdown.
 * volume changes are applied directly via setMusicVolume(float) rather
 * than being polled each frame.
 */
public class AudioManager extends Manager implements IAudioManager {

    /** gdx audio backend, stored to avoid repeated static field access */
    private final Audio audio = Gdx.audio;

    /** gdx file system backend, stored to avoid repeated static field access */
    private final Files files = Gdx.files;

    /** cached music tracks, keyed by caller-supplied name */
    private final Map<String, Music> musicCache = new HashMap<>();

    /** cached sound effects, keyed by caller-supplied name */
    private final Map<String, Sound> soundCache = new HashMap<>();

    /** last playback time per sound key, used for lightweight SFX throttling */
    private final Map<String, Long> soundLastPlayedAt = new HashMap<>();

    /** the currently active music track, or null if none is playing */
    private Music currentMusic;

    /** the key of the currently active music track, or null if none is playing */
    private String currentMusicKey;

    /**
     * loads game assets into the cache on engine init so they are ready
     * before any scene requests playback.
     */
    @Override
    protected void onInit() {
        loadMusic("menu",           "music/menu.mp3");
        loadMusic("game",           "music/background.mp3");
        loadMusic("game-over",      "music/game-over.mp3");
        loadMusic("level-complete", "music/level-complete.mp3");

        loadSound("answer", "sound/sfx_answer.ogg");
        loadSound("wrong",  "sound/sfx_wrong.ogg");
        loadSound("heart",  "sound/sfx_heart.ogg");
        loadSound("hurt",   "sound/sfx_hurt.ogg");
        loadSound("die",    "sound/sfx_die.ogg");
        loadSound("select", "sound/sfx_select.ogg");
    }

    /**
     * pauses the currently playing music track, if any.
     */
    public void pauseMusic() {
        if (currentMusic != null) {
            currentMusic.pause();
        }
    }

    /**
     * resumes the currently playing music track, reapplying the stored volume.
     * if no track is active this is a no-op.
     */
    public void resumeMusic() {
        if (currentMusic != null) {
            currentMusic.setVolume(Settings.getMusicVolume());
            currentMusic.play();
        }
    }

    /**
     * loads a music track into the cache under the given key.
     * if the key is already cached, the call is a no-op.
     *
     * @param key      the name to associate with the track
     * @param filePath the internal asset path
     */
    public void loadMusic(String key, String filePath) {
        if (!musicCache.containsKey(key)) {
            musicCache.put(key, audio.newMusic(files.internal(filePath)));
        } else {
            Gdx.app.log("AudioManager", "loadMusic: key '" + key + "' already loaded, ignoring");
        }
    }

    /**
     * loads a sound effect into the cache under the given key.
     * if the key is already cached, the call is a no-op.
     *
     * @param key      the name to associate with the sound
     * @param filePath the internal asset path
     */
    public void loadSound(String key, String filePath) {
        if (!soundCache.containsKey(key)) {
            soundCache.put(key, audio.newSound(files.internal(filePath)));
        } else {
            Gdx.app.log("AudioManager", "loadSound: key '" + key + "' already loaded, ignoring");
        }
    }

    /**
     * plays a cached music track, stopping any currently playing track first.
     * if the requested track is already playing, the call is a no-op.
     *
     * @param key  the name of the track to play
     * @param loop whether the track should loop continuously
     */
    public void playMusic(String key, boolean loop) {
        if (key == null) {
            throw new IllegalArgumentException("music key must not be null");
        }
        if (key.equals(currentMusicKey)) {
            return;
        }

        if (currentMusic != null) {
            currentMusic.stop();
            // clear state before the cache lookup so a cache miss does not
            // leave stale references pointing at the stopped track
            currentMusic = null;
            currentMusicKey = null;
        }

        Music next = musicCache.get(key);
        if (next != null) {
            currentMusic = next;
            currentMusicKey = key;
            currentMusic.setLooping(loop);
            currentMusic.setVolume(Settings.getMusicVolume());
            currentMusic.play();
        } else {
            Gdx.app.log("AudioManager", "playMusic: key '" + key + "' not found in cache, ignoring");
        }
    }

    /**
     * plays a cached sound effect once at the current SFX volume.
     *
     * @param key the name of the sound to play
     */
    public void playSound(String key) {
        playSound(key, 0L);
    }

    /**
     * plays a cached sound effect once at the current SFX volume, optionally throttled by key.
     *
     * @param key the name of the sound to play
     * @param minIntervalMs the minimum time in milliseconds between plays of the same key
     */
    public void playSound(String key, long minIntervalMs) {
        if (key == null) {
            throw new IllegalArgumentException("sound key must not be null");
        }
        long now = TimeUtils.millis();
        Long lastPlayedAt = soundLastPlayedAt.get(key);
        if (minIntervalMs > 0L && lastPlayedAt != null && now - lastPlayedAt < minIntervalMs) {
            return;
        }
        Sound sound = soundCache.get(key);
        if (sound != null) {
            sound.play(Settings.getSfxVolume());
            soundLastPlayedAt.put(key, now);
        } else {
            Gdx.app.log("AudioManager", "playSound: key '" + key + "' not found in cache, ignoring");
        }
    }

    /**
     * clamps the given volume via Settings.setMusicVolume(float),
     * and applies it to the currently playing music track.
     *
     * @param volume the desired volume level (0.0 = silent, 1.0 = maximum)
     */
    public void setMusicVolume(float volume) {
        Settings.setMusicVolume(volume);
        if (currentMusic != null) {
            currentMusic.setVolume(Settings.getMusicVolume());
        }
    }

    /**
     * clamps the given SFX volume via Settings.setSfxVolume(float).
     *
     * @param volume the desired volume level (0.0 = silent, 1.0 = maximum)
     */
    public void setSfxVolume(float volume) {
        Settings.setSfxVolume(volume);
    }

    /**
     * returns the current music volume as stored in settings.
     *
     * @return the current volume level in the range [0.0, 1.0]
     */
    public float getMusicVolume() {
        return Settings.getMusicVolume();
    }

    /**
     * returns the current SFX volume as stored in settings.
     *
     * @return the current volume level in the range [0.0, 1.0]
     */
    public float getSfxVolume() {
        return Settings.getSfxVolume();
    }

    /**
     * disposes all loaded audio resources and clears the caches.
     */
    @Override
    protected void onShutdown() {
        // Music.dispose() implicitly stops; no separate stop call needed
        for (Music music : musicCache.values()) {
            music.dispose();
        }
        for (Sound sound : soundCache.values()) {
            sound.dispose();
        }
        musicCache.clear();
        soundCache.clear();
        soundLastPlayedAt.clear();
        currentMusic = null;
        currentMusicKey = null;
    }
}

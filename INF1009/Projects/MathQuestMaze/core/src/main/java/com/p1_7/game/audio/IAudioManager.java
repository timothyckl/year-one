package com.p1_7.game.audio;

/**
 * Contract for game-level audio management.
 *
 * implementations handle loading and playback of music tracks and
 * sound effects, and expose volume control for the active track.
 */
public interface IAudioManager {

    /**
     * loads a music track into the cache under the given key.
     * if the key is already cached, the call is a no-op.
     *
     * @param key      the name to associate with the track
     * @param filePath the internal asset path
     */
    void loadMusic(String key, String filePath);

    /**
     * loads a sound effect into the cache under the given key.
     * if the key is already cached, the call is a no-op.
     *
     * @param key      the name to associate with the sound
     * @param filePath the internal asset path
     */
    void loadSound(String key, String filePath);

    /**
     * plays a cached music track, stopping any currently playing track first.
     * if the requested track is already playing, the call is a no-op.
     *
     * @param key  the name of the track to play
     * @param loop whether the track should loop continuously
     */
    void playMusic(String key, boolean loop);

    /**
     * plays a cached sound effect once.
     *
     * @param key the name of the sound to play
     */
    void playSound(String key);

    /**
     * plays a cached sound effect once unless it was played too recently.
     *
     * @param key the name of the sound to play
     * @param minIntervalMs the minimum time in milliseconds between plays of the same key
     */
    void playSound(String key, long minIntervalMs);

    /**
     * sets the volume on the currently playing music track.
     * the value is clamped to [0.0, 1.0] before being applied.
     * as a side-effect, the clamped value is persisted via Settings.setMusicVolume(float)
     * so that any track started later inherits the same volume.
     *
     * @param volume the desired volume level (0.0 = silent, 1.0 = maximum)
     */
    void setMusicVolume(float volume);

    /**
     * sets the volume used for sound effects.
     * the value is clamped to [0.0, 1.0] before being persisted.
     *
     * @param volume the desired SFX volume level (0.0 = silent, 1.0 = maximum)
     */
    void setSfxVolume(float volume);

    /**
     * returns the current music volume as stored in settings.
     *
     * @return the current volume level in the range [0.0, 1.0]
     */
    float getMusicVolume();

    /**
     * returns the current SFX volume as stored in settings.
     *
     * @return the current SFX volume level in the range [0.0, 1.0]
     */
    float getSfxVolume();

    /**
     * pauses the currently playing music track.
     * if no track is active this is a no-op.
     */
    void pauseMusic();

    /**
     * resumes the currently playing music track, reapplying the stored volume.
     * if no track is active this is a no-op.
     */
    void resumeMusic();
}

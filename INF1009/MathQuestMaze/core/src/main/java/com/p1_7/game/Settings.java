package com.p1_7.game;

/**
 * Game application configuration values.
 *
 * All mutable fields are private; callers must use the provided getters
 * and setters rather than accessing fields directly. The default values
 * mirror those set in Lwjgl3Launcher; if the launcher changes the window
 * size the game should call setResolution(int, int) accordingly.
 */
public class Settings {

    /** lowest supported brightness level; used to avoid a fully black screen */
    public static final float MIN_BRIGHTNESS_LEVEL = 0.1f;

    /** default brightness level used on startup */
    public static final float DEFAULT_BRIGHTNESS_LEVEL = 0.9f;

    /**
     * Preset resolution options available to the player.
     *
     * Each inner array is a {width, height} pair in pixels.
     * These map directly to the integer arguments accepted by
     * setResolution(int, int), avoiding string parsing at the call site.
     */
    private static final int[][] RESOLUTIONS = {
        {800,  600},
        {1280, 720},
        {1600, 900},
        {1920, 1080}
    };

    /**
     * Returns a defensive copy of the preset resolution table.
     *
     * Each element is a two-element {width, height} pair in pixels.
     * The returned array is independent of the internal table; callers may
     * modify it freely without affecting the canonical values.
     *
     * @return a new int[][] containing the preset {width, height} pairs
     */
    public static int[][] getResolutions() {
        int[][] copy = new int[RESOLUTIONS.length][];
        for (int i = 0; i < RESOLUTIONS.length; i++) {
            copy[i] = RESOLUTIONS[i].clone();
        }
        return copy;
    }

    private static int   windowWidth     = 1280;
    private static int   windowHeight    = 720;
    private static float musicVolume     = 0.5f;
    private static float sfxVolume       = 0.5f;
    private static float brightnessLevel = DEFAULT_BRIGHTNESS_LEVEL;

    /** returns the window width in pixels */
    public static int getWindowWidth() { return windowWidth; }

    /** returns the window height in pixels */
    public static int getWindowHeight() { return windowHeight; }

    /** returns the music volume in the range [0.0, 1.0] */
    public static float getMusicVolume() { return musicVolume; }

    /** returns the SFX volume in the range [0.0, 1.0] */
    public static float getSfxVolume() { return sfxVolume; }

    /** returns the screen brightness level in the range [0.1, 1.0] */
    public static float getBrightnessLevel() { return brightnessLevel; }

    /**
     * Sets the music volume, clamped to the valid range [0.0, 1.0].
     *
     * @param volume the desired volume level; values outside [0.0, 1.0] are clamped
     */
    public static void setMusicVolume(float volume) {
        musicVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }

    /**
     * Sets the SFX volume, clamped to the valid range [0.0, 1.0].
     *
     * @param volume the desired volume level; values outside [0.0, 1.0] are clamped
     */
    public static void setSfxVolume(float volume) {
        sfxVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }

    /**
     * Sets the screen brightness level, clamped to the valid range [0.1, 1.0].
     *
     * @param level the desired brightness level; values outside [0.1, 1.0] are clamped
     */
    public static void setBrightnessLevel(float level) {
        brightnessLevel = Math.max(MIN_BRIGHTNESS_LEVEL, Math.min(1.0f, level));
    }

    /**
     * Sets the window resolution by updating windowWidth and windowHeight.
     *
     * This method only updates the stored values. The caller is responsible for
     * applying the change to the platform window (e.g. via the libGDX graphics backend).
     *
     * @param width  the desired window width in pixels
     * @param height the desired window height in pixels
     */
    public static void setResolution(int width, int height) {
        windowWidth  = width;
        windowHeight = height;
    }
}

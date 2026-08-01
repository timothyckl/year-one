package com.p1_7.game;

/**
 * named constants for gameplay tuning values that were previously scattered
 * as inline literals across GameScene and its collaborators.
 */
public final class GameConfig {

    // phase hold timers ───────────────────────────────────────────────

    /** hold time for QUESTION_INTRO — matches panel animation total (1.5 s + 1.0 s) */
    public static final float QUESTION_INTRO_HOLD_SECONDS = 2.5f;

    /** hold time for the FEEDBACK phase — long enough for the player to read the result */
    public static final float FEEDBACK_HOLD_SECONDS = 2.0f;

    /** hold time for ROUND_RESET before the next question begins */
    public static final float ROUND_RESET_HOLD_SECONDS = 1.0f;

    // room entry ──────────────────────────────────────────────────────

    /** re-entry cooldown in seconds before a wrong room can penalise the player again */
    public static final float ROOM_COOLDOWN_SECONDS = 1.0f;

    /** width of the centre trigger zone within each answer room in pixels */
    public static final float ROOM_TRIGGER_WIDTH = 30f;

    /** height of the centre trigger zone within each answer room in pixels */
    public static final float ROOM_TRIGGER_HEIGHT = 30f;

    // item spawning ───────────────────────────────────────────────────

    /** minimum number of hearts spawned per level */
    public static final int HEART_SPAWN_MIN = 1;

    /** exclusive upper bound for heart count (produces 1–3 hearts) */
    public static final int HEART_SPAWN_MAX = 4;

    /** maximum placement attempts per heart before giving up */
    public static final int HEART_SPAWN_ATTEMPTS = 24;

    // debug ───────────────────────────────────────────────────────────

    /** set to true to draw hitbox outlines over all entities and trigger zones */
    public static final boolean DEBUG_HITBOXES = false;

    private GameConfig() {
        // prevent instantiation
    }
}

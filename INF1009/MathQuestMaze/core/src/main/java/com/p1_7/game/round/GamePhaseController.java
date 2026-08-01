package com.p1_7.game.round;

import java.util.Arrays;

import com.p1_7.abstractengine.collision.IBounds;
import com.p1_7.game.GameConfig;
import com.p1_7.game.character.Player;

/**
 * owns the round-phase state machine, non-interactive hold timers, and
 * answer-room entry detection.
 *
 * GameScene delegates to this controller each frame. scene-level concerns
 * (changing scenes, freezing enemies) remain in GameScene; this class handles
 * only phase tracking and room-choice submission.
 */
public class GamePhaseController {

    /** phase recorded at the end of the previous update; used to detect transitions */
    private RoundPhase lastKnownPhase;

    /** countdown timer used to auto-advance through non-interactive phases */
    private float phaseHoldTimer;

    /** tracks whether the player is currently overlapping each answer room */
    private final boolean[] playerInsideRoom = new boolean[4];

    /** per-room countdown timer; blocks re-entry while greater than zero */
    private final float[] roomCooldownTimers = new float[4];

    /**
     * detects whether the phase has changed since the last call and, if so,
     * notifies the listener and sets the appropriate hold timer.
     *
     * @param currentPhase the phase returned by the orchestrator this frame
     * @param orchestrator the level orchestrator
     * @param listener     callback to notify on phase transitions
     */
    public void detectPhaseChange(RoundPhase currentPhase,
                                  ILevelOrchestrator orchestrator,
                                  GamePhaseListener listener) {
        if (currentPhase != lastKnownPhase) {
            listener.onPhaseChanged(lastKnownPhase, currentPhase, orchestrator);
            applyHoldTimer(currentPhase);
            lastKnownPhase = currentPhase;
        }
    }

    /**
     * ticks the hold timer down by the elapsed time.
     *
     * @param deltaTime seconds since the last frame
     * @return true when the timer has expired (less than or equal to zero)
     */
    public boolean tickHoldTimer(float deltaTime) {
        phaseHoldTimer -= deltaTime;
        return phaseHoldTimer <= 0f;
    }

    /**
     * checks each answer room for player overlap. on first entry fires
     * submitRoomChoice() via the orchestrator; on exit starts the re-entry cooldown.
     *
     * when the wrong room is entered, playerInsideRoom[i] stays true until
     * the player physically exits, preventing the cooldown from triggering
     * a second penalty without leaving first.
     *
     * @param deltaTime       seconds elapsed since the previous frame
     * @param player          the player entity
     * @param cachedRoomBounds pre-cached [x, y, w, h] arrays for the four rooms
     * @param orchestrator    the level orchestrator used to submit room choices
     */
    public void checkRoomEntry(float deltaTime, Player player,
                               float[][] cachedRoomBounds,
                               ILevelOrchestrator orchestrator) {
        IBounds playerBounds = player.getBounds();

        for (int i = 0; i < playerInsideRoom.length; i++) {
            // tick the cooldown down; clamp to zero
            if (roomCooldownTimers[i] > 0f) {
                roomCooldownTimers[i] = Math.max(0f, roomCooldownTimers[i] - deltaTime);
            }

            boolean overlapping = overlapsRoom(playerBounds, cachedRoomBounds[i]);

            if (overlapping && !playerInsideRoom[i] && roomCooldownTimers[i] <= 0f) {
                // player just entered a room with no active cooldown
                playerInsideRoom[i] = true;
                int healthBefore = orchestrator.getHealth();
                orchestrator.submitRoomChoice(i);
                if (orchestrator.getHealth() < healthBefore) {
                    player.triggerDamageAnimation();
                }
                // phase has changed after submitRoomChoice — stop checking further rooms
                break;
            } else if (!overlapping && playerInsideRoom[i]) {
                // player just exited — start cooldown so re-entry does not instant-trigger
                playerInsideRoom[i] = false;
                roomCooldownTimers[i] = GameConfig.ROOM_COOLDOWN_SECONDS;
            }
        }
    }

    /**
     * clears per-room overlap flags and cooldown timers.
     * called on ROUND_RESET so the next question starts with a clean slate.
     */
    public void resetRoomState() {
        Arrays.fill(playerInsideRoom, false);
        Arrays.fill(roomCooldownTimers, 0f);
    }

    /**
     * forces the last-known phase to the given value.
     * used during scene initialisation so the first update tick does not fire
     * a spurious onPhaseChanged for the initial QUESTION_INTRO.
     *
     * @param phase the phase to record
     */
    public void setLastKnownPhase(RoundPhase phase) {
        this.lastKnownPhase = phase;
    }

    /**
     * forces the hold timer to the given value.
     * used during scene initialisation for the initial QUESTION_INTRO hold.
     *
     * @param seconds the hold duration in seconds
     */
    public void setHoldTimer(float seconds) {
        this.phaseHoldTimer = seconds;
    }

    /**
     * returns true for phases that have no valid advance() transition.
     *
     * @param phase the phase to test
     * @return true if the phase is LEVEL_COMPLETE or GAME_OVER
     */
    public static boolean isTerminalPhase(RoundPhase phase) {
        return phase == RoundPhase.LEVEL_COMPLETE || phase == RoundPhase.GAME_OVER;
    }

    // private helpers ─────────────────────────────────────────────────

    /**
     * sets the hold timer based on the incoming phase.
     * terminal phases share the feedback hold so the overlay is visible before transitioning.
     */
    private void applyHoldTimer(RoundPhase phase) {
        if (phase == RoundPhase.CHOOSING) {
            return;
        }
        if (phase == RoundPhase.FEEDBACK || isTerminalPhase(phase)) {
            phaseHoldTimer = GameConfig.FEEDBACK_HOLD_SECONDS;
        } else if (phase == RoundPhase.QUESTION_INTRO) {
            phaseHoldTimer = GameConfig.QUESTION_INTRO_HOLD_SECONDS;
        } else {
            phaseHoldTimer = GameConfig.ROUND_RESET_HOLD_SECONDS;
        }
    }

    /**
     * returns true if the player's AABB overlaps the centre trigger zone of the room.
     *
     * @param playerBounds the player's current AABB
     * @param room         a four-element [x, y, w, h] room rectangle
     * @return true if the two AABBs overlap on both axes
     */
    private static boolean overlapsRoom(IBounds playerBounds, float[] room) {
        float triggerX = room[0] + (room[2] - GameConfig.ROOM_TRIGGER_WIDTH)  / 2f;
        float triggerY = room[1] + (room[3] - GameConfig.ROOM_TRIGGER_HEIGHT) / 2f;

        float[] pMin = playerBounds.getMinPosition();
        float[] pExt = playerBounds.getExtent();
        float pMaxX = pMin[0] + pExt[0];
        float pMaxY = pMin[1] + pExt[1];
        return pMaxX > triggerX                              && pMin[0] < triggerX + GameConfig.ROOM_TRIGGER_WIDTH
            && pMaxY > triggerY                              && pMin[1] < triggerY + GameConfig.ROOM_TRIGGER_HEIGHT;
    }
}

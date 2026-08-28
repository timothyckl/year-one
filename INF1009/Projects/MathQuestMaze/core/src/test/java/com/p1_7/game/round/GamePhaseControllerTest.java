package com.p1_7.game.round;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates GamePhaseController's static isTerminalPhase classification
 * and the hold timer logic in tickHoldTimer.
 *
 * checkRoomEntry requires a live Player instance (libGDX-dependent) so
 * those paths are covered by integration tests rather than unit tests here.
 */
public class GamePhaseControllerTest {

    private GamePhaseController controller;

    @BeforeEach
    public void setUp() {
        controller = new GamePhaseController();
    }

    // --- isTerminalPhase ---

    @Test
    public void testIsTerminalPhase_levelComplete_returnsTrue() {
        assertTrue(GamePhaseController.isTerminalPhase(RoundPhase.LEVEL_COMPLETE));
    }

    @Test
    public void testIsTerminalPhase_gameOver_returnsTrue() {
        assertTrue(GamePhaseController.isTerminalPhase(RoundPhase.GAME_OVER));
    }

    @Test
    public void testIsTerminalPhase_choosing_returnsFalse() {
        assertFalse(GamePhaseController.isTerminalPhase(RoundPhase.CHOOSING));
    }

    @Test
    public void testIsTerminalPhase_questionIntro_returnsFalse() {
        assertFalse(GamePhaseController.isTerminalPhase(RoundPhase.QUESTION_INTRO));
    }

    @Test
    public void testIsTerminalPhase_feedback_returnsFalse() {
        assertFalse(GamePhaseController.isTerminalPhase(RoundPhase.FEEDBACK));
    }

    @Test
    public void testIsTerminalPhase_roundReset_returnsFalse() {
        assertFalse(GamePhaseController.isTerminalPhase(RoundPhase.ROUND_RESET));
    }

    // --- tickHoldTimer ---

    @Test
    public void testTickHoldTimer_beforeExpiry_returnsFalse() {
        // seed a 2-second timer; tick by 1 second — should not yet expire
        controller.setHoldTimer(2.0f);
        boolean expired = controller.tickHoldTimer(1.0f);
        assertFalse(expired,
            "Timer should not expire when remaining time is still positive");
    }

    @Test
    public void testTickHoldTimer_exact_returnsTrue() {
        // tick by exactly the timer value — boundary condition
        controller.setHoldTimer(1.0f);
        boolean expired = controller.tickHoldTimer(1.0f);
        assertTrue(expired,
            "Timer should expire when delta equals the remaining time exactly");
    }

    @Test
    public void testTickHoldTimer_over_returnsTrue() {
        // tick by more than the remaining time
        controller.setHoldTimer(0.5f);
        boolean expired = controller.tickHoldTimer(1.0f);
        assertTrue(expired,
            "Timer should expire when delta exceeds the remaining time");
    }

    @Test
    public void testTickHoldTimer_multipleTicksToExpiry() {
        // three ticks of 0.4 s each against a 1-second timer
        controller.setHoldTimer(1.0f);

        assertFalse(controller.tickHoldTimer(0.4f), "First tick: 0.6 s remaining");
        assertFalse(controller.tickHoldTimer(0.4f), "Second tick: 0.2 s remaining");
        assertTrue(controller.tickHoldTimer(0.4f),  "Third tick: -0.2 s — should expire");
    }

    // --- setLastKnownPhase / resetRoomState round-trip ---

    @Test
    public void testSetLastKnownPhase_doesNotThrow() {
        // primarily ensures the setter exists and is callable for all phases
        for (RoundPhase phase : RoundPhase.values()) {
            assertDoesNotThrow(() -> controller.setLastKnownPhase(phase));
        }
    }

    @Test
    public void testResetRoomState_doesNotThrow() {
        assertDoesNotThrow(() -> controller.resetRoomState(),
            "resetRoomState must be callable without crashing");
    }
}

package com.p1_7.game.round;

/**
 * callback for phase transitions detected by GamePhaseController.
 *
 * GameScene implements this to react to transitions — resetting the player
 * on ROUND_RESET, refreshing the answer cache on QUESTION_INTRO, etc.
 */
public interface GamePhaseListener {

    /**
     * called when the game round transitions from one phase to another.
     *
     * @param from         the previous phase (null on the very first transition)
     * @param to           the new phase
     * @param orchestrator the level orchestrator for reading questions and room assignments
     */
    void onPhaseChanged(RoundPhase from, RoundPhase to, ILevelOrchestrator orchestrator);
}

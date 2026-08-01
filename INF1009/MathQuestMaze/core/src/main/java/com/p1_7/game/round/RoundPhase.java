package com.p1_7.game.round;

/**
 * represents the discrete phases of a single game round.
 *
 * the phase state machine is driven by gameround: submitAnswer() transitions out of
 * choosing, and advance() moves through the remaining phases. level_complete and
 * game_over are terminal — advance() throws from either of them.
 */
public enum RoundPhase {

    /** a new question is being presented to the player */
    QUESTION_INTRO,

    /** the player is selecting an answer from the four options */
    CHOOSING,

    /** the result of the last submitted answer is being shown */
    FEEDBACK,

    /** a correct answer was given; preparing to load the next question */
    ROUND_RESET,

    /** the score target has been reached; terminal phase */
    LEVEL_COMPLETE,

    /** health has reached zero; terminal phase */
    GAME_OVER
}

package com.p1_7.game.round;

import com.p1_7.game.math.Difficulty;
import com.p1_7.game.math.MathQuestion;

/**
 * facade that GameScene depends on to drive a single playthrough session.
 *
 * coordinates GameRound and RoomAssignment so the scene interacts with one object
 * rather than managing multiple domain classes. all state-querying methods are only
 * valid after startLevel has been called.
 */
public interface ILevelOrchestrator {

    /**
     * initialises a new game round at the given difficulty and generates the first question
     * and room assignment.
     *
     * must be called before any other method on this interface.
     *
     * @param difficulty the difficulty level to use for question generation; must not be null
     * @throws IllegalArgumentException if difficulty is null
     */
    void startLevel(Difficulty difficulty);

    /**
     * updates the difficulty that should be used the next time a level starts.
     *
     * this does not create a new round by itself; GameScene calls startLevel()
     * when gameplay begins.
     *
     * @param difficulty the difficulty to remember; must not be null
     * @throws IllegalArgumentException if difficulty is null
     */
    void setCurrentDifficulty(Difficulty difficulty);

    /**
     * returns the currently selected difficulty.
     *
     * before the first round starts this defaults to EASY so menu-driven game
     * starts have a stable baseline.
     *
     * @return the current difficulty selection
     */
    Difficulty getCurrentDifficulty();

    /**
     * returns the current phase of the active game round.
     *
     * @return the current RoundPhase
     * @throws IllegalStateException if startLevel has not been called
     */
    RoundPhase getPhase();

    /**
     * returns the question that is currently active in the game round.
     *
     * @return the current MathQuestion
     * @throws IllegalStateException if startLevel has not been called
     */
    MathQuestion getCurrentQuestion();

    /**
     * returns the room assignment for the current question.
     *
     * maps each room index (0–3) to the answer value placed in that room.
     *
     * @return the current RoomAssignment
     * @throws IllegalStateException if startLevel has not been called
     */
    RoomAssignment getRoomAssignment();

    /**
     * returns the player's current score.
     *
     * @return the number of correct answers given so far this round
     * @throws IllegalStateException if startLevel has not been called
     */
    int getScore();

    /**
     * returns the player's current health.
     *
     * @return the number of remaining lives
     * @throws IllegalStateException if startLevel has not been called
     */
    int getHealth();

    /**
     * returns the player's maximum health.
     *
     * @return the health cap
     * @throws IllegalStateException if startLevel has not been called
     */
    int getMaxHealth();

    /**
     * returns whether the most recently submitted answer was correct.
     *
     * meaningful only during the feedback phase.
     *
     * @return true if the last answer was correct, false otherwise
     * @throws IllegalStateException if startLevel has not been called
     */
    boolean isLastAnswerCorrect();

    /**
     * returns whether the most recent damage event came from enemy contact.
     *
     * @return true if the latest damaging event was enemy contact
     * @throws IllegalStateException if startLevel has not been called
     */
    boolean wasLastDamageFromEnemy();

    /**
     * applies one point of damage from an enemy collision to the player.
     *
     * @throws IllegalStateException if startLevel has not been called
     */
    void applyEnemyDamage();

    /**
     * restores up to the given amount of player health.
     *
     * @param amount number of health points to restore; must be positive
     * @return true if health increased, false if the player was already at full health
     * @throws IllegalStateException if startLevel has not been called
     */
    boolean healPlayer(int amount);

    /**
     * resolves the answer for the given room index and submits it to the game round.
     *
     * looks up the answer value assigned to roomIndex in the current room assignment and
     * delegates to GameRound.submitAnswer with that value.
     *
     * @param roomIndex the room index the player has chosen; must be in the range 0–3
     * @throws IllegalStateException if startLevel has not been called
     * @throws IllegalArgumentException if roomIndex is not in the range 0–3
     */
    void submitRoomChoice(int roomIndex);

    /**
     * advances the game round to the next phase.
     *
     * if the round transitions out of ROUND_RESET, the room assignment is refreshed to
     * match the newly generated question.
     *
     * @throws IllegalStateException if startLevel has not been called, or if the current
     *                               phase does not permit advancing
     */
    void advance();
}

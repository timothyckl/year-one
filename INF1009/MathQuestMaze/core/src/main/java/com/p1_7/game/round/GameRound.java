package com.p1_7.game.round;

import com.p1_7.game.math.MathQuestion;
import com.p1_7.game.math.QuestionGenerator;

/**
 * stateful domain object that drives a single playthrough of the math maze game.
 *
 * holds the player's current score, health, the active question, and the current round
 * phase. transitions are triggered by the scene calling submitAnswer() to evaluate a
 * player's choice, and advance() to move to the next phase once the scene has finished
 * displaying the current one.
 *
 * the phase state machine is deterministic: each call to advance() has exactly one
 * valid next phase, and both terminal phases (level_complete, game_over) throw rather
 * than transition.
 */
public class GameRound {

    /** initial health granted at the start of every round */
    private static final int STARTING_HEALTH = 3;

    /** score the player must reach to complete the level */
    private static final int WINNING_SCORE = 5;

    /** generator used to produce each new question */
    private final QuestionGenerator questionGenerator;

    /** number of correct answers given so far this round */
    private int score;

    /** remaining lives; reaching zero triggers game_over */
    private int health;

    /** the question currently active and waiting for a player answer */
    private MathQuestion currentQuestion;

    /** current position in the round phase state machine */
    private RoundPhase phase;

    /**
     * whether the most recent call to submitAnswer recorded a correct answer.
     * used by advance() to resolve the feedback branch and exposed for scene display.
     */
    private boolean lastAnswerCorrect;

    /** true when the most recent damage event came from enemy contact */
    private boolean lastDamageFromEnemy;

    /**
     * constructs a new game round using the given question generator.
     *
     * initialises score to 0, health to the starting value, phase to question_intro,
     * and generates the first question immediately.
     *
     * @param questionGenerator the generator that supplies questions for this round; must not be null
     * @throws IllegalArgumentException if questionGenerator is null
     */
    public GameRound(QuestionGenerator questionGenerator) {
        if (questionGenerator == null) {
            throw new IllegalArgumentException("questionGenerator must not be null");
        }

        this.questionGenerator = questionGenerator;
        this.score = 0;
        this.health = STARTING_HEALTH;
        this.phase = RoundPhase.QUESTION_INTRO;
        this.lastDamageFromEnemy = false;
        // generate the opening question so the scene has something to display immediately
        this.currentQuestion = questionGenerator.generateQuestion();
    }

    /**
     * returns the player's current score.
     *
     * @return the number of correct answers given so far
     */
    public int getScore() {
        return score;
    }

    /**
     * returns the player's remaining health.
     *
     * @return the number of lives remaining
     */
    public int getHealth() {
        return health;
    }

    /**
     * returns the maximum player health for this round.
     *
     * @return the health cap
     */
    public int getMaxHealth() {
        return STARTING_HEALTH;
    }

    /**
     * returns the question currently active in this round.
     *
     * @return the current math question
     */
    public MathQuestion getCurrentQuestion() {
        return currentQuestion;
    }

    /**
     * returns the current phase of the round state machine.
     *
     * @return the current round phase
     */
    public RoundPhase getPhase() {
        return phase;
    }

    /**
     * returns whether the most recently submitted answer was correct.
     *
     * meaningful only during the feedback phase; undefined before the first submitAnswer call.
     *
     * @return true if the last answer was correct, false otherwise
     */
    public boolean isLastAnswerCorrect() {
        return lastAnswerCorrect;
    }

    /**
     * returns whether the most recent damage event came from enemy contact.
     *
     * @return true if the latest damaging event was enemy contact
     */
    public boolean wasLastDamageFromEnemy() {
        return lastDamageFromEnemy;
    }

    /**
     * evaluates a player's answer against the current question and transitions the phase accordingly.
     *
     * correct answer: increments score and transitions to level_complete if the winning score
     * is reached, otherwise to feedback.
     *
     * wrong answer: decrements health and transitions to game_over if health reaches zero,
     * otherwise to feedback. the current question is retained on a wrong answer.
     *
     * @param answer the integer answer chosen by the player
     * @throws IllegalStateException if the current phase is not choosing
     */
    public void submitAnswer(int answer) {
        if (phase != RoundPhase.CHOOSING) {
            throw new IllegalStateException(
                "submitAnswer called in invalid phase: " + phase + "; expected CHOOSING");
        }

        if (answer == currentQuestion.getCorrectAnswer()) {
            // correct: reward the player and check win condition
            score++;
            lastAnswerCorrect = true;
            lastDamageFromEnemy = false;
            phase = (score >= WINNING_SCORE) ? RoundPhase.LEVEL_COMPLETE : RoundPhase.FEEDBACK;
        } else {
            // wrong: penalise the player and check loss condition; question stays active
            health--;
            lastAnswerCorrect = false;
            lastDamageFromEnemy = false;
            phase = (health <= 0) ? RoundPhase.GAME_OVER : RoundPhase.FEEDBACK;
        }
    }

    /**
     * applies one point of enemy-contact damage while the player is actively choosing.
     *
     * unlike submitAnswer(), this does not transition through FEEDBACK for non-lethal hits;
     * it only reduces health and moves directly to GAME_OVER if health reaches zero.
     *
     * @throws IllegalStateException if the current phase is not choosing
     */
    public void applyEnemyDamage() {
        if (phase != RoundPhase.CHOOSING) {
            throw new IllegalStateException(
                "applyEnemyDamage called in invalid phase: " + phase + "; expected CHOOSING");
        }

        health--;
        lastDamageFromEnemy = true;
        if (health <= 0) {
            phase = RoundPhase.GAME_OVER;
        }
    }

    /**
     * restores up to the given amount of health, capped at the round maximum.
     *
     * @param amount number of health points to restore; must be positive
     * @return true if health increased, false if the player was already at full health
     */
    public boolean healPlayer(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        if (health >= STARTING_HEALTH) {
            return false;
        }

        health = Math.min(STARTING_HEALTH, health + amount);
        return true;
    }

    /**
     * advances the round to the next phase according to the deterministic state machine.
     *
     * valid transitions:
     *   question_intro  -> choosing        (no side effect)
     *   feedback (correct) -> round_reset  (no side effect)
     *   feedback (wrong)   -> choosing     (same question retained)
     *   round_reset     -> question_intro  (generates a new question)
     *
     * level_complete and game_over are terminal: calling advance() from either throws.
     * calling advance() from choosing is also invalid; use submitAnswer() instead.
     *
     * @throws IllegalStateException if called from level_complete, game_over, choosing,
     *                               or any other phase that has no valid advance transition
     */
    public void advance() {
        switch (phase) {
            case QUESTION_INTRO:
                phase = RoundPhase.CHOOSING;
                break;

            case FEEDBACK:
                // branch based on whether the last answer was correct
                if (lastAnswerCorrect) {
                    phase = RoundPhase.ROUND_RESET;
                } else {
                    // same question; player tries again
                    phase = RoundPhase.CHOOSING;
                }
                break;

            case ROUND_RESET:
                // generate the new question before updating phase so a generator failure
                // does not leave the round with a stale question in an inconsistent state
                currentQuestion = questionGenerator.generateQuestion();
                phase = RoundPhase.QUESTION_INTRO;
                break;

            case LEVEL_COMPLETE:
                throw new IllegalStateException(
                    "advance called on terminal phase LEVEL_COMPLETE");

            case GAME_OVER:
                throw new IllegalStateException(
                    "advance called on terminal phase GAME_OVER");

            case CHOOSING:
                throw new IllegalStateException(
                    "advance is not valid from CHOOSING; call submitAnswer() instead");

            default:
                throw new IllegalStateException(
                    "advance called in unexpected phase: " + phase);
        }
    }
}

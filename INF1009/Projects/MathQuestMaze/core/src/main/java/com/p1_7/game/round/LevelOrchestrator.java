package com.p1_7.game.round;

import com.p1_7.game.math.Difficulty;
import com.p1_7.game.math.MathQuestion;
import com.p1_7.game.math.QuestionGenerator;

/**
 * concrete coordinator that implements ILevelOrchestrator by delegating to GameRound
 * and RoomAssignment.
 *
 * owns one GameRound per call to startLevel and keeps the room assignment in sync with
 * the active question. GameScene should depend on ILevelOrchestrator, not this class.
 */
public class LevelOrchestrator implements ILevelOrchestrator {

    /** factory used to create a new room assignment whenever the question changes */
    private final RoomAssigner roomAssigner;

    /** the active game round; null until startLevel is called */
    private GameRound gameRound;

    /** the current mapping of room indices to answer values; null until startLevel is called */
    private RoomAssignment roomAssignment;

    /** difficulty selected for the current or next gameplay session */
    private Difficulty currentDifficulty = Difficulty.EASY;

    /**
     * constructs a level orchestrator with a default room assigner.
     *
     * call startLevel before using any other method.
     */
    public LevelOrchestrator() {
        this(new RoomAssigner());
    }

    /**
     * constructs a level orchestrator with the given room assigner.
     *
     * package-private to allow injection in unit tests without exposing the
     * dependency publicly.
     *
     * @param roomAssigner the room assigner to use; must not be null
     * @throws IllegalArgumentException if roomAssigner is null
     */
    LevelOrchestrator(RoomAssigner roomAssigner) {
        if (roomAssigner == null) {
            throw new IllegalArgumentException("roomAssigner must not be null");
        }
        this.roomAssigner = roomAssigner;
    }

    /**
     * creates a new QuestionGenerator and GameRound for the given difficulty, then
     * refreshes the room assignment to match the first question. if called while a
     * round is already active, the previous round's score, health, and phase are
     * discarded and a new round begins immediately.
     *
     * @param difficulty the difficulty level for this session; must not be null
     * @throws IllegalArgumentException if difficulty is null
     */
    @Override
    public void startLevel(Difficulty difficulty) {
        setCurrentDifficulty(difficulty);
        QuestionGenerator generator = new QuestionGenerator(difficulty);
        gameRound = new GameRound(generator);
        refreshAssignment();
    }

    @Override
    public void setCurrentDifficulty(Difficulty difficulty) {
        if (difficulty == null) {
            throw new IllegalArgumentException("difficulty must not be null");
        }
        this.currentDifficulty = difficulty;
    }

    @Override
    public Difficulty getCurrentDifficulty() {
        return currentDifficulty;
    }

    @Override
    public RoundPhase getPhase() {
        requireActiveRound();
        return gameRound.getPhase();
    }

    @Override
    public MathQuestion getCurrentQuestion() {
        requireActiveRound();
        return gameRound.getCurrentQuestion();
    }

    @Override
    public RoomAssignment getRoomAssignment() {
        requireActiveRound();
        return roomAssignment;
    }

    @Override
    public int getScore() {
        requireActiveRound();
        return gameRound.getScore();
    }

    @Override
    public int getHealth() {
        requireActiveRound();
        return gameRound.getHealth();
    }

    @Override
    public int getMaxHealth() {
        requireActiveRound();
        return gameRound.getMaxHealth();
    }

    @Override
    public boolean isLastAnswerCorrect() {
        requireActiveRound();
        return gameRound.isLastAnswerCorrect();
    }

    @Override
    public boolean wasLastDamageFromEnemy() {
        requireActiveRound();
        return gameRound.wasLastDamageFromEnemy();
    }

    @Override
    public void applyEnemyDamage() {
        requireActiveRound();
        gameRound.applyEnemyDamage();
    }

    @Override
    public boolean healPlayer(int amount) {
        requireActiveRound();
        return gameRound.healPlayer(amount);
    }

    /**
     * resolves the answer for the given room index from the current assignment and
     * delegates to GameRound.submitAnswer.
     *
     * @param roomIndex the chosen room index; must be in the range 0–3
     * @throws IllegalStateException    if startLevel has not been called
     * @throws IllegalArgumentException if roomIndex is not in the range 0–3
     */
    @Override
    public void submitRoomChoice(int roomIndex) {
        requireActiveRound();

        // resolve the answer value from the current room layout before submitting
        int answer = roomAssignment.getAnswerForRoom(roomIndex);
        gameRound.submitAnswer(answer);
    }

    /**
     * captures the phase before delegating so the assignment can be refreshed when
     * advancing out of ROUND_RESET, which is the transition that loads a new question.
     *
     * @throws IllegalStateException if startLevel has not been called or the current
     *                               phase does not permit advancing
     */
    @Override
    public void advance() {
        requireActiveRound();

        // capture phase before advancing so we can detect the ROUND_RESET transition
        RoundPhase phaseBefore = gameRound.getPhase();
        gameRound.advance();

        // ROUND_RESET -> QUESTION_INTRO is the only transition that produces a new question;
        // refresh the room layout so it matches the newly generated question
        if (phaseBefore == RoundPhase.ROUND_RESET) {
            refreshAssignment();
        }
    }

    /**
     * rebuilds the room assignment to match the game round's current question.
     *
     * called after startLevel and after every advance from ROUND_RESET.
     */
    private void refreshAssignment() {
        roomAssignment = roomAssigner.assign(gameRound.getCurrentQuestion());
    }

    /**
     * throws an IllegalStateException if startLevel has not yet been called.
     *
     * @throws IllegalStateException if gameRound is null
     */
    private void requireActiveRound() {
        if (gameRound == null) {
            throw new IllegalStateException(
                "startLevel must be called before using the orchestrator");
        }
    }
}

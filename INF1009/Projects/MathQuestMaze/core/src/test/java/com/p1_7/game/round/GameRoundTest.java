package com.p1_7.game.round;

import com.p1_7.game.math.MathQuestion;
import com.p1_7.game.math.QuestionGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Validates GameRound's state machine: initial state, correct/wrong answer paths,
 * terminal conditions, advance() transitions, and healPlayer clamping.
 */
public class GameRoundTest {

    private QuestionGenerator mockGenerator;
    private MathQuestion fixedQuestion;
    private GameRound round;

    @BeforeEach
    public void setUp() {
        // use a fixed question so tests are not sensitive to generation randomness
        fixedQuestion = new MathQuestion("2 + 2 = ?", 4, Arrays.asList(4, 1, 7, 9));
        mockGenerator = mock(QuestionGenerator.class);
        when(mockGenerator.generateQuestion()).thenReturn(fixedQuestion);

        round = new GameRound(mockGenerator);
    }

    // --- initial state ---

    @Test
    public void testInitialState_scoreIsZero() {
        assertEquals(0, round.getScore());
    }

    @Test
    public void testInitialState_healthIsThree() {
        assertEquals(3, round.getHealth());
    }

    @Test
    public void testInitialState_phaseIsQuestionIntro() {
        assertEquals(RoundPhase.QUESTION_INTRO, round.getPhase());
    }

    @Test
    public void testInitialState_questionIsGenerated() {
        assertNotNull(round.getCurrentQuestion(),
            "A question should be generated immediately on construction");
    }

    // --- submitAnswer: correct answer ---

    @Test
    public void testSubmitAnswer_correct_incrementsScore() {
        // advance to CHOOSING first
        round.advance();
        assertEquals(RoundPhase.CHOOSING, round.getPhase());

        round.submitAnswer(fixedQuestion.getCorrectAnswer());

        assertEquals(1, round.getScore());
    }

    @Test
    public void testSubmitAnswer_correct_setsLastAnswerCorrectTrue() {
        round.advance();
        round.submitAnswer(fixedQuestion.getCorrectAnswer());
        assertTrue(round.isLastAnswerCorrect());
    }

    @Test
    public void testSubmitAnswer_correct_transitionsToFeedback() {
        round.advance();
        round.submitAnswer(fixedQuestion.getCorrectAnswer());
        assertEquals(RoundPhase.FEEDBACK, round.getPhase());
    }

    // --- submitAnswer: wrong answer ---

    @Test
    public void testSubmitAnswer_wrong_reducesHealth() {
        round.advance();
        round.submitAnswer(fixedQuestion.getCorrectAnswer() + 1);
        assertEquals(2, round.getHealth());
    }

    @Test
    public void testSubmitAnswer_wrong_setsLastAnswerCorrectFalse() {
        round.advance();
        round.submitAnswer(fixedQuestion.getCorrectAnswer() + 1);
        assertFalse(round.isLastAnswerCorrect());
    }

    @Test
    public void testSubmitAnswer_wrong_transitionsToFeedback() {
        round.advance();
        round.submitAnswer(fixedQuestion.getCorrectAnswer() + 1);
        assertEquals(RoundPhase.FEEDBACK, round.getPhase());
    }

    // --- terminal condition: GAME_OVER ---

    @Test
    public void testSubmitAnswer_healthReachesZero_phaseBecomesGameOver() {
        round.advance(); // QUESTION_INTRO → CHOOSING

        // deplete health with wrong answers (cycling through feedback → choosing)
        int wrong = fixedQuestion.getCorrectAnswer() + 1;
        round.submitAnswer(wrong); // health = 2, FEEDBACK
        round.advance();           // FEEDBACK (wrong) → CHOOSING
        round.submitAnswer(wrong); // health = 1, FEEDBACK
        round.advance();           // CHOOSING
        round.submitAnswer(wrong); // health = 0 → GAME_OVER

        assertEquals(RoundPhase.GAME_OVER, round.getPhase());
        assertEquals(0, round.getHealth());
    }

    // --- terminal condition: LEVEL_COMPLETE ---

    @Test
    public void testSubmitAnswer_scoreFive_phaseBecomesLevelComplete() {
        int correct = fixedQuestion.getCorrectAnswer();
        // answer correctly 5 times, cycling INTRO→CHOOSING→FEEDBACK→RESET→INTRO between each
        for (int i = 0; i < 5; i++) {
            round.advance();           // → CHOOSING
            round.submitAnswer(correct);
            if (round.getPhase() == RoundPhase.LEVEL_COMPLETE) break;
            round.advance();           // FEEDBACK (correct) → ROUND_RESET
            round.advance();           // ROUND_RESET → QUESTION_INTRO
        }

        assertEquals(RoundPhase.LEVEL_COMPLETE, round.getPhase());
        assertEquals(5, round.getScore());
    }

    // --- advance() transitions ---

    @Test
    public void testAdvance_fromQuestionIntro_goesToChoosing() {
        assertEquals(RoundPhase.QUESTION_INTRO, round.getPhase());
        round.advance();
        assertEquals(RoundPhase.CHOOSING, round.getPhase());
    }

    @Test
    public void testAdvance_fromFeedbackCorrect_goesToRoundReset() {
        round.advance();
        round.submitAnswer(fixedQuestion.getCorrectAnswer());
        assertEquals(RoundPhase.FEEDBACK, round.getPhase());

        round.advance();
        assertEquals(RoundPhase.ROUND_RESET, round.getPhase());
    }

    @Test
    public void testAdvance_fromFeedbackWrong_goesToChoosing() {
        round.advance();
        round.submitAnswer(fixedQuestion.getCorrectAnswer() + 1);
        assertEquals(RoundPhase.FEEDBACK, round.getPhase());

        round.advance();
        assertEquals(RoundPhase.CHOOSING, round.getPhase());
    }

    @Test
    public void testAdvance_fromRoundReset_goesToQuestionIntro() {
        round.advance();
        round.submitAnswer(fixedQuestion.getCorrectAnswer());
        round.advance(); // FEEDBACK → ROUND_RESET
        round.advance(); // ROUND_RESET → QUESTION_INTRO

        assertEquals(RoundPhase.QUESTION_INTRO, round.getPhase());
    }

    @Test
    public void testAdvance_fromLevelComplete_throwsIllegalStateException() {
        // reach LEVEL_COMPLETE
        int correct = fixedQuestion.getCorrectAnswer();
        for (int i = 0; i < 5; i++) {
            round.advance();
            round.submitAnswer(correct);
            if (round.getPhase() == RoundPhase.LEVEL_COMPLETE) break;
            round.advance();
            round.advance();
        }

        assertThrows(IllegalStateException.class, round::advance,
            "advance() must throw from the terminal LEVEL_COMPLETE phase");
    }

    @Test
    public void testAdvance_fromGameOver_throwsIllegalStateException() {
        round.advance();
        int wrong = fixedQuestion.getCorrectAnswer() + 1;
        round.submitAnswer(wrong);
        round.advance();
        round.submitAnswer(wrong);
        round.advance();
        round.submitAnswer(wrong);
        assertEquals(RoundPhase.GAME_OVER, round.getPhase());

        assertThrows(IllegalStateException.class, round::advance,
            "advance() must throw from the terminal GAME_OVER phase");
    }

    @Test
    public void testAdvance_fromChoosing_throwsIllegalStateException() {
        round.advance(); // → CHOOSING
        assertThrows(IllegalStateException.class, round::advance,
            "advance() is invalid from CHOOSING; submitAnswer() should be used instead");
    }

    // --- submitAnswer called from wrong phase ---

    @Test
    public void testSubmitAnswer_fromNonChoosing_throwsIllegalStateException() {
        // phase is QUESTION_INTRO at this point
        assertThrows(IllegalStateException.class,
            () -> round.submitAnswer(4),
            "submitAnswer() must throw if phase is not CHOOSING"
        );
    }

    // --- applyEnemyDamage ---

    @Test
    public void testApplyEnemyDamage_reducesHealth() {
        round.advance(); // → CHOOSING
        round.applyEnemyDamage();
        assertEquals(2, round.getHealth());
    }

    @Test
    public void testApplyEnemyDamage_setsLastDamageFromEnemyTrue() {
        round.advance();
        round.applyEnemyDamage();
        assertTrue(round.wasLastDamageFromEnemy());
    }

    @Test
    public void testApplyEnemyDamage_lethal_phaseBecomesGameOver() {
        round.advance();
        round.applyEnemyDamage(); // health = 2
        round.applyEnemyDamage(); // health = 1
        round.applyEnemyDamage(); // health = 0 → GAME_OVER

        assertEquals(RoundPhase.GAME_OVER, round.getPhase());
    }

    @Test
    public void testApplyEnemyDamage_fromNonChoosing_throwsIllegalStateException() {
        // phase is QUESTION_INTRO
        assertThrows(IllegalStateException.class,
            round::applyEnemyDamage,
            "applyEnemyDamage() must throw if phase is not CHOOSING"
        );
    }

    // --- healPlayer ---

    @Test
    public void testHealPlayer_restoresHealth() {
        round.advance();
        round.submitAnswer(fixedQuestion.getCorrectAnswer() + 1); // health = 2
        round.advance(); // FEEDBACK → CHOOSING

        boolean healed = round.healPlayer(1);
        assertTrue(healed);
        assertEquals(3, round.getHealth());
    }

    @Test
    public void testHealPlayer_atFullHealth_returnsFalse() {
        boolean healed = round.healPlayer(1);
        assertFalse(healed,
            "healPlayer should return false when already at full health");
        assertEquals(3, round.getHealth(),
            "Health should remain unchanged when already at max");
    }

    @Test
    public void testHealPlayer_clampsToMaxHealth() {
        round.advance();
        // take two damage points (go to 1 health)
        round.submitAnswer(fixedQuestion.getCorrectAnswer() + 1);
        round.advance();
        round.submitAnswer(fixedQuestion.getCorrectAnswer() + 1);
        round.advance();

        // try to heal by more than the deficit
        round.healPlayer(10);
        assertEquals(round.getMaxHealth(), round.getHealth(),
            "Health must not exceed the maximum");
    }

    @Test
    public void testHealPlayer_zeroAmount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> round.healPlayer(0),
            "Healing by zero must throw IllegalArgumentException"
        );
    }

    @Test
    public void testNullQuestionGenerator_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> new GameRound(null),
            "A null QuestionGenerator must throw IllegalArgumentException"
        );
    }
}

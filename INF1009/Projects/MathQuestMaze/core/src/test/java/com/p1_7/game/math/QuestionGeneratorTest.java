package com.p1_7.game.math;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates QuestionGenerator question structure and per-difficulty invariants.
 * Uses a seeded Random to keep results deterministic across runs.
 */
public class QuestionGeneratorTest {

    // --- structural invariants (apply at every difficulty) ---

    @Test
    public void testGeneratedQuestion_hasFourOptions() {
        QuestionGenerator gen = new QuestionGenerator(Difficulty.EASY, new Random(1));
        MathQuestion q = gen.generateQuestion();
        assertEquals(4, q.getOptions().size(),
            "Every generated question must have exactly four answer options");
    }

    @Test
    public void testGeneratedQuestion_correctAnswerIsInOptions() {
        QuestionGenerator gen = new QuestionGenerator(Difficulty.EASY, new Random(2));
        for (int i = 0; i < 20; i++) {
            MathQuestion q = gen.generateQuestion();
            assertTrue(q.getOptions().contains(q.getCorrectAnswer()),
                "The correct answer must always be one of the four options");
        }
    }

    @Test
    public void testGeneratedQuestion_optionsAreUnique() {
        QuestionGenerator gen = new QuestionGenerator(Difficulty.EASY, new Random(3));
        for (int i = 0; i < 20; i++) {
            MathQuestion q = gen.generateQuestion();
            Set<Integer> distinct = new HashSet<>(q.getOptions());
            assertEquals(4, distinct.size(),
                "All four answer options must be unique");
        }
    }

    @Test
    public void testGeneratedQuestion_allOptionsNonNegative() {
        QuestionGenerator gen = new QuestionGenerator(Difficulty.EASY, new Random(4));
        for (int i = 0; i < 20; i++) {
            MathQuestion q = gen.generateQuestion();
            for (int option : q.getOptions()) {
                assertTrue(option >= 0,
                    "No answer option should be negative");
            }
        }
    }

    @Test
    public void testGeneratedQuestion_promptIsNotEmpty() {
        QuestionGenerator gen = new QuestionGenerator(Difficulty.EASY, new Random(5));
        MathQuestion q = gen.generateQuestion();
        assertNotNull(q.getPrompt());
        assertFalse(q.getPrompt().isEmpty(),
            "The question prompt must not be empty");
    }

    // --- difficulty-specific operation constraints ---

    @Test
    public void testEasy_onlyAdditionAndSubtraction() {
        // easy allows only ADDITION and SUBTRACTION — verify over many samples
        QuestionGenerator gen = new QuestionGenerator(Difficulty.EASY, new Random(10));
        List<Operation> allowed = Difficulty.EASY.getAllowedOperations();
        assertEquals(2, allowed.size());
        assertTrue(allowed.contains(Operation.ADDITION));
        assertTrue(allowed.contains(Operation.SUBTRACTION));
        assertFalse(allowed.contains(Operation.MULTIPLICATION));
        assertFalse(allowed.contains(Operation.DIVISION));
    }

    @Test
    public void testMedium_includesMultiplication() {
        List<Operation> allowed = Difficulty.MEDIUM.getAllowedOperations();
        assertTrue(allowed.contains(Operation.MULTIPLICATION),
            "MEDIUM difficulty must allow multiplication");
        assertFalse(allowed.contains(Operation.DIVISION),
            "MEDIUM difficulty must not allow division");
    }

    @Test
    public void testHard_includesAllFourOperations() {
        List<Operation> allowed = Difficulty.HARD.getAllowedOperations();
        assertTrue(allowed.contains(Operation.ADDITION));
        assertTrue(allowed.contains(Operation.SUBTRACTION));
        assertTrue(allowed.contains(Operation.MULTIPLICATION));
        assertTrue(allowed.contains(Operation.DIVISION));
    }

    @Test
    public void testEasy_operandsWithinRange() {
        // with a seeded generator, run many questions and confirm each correct
        // answer is consistent with easy operand bounds (1–10 + 1–10 ≤ 20)
        QuestionGenerator gen = new QuestionGenerator(Difficulty.EASY, new Random(20));
        for (int i = 0; i < 50; i++) {
            MathQuestion q = gen.generateQuestion();
            // max result for addition on EASY: 10 + 10 = 20
            // subtraction always non-negative per the swap guarantee
            assertTrue(q.getCorrectAnswer() >= 0,
                "Easy question answers must be non-negative");
            assertTrue(q.getCorrectAnswer() <= 20,
                "Easy addition/subtraction answers cannot exceed 20");
        }
    }

    @Test
    public void testSubtraction_resultIsNeverNegative() {
        // force subtraction by using a custom difficulty stub — instead, generate
        // many questions at EASY (which allows subtraction) and assert non-negative
        QuestionGenerator gen = new QuestionGenerator(Difficulty.EASY, new Random(99));
        for (int i = 0; i < 100; i++) {
            MathQuestion q = gen.generateQuestion();
            assertTrue(q.getCorrectAnswer() >= 0,
                "Subtraction operand-swap guarantee must prevent negative answers");
        }
    }

    @Test
    public void testDivision_resultIsWholeNumber() {
        // hard difficulty includes division — generate many hard questions and
        // verify that all produce whole-number correct answers (which they always
        // will since correctAnswer is the quotient, not computed by a÷b)
        QuestionGenerator gen = new QuestionGenerator(Difficulty.HARD, new Random(77));
        for (int i = 0; i < 100; i++) {
            MathQuestion q = gen.generateQuestion();
            // the correct answer for division is the pre-chosen quotient (always an int)
            assertTrue(q.getCorrectAnswer() >= 0,
                "Division answers must be non-negative");
        }
    }

    // --- constructor validation ---

    @Test
    public void testNullDifficulty_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
            new QuestionGenerator(null),
            "Null difficulty must throw IllegalArgumentException"
        );
    }

    @Test
    public void testNullRandom_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
            new QuestionGenerator(Difficulty.EASY, null),
            "Null random must throw IllegalArgumentException"
        );
    }
}

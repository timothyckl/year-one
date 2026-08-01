package com.p1_7.game.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * stateful generator that produces math question instances for a given difficulty level.
 *
 * each call to generateQuestion produces a new arithmetic question with one correct answer
 * and three plausible distractors. the difficulty controls the operand range and the set
 * of allowed operations; each allowed operation has equal probability of being selected.
 */
public class QuestionGenerator {

    /** the difficulty level that determines the operand range and allowed operations */
    private final Difficulty difficulty;

    /** random number source used for all generation decisions */
    private final Random random;

    /**
     * constructs a question generator for the given difficulty level using an unseeded random source.
     *
     * @param difficulty the difficulty that controls operand range and operations; must not be null
     * @throws IllegalArgumentException if difficulty is null
     */
    public QuestionGenerator(Difficulty difficulty) {
        this(difficulty, new Random());
    }

    /**
     * constructs a question generator with an explicit random source.
     *
     * prefer this overload in tests: passing a seeded random instance makes generation
     * deterministic, allowing specific behaviours (e.g. operand swapping) to be asserted reliably.
     *
     * @param difficulty the difficulty that controls operand range and operations; must not be null
     * @param random     the random source to use for all generation decisions; must not be null
     * @throws IllegalArgumentException if difficulty or random is null
     */
    public QuestionGenerator(Difficulty difficulty, Random random) {
        if (difficulty == null) {
            throw new IllegalArgumentException("difficulty must not be null");
        }
        if (random == null) {
            throw new IllegalArgumentException("random must not be null");
        }
        this.difficulty = difficulty;
        this.random = random;
    }

    /**
     * generates a single math question appropriate for this generator's difficulty.
     *
     * the operation is chosen with equal probability from the difficulty's allowed set.
     * subtraction operands are swapped when necessary to ensure a non-negative result.
     * division always produces a whole-number quotient. the returned question contains
     * exactly four unique non-negative integer options including the correct answer.
     *
     * @return a new math question with a prompt, correct answer, and four answer options
     */
    public MathQuestion generateQuestion() {
        List<Operation> ops = difficulty.getAllowedOperations();
        Operation operation = ops.get(random.nextInt(ops.size()));

        int min = difficulty.getMinOperand();
        int max = difficulty.getMaxOperand();

        int correctAnswer;
        String prompt;

        if (operation == Operation.ADDITION) {
            int a = min + random.nextInt(max - min + 1);
            int b = min + random.nextInt(max - min + 1);
            correctAnswer = a + b;
            prompt = a + " + " + b + " = ?";

        } else if (operation == Operation.SUBTRACTION) {
            int a = min + random.nextInt(max - min + 1);
            int b = min + random.nextInt(max - min + 1);
            // swap operands so the result is always >= 0
            if (a < b) {
                int temp = a;
                a = b;
                b = temp;
            }
            correctAnswer = a - b;
            prompt = a + " - " + b + " = ?";

        } else if (operation == Operation.MULTIPLICATION) {
            int a = min + random.nextInt(max - min + 1);
            int b = min + random.nextInt(max - min + 1);
            correctAnswer = a * b;
            prompt = a + " \u00d7 " + b + " = ?";

        } else if (operation == Operation.DIVISION) {
            // pick the quotient first so answers are distributed uniformly across the operand range;
            // picking the divisor first would concentrate answers near 1 for large divisors
            int q = min + random.nextInt(max - min + 1);
            // largest divisor b such that the dividend b * q stays within the operand range
            int maxDivisor = max / q;
            int b = min + random.nextInt(maxDivisor - min + 1);
            int a = b * q;
            correctAnswer = q;
            prompt = a + " \u00f7 " + b + " = ?";

        } else {
            throw new IllegalStateException("unhandled operation: " + operation);
        }

        List<Integer> options = buildOptions(correctAnswer);
        return new MathQuestion(prompt, correctAnswer, options);
    }

    /**
     * builds a shuffled list of four unique non-negative integer answer options.
     *
     * starts with the correct answer then fills the remaining three slots using random
     * offsets drawn from the set {-5,-4,-3,-2,-1,1,2,3,4,5}. only offsets that produce
     * values distinct from the correct answer and from each other, and that are non-negative,
     * are accepted. the final list is shuffled so the correct answer position varies.
     *
     * @param correctAnswer the answer that must appear in the returned list
     * @return a shuffled list of exactly four unique non-negative integers including correctAnswer
     */
    private List<Integer> buildOptions(int correctAnswer) {
        List<Integer> options = new ArrayList<>();
        options.add(correctAnswer);

        // candidate offsets to apply to the correct answer to produce distractors
        List<Integer> offsets = Arrays.asList(-5, -4, -3, -2, -1, 1, 2, 3, 4, 5);
        Collections.shuffle(offsets, random);

        for (int offset : offsets) {
            if (options.size() == 4) {
                break;
            }

            int candidate = correctAnswer + offset;

            // skip negative values and values already in the list
            if (candidate < 0 || options.contains(candidate)) {
                continue;
            }

            options.add(candidate);
        }

        // guard against an exhausted offset pool producing fewer than 4 options
        if (options.size() != 4) {
            throw new IllegalStateException(
                "buildOptions failed to produce 4 unique non-negative distractors for correctAnswer="
                    + correctAnswer + "; only produced " + options.size());
        }

        // shuffle so the correct answer does not always appear at index 0
        Collections.shuffle(options, random);
        return options;
    }
}

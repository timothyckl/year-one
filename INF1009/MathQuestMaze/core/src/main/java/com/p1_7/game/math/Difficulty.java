package com.p1_7.game.math;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * difficulty levels available in the math maze game.
 *
 * each constant maps to an inclusive operand range used by the question generator
 * to control how large the numbers in arithmetic questions can be, and a set of
 * allowed operations that may appear at that difficulty.
 */
public enum Difficulty {

    /** easy difficulty — operands 1–10, addition and subtraction only */
    EASY(1, 10, Operation.ADDITION, Operation.SUBTRACTION),

    /** medium difficulty — operands 1–20, addition, subtraction, and multiplication */
    MEDIUM(1, 20, Operation.ADDITION, Operation.SUBTRACTION, Operation.MULTIPLICATION),

    /** hard difficulty — operands 1–100, all four basic operations */
    HARD(1, 100, Operation.ADDITION, Operation.SUBTRACTION, Operation.MULTIPLICATION, Operation.DIVISION);

    /** the smallest operand value that may appear in a question at this difficulty */
    private final int minOperand;

    /** the largest operand value that may appear in a question at this difficulty */
    private final int maxOperand;

    /** the operations that may be selected when generating a question at this difficulty */
    private final List<Operation> allowedOperations;

    /**
     * constructs a difficulty level with the given operand range and allowed operations.
     *
     * @param minOperand         the inclusive lower bound for operands
     * @param maxOperand         the inclusive upper bound for operands
     * @param allowedOperations  one or more operations permitted at this difficulty
     */
    Difficulty(int minOperand, int maxOperand, Operation... allowedOperations) {
        if (minOperand > maxOperand) {
            throw new IllegalArgumentException(
                "minOperand (" + minOperand + ") must be <= maxOperand (" + maxOperand + ")");
        }
        if (allowedOperations == null || allowedOperations.length == 0) {
            throw new IllegalArgumentException("allowedOperations must contain at least one operation");
        }
        this.minOperand = minOperand;
        this.maxOperand = maxOperand;
        this.allowedOperations = Collections.unmodifiableList(Arrays.asList(allowedOperations));
    }

    /**
     * returns the smallest operand value for this difficulty.
     *
     * @return inclusive lower bound for operands
     */
    public int getMinOperand() {
        return minOperand;
    }

    /**
     * returns the largest operand value for this difficulty.
     *
     * @return inclusive upper bound for operands
     */
    public int getMaxOperand() {
        return maxOperand;
    }

    /**
     * returns the operations permitted at this difficulty level.
     *
     * each operation in the returned list has equal probability of being selected
     * by the question generator.
     *
     * @return an unmodifiable list of allowed operations; never null or empty
     */
    public List<Operation> getAllowedOperations() {
        return allowedOperations;
    }
}

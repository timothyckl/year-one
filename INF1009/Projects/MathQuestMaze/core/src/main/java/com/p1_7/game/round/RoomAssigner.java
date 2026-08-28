package com.p1_7.game.round;

import com.p1_7.game.math.MathQuestion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * assigns answer options to room indices with its own independent shuffle.
 *
 * room index ordering is decoupled from the option ordering in MathQuestion
 * so the two shuffle steps are independent and neither relies on the other.
 */
public class RoomAssigner {

    /** random source for shuffling room indices; injectable for testability */
    private final Random random;

    /**
     * constructs an assigner using the default random source.
     */
    public RoomAssigner() {
        this(new Random());
    }

    /**
     * constructs an assigner with an explicit random source.
     *
     * @param random the random source used for shuffling room indices
     */
    public RoomAssigner(Random random) {
        if (random == null) {
            throw new IllegalArgumentException("random must not be null");
        }
        this.random = random;
    }

    /**
     * assigns the four answer options from the given question to room indices 0–3
     * using an independently shuffled room-index array.
     *
     * @param question the math question whose options will be assigned to rooms;
     *                 must not be null and must contain exactly four options
     * @return an immutable room assignment mapping each index 0–3 to one answer value
     * @throws IllegalArgumentException if question is null or does not have exactly 4 options
     */
    public RoomAssignment assign(MathQuestion question) {
        if (question == null) {
            throw new IllegalArgumentException("question must not be null");
        }

        List<Integer> options = question.getOptions();
        if (options.size() != 4) {
            throw new IllegalArgumentException(
                "question must have exactly 4 options, got: " + options.size());
        }

        // shuffle room indices independently of the option order
        int[] roomIndices = { 0, 1, 2, 3 };
        shuffleArray(roomIndices);

        Map<Integer, Integer> roomToAnswer = new HashMap<>();
        for (int i = 0; i < options.size(); i++) {
            roomToAnswer.put(roomIndices[i], options.get(i));
        }

        return new RoomAssignment(roomToAnswer);
    }

    /**
     * Fisher-Yates shuffle of the given array in place.
     */
    private void shuffleArray(int[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
    }
}

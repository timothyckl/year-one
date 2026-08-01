package com.p1_7.game.round;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * immutable value object that maps room indices (0–3) to integer answer values.
 *
 * each entry associates one of the four dungeon room slots with the answer option
 * that the player will encounter by entering that room. the assignment is fixed for
 * the duration of a single question and replaced when a new question is loaded.
 */
public class RoomAssignment {

    /** unmodifiable map from room index to the answer value assigned to that room */
    private final Map<Integer, Integer> roomToAnswer;

    /**
     * constructs a room assignment from the given room-to-answer map.
     *
     * a defensive unmodifiable copy is stored so the caller cannot mutate the assignment
     * after construction.
     *
     * @param roomToAnswer a map with keys 0–3, each mapping to an integer answer value;
     *                     must not be null and must contain exactly four entries
     * @throws IllegalArgumentException if roomToAnswer is null or does not contain exactly four entries
     */
    public RoomAssignment(Map<Integer, Integer> roomToAnswer) {
        if (roomToAnswer == null) {
            throw new IllegalArgumentException("roomToAnswer must not be null");
        }
        if (roomToAnswer.size() != 4) {
            throw new IllegalArgumentException(
                "roomToAnswer must contain exactly 4 entries, got: " + roomToAnswer.size());
        }

        // defensive copy so the assignment cannot be mutated through the original map reference
        this.roomToAnswer = Collections.unmodifiableMap(new HashMap<>(roomToAnswer));
    }

    /**
     * returns the answer value assigned to the given room index.
     *
     * @param roomIndex the room slot to look up; must be in the range 0–3 inclusive
     * @return the integer answer value assigned to that room
     * @throws IllegalArgumentException if roomIndex is not in the range 0–3
     */
    public int getAnswerForRoom(int roomIndex) {
        Integer answer = roomToAnswer.get(roomIndex);
        if (answer == null) {
            throw new IllegalArgumentException(
                "roomIndex must be in [0, 3], got: " + roomIndex);
        }
        return answer;
    }
}

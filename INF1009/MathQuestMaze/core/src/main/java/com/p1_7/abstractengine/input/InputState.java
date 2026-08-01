package com.p1_7.abstractengine.input;

/**
 * the discrete per-frame state of a logical input action.
 */
public enum InputState {

    /** action just became active this frame */
    PRESSED,

    /** action has been held since a previous frame */
    HELD,

    /** action just became inactive this frame */
    RELEASED
}

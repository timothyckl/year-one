package com.p1_7.abstractengine.input;

/**
 * unified input interface that combines read-only action queries with
 * write-side key/button remapping.
 *
 * this type is intended for configuration and setup code (e.g. game
 * initialisation or a key-rebinding screen) that needs to bind keys and
 * query input state in the same place.
 */
public interface IInputManager extends IInputQuery, IInputMapping {
}

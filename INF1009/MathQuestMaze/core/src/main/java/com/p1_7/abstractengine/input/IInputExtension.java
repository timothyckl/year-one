package com.p1_7.abstractengine.input;

/**
 * marker interface for game-layer input extensions registered with InputManager.
 *
 * implement this to define a new input type (e.g. cursor position, touch,
 * gamepad axes) that can be registered via IInputExtensionRegistry and
 * retrieved by scenes through IInputManager.
 */
public interface IInputExtension {
}

package com.p1_7.abstractengine.input;

/**
 * abstraction over physical input device polling, allowing the engine
 * to query key and button state without depending on any specific framework.
 */
public interface IInputSource {

    /**
     * returns whether the specified keyboard key is currently pressed.
     *
     * @param keyCode the platform key code
     * @return true if the key is held down
     */
    boolean isKeyPressed(int keyCode);

    /**
     * returns whether the specified mouse or controller button is currently
     * pressed.
     *
     * @param buttonCode the platform button code
     * @return true if the button is held down
     */
    boolean isButtonPressed(int buttonCode);
}

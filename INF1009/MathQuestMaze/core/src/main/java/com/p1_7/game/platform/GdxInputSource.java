package com.p1_7.game.platform;

import com.badlogic.gdx.Gdx;
import com.p1_7.abstractengine.input.IInputSource;

/**
 * libgdx implementation of IInputSource that delegates to Gdx.input.
 */
public class GdxInputSource implements IInputSource {

    /**
     * returns whether the specified keyboard key is currently pressed.
     *
     * @param keyCode the libgdx key code
     * @return true if the key is held down
     */
    @Override
    public boolean isKeyPressed(int keyCode) {
        return Gdx.input.isKeyPressed(keyCode);
    }

    /**
     * returns whether the specified mouse button is currently pressed.
     *
     * @param buttonCode the libgdx button code
     * @return true if the button is held down
     */
    @Override
    public boolean isButtonPressed(int buttonCode) {
        return Gdx.input.isButtonPressed(buttonCode);
    }
}

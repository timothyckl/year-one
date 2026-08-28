package com.p1_7.game.input;

import com.p1_7.abstractengine.input.IInputExtension;

/**
 * provides world-space cursor coordinates with the Y-flip already applied,
 * decoupling scene and entity code from direct Gdx.input polling.
 *
 * origin is bottom-left; Y increases upwards, matching the render system.
 */
public interface ICursorSource extends IInputExtension {

    /**
     * returns the cursor X position in world space.
     *
     * @return cursor x in pixels from the left edge
     */
    float getCursorX();

    /**
     * returns the cursor Y position in world space with Y-flip applied.
     *
     * @return cursor y in pixels from the bottom edge
     */
    float getCursorY();
}

package com.p1_7.game.platform;

import com.badlogic.gdx.Gdx;
import com.p1_7.game.Settings;
import com.p1_7.game.input.ICursorSource;

/**
 * libGDX implementation of ICursorSource.
 *
 * applies the Y-flip using Gdx.graphics.getHeight() so the result is
 * always correct even if the window is resized after startup.
 */
public class GdxCursorSource implements ICursorSource {

    private float scaleX() {
        int screenWidth = Gdx.graphics.getWidth();
        if (screenWidth <= 0) {
            return 1f;
        }
        return (float) Settings.getWindowWidth() / screenWidth;
    }

    private float scaleY() {
        int screenHeight = Gdx.graphics.getHeight();
        if (screenHeight <= 0) {
            return 1f;
        }
        return (float) Settings.getWindowHeight() / screenHeight;
    }

    /**
     * returns the raw horizontal cursor position in screen pixels.
     *
     * @return cursor x in pixels from the left edge
     */
    @Override
    public float getCursorX() {
        return Gdx.input.getX() * scaleX();
    }

    /**
     * returns the cursor Y position flipped to world space (bottom-left origin).
     *
     * @return cursor y in pixels from the bottom edge
     */
    @Override
    public float getCursorY() {
        return (Gdx.graphics.getHeight() - Gdx.input.getY()) * scaleY();
    }
}

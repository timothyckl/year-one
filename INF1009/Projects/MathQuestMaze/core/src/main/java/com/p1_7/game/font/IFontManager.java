package com.p1_7.game.font;

import com.badlogic.gdx.graphics.g2d.BitmapFont;

/**
 * Contract for shared game font access.
 */
public interface IFontManager {

    BitmapFont getGoldDisplayFont(int size);

    BitmapFont getDarkTextFont(int size);

    BitmapFont getPromptFont();

    BitmapFont getLightTextFont(int size);
}

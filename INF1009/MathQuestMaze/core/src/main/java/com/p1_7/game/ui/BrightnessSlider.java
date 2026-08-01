package com.p1_7.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.p1_7.game.Settings;

/**
 * horizontal brightness slider. value range is [Settings.MIN_BRIGHTNESS_LEVEL, 1.0]
 * with a gold colour scheme.
 *
 * the slider does not mutate Settings directly; the owning scene is responsible for
 * calling Settings.setBrightnessLevel(getValue()) after hasMoved() returns true.
 */
public final class BrightnessSlider extends Slider {

    private static final Color COLOUR_TRACK     = new Color(0.20f, 0.20f, 0.55f, 1f);
    private static final Color COLOUR_FILLED    = new Color(1.00f, 0.88f, 0.42f, 1f);
    private static final Color COLOUR_KNOB      = new Color(0.82f, 0.64f, 0.18f, 1f);
    private static final Color COLOUR_KNOB_DRAG = Color.WHITE;

    /**
     * creates a brightness slider centred at (centreX, centreY).
     *
     * @param centreX      horizontal centre of the slider in world coordinates
     * @param centreY      vertical centre of the slider in world coordinates
     * @param trackWidth   pixel span of the full draggable range
     * @param initialValue starting brightness level in [Settings.MIN_BRIGHTNESS_LEVEL, 1.0]
     */
    public BrightnessSlider(float centreX, float centreY, float trackWidth, float initialValue) {
        super(centreX, centreY, trackWidth, initialValue);
    }

    @Override
    protected float getMinValue() { return Settings.MIN_BRIGHTNESS_LEVEL; }

    @Override
    protected float getMaxValue() { return 1f; }

    @Override
    protected Color getTrackColour() { return COLOUR_TRACK; }

    @Override
    protected Color getFilledColour() { return COLOUR_FILLED; }

    @Override
    protected Color getKnobColour() { return COLOUR_KNOB; }

    @Override
    protected Color getKnobDragColour() { return COLOUR_KNOB_DRAG; }
}

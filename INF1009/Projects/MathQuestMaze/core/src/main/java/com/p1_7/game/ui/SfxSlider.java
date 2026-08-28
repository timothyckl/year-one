package com.p1_7.game.ui;

import com.badlogic.gdx.graphics.Color;

/**
 * horizontal SFX volume slider. value range is [0.0, 1.0]
 * with a teal colour scheme distinct from music and brightness.
 */
public final class SfxSlider extends Slider {

    private static final Color COLOUR_TRACK     = new Color(0.14f, 0.30f, 0.36f, 1f);
    private static final Color COLOUR_FILLED    = new Color(0.56f, 0.95f, 0.92f, 1f);
    private static final Color COLOUR_KNOB      = new Color(0.12f, 0.70f, 0.68f, 1f);
    private static final Color COLOUR_KNOB_DRAG = Color.WHITE;

    /**
     * creates an SFX slider centred at (centreX, centreY).
     *
     * @param centreX horizontal centre of the slider in world coordinates
     * @param centreY vertical centre of the slider in world coordinates
     * @param trackWidth pixel span of the full draggable range
     * @param initialValue starting volume level in [0.0, 1.0]
     */
    public SfxSlider(float centreX, float centreY, float trackWidth, float initialValue) {
        super(centreX, centreY, trackWidth, initialValue);
    }

    @Override
    protected float getMinValue() { return 0f; }

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

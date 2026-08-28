package com.p1_7.game.ui;

import com.badlogic.gdx.graphics.Color;

/**
 * Horizontal volume slider. Value range is [0.0, 1.0] with a blue colour scheme.
 */
public final class VolumeSlider extends Slider {

    private static final Color COLOUR_TRACK     = new Color(0.20f, 0.20f, 0.55f, 1f);
    private static final Color COLOUR_FILLED    = new Color(0.80f, 0.80f, 1.00f, 1f);
    private static final Color COLOUR_KNOB      = new Color(0.35f, 0.35f, 0.80f, 1f);
    private static final Color COLOUR_KNOB_DRAG = Color.WHITE;

    /**
     * Creates a volume slider centred at (centreX, centreY).
     *
     * @param centreX      horizontal centre of the slider in world coordinates
     * @param centreY      vertical centre of the slider in world coordinates
     * @param trackWidth   pixel span of the full draggable range
     * @param initialValue starting volume level in [0.0, 1.0]
     */
    public VolumeSlider(float centreX, float centreY, float trackWidth, float initialValue) {
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

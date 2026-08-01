package com.p1_7.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.game.spatial.IDisposable;
import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.abstractengine.input.InputState;
import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.render.IRenderable;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.spatial.Transform2D;
import com.p1_7.game.input.GameActions;
import com.p1_7.game.input.ICursorSource;
import com.p1_7.game.platform.GdxDrawContext;

/**
 * abstract base class for horizontal slider UI entities. handles drag input, value clamping,
 * track/knob rendering, and the moved flag; subclasses supply the value range and colour scheme.
 *
 * call updateInput() once per frame, then check hasMoved() and getValue().
 * call resetMoved() after applying the value.
 */
public abstract class Slider extends Entity implements IRenderable, IDisposable {

    /** radius of the draggable knob */
    private static final float KNOB_RADIUS  = 14f;
    /** half-height of the flat track bar */
    private static final float TRACK_HALF_H = 5f;

    private final Transform2D transform;
    private final float       trackLeft;
    private final float       trackCentreY;
    private final float       trackWidth;
    private final float       knobRadius;

    private float   value;
    private boolean dragging = false;
    private boolean moved    = false;

    /**
     * builds shared slider state from centre position, track span, and initial value.
     *
     * @param centreX      horizontal centre of the slider in world coordinates
     * @param centreY      vertical centre of the slider in world coordinates
     * @param trackWidth   pixel span of the full draggable range
     * @param initialValue starting value clamped to [getMinValue(), getMaxValue()]
     */
    protected Slider(float centreX, float centreY, float trackWidth, float initialValue) {
        this.trackLeft    = centreX - trackWidth / 2f;
        this.trackCentreY = centreY;
        this.trackWidth   = trackWidth;
        this.knobRadius   = KNOB_RADIUS;
        this.value        = clampValue(initialValue);
        this.transform    = new Transform2D(
            trackLeft,
            centreY - KNOB_RADIUS,
            trackWidth,
            KNOB_RADIUS * 2f
        );
    }

    // abstract range and colour getters ───────────────────────

    /** returns the lower bound of this slider's value range. */
    protected abstract float getMinValue();

    /** returns the upper bound of this slider's value range. */
    protected abstract float getMaxValue();

    /** returns the colour used for the unfilled portion of the track. */
    protected abstract Color getTrackColour();

    /** returns the colour used for the filled portion of the track. */
    protected abstract Color getFilledColour();

    /** returns the knob colour in its idle state. */
    protected abstract Color getKnobColour();

    /** returns the knob colour while it is being dragged. */
    protected abstract Color getKnobDragColour();

    // concrete input handling ──────────────────────────────────

    /**
     * polls cursor position and drag state. does not mutate any external state.
     * call once per frame from the scene's update().
     *
     * @param cursor     the world-space cursor source (Y-flip already applied)
     * @param inputQuery the logical input query for this frame
     */
    public void updateInput(ICursorSource cursor, IInputQuery inputQuery) {
        float mx        = cursor.getCursorX();
        float my        = cursor.getCursorY();
        float knobX     = trackLeft + normalizeValue(value) * trackWidth;
        InputState pointerState = inputQuery.getActionState(GameActions.POINTER_PRIMARY);

        moved = false;

        if (pointerState == InputState.PRESSED) {
            float dx        = mx - knobX;
            float dy        = my - trackCentreY;
            float hitRadius = knobRadius * 1.5f;
            if (dx * dx + dy * dy <= hitRadius * hitRadius) {
                dragging = true;
            }
        }

        if (dragging && (pointerState == InputState.PRESSED || pointerState == InputState.HELD)) {
            float clampedTrackX = Math.max(trackLeft, Math.min(trackLeft + trackWidth, mx));
            float normalized    = (clampedTrackX - trackLeft) / trackWidth;
            value = clampValue(getMinValue() + normalized * (getMaxValue() - getMinValue()));
            moved = true;
        }

        if (pointerState == null || pointerState == InputState.RELEASED) {
            dragging = false;
        }
    }

    /** returns the current value within [getMinValue(), getMaxValue()]. */
    public float getValue() { return value; }

    /** returns true if the slider value changed this frame. */
    public boolean hasMoved() { return moved; }

    /** clears the moved flag — call after handling the value change. */
    public void resetMoved() { moved = false; }

    /** returns null — sliders manage no asset files. */
    @Override
    public String getAssetPath() { return null; }

    @Override
    public ITransform getTransform() { return transform; }

    /**
     * draws the track background, the filled portion, and the knob using the subclass colour scheme.
     *
     * @param ctx the draw context for this frame
     */
    @Override
    public void render(IDrawContext ctx) {
        GdxDrawContext gdxCtx = (GdxDrawContext) ctx;
        float normalized      = normalizeValue(value);
        float knobX           = trackLeft + normalized * trackWidth;

        gdxCtx.rect(getTrackColour(),  trackLeft, trackCentreY - TRACK_HALF_H,
                    trackWidth,                   TRACK_HALF_H * 2f, true);
        gdxCtx.rect(getFilledColour(), trackLeft, trackCentreY - TRACK_HALF_H,
                    normalized * trackWidth,      TRACK_HALF_H * 2f, true);
        gdxCtx.circle(dragging ? getKnobDragColour() : getKnobColour(),
                      knobX, trackCentreY, knobRadius, true);
    }

    /** no-op — sliders own no GPU resources. */
    @Override
    public void dispose() { }

    /**
     * maps a raw value to a normalised [0, 1] track position.
     *
     * @param rawValue the value in [getMinValue(), getMaxValue()]
     * @return position along the track from 0 (left) to 1 (right)
     */
    protected float normalizeValue(float rawValue) {
        return (rawValue - getMinValue()) / (getMaxValue() - getMinValue());
    }

    /**
     * clamps a value to the valid range [getMinValue(), getMaxValue()].
     *
     * @param rawValue the value to clamp
     * @return the clamped value
     */
    protected float clampValue(float rawValue) {
        return Math.max(getMinValue(), Math.min(getMaxValue(), rawValue));
    }
}

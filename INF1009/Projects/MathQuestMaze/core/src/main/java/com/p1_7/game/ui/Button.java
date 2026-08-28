package com.p1_7.game.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.game.audio.IAudioManager;
import com.p1_7.game.spatial.IDisposable;
import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.abstractengine.input.InputState;
import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.render.IRenderable;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.spatial.Transform2D;
import com.p1_7.game.input.GameActions;
import com.p1_7.game.input.ICursorSource;

/**
 * abstract base class for all button-style UI entities. manages transform layout, hit-testing,
 * and click detection so that subclasses only need to supply rendering and resource cleanup.
 *
 * call updateInput() once per frame, then check isClicked().
 * call resetClick() after handling the action so it fires only once.
 */
public abstract class Button extends Entity implements IRenderable, IDisposable {

    public static final long SELECT_SOUND_COOLDOWN_MS = 75L;

    /** standard button width shared by all button subclasses */
    public static final float BUTTON_WIDTH  = 260f;
    /** standard button height shared by all button subclasses */
    public static final float BUTTON_HEIGHT = 55f;

    protected final Transform2D transform;
    protected final String      label;
    protected final BitmapFont  font;
    protected final GlyphLayout layout;

    /** true while the cursor is inside the button bounds */
    protected boolean hovered = false;

    private boolean clicked = false;

    /**
     * builds the shared button state from centre position and font.
     *
     * @param label   text shown on the button
     * @param centreX horizontal centre in world coordinates
     * @param centreY vertical centre in world coordinates
     * @param font    BitmapFont owned by the scene, not disposed by this button
     */
    protected Button(String label, float centreX, float centreY, BitmapFont font) {
        float x = centreX - BUTTON_WIDTH  / 2f;
        float y = centreY - BUTTON_HEIGHT / 2f;
        this.transform = new Transform2D(x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
        this.label     = label;
        this.font      = font;
        this.layout    = new GlyphLayout(font, label);
    }

    /**
     * polls cursor position and click state.
     * call once per frame from the scene's update().
     *
     * @param cursor     the world-space cursor source (Y-flip already applied)
     * @param inputQuery the logical input query for this frame
     */
    public void updateInput(ICursorSource cursor, IInputQuery inputQuery) {
        updateInput(cursor, inputQuery, null);
    }

    /**
     * polls cursor position and click state, optionally triggering UI sound effects.
     * call once per frame from the scene's update().
     *
     * @param cursor       the world-space cursor source (Y-flip already applied)
     * @param inputQuery   the logical input query for this frame
     * @param audioManager audio service used for hover/select feedback; may be null
     */
    public void updateInput(ICursorSource cursor, IInputQuery inputQuery, IAudioManager audioManager) {
        float mx = cursor.getCursorX();
        float my = cursor.getCursorY();

        float bx = transform.getPosition(0);
        float by = transform.getPosition(1);

        hovered = mx >= bx && mx <= bx + BUTTON_WIDTH
               && my >= by && my <= by + BUTTON_HEIGHT;

        if (hovered
            && inputQuery.getActionState(GameActions.POINTER_PRIMARY) == InputState.PRESSED) {
            clicked = true;
            if (audioManager != null) {
                audioManager.playSound("select", SELECT_SOUND_COOLDOWN_MS);
            }
        }
    }

    /** returns true if the button was clicked this frame. */
    public boolean isClicked() { return clicked; }

    /** clears the click flag — call after handling the action. */
    public void resetClick() { clicked = false; }

    /** returns null — subclasses manage their own asset loading. */
    @Override
    public String getAssetPath() { return null; }

    @Override
    public ITransform getTransform() { return transform; }

    @Override
    public abstract void render(IDrawContext ctx);

    @Override
    public abstract void dispose();
}

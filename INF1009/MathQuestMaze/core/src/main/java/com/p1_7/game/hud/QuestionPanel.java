package com.p1_7.game.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.p1_7.abstractengine.engine.IUpdatable;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.render.IRenderable;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.Settings;
import com.p1_7.game.spatial.Transform2D;
import com.p1_7.game.platform.GdxDrawContext;

/**
 * animated question panel entity that slides from the screen centre to the bottom
 * of the screen during the QUESTION_INTRO phase, then holds so the player can read
 * the question before the phase advances.
 *
 * the panel renders a semi-transparent dark background with the current question
 * text centred within it.
 */
public class QuestionPanel extends Entity implements IRenderable, IUpdatable {

    /** panel width in pixels — spans the full screen width */
    private static final float PANEL_W = Settings.getWindowWidth();

    /** panel height in pixels */
    private static final float PANEL_H = 60f;

    /** fixed horizontal position — flush with the left edge of the screen */
    private static final float PANEL_X = 0f;

    /** starting y position — panel bottom at approximately screen centre */
    private static final float START_Y = 330f;

    /** resting y position — panel bottom just above the bottom wall */
    private static final float END_Y = 40f;

    /** pause before the slide begins, giving the player time to read the question */
    private static final float ANIM_START_DELAY = 1.5f;

    /** duration of the slide animation in seconds */
    private static final float ANIM_DURATION = 1.0f;

    // TODO palette: replace hardcoded colour with a shared palette entry
    /** brighter slate-blue panel background so the prompt stays readable */
    private static final Color PANEL_BG = new Color(0.24f, 0.29f, 0.40f, 0.94f);

    /** transform used to satisfy ITransformable; y-axis is kept in sync with currentY */
    private final Transform2D transform;

    /** font used to render the question text */
    private final BitmapFont font;

    /** the question string currently displayed on the panel */
    private String questionText;

    /** current y position of the panel; interpolated between START_Y and END_Y */
    private float currentY;

    /** countdown before the slide begins; slide does not start until this reaches zero */
    private float delayTimer;

    /** animation progress in the range 0..1; 1 means the animation is complete */
    private float animProgress;

    /**
     * cached glyph layout for questionText; recomputed in beginIntro so render never allocates.
     * GlyphLayout is reusable — setText() updates it in-place.
     */
    private final GlyphLayout layout = new GlyphLayout();

    /**
     * constructs a question panel with the given font.
     *
     * the panel starts at rest (END_Y) with no text and animation complete,
     * so it is invisible until beginIntro is called.
     *
     * @param font the bitmap font to use for question text rendering
     */
    public QuestionPanel(BitmapFont font) {
        this.font         = font;
        this.transform    = new Transform2D(PANEL_X, END_Y, PANEL_W, PANEL_H);
        this.currentY     = END_Y;
        this.delayTimer   = 0f;
        this.animProgress = 1f;
        this.questionText = "";
    }

    /**
     * resets the panel to the top of its animation path and starts the slide.
     *
     * call this at the start of each QUESTION_INTRO phase to show the new question.
     *
     * @param questionText the question string to display during the slide
     */
    public void beginIntro(String questionText) {
        this.questionText = questionText;
        // recompute layout once per question so render() never allocates
        layout.setText(font, questionText);
        this.currentY     = START_Y;
        this.delayTimer   = ANIM_START_DELAY;
        this.animProgress = 0f;
    }

    /**
     * advances the linear slide animation by deltaTime seconds.
     *
     * no-op once animProgress reaches 1 (animation already complete).
     *
     * @param deltaTime elapsed seconds since the last frame
     */
    public void update(float deltaTime) {
        if (animProgress >= 1f) return;
        // wait for the pre-slide delay before starting the animation
        if (delayTimer > 0f) {
            delayTimer = Math.max(0f, delayTimer - deltaTime);
            return;
        }
        animProgress = Math.min(1f, animProgress + deltaTime / ANIM_DURATION);
        currentY = START_Y + (END_Y - START_Y) * animProgress;
        // keep transform in sync so ITransformable consumers see the correct position
        transform.setPosition(1, currentY);
    }

    @Override
    public String getAssetPath() {
        return null;
    }

    @Override
    public ITransform getTransform() {
        return transform;
    }

    /**
     * draws the dark panel background and centres the question text within it.
     *
     * during the pre-slide delay the panel is intentionally visible at START_Y so the
     * player has time to read the question before it slides to the bottom of the screen.
     *
     * @param ctx the draw context for this frame
     */
    @Override
    public void render(IDrawContext ctx) {
        GdxDrawContext gdx = (GdxDrawContext) ctx;
        gdx.drawTintedQuad(PANEL_BG, PANEL_X, currentY, PANEL_W, PANEL_H);
        // layout was pre-computed in beginIntro — no allocation here
        gdx.drawFont(font, questionText,
            PANEL_X + (PANEL_W - layout.width) / 2f,
            currentY + PANEL_H / 2f + layout.height / 2f);
    }
}

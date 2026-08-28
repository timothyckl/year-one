package com.p1_7.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.game.platform.GdxDrawContext;

/**
 * reusable UI button for menus. supports two modes:
 *
 *   TEXTURED   — draws a PNG for the button body; hover applies a colour tint.
 *               create via MenuButton.withTexture(...).
 *
 *   PROCEDURAL — draws a plain coloured rectangle (no image files needed).
 *               create via new MenuButton(...).
 *
 * in both modes the label is drawn centred over the button with the
 * supplied BitmapFont. the font is owned by the scene, not the button.
 *
 * call updateInput() every frame, then check isClicked().
 * call resetClick() after handling the action so it fires only once.
 * call dispose() inside the scene's onExit() when the button was created via
 * withTexture(); it is a safe no-op for procedurally constructed instances.
 */
public class MenuButton extends Button {

    // procedural fallback colours ─────────────────────────────
    private static final Color COLOUR_NORMAL = new Color(0.20f, 0.20f, 0.55f, 1f);
    private static final Color COLOUR_HOVER  = new Color(0.35f, 0.35f, 0.80f, 1f);
    private static final Color COLOUR_BORDER = new Color(0.80f, 0.80f, 1.00f, 1f);
    private static final float BORDER_THICK  = 3f;

    // textured hover tints ────────────────────────────────────
    private static final Color TINT_NORMAL = Color.WHITE.cpy();
    private static final Color TINT_HOVER  = new Color(0.75f, 0.88f, 1.0f, 1f);

    /** null in procedural mode */
    private final Texture texNormal;
    /** null in procedural mode; may equal texNormal if no separate hover image */
    private final Texture texHover;

    // factory method (textured) ───────────────────────────────

    /**
     * creates a textured button that loads its own PNG files.
     *
     * @param label      text shown on the button
     * @param centreX    horizontal centre in world coordinates
     * @param centreY    vertical centre in world coordinates
     * @param font       BitmapFont owned by the scene (not disposed by this button)
     * @param normalPath asset path for the normal state, e.g. "menu/button.png"
     * @param hoverPath  asset path for the hover state, e.g. "menu/button_hover.png";
     *                   pass null to reuse normalPath with a colour tint on hover
     */
    public static MenuButton withTexture(String label,
                                         float centreX, float centreY,
                                         BitmapFont font,
                                         String normalPath,
                                         String hoverPath) {
        Texture normal = new Texture(Gdx.files.internal(normalPath));
        normal.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        Texture hover = (hoverPath != null)
                ? new Texture(Gdx.files.internal(hoverPath))
                : normal;   // reuse normal, tinted in render()
        if (hoverPath != null) {
            hover.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }

        return new MenuButton(label, centreX, centreY, font, normal, hover);
    }

    // constructors ────────────────────────────────────────────

    /**
     * procedural constructor — no image assets required.
     *
     * @param label   text shown on the button
     * @param centreX horizontal centre in world coordinates
     * @param centreY vertical centre in world coordinates
     * @param font    BitmapFont owned by the scene
     */
    public MenuButton(String label, float centreX, float centreY, BitmapFont font) {
        this(label, centreX, centreY, font, null, null);
    }

    /** full internal constructor used by both modes. */
    private MenuButton(String label, float centreX, float centreY,
                       BitmapFont font, Texture normal, Texture hover) {
        super(label, centreX, centreY, font);
        this.texNormal = normal;
        this.texHover  = hover;
    }

    // IRenderable ─────────────────────────────────────────────

    /**
     * draws the button background (textured or procedural) then the label.
     * pass transitions are fully managed by GdxDrawContext; no begin/end calls here.
     *
     * @param ctx the draw context for this frame
     */
    @Override
    public void render(IDrawContext ctx) {
        GdxDrawContext gdxCtx = (GdxDrawContext) ctx;

        float x = transform.getPosition(0);
        float y = transform.getPosition(1);

        if (texNormal != null) {
            // textured mode ─────────────────────────────────
            gdxCtx.drawRawTexture(hovered ? texHover : texNormal,
                                  hovered ? TINT_HOVER : TINT_NORMAL,
                                  x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
        } else {
            // procedural fallback ───────────────────────────
            gdxCtx.rect(COLOUR_BORDER,
                        x - BORDER_THICK, y - BORDER_THICK,
                        BUTTON_WIDTH  + BORDER_THICK * 2,
                        BUTTON_HEIGHT + BORDER_THICK * 2, true);
            gdxCtx.rect(hovered ? COLOUR_HOVER : COLOUR_NORMAL,
                        x, y, BUTTON_WIDTH, BUTTON_HEIGHT, true);
        }

        // label drawn in both modes, centred over the button body
        layout.setText(font, label);
        gdxCtx.drawFont(font, label,
            x + (BUTTON_WIDTH  - layout.width)  / 2f,
            y + (BUTTON_HEIGHT + layout.height) / 2f);
    }

    /**
     * releases textures owned by this button.
     * the font is NOT disposed — the scene owns it.
     * safe no-op for procedurally constructed instances (no textures to release).
     */
    @Override
    public void dispose() {
        if (texNormal != null) texNormal.dispose();
        if (texHover != null && texHover != texNormal) texHover.dispose();
    }
}

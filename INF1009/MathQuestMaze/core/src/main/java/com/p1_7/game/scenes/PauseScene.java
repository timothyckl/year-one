package com.p1_7.game.scenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.p1_7.abstractengine.input.IInputExtensionRegistry;
import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.abstractengine.input.InputState;
import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.render.IRenderable;
import com.p1_7.abstractengine.render.IRenderQueue;
import com.p1_7.abstractengine.scene.Scene;
import com.p1_7.abstractengine.scene.SceneContext;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.Settings;
import com.p1_7.game.audio.IAudioManager;
import com.p1_7.game.spatial.Transform2D;
import com.p1_7.game.input.GameActions;
import com.p1_7.game.input.ICursorSource;
import com.p1_7.game.font.IFontManager;
import com.p1_7.game.platform.GdxDrawContext;
import com.p1_7.game.ui.MenuButton;
import com.p1_7.game.ui.Text;

/**
 * overlay scene shown when ESC is pressed during the CHOOSING phase.
 *
 * renders a dim quad over the frozen game world with a centred panel containing
 * a PAUSED title and three buttons: Resume, Settings, and Return to Menu.
 * the game scene remains suspended and visible behind this scene at all times.
 */
public class PauseScene extends Scene {

    private static final String BTN_ASSET   = "menu/button.png";
    private static final String HOVER_ASSET = "menu/button_hover.png";

    private static final float PANEL_WIDTH    = 400f;
    private static final float PANEL_HEIGHT   = 340f;
    private static final float BUTTON_SPACING = 80f;

    // pre-allocated colours to avoid per-frame allocation
    private static final Color DIM_COLOUR   = new Color(0f, 0f, 0f, 0.6f);
    private static final Color PANEL_COLOUR = new Color(0.10f, 0.13f, 0.20f, 0.95f);

    private float centreX;
    private float centreY;

    // cached on onEnter so buttons can navigate back without a context parameter
    private String suspendedKey;

    private BitmapFont  titleFont;
    private BitmapFont  buttonFont;
    private IRenderable dimOverlay;
    private Text        pauseTitle;
    private MenuButton  resumeButton;
    private MenuButton  settingsButton;
    private MenuButton  returnToMenuButton;

    public PauseScene() {
        this.name = "pause";
    }

    /**
     * initialises fonts, the dim overlay, title text, and the three menu buttons.
     *
     * @param context the engine service context
     */
    @Override
    public void onEnter(SceneContext context) {
        centreX = Settings.getWindowWidth()  / 2f;
        centreY = Settings.getWindowHeight() / 2f;

        // cache the suspended key so navigation helpers can reference it without context
        suspendedKey = context.getSuspendedSceneKey();

        IFontManager fontManager = context.get(IFontManager.class);
        titleFont  = fontManager.getGoldDisplayFont(48);
        buttonFont = fontManager.getDarkTextFont(26);

        dimOverlay = buildDimOverlay();
        pauseTitle = new Text("PAUSED", centreX, centreY + 130f, titleFont);

        resumeButton = MenuButton.withTexture(
            "RESUME", centreX, centreY + 60f,
            buttonFont, BTN_ASSET, HOVER_ASSET);
        settingsButton = MenuButton.withTexture(
            "SETTINGS", centreX, centreY - 20f,
            buttonFont, BTN_ASSET, HOVER_ASSET);
        returnToMenuButton = MenuButton.withTexture(
            "MAIN MENU", centreX, centreY - 100f,
            buttonFont, BTN_ASSET, HOVER_ASSET);
    }

    /**
     * releases all owned resources and nulls scene-local references.
     *
     * @param context the engine service context
     */
    @Override
    public void onExit(SceneContext context) {
        if (resumeButton       != null) resumeButton.dispose();
        if (settingsButton     != null) settingsButton.dispose();
        if (returnToMenuButton != null) returnToMenuButton.dispose();

        dimOverlay         = null;
        pauseTitle         = null;
        resumeButton       = null;
        settingsButton     = null;
        returnToMenuButton = null;
        titleFont          = null;
        buttonFont         = null;
        suspendedKey       = null;
    }

    /**
     * handles ESC to resume, and polls button clicks for all three actions.
     *
     * @param deltaTime elapsed seconds since the last frame
     * @param context   the engine service context
     */
    @Override
    public void update(float deltaTime, SceneContext context) {
        IInputQuery inputQuery = context.get(IInputQuery.class);
        IAudioManager audio = context.get(IAudioManager.class);

        // esc while paused resumes the game, mirroring the key that opened the menu
        if (inputQuery.getActionState(GameActions.MENU_BACK) == InputState.PRESSED) {
            resumeGame(context);
            return;
        }

        IInputExtensionRegistry reg = context.get(IInputExtensionRegistry.class);
        ICursorSource cursor = reg.hasExtension(ICursorSource.class)
            ? reg.getExtension(ICursorSource.class) : null;
        if (cursor == null) return;

        resumeButton.updateInput(cursor, inputQuery, audio);
        settingsButton.updateInput(cursor, inputQuery, audio);
        returnToMenuButton.updateInput(cursor, inputQuery, audio);

        if (resumeButton.isClicked()) {
            resumeButton.resetClick();
            resumeGame(context);
        } else if (settingsButton.isClicked()) {
            settingsButton.resetClick();
            context.changeScene("settings");
        } else if (returnToMenuButton.isClicked()) {
            returnToMenuButton.resetClick();
            returnToMenu(context);
        }
    }

    /**
     * queues the dim overlay, title, and all three buttons for rendering.
     *
     * @param renderQueue the render queue accumulator for this frame
     */
    @Override
    public void submitRenderable(IRenderQueue renderQueue) {
        if (dimOverlay == null) return; // scene has already exited
        // SceneManager already submitted the frozen game world before calling this;
        // queue the dim layer and panel on top of it
        renderQueue.queue(dimOverlay);
        renderQueue.queue(pauseTitle);
        renderQueue.queue(resumeButton);
        renderQueue.queue(settingsButton);
        renderQueue.queue(returnToMenuButton);
    }

    /**
     * resumes the suspended game scene and collapses the pause overlay.
     *
     * @param context the engine service context
     */
    private void resumeGame(SceneContext context) {
        if (suspendedKey == null) return; // guard against calls after onExit
        // SceneManager detects that the target matches suspendedSceneKey and calls onResume()
        context.changeScene(suspendedKey);
    }

    /**
     * exits the suspended game scene explicitly, clears the suspension record,
     * then navigates to the main menu.
     *
     * SceneManager's non-resume changeScene only exits the current (overlay) scene;
     * it cannot tell whether a suspended scene should be discarded or preserved, so
     * the caller is responsible for cleanup when leaving the overlay chain entirely.
     *
     * @param context the engine service context
     */
    private void returnToMenu(SceneContext context) {
        if (suspendedKey != null) {
            Scene suspended = context.getScene(suspendedKey);
            if (suspended != null) {
                suspended.onExit(context);
            }
            context.clearSuspendedScene();
        }
        context.changeScene("menu");
    }

    /**
     * builds a renderable that draws a full-screen dim quad followed by the centred panel background.
     * uses the same anonymous IRenderable pattern as BrightnessOverlay.
     *
     * @return the combined dim-and-panel renderable
     */
    private IRenderable buildDimOverlay() {
        final Transform2D fullScreen = new Transform2D(
            0f, 0f, Settings.getWindowWidth(), Settings.getWindowHeight());
        final float panelX = centreX - PANEL_WIDTH  / 2f;
        final float panelY = centreY - PANEL_HEIGHT / 2f;

        return new IRenderable() {
            @Override public String     getAssetPath() { return null; }
            @Override public ITransform getTransform() { return fullScreen; }

            @Override
            public void render(IDrawContext ctx) {
                GdxDrawContext gdx = (GdxDrawContext) ctx;
                // dim the frozen game world so the pause menu stands out
                gdx.drawTintedQuad(DIM_COLOUR,
                    0f, 0f, Settings.getWindowWidth(), Settings.getWindowHeight());
                // solid panel backing for the title and buttons
                gdx.drawTintedQuad(PANEL_COLOUR, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);
            }
        };
    }
}

package com.p1_7.game.scenes;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.p1_7.abstractengine.input.IInputExtensionRegistry;
import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.abstractengine.input.InputState;
import com.p1_7.abstractengine.render.IRenderQueue;
import com.p1_7.abstractengine.scene.Scene;
import com.p1_7.abstractengine.scene.SceneContext;
import com.p1_7.game.Settings;
import com.p1_7.game.ui.BackgroundImage;
import com.p1_7.game.ui.BrightnessOverlay;
import com.p1_7.game.ui.MenuButton;
import com.p1_7.game.ui.Text;
import com.p1_7.game.input.GameActions;
import com.p1_7.game.input.ICursorSource;
import com.p1_7.game.audio.IAudioManager;
import com.p1_7.game.font.IFontManager;

/**
 * Shown when the player's health reaches zero and the game ends.
 *
 * Offers a RETRY button to restart the game and a MAIN MENU button
 * to return to the main menu.
 */
public class GameOverScene extends Scene {

    private static final float INPUT_COOLDOWN_SECONDS = 0.18f;
    private static final String BG_ASSET = "background.png";
    private static final String BTN_ASSET = "menu/button.png";
    private static final String HOVER_ASSET = "menu/button_hover.png";

    // ui elements ───────────────────────────────────────────────────────────
    private BitmapFont titleFont;
    private BitmapFont promptFont;
    private BitmapFont buttonFont;
    private BackgroundImage background;
    private Text title;
    private Text promptStatus;
    private Text hintSpace;
    private Text hintEsc;
    private MenuButton retryButton;
    private MenuButton mainMenuButton;
    private BrightnessOverlay brightnessOverlay;

    private float inputCooldown;

    public GameOverScene() {
        this.name = "game-over";
    }

    /**
     * initialises fonts, input, and all UI elements for the game-over screen.
     *
     * @param context the scene context used to resolve shared services
     */
    @Override
    public void onEnter(SceneContext context) {
        context.get(IAudioManager.class).playMusic("game-over", false);
        IFontManager fontManager = context.get(IFontManager.class);
        titleFont = fontManager.getGoldDisplayFont(54);
        promptFont = fontManager.getPromptFont();
        buttonFont = fontManager.getDarkTextFont(22);

        float cx = Settings.getWindowWidth() / 2f;
        float cy = Settings.getWindowHeight() / 2f;
        background = new BackgroundImage(BG_ASSET);
        title = new Text("GAME OVER", cx, cy + 120f, titleFont);
        promptStatus = new Text("Better luck next time!", cx, cy + 55f, promptFont);
        retryButton = MenuButton.withTexture("RETRY", cx, cy - 10f, buttonFont, BTN_ASSET, HOVER_ASSET);
        mainMenuButton = MenuButton.withTexture("MAIN MENU", cx, cy - 85f, buttonFont, BTN_ASSET, HOVER_ASSET);
        hintSpace = new Text("SPACE - Retry", cx, cy - 175f, promptFont);
        hintEsc = new Text("ESC - Main Menu", cx, cy - 220f, promptFont);
        brightnessOverlay = new BrightnessOverlay();

        inputCooldown = INPUT_COOLDOWN_SECONDS;
    }

    /**
     * disposes resources and releases all field references to aid garbage collection.
     *
     * @param context the scene context (not used during disposal)
     */
    @Override
    public void onExit(SceneContext context) {
        if (retryButton != null) retryButton.dispose();
        if (mainMenuButton != null) mainMenuButton.dispose();
        background   = null;
        title        = null;
        promptStatus = null;
        hintSpace    = null;
        hintEsc      = null;
        retryButton    = null;
        mainMenuButton = null;
        brightnessOverlay = null;
        titleFont  = null;
        promptFont = null;
        buttonFont = null;
    }

    /**
     * handles input cooldown and button/keyboard interactions each frame.
     *
     * @param deltaTime elapsed time in seconds since the last frame
     * @param context   the scene context used to trigger scene changes
     */
    @Override
    public void update(float deltaTime, SceneContext context) {
        if (inputCooldown > 0f) {
            inputCooldown -= deltaTime;
            return;
        }

        IInputQuery inputQuery = context.get(IInputQuery.class);
        IAudioManager audio = context.get(IAudioManager.class);
        IInputExtensionRegistry inputRegistry = context.get(IInputExtensionRegistry.class);
        ICursorSource cursorSource = inputRegistry.hasExtension(ICursorSource.class)
            ? inputRegistry.getExtension(ICursorSource.class) : null;
        // cursorSource stays null if not registered; guard below handles it cleanly
        if (cursorSource != null) {
            retryButton.updateInput(cursorSource, inputQuery, audio);
            mainMenuButton.updateInput(cursorSource, inputQuery, audio);
        }

        boolean backPressed = inputQuery.getActionState(GameActions.MENU_BACK) == InputState.PRESSED;
        boolean confirmPressed =
            inputQuery.getActionState(GameActions.MENU_CONFIRM) == InputState.PRESSED;

        if (backPressed || mainMenuButton.isClicked()) {
            mainMenuButton.resetClick();
            context.changeScene("menu");
            return;
        }

        if (confirmPressed || retryButton.isClicked()) {
            retryButton.resetClick();
            context.changeScene("game");
        }
    }

    /**
     * queues all UI elements to the render queue in painter's order.
     *
     * @param renderQueue the render queue accumulator for this frame
     */
    @Override
    public void submitRenderable(IRenderQueue renderQueue) {
        renderQueue.queue(background);
        renderQueue.queue(title);
        renderQueue.queue(promptStatus);
        renderQueue.queue(retryButton);
        renderQueue.queue(mainMenuButton);
        renderQueue.queue(hintSpace);
        renderQueue.queue(hintEsc);
        renderQueue.queue(brightnessOverlay);
    }
}

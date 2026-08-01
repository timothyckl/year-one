package com.p1_7.game.scenes;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.p1_7.abstractengine.input.IInputExtensionRegistry;
import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.abstractengine.input.InputState;
import com.p1_7.abstractengine.render.IRenderQueue;
import com.p1_7.abstractengine.scene.Scene;
import com.p1_7.abstractengine.scene.SceneContext;
import com.p1_7.game.Settings;
import com.p1_7.game.math.Difficulty;
import com.p1_7.game.ui.BackgroundImage;
import com.p1_7.game.ui.BrightnessOverlay;
import com.p1_7.game.ui.MenuButton;
import com.p1_7.game.ui.Text;
import com.p1_7.game.input.GameActions;
import com.p1_7.game.input.ICursorSource;
import com.p1_7.game.audio.IAudioManager;
import com.p1_7.game.font.IFontManager;
import com.p1_7.game.round.ILevelOrchestrator;

/**
 * shown after the player clears a level; displays the current level number, a prompt
 * for the next level, and navigation buttons.
 *
 * advances the difficulty via ILevelOrchestrator before transitioning to the game scene.
 * if the player has cleared the final level, the continue button becomes "play again"
 * and restarts from the first difficulty rather than advancing.
 */
public class LevelCompleteScene extends Scene {

    private static final int MAX_LEVEL = 3;
    private static final float INPUT_COOLDOWN_SECONDS = 0.18f;
    private static final String BG_ASSET = "background.png";
    private static final String BTN_ASSET = "menu/button.png";
    private static final String HOVER_ASSET = "menu/button_hover.png";

    private BitmapFont titleFont;
    private BitmapFont promptFont;
    private BitmapFont buttonFont;
    private BackgroundImage background;
    private Text title;
    private Text promptStatus;
    private Text hintSpace;
    private Text hintEsc;
    private MenuButton continueButton;
    private MenuButton mainMenuButton;
    private BrightnessOverlay brightnessOverlay;
    private float inputCooldown;

    public LevelCompleteScene() {
        this.name = "level-complete";
    }

    @Override
    public void onEnter(SceneContext context) {
        context.get(IAudioManager.class).playMusic("level-complete", false);
        IFontManager fontManager = context.get(IFontManager.class);
        titleFont = fontManager.getGoldDisplayFont(54);
        promptFont = fontManager.getPromptFont();
        buttonFont = fontManager.getDarkTextFont(22);

        float cx = Settings.getWindowWidth() / 2f;
        float cy = Settings.getWindowHeight() / 2f;
        Difficulty currentDifficulty = context.get(ILevelOrchestrator.class).getCurrentDifficulty();
        int currentLevel = getLevelNumber(currentDifficulty);
        boolean lastLevel = isLastLevel(currentDifficulty);
        int nextLevel = lastLevel ? 1 : currentLevel + 1;
        String continueLabel = lastLevel ? "PLAY AGAIN" : "CONTINUE";
        String spaceHint = lastLevel ? "SPACE - Play Again" : "SPACE - Continue";
        background = new BackgroundImage(BG_ASSET);
        title = new Text("LEVEL " + currentLevel + " COMPLETE!", cx, cy + 120f, titleFont);
        promptStatus = new Text("Next up: Level " + nextLevel, cx, cy + 55f, promptFont);
        continueButton = MenuButton.withTexture(continueLabel, cx, cy - 10f, buttonFont, BTN_ASSET, HOVER_ASSET);
        mainMenuButton = MenuButton.withTexture("MAIN MENU", cx, cy - 85f, buttonFont, BTN_ASSET, HOVER_ASSET);
        hintSpace = new Text(spaceHint, cx, cy - 175f, promptFont);
        hintEsc = new Text("ESC - Main Menu", cx, cy - 220f, promptFont);
        brightnessOverlay = new BrightnessOverlay();

        inputCooldown = INPUT_COOLDOWN_SECONDS;
    }

    @Override
    public void onExit(SceneContext context) {
        if (continueButton != null) continueButton.dispose();
        if (mainMenuButton != null) mainMenuButton.dispose();
        background        = null;
        title             = null;
        promptStatus      = null;
        hintSpace         = null;
        hintEsc           = null;
        continueButton    = null;
        mainMenuButton    = null;
        brightnessOverlay = null;
        titleFont         = null;
        promptFont        = null;
        buttonFont        = null;
    }

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
            continueButton.updateInput(cursorSource, inputQuery, audio);
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

        if (confirmPressed || continueButton.isClicked()) {
            continueButton.resetClick();
            ILevelOrchestrator orchestrator = context.get(ILevelOrchestrator.class);
            orchestrator.setCurrentDifficulty(getNextDifficulty(orchestrator.getCurrentDifficulty()));
            context.changeScene("game");
        }
    }

    @Override
    public void submitRenderable(IRenderQueue renderQueue) {
        renderQueue.queue(background);
        renderQueue.queue(title);
        renderQueue.queue(promptStatus);
        renderQueue.queue(continueButton);
        renderQueue.queue(mainMenuButton);
        renderQueue.queue(hintSpace);
        renderQueue.queue(hintEsc);
        renderQueue.queue(brightnessOverlay);
    }

    private static boolean isLastLevel(Difficulty difficulty) {
        return getLevelNumber(difficulty) >= MAX_LEVEL;
    }

    private static int getLevelNumber(Difficulty difficulty) {
        if (difficulty == Difficulty.MEDIUM) {
            return 2;
        }
        if (difficulty == Difficulty.HARD) {
            return 3;
        }
        return 1;
    }

    private static Difficulty getNextDifficulty(Difficulty currentDifficulty) {
        if (currentDifficulty == Difficulty.EASY) {
            return Difficulty.MEDIUM;
        }
        if (currentDifficulty == Difficulty.MEDIUM) {
            return Difficulty.HARD;
        }
        return Difficulty.EASY;
    }
}

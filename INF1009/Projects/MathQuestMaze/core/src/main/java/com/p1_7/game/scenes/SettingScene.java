package com.p1_7.game.scenes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.p1_7.abstractengine.input.IInputExtensionRegistry;
import com.p1_7.abstractengine.input.IInputManager;
import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.abstractengine.input.InputState;
import com.p1_7.abstractengine.render.IRenderQueue;
import com.p1_7.abstractengine.scene.Scene;
import com.p1_7.abstractengine.scene.SceneContext;
import com.p1_7.game.Settings;
import com.p1_7.game.ui.BackgroundImage;
import com.p1_7.game.ui.BrightnessOverlay;
import com.p1_7.game.ui.BrightnessSlider;
import com.p1_7.game.ui.Button;
import com.p1_7.game.ui.MenuButton;
import com.p1_7.game.ui.RemapSlot;
import com.p1_7.game.ui.SfxSlider;
import com.p1_7.game.ui.Text;
import com.p1_7.game.ui.VolumeSlider;
import com.p1_7.game.input.GameActions;
import com.p1_7.game.input.ICursorSource;
import com.p1_7.game.audio.IAudioManager;
import com.p1_7.game.font.IFontManager;

/**
 * lets the player adjust volume and brightness levels, and remap primary and alternate
 * key bindings for each game action.
 *
 * key remapping uses a raw input listener; bindings are written back to IInputManager
 * when a key is pressed.
 */
public class SettingScene extends Scene {


    private static final String BG_ASSET = "background.png";
    private static final String BTN_ASSET = "menu/button.png";
    private static final String HOVER_ASSET = "menu/button_hover.png";

    private float centreX;
    private float centreY;

    private BitmapFont headingFont;
    private BitmapFont labelFont;
    private BitmapFont tableFont;
    private BitmapFont buttonFont;

    // kept as a field because syncRemapBindings() is called via the remapInputProcessor
    // callback chain (keyDown → applyActiveRemap → syncRemapBindings), which fires outside
    // the hook lifecycle and cannot receive context as a parameter
    private IInputManager inputManager;
    private InputProcessor previousInputProcessor;
    private final InputProcessor remapInputProcessor = new InputAdapter() {
        @Override
        public boolean keyDown(int keycode) {
            if (!isListeningForRemap()) {
                return false;
            }
            if (keycode == Input.Keys.ESCAPE) {
                cancelActiveRemap();
            } else {
                applyActiveRemap(keycode);
            }
            return true;
        }
    };

    private BackgroundImage background;
    private Text heading;
    private Text musicLabel;
    private Text musicValueLabel;
    private VolumeSlider volumeSlider;
    private Text sfxLabel;
    private Text sfxValueLabel;
    private SfxSlider sfxSlider;
    private Text brightnessLabel;
    private Text brightnessValueLabel;
    private BrightnessSlider brightnessSlider;
    private Text controlsHeading;
    private Text remapHint;
    private Text actionHeader;
    private Text primaryHeader;
    private Text alternateHeader;
    private MenuButton backButton;
    private BrightnessOverlay brightnessOverlay;
    private final List<RemapSlot> remapSlots = new ArrayList<>();
    private RemapSlot activeRemapSlot;
    private RemapSlot.BindingColumn activeRemapColumn;

    // scene to return to when back is pressed; set at onEnter and cleared at onExit
    private String returnScene;

    public SettingScene() {
        this.name = "settings";
    }

    @Override
    public void onEnter(SceneContext context) {
        // determine origin so back navigation goes to the right scene regardless of
        // what happens to the suspended scene record later in the session
        returnScene = context.getSuspendedSceneKey() != null ? "pause" : "menu";
        computeSceneCenter();
        resolveSceneServices(context);
        IFontManager fontManager = context.get(IFontManager.class);
        IAudioManager audio = context.get(IAudioManager.class);
        createFonts(fontManager);
        createSceneComponents(audio);
        syncRemapBindings();
    }

    @Override
    public void onExit(SceneContext context) {
        returnScene = null;
        stopListening();
        clearRemapState();
        disposeSceneComponents();
        disposeFonts();
        clearResolvedServices();
    }

    @Override
    public void update(float deltaTime, SceneContext context) {
        if (isListeningForRemap()) {
            return;
        }

        if (handleSceneExit(context)) {
            return;
        }

        IInputExtensionRegistry inputRegistry = context.get(IInputExtensionRegistry.class);
        ICursorSource cursorSource = inputRegistry.hasExtension(ICursorSource.class)
            ? inputRegistry.getExtension(ICursorSource.class) : null;
        if (cursorSource == null) {
            return;
        }

        IInputQuery inputQuery = context.get(IInputQuery.class);
        IAudioManager audio = context.get(IAudioManager.class);
        updateSliderInputs(cursorSource, inputQuery);
        updateRemapInput(cursorSource, inputQuery, audio);
        updateBackButtonInput(cursorSource, inputQuery, audio);
        applySliderChanges(audio);
        handleBackButtonClick(context);
    }

    @Override
    public void submitRenderable(IRenderQueue renderQueue) {
        queuePrimaryRenderables(renderQueue);
        queueRemapRenderables(renderQueue);
        renderQueue.queue(backButton);
        renderQueue.queue(brightnessOverlay);
    }

    private void computeSceneCenter() {
        centreX = Settings.getWindowWidth() / 2f;
        centreY = Settings.getWindowHeight() / 2f;
    }

    private void resolveSceneServices(SceneContext context) {
        // see field declaration for why inputManager is retained across hooks
        inputManager = context.get(IInputManager.class);
    }

    private void createFonts(IFontManager fontManager) {
        headingFont = fontManager.getGoldDisplayFont(52);
        labelFont = fontManager.getDarkTextFont(24);
        tableFont = fontManager.getDarkTextFont(22);
        buttonFont = fontManager.getDarkTextFont(26);
    }

    private void createSceneComponents(IAudioManager audio) {
        float screenHeight = Settings.getWindowHeight();
        // offset 24f to the right to visually balance the label column on the left
        float sliderCenterX = centreX + 24f;
        float sliderWidth = 340f;
        float sliderLabelX = sliderCenterX - sliderWidth / 2f - 120f;
        float sliderValueX = sliderCenterX + sliderWidth / 2f + 82f;
        float backButtonY = screenHeight * 0.085f;
        float hintY = backButtonY + 54f;
        float rowSpacing = 36f;
        float firstRowY = hintY + 176f;
        float tableHeaderY = firstRowY + 46f;
        float controlsHeadingY = tableHeaderY + 36f;
        float sliderRowGap = 74f;
        float brightnessRowY = controlsHeadingY + 64f;
        float sfxRowY = brightnessRowY + sliderRowGap;
        float musicRowY = sfxRowY + sliderRowGap;
        float headingY = musicRowY + 78f;

        background = new BackgroundImage(BG_ASSET);
        heading = createCenteredLabel("SETTINGS", headingY, headingFont);
        musicLabel = new Text("Music", sliderLabelX, musicRowY + 10f, labelFont);
        musicValueLabel = new Text(percentText(audio.getMusicVolume()), sliderValueX, musicRowY + 10f, labelFont);
        sfxLabel = new Text("SFX", sliderLabelX, sfxRowY + 10f, labelFont);
        sfxValueLabel = new Text(percentText(audio.getSfxVolume()), sliderValueX, sfxRowY + 10f, labelFont);
        brightnessLabel = new Text("Brightness", sliderLabelX, brightnessRowY + 10f, labelFont);
        brightnessValueLabel = new Text(percentText(Settings.getBrightnessLevel()), sliderValueX, brightnessRowY + 10f, labelFont);
        controlsHeading = createCenteredLabel("CONTROLS", controlsHeadingY, buttonFont);
        remapHint = createCenteredLabel(idleRemapHintText(), hintY, tableFont);
        volumeSlider = new VolumeSlider(sliderCenterX, musicRowY, sliderWidth, audio.getMusicVolume());
        sfxSlider = new SfxSlider(sliderCenterX, sfxRowY, sliderWidth, audio.getSfxVolume());
        brightnessSlider = new BrightnessSlider(sliderCenterX, brightnessRowY, sliderWidth, Settings.getBrightnessLevel());
        backButton = MenuButton.withTexture("BACK", centreX, backButtonY, buttonFont, BTN_ASSET, HOVER_ASSET);
        brightnessOverlay = new BrightnessOverlay();

        createRemapHeaders(tableHeaderY);
        buildRemapSlots(firstRowY, rowSpacing);
    }

    private Text createCenteredLabel(String text, float centreYPosition, BitmapFont font) {
        return new Text(text, centreX, centreYPosition, font);
    }

    private void createRemapHeaders(float tableHeaderY) {
        float tableLeft = centreX - RemapSlot.TABLE_WIDTH / 2f;
        actionHeader = new Text("ACTION",
            tableLeft + RemapSlot.ACTION_COLUMN_WIDTH / 2f,
            tableHeaderY,
            tableFont);
        primaryHeader = new Text("PRIMARY",
            tableLeft + RemapSlot.ACTION_COLUMN_WIDTH + RemapSlot.CELL_GAP + RemapSlot.KEY_COLUMN_WIDTH / 2f,
            tableHeaderY,
            tableFont);
        alternateHeader = new Text("ALTERNATE",
            tableLeft + RemapSlot.ACTION_COLUMN_WIDTH + RemapSlot.CELL_GAP * 2f
                + RemapSlot.KEY_COLUMN_WIDTH * 1.5f,
            tableHeaderY,
            tableFont);
    }

    private void clearRemapState() {
        remapSlots.clear();
        activeRemapSlot = null;
        activeRemapColumn = null;
    }

    private void disposeSceneComponents() {
        if (backButton != null) {
            backButton.dispose();
        }
        background = null;
        heading = null;
        musicLabel = null;
        musicValueLabel = null;
        volumeSlider = null;
        sfxLabel = null;
        sfxValueLabel = null;
        sfxSlider = null;
        brightnessLabel = null;
        brightnessValueLabel = null;
        brightnessSlider = null;
        controlsHeading = null;
        remapHint = null;
        actionHeader = null;
        primaryHeader = null;
        alternateHeader = null;
        backButton = null;
        brightnessOverlay = null;
    }

    private void disposeFonts() {
        headingFont = null;
        labelFont = null;
        tableFont = null;
        buttonFont = null;
    }

    private void clearResolvedServices() {
        inputManager = null;
        previousInputProcessor = null;
    }

    private boolean handleSceneExit(SceneContext context) {
        IInputQuery inputQuery = context.get(IInputQuery.class);
        if (inputQuery.getActionState(GameActions.MENU_BACK) == InputState.PRESSED) {
            navigateBack(context);
            return true;
        }
        return false;
    }

    /**
     * navigates back to the scene that opened settings, determined at onEnter time.
     *
     * @param context the engine service context
     */
    private void navigateBack(SceneContext context) {
        context.changeScene(returnScene);
    }

    private void updateSliderInputs(ICursorSource cursorSource, IInputQuery inputQuery) {
        volumeSlider.updateInput(cursorSource, inputQuery);
        sfxSlider.updateInput(cursorSource, inputQuery);
        brightnessSlider.updateInput(cursorSource, inputQuery);
    }

    private void updateBackButtonInput(ICursorSource cursorSource, IInputQuery inputQuery, IAudioManager audio) {
        backButton.updateInput(cursorSource, inputQuery, audio);
    }

    private void applySliderChanges(IAudioManager audio) {
        if (volumeSlider.hasMoved()) {
            audio.setMusicVolume(volumeSlider.getValue());
            musicValueLabel.setText(percentText(audio.getMusicVolume()));
            volumeSlider.resetMoved();
        }
        if (sfxSlider.hasMoved()) {
            audio.setSfxVolume(sfxSlider.getValue());
            sfxValueLabel.setText(percentText(audio.getSfxVolume()));
            sfxSlider.resetMoved();
        }
        if (brightnessSlider.hasMoved()) {
            Settings.setBrightnessLevel(brightnessSlider.getValue());
            brightnessValueLabel.setText(percentText(Settings.getBrightnessLevel()));
            brightnessSlider.resetMoved();
        }
    }

    private void handleBackButtonClick(SceneContext context) {
        if (backButton.isClicked()) {
            backButton.resetClick();
            navigateBack(context);
        }
    }

    private void queuePrimaryRenderables(IRenderQueue renderQueue) {
        renderQueue.queue(background);
        renderQueue.queue(heading);
        renderQueue.queue(musicLabel);
        renderQueue.queue(musicValueLabel);
        renderQueue.queue(volumeSlider);
        renderQueue.queue(sfxLabel);
        renderQueue.queue(sfxValueLabel);
        renderQueue.queue(sfxSlider);
        renderQueue.queue(brightnessLabel);
        renderQueue.queue(brightnessValueLabel);
        renderQueue.queue(brightnessSlider);
        renderQueue.queue(controlsHeading);
    }

    private void queueRemapRenderables(IRenderQueue renderQueue) {
        renderQueue.queue(actionHeader);
        renderQueue.queue(primaryHeader);
        renderQueue.queue(alternateHeader);
        for (int i = 0; i < remapSlots.size(); i++) {
            renderQueue.queue(remapSlots.get(i));
        }
        renderQueue.queue(remapHint);
    }

    private String percentText(float value) {
        return Math.round(value * 100) + "%";
    }

    private String idleRemapHintText() {
        return "Click a binding to remap it";
    }

    private void buildRemapSlots(float firstRowY, float rowSpacing) {
        remapSlots.clear();
        List<GameActions.BindingSpec> bindings = GameActions.getMovementBindings();
        float rowY = firstRowY;
        for (int i = 0; i < bindings.size(); i++) {
            GameActions.BindingSpec binding = bindings.get(i);
            remapSlots.add(createRemapSlot(binding, rowY));
            rowY -= rowSpacing;
        }
    }

    private RemapSlot createRemapSlot(GameActions.BindingSpec binding, float rowCentreY) {
        List<Integer> keys = new ArrayList<>(inputManager.getKeysForAction(binding.getActionId()));
        Collections.sort(keys);

        boolean hasPrimaryDefault = keys.remove(Integer.valueOf(binding.getPrimaryKeyCode()));
        boolean hasAlternateDefault = keys.remove(Integer.valueOf(binding.getAlternateKeyCode()));

        int primaryKeyCode = hasPrimaryDefault
            ? binding.getPrimaryKeyCode()
            : takeFirstDistinctKey(keys, binding.getAlternateKeyCode(), binding.getPrimaryKeyCode());

        int alternateKeyCode = hasAlternateDefault
            ? binding.getAlternateKeyCode()
            : takeFirstDistinctKey(keys, primaryKeyCode, binding.getAlternateKeyCode());

        return new RemapSlot(
            binding.getLabel(),
            binding.getActionId(),
            primaryKeyCode,
            alternateKeyCode,
            centreX,
            rowCentreY,
            tableFont
        );
    }

    private int takeFirstDistinctKey(List<Integer> keys, int disallowedKeyCode, int fallbackKeyCode) {
        for (int i = 0; i < keys.size(); i++) {
            int keyCode = keys.get(i);
            if (keyCode != disallowedKeyCode) {
                keys.remove(i);
                return keyCode;
            }
        }
        return fallbackKeyCode;
    }

    private void updateRemapInput(ICursorSource cursorSource, IInputQuery inputQuery, IAudioManager audio) {
        float mx = cursorSource.getCursorX();
        float my = cursorSource.getCursorY();
        boolean clickStarted =
            inputQuery.getActionState(GameActions.POINTER_PRIMARY) == InputState.PRESSED;

        for (int i = 0; i < remapSlots.size(); i++) {
            RemapSlot slot = remapSlots.get(i);
            RemapSlot.BindingColumn hitColumn = slot.hitTest(mx, my);
            slot.setHoveredColumn(hitColumn);
            slot.setActiveColumn(activeRemapSlot == slot ? activeRemapColumn : null);
            if (clickStarted && hitColumn != null) {
                if (audio != null) {
                    audio.playSound("select", Button.SELECT_SOUND_COOLDOWN_MS);
                }
                startListening(slot, hitColumn);
                return;
            }
        }
    }

    private void startListening(RemapSlot slot, RemapSlot.BindingColumn column) {
        activeRemapSlot = slot;
        activeRemapColumn = column;
        remapHint.setText("Press a key for " + slot.getLabel() + " (" + column.getLabel() + ")");
        previousInputProcessor = Gdx.input.getInputProcessor();
        Gdx.input.setInputProcessor(remapInputProcessor);
        refreshRemapVisualState();
    }

    private boolean isListeningForRemap() {
        return activeRemapSlot != null && activeRemapColumn != null;
    }

    private void cancelActiveRemap() {
        stopListening();
    }

    private void applyActiveRemap(int keyCode) {
        if (!isListeningForRemap()) {
            return;
        }

        String reservedUiKeyMessage = getReservedUiKeyMessage(keyCode);
        if (reservedUiKeyMessage != null) {
            remapHint.setText(reservedUiKeyMessage);
            refreshRemapVisualState();
            return;
        }

        int previousKeyCode = activeRemapSlot.getKeyCode(activeRemapColumn);
        int siblingKeyCode = activeRemapSlot.getOtherKeyCode(activeRemapColumn);

        if (keyCode == previousKeyCode || keyCode == siblingKeyCode) {
            stopListening();
            return;
        }

        RemapSlot ownerSlot = null;
        RemapSlot.BindingColumn ownerColumn = null;
        for (int i = 0; i < remapSlots.size(); i++) {
            RemapSlot slot = remapSlots.get(i);
            RemapSlot.BindingColumn column = slot.findColumnForKey(keyCode);
            if (column != null) {
                ownerSlot = slot;
                ownerColumn = column;
                break;
            }
        }

        if (ownerSlot != null && ownerColumn != null) {
            ownerSlot.setKeyCode(ownerColumn, previousKeyCode);
        }
        activeRemapSlot.setKeyCode(activeRemapColumn, keyCode);

        syncRemapBindings();
        stopListening();
    }

    private String getReservedUiKeyMessage(int keyCode) {
        if (keyCode == Input.Keys.SPACE) {
            return Input.Keys.toString(keyCode) + " is reserved for menu confirm";
        }
        if (keyCode == Input.Keys.ESCAPE || keyCode == Input.Keys.BACKSPACE) {
            return Input.Keys.toString(keyCode) + " is reserved for menu back";
        }
        return null;
    }

    private void syncRemapBindings() {
        for (int i = 0; i < remapSlots.size(); i++) {
            inputManager.unbindAction(remapSlots.get(i).getActionId());
        }
        for (int i = 0; i < remapSlots.size(); i++) {
            RemapSlot slot = remapSlots.get(i);
            inputManager.bindKey(slot.getPrimaryKeyCode(), slot.getActionId());
            inputManager.bindKey(slot.getAlternateKeyCode(), slot.getActionId());
        }
    }

    private void stopListening() {
        if (Gdx.input.getInputProcessor() == remapInputProcessor) {
            Gdx.input.setInputProcessor(previousInputProcessor);
        }
        previousInputProcessor = null;
        activeRemapSlot = null;
        activeRemapColumn = null;
        if (remapHint != null) {
            remapHint.setText(idleRemapHintText());
        }
        refreshRemapVisualState();
    }

    private void refreshRemapVisualState() {
        for (int i = 0; i < remapSlots.size(); i++) {
            RemapSlot slot = remapSlots.get(i);
            slot.setHoveredColumn(null);
            slot.setActiveColumn(activeRemapSlot == slot ? activeRemapColumn : null);
        }
    }

}

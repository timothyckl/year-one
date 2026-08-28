package com.p1_7.game.scenes;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.p1_7.abstractengine.input.IInputExtensionRegistry;
import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.abstractengine.input.InputState;
import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.render.IRenderQueue;
import com.p1_7.abstractengine.render.IRenderable;
import com.p1_7.abstractengine.scene.Scene;
import com.p1_7.abstractengine.scene.SceneContext;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.Settings;
import com.p1_7.game.spatial.Transform2D;
import com.p1_7.game.input.GameActions;
import com.p1_7.game.input.ICursorSource;
import com.p1_7.game.audio.IAudioManager;
import com.p1_7.game.font.IFontManager;
import com.p1_7.game.platform.GdxDrawContext;
import com.p1_7.game.ui.BackgroundImage;
import com.p1_7.game.ui.BrightnessOverlay;
import com.p1_7.game.ui.MenuButton;
import com.p1_7.game.ui.Text;

/**
 * Simple guide scene that explains the core game loop and controls.
 */
public final class HowToPlayScene extends Scene {

    private static final String BG_ASSET = "background.png";
    private static final String BTN_ASSET = "menu/button.png";
    private static final String HOVER_ASSET = "menu/button_hover.png";
    private static final String PLAYER_ASSET = "player.png";
    private static final String GOBLIN_ASSET = "goblin-walk.png";
    private static final String SKELETON_ASSET = "skeleton-walk.png";
    private static final String HEART_ASSET = "Heart.png";

    private static final int STRIP_FRAME_W = 96;
    private static final int STRIP_FRAME_H = 64;
    private static final int HEART_FRAME_SIZE = 16;
    private static final int HEART_FULL_FRAME = 4;

    private static final Color PANEL_FILL = new Color(0.96f, 0.98f, 1f, 0.70f);
    private static final Color PANEL_BORDER = new Color(0.85f, 0.92f, 1f, 0.92f);

    private BitmapFont headingFont;
    private BitmapFont subtitleFont;
    private BitmapFont sectionFont;
    private BitmapFont bodyFont;
    private BitmapFont buttonFont;

    private BackgroundImage background;
    private Text heading;
    private Text subtitle;
    private final List<IRenderable> panels = new ArrayList<>();
    private final List<IRenderable> ornaments = new ArrayList<>();
    private final List<IRenderable> guideText = new ArrayList<>();
    private MenuButton backButton;
    private BrightnessOverlay brightnessOverlay;

    public HowToPlayScene() {
        this.name = "how-to-play";
    }

    @Override
    public void onEnter(SceneContext context) {
        context.get(IAudioManager.class).playMusic("menu", true);

        IFontManager fontManager = context.get(IFontManager.class);
        headingFont = fontManager.getGoldDisplayFont(40);
        subtitleFont = fontManager.getDarkTextFont(18);
        sectionFont = fontManager.getDarkTextFont(22);
        bodyFont = fontManager.getDarkTextFont(16);
        buttonFont = fontManager.getDarkTextFont(24);

        float centreX = Settings.getWindowWidth() / 2f;
        float headingY = Settings.getWindowHeight() * 0.86f - 15f;

        background = new BackgroundImage(BG_ASSET);
        heading = new Text("HOW TO PLAY", centreX, headingY, headingFont);
        subtitle = new Text("The basics before you step into the maze.", centreX, headingY - 42f, subtitleFont);
        backButton = MenuButton.withTexture(
            "BACK",
            centreX,
            Settings.getWindowHeight() * 0.085f,
            buttonFont,
            BTN_ASSET,
            HOVER_ASSET
        );
        brightnessOverlay = new BrightnessOverlay();

        buildLayout();
    }

    private void buildLayout() {
        panels.clear();
        ornaments.clear();
        guideText.clear();

        float centreX = Settings.getWindowWidth() / 2f;
        float panelWidth = 900f;
        float panelHeight = 530f;
        float panelCentreY = 375f;
        float panelX = centreX - panelWidth / 2f;
        float panelY = panelCentreY - panelHeight / 2f;
        float leftX = panelX + 58f;
        float rightX = panelX + 620f;
        float sectionGap = 40f;
        float lineGap = 22f;
        float legendRowGap = 72f;
        float legendFirstRowGap = 32f;
        float legendLabelX = rightX + 66f;
        float legendSpriteCentreX = rightX + 22f;
        float legendSpriteYOffset = 18f;
        float textYOffset = 15f;

        addPanel(panelX, panelY, panelWidth, panelHeight);

        float y = panelY + panelHeight - 102f - textYOffset;
        addGuideLine("OBJECTIVE", leftX, y, sectionFont);
        y -= 32f;
        addGuideLine("Reach the room with the correct answer to solve the question.", leftX, y, bodyFont);
        y -= lineGap;
        addGuideLine("Step into the matching answer room to advance the round.", leftX, y, bodyFont);

        y -= sectionGap;
        addGuideLine("CONTROLS", leftX, y, sectionFont);
        y -= 32f;
        addGuideLine("Move with WASD or the arrow keys.", leftX, y, bodyFont);
        y -= lineGap;
        addGuideLine("Press ESC during a round to pause.", leftX, y, bodyFont);

        y -= sectionGap;
        addGuideLine("DANGERS", leftX, y, sectionFont);
        y -= 32f;
        addGuideLine("Goblins guard the corner rooms.", leftX, y, bodyFont);
        y -= lineGap;
        addGuideLine("Skeletons roam the corridors.", leftX, y, bodyFont);
        y -= lineGap;
        addGuideLine("Touching an attacking enemy costs 1 health.", leftX, y, bodyFont);

        y -= sectionGap;
        addGuideLine("SURVIVE & SCORE", leftX, y, sectionFont);
        y -= 32f;
        addGuideLine("Hearts restore health when picked up.", leftX, y, bodyFont);
        y -= lineGap;
        addGuideLine("Correct answers raise your score and push you to the next level.", leftX, y, bodyFont);
        y -= lineGap;
        addGuideLine("Read the prompt, move fast, and choose carefully.", leftX, y, bodyFont);

        float legendY = panelY + panelHeight - 102f - textYOffset;
        addGuideLine("MAZE LEGEND", rightX, legendY, sectionFont);

        legendY -= legendFirstRowGap;
        addSprite(PLAYER_ASSET, 0, legendSpriteCentreX, legendY - legendSpriteYOffset, 142f, 112f, false);
        addGuideLine("You", legendLabelX, legendY, bodyFont);

        legendY -= legendRowGap;
        addSprite(GOBLIN_ASSET, 0, legendSpriteCentreX, legendY - legendSpriteYOffset, 142f, 112f, false);
        addGuideLine("Goblin enemy", legendLabelX, legendY, bodyFont);

        legendY -= legendRowGap;
        addSprite(SKELETON_ASSET, 0, legendSpriteCentreX, legendY - legendSpriteYOffset, 142f, 112f, false);
        addGuideLine("Skeleton enemy", legendLabelX, legendY, bodyFont);

        legendY -= legendRowGap;
        addHeart(legendSpriteCentreX, legendY - 4f, 26f);
        addGuideLine("Health pickup", legendLabelX, legendY, bodyFont);
    }

    private void addPanel(float x, float y, float width, float height) {
        Transform2D transform = new Transform2D(x, y, width, height);
        panels.add(new IRenderable() {
            @Override public String getAssetPath() { return null; }
            @Override public ITransform getTransform() { return transform; }

            @Override
            public void render(IDrawContext ctx) {
                GdxDrawContext gdx = (GdxDrawContext) ctx;
                gdx.drawTintedQuad(PANEL_FILL, x, y, width, height);
                gdx.rect(PANEL_BORDER, x, y, width, height, false);
            }
        });
    }

    private void addGuideLine(String text, float x, float baselineY, BitmapFont font) {
        guideText.add(new LeftAlignedText(text, x, baselineY, font));
    }

    private void addSprite(String assetPath, int frameIndex, float centreX, float centreY,
                           float width, float height, boolean flipX) {
        float x = centreX - width / 2f;
        float y = centreY - height / 2f;
        int srcX = frameIndex * STRIP_FRAME_W;
        Transform2D transform = new Transform2D(x, y, width, height);
        ornaments.add(new IRenderable() {
            @Override public String getAssetPath() { return assetPath; }
            @Override public ITransform getTransform() { return transform; }

            @Override
            public void render(IDrawContext ctx) {
                ((GdxDrawContext) ctx).drawTextureRegion(
                    assetPath, srcX, 0, STRIP_FRAME_W, STRIP_FRAME_H, x, y, width, height, flipX
                );
            }
        });
    }

    private void addHeart(float centreX, float centreY, float size) {
        float x = centreX - size / 2f;
        float y = centreY - size / 2f;
        int srcX = HEART_FULL_FRAME * HEART_FRAME_SIZE;
        Transform2D transform = new Transform2D(x, y, size, size);
        ornaments.add(new IRenderable() {
            @Override public String getAssetPath() { return HEART_ASSET; }
            @Override public ITransform getTransform() { return transform; }

            @Override
            public void render(IDrawContext ctx) {
                ((GdxDrawContext) ctx).drawTextureRegion(
                    HEART_ASSET,
                    srcX,
                    0,
                    HEART_FRAME_SIZE,
                    HEART_FRAME_SIZE,
                    x,
                    y,
                    size,
                    size,
                    false
                );
            }
        });
    }

    @Override
    public void onExit(SceneContext context) {
        if (backButton != null) {
            backButton.dispose();
        }
        background = null;
        heading = null;
        subtitle = null;
        panels.clear();
        ornaments.clear();
        guideText.clear();
        backButton = null;
        brightnessOverlay = null;
        headingFont = null;
        subtitleFont = null;
        sectionFont = null;
        bodyFont = null;
        buttonFont = null;
    }

    @Override
    public void update(float deltaTime, SceneContext context) {
        IInputQuery inputQuery = context.get(IInputQuery.class);
        IAudioManager audio = context.get(IAudioManager.class);
        if (inputQuery.getActionState(GameActions.MENU_BACK) == InputState.PRESSED) {
            context.changeScene("menu");
            return;
        }

        IInputExtensionRegistry inputRegistry = context.get(IInputExtensionRegistry.class);
        ICursorSource cursorSource = inputRegistry.hasExtension(ICursorSource.class)
            ? inputRegistry.getExtension(ICursorSource.class) : null;
        if (cursorSource == null) {
            return;
        }

        backButton.updateInput(cursorSource, inputQuery, audio);
        if (backButton.isClicked()) {
            backButton.resetClick();
            context.changeScene("menu");
        }
    }

    @Override
    public void submitRenderable(IRenderQueue renderQueue) {
        renderQueue.queue(background);
        for (IRenderable panel : panels) {
            renderQueue.queue(panel);
        }
        renderQueue.queue(heading);
        renderQueue.queue(subtitle);
        for (IRenderable ornament : ornaments) {
            renderQueue.queue(ornament);
        }
        for (IRenderable line : guideText) {
            renderQueue.queue(line);
        }
        renderQueue.queue(backButton);
        renderQueue.queue(brightnessOverlay);
    }

    private static final class LeftAlignedText implements IRenderable {
        private final String text;
        private final float x;
        private final float baselineY;
        private final BitmapFont font;
        private final GlyphLayout layout;
        private final Transform2D transform;

        private LeftAlignedText(String text, float x, float baselineY, BitmapFont font) {
            this.text = text;
            this.x = x;
            this.baselineY = baselineY;
            this.font = font;
            this.layout = new GlyphLayout(font, text);
            this.transform = new Transform2D(x, baselineY - layout.height, layout.width, layout.height);
        }

        @Override
        public String getAssetPath() {
            return null;
        }

        @Override
        public ITransform getTransform() {
            return transform;
        }

        @Override
        public void render(IDrawContext ctx) {
            ((GdxDrawContext) ctx).drawFont(font, text, x, baselineY);
        }
    }
}

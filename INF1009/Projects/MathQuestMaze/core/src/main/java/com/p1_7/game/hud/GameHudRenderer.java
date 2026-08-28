package com.p1_7.game.hud;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.render.IRenderable;
import com.p1_7.abstractengine.render.IRenderQueue;
import com.p1_7.abstractengine.scene.SceneContext;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.Settings;
import com.p1_7.game.spatial.Transform2D;
import com.p1_7.game.math.Difficulty;
import com.p1_7.game.round.RoundPhase;
import com.p1_7.game.round.ILevelOrchestrator;
import com.p1_7.game.font.IFontManager;
import com.p1_7.game.maze.MazeLayout;
import com.p1_7.game.platform.GdxDrawContext;
import com.p1_7.game.ui.BrightnessOverlay;

/**
 * owns all HUD renderables — score, health, level label, question panel,
 * feedback overlay, room answer labels, and the brightness overlay.
 *
 * created and initialised by GameScene in onEnter(); submitRoomLabels() and
 * submitHudOverlays() are called each frame to queue the HUD layer in painter's order.
 */
public class GameHudRenderer {

    /** pre-allocated overlay colours — reused every frame */
    private static final Color OVERLAY_CORRECT = new Color(0.08f, 0.62f, 0.22f, 0.42f);
    private static final Color OVERLAY_WRONG   = new Color(0.75f, 0.08f, 0.08f, 0.42f);

    /** vertical offset from the strip baseline to the score text baseline */
    private static final float HUD_SCORE_BASELINE_OFFSET = 31f;

    /** vertical offset from the strip baseline to the health pip top edge */
    private static final float HUD_HEALTH_BASELINE_OFFSET = 14f;

    // fields populated during init() ──────────────────────────────────

    private BitmapFont promptFont;
    private BitmapFont questionFont;
    private BitmapFont hudFont;

    private QuestionPanel questionPanel;
    private HudStrip      hudStrip;

    private List<IRenderable> roomRenderables;
    private IRenderable feedbackOverlay;
    private IRenderable scoreDisplay;
    private IRenderable healthDisplay;
    private IRenderable levelDisplay;
    private BrightnessOverlay brightnessOverlay;

    private String[]      roomAnswerTexts;
    private GlyphLayout[] roomAnswerLayouts;

    /**
     * builds every HUD renderable. must be called once during scene entry.
     *
     * @param context      the engine service context for font access
     * @param layout       the maze layout providing room geometry
     * @param orchestrator the level orchestrator
     * @param difficulty   the current difficulty level
     */
    public void init(SceneContext context, MazeLayout layout,
                     ILevelOrchestrator orchestrator, Difficulty difficulty) {
        IFontManager fontManager = context.get(IFontManager.class);
        this.promptFont   = fontManager.getLightTextFont(28);
        this.questionFont = fontManager.getLightTextFont(36);
        this.hudFont      = fontManager.getLightTextFont(22);

        // allocate per-room answer caches
        this.roomAnswerTexts   = new String[4];
        this.roomAnswerLayouts = new GlyphLayout[4];
        for (int i = 0; i < 4; i++) {
            roomAnswerLayouts[i] = new GlyphLayout();
        }

        this.brightnessOverlay = new BrightnessOverlay();
        this.hudStrip = new HudStrip();

        buildRoomRenderables(layout);
        refreshAnswerCache(orchestrator);

        this.questionPanel = new QuestionPanel(questionFont);
        questionPanel.beginIntro(orchestrator.getCurrentQuestion().getPrompt());

        buildFeedbackOverlay(orchestrator);
        buildScoreDisplay(orchestrator);
        buildHealthDisplay(orchestrator);
        buildLevelDisplay(difficulty);
    }

    /**
     * queues the room answer labels. called before entities so labels sit
     * behind sprites in painter's order.
     *
     * @param renderQueue the frame's render queue
     */
    public void submitRoomLabels(IRenderQueue renderQueue) {
        for (IRenderable room : roomRenderables) {
            renderQueue.queue(room);
        }
    }

    /**
     * queues the question panel, HUD strip, score, health, level, feedback,
     * and brightness overlays. called after entities so HUD sits on top.
     *
     * @param renderQueue the frame's render queue
     * @param paused      true when the scene is suspended — skips the brightness overlay
     *                    to avoid double-dimming with the pause overlay
     */
    public void submitHudOverlays(IRenderQueue renderQueue, boolean paused) {
        renderQueue.queue(questionPanel);
        renderQueue.queue(hudStrip);
        renderQueue.queue(scoreDisplay);
        renderQueue.queue(healthDisplay);
        renderQueue.queue(levelDisplay);
        renderQueue.queue(feedbackOverlay);
        if (!paused) {
            renderQueue.queue(brightnessOverlay);
        }
    }

    /**
     * advances the question panel slide animation.
     *
     * @param deltaTime seconds since the last frame
     */
    public void updateQuestionPanel(float deltaTime) {
        questionPanel.update(deltaTime);
    }

    /**
     * starts the question panel intro animation for a new question.
     *
     * @param prompt the question text to display
     */
    public void beginQuestionIntro(String prompt) {
        questionPanel.beginIntro(prompt);
    }

    /**
     * reads the current room assignment from the orchestrator and pre-computes
     * the answer text and glyph layout for each room so that room renderables
     * never allocate during their render path.
     *
     * @param orchestrator the level orchestrator
     */
    public void refreshAnswerCache(ILevelOrchestrator orchestrator) {
        for (int i = 0; i < 4; i++) {
            roomAnswerTexts[i] = String.valueOf(
                orchestrator.getRoomAssignment().getAnswerForRoom(i));
            roomAnswerLayouts[i].setText(promptFont, roomAnswerTexts[i]);
        }
    }

    /**
     * nulls all fields so they can be garbage collected on scene exit.
     */
    public void dispose() {
        roomRenderables   = null;
        roomAnswerTexts   = null;
        roomAnswerLayouts = null;
        questionPanel     = null;
        promptFont        = null;
        questionFont      = null;
        hudFont           = null;
        feedbackOverlay   = null;
        scoreDisplay      = null;
        healthDisplay     = null;
        levelDisplay      = null;
        hudStrip          = null;
        brightnessOverlay = null;
    }

    // renderable builders ─────────────────────────────────────────────

    private void buildRoomRenderables(MazeLayout layout) {
        this.roomRenderables = new ArrayList<>(4);
        List<float[]> allRooms = layout.getAllRoomBounds();

        final BitmapFont    roomFont           = promptFont;
        final String[]      capturedAnswerTexts   = roomAnswerTexts;
        final GlyphLayout[] capturedAnswerLayouts = roomAnswerLayouts;

        for (int i = 0; i < allRooms.size(); i++) {
            final int     roomIndex = i;
            final float[] rect      = allRooms.get(i);
            final Transform2D roomTransform = new Transform2D(
                rect[0], rect[1], rect[2], rect[3]);

            roomRenderables.add(new IRenderable() {
                @Override public String     getAssetPath() { return null; }
                @Override public ITransform getTransform() { return roomTransform; }

                @Override
                public void render(IDrawContext ctx) {
                    GdxDrawContext gdx = (GdxDrawContext) ctx;
                    GlyphLayout gl = capturedAnswerLayouts[roomIndex];
                    gdx.drawFont(roomFont, capturedAnswerTexts[roomIndex],
                        rect[0] + (rect[2] - gl.width) / 2f,
                        rect[1] + rect[3] / 2f + gl.height / 2f);
                }
            });
        }
    }

    private void buildFeedbackOverlay(ILevelOrchestrator orchestrator) {
        final ILevelOrchestrator orch = orchestrator;
        final BitmapFont capturedHudFont = hudFont;
        final GlyphLayout correctLayout = new GlyphLayout(capturedHudFont, "CORRECT!");
        final GlyphLayout wrongLayout   = new GlyphLayout(capturedHudFont, "WRONG!");
        final Color overlayColour = new Color();

        this.feedbackOverlay = new IRenderable() {
            private final Transform2D t = new Transform2D(
                0f, 0f, Settings.getWindowWidth(), HudStrip.PLAYFIELD_HEIGHT);

            @Override public String     getAssetPath() { return null; }
            @Override public ITransform getTransform() { return t; }

            @Override
            public void render(IDrawContext ctx) {
                RoundPhase p = orch.getPhase();
                boolean enemyDeath = p == RoundPhase.GAME_OVER && orch.wasLastDamageFromEnemy();
                if (p != RoundPhase.FEEDBACK
                        && p != RoundPhase.LEVEL_COMPLETE
                        && p != RoundPhase.GAME_OVER) return;
                if (enemyDeath) return;

                boolean correct = orch.isLastAnswerCorrect();
                overlayColour.set(correct ? OVERLAY_CORRECT : OVERLAY_WRONG);
                GdxDrawContext gdx = (GdxDrawContext) ctx;
                gdx.drawTintedQuad(overlayColour, 0f, 0f,
                    Settings.getWindowWidth(), HudStrip.PLAYFIELD_HEIGHT);

                String      msg    = correct ? "CORRECT!" : "WRONG!";
                GlyphLayout layout = correct ? correctLayout : wrongLayout;
                gdx.drawFont(capturedHudFont, msg,
                    Settings.getWindowWidth() / 2f - layout.width / 2f,
                    HudStrip.PLAYFIELD_HEIGHT / 2f + 60f);
            }
        };
    }

    private void buildScoreDisplay(ILevelOrchestrator orchestrator) {
        final ILevelOrchestrator orch = orchestrator;
        final BitmapFont capturedHudFont = hudFont;

        this.scoreDisplay = new IRenderable() {
            private static final float RIGHT_PADDING = 18f;
            private final float BASELINE_Y = HudStrip.STRIP_Y + HUD_SCORE_BASELINE_OFFSET;
            private final Transform2D t = new Transform2D(
                Settings.getWindowWidth() - RIGHT_PADDING, BASELINE_Y, 0f, 0f);
            private final GlyphLayout layout = new GlyphLayout();

            @Override public String     getAssetPath() { return null; }
            @Override public ITransform getTransform() { return t; }

            @Override
            public void render(IDrawContext ctx) {
                String text = "Score: " + orch.getScore() + "/5";
                layout.setText(capturedHudFont, text);
                ((GdxDrawContext) ctx).drawFont(capturedHudFont, text,
                    Settings.getWindowWidth() - RIGHT_PADDING - layout.width,
                    BASELINE_Y);
            }
        };
    }

    private void buildHealthDisplay(ILevelOrchestrator orchestrator) {
        final ILevelOrchestrator orch = orchestrator;
        final BitmapFont capturedHudFont = hudFont;

        this.healthDisplay = new IRenderable() {
            private static final String HEART_ASSET  = "Heart.png";
            private static final String LABEL        = "Health:";
            private static final int    FRAME_SIZE   = 16;
            private static final int    FRAME_FULL   = 4;
            private static final int    FRAME_EMPTY  = 0;
            private static final float  SQ           = 20f;
            private static final float  GAP          = 6f;
            private static final float  LABEL_MARGIN = 8f;
            private static final float  BASE_X       = 18f;
            private final float       BASE_Y      = HudStrip.STRIP_Y + HUD_HEALTH_BASELINE_OFFSET;
            private final float       LABEL_Y     = HudStrip.STRIP_Y + HUD_SCORE_BASELINE_OFFSET;
            private final GlyphLayout labelLayout = new GlyphLayout(capturedHudFont, LABEL);
            private final Transform2D t           = new Transform2D(BASE_X, BASE_Y, 0f, 0f);

            @Override public String     getAssetPath() { return null; }
            @Override public ITransform getTransform() { return t; }

            @Override
            public void render(IDrawContext ctx) {
                GdxDrawContext gdx     = (GdxDrawContext) ctx;
                int           health   = orch.getHealth();
                float         heartsX  = BASE_X + labelLayout.width + LABEL_MARGIN;
                gdx.drawFont(capturedHudFont, LABEL, BASE_X, LABEL_Y);
                for (int i = 0; i < 3; i++) {
                    float x    = heartsX + i * (SQ + GAP);
                    int   frame = (i < health) ? FRAME_FULL : FRAME_EMPTY;
                    int   srcX  = frame * FRAME_SIZE;
                    gdx.drawTextureRegion(HEART_ASSET, srcX, 0, FRAME_SIZE, FRAME_SIZE,
                                         x, BASE_Y, SQ, SQ, false);
                }
            }
        };
    }

    private void buildLevelDisplay(Difficulty difficulty) {
        final BitmapFont capturedHudFont = hudFont;
        final Difficulty capturedDifficulty = difficulty;

        this.levelDisplay = new IRenderable() {
            private final float BASELINE_Y = HudStrip.STRIP_Y + HUD_SCORE_BASELINE_OFFSET;
            private final float CENTRE_X   = Settings.getWindowWidth() / 2f;
            private final Transform2D t = new Transform2D(CENTRE_X, BASELINE_Y, 0f, 0f);
            private final GlyphLayout layout = new GlyphLayout();

            @Override public String     getAssetPath() { return null; }
            @Override public ITransform getTransform() { return t; }

            @Override
            public void render(IDrawContext ctx) {
                String text = "Level " + getLevelNumber(capturedDifficulty)
                    + " - " + formatDifficultyLabel(capturedDifficulty);
                layout.setText(capturedHudFont, text);
                ((GdxDrawContext) ctx).drawFont(capturedHudFont, text,
                    CENTRE_X - layout.width / 2f, BASELINE_Y);
            }
        };
    }

    // static helpers ──────────────────────────────────────────────────

    private static int getLevelNumber(Difficulty difficulty) {
        if (difficulty == Difficulty.MEDIUM) return 2;
        if (difficulty == Difficulty.HARD)   return 3;
        return 1;
    }

    private static String formatDifficultyLabel(Difficulty difficulty) {
        if (difficulty == Difficulty.MEDIUM) return "Medium";
        if (difficulty == Difficulty.HARD)   return "Hard";
        return "Easy";
    }
}

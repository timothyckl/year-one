package com.p1_7.game.scenes;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.p1_7.abstractengine.collision.IBounds;
import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.abstractengine.input.InputState;
import com.p1_7.game.input.GameActions;
import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.render.IRenderable;
import com.p1_7.abstractengine.render.IRenderQueue;
import com.p1_7.abstractengine.scene.Scene;
import com.p1_7.abstractengine.scene.SceneContext;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.GameConfig;
import com.p1_7.game.Settings;
import com.p1_7.game.spatial.Transform2D;
import com.p1_7.game.hud.GameHudRenderer;
import com.p1_7.game.hud.HudStrip;
import com.p1_7.game.character.HostileCharacter;
import com.p1_7.game.character.Player;
import com.p1_7.game.math.Difficulty;
import com.p1_7.game.round.EnemyController;
import com.p1_7.game.round.GamePhaseController;
import com.p1_7.game.round.GamePhaseListener;
import com.p1_7.game.round.ItemSpawner;
import com.p1_7.game.round.MovementPipeline;
import com.p1_7.game.round.RoundPhase;
import com.p1_7.game.round.ILevelOrchestrator;
import com.p1_7.game.maze.MazeCollisionManager;
import com.p1_7.game.maze.MazeLayout;
import com.p1_7.game.maze.WallCollidable;
import com.p1_7.game.character.GameMovementManager;
import com.p1_7.game.audio.IAudioManager;
import com.p1_7.game.collectible.Item;
import com.p1_7.game.collectible.ItemCollectionListener;
import com.p1_7.game.character.PlayerDamageListener;
import com.p1_7.game.platform.GdxDrawContext;

/**
 * core gameplay scene — wires the level orchestrator, player movement, wall collision,
 * and answer-room entry detection.
 *
 * delegates gameplay responsibilities to focused collaborators:
 *   - GamePhaseController  — phase state machine and room-entry detection
 *   - EnemyController      — enemy spawning and AI updates
 *   - GameHudRenderer      — score, health, level, feedback, and answer labels
 *   - ItemSpawner          — heart pickup placement
 *   - MovementPipeline     — documents the three-step movement ordering
 */
public class GameScene extends Scene implements GamePhaseListener, ItemCollectionListener, PlayerDamageListener {

    /** lighter blue-slate background for the walkable playfield */
    private static final Color SCENE_BG_COLOUR = new Color(0.15f, 0.19f, 0.27f, 1f);

    // debug hitbox colours
    private static final Color DBG_PLAYER   = new Color(0f,   1f,   0f,   1f);
    private static final Color DBG_WALL_BOX = new Color(1f,   0f,   0f,   1f);
    private static final Color DBG_DMG_BOX  = new Color(1f,   0.5f, 0f,   1f);
    private static final Color DBG_ITEM     = new Color(1f,   1f,   0f,   1f);
    private static final Color DBG_ROOM     = new Color(0f,   1f,   1f,   1f);

    /** solid wall fill colour for the generated maze */
    private static final Color WALL_FILL_COLOUR = new Color(0.07f, 0.10f, 0.16f, 1f);

    // collaborators ───────────────────────────────────────────────────

    private final GamePhaseController phaseController  = new GamePhaseController();
    private final EnemyController     enemyController  = new EnemyController();
    private final ItemSpawner         itemSpawner      = new ItemSpawner();
    private final MovementPipeline    movementPipeline = new MovementPipeline();
    private final GameHudRenderer     hudRenderer      = new GameHudRenderer();

    // scene-lifecycle fields ──────────────────────────────────────────

    /** the fixed spatial layout providing spawn point, room bounds, and wall bounds */
    private MazeLayout layout;

    /** the player entity — created at scene entry, released on exit */
    private Player player;

    /** all hostile characters (goblins + skeletons) */
    private List<HostileCharacter> enemies;

    /** collectable pickups currently present in the maze */
    private List<Item> items;

    /** cached audio manager for sound effect playback */
    private IAudioManager audioManager;

    /** solid background quad for the gameplay area */
    private IRenderable backgroundRenderable;

    /** debug overlay that draws hitbox outlines; toggled with F1 */
    private IRenderable debugHitboxRenderable;

    /** whether to draw hitbox outlines this frame */
    private boolean showHitboxes = GameConfig.DEBUG_HITBOXES;

    /** wall collidables registered with the collision manager */
    private List<WallCollidable> wallCollidables;

    /** one renderable per wall rectangle so the maze geometry is visible */
    private List<IRenderable> wallRenderables;

    /**
     * cached copies of the four room bounds arrays; MazeLayout is immutable so these
     * never change — caching avoids defensive-clone allocations inside the per-frame loop
     */
    private float[][] cachedRoomBounds;

    /**
     * constructs the game scene with the scene key "game".
     */
    public GameScene() {
        this.name = "game";
    }

    /**
     * initialises the layout, player, input query, orchestrator, and room state.
     *
     * @param context the engine service context
     */
    @Override
    public void onEnter(SceneContext context) {
        // ensure the scene always starts unpaused
        setPaused(false);
        this.audioManager = context.get(IAudioManager.class);
        audioManager.playMusic("game", true);
        this.layout   = MazeLayout.createDefault();
        float[] spawn = layout.getSpawnPoint();
        this.player   = new Player(spawn[0], spawn[1]);
        this.backgroundRenderable = createBackgroundRenderable();

        // wire movement manager and register the player for position integration
        GameMovementManager movementManager = context.get(GameMovementManager.class);
        movementManager.registerMovable(player);

        // wire collision manager and register the player and all walls
        MazeCollisionManager collisionManager = context.get(MazeCollisionManager.class);
        this.wallCollidables = new ArrayList<>();
        this.wallRenderables = new ArrayList<>();
        collisionManager.registerPlayer(player);
        for (float[] rect : layout.getWallBounds()) {
            WallCollidable wall = new WallCollidable(rect);
            wallCollidables.add(wall);
            collisionManager.registerWall(wall);
            wallRenderables.add(createWallRenderable(rect));
        }

        // wire level orchestrator and start the session at the selected difficulty
        ILevelOrchestrator orchestrator = context.get(ILevelOrchestrator.class);
        Difficulty difficulty = orchestrator.getCurrentDifficulty();
        orchestrator.startLevel(difficulty);
        player.bindGameplay(orchestrator);
        player.bindDamageListener(this);

        // cache room bounds once
        this.cachedRoomBounds = new float[4][];
        for (int i = 0; i < cachedRoomBounds.length; i++) {
            cachedRoomBounds[i] = layout.getRoomBounds(i);
        }

        // spawn enemies via the controller
        this.enemies = enemyController.spawnEnemies(layout, difficulty);
        for (HostileCharacter enemy : enemies) {
            movementManager.registerMovable(enemy);
            collisionManager.registerMover(enemy);
        }

        // spawn items via the spawner and register scene as the collection listener
        this.items = itemSpawner.spawnItems(layout, orchestrator);
        for (Item item : items) {
            item.bindListener(this);
            collisionManager.registerItem(item);
        }

        // initialise HUD — fonts, answer labels, panels, overlays
        hudRenderer.init(context, layout, orchestrator, difficulty);

        // initialise phase controller — prevent a spurious onPhaseChanged on the first tick
        phaseController.setLastKnownPhase(orchestrator.getPhase());
        phaseController.setHoldTimer(GameConfig.QUESTION_INTRO_HOLD_SECONDS);

        // build the debug hitbox overlay (references live scene fields via closure)
        debugHitboxRenderable = createDebugHitboxRenderable();

    }

    /**
     * releases all scene-owned references so they can be garbage collected.
     *
     * @param context the engine service context
     */
    @Override
    public void onExit(SceneContext context) {
        GameMovementManager movementManager = context.get(GameMovementManager.class);
        MazeCollisionManager collisionManager = context.get(MazeCollisionManager.class);

        movementManager.unregisterMovable(player);
        for (HostileCharacter enemy : enemies) {
            movementManager.unregisterMovable(enemy);
            collisionManager.unregisterMover(enemy);
        }
        for (Item item : items) {
            collisionManager.unregisterItem(item);
        }
        enemies = null;
        items   = null;

        collisionManager.unregisterPlayer();
        for (WallCollidable wall : wallCollidables) {
            collisionManager.unregisterWall(wall);
        }

        hudRenderer.dispose();

        cachedRoomBounds      = null;
        wallRenderables       = null;
        layout                = null;
        player                = null;
        backgroundRenderable  = null;
        debugHitboxRenderable = null;
        wallCollidables       = null;
        audioManager          = null;
    }

    /**
     * called when the pause overlay opens — freezes the game world in place.
     *
     * @param context the engine service context
     */
    @Override
    public void onSuspend(SceneContext context) {
        setPaused(true);
        player.setVelocity(new float[]{ 0f, 0f });
        enemyController.freezeEnemies(enemies);
        context.get(IAudioManager.class).pauseMusic();
    }

    /**
     * called when the pause overlay closes — resumes normal gameplay.
     *
     * @param context the engine service context
     */
    @Override
    public void onResume(SceneContext context) {
        setPaused(false);
        context.get(IAudioManager.class).resumeMusic();
    }

    /**
     * resolves player input, detects phase changes, and checks answer-room entry.
     *
     * @param deltaTime elapsed seconds since the last frame
     * @param context   the engine service context
     */
    @Override
    public void update(float deltaTime, SceneContext context) {
        ILevelOrchestrator orchestrator = context.get(ILevelOrchestrator.class);
        IInputQuery inputQuery = context.get(IInputQuery.class);
        if (inputQuery.getActionState(GameActions.DEBUG_TOGGLE) == InputState.PRESSED) {
            showHitboxes = !showHitboxes;
        }
        RoundPhase phase = orchestrator.getPhase();

        // detect phase change and react
        phaseController.detectPhaseChange(phase, orchestrator, this);

        // non-interactive phases: freeze input and auto-advance after hold timer
        if (phase != RoundPhase.CHOOSING) {
            if (phase == RoundPhase.GAME_OVER && orchestrator.wasLastDamageFromEnemy()) {
                context.changeScene("game-over");
                return;
            }
            // step 1 of the movement pipeline — velocity zeroed via phase lock
            movementPipeline.step(deltaTime, player, inputQuery, phase);
            enemyController.freezeEnemies(enemies);
            if (phase == RoundPhase.QUESTION_INTRO) {
                hudRenderer.updateQuestionPanel(deltaTime);
            }
            if (phaseController.tickHoldTimer(deltaTime)) {
                if (GamePhaseController.isTerminalPhase(phase)) {
                    context.changeScene(
                        phase == RoundPhase.LEVEL_COMPLETE ? "level-complete" : "game-over");
                } else {
                    orchestrator.advance();
                    // re-read and process the resulting phase in the same frame
                    RoundPhase newPhase = orchestrator.getPhase();
                    phaseController.detectPhaseChange(newPhase, orchestrator, this);
                }
            }
            return;
        }

        // choosing phase: check for pause request before processing player input
        if (inputQuery.getActionState(GameActions.MENU_BACK) == InputState.PRESSED) {
            context.suspendScene("pause");
            return;
        }

        // step 1 of the movement pipeline — resolve input into velocity
        movementPipeline.step(deltaTime, player, inputQuery, phase);
        enemyController.updateEnemies(deltaTime, enemies, player, wallCollidables);
        phaseController.checkRoomEntry(deltaTime, player, cachedRoomBounds, orchestrator);
    }

    /**
     * submits all renderables to the queue in painter's order.
     *
     * @param renderQueue the render queue accumulator for this frame
     */
    @Override
    public void submitRenderable(IRenderQueue renderQueue) {
        if (wallRenderables == null) return; // scene has already exited
        renderQueue.queue(backgroundRenderable);
        for (IRenderable wall : wallRenderables) {
            renderQueue.queue(wall);
        }
        // room answer labels sit behind entity sprites
        hudRenderer.submitRoomLabels(renderQueue);
        for (HostileCharacter enemy : enemies) {
            renderQueue.queue(enemy);
        }
        for (Item item : items) {
            if (item.isActive()) {
                renderQueue.queue(item);
            }
        }
        renderQueue.queue(player);
        // debug hitbox overlays sit on top of sprites, below HUD
        if (showHitboxes) {
            renderQueue.queue(debugHitboxRenderable);
        }
        // HUD overlays sit on top of everything
        hudRenderer.submitHudOverlays(renderQueue, paused);
    }

    // GamePhaseListener ───────────────────────────────────────────────

    /**
     * called whenever the round phase transitions. resets the player on ROUND_RESET
     * and refreshes the answer cache / panel animation on QUESTION_INTRO.
     */
    @Override
    public void onPhaseChanged(RoundPhase from, RoundPhase to,
                               ILevelOrchestrator orchestrator) {
        if (to == RoundPhase.FEEDBACK) {
            audioManager.playSound(orchestrator.isLastAnswerCorrect() ? "answer" : "wrong");
        } else if (to == RoundPhase.LEVEL_COMPLETE) {
            audioManager.playSound("answer");
        } else if (to == RoundPhase.GAME_OVER) {
            audioManager.playSound("die");
        }
        if (to == RoundPhase.ROUND_RESET) {
            player.resetToSpawn(layout.getSpawnPoint());
            for (HostileCharacter enemy : enemies) {
                enemy.resetToSpawn();
            }
            phaseController.resetRoomState();
        }
        if (to == RoundPhase.QUESTION_INTRO) {
            hudRenderer.refreshAnswerCache(orchestrator);
            hudRenderer.beginQuestionIntro(orchestrator.getCurrentQuestion().getPrompt());
        }
    }

    // ItemCollectionListener ──────────────────────────────────────────

    /**
     * plays the collect sound for the item type, if one is defined.
     *
     * @param item the item that was collected
     */
    @Override
    public void onItemCollected(Item item) {
        String key = item.getCollectSoundKey();
        if (audioManager != null && key != null) {
            audioManager.playSound(key);
        }
    }

    // PlayerDamageListener ────────────────────────────────────────────

    /**
     * plays the hurt sound when the player takes enemy damage.
     */
    @Override
    public void onPlayerDamaged() {
        if (audioManager != null) {
            audioManager.playSound("hurt");
        }
    }

    // private helpers ─────────────────────────────────────────────────

    private IRenderable createWallRenderable(float[] rect) {
        final float[] stableRect = rect.clone();
        final Transform2D transform = new Transform2D(
            stableRect[0], stableRect[1], stableRect[2], stableRect[3]);
        return new IRenderable() {
            @Override public String getAssetPath() { return null; }
            @Override public ITransform getTransform() { return transform; }

            @Override
            public void render(IDrawContext ctx) {
                ((GdxDrawContext) ctx).rect(
                    WALL_FILL_COLOUR,
                    stableRect[0], stableRect[1], stableRect[2], stableRect[3], true);
            }
        };
    }

    private IRenderable createDebugHitboxRenderable() {
        final Transform2D transform = new Transform2D(
            0f, 0f, Settings.getWindowWidth(), HudStrip.PLAYFIELD_HEIGHT);
        return new IRenderable() {
            @Override public String getAssetPath() { return null; }
            @Override public ITransform getTransform() { return transform; }

            @Override
            public void render(IDrawContext ctx) {
                GdxDrawContext gdx = (GdxDrawContext) ctx;

                // player wall hitbox
                IBounds pb = player.getBounds();
                float[] pMin = pb.getMinPosition(); float[] pExt = pb.getExtent();
                gdx.rect(DBG_PLAYER, pMin[0], pMin[1], pExt[0], pExt[1], false);

                // enemy wall box (red) and damage zone (orange)
                for (HostileCharacter enemy : enemies) {
                    IBounds wb = enemy.getBounds();
                    float[] wMin = wb.getMinPosition(); float[] wExt = wb.getExtent();
                    gdx.rect(DBG_WALL_BOX, wMin[0], wMin[1], wExt[0], wExt[1], false);

                    IBounds db = enemy.getDamageBounds();
                    float[] dMin = db.getMinPosition(); float[] dExt = db.getExtent();
                    gdx.rect(DBG_DMG_BOX, dMin[0], dMin[1], dExt[0], dExt[1], false);
                }

                // item pickup bounds
                for (Item item : items) {
                    if (!item.isActive()) continue;
                    IBounds ib = item.getBounds();
                    float[] iMin = ib.getMinPosition(); float[] iExt = ib.getExtent();
                    gdx.rect(DBG_ITEM, iMin[0], iMin[1], iExt[0], iExt[1], false);
                }

                // room trigger zones
                for (float[] room : cachedRoomBounds) {
                    float tx = room[0] + (room[2] - GameConfig.ROOM_TRIGGER_WIDTH)  / 2f;
                    float ty = room[1] + (room[3] - GameConfig.ROOM_TRIGGER_HEIGHT) / 2f;
                    gdx.rect(DBG_ROOM, tx, ty, GameConfig.ROOM_TRIGGER_WIDTH, GameConfig.ROOM_TRIGGER_HEIGHT, false);
                }
            }
        };
    }

    private IRenderable createBackgroundRenderable() {
        final Transform2D transform = new Transform2D(
            0f, 0f, Settings.getWindowWidth(), HudStrip.PLAYFIELD_HEIGHT);
        return new IRenderable() {
            @Override public String getAssetPath() { return null; }
            @Override public ITransform getTransform() { return transform; }

            @Override
            public void render(IDrawContext ctx) {
                ((GdxDrawContext) ctx).drawTintedQuad(
                    SCENE_BG_COLOUR, 0f, 0f,
                    Settings.getWindowWidth(), HudStrip.PLAYFIELD_HEIGHT);
            }
        };
    }

}

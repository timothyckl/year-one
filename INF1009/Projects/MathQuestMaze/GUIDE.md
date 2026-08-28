# AbstractEngine — API Usage Guide

a comprehensive guide to building games and simulations with the AbstractEngine framework.

---

## Table of Contents

1. [Overview](#1-overview)
2. [Getting Started](#2-getting-started)
3. [Platform Adapters](#3-platform-adapters)
4. [Transforms and Bounds](#4-transforms-and-bounds)
5. [Entities](#5-entities)
6. [Scenes](#6-scenes)
7. [Input](#7-input)
8. [Movement](#8-movement)
9. [Collision](#9-collision)
10. [Rendering](#10-rendering)
11. [Managers](#11-managers)
12. [Putting It All Together](#12-putting-it-all-together)

---

## 1. Overview

AbstractEngine is a lightweight, platform-agnostic game engine written in Java. it provides the core systems every game or simulation needs — entity management, scene orchestration, input handling, physics movement, collision detection, and rendering — without coupling you to any particular graphics library or platform.

### Core Design Principles

- **Manager lifecycle** — all systems are `Manager` subclasses with a consistent `init` → `update` → `shutdown` lifecycle. the `Engine` sorts them by dependency order automatically.
- **Capability-based entities** — entities gain behaviour by implementing interfaces (`IMovable`, `ICollidable`, `IRenderItem`, etc.) rather than inheriting from deep class hierarchies.
- **Dimension-agnostic transforms** — positions, sizes, velocities, and bounds are expressed as `float[]` arrays, so the same engine code works for 2D, 3D, or any dimensionality.
- **Platform abstraction** — rendering, input, and asset loading are defined as interfaces. you supply platform-specific implementations (e.g. libGDX, LWJGL, JavaFX) and the engine stays decoupled.

---

## 2. Getting Started

### The Engine Lifecycle

the `Engine` class orchestrates the entire framework. its lifecycle has four phases:

1. **Registration** — register managers and updatables with the engine.
2. **Initialisation** — `engine.init()` topologically sorts managers by their declared dependencies, wires them together, and calls `init()` on each.
3. **Game loop** — each frame calls `engine.update(deltaTime)` then `engine.render()`.
4. **Shutdown** — `engine.shutdown()` tears down managers in reverse dependency order.

### Minimal Setup

```java
// create the engine
Engine engine = new Engine();

// register the core managers
EntityManager entityManager = new EntityManager();
engine.registerManager(entityManager);

SceneManager sceneManager = new SceneManager();
engine.registerManager(sceneManager);

// register at least one scene
sceneManager.registerScene(new MyScene());
sceneManager.setInitialScene("my-scene");

// initialise everything
engine.init();

// in your game loop:
engine.update(deltaTime);
engine.render();

// when exiting:
engine.shutdown();
```

> **note:** `registerManager` must be called *before* `engine.init()`. the engine indexes each manager by its class, so you can later retrieve it with `engine.getManager(EntityManager.class)`.

---

## 3. Platform Adapters

before the engine can poll input or draw anything, you need to provide platform-specific implementations for two contracts: `IInputSource` and `RenderManager`.

### Input Source

implement `IInputSource` to bridge your platform's raw input into the engine:

```java
public class MyInputSource implements IInputSource {
    @Override
    public boolean isKeyPressed(int keyCode) {
        // return true if the key identified by keyCode is currently held down
        return PlatformKeyboard.isDown(keyCode);
    }

    @Override
    public boolean isButtonPressed(int buttonCode) {
        // return true if the mouse/controller button is currently held down
        return PlatformMouse.isDown(buttonCode);
    }
}
```

pass this to `InputManager` at construction time:

```java
InputManager inputManager = new InputManager(new MyInputSource());
engine.registerManager(inputManager);
```

### Render Manager

extend `RenderManager` and implement the three factory methods:

```java
public class MyRenderManager extends RenderManager {
    @Override
    protected ISpriteBatch createSpriteBatch() {
        // return your platform's sprite batch implementation
        return new MySpriteBatch();
    }

    @Override
    protected IShapeRenderer createShapeRenderer() {
        // return your platform's shape renderer implementation
        return new MyShapeRenderer();
    }

    @Override
    protected IAssetStore createAssetStore() {
        // return your platform's asset loading implementation
        return new MyAssetStore();
    }
}
```

the `RenderManager` calls these factories during `onInit()`, storing the results in `batch`, `shapeRenderer`, and `assetStore`. during each frame's `render()` call, it iterates the render queue and draws each item using these resources.

#### ISpriteBatch

draws textured quads (sprites):

| Method | Purpose |
|--------|---------|
| `begin()` | starts a new drawing batch |
| `end()` | flushes buffered draws to the screen |
| `draw(Object textureHandle, float x, float y, float width, float height)` | draws a texture at the given position and size |
| `dispose()` | releases GPU resources |

#### IShapeRenderer

draws geometric primitives (rectangles, circles, lines):

| Method | Purpose |
|--------|---------|
| `begin()` | starts drawing filled shapes |
| `end()` | flushes the shape batch |
| `dispose()` | releases resources |

#### IAssetStore

loads and manages game assets:

| Method | Purpose |
|--------|---------|
| `loadTexture(String assetPath)` | loads a texture from the given path; blocks until ready and returns a texture handle |
| `finishLoading()` | flushes any queued loading operations |
| `dispose()` | disposes all loaded assets |

---

## 4. Transforms and Bounds

### ITransform

`ITransform` describes an entity's position and size in space. all values are `float[]` arrays, so the same interface works in any number of dimensions:

```java
public class Transform2D implements ITransform {
    private float[] position = new float[2];
    private float[] size = new float[2];

    public Transform2D(float x, float y, float width, float height) {
        position[0] = x;
        position[1] = y;
        size[0] = width;
        size[1] = height;
    }

    @Override
    public float getPosition(int axis) { return position[axis]; }

    @Override
    public void setPosition(int axis, float value) { position[axis] = value; }

    @Override
    public float getSize(int axis) { return size[axis]; }

    @Override
    public void setSize(int axis, float value) { size[axis] = value; }

    @Override
    public int getDimensions() { return 2; }
}
```

> **tip:** accessors are per-axis (e.g. `getPosition(0)` for x, `getPosition(1)` for y). this prevents callers from holding a reference to the internal array and mutating it externally. `getDimensions()` lets engine code verify dimensional consistency at runtime.

### IBounds

`IBounds` defines collision shapes for overlap detection. like `ITransform`, it is dimension-agnostic:

```java
public class Rectangle2D implements IBounds {
    private float[] minPosition = new float[2];
    private float[] extent = new float[2];

    public Rectangle2D(float x, float y, float width, float height) {
        minPosition[0] = x;
        minPosition[1] = y;
        extent[0] = width;
        extent[1] = height;
    }

    // default constructor for cached instances updated via set()
    public Rectangle2D() {
        this(0f, 0f, 0f, 0f);
    }

    @Override
    public boolean overlaps(IBounds other) {
        if (!(other instanceof Rectangle2D)) return false;
        Rectangle2D rect = (Rectangle2D) other;

        // separating axis theorem for axis-aligned rectangles
        float x = minPosition[0], y = minPosition[1];
        float w = extent[0], h = extent[1];
        float rx = rect.minPosition[0], ry = rect.minPosition[1];
        float rw = rect.extent[0], rh = rect.extent[1];

        return x < rx + rw && x + w > rx
            && y < ry + rh && y + h > ry;
    }

    @Override
    public float[] getMinPosition() { return minPosition; }

    @Override
    public float[] getExtent() { return extent; }

    @Override
    public void set(float[] minPosition, float[] extent) {
        System.arraycopy(minPosition, 0, this.minPosition, 0, 2);
        System.arraycopy(extent, 0, this.extent, 0, 2);
    }

    // convenience setter using individual floats
    public void set(float x, float y, float width, float height) {
        minPosition[0] = x; minPosition[1] = y;
        extent[0] = width;  extent[1] = height;
    }

    @Override
    public int getDimensions() { return 2; }
}
```

---

## 5. Entities

### The Entity Base Class

all game objects extend `Entity`, which provides a unique `UUID` and an active flag:

```java
public class MyEntity extends Entity {
    // Entity auto-generates an id via UUID.randomUUID()
    // use isActive() / setActive(boolean) to enable/disable
}
```

### Capability Interfaces

entities gain behaviour by implementing one or more interfaces:

| Interface | Capability |
|-----------|-----------|
| `ITransformable` | has a spatial transform (`getTransform()`) |
| `IMovable` | can move via physics (`getVelocity()`, `setVelocity()`, `getAcceleration()`, `setAcceleration()`, `move()`) |
| `ICollidable` | participates in collision detection (`getBounds()`, `onCollision()`) |
| `IRenderable` | can be drawn as a sprite (`getAssetPath()`) |
| `IRenderItem` | combines `IRenderable` + `ITransformable`; can be submitted to the render queue |
| `ICustomRenderable` | draws itself procedurally via `renderCustom(ISpriteBatch, IShapeRenderer)` |

### The SpriteEntity Pattern

for entities that combine movement, collision, and sprite rendering, consider a convenience base class:

```java
public abstract class SpriteEntity extends Entity
    implements IRenderItem, IMovable, ICollidable {

    protected final Transform2D transform;
    protected final String assetPath;
    protected float[] velocity = new float[2];
    protected float[] acceleration = new float[2];
    private final Rectangle2D bounds;

    protected SpriteEntity(String assetPath, float x, float y,
                           float width, float height) {
        this.assetPath = assetPath;
        this.transform = new Transform2D(x, y, width, height);
        this.bounds = new Rectangle2D();
    }

    @Override
    public String getAssetPath() { return assetPath; }

    @Override
    public ITransform getTransform() { return transform; }

    @Override
    public float[] getVelocity() { return velocity; }

    @Override
    public void setVelocity(float[] velocity) {
        System.arraycopy(velocity, 0, this.velocity, 0, 2);
    }

    @Override
    public float[] getAcceleration() { return acceleration; }

    @Override
    public void setAcceleration(float[] acceleration) {
        System.arraycopy(acceleration, 0, this.acceleration, 0, 2);
    }

    @Override
    public void move(float deltaTime) {
        // euler integration: v += a*dt; p += v*dt
        velocity[0] += acceleration[0] * deltaTime;
        velocity[1] += acceleration[1] * deltaTime;

        transform.setPosition(0, transform.getPosition(0) + velocity[0] * deltaTime);
        transform.setPosition(1, transform.getPosition(1) + velocity[1] * deltaTime);
    }

    @Override
    public IBounds getBounds() {
        // derive bounding box from transform (sync cached rectangle)
        bounds.set(
            transform.getPosition(0), transform.getPosition(1),
            transform.getSize(0),     transform.getSize(1)
        );
        return bounds;
    }

    @Override
    public abstract void onCollision(ICollidable other);
}
```

### Creating Entities with EntityFactory

use `EntityFactory` lambdas to create entities through the entity manager:

```java
// create entity via the entity manager
IEntityManager entities = context.get(IEntityManager.class);
Entity bucket = entities.createEntity(() -> new Bucket(300, 0));
```

`createEntity` calls the factory's `create()` method, adds the resulting entity to the internal store, and returns it.

---

## 6. Scenes

### Extending Scene

scenes control what the player sees and interacts with. extend `Scene` and implement the lifecycle hooks:

```java
public class GameScene extends Scene {
    public GameScene() {
        this.name = "game"; // used as the scene's registry key
    }

    @Override
    public void onEnter(SceneContext context) {
        // called once when this scene becomes active
        // create entities, load assets, set up state
    }

    @Override
    public void onExit(SceneContext context) {
        // called once when leaving this scene
        // dispose resources, remove entities
    }

    @Override
    public void update(float deltaTime, SceneContext context) {
        // called every frame (unless paused)
        // game logic goes here
    }

    @Override
    public void submitRenderable(IRenderQueue renderQueue) {
        // called every frame after update
        // push visible entities into the render queue
        renderQueue.queue(myEntity);
    }
}
```

### Optional Lifecycle Hooks

for scenes that can be suspended and resumed (e.g. pausing):

```java
@Override
public void onSuspend(SceneContext context) {
    // called when another scene is pushed on top
    // pause audio, save transient state
}

@Override
public void onResume(SceneContext context) {
    // called when returning to this scene from a suspended state
    // resume audio, refresh state
}
```

### SceneContext

`SceneContext` is the scene's window into the engine's systems:

| Method | Returns | Purpose |
|--------|---------|---------|
| `get(Class<T> type)` | `T` | look up any registered service by its type |
| `changeScene(String key)` | `void` | transition to a different scene |
| `suspendScene(String key)` | `void` | push a new scene, suspending the current one |
| `getScene(String key)` | `Scene` | retrieve a registered scene by name |

use `get` to access the core services:

```java
IEntityManager entities = context.get(IEntityManager.class);
IRenderQueue   queue    = context.get(IRenderQueue.class);
IInputQuery    input    = context.get(IInputQuery.class);
```

### Scene Transitions

```java
// hard transition — calls onExit on current, onEnter on target
context.changeScene("gameover");

// suspend transition — calls onSuspend on current, onEnter on target
// when the target exits, the original scene's onResume is called
context.suspendScene("pause");
```

### Registering Scenes

```java
SceneManager sceneManager = new SceneManager();
sceneManager.registerScene(new MenuScene());
sceneManager.registerScene(new GameScene(movementManager, collisionManager));
sceneManager.registerScene(new PauseScene());
sceneManager.setInitialScene("menu");
engine.registerManager(sceneManager);
```

> **note:** `setInitialScene` must be called before `engine.init()`. the scene manager will call `onEnter` on the initial scene during its own initialisation.

---

## 7. Input

the input system translates raw key/button codes into logical actions, letting you decouple game logic from specific key bindings.

### Defining Actions

create `ActionId` constants to represent logical actions:

```java
public class GameActions {
    public static final ActionId MOVE_LEFT  = new ActionId("MOVE_LEFT");
    public static final ActionId MOVE_RIGHT = new ActionId("MOVE_RIGHT");
    public static final ActionId JUMP       = new ActionId("JUMP");
    public static final ActionId PAUSE      = new ActionId("PAUSE");
}
```

### Configuring Bindings

bind physical keys to logical actions via `InputMapping`:

```java
InputManager inputManager = new InputManager(new MyInputSource());

// InputManager implements IInputMapping directly — bind keys on it directly
inputManager.bindKey(Keys.LEFT,  GameActions.MOVE_LEFT);
inputManager.bindKey(Keys.A,     GameActions.MOVE_LEFT);   // multiple keys per action
inputManager.bindKey(Keys.RIGHT, GameActions.MOVE_RIGHT);
inputManager.bindKey(Keys.D,     GameActions.MOVE_RIGHT);
inputManager.bindKey(Keys.SPACE, GameActions.JUMP);
inputManager.bindKey(Keys.ESCAPE, GameActions.PAUSE);

// you can also bind mouse/controller buttons
inputManager.bindButton(Buttons.LEFT, GameActions.SHOOT);

engine.registerManager(inputManager);
```

### Querying Input

use `IInputQuery` (available via `context.get(IInputQuery.class)`) to check action states:

```java
@Override
public void update(float deltaTime, SceneContext context) {
    IInputQuery input = context.get(IInputQuery.class);

    // check if an action is active (PRESSED or HELD)
    if (input.isActionActive(GameActions.MOVE_LEFT)) {
        // move the player left
    }

    // check for exact state transitions
    if (input.getActionState(GameActions.JUMP) == InputState.PRESSED) {
        // trigger a jump (only on the initial press, not while held)
    }

    if (input.getActionState(GameActions.PAUSE) == InputState.PRESSED) {
        context.suspendScene("pause");
    }
}
```

### InputState

| State | Meaning |
|-------|---------|
| `PRESSED` | the action just became active this frame |
| `HELD` | the action has been held since a previous frame |
| `RELEASED` | the action just became inactive this frame |

> **tip:** use `isActionActive()` for continuous actions like movement. use `getActionState() == InputState.PRESSED` for one-shot triggers like jumping or pausing.

---

## 8. Movement

the `MovementManager` drives physics-enabled entities each frame.

### Implementing IMovable

any entity that implements `IMovable` can be registered with the movement manager:

```java
@Override
public void move(float deltaTime) {
    // euler integration: v += a*dt; p += v*dt
    velocity[0] += acceleration[0] * deltaTime;
    velocity[1] += acceleration[1] * deltaTime;

    transform.setPosition(0, transform.getPosition(0) + velocity[0] * deltaTime);
    transform.setPosition(1, transform.getPosition(1) + velocity[1] * deltaTime);
}
```

the `MovementManager` calls `move(deltaTime)` on every registered movable each frame.

### Registration

register and unregister movables (typically in your scene's `onEnter` and `onExit`):

```java
movementManager.registerMovable(player);
movementManager.registerMovable(projectile);

// later, when removing:
movementManager.unregisterMovable(projectile);
```

### World Bounds

enable boundary clamping to keep entities within the play area:

```java
// set the world bounds (min corner and max corner per axis)
movementManager.setWorldBounds(
    new float[]{0, 0},         // min bounds
    new float[]{800, 600}      // max bounds
);
movementManager.setBoundariesEnabled(true);
```

when enabled, the movement manager clamps entity positions to these bounds after each `move()` call.

---

## 9. Collision

### Extending CollisionManager

`CollisionManager` is abstract — you must subclass it and implement `resolve()`:

```java
public class MyCollisionManager extends CollisionManager {
    @Override
    protected void resolve(List<CollisionPair> collisions) {
        // notify both entities in each collision pair
        for (CollisionPair pair : collisions) {
            pair.getEntityA().onCollision(pair.getEntityB());
            pair.getEntityB().onCollision(pair.getEntityA());
        }
    }
}
```

the `resolve` method receives a list of `CollisionPair` objects. each pair contains two `ICollidable` entities whose bounds overlapped during the `detect()` phase. you decide what happens: invoke callbacks, apply knockback, destroy entities, etc.

### Registering Collidables

```java
collisionManager.registerCollidable(player);
collisionManager.registerCollidable(enemy);

// when removing:
collisionManager.unregisterCollidable(enemy);
```

### Collision Callbacks

implement `onCollision` in your entities to respond:

```java
public class Bullet extends SpriteEntity {
    @Override
    public void onCollision(ICollidable other) {
        if (other instanceof Enemy) {
            setActive(false); // destroy the bullet
        }
    }
}
```

### Spatial Tree Broad Phase

for scenes with many collidables, enable spatial indexing to reduce the number of overlap checks:

```java
collisionManager.setWorldBounds(
    new float[]{0, 0},         // world minimum position
    new float[]{800, 600}      // world extent (width, height)
);
```

calling `setWorldBounds` activates the `SpatialTree` broad phase. the tree recursively subdivides space and only tests entities in overlapping regions, significantly reducing the O(n²) pair count.

---

## 10. Rendering

### Sprite Rendering with IRenderable

for texture-based entities, implement `IRenderItem` (which combines `IRenderable` and `ITransformable`):

```java
public class Background implements IRenderItem {
    private final Transform2D transform;
    private static final String ASSET_PATH = "background.png";

    public Background(float width, float height) {
        this.transform = new Transform2D(0, 0, width, height);
    }

    @Override
    public String getAssetPath() { return ASSET_PATH; }

    @Override
    public ITransform getTransform() { return transform; }
}
```

the `RenderManager` loads the texture via `IAssetStore.loadTexture(assetPath)` and draws it at the entity's position and size using `ISpriteBatch.draw()`.

### Procedural Rendering with ICustomRenderable

for entities that draw themselves programmatically, implement `ICustomRenderable`:

```java
public class HealthBar extends Entity implements IRenderItem, ICustomRenderable {
    private float healthPercent = 1.0f;

    @Override
    public String getAssetPath() {
        return null; // null signals the render manager to use custom rendering
    }

    @Override
    public void renderCustom(ISpriteBatch batch, IShapeRenderer shapeRenderer) {
        // draw a coloured bar using the shape renderer
        shapeRenderer.begin();
        // ... draw background bar, then fill bar based on healthPercent
        shapeRenderer.end();
    }

    @Override
    public ITransform getTransform() { return transform; }
}
```

when `getAssetPath()` returns `null` and the entity implements `ICustomRenderable`, the render manager delegates drawing to `renderCustom()` instead of using the standard sprite path.

### Submitting to the Render Queue

entities are not drawn automatically. each frame, your scene must push visible entities into the render queue in `submitRenderable()`:

```java
@Override
public void submitRenderable(IRenderQueue renderQueue) {
    renderQueue.queue(background);   // draw first (back layer)
    renderQueue.queue(player);       // draw on top
    renderQueue.queue(healthBar);    // draw last (front layer)
}
```

items are drawn in the order they are queued. the render queue is cleared automatically after each frame's render pass.

---

## 11. Managers

### Creating Custom Managers

extend `Manager` for systems that need only `init` / `shutdown`:

```java
public class AudioManager extends Manager {
    @Override
    protected void onInit() {
        // set up audio system
    }

    @Override
    protected void onShutdown() {
        // release audio resources
    }
}
```

extend `UpdatableManager` for systems that also need per-frame updates:

```java
public class ParticleManager extends UpdatableManager {
    @Override
    protected void onInit() {
        // allocate particle buffers
    }

    @Override
    protected void onUpdate(float deltaTime) {
        // advance particle simulations
    }

    @Override
    protected void onShutdown() {
        // release particle buffers
    }
}
```

### Declaring Dependencies

override `getDependencies()` to declare which managers yours depends on:

```java
@Override
@SuppressWarnings("unchecked")
public Class<? extends IManager>[] getDependencies() {
    return new Class[]{EntityManager.class, RenderManager.class};
}
```

the engine topologically sorts all managers by their dependencies, ensuring they are initialised in the correct order and shut down in reverse order.

### Wiring Dependencies

override `onWire(ManagerResolver)` to look up and store references to your dependencies:

```java
private EntityManager entityManager;
private RenderManager renderManager;

@Override
public void onWire(ManagerResolver resolver) {
    entityManager = resolver.resolve(EntityManager.class);
    renderManager = resolver.resolve(RenderManager.class);
}
```

`onWire` is called after all managers are registered but before `onInit()`, so your dependencies are guaranteed to be available.

---

## 12. Putting It All Together

here is a complete example wiring an engine with all managers, scenes, entities, and input for a simple game.

### Main Entry Point

```java
public class Main {
    private Engine engine;

    public void create() {
        engine = new Engine();

        // core managers
        EntityManager entityManager = new EntityManager();
        engine.registerManager(entityManager);

        // input
        InputManager inputManager = new InputManager(new MyInputSource());
        inputManager.bindKey(Keys.LEFT,  GameActions.MOVE_LEFT);
        inputManager.bindKey(Keys.RIGHT, GameActions.MOVE_RIGHT);
        inputManager.bindKey(Keys.SPACE, GameActions.JUMP);
        engine.registerManager(inputManager);

        // movement
        MovementManager movementManager = new MovementManager();
        movementManager.setWorldBounds(new float[]{0, 0}, new float[]{800, 600});
        movementManager.setBoundariesEnabled(true);
        engine.registerManager(movementManager);

        // collision
        MyCollisionManager collisionManager = new MyCollisionManager();
        collisionManager.setWorldBounds(new float[]{0, 0}, new float[]{800, 600});
        engine.registerManager(collisionManager);

        // rendering (platform-specific)
        MyRenderManager renderManager = new MyRenderManager();
        engine.registerManager(renderManager);

        // scenes
        SceneManager sceneManager = new SceneManager();
        sceneManager.registerScene(new MenuScene());
        sceneManager.registerScene(
            new GameScene(movementManager, collisionManager));
        sceneManager.setInitialScene("menu");
        engine.registerManager(sceneManager);

        // start
        engine.init();
    }

    public void gameLoop(float deltaTime) {
        engine.update(deltaTime);
        engine.render();
    }

    public void dispose() {
        engine.shutdown();
    }
}
```

### A Game Scene

```java
public class GameScene extends Scene {
    private final MovementManager movementManager;
    private final CollisionManager collisionManager;
    private Player player;

    public GameScene(MovementManager movementManager,
                     CollisionManager collisionManager) {
        this.name = "game";
        this.movementManager = movementManager;
        this.collisionManager = collisionManager;
    }

    @Override
    public void onEnter(SceneContext context) {
        // create the player entity
        IEntityManager entities = context.get(IEntityManager.class);
        player = (Player) entities.createEntity(() -> new Player(400, 50));

        // register with physics systems
        movementManager.registerMovable(player);
        collisionManager.registerCollidable(player);
    }

    @Override
    public void onExit(SceneContext context) {
        // clean up
        movementManager.unregisterMovable(player);
        collisionManager.unregisterCollidable(player);
        context.get(IEntityManager.class).removeEntity(player.getId());
    }

    @Override
    public void update(float deltaTime, SceneContext context) {
        IInputQuery input = context.get(IInputQuery.class);

        // set velocity based on input
        float speed = 200f;
        float vx = 0;
        if (input.isActionActive(GameActions.MOVE_LEFT))  vx -= speed;
        if (input.isActionActive(GameActions.MOVE_RIGHT)) vx += speed;
        player.setVelocity(new float[]{vx, player.getVelocity()[1]});
    }

    @Override
    public void submitRenderable(IRenderQueue renderQueue) {
        renderQueue.queue(player);
    }
}
```

### A Player Entity

```java
public class Player extends SpriteEntity {
    public Player(float x, float y) {
        super("player.png", x, y, 64, 64);
    }

    @Override
    public void onCollision(ICollidable other) {
        if (other instanceof Enemy) {
            // handle damage
        }
    }
}
```

### A Collision Manager

```java
public class MyCollisionManager extends CollisionManager {
    @Override
    protected void resolve(List<CollisionPair> collisions) {
        for (CollisionPair pair : collisions) {
            pair.getEntityA().onCollision(pair.getEntityB());
            pair.getEntityB().onCollision(pair.getEntityA());
        }
    }
}
```

---

## Quick Reference

| What you want to do | Interface/Class to use |
|---------------------|----------------------|
| Bootstrap the engine | `Engine` |
| Manage game objects | `EntityManager`, `Entity`, `EntityFactory` |
| Organise game states | `SceneManager`, `Scene`, `SceneContext` |
| Handle player input | `InputManager`, `ActionId`, `InputMapping`, `IInputQuery` |
| Move entities with physics | `MovementManager`, `IMovable` |
| Detect and resolve collisions | `CollisionManager`, `ICollidable`, `IBounds` |
| Draw sprites | `RenderManager`, `IRenderItem`, `IRenderable` |
| Draw procedurally | `ICustomRenderable` |
| Create custom systems | `Manager`, `UpdatableManager`, `ManagerResolver` |
| Define spatial data | `ITransform`, `ITransformable` |

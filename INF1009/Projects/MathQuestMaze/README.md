# Math Quest Maze & Abstract Simulation Engine

![CI Pipeline](https://github.com/timothyckl/INF1009-P1-09-AbstractEngine/actions/workflows/CI-test.yml/badge.svg)

Math Quest Maze is an educational game for primary school students (ages 6–10) that builds arithmetic fluency through maze navigation and problem-solving. It is built on top of the **Abstract Simulation Engine** — a reusable, framework-agnostic game engine following OOP and SOLID principles.

---

## Table of Contents

- [Game: Math Quest Maze](#game-math-quest-maze)
- [Engine Architecture](#engine-architecture)
- [Project Structure](#project-structure)
- [CI/CD Pipeline](#cicd-pipeline)
- [Getting Started](#getting-started)
- [Requirements](#requirements)

---

## Game: Math Quest Maze

**Screenshots**

| Menu | Gameplay |
|------|----------|
| <img width="1276" height="715" alt="menu-scene" src="https://github.com/user-attachments/assets/eb520c4c-cb80-4481-abe1-3724838ffd15" /> | <img width="1275" height="698" alt="game-scene" src="https://github.com/user-attachments/assets/45b622a5-7014-40d9-8b09-571fd90bbf92" /> |

| How To Play | Settings |
|-------------|----------|
| <img width="1277" height="714" alt="how-to-play-scene" src="https://github.com/user-attachments/assets/5e5a4773-a644-489c-9ef7-9eb5b9b2be4a" /> | <img width="1275" height="717" alt="setting-scene" src="https://github.com/user-attachments/assets/89ad6f21-9546-4f63-b7d8-eda9f8ec972e" /> |

**Gameplay**
- A maths question is displayed each round; the maze contains four answer rooms — enter the correct one to score a point
- Win by answering 5 questions correctly; wrong rooms cost 1 HP and re-present the same question
- Start each round with 3 HP; collect hearts (1–3 per round) to restore 1 HP each

**Difficulty**

| Level  | Operand range | Operations | Enemies |
|--------|---------------|------------|---------|
| Easy   | 1 – 10        | Addition, Subtraction | Fewer, placed farther away |
| Medium | 1 – 20        | Addition, Subtraction, Multiplication | Moderate count and aggression |
| Hard   | 1 – 100       | Addition, Subtraction, Multiplication, Division | More, placed in closer proximity |

**Enemy AI** — PATROL → CHASE → ATTACK

| Enemy | Patrol | Detection | Chase | Attack |
|-------|--------|-----------|-------|--------|
| Goblin | 40 px/s, reverses every 1.5 s | 300 px radius | 80 px/s | Within 50 px |
| Skeleton | Waypoints at 58 px/s | 260 px, line-of-sight | 76 px/s | Within 40 px |

**Controls**

| Action | Primary | Alternate |
|--------|---------|-----------|
| Move Up | W | ↑ |
| Move Down | S | ↓ |
| Move Left | A | ← |
| Move Right | D | → |
| Pause / Back | Escape | Backspace |
| Confirm | Space | — |

All movement bindings are remappable via the Settings screen.

**Scene flow:** Main Menu → Gameplay → Pause / Level Complete / Game Over

---

## Engine Architecture

<img width="1500" height="734" alt="engine-uml" src="https://github.com/user-attachments/assets/ad92dbba-c62a-4da1-b619-21e0ff97cad1" />

**Layers**

| Layer | Package | Responsibility |
|-------|---------|----------------|
| Abstract Engine | `com.p1_7.abstractengine` | Framework-level abstractions; zero libGDX imports |
| Game | `com.p1_7.game` | All game logic; depends only on engine abstractions |
| Platform | `com.p1_7.game.platform` | libGDX implementations; sole framework dependency |

**Abstract base classes**

- **`Manager`** – init/shutdown lifecycle for all subsystems
- **`UpdatableManager`** – extends `Manager` to participate in the per-frame update loop
- **`Entity`** – base for all game objects; assigned unique IDs and active state
- **`Scene`** – base for game states with lifecycle hooks (onEnter, onExit, onSuspend, onResume, update, submitRenderable)

**Concrete managers**

- `EntityManager` – entity creation, removal, and repository access
- `SceneManager` – scene transitions, deferred to the next tick to prevent mid-frame state changes
- `MovementManager` – advances all registered `IMovable` entities each frame; clamps to world bounds
- `CollisionManager` – `SpatialTree` broadphase + `CollisionDetector` narrowphase
- `RenderManager` – drives the render queue via `IDrawContext`, `ISpriteBatch`, and `IShapeRenderer`
- `InputManager` – maps `IInputSource` events to `ActionId` constants; supports rebindable bindings via `IInputExtension`

**Innovations**

- **Topological manager initialisation** — managers declare dependencies via `getDependencies()`; `DependencySorter` builds a `DirectedAcyclicGraph` and applies Kahn's algorithm at startup to derive a valid init order. Circular dependencies are detected and reported with clear error messages.
- **Dimension-agnostic spatial tree** — `SpatialTree` generalises quadtree/octree to N dimensions, producing 2<sup>d</sup> children per node. Child placement uses a per-axis bitmask; entities spanning a midpoint stay in the parent.
- **Platform-neutral interfaces** — rendering and input are expressed as engine-layer interfaces (`IDrawContext`, `ISpriteBatch`, `IShapeRenderer`, `IAssetStore`, `IInputSource`); all libGDX coupling lives in `platform/`.
- **Type-keyed manager registry** — `Engine.getManager(Class<T>)` resolves any manager by concrete class, superclass, or interface, providing compile-safe service lookup without a framework dependency.

**SOLID & Design Patterns**

| Principle / Pattern | Application |
|---------------------|-------------|
| SRP | `MovementManager` only advances positions; `QuestionGenerator` only produces questions; `GamePhaseController` only drives phase transitions |
| OCP | `Manager` exposes `onInit()`/`onShutdown()` hooks; `CollisionManager` declares abstract `resolve()` — subclasses add behaviour without modifying the base |
| LSP | All scenes managed as `Scene`; `Player`, `Goblin`, `Skeleton` are interchangeable as `Character` / `IRenderable` / `IMovable` / `ICollidable` |
| ISP | `IEntityRepository` (read) / `IEntityMutator` (write); `IInputQuery` (state) / `IInputMapping` (remapping) |
| DIP | `Engine` operates on `IManager`; `RenderManager` depends on `ISpriteBatch`, `IShapeRenderer`, `IAssetStore` — never concrete types |
| Template Method | `Manager.init()` and `shutdown()` are `final`; behaviour added by overriding `onInit()`/`onShutdown()` |
| Factory | `EntityFactory` decouples entity creation from `EntityManager`; `RenderManager` is an abstract factory for rendering resources |
| Observer | `PlayerDamageListener`, `ItemCollectionListener`, `GamePhaseListener` decouple event producers from consumers |
| State | `RoundPhase` FSM (`CHOOSING` → `QUESTION_INTRO` → `FEEDBACK` → `ROUND_RESET` → `LEVEL_COMPLETE` / `GAME_OVER`) driven by `GamePhaseController` |
| Facade | `LevelOrchestrator` hides round orchestration from `GameScene`; `SceneContext` wraps manager access; `IDrawContext` hides pass-switching |
| Service Locator | `Engine.getManager(Class<T>)` and `SceneContext.get(Class<T>)` resolve dependencies by type at runtime |

---

## Project Structure

### Game layer

```bash
game/
├── audio/       # AudioManager, IAudioManager
├── character/   # Player, Goblin, Skeleton, HostileCharacter, EnemyDamageZone,
│                #   GameMovementManager, PlayerDamageListener
├── collectible/ # Item, Heart, ItemCollectionListener
├── font/        # FontManager, IFontManager
├── hud/         # GameHudRenderer, HudStrip, QuestionPanel
├── input/       # GameActions, ICursorSource
├── math/        # Difficulty, Operation, MathQuestion, QuestionGenerator
├── maze/        # MazeLayout, MazeCollisionManager, WallCollidable
├── platform/    # GdxRenderManager, GdxInputSource, GdxDrawContext, GdxAssetStore,
│                #   GdxSpriteBatch, GdxShapeRenderer, GdxCursorSource
├── round/       # GameRound, LevelOrchestrator, GamePhaseController, EnemyController,
│                #   ItemSpawner, MovementPipeline, RoomAssigner, RoomAssignment, RoundPhase
├── scenes/      # MenuScene, GameScene, PauseScene, GameOverScene, LevelCompleteScene,
│                #   HowToPlayScene, SettingScene
├── spatial/     # Transform2D, Bounds2D, IDisposable
└── ui/          # Button, MenuButton, Slider, VolumeSlider, SfxSlider, BrightnessSlider,
                 #   BrightnessOverlay, RemapSlot, Text, BackgroundImage
```

### Engine layer

```bash
abstractengine/
├── collision/   # ICollidable, IBounds, CollisionManager, CollisionDetector, SpatialTree, CollisionPair
├── engine/      # Engine, Manager, UpdatableManager, IManager, IUpdatable,
│                #   ManagerResolver, DependencySorter, DirectedAcyclicGraph
├── entity/      # Entity, EntityManager, EntityFactory, IEntityManager, IEntityMutator, IEntityRepository
├── input/       # InputManager, InputMapping, IInputManager, IInputQuery, IInputSource,
│                #   IInputMapping, IInputExtension, IInputExtensionRegistry,
│                #   ActionId, InputBindingSpec, InputEvent, InputState
├── movement/    # MovementManager, IMovable
├── render/      # RenderManager, RenderQueue, IRenderable, IRenderQueue,
│                #   IDrawContext, IAssetStore, ISpriteBatch, IShapeRenderer
├── scene/       # Scene, SceneManager, SceneContext
└── transform/   # ITransform, ITransformable
```

---

## CI/CD Pipeline

Every push and pull request is automatically built and tested via GitHub Actions. The suite comprises 22 JUnit 5 test classes covering dependency resolution, collision detection, input state transitions, scene lifecycle, question generation, and round state machine behaviour.

## Getting Started

```bash
./gradlew run
```

## Requirements

- Java 8 or higher
- Gradle (libGDX is managed automatically as a Gradle dependency)

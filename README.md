# Static.exe — 2D Glitch-Based Narrative Game

A Java/LibGDX platformer where the simulation you inhabit is slowly breaking apart.
Toggle between stable and glitched versions of the world to solve puzzles,
uncover the truth from fractured NPCs, and choose your ending.

### Steps

1. **Unzip** `static_exe_game.zip` anywhere on your machine.
2. **Open** the `static_exe/` folder in IntelliJ IDEA as a Gradle project.
   - File → Open → select the `static_exe` folder → Trust Project
3. **Wait** for Gradle sync to finish (it downloads LibGDX automatically ~30 sec).
4. **Run** the game:
   - Expand `desktop/src/main/java/com/staticgame/desktop/`
   - Right-click `DesktopLauncher` → Run

Or from a terminal in the project root:
```
./gradlew desktop:run          # run the game
./gradlew desktop:dist         # build a standalone JAR in /dist
```

> **macOS only:** if you get a JVM crash, add `-XstartOnFirstThread` to your
> run configuration VM options (already handled in the Gradle `run` task).

---

## Controls

| Key | Action |
|-----|--------|
| `A` / `←`  | Move left |
| `D` / `→`  | Move right |
| `W` / `↑` / `Space` | Jump |
| `E` | Interact with NPC |
| `R` | Reality Shift (toggle world state) |
| `Esc` | Return to main menu |
| `Enter` | Confirm on ending screen |
| `M` | Return to menu from ending screen |

---

## How to Get Each Ending

| Ending | How |
|--------|-----|
| **ACCEPT** | Reach the exit without talking to 3+ NPCs |
| **RESIST** | Talk to 3 or more NPCs, then reach the exit |
| **CORRUPT** | Shift reality 10+ times before reaching the exit |

The glitch intensity bar (top-right) shows how corrupted the world is.
Watch it — the more you shift, the harder the RESIST ending becomes.

---

## Project Structure

```
static_exe/
├── core/src/main/java/com/staticgame/
│   ├── StaticGame.java            Entry point (extends LibGDX Game)
│   ├── utils/
│   │   └── GameState.java         Persistent playthrough data
│   ├── entities/
│   │   ├── Entity.java            Abstract base (inheritance root)
│   │   ├── Player.java            Movement, jumping, interaction
│   │   └── npcs/
│   │       ├── NPC.java           Abstract NPC (polymorphism root)
│   │       ├── RepeaterNPC.java   Loops same line, worsens with shifts
│   │       ├── AwareNPC.java      Remembers visits, advances dialogue
│   │       └── GlitchNPC.java     Only exists in glitch state
│   ├── world/
│   │   ├── Tile.java              8 tile types incl. GLITCH_ONLY / NORMAL_ONLY
│   │   ├── World.java             Level map, NPC placement, exit detection
│   │   ├── PuzzleElement.java     Two-state activation puzzle
│   │   └── BackgroundRenderer.java Procedural parallax background
│   ├── systems/
│   │   ├── CollisionSystem.java   Separated-axis AABB physics
│   │   ├── RealityShiftSystem.java Core mechanic + visual effects
│   │   ├── CameraManager.java     Smooth follow + screen shake
│   │   ├── UIManager.java         Typewriter dialogue, HUD, prompts
│   │   └── AudioManager.java      Music + SFX (graceful if files missing)
│   └── screens/
│       ├── MenuScreen.java        Animated glitch title screen
│       ├── GameScreen.java        Main loop — orchestrates all systems
│       └── EndingScreen.java      3 endings with typewriter + corrupt FX
└── desktop/src/main/java/com/staticgame/desktop/
    └── DesktopLauncher.java       main() — run this class
```

---

## OOP Concepts Demonstrated

### Inheritance
```
Entity (abstract)
├── Player
└── NPC (abstract)
    ├── RepeaterNPC
    ├── AwareNPC
    └── GlitchNPC
```

### Abstraction
`Entity` declares `update(float delta)` and `render(SpriteBatch batch)` as abstract —
every subclass must implement them, but callers don't care which type they have.

`NPC` declares `speak(boolean, GameState)` and `onInteract(Player, GameState)` as
abstract — each NPC type provides its own completely different implementation.

### Polymorphism
`World` holds `List<NPC> npcs`. In `update()` it calls `npc.update(delta)` on all of
them — RepeaterNPC, AwareNPC, and GlitchNPC all respond differently without World
needing to know which type each one is.

### Encapsulation
Each system owns its own state and exposes only what others need:
- `GameState` is the only shared mutable object — everything else is private
- `Player`'s velocity is private; `CollisionSystem` uses `pushOutX/Y()` and
  `setVelocityY()` rather than touching fields directly
- `RealityShiftSystem` owns the shift timer and visual effects; callers just call `shift()`

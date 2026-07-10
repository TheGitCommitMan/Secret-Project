# Among Us - Jetpack Compose High-Fidelity Recreation

A complete, stunning, high-fidelity 2D recreation of **Among Us** built entirely from absolute scratch on Android using **Kotlin, Jetpack Compose, and SQLite (Room)**. This project implements a fully offline-simulated multiplayer environment with advanced Bot AI, fully interactive task minigames, a dedicated lobby hosting and joining system, and modern Material 3 styling.

---

## 🚀 Key Implemented Features

### 1. Advanced Player Character Controller
* **Smooth Dual-Input Movement**: Support for physical keyboard inputs (`W`, `A`, `S`, `D` / Arrow keys) and a responsive, fluid virtual joystick for mobile screens.
* **Velocity Normalization**: Custom diagonal velocity checking to ensure players walk at the exact same speed whether moving horizontally, vertically, or diagonally.
* **Robust Sliding Collisions**: Complete axis-aligned sliding collision resolution against both map walls and other active players.
* **Procedural Walk Animations**: The crewmate's legs dynamically animate using custom sine-wave canvas math, scaling walking speed and locking to a halt when still.

### 2. Fully Interactive Task Minigames
* **Swipe Card (Admin)**: Interactive wallet slide that calculates drag speed. Alerts the player if they swipe *Too Fast* or *Too Slow*.
* **Fix Wiring (Electrical)**: Interactive click-and-drag wiring system requiring players to drag and match matching colored terminal cables.
* **Prime Shields (Shields)**: Responsive hexagonal grid panels. Tap red offline panels to prime secondary defensive shields.
* **Submit Scan (MedBay)**: Fully automated bio-scanner platform. Stand on the deck to run a diagnostic medical timeline scan.
* **Download Data (Cafeteria)**: Live cyberspace downloader showing file folders transfer progression from external nodes to the Admin computer.
* **Clear Asteroids (Weapons)**: Canvas-driven shooter. Tap flying space rocks on a radar crosshair to shoot them down.

### 3. Lobby Hosting & Joining System
* **Lobby Browser**: Discover available simulated public spaceship rooms with full map, player count, and config parameters.
* **Lobby Hosting**: Host custom private lobbies generating unique **6-letter room codes**.
* **Join by Room Code**: Enter a unique room code in the search bar to immediately jump into a hosted lobby.

### 4. Third-Party Bot API Integration
* **API Key Dashboard**: A custom administrative panel integrated natively into the Customization screen.
* **Secure Token Generation**: Generates cryptographically secure, high-fidelity integration tokens prefixed with `au_live_` for automated bots.
* **Key Lifecycle Management**: View active keys, revoke compromised tokens (converting them to masked text), or completely delete integrations.

### 5. Polished Among Us Aesthetic
* **Vector Art Painter**: Custom canvas-drawn crewmates featuring realistic helmets, backpacks, and visors with realistic gradient glares.
* **Cosmetic Layering**: Support for customizable suits (Suit, Astronaut, Doctor, Police) and hats (Sprout, Toilet Paper, Viking, Chef, Cowboy, Astronaut).
* **Game Loop Engine**: Implements the full gameplay loop including emergency meeting voting, anonymous ballots, ejection screens, ghost sports, and localized proximity vision.

---

## 🎨 Media & Visuals
* **Canvas-Drawn Crewmates**: Every crewmate is rendered procedurally using Android's Canvas API, allowing for dynamic color swaps and smooth animation of backpacks and visors.
* **Interactive Task Visuals**: Task minigames feature custom-drawn interfaces—from the sliding card in Admin to the reactive wiring in Electrical—providing a tactile feel built entirely without external image assets.

---

## 🛠️ Controls & How to Play

* **Movement**: Use **`W`, `A`, `S`, `D`** or **Arrow keys** on a keyboard, or the **Virtual Joystick** on touch screens.
* **Interactions**: Walk up to task machines (yellow highlights) or vent grates (for Impostors). A contextual button (`USE`, `VENT`, `REPORT`, or `SABOTAGE`) will light up in the bottom right corner.
* **Bot Customization**: Go to **Customize** from the main menu to change your crewmate's name, color, suit skin, hat, or manage bot API keys.

---

## 📂 Core Project Architecture

```
app/src/main/java/com/example/game/
├── MainActivity.kt        # Pre-game screens (Main Menu, Lobby, Customize, Stats, Achievements)
├── ActiveGameView.kt      # Main active game viewport (Map rendering, keyboard input, HUD overlay)
├── GameViewModel.kt       # Core state machine, bot decision AI, physics loop, game configurations
├── GameModels.kt          # Strong types, Lobbies, Sabotages, Sound triggers, and BotApiKey data classes
├── GameMapData.kt         # Map geometry, rooms layout, vent networks, and collision boundaries
├── CrewmatePainter.kt     # Canvas rendering logic for crewmates, dead bodies, skins, and hats
└── TaskMinigames.kt       # Fully interactive Compose UI components for all 6 core mini-games
```

---

## 📦 Building & Installation

This project is built using Gradle Kotlin DSL.
1. Sync project files with **Gradle 8.0+** and **Kotlin 1.8.0+**.
2. Run the task `:app:assembleDebug` to generate a compiled APK.
3. Install onto any compatible Android device or running emulator.

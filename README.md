# 🌌 Among Us - Jetpack Compose High-Fidelity Recreation

**A complete, stunning, high-fidelity 2D recreation of Among Us built from scratch on Android.**

This project is a technical showcase of **Kotlin, Jetpack Compose, and Firebase Firestore**, implementing a fully simulated multiplayer environment with advanced Bot AI, real-time matchmaking, and professional-grade game systems.

---

## 🚀 Advanced Implemented Features

### 1. Real-Time Firestore Matchmaking
*   **Live Lobby System**: Powered by **Firebase Firestore** for real-time room discovery, player synchronization, and state updates.
*   **6-Letter Room Codes**: Unique, cryptographically generated codes for private lobby hosting.
*   **Dynamic Synchronization**: Real-time updates for player colors, skins, and "Ready" statuses within the lobby.

### 2. High-Performance Character Controller
*   **Precision Movement**: Dual-input support for physical keyboards (`W`, `A`, `S`, `D`) and a custom responsive virtual joystick.
*   **Movement Normalization**: Sophisticated velocity checking to ensure consistent speed across horizontal, vertical, and diagonal vectors.
*   **Collision Resolver**: Robust sliding collision resolution against map geometry and dynamic player entities.

### 3. Comprehensive State Management
*   **11 Screen States**: A sophisticated navigation architecture managing everything from the **Main Menu** and **Lobby Browser** to the **Active Game View**, **Emergency Meetings**, and **Ejection Sequences**.
*   **Game Loop Engine**: Implements the full gameplay lifecycle: Proximity Vision, Sabotage events, Venting, and Localized Voting Ballots.

### 4. Interactive Task Minigames
*   **Swipe Card**: Real-time drag speed calculation with *Too Fast/Too Slow* feedback.
*   **Fix Wiring**: Physics-based cable matching.
*   **Submit Scan**: Automated diagnostic medical timeline animation.
*   **Clear Asteroids**: Canvas-driven shooter with radar crosshair tracking.

### 5. Third-Party Bot API Manager
*   **Developer Dashboard**: Integrated management panel for external bot integrations.
*   **Secure API Tokens**: Generation of `au_live_` prefixed tokens for high-fidelity bot control.
*   **Lifecycle Control**: Full visibility, masking, revoking, and deletion of active integration tokens.

---

## 🎨 Aesthetic & Rendering
*   **Vector Art Painter**: Custom canvas-drawn crewmates with realistic gradients and lighting glares.
*   **Cosmetic Layering**: Support for multi-layer rendering of Suits (Police, Doctor, Astronaut) and Hats (Sprout, Viking, Cowboy).

---

## 📂 Architecture Overview

```
app/src/main/java/com/example/game/
├── MainActivity.kt        # State Coordinator (11 unique screen states)
├── GameViewModel.kt       # Physics Loop, Firestore Matchmaking, & Bot AI
├── ActiveGameView.kt      # High-performance Canvas Rendering & Input handling
├── CrewmatePainter.kt     # Procedural Animation & Cosmetic Layering
└── TaskMinigames.kt       # Interactive Compose UI for all 6 core tasks
```

---

## 🛠️ Controls
*   **Move**: `W`, `A`, `S`, `D` / Arrow Keys or Virtual Joystick.
*   **Action**: Contextual `USE`, `VENT`, or `REPORT` buttons trigger based on proximity.
*   **Admin**: Access the **Customize** menu to manage Bot API keys and Crewmate appearance.

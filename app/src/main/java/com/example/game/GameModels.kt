package com.example.game

import androidx.compose.ui.graphics.Color
import java.util.UUID

enum class GameScreen {
    MainMenu,
    LobbyBrowser,
    LobbyRoom,
    Customize,
    Stats,
    Achievements,
    Settings,
    ActiveGame,
    EmergencyMeeting,
    EjectionScreen,
    GameOverScreen
}

data class LobbyInfo(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val host: String = "",
    val playersCount: Int = 0,
    val maxPlayers: Int = 10,
    val mapName: String = "The Skeld",
    val impostorCount: Int = 1,
    val code: String = (1..6).map { "ABCDEFGHIJKLMNOPQRSTUVWXYZ".random() }.joinToString("")
)

enum class MapName {
    TheSkeld,
    MiraHQ,
    Polus
}

enum class PlayerRole {
    Crewmate,
    Impostor
}

data class GameTask(
    val id: String,
    val roomName: String,
    val taskType: TaskType,
    val isCompleted: Boolean = false,
    val progress: Float = 0f // For download etc.
)

enum class TaskType {
    SwipeCard,
    FixWiring,
    PrimeShields,
    SubmitScan,
    DownloadData,
    ClearAsteroids
}

data class GameVent(
    val id: String,
    val roomName: String,
    val x: Float,
    val y: Float,
    val connectedVentIds: List<String>
)

data class GameRoom(
    val name: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val color: Color
)

data class DeadBody(
    val playerId: String,
    val playerName: String,
    val colorName: String,
    val x: Float,
    val y: Float,
    val roomName: String
)

enum class ActiveSabotageType {
    None,
    ReactorMeltdown,
    O2Depletion,
    LightsOut
}

data class ActiveSabotage(
    val type: ActiveSabotageType,
    val timer: Int, // countdown in seconds
    val isResolvedLeft: Boolean = false,
    val isResolvedRight: Boolean = false
)

enum class SoundType {
    Alarm,
    Kill,
    Vent,
    Report,
    Emergency,
    TaskComplete,
    MeetingVote,
    GameWin,
    GameLose
}

// Colors
val AmongUsColors = mapOf(
    "Red" to Color(0xFFC61A1A),
    "Blue" to Color(0xFF1D32C4),
    "Green" to Color(0xFF127F1C),
    "Pink" to Color(0xFFE455D4),
    "Orange" to Color(0xFFE07914),
    "Yellow" to Color(0xFFF3F315),
    "Black" to Color(0xFF3F474E),
    "White" to Color(0xFFD6E0F0),
    "Purple" to Color(0xFF6B2FBC),
    "Cyan" to Color(0xFF38E5E5),
    "Lime" to Color(0xFF50EF39)
)

data class BotApiKey(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val token: String,
    val createdAt: String,
    val isRevoked: Boolean = false
)

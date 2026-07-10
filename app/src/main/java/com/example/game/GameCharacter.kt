package com.example.game

data class GameCharacter(
    val id: String,
    val name: String,
    val colorName: String,
    val isImpostor: Boolean,
    val isBot: Boolean,
    val x: Float,
    val y: Float,
    val vx: Float = 0f,
    val vy: Float = 0f,
    val isDead: Boolean = false,
    val isGhost: Boolean = false,
    val hasVoted: Boolean = false,
    val votedForId: String? = null, // String id of player, "skip", or null
    val currentRoom: String = "Cafeteria",
    
    // AI navigation & decision making
    val aiState: String = "idle", // "wandering", "moving_to_task", "doing_task", "hunting", "fixing_sabotage", "venting"
    val targetX: Float? = null,
    val targetY: Float? = null,
    val aiTaskTimer: Float = 0f,
    val currentTaskTargetId: String? = null,
    val currentVentId: String? = null,
    val ventTimer: Float = 0f,
    val suspicionScore: Int = 0,
    val killCooldown: Float = 25f,
    val sabotageCooldown: Float = 20f,
    
    // Customization
    val hatId: String = "none",
    val skinId: String = "none",
    val petId: String = "none"
) {
    val speed: Float = 4f
}

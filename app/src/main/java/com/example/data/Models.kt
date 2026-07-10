package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfile(
    @PrimaryKey val id: Int = 1,
    val playerName: String = "Crewmate",
    val colorName: String = "Red",
    val hatId: String = "none",
    val skinId: String = "none",
    val petId: String = "none"
)

@Entity(tableName = "game_stats")
data class GameStats(
    @PrimaryKey val id: Int = 1,
    val gamesPlayed: Int = 0,
    val crewmateWins: Int = 0,
    val impostorWins: Int = 0,
    val totalKills: Int = 0,
    val tasksCompleted: Int = 0,
    val sabotagesFixed: Int = 0,
    val meetingsCalled: Int = 0
)

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long = 0L
)

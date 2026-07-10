package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {
    val playerProfile: Flow<PlayerProfile?> = appDao.getPlayerProfile()
    val gameStats: Flow<GameStats?> = appDao.getGameStats()
    val achievements: Flow<List<Achievement>> = appDao.getAchievements()

    suspend fun savePlayerProfile(profile: PlayerProfile) {
        appDao.savePlayerProfile(profile)
    }

    suspend fun saveGameStats(stats: GameStats) {
        appDao.saveGameStats(stats)
    }

    suspend fun incrementStats(
        gamesPlayed: Int = 0,
        crewmateWins: Int = 0,
        impostorWins: Int = 0,
        totalKills: Int = 0,
        tasksCompleted: Int = 0,
        sabotagesFixed: Int = 0,
        meetingsCalled: Int = 0
    ) {
        val current = appDao.getGameStatsSync() ?: GameStats()
        val updated = current.copy(
            gamesPlayed = current.gamesPlayed + gamesPlayed,
            crewmateWins = current.crewmateWins + crewmateWins,
            impostorWins = current.impostorWins + impostorWins,
            totalKills = current.totalKills + totalKills,
            tasksCompleted = current.tasksCompleted + tasksCompleted,
            sabotagesFixed = current.sabotagesFixed + sabotagesFixed,
            meetingsCalled = current.meetingsCalled + meetingsCalled
        )
        appDao.saveGameStats(updated)

        // Check stats-based achievements
        if (updated.totalKills >= 1) {
            unlockAchievement("first_kill")
        }
        if (updated.tasksCompleted >= 15) {
            unlockAchievement("taskmaster")
        }
        if (updated.meetingsCalled >= 1) {
            unlockAchievement("meeting_maker")
        }
    }

    suspend fun unlockAchievement(id: String) {
        appDao.unlockAchievement(id)
    }
}

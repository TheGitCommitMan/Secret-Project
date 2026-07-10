package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [PlayerProfile::class, GameStats::class, Achievement::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "among_us_db"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Prepopulate default achievements, stats, and profile
                        scope.launch(Dispatchers.IO) {
                            val dao = getDatabase(context, scope).appDao()
                            dao.savePlayerProfile(PlayerProfile())
                            dao.saveGameStats(GameStats())
                            dao.saveAchievements(getDefaultAchievements())
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private fun getDefaultAchievements(): List<Achievement> {
            return listOf(
                Achievement("first_kill", "Blood on My Hands", "Perform your very first kill as an Impostor."),
                Achievement("taskmaster", "Taskmaster", "Complete a total of 15 tasks in games."),
                Achievement("deceiver", "Master of Deception", "Win a game as an Impostor without being voted off."),
                Achievement("first_scan", "MedBay Regular", "Submit a scan in the MedBay."),
                Achievement("meeting_maker", "Emergency Caller", "Call an Emergency Meeting to discuss sus behavior."),
                Achievement("vent_traveler", "Underground Network", "Travel through a vent successfully as Impostor."),
                Achievement("saviour", "Sabotage Master", "Resolve an active sabotage before the timer runs out.")
            )
        }
    }
}

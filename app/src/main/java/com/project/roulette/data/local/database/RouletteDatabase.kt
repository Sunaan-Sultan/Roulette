package com.project.roulette.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.project.roulette.data.local.database.entity.SpinHistoryEntity
import com.project.roulette.data.local.database.entity.WheelEntity

/**
 * Room database configuration for the Roulette app.
 * Singleton pattern: single instance managed by DI container.
 */
@Database(
    entities = [WheelEntity::class, SpinHistoryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class RouletteDatabase : RoomDatabase() {
    abstract fun wheelDao(): WheelDao
    abstract fun spinHistoryDao(): SpinHistoryDao

    companion object {
        @Volatile
        private var instance: RouletteDatabase? = null

        fun getInstance(context: Context): RouletteDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    RouletteDatabase::class.java,
                    "roulette_database"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
    }
}


package com.project.roulette.di

import android.content.Context
import com.project.roulette.data.local.database.RouletteDatabase
import com.project.roulette.data.local.database.SpinHistoryDao
import com.project.roulette.data.local.database.WheelDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database and DAO dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideRouletteDatabase(@ApplicationContext context: Context): RouletteDatabase =
        RouletteDatabase.getInstance(context)

    @Provides
    @Singleton
    fun provideWheelDao(database: RouletteDatabase): WheelDao =
        database.wheelDao()

    @Provides
    @Singleton
    fun provideSpinHistoryDao(database: RouletteDatabase): SpinHistoryDao =
        database.spinHistoryDao()
}

package com.project.roulette.di

import com.project.roulette.data.repository.SpinHistoryRepositoryImpl
import com.project.roulette.data.repository.StatisticsRepositoryImpl
import com.project.roulette.data.repository.WheelRepositoryImpl
import com.project.roulette.domain.repository.SpinHistoryRepository
import com.project.roulette.domain.repository.StatisticsRepository
import com.project.roulette.domain.repository.WheelRepository
import com.project.roulette.domain.repository.NotificationRepository
import com.project.roulette.data.repository.NotificationRepositoryImpl
import com.project.roulette.data.repository.PreferenceRepositoryImpl
import com.project.roulette.domain.repository.PreferenceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repository bindings.
 * Demonstrates Dependency Inversion: bind implementations to interfaces.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Singleton
    @Binds
    abstract fun bindWheelRepository(
        impl: WheelRepositoryImpl
    ): WheelRepository

    @Singleton
    @Binds
    abstract fun bindSpinHistoryRepository(
        impl: SpinHistoryRepositoryImpl
    ): SpinHistoryRepository

    @Singleton
    @Binds
    abstract fun bindStatisticsRepository(
        impl: StatisticsRepositoryImpl
    ): StatisticsRepository

    @Singleton
    @Binds
    abstract fun bindNotificationRepository(
        impl: NotificationRepositoryImpl
    ): NotificationRepository

    @Singleton
    @Binds
    abstract fun bindPreferenceRepository(
        impl: PreferenceRepositoryImpl
    ): PreferenceRepository
}


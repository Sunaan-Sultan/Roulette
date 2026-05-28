package com.project.roulette.di

import com.project.roulette.domain.repository.SpinHistoryRepository
import com.project.roulette.domain.repository.StatisticsRepository
import com.project.roulette.domain.repository.WheelRepository
import com.project.roulette.domain.usecase.selection.SelectionAlgorithmFactory
import com.project.roulette.domain.usecase.spin.ClearSpinHistoryUseCase
import com.project.roulette.domain.usecase.spin.GetRecentSpinsUseCase
import com.project.roulette.domain.usecase.spin.GetSpinHistoryUseCase
import com.project.roulette.domain.usecase.spin.SpinWheelUseCase
import com.project.roulette.domain.usecase.statistics.ClearStatisticsUseCase
import com.project.roulette.domain.usecase.statistics.GetWheelStatisticsUseCase
import com.project.roulette.domain.usecase.wheel.CreateWheelUseCase
import com.project.roulette.domain.usecase.wheel.DeleteWheelUseCase
import com.project.roulette.domain.usecase.wheel.GetAllWheelsUseCase
import com.project.roulette.domain.usecase.wheel.GetWheelByIdUseCase
import com.project.roulette.domain.usecase.wheel.SearchWheelsUseCase
import com.project.roulette.domain.usecase.wheel.UpdateWheelUseCase
import com.project.roulette.domain.usecase.wheel.ToggleFavoriteUseCase
import com.project.roulette.domain.usecase.wheel.GetGlobalStatsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for use case dependencies.
 * Demonstrates composition over inheritance.
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Singleton
    @Provides
    fun provideSelectionAlgorithmFactory(): SelectionAlgorithmFactory = SelectionAlgorithmFactory

    // Wheel Use Cases
    @Singleton
    @Provides
    fun provideGetAllWheelsUseCase(wheelRepository: WheelRepository): GetAllWheelsUseCase =
        GetAllWheelsUseCase(wheelRepository)

    @Singleton
    @Provides
    fun provideGetWheelByIdUseCase(wheelRepository: WheelRepository): GetWheelByIdUseCase =
        GetWheelByIdUseCase(wheelRepository)

    @Singleton
    @Provides
    fun provideCreateWheelUseCase(
        wheelRepository: WheelRepository,
        notificationRepository: com.project.roulette.domain.repository.NotificationRepository
    ): CreateWheelUseCase =
        CreateWheelUseCase(wheelRepository, notificationRepository)

    @Singleton
    @Provides
    fun provideUpdateWheelUseCase(
        wheelRepository: WheelRepository,
        notificationRepository: com.project.roulette.domain.repository.NotificationRepository
    ): UpdateWheelUseCase =
        UpdateWheelUseCase(wheelRepository, notificationRepository)

    @Singleton
    @Provides
    fun provideDeleteWheelUseCase(wheelRepository: WheelRepository): DeleteWheelUseCase =
        DeleteWheelUseCase(wheelRepository)

    @Singleton
    @Provides
    fun provideSearchWheelsUseCase(wheelRepository: WheelRepository): SearchWheelsUseCase =
        SearchWheelsUseCase(wheelRepository)

    @Singleton
    @Provides
    fun provideToggleFavoriteUseCase(
        wheelRepository: WheelRepository,
        notificationRepository: com.project.roulette.domain.repository.NotificationRepository
    ): ToggleFavoriteUseCase =
        ToggleFavoriteUseCase(wheelRepository, notificationRepository)

    @Singleton
    @Provides
    fun provideGetGlobalStatsUseCase(
        wheelRepository: WheelRepository,
        spinHistoryRepository: SpinHistoryRepository
    ): GetGlobalStatsUseCase =
        GetGlobalStatsUseCase(wheelRepository, spinHistoryRepository)

    // Spin Use Cases
    @Singleton
    @Provides
    fun provideSpinWheelUseCase(
        wheelRepository: WheelRepository,
        spinHistoryRepository: SpinHistoryRepository,
        notificationRepository: com.project.roulette.domain.repository.NotificationRepository,
        selectionAlgorithmFactory: SelectionAlgorithmFactory
    ): SpinWheelUseCase =
        SpinWheelUseCase(wheelRepository, spinHistoryRepository, notificationRepository, selectionAlgorithmFactory)

    @Singleton
    @Provides
    fun provideGetSpinHistoryUseCase(spinHistoryRepository: SpinHistoryRepository): GetSpinHistoryUseCase =
        GetSpinHistoryUseCase(spinHistoryRepository)

    @Singleton
    @Provides
    fun provideGetRecentSpinsUseCase(spinHistoryRepository: SpinHistoryRepository): GetRecentSpinsUseCase =
        GetRecentSpinsUseCase(spinHistoryRepository)

    @Singleton
    @Provides
    fun provideClearSpinHistoryUseCase(spinHistoryRepository: SpinHistoryRepository): ClearSpinHistoryUseCase =
        ClearSpinHistoryUseCase(spinHistoryRepository)

    // Statistics Use Cases
    @Singleton
    @Provides
    fun provideGetWheelStatisticsUseCase(statisticsRepository: StatisticsRepository): GetWheelStatisticsUseCase =
        GetWheelStatisticsUseCase(statisticsRepository)

    @Singleton
    @Provides
    fun provideClearStatisticsUseCase(statisticsRepository: StatisticsRepository): ClearStatisticsUseCase =
        ClearStatisticsUseCase(statisticsRepository)
}


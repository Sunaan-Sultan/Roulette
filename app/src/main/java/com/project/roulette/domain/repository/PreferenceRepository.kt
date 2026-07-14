package com.project.roulette.domain.repository

import com.project.roulette.domain.usecase.selection.SelectionAlgorithmFactory
import com.project.roulette.presentation.viewmodel.WheelViewModel
import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {
    val paletteIndex: Flow<Int>
    val defaultAlgorithm: Flow<SelectionAlgorithmFactory.AlgorithmType>
    val defaultSpinSpeed: Flow<WheelViewModel.SpinSpeed>
    val spinSoundEnabled: Flow<Boolean>
    val confettiEnabled: Flow<Boolean>
    val removeAfterPickEnabled: Flow<Boolean>
    val lastSeenChangelogVersion: Flow<Int>

    suspend fun updatePaletteIndex(index: Int)
    suspend fun updateDefaultAlgorithm(algorithm: SelectionAlgorithmFactory.AlgorithmType)
    suspend fun updateDefaultSpinSpeed(speed: WheelViewModel.SpinSpeed)
    suspend fun updateSpinSoundEnabled(enabled: Boolean)
    suspend fun updateConfettiEnabled(enabled: Boolean)
    suspend fun updateRemoveAfterPickEnabled(enabled: Boolean)
    suspend fun updateLastSeenChangelogVersion(versionCode: Int)
    suspend fun clearAllData()
}

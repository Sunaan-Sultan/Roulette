package com.project.roulette.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.roulette.domain.repository.PreferenceRepository
import com.project.roulette.domain.usecase.selection.SelectionAlgorithmFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    val paletteIndex: StateFlow<Int> = preferenceRepository.paletteIndex
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val defaultAlgorithm: StateFlow<SelectionAlgorithmFactory.AlgorithmType> = preferenceRepository.defaultAlgorithm
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SelectionAlgorithmFactory.AlgorithmType.UNIFORM)

    val defaultSpinSpeed: StateFlow<WheelViewModel.SpinSpeed> = preferenceRepository.defaultSpinSpeed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WheelViewModel.SpinSpeed.MEDIUM)

    val spinSoundEnabled: StateFlow<Boolean> = preferenceRepository.spinSoundEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val confettiEnabled: StateFlow<Boolean> = preferenceRepository.confettiEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val removeAfterPickEnabled: StateFlow<Boolean> = preferenceRepository.removeAfterPickEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun updatePaletteIndex(index: Int) {
        viewModelScope.launch { preferenceRepository.updatePaletteIndex(index) }
    }

    fun updateDefaultAlgorithm(algorithm: SelectionAlgorithmFactory.AlgorithmType) {
        viewModelScope.launch { preferenceRepository.updateDefaultAlgorithm(algorithm) }
    }

    fun updateDefaultSpinSpeed(speed: WheelViewModel.SpinSpeed) {
        viewModelScope.launch { preferenceRepository.updateDefaultSpinSpeed(speed) }
    }

    fun updateSpinSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { preferenceRepository.updateSpinSoundEnabled(enabled) }
    }

    fun updateConfettiEnabled(enabled: Boolean) {
        viewModelScope.launch { preferenceRepository.updateConfettiEnabled(enabled) }
    }

    fun updateRemoveAfterPickEnabled(enabled: Boolean) {
        viewModelScope.launch { preferenceRepository.updateRemoveAfterPickEnabled(enabled) }
    }

    fun clearAllData() {
        viewModelScope.launch { preferenceRepository.clearAllData() }
    }
}

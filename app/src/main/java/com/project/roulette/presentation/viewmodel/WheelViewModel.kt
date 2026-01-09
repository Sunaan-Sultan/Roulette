package com.project.roulette.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.roulette.domain.model.Result
import com.project.roulette.domain.usecase.selection.SelectionAlgorithmFactory
import com.project.roulette.domain.usecase.spin.GetRecentSpinsUseCase
import com.project.roulette.domain.usecase.spin.SpinWheelUseCase
import com.project.roulette.domain.usecase.statistics.GetWheelStatisticsUseCase
import com.project.roulette.domain.usecase.wheel.GetWheelByIdUseCase
import com.project.roulette.presentation.model.WheelUiState
import com.project.roulette.util.audio.HapticFeedback
import com.project.roulette.util.audio.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Wheel/Spin screen.
 * Handles wheel spinning logic and animations.
 */
@HiltViewModel
class WheelViewModel @Inject constructor(
    private val getWheelByIdUseCase: GetWheelByIdUseCase,
    private val spinWheelUseCase: SpinWheelUseCase,
    private val getRecentSpinsUseCase: GetRecentSpinsUseCase,
    private val getWheelStatisticsUseCase: GetWheelStatisticsUseCase,
    private val soundManager: SoundManager,
    private val hapticFeedback: HapticFeedback
) : ViewModel() {

    private val _uiState = MutableStateFlow<WheelUiState>(WheelUiState.Loading)
    val uiState: StateFlow<WheelUiState> = _uiState.asStateFlow()

    private val _selectedAlgorithm = MutableStateFlow(SelectionAlgorithmFactory.AlgorithmType.UNIFORM)
    val selectedAlgorithm: StateFlow<SelectionAlgorithmFactory.AlgorithmType> = _selectedAlgorithm.asStateFlow()

    private val _spinDuration = MutableStateFlow(10000L)
    val spinDuration: StateFlow<Long> = _spinDuration.asStateFlow()

    // Holds the outcome produced by the selection algorithm and recorded by the use case
    private val _pendingSpinOutcome = MutableStateFlow<SpinWheelUseCase.SpinOutcome?>(null)
    val pendingSpinOutcome: StateFlow<SpinWheelUseCase.SpinOutcome?> = _pendingSpinOutcome.asStateFlow()

    /**
     * Load wheel by ID.
     */
    fun loadWheel(wheelId: String) {
        viewModelScope.launch {
            _uiState.value = WheelUiState.Loading
            getWheelByIdUseCase(wheelId).collect { result ->
                _uiState.value = when (result) {
                    is Result.Success -> WheelUiState.Success(wheel = result.data)
                    is Result.Error -> WheelUiState.Error(result.exception.message ?: "Failed to load wheel")
                    is Result.Loading -> WheelUiState.Loading
                }
            }
        }
    }

    /**
     * Spin the wheel — execute selection and record result, but defer UI 'spinning finished' until
     * the animation completes and the UI calls `onAnimationComplete()`.
     */
    fun spinWheel() {
        val currentState = _uiState.value
        if (currentState !is WheelUiState.Success || currentState.isSpinning) {
            return
        }

        viewModelScope.launch {
            try {
                // Update UI to show spinning state
                _uiState.value = currentState.copy(isSpinning = true)

                // Play sound and haptic feedback
                soundManager.playSpinStart()
                hapticFeedback.spinVibration()

                // Execute spin selection (suspend use case)
                val result = spinWheelUseCase(
                    wheelId = currentState.wheel.id,
                    algorithmType = _selectedAlgorithm.value,
                    spinDuration = _spinDuration.value
                )

                // On success, store pending spin outcome for UI animation
                when (result) {
                    is Result.Success -> {
                        _pendingSpinOutcome.value = result.data
                        // Do not set isSpinning = false here; UI will animate and notify when done
                    }

                    is Result.Error -> {
                        hapticFeedback.cancel()
                        _uiState.value = WheelUiState.Error(result.exception.message ?: "Spin failed")
                    }

                    else -> {}
                }
            } catch (e: Exception) {
                hapticFeedback.cancel()
                _uiState.value = WheelUiState.Error(e.message ?: "Unknown error during spin")
            }
        }
    }

    /**
     * Called by UI when spin animation finishes. This finalizes state and clears the pending outcome.
     */
    fun onAnimationComplete() {
        val outcome = _pendingSpinOutcome.value ?: return
        val currentState = _uiState.value
        if (currentState !is WheelUiState.Success) return

        // Trigger completion feedback
        soundManager.playSpinEnd()
        hapticFeedback.successPattern()

        // Update UI with spin result and stop spinning
        _uiState.value = currentState.copy(
            isSpinning = false,
            lastSpinResult = outcome.spinResult,
            spinProgress = 0f
        )

        // Clear pending outcome
        _pendingSpinOutcome.value = null
    }

    /**
     * Update spin animation progress (called from UI during animation).
     */
    fun updateSpinProgress(progress: Float) {
        val currentState = _uiState.value
        if (currentState is WheelUiState.Success) {
            _uiState.value = currentState.copy(spinProgress = progress)
        }
    }

    /**
     * Set selection algorithm.
     */
    fun setSelectionAlgorithm(algorithmType: SelectionAlgorithmFactory.AlgorithmType) {
        _selectedAlgorithm.value = algorithmType
    }

    /**
     * Set spin duration.
     */
    fun setSpinDuration(durationMs: Long) {
        _spinDuration.value = durationMs.coerceIn(2000L, 15000L) // 2-15 seconds
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
        hapticFeedback.cancel()
    }
}

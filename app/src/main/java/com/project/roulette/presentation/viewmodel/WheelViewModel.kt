package com.project.roulette.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.roulette.domain.model.Result
import com.project.roulette.domain.usecase.selection.SelectionAlgorithmFactory
import com.project.roulette.domain.usecase.spin.GetRecentSpinsUseCase
import com.project.roulette.domain.usecase.spin.SpinWheelUseCase
import com.project.roulette.domain.usecase.statistics.GetWheelStatisticsUseCase
import com.project.roulette.domain.usecase.wheel.GetWheelByIdUseCase
import com.project.roulette.domain.usecase.wheel.UpdateWheelUseCase
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
    private val updateWheelUseCase: UpdateWheelUseCase,
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

    enum class SpinSpeed(val label: String, val durationMs: Long, val rotations: Int) {
        SLOW("Slow", 8000L, 3),
        LEISURELY("Leisurely", 6000L, 5),
        MEDIUM("Medium", 4000L, 7),
        FAST("Fast", 3000L, 10),
        BLAZING("Blazing", 2000L, 15)
    }

    private val _spinSpeed = MutableStateFlow(SpinSpeed.MEDIUM)
    val spinSpeed: StateFlow<SpinSpeed> = _spinSpeed.asStateFlow()

    private val _seed = MutableStateFlow<Long?>(null)
    val seed: StateFlow<Long?> = _seed.asStateFlow()

    private val _spinsToday = MutableStateFlow(0)
    val spinsToday: StateFlow<Int> = _spinsToday.asStateFlow()

    private val _rrQueue = MutableStateFlow<List<String>>(emptyList())
    val rrQueue: StateFlow<List<String>> = _rrQueue.asStateFlow()

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
            
            // Load spins today for this wheel
            getWheelStatisticsUseCase(wheelId).collect { result ->
                if (result is Result.Success) {
                    _spinsToday.value = result.data.totalSpins
                    
                    val currentState = _uiState.value
                    if (currentState is WheelUiState.Success && _selectedAlgorithm.value == SelectionAlgorithmFactory.AlgorithmType.ROUND_ROBIN) {
                        val total = currentState.wheel.getActiveSegments().size
                        if (total > 0) {
                            val remaining = total - (result.data.totalSpins % total)
                            _uiState.value = currentState.copy(rrRemaining = remaining)
                        }
                    }
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
                _uiState.value = currentState.copy(isSpinning = true, lastSpinResult = null)

                // Play sound and haptic feedback
                soundManager.playSpinStart()
                hapticFeedback.spinVibration()

                // Execute spin selection (suspend use case)
                val result = spinWheelUseCase(
                    wheelId = currentState.wheel.id,
                    algorithmType = _selectedAlgorithm.value,
                    spinDuration = _spinSpeed.value.durationMs,
                    seed = _seed.value
                )

                // On success, store pending spin outcome for UI animation
                when (result) {
                    is Result.Success -> {
                        val outcome = result.data
                        val info = when (_selectedAlgorithm.value) {
                            SelectionAlgorithmFactory.AlgorithmType.WEIGHTED -> {
                                val total = currentState.wheel.getTotalWeight()
                                val weight = outcome.selectedSegment.weight
                                val percent = (weight / total * 100).toInt()
                                "$percent% chance"
                            }
                            SelectionAlgorithmFactory.AlgorithmType.SEEDED -> {
                                "seed #${_seed.value ?: "random"}"
                            }
                            SelectionAlgorithmFactory.AlgorithmType.ROUND_ROBIN -> {
                                // For RR, we might want to track it better, but for now:
                                val total = currentState.wheel.getActiveSegments().size
                                "1/$total done" // Placeholder
                            }
                            else -> null
                        }
                        
                        _uiState.value = currentState.copy(
                            isSpinning = true,
                            lastSpinResult = null,
                            algorithmInfo = info
                        )
                        _pendingSpinOutcome.value = outcome
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
        val totalSegments = currentState.wheel.getActiveSegments().size
        var doneCount = 0
        val newRrRemaining = if (_selectedAlgorithm.value == SelectionAlgorithmFactory.AlgorithmType.ROUND_ROBIN) {
            val current = currentState.rrRemaining ?: totalSegments
            doneCount = totalSegments - current + 1
            if (current <= 1) totalSegments else current - 1
        } else null

        val rrInfo = if (_selectedAlgorithm.value == SelectionAlgorithmFactory.AlgorithmType.ROUND_ROBIN) {
            "$doneCount/$totalSegments done"
        } else currentState.algorithmInfo

        _uiState.value = currentState.copy(
            isSpinning = false,
            lastSpinResult = outcome.spinResult,
            spinProgress = 0f,
            rrRemaining = newRrRemaining,
            algorithmInfo = rrInfo
        )
        
        _spinsToday.value += 1

        // Clear pending outcome
        _pendingSpinOutcome.value = null
    }

    /**
     * Clear the last spin result (e.g. when result dialog is dismissed)
     */
    fun clearResult() {
        val currentState = _uiState.value
        if (currentState is WheelUiState.Success) {
            _uiState.value = currentState.copy(lastSpinResult = null)
        }
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
        val currentState = _uiState.value
        if (currentState is WheelUiState.Success) {
            _uiState.value = currentState.copy(
                algorithmInfo = null,
                rrRemaining = if (algorithmType == SelectionAlgorithmFactory.AlgorithmType.ROUND_ROBIN) {
                    currentState.wheel.getActiveSegments().size
                } else null
            )
        }
    }

    /**
     * Set spin speed.
     */
    fun setSpinSpeed(speed: SpinSpeed) {
        _spinSpeed.value = speed
    }

    /**
     * Set seed for Seeded algorithm.
     */
    fun setSeed(seed: Long?) {
        _seed.value = seed
    }

    /**
     * Update segment weight for weighted algorithm.
     */
    fun updateSegmentWeight(segmentId: String, weight: Float) {
        val currentState = _uiState.value
        if (currentState !is WheelUiState.Success) return

        val updatedSegments = currentState.wheel.segments.map {
            if (it.id == segmentId) it.copy(weight = weight) else it
        }
        val updatedWheel = currentState.wheel.copy(segments = updatedSegments)
        
        _uiState.value = currentState.copy(wheel = updatedWheel)
        
        viewModelScope.launch {
            updateWheelUseCase(updatedWheel)
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
        hapticFeedback.cancel()
    }
}

package com.project.roulette.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.roulette.domain.model.Segment
import com.project.roulette.domain.model.SpinResult
import com.project.roulette.domain.model.Wheel
import com.project.roulette.domain.usecase.selection.SelectionAlgorithmFactory
import com.project.roulette.presentation.model.WheelUiState
import com.project.roulette.util.audio.HapticFeedback
import com.project.roulette.util.audio.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

/**
 * ViewModel for Wheel Preview screen.
 * Handles temporary spin logic without persisting data.
 */
@HiltViewModel
class PreviewViewModel @Inject constructor(
    private val soundManager: SoundManager,
    private val hapticFeedback: HapticFeedback
) : ViewModel() {

    private val _uiState = MutableStateFlow<WheelUiState>(WheelUiState.Loading)
    val uiState: StateFlow<WheelUiState> = _uiState.asStateFlow()

    private val _testSpinsCount = MutableStateFlow(0)
    val testSpinsCount: StateFlow<Int> = _testSpinsCount.asStateFlow()

    private val _lastPick = MutableStateFlow<String?>(null)
    val lastPick: StateFlow<String?> = _lastPick.asStateFlow()

    private val _spinHistory = MutableStateFlow<List<SpinResult>>(emptyList())
    val spinHistory: StateFlow<List<SpinResult>> = _spinHistory.asStateFlow()

    private val _pendingSpinOutcome = MutableStateFlow<PreviewSpinOutcome?>(null)
    val pendingSpinOutcome: StateFlow<PreviewSpinOutcome?> = _pendingSpinOutcome.asStateFlow()

    data class PreviewSpinOutcome(
        val selectedSegment: Segment,
        val spinResult: SpinResult
    )

    fun initialize(wheel: Wheel) {
        _uiState.value = WheelUiState.Success(wheel = wheel)
    }

    fun spinWheel() {
        val currentState = _uiState.value
        if (currentState !is WheelUiState.Success || currentState.isSpinning) return

        viewModelScope.launch {
            _uiState.value = currentState.copy(isSpinning = true, lastSpinResult = null)

            if (currentState.wheel.spinSound) {
                soundManager.playSpinStart()
            }
            hapticFeedback.spinVibration()

            // Simulate selection using Weighted Random by default for Preview
            val algorithm = SelectionAlgorithmFactory.createAlgorithm(SelectionAlgorithmFactory.AlgorithmType.WEIGHTED)
            val selectedSegment = algorithm.selectSegment(currentState.wheel.getActiveSegments())

            if (selectedSegment != null) {
                val duration = 3000L
                val spinResult = SpinResult(
                    id = UUID.randomUUID().toString(),
                    wheelId = currentState.wheel.id,
                    selectedSegmentId = selectedSegment.id,
                    selectedSegmentName = selectedSegment.name,
                    spinTimestamp = Clock.System.now(),
                    spinDuration = duration,
                    finalAngle = Random.nextFloat() * 360f
                )

                _pendingSpinOutcome.value = PreviewSpinOutcome(selectedSegment, spinResult)
            } else {
                _uiState.value = currentState.copy(isSpinning = false)
            }
        }
    }

    fun onAnimationComplete() {
        val outcome = _pendingSpinOutcome.value ?: return
        val currentState = _uiState.value
        if (currentState !is WheelUiState.Success) return

        if (currentState.wheel.spinSound) {
            soundManager.playSpinEnd()
        }
        hapticFeedback.successPattern()

        _uiState.value = currentState.copy(
            isSpinning = false,
            lastSpinResult = outcome.spinResult
        )

        _testSpinsCount.value += 1
        _lastPick.value = outcome.selectedSegment.name
        _spinHistory.value = (listOf(outcome.spinResult) + _spinHistory.value).take(4)

        _pendingSpinOutcome.value = null
    }

    fun clearResult() {
        val currentState = _uiState.value
        if (currentState is WheelUiState.Success) {
            soundManager.stopAll()
            _uiState.value = currentState.copy(lastSpinResult = null)
        }
    }

    fun playTickSound() {
        soundManager.playTick()
    }

    override fun onCleared() {
        super.onCleared()
        hapticFeedback.cancel()
    }
}

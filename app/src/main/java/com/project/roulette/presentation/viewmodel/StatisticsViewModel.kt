package com.project.roulette.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.roulette.domain.model.Result
import com.project.roulette.domain.usecase.statistics.ClearStatisticsUseCase
import com.project.roulette.domain.usecase.statistics.GetWheelStatisticsUseCase
import com.project.roulette.domain.usecase.wheel.GetWheelByIdUseCase
import com.project.roulette.presentation.model.StatisticsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Statistics screen.
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getWheelStatisticsUseCase: GetWheelStatisticsUseCase,
    private val clearStatisticsUseCase: ClearStatisticsUseCase,
    private val getWheelByIdUseCase: GetWheelByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatisticsUiState>(StatisticsUiState.Loading)
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    /**
     * Load statistics for a wheel.
     */
    fun loadStatistics(wheelId: String) {
        viewModelScope.launch {
            _uiState.value = StatisticsUiState.Loading
            try {
                // Get wheel name
                var wheelName = "Wheel"
                getWheelByIdUseCase(wheelId).collect { result ->
                    if (result is Result.Success) {
                        wheelName = result.data.name
                    }
                }

                // Get statistics
                getWheelStatisticsUseCase(wheelId).collect { result ->
                    _uiState.value = when (result) {
                        is Result.Success -> StatisticsUiState.Success(
                            statistics = result.data,
                            wheelName = wheelName
                        )

                        is Result.Error -> StatisticsUiState.Error(result.exception.message ?: "Failed to load statistics")
                        is Result.Loading -> StatisticsUiState.Loading
                    }
                }
            } catch (e: Exception) {
                _uiState.value = StatisticsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Clear statistics for a wheel.
     */
    fun clearStatistics(wheelId: String) {
        viewModelScope.launch {
            val result = clearStatisticsUseCase(wheelId)
            when (result) {
                is Result.Success -> loadStatistics(wheelId)
                is Result.Error -> {
                    _uiState.value = StatisticsUiState.Error(result.exception.message ?: "Failed to clear statistics")
                }

                else -> {}
            }
        }
    }
}


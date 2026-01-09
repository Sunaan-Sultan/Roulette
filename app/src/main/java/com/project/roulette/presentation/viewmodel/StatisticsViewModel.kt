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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
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

            // Combine wheel and statistics flows so we can react to updates and avoid blocking
            getWheelByIdUseCase(wheelId)
                .combine(getWheelStatisticsUseCase(wheelId)) { wheelResult, statsResult ->
                    Pair(wheelResult, statsResult)
                }
                .catch { e ->
                    _uiState.value = StatisticsUiState.Error(e.message ?: "Failed to load statistics")
                }
                .collect { (wheelResult, statsResult) ->
                    when {
                        wheelResult is Result.Success && statsResult is Result.Success -> {
                            _uiState.value = StatisticsUiState.Success(
                                statistics = statsResult.data,
                                wheelName = wheelResult.data.name
                            )
                        }

                        statsResult is Result.Error -> {
                            _uiState.value = StatisticsUiState.Error(
                                statsResult.exception.message ?: "Failed to load statistics"
                            )
                        }

                        wheelResult is Result.Error -> {
                            _uiState.value = StatisticsUiState.Error(
                                wheelResult.exception.message ?: "Failed to load wheel"
                            )
                        }

                        else -> {
                            _uiState.value = StatisticsUiState.Error("Unknown error while loading statistics")
                        }
                    }
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

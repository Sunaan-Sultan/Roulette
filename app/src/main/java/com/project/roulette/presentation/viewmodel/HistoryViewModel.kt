package com.project.roulette.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.roulette.domain.model.Result
import com.project.roulette.domain.usecase.spin.ClearSpinHistoryUseCase
import com.project.roulette.domain.usecase.spin.GetRecentSpinsUseCase
import com.project.roulette.domain.usecase.wheel.GetWheelByIdUseCase
import com.project.roulette.presentation.model.HistoryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Spin History screen.
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getWheelByIdUseCase: GetWheelByIdUseCase,
    private val getRecentSpinsUseCase: GetRecentSpinsUseCase,
    private val clearSpinHistoryUseCase: ClearSpinHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    /**
     * Load spin history and wheel name for the given wheel.
     */
    fun loadHistory(wheelId: String, limit: Int = 50) {
        viewModelScope.launch {
            _uiState.value = HistoryUiState.Loading

            // Combine wheel and spins flows so we can react to updates and avoid `.first()` cancellation races
            getWheelByIdUseCase(wheelId)
                .combine(getRecentSpinsUseCase(wheelId, limit)) { wheelResult, spinsResult ->
                    Pair(wheelResult, spinsResult)
                }
                .catch { e ->
                    // Emit an error UI state if combining fails
                    _uiState.value = HistoryUiState.Error(e.message ?: "Failed to load history")
                }
                .collect { (wheelResult, spinsResult) ->
                    when {
                        wheelResult is Result.Success && spinsResult is Result.Success -> {
                            _uiState.value = HistoryUiState.Success(
                                wheelName = wheelResult.data.name,
                                spinResults = spinsResult.data
                            )
                        }

                        spinsResult is Result.Error -> {
                            _uiState.value = HistoryUiState.Error(
                                spinsResult.exception.message ?: "Failed to load history"
                            )
                        }

                        wheelResult is Result.Error -> {
                            _uiState.value = HistoryUiState.Error(
                                wheelResult.exception.message ?: "Failed to load wheel"
                            )
                        }

                        else -> {
                            _uiState.value = HistoryUiState.Error("Unknown error while loading history")
                        }
                    }
                }
        }
    }

    /**
     * Clear history for a wheel and reload.
     */
    fun clearHistory(wheelId: String) {
        viewModelScope.launch {
            try {
                val result = clearSpinHistoryUseCase(wheelId)
                when (result) {
                    is Result.Success -> loadHistory(wheelId)
                    is Result.Error -> _uiState.value = HistoryUiState.Error(
                        result.exception.message ?: "Failed to clear history"
                    )

                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.value = HistoryUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

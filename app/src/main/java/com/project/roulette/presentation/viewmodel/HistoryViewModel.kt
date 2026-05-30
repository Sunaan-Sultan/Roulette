package com.project.roulette.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.roulette.domain.model.Result
import com.project.roulette.domain.model.SpinResult
import com.project.roulette.domain.model.Wheel
import com.project.roulette.domain.usecase.spin.ClearSpinHistoryUseCase
import com.project.roulette.domain.usecase.spin.GetRecentSpinsUseCase
import com.project.roulette.domain.usecase.wheel.GetWheelByIdUseCase
import com.project.roulette.presentation.model.DistributionItem
import com.project.roulette.presentation.model.HistoryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

sealed class HistoryEffect {
    data class ShareCsv(val csvData: String, val wheelName: String) : HistoryEffect()
    data class SharePdf(
        val wheelName: String, 
        val results: List<SpinResult>,
        val stats: PdfStats
    ) : HistoryEffect()
}

data class PdfStats(
    val totalSpins: Int,
    val avgDuration: Float,
    val mostPicked: String?,
    val distribution: List<DistributionItem>
)

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

    private val _effect = Channel<HistoryEffect>()
    val effect = _effect.receiveAsFlow()

    private val _selectedFilter = MutableStateFlow<String?>(null)
    private val _isDescending = MutableStateFlow(true)

    /**
     * Load spin history and wheel name for the given wheel.
     */
    fun loadHistory(wheelId: String, limit: Int = 100) {
        viewModelScope.launch {
            _uiState.value = HistoryUiState.Loading

            combine(
                getWheelByIdUseCase(wheelId),
                getRecentSpinsUseCase(wheelId, limit),
                _selectedFilter,
                _isDescending
            ) { wheelResult, spinsResult, filter, isDesc ->
                Triple(wheelResult, spinsResult, Pair(filter, isDesc))
            }
                .catch { e ->
                    _uiState.value = HistoryUiState.Error(e.message ?: "Failed to load history")
                }
                .collect { (wheelResult, spinsResult, sortFilter) ->
                    val (filter, isDesc) = sortFilter

                    if (wheelResult is Result.Success && spinsResult is Result.Success) {
                        val wheel = wheelResult.data
                        val spins = spinsResult.data

                        val filtered = if (filter == null) {
                            spins
                        } else {
                            spins.filter { it.selectedSegmentName == filter }
                        }

                        val sorted = if (isDesc) {
                            filtered.sortedByDescending { it.spinTimestamp }
                        } else {
                            filtered.sortedBy { it.spinTimestamp }
                        }

                        // Calculate Stats
                        val totalSpins = spins.size
                        val avgDuration = if (totalSpins > 0) spins.map { it.spinDuration }.average().toFloat() else 0f
                        
                        val counts = spins.groupingBy { it.selectedSegmentName }.eachCount()
                        val mostPicked = counts.maxByOrNull { it.value }?.key

                        val distribution = wheel.segments.map { segment ->
                            val count = counts[segment.name] ?: 0
                            val percent = if (totalSpins > 0) (count * 100 / totalSpins) else 0
                            DistributionItem(segment.name, count, percent, segment.color)
                        }.sortedByDescending { it.count }

                        _uiState.value = HistoryUiState.Success(
                            wheel = wheel,
                            spinResults = spins,
                            filteredResults = sorted,
                            totalSpins = totalSpins,
                            avgDuration = avgDuration / 1000f, // in seconds
                            mostPickedName = mostPicked,
                            winDistribution = distribution,
                            selectedFilter = filter,
                            isDescending = isDesc
                        )
                    } else if (spinsResult is Result.Error || wheelResult is Result.Error) {
                        val msg = (spinsResult as? Result.Error)?.exception?.message 
                            ?: (wheelResult as? Result.Error)?.exception?.message 
                            ?: "Failed to load"
                        _uiState.value = HistoryUiState.Error(msg)
                    }
                }
        }
    }

    fun setFilter(name: String?) {
        _selectedFilter.value = name
    }

    fun toggleSort() {
        _isDescending.value = !_isDescending.value
    }

    fun exportHistory() {
        val state = _uiState.value as? HistoryUiState.Success ?: return
        
        viewModelScope.launch {
            val stats = PdfStats(
                totalSpins = state.totalSpins,
                avgDuration = state.avgDuration,
                mostPicked = state.mostPickedName,
                distribution = state.winDistribution
            )
            _effect.send(HistoryEffect.SharePdf(state.wheel.name, state.spinResults, stats))
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
                    is Result.Success -> {
                        // Reload or the combined flow will handle it if the history use case is reactive
                    }
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

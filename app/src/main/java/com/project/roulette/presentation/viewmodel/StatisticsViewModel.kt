package com.project.roulette.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.roulette.domain.model.Result
import com.project.roulette.domain.model.SpinResult
import com.project.roulette.domain.model.Wheel
import com.project.roulette.domain.model.WheelStatistics
import com.project.roulette.domain.usecase.spin.GetSpinHistoryUseCase
import com.project.roulette.domain.usecase.statistics.ClearStatisticsUseCase
import com.project.roulette.domain.usecase.statistics.GetWheelStatisticsUseCase
import com.project.roulette.domain.usecase.wheel.GetWheelByIdUseCase
import com.project.roulette.presentation.model.ChartType
import com.project.roulette.presentation.model.StatisticsUiState
import com.project.roulette.presentation.model.StreakInfo
import com.project.roulette.presentation.model.TimelineItem
import com.project.roulette.presentation.model.DistributionItem
import com.project.roulette.presentation.model.PdfStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

sealed class StatisticsEffect {
    data class SharePdf(
        val wheelName: String, 
        val results: List<SpinResult>,
        val stats: PdfStats
    ) : StatisticsEffect()
}

/**
 * ViewModel for Statistics screen.
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getWheelStatisticsUseCase: GetWheelStatisticsUseCase,
    private val clearStatisticsUseCase: ClearStatisticsUseCase,
    private val getWheelByIdUseCase: GetWheelByIdUseCase,
    private val getSpinHistoryUseCase: GetSpinHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatisticsUiState>(StatisticsUiState.Loading)
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private val _effect = Channel<StatisticsEffect>()
    val effect = _effect.receiveAsFlow()

    private val _chartType = MutableStateFlow(ChartType.BAR)

    /**
     * Load statistics for a wheel.
     */
    fun loadStatistics(wheelId: String) {
        viewModelScope.launch {
            _uiState.value = StatisticsUiState.Loading

            combine(
                getWheelByIdUseCase(wheelId),
                getWheelStatisticsUseCase(wheelId),
                getSpinHistoryUseCase(wheelId),
                _chartType
            ) { wheelResult, statsResult, historyResult, chartType ->
                DataPack(wheelResult, statsResult, historyResult, chartType)
            }
                .catch { e ->
                    _uiState.value = StatisticsUiState.Error(e.message ?: "Failed to load statistics")
                }
                .collect { data ->
                    val wheelR = data.wheelR
                    val statsR = data.statsR
                    val historyR = data.historyR
                    val chartType = data.chartType

                    if (wheelR is Result.Success && statsR is Result.Success && historyR is Result.Success) {
                        val wheel = wheelR.data
                        val stats = statsR.data
                        val history = historyR.data.sortedBy { it.spinTimestamp }

                        // 1. Fairness Score & Message
                        val (fairnessScore, fairnessMessage) = calculateFairness(stats)

                        // 2. Lucky Streaks
                        val streaks = calculateStreaks(wheel, history)

                        // 3. Spin Timeline
                        val timeline = history.takeLast(20).mapIndexed { index, spin ->
                            val segment = wheel.segments.find { it.name == spin.selectedSegmentName }
                            TimelineItem(segment?.color ?: androidx.compose.ui.graphics.Color.Gray, index)
                        }

                        _uiState.value = StatisticsUiState.Success(
                            wheel = wheel,
                            statistics = stats,
                            spinResults = history,
                            fairnessScore = fairnessScore,
                            fairnessMessage = fairnessMessage,
                            streaks = streaks,
                            timelineData = timeline,
                            chartType = chartType
                        )
                    } else if (statsR is Result.Error || wheelR is Result.Error || historyR is Result.Error) {
                        val msg = (statsR as? Result.Error)?.exception?.message 
                            ?: (wheelR as? Result.Error)?.exception?.message 
                            ?: (historyR as? Result.Error)?.exception?.message
                            ?: "Failed to load data"
                        _uiState.value = StatisticsUiState.Error(msg)
                    }
                }
        }
    }

    private data class DataPack(
        val wheelR: Result<Wheel>,
        val statsR: Result<WheelStatistics>,
        val historyR: Result<List<SpinResult>>,
        val chartType: ChartType
    )

    private fun calculateFairness(stats: WheelStatistics): Pair<Int, String> {
        if (stats.totalSpins < 5) return 100 to "Keep spinning to get a fairness score!"
        
        val counts = stats.selectionCounts
        val mostPicked = counts.maxByOrNull { it.value }
        val leastPicked = counts.filter { it.value > 0 }.minByOrNull { it.value } ?: counts.minByOrNull { it.value }
        
        if (mostPicked == null || leastPicked == null || leastPicked.value == 0) {
             return 30 to "${mostPicked?.key} is dominating. Try adjusting weights or resetting."
        }

        val ratio = mostPicked.value.toFloat() / leastPicked.value.toFloat()
        val score = (100 / ratio).toInt().coerceIn(10, 100)
        
        val message = if (ratio > 2.5) {
            "${mostPicked.key} was picked ${String.format(Locale.US, "%.1fx", ratio)} more than ${leastPicked.key}. Consider adjusting weights for a fairer distribution."
        } else {
            "The wheel is looking quite fair! Good distribution."
        }

        return score to message
    }

    private fun calculateStreaks(wheel: Wheel, history: List<SpinResult>): List<StreakInfo> {
        if (history.isEmpty()) return emptyList()
        
        val streaks = mutableListOf<StreakInfo>()
        val latestName = history.last().selectedSegmentName
        var currentCount = 0
        
        for (i in history.indices.reversed()) {
            if (history[i].selectedSegmentName == latestName) {
                currentCount++
            } else {
                break
            }
        }
        
        val segment = wheel.segments.find { it.name == latestName }
        streaks.add(StreakInfo(latestName, currentCount, segment?.color ?: androidx.compose.ui.graphics.Color.Gray))
        
        return streaks
    }

    fun toggleChartType() {
        val current = _chartType.value
        _chartType.value = if (current == ChartType.BAR) ChartType.RING else ChartType.BAR
    }

    fun exportHistory() {
        val state = _uiState.value as? StatisticsUiState.Success ?: return
        
        viewModelScope.launch {
            val counts = state.statistics.selectionCounts
            val distribution = state.wheel.segments.map { segment ->
                val count = counts[segment.name] ?: 0
                val percent = if (state.statistics.totalSpins > 0) (count * 100 / state.statistics.totalSpins) else 0
                DistributionItem(segment.name, count, percent, segment.color)
            }

            val avgDuration = if (state.spinResults.isNotEmpty()) {
                state.spinResults.map { it.spinDuration }.average().toFloat()
            } else 0f

            val stats = PdfStats(
                totalSpins = state.statistics.totalSpins,
                avgDuration = avgDuration / 1000f,
                mostPicked = state.statistics.mostFrequent,
                distribution = distribution
            )
            _effect.send(StatisticsEffect.SharePdf(state.wheel.name, state.spinResults, stats))
        }
    }

    fun clearStatistics(wheelId: String) {
        viewModelScope.launch {
            val result = clearStatisticsUseCase(wheelId)
            if (result is Result.Error) {
                _uiState.value = StatisticsUiState.Error(result.exception.message ?: "Failed to clear")
            }
        }
    }
}

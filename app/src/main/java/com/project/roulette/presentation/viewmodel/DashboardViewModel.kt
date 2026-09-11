package com.project.roulette.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.roulette.domain.model.Result
import com.project.roulette.domain.model.SpinResult
import com.project.roulette.domain.model.Wheel
import com.project.roulette.domain.repository.NotificationRepository
import com.project.roulette.domain.usecase.dashboard.GetGlobalRecentSpinsUseCase
import com.project.roulette.domain.usecase.dashboard.GetSpinsInWindowUseCase
import com.project.roulette.domain.usecase.wheel.GetAllWheelsUseCase
import com.project.roulette.domain.usecase.wheel.GetGlobalStatsUseCase
import com.project.roulette.domain.usecase.wheel.GlobalStats
import com.project.roulette.presentation.component.WheelAccentPalette
import com.project.roulette.presentation.component.wheelAccentFor
import com.project.roulette.presentation.model.ChartType
import com.project.roulette.presentation.model.DashboardUiState
import com.project.roulette.presentation.model.DayActivity
import com.project.roulette.presentation.model.DistributionItem
import com.project.roulette.presentation.model.QuickWheel
import com.project.roulette.presentation.model.RecentSpinItem
import com.project.roulette.presentation.model.ShareSlice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

private const val RECENT_FEED_LIMIT = 24
private const val ACTIVITY_WINDOW_DAYS = 120
private const val WEEK_DAYS = 7
private const val SHARE_SLICE_LIMIT = 5
private const val TOP_PICK_LIMIT = 5
private const val QUICK_WHEEL_LIMIT = 8
private const val SEGMENT_PREVIEW_LIMIT = 10

private val DayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

private val OtherSliceColor = androidx.compose.ui.graphics.Color(0xFF8E8E93)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getAllWheelsUseCase: GetAllWheelsUseCase,
    getGlobalStatsUseCase: GetGlobalStatsUseCase,
    getGlobalRecentSpinsUseCase: GetGlobalRecentSpinsUseCase,
    getSpinsInWindowUseCase: GetSpinsInWindowUseCase,
    notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _chartType = MutableStateFlow(ChartType.RING)
    val chartType: StateFlow<ChartType> = _chartType.asStateFlow()

    init {
        val zone = TimeZone.currentSystemDefault()
        val windowStart = Clock.System.now()
            .toEpochMilliseconds() - ACTIVITY_WINDOW_DAYS * 24L * 60L * 60L * 1000L

        val core = combine(
            getAllWheelsUseCase(),
            getGlobalStatsUseCase(),
            getGlobalStatsUseCase.getSpinCounts(),
            notificationRepository.getUnreadCount()
        ) { wheelsResult, stats, spinCounts, unreadCount ->
            CorePack(wheelsResult, stats, spinCounts, unreadCount)
        }

        viewModelScope.launch {
            combine(
                core,
                getGlobalRecentSpinsUseCase(RECENT_FEED_LIMIT),
                getSpinsInWindowUseCase(windowStart)
            ) { pack, recentSpins, windowSpins ->
                when (val wheelsResult = pack.wheelsResult) {
                    is Result.Success -> build(
                        wheels = wheelsResult.data,
                        stats = pack.stats,
                        spinCounts = pack.spinCounts,
                        unreadCount = pack.unreadCount,
                        recentSpins = recentSpins,
                        windowSpins = windowSpins,
                        zone = zone
                    )

                    is Result.Error -> DashboardUiState.Error(
                        wheelsResult.exception.message ?: "Unable to load your dashboard"
                    )

                    is Result.Loading -> DashboardUiState.Loading
                }
            }
                .catch { e ->
                    _uiState.value = DashboardUiState.Error(e.message ?: "Unable to load your dashboard")
                }
                .collect { _uiState.value = it }
        }
    }

    fun toggleChartType() {
        _chartType.value = if (_chartType.value == ChartType.RING) ChartType.BAR else ChartType.RING
    }

    private fun build(
        wheels: List<Wheel>,
        stats: GlobalStats,
        spinCounts: Map<String, Int>,
        unreadCount: Int,
        recentSpins: List<SpinResult>,
        windowSpins: List<SpinResult>,
        zone: TimeZone
    ): DashboardUiState.Success {
        val wheelsById = wheels.associateBy { it.id }
        val wheelColors = assignWheelColors(wheels)
        val today = Clock.System.now().toLocalDateTime(zone).date

        val spinsByDay = windowSpins
            .groupingBy { it.spinTimestamp.toLocalDateTime(zone).date }
            .eachCount()

        val weekStart = today.minus(WEEK_DAYS - 1, DateTimeUnit.DAY)
        val weekActivity = (0 until WEEK_DAYS).map { offset ->
            val date = weekStart.plus(offset, DateTimeUnit.DAY)
            DayActivity(
                label = DayLabels[date.dayOfWeek.ordinal],
                count = spinsByDay[date] ?: 0,
                isToday = date == today
            )
        }

        val feed = recentSpins.map { spin ->
            val wheel = wheelsById[spin.wheelId]
            RecentSpinItem(
                id = spin.id,
                wheelId = spin.wheelId,
                wheelName = wheel?.name ?: "Removed wheel",
                segmentName = spin.selectedSegmentName,
                color = wheelColors[spin.wheelId] ?: wheelAccentFor(spin.wheelId),
                timestamp = spin.spinTimestamp
            )
        }

        val quickWheels = wheels
            .sortedWith(
                compareByDescending<Wheel> { spinCounts[it.id] ?: 0 }
                    .thenByDescending { it.updatedAt }
            )
            .take(QUICK_WHEEL_LIMIT)
            .map { wheel ->
                QuickWheel(
                    id = wheel.id,
                    name = wheel.name,
                    segmentCount = wheel.segments.size,
                    spinCount = spinCounts[wheel.id] ?: 0,
                    isFavorite = wheel.isFavorite,
                    color = wheelColors[wheel.id] ?: wheelAccentFor(wheel.id),
                    segmentColors = wheel.segments.take(SEGMENT_PREVIEW_LIMIT).map { it.color }
                )
            }

        return DashboardUiState.Success(
            totalWheels = wheels.size,
            totalSpins = stats.totalSpins,
            spinsToday = stats.spinsToday,
            spinsThisWeek = weekActivity.sumOf { it.count },
            dayStreak = streakLength(spinsByDay.keys, today),
            bestDayCount = spinsByDay.values.maxOrNull() ?: 0,
            unreadNotificationCount = unreadCount,
            weekActivity = weekActivity,
            wheelShare = buildShare(wheels, spinCounts, stats.totalSpins, wheelColors),
            topPicks = buildTopPicks(windowSpins.ifEmpty { recentSpins }, wheelsById),
            recentSpins = feed,
            quickWheels = quickWheels
        )
    }

    private fun buildShare(
        wheels: List<Wheel>,
        spinCounts: Map<String, Int>,
        totalSpins: Int,
        wheelColors: Map<String, androidx.compose.ui.graphics.Color>
    ): List<ShareSlice> {
        if (totalSpins <= 0) return emptyList()

        val ranked = wheels
            .map { it to (spinCounts[it.id] ?: 0) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }

        val slices = ranked.take(SHARE_SLICE_LIMIT).map { (wheel, count) ->
            ShareSlice(
                wheelId = wheel.id,
                label = wheel.name,
                count = count,
                percentage = percentOf(count, totalSpins),
                color = wheelColors[wheel.id] ?: wheelAccentFor(wheel.id)
            )
        }

        val remainder = totalSpins - slices.sumOf { it.count }
        if (remainder <= 0) return slices

        return slices + ShareSlice(
            wheelId = null,
            label = if (ranked.size > SHARE_SLICE_LIMIT) "Other wheels" else "Past wheels",
            count = remainder,
            percentage = percentOf(remainder, totalSpins),
            color = OtherSliceColor
        )
    }

    private fun buildTopPicks(
        spins: List<SpinResult>,
        wheelsById: Map<String, Wheel>
    ): List<DistributionItem> {
        if (spins.isEmpty()) return emptyList()

        val counts = spins.groupingBy { it.selectedSegmentName }.eachCount()
        val total = spins.size

        return counts.entries
            .sortedByDescending { it.value }
            .take(TOP_PICK_LIMIT)
            .map { (name, count) ->
                val color = spins
                    .asSequence()
                    .filter { it.selectedSegmentName == name }
                    .mapNotNull { spin ->
                        wheelsById[spin.wheelId]?.segments?.find { it.name == name }?.color
                    }
                    .firstOrNull()
                    ?: wheelAccentFor(name)

                DistributionItem(
                    name = name,
                    count = count,
                    percentage = percentOf(count, total),
                    color = color
                )
            }
    }

    private fun streakLength(daysWithSpins: Set<LocalDate>, today: LocalDate): Int {
        if (daysWithSpins.isEmpty()) return 0

        val yesterday = today.minus(1, DateTimeUnit.DAY)
        var cursor = when {
            daysWithSpins.contains(today) -> today
            daysWithSpins.contains(yesterday) -> yesterday
            else -> return 0
        }

        var streak = 0
        while (daysWithSpins.contains(cursor)) {
            streak++
            cursor = cursor.minus(1, DateTimeUnit.DAY)
        }
        return streak
    }

    private fun assignWheelColors(
        wheels: List<Wheel>
    ): Map<String, androidx.compose.ui.graphics.Color> {
        val taken = mutableSetOf<androidx.compose.ui.graphics.Color>()
        return wheels
            .sortedBy { it.createdAt }
            .associate { wheel ->
                val preferred = wheelAccentFor(wheel.id)
                val color = if (taken.add(preferred)) {
                    preferred
                } else {
                    WheelAccentPalette.firstOrNull { it !in taken }
                        ?.also { taken.add(it) }
                        ?: preferred
                }
                wheel.id to color
            }
    }

    private fun percentOf(count: Int, total: Int): Int =
        if (total <= 0) 0 else Math.round(count * 100f / total)

    private data class CorePack(
        val wheelsResult: Result<List<Wheel>>,
        val stats: GlobalStats,
        val spinCounts: Map<String, Int>,
        val unreadCount: Int
    )
}

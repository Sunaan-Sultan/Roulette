package com.project.roulette.presentation.model

import com.project.roulette.domain.model.SpinResult
import com.project.roulette.domain.model.Wheel
import com.project.roulette.domain.model.WheelStatistics

/**
 * UI state classes for the presentation layer screens.
 * Each screen exposes a sealed UiState to the UI so composables can
 * render loading/success/error states in a type-safe way.
 */

// Home / Wheel list screen state
sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val wheels: List<Wheel>,
        val wheelSpinCounts: Map<String, Int> = emptyMap(),
        val unreadNotificationCount: Int = 0,
        val totalWheels: Int = 0,
        val totalSpins: Int = 0,
        val spinsToday: Int = 0,
        val selectedWheelId: String? = null,
        val currentFilter: HomeFilter = HomeFilter.ALL
    ) : HomeUiState()

    data class Error(val message: String) : HomeUiState()
}

enum class HomeFilter {
    ALL, RECENT, FAVOURITES, MOST_USED, FAVOURITES_RECENT, FAVOURITES_MOST_USED
}

// Home dashboard state
sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val totalWheels: Int = 0,
        val totalSpins: Int = 0,
        val spinsToday: Int = 0,
        val spinsThisWeek: Int = 0,
        val dayStreak: Int = 0,
        val bestDayCount: Int = 0,
        val unreadNotificationCount: Int = 0,
        val weekActivity: List<DayActivity> = emptyList(),
        val wheelShare: List<ShareSlice> = emptyList(),
        val topPicks: List<DistributionItem> = emptyList(),
        val recentSpins: List<RecentSpinItem> = emptyList(),
        val quickWheels: List<QuickWheel> = emptyList()
    ) : DashboardUiState() {
        val hasWheels: Boolean get() = totalWheels > 0
        val hasSpins: Boolean get() = totalSpins > 0
    }

    data class Error(val message: String) : DashboardUiState()
}

data class DayActivity(
    val label: String,
    val count: Int,
    val isToday: Boolean
)

data class ShareSlice(
    val wheelId: String?,
    val label: String,
    val count: Int,
    val percentage: Int,
    val color: androidx.compose.ui.graphics.Color
)

data class RecentSpinItem(
    val id: String,
    val wheelId: String,
    val wheelName: String,
    val segmentName: String,
    val color: androidx.compose.ui.graphics.Color,
    val timestamp: kotlinx.datetime.Instant
)

data class QuickWheel(
    val id: String,
    val name: String,
    val segmentCount: Int,
    val spinCount: Int,
    val isFavorite: Boolean,
    val color: androidx.compose.ui.graphics.Color,
    val segmentColors: List<androidx.compose.ui.graphics.Color> = emptyList()
)

// Single Wheel screen state
sealed class WheelUiState {
    object Loading : WheelUiState()
    data class Success(
        val wheel: Wheel,
        val isSpinning: Boolean = false,
        val spinProgress: Float = 0f,
        val lastSpinResult: SpinResult? = null,
        val recentSpins: List<SpinResult> = emptyList(),
        val algorithmInfo: String? = null, // e.g. "34% chance", "seed #421", "2/4 done"
        val rrRemaining: Int? = null // for RR badge
    ) : WheelUiState()
    data class Error(val message: String) : WheelUiState()
}

// History screen state
sealed class HistoryUiState {
    object Loading : HistoryUiState()
    data class Success(
        val wheel: Wheel,
        val spinResults: List<SpinResult>,
        val filteredResults: List<SpinResult>,
        val totalSpins: Int,
        val avgDuration: Float,
        val mostPickedName: String?,
        val winDistribution: List<DistributionItem>,
        val selectedFilter: String? = null,
        val isDescending: Boolean = true
    ) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}

data class DistributionItem(
    val name: String,
    val count: Int,
    val percentage: Int,
    val color: androidx.compose.ui.graphics.Color
)

data class PdfStats(
    val totalSpins: Int,
    val avgDuration: Float,
    val mostPicked: String?,
    val distribution: List<DistributionItem>
)

// Statistics screen state
sealed class StatisticsUiState {
    object Loading : StatisticsUiState()
    data class Success(
        val wheel: Wheel,
        val statistics: WheelStatistics,
        val spinResults: List<SpinResult>,
        val fairnessScore: Int,
        val fairnessMessage: String,
        val streaks: List<StreakInfo>,
        val timelineData: List<TimelineItem>,
        val chartType: ChartType = ChartType.BAR
    ) : StatisticsUiState()
    data class Error(val message: String) : StatisticsUiState()
}

data class StreakInfo(
    val name: String,
    val count: Int,
    val color: androidx.compose.ui.graphics.Color
)

data class TimelineItem(
    val color: androidx.compose.ui.graphics.Color,
    val index: Int
)

enum class ChartType {
    BAR, RING
}

// Editor (create/edit wheel) screen state
sealed class EditorUiState {
    object Loading : EditorUiState()
    data class Success(
        val wheel: Wheel? = null,
        val isSaving: Boolean = false,
        val saveError: String? = null,
        val isNew: Boolean = true,
        val isSaved: Boolean = false // flag set when save completes successfully
    ) : EditorUiState()
    data class Error(val message: String) : EditorUiState()
}

// Notification screen state
sealed class NotificationUiState {
    object Loading : NotificationUiState()
    data class Success(
        val notifications: List<com.project.roulette.domain.model.Notification>,
        val unreadCount: Int = 0,
        val currentFilter: NotificationFilter = NotificationFilter.ALL
    ) : NotificationUiState()
    data class Error(val message: String) : NotificationUiState()
}

enum class NotificationFilter {
    ALL, UNREAD, ACTIVITY
}

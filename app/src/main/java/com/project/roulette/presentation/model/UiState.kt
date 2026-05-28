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

// Single Wheel screen state
sealed class WheelUiState {
    object Loading : WheelUiState()
    data class Success(
        val wheel: Wheel,
        val isSpinning: Boolean = false,
        val spinProgress: Float = 0f,
        val lastSpinResult: SpinResult? = null
    ) : WheelUiState()
    data class Error(val message: String) : WheelUiState()
}

// History screen state
sealed class HistoryUiState {
    object Loading : HistoryUiState()
    data class Success(
        val wheelName: String,
        val spinResults: List<SpinResult>
    ) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}

// Statistics screen state
sealed class StatisticsUiState {
    object Loading : StatisticsUiState()
    data class Success(
        val wheelName: String,
        val statistics: WheelStatistics
    ) : StatisticsUiState()
    data class Error(val message: String) : StatisticsUiState()
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

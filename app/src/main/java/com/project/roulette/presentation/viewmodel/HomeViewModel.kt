package com.project.roulette.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.roulette.domain.model.Result
import com.project.roulette.domain.model.Wheel
import com.project.roulette.domain.usecase.wheel.CreateWheelUseCase
import com.project.roulette.domain.usecase.wheel.DeleteWheelUseCase
import com.project.roulette.domain.usecase.wheel.GetAllWheelsUseCase
import com.project.roulette.domain.usecase.wheel.GetGlobalStatsUseCase
import com.project.roulette.domain.usecase.wheel.SearchWheelsUseCase
import com.project.roulette.domain.usecase.wheel.ToggleFavoriteUseCase
import com.project.roulette.presentation.model.HomeFilter
import com.project.roulette.presentation.model.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllWheelsUseCase: GetAllWheelsUseCase,
    private val createWheelUseCase: CreateWheelUseCase,
    private val deleteWheelUseCase: DeleteWheelUseCase,
    private val searchWheelsUseCase: SearchWheelsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getGlobalStatsUseCase: GetGlobalStatsUseCase
) : ViewModel() {

    private val _filter = MutableStateFlow(HomeFilter.ALL)
    val filter: StateFlow<HomeFilter> = _filter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _selectedWheelId = MutableStateFlow<String?>(null)
    val selectedWheelId: StateFlow<String?> = _selectedWheelId.asStateFlow()

    init {
        observeWheels()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeWheels() {
        viewModelScope.launch {
            combine(
                _filter,
                _searchQuery,
                getGlobalStatsUseCase(),
                getGlobalStatsUseCase.getSpinCounts()
            ) { filter, query, globalStats, spinCounts ->
                HomeParams(filter, query, globalStats, spinCounts)
            }.flatMapLatest { params ->
                val wheelsFlow = if (params.query.isNotEmpty()) {
                    searchWheelsUseCase(params.query)
                } else {
                    getAllWheelsUseCase()
                }

                wheelsFlow.map { result ->
                    when (result) {
                        is Result.Success -> {
                            val filteredWheels = when (params.filter) {
                                HomeFilter.ALL -> result.data
                                HomeFilter.RECENT -> result.data.sortedByDescending { it.updatedAt }
                                HomeFilter.FAVOURITES -> result.data.filter { it.isFavorite }
                                HomeFilter.MOST_USED -> result.data.sortedByDescending { params.spinCounts[it.id] ?: 0 }
                                HomeFilter.FAVOURITES_RECENT -> result.data.filter { it.isFavorite }.sortedByDescending { it.updatedAt }
                                HomeFilter.FAVOURITES_MOST_USED -> result.data.filter { it.isFavorite }.sortedByDescending { params.spinCounts[it.id] ?: 0 }
                            }
                            HomeUiState.Success(
                                wheels = filteredWheels,
                                wheelSpinCounts = params.spinCounts,
                                totalWheels = result.data.size,
                                totalSpins = params.globalStats.totalSpins,
                                spinsToday = params.globalStats.spinsToday,
                                selectedWheelId = _selectedWheelId.value,
                                currentFilter = params.filter
                            )
                        }
                        is Result.Error -> HomeUiState.Error(result.exception.message ?: "Unknown error")
                        is Result.Loading -> HomeUiState.Loading
                    }
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun loadAllWheels() {
        _searchQuery.value = ""
    }

    fun searchWheels(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: HomeFilter) {
        _filter.value = filter
    }

    fun toggleFavorite(wheelId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            toggleFavoriteUseCase(wheelId, !currentStatus)
        }
    }

    fun createWheel(wheel: Wheel) {
        viewModelScope.launch {
            val result = createWheelUseCase(wheel)
            if (result is Result.Success) {
                _selectedWheelId.value = result.data
            }
        }
    }

    fun deleteWheel(wheelId: String) {
        viewModelScope.launch {
            val result = deleteWheelUseCase(wheelId)
            if (result is Result.Success) {
                if (_selectedWheelId.value == wheelId) {
                    _selectedWheelId.value = null
                }
            }
        }
    }

    fun selectWheel(wheelId: String) {
        _selectedWheelId.value = wheelId
        if (_uiState.value is HomeUiState.Success) {
            val success = _uiState.value as HomeUiState.Success
            _uiState.value = success.copy(selectedWheelId = wheelId)
        }
    }
}

data class HomeParams(
    val filter: HomeFilter,
    val query: String,
    val globalStats: com.project.roulette.domain.usecase.wheel.GlobalStats,
    val spinCounts: Map<String, Int>
)

package com.project.roulette.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.roulette.domain.model.Result
import com.project.roulette.domain.model.Wheel
import com.project.roulette.domain.usecase.wheel.CreateWheelUseCase
import com.project.roulette.domain.usecase.wheel.DeleteWheelUseCase
import com.project.roulette.domain.usecase.wheel.GetAllWheelsUseCase
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
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Home/Wheel List screen.
 * Demonstrates ViewModel pattern for lifecycle-aware state management.
 * Dependency Injection: all use cases injected.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllWheelsUseCase: GetAllWheelsUseCase,
    private val createWheelUseCase: CreateWheelUseCase,
    private val deleteWheelUseCase: DeleteWheelUseCase,
    private val searchWheelsUseCase: SearchWheelsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
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
            combine(_filter, _searchQuery) { filter, query ->
                Pair(filter, query)
            }.flatMapLatest { (_, query) ->
                if (query.isNotEmpty()) {
                    searchWheelsUseCase(query)
                } else {
                    getAllWheelsUseCase()
                }
            }.collect { result ->
                _uiState.value = when (result) {
                    is Result.Success -> {
                        val filteredWheels = when (_filter.value) {
                            HomeFilter.ALL -> result.data
                            HomeFilter.RECENT -> result.data
                            HomeFilter.FAVOURITES -> result.data.filter { it.isFavorite }
                        }
                        HomeUiState.Success(
                            wheels = filteredWheels,
                            selectedWheelId = _selectedWheelId.value,
                            currentFilter = _filter.value
                        )
                    }
                    is Result.Error -> HomeUiState.Error(result.exception.message ?: "Unknown error")
                    is Result.Loading -> HomeUiState.Loading
                }
            }
        }
    }

    /**
     * Load all wheels.
     */
    fun loadAllWheels() {
        _searchQuery.value = ""
    }

    /**
     * Set search query.
     */
    fun searchWheels(query: String) {
        _searchQuery.value = query
    }

    /**
     * Set current filter.
     */
    fun setFilter(filter: HomeFilter) {
        _filter.value = filter
    }

    /**
     * Toggle favorite status.
     */
    fun toggleFavorite(wheelId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            toggleFavoriteUseCase(wheelId, !currentStatus)
        }
    }

    /**
     * Create a new wheel.
     */
    fun createWheel(wheel: Wheel) {
        viewModelScope.launch {
            val result = createWheelUseCase(wheel)
            if (result is Result.Success) {
                _selectedWheelId.value = result.data
            }
        }
    }

    /**
     * Delete a wheel by ID.
     */
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

    /**
     * Select a wheel.
     */
    fun selectWheel(wheelId: String) {
        _selectedWheelId.value = wheelId
        if (_uiState.value is HomeUiState.Success) {
            val success = _uiState.value as HomeUiState.Success
            _uiState.value = success.copy(selectedWheelId = wheelId)
        }
    }
}

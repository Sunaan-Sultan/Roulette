package com.project.roulette.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.roulette.domain.model.Result
import com.project.roulette.domain.model.Wheel
import com.project.roulette.domain.usecase.wheel.CreateWheelUseCase
import com.project.roulette.domain.usecase.wheel.DeleteWheelUseCase
import com.project.roulette.domain.usecase.wheel.GetAllWheelsUseCase
import com.project.roulette.domain.usecase.wheel.GetWheelByIdUseCase
import com.project.roulette.domain.usecase.wheel.SearchWheelsUseCase
import com.project.roulette.domain.usecase.wheel.UpdateWheelUseCase
import com.project.roulette.presentation.model.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val searchWheelsUseCase: SearchWheelsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _selectedWheelId = MutableStateFlow<String?>(null)
    val selectedWheelId: StateFlow<String?> = _selectedWheelId.asStateFlow()

    init {
        loadAllWheels()
    }

    /**
     * Load all wheels.
     */
    fun loadAllWheels() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            getAllWheelsUseCase().collect { result ->
                _uiState.value = when (result) {
                    is Result.Success -> HomeUiState.Success(
                        wheels = result.data,
                        selectedWheelId = _selectedWheelId.value
                    )

                    is Result.Error -> HomeUiState.Error(result.exception.message ?: "Unknown error")
                    is Result.Loading -> HomeUiState.Loading
                }
            }
        }
    }

    /**
     * Create a new wheel.
     */
    fun createWheel(wheel: Wheel) {
        viewModelScope.launch {
            val result = createWheelUseCase(wheel)
            when (result) {
                is Result.Success -> {
                    _selectedWheelId.value = result.data
                    loadAllWheels()
                }

                is Result.Error -> {
                    _uiState.value = HomeUiState.Error(result.exception.message ?: "Failed to create wheel")
                }

                else -> {}
            }
        }
    }

    /**
     * Delete a wheel by ID.
     */
    fun deleteWheel(wheelId: String) {
        viewModelScope.launch {
            val result = deleteWheelUseCase(wheelId)
            when (result) {
                is Result.Success -> {
                    if (_selectedWheelId.value == wheelId) {
                        _selectedWheelId.value = null
                    }
                    loadAllWheels()
                }

                is Result.Error -> {
                    _uiState.value = HomeUiState.Error(result.exception.message ?: "Failed to delete wheel")
                }

                else -> {}
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

    /**
     * Search wheels.
     */
    fun searchWheels(query: String) {
        if (query.isEmpty()) {
            loadAllWheels()
            return
        }

        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            searchWheelsUseCase(query).collect { result ->
                _uiState.value = when (result) {
                    is Result.Success -> HomeUiState.Success(
                        wheels = result.data,
                        selectedWheelId = _selectedWheelId.value
                    )

                    is Result.Error -> HomeUiState.Error(result.exception.message ?: "Search failed")
                    is Result.Loading -> HomeUiState.Loading
                }
            }
        }
    }
}


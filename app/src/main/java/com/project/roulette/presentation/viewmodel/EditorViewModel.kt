package com.project.roulette.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.graphics.Color
import com.project.roulette.domain.model.Result
import com.project.roulette.domain.model.Segment
import com.project.roulette.domain.model.Wheel
import com.project.roulette.domain.usecase.wheel.CreateWheelUseCase
import com.project.roulette.domain.usecase.wheel.GetWheelByIdUseCase
import com.project.roulette.domain.usecase.wheel.UpdateWheelUseCase
import com.project.roulette.presentation.model.EditorUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for Wheel Editor screen.
 */
@HiltViewModel
class EditorViewModel @Inject constructor(
    private val getWheelByIdUseCase: GetWheelByIdUseCase,
    private val createWheelUseCase: CreateWheelUseCase,
    private val updateWheelUseCase: UpdateWheelUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditorUiState>(EditorUiState.Success())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    /**
     * Initialize editor for creating a new wheel.
     */
    fun initializeNew() {
        val initialSegments = listOf(
            Segment(UUID.randomUUID().toString(), "Option 1", Color.Red, 1f),
            Segment(UUID.randomUUID().toString(), "Option 2", Color.Green, 1f),
            Segment(UUID.randomUUID().toString(), "Option 3", Color.Blue, 1f)
        )
        val now = Clock.System.now()
        val newWheel = Wheel(
            id = UUID.randomUUID().toString(),
            name = "New Wheel",
            segments = initialSegments,
            createdAt = now,
            updatedAt = now
        )
        _uiState.value = EditorUiState.Success(
            wheel = newWheel,
            isNew = true,
            isSaved = false
        )
    }

    /**
     * Load wheel for editing.
     */
    fun loadWheel(wheelId: String) {
        viewModelScope.launch {
            _uiState.value = EditorUiState.Loading
            getWheelByIdUseCase(wheelId).collect { result ->
                _uiState.value = when (result) {
                    is Result.Success -> EditorUiState.Success(
                        wheel = result.data,
                        isNew = false,
                        isSaved = false
                    )

                    is Result.Error -> EditorUiState.Error(result.exception.message ?: "Failed to load wheel")
                    is Result.Loading -> EditorUiState.Loading
                }
            }
        }
    }

    /**
     * Update wheel name.
     */
    fun updateWheelName(name: String) {
        val currentState = _uiState.value
        if (currentState is EditorUiState.Success && currentState.wheel != null) {
            val updated = currentState.wheel.copy(
                name = name,
                updatedAt = Clock.System.now()
            )
            _uiState.value = currentState.copy(wheel = updated, isSaved = false)
        }
    }

    /**
     * Update wheel description.
     */
    fun updateWheelDescription(description: String) {
        val currentState = _uiState.value
        if (currentState is EditorUiState.Success && currentState.wheel != null) {
            val updated = currentState.wheel.copy(
                description = description,
                updatedAt = Clock.System.now()
            )
            _uiState.value = currentState.copy(wheel = updated, isSaved = false)
        }
    }

    /**
     * Add a new segment.
     */
    fun addSegment(name: String, color: Color) {
        val currentState = _uiState.value
        if (currentState is EditorUiState.Success && currentState.wheel != null) {
            val newSegment = Segment(
                id = UUID.randomUUID().toString(),
                name = name,
                color = color,
                weight = 1f
            )
            val updated = currentState.wheel.copy(
                segments = currentState.wheel.segments + newSegment,
                updatedAt = Clock.System.now()
            )
            _uiState.value = currentState.copy(wheel = updated, isSaved = false)
        }
    }

    /**
     * Remove a segment.
     */
    fun removeSegment(segmentId: String) {
        val currentState = _uiState.value
        if (currentState is EditorUiState.Success && currentState.wheel != null) {
            val currentSegments = currentState.wheel.segments
            if (currentSegments.size <= 1) {
                // Do not allow removing the last segment - update UI state with an inline error
                _uiState.value = currentState.copy(
                    isSaved = false,
                    saveError = "Cannot remove the last segment"
                )
                return
            }

            val updated = currentState.wheel.copy(
                segments = currentSegments.filter { it.id != segmentId },
                updatedAt = Clock.System.now()
            )
            _uiState.value = currentState.copy(wheel = updated, isSaved = false, saveError = null)
        }
    }

    /**
     * Update segment.
     */
    fun updateSegment(segmentId: String, name: String, color: Color, weight: Float) {
        val currentState = _uiState.value
        if (currentState is EditorUiState.Success && currentState.wheel != null) {
            val updated = currentState.wheel.copy(
                segments = currentState.wheel.segments.map { segment ->
                    if (segment.id == segmentId) {
                        // Ensure we never set a blank name into the domain model.
                        val safeName = if (name.isBlank()) segment.name.ifBlank { "Option" } else name
                        // Ensure weight is positive; fallback to previous weight if invalid
                        val safeWeight = if (weight > 0f) weight else segment.weight
                        segment.copy(name = safeName, color = color, weight = safeWeight)
                    } else {
                        segment
                    }
                },
                updatedAt = Clock.System.now()
            )
            _uiState.value = currentState.copy(wheel = updated, isSaved = false)
        }
    }

    /**
     * Save wheel.
     */
    fun saveWheel() {
        val currentState = _uiState.value
        if (currentState !is EditorUiState.Success || currentState.wheel == null) {
            return
        }

        // Validate and sanitize before saving
        val originalWheel = currentState.wheel
        if (originalWheel.segments.isEmpty()) {
            // Do not attempt to save a wheel without segments
            _uiState.value = currentState.copy(
                isSaving = false,
                saveError = "Wheel must have at least one segment",
                isSaved = false
            )
            return
        }

        // Ensure segment names are not blank (defensive): replace blank names with a default
        val sanitizedSegments = originalWheel.segments.mapIndexed { index, seg ->
            if (seg.name.isBlank()) {
                seg.copy(name = "Option ${index + 1}")
            } else seg
        }

        val sanitizedWheel = originalWheel.copy(segments = sanitizedSegments)

        viewModelScope.launch {
            try {
                _uiState.value = currentState.copy(isSaving = true, saveError = null)

                val result = if (currentState.isNew) {
                    createWheelUseCase(sanitizedWheel)
                } else {
                    updateWheelUseCase(sanitizedWheel)
                }

                when (result) {
                    is Result.Success -> {
                        _uiState.value = currentState.copy(
                            isSaving = false,
                            isNew = false,
                            isSaved = true
                        )
                    }

                    is Result.Error -> {
                        _uiState.value = currentState.copy(
                            isSaving = false,
                            saveError = result.exception.message ?: "Save failed",
                            isSaved = false
                        )
                    }

                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isSaving = false,
                    saveError = e.message ?: "Unknown error",
                    isSaved = false
                )
            }
        }
    }

    /**
     * Clear the inline save error flag/message
     */
    fun clearSaveError() {
        val currentState = _uiState.value
        if (currentState is EditorUiState.Success) {
            _uiState.value = currentState.copy(saveError = null)
        }
    }
}

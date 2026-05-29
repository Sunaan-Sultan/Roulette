package com.project.roulette.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.project.roulette.ui.theme.ThemePalette
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
        val defaultPaletteIndex = 0
        val now = Clock.System.now()

        // Prepare initial segments to avoid Wheel validation error (must have at least one)
        val initialNames = listOf("Option 1", "Option 2", "Option 3")
        val initialSegments = initialNames.map {
            Segment(id = UUID.randomUUID().toString(), name = it, color = Color.Gray, weight = 1f, isActive = true)
        }

        val newWheel = Wheel(
            id = UUID.randomUUID().toString(),
            name = "New Wheel",
            segments = initialSegments,
            createdAt = now,
            updatedAt = now,
            themePaletteIndex = defaultPaletteIndex
        )
        _uiState.value = EditorUiState.Success(wheel = newWheel, isNew = true)

        // Apply initial theme
        updateSegmentColors()
    }

    /**
     * Load wheel for editing.
     */
    fun loadWheel(wheelId: String) {
        viewModelScope.launch {
            _uiState.value = EditorUiState.Loading
            val result = getWheelByIdUseCase(wheelId).first()
            _uiState.value = when (result) {
                is Result.Success<Wheel> -> {
                    val wheel = result.data
                    val activatedSegments = wheel.segments.map { it.copy(isActive = true) }
                    EditorUiState.Success(
                        wheel = wheel.copy(segments = activatedSegments),
                        isNew = false,
                        isSaved = false
                    )
                }
                is Result.Error -> EditorUiState.Error(result.exception.message ?: "Failed to load wheel")
                else -> EditorUiState.Loading
            }
        }
    }

    fun updateWheelName(name: String) {
        _uiState.update { currentState ->
            if (currentState is EditorUiState.Success && currentState.wheel != null) {
                val internalName = if (name.isBlank()) "\u200B" else name
                val updated = currentState.wheel.copy(name = internalName, updatedAt = Clock.System.now())
                currentState.copy(wheel = updated, isSaved = false)
            } else currentState
        }
    }

    fun updateThemePalette(index: Int) {
        _uiState.update { currentState ->
            if (currentState is EditorUiState.Success && currentState.wheel != null) {
                val updated = currentState.wheel.copy(themePaletteIndex = index)
                currentState.copy(wheel = updated, isSaved = false)
            } else currentState
        }
        updateSegmentColors()
    }

    fun updateSpinSound(enabled: Boolean) {
        _uiState.update { currentState ->
            if (currentState is EditorUiState.Success && currentState.wheel != null) {
                val updated = currentState.wheel.copy(spinSound = enabled)
                currentState.copy(wheel = updated, isSaved = false)
            } else currentState
        }
    }

    fun updateRemoveAfterPick(enabled: Boolean) {
        _uiState.update { currentState ->
            if (currentState is EditorUiState.Success && currentState.wheel != null) {
                val updated = currentState.wheel.copy(removeAfterPick = enabled)
                currentState.copy(wheel = updated, isSaved = false)
            } else currentState
        }
    }

    fun addSegment(name: String) {
        _uiState.update { currentState ->
            if (currentState is EditorUiState.Success && currentState.wheel != null) {
                val internalName = name.ifBlank { "\u200B" }
                val newSegment = Segment(
                    id = UUID.randomUUID().toString(),
                    name = internalName,
                    color = Color.Gray,
                    weight = 1f,
                    isActive = true
                )
                val updated = currentState.wheel.copy(
                    segments = currentState.wheel.segments + newSegment,
                    updatedAt = Clock.System.now()
                )
                currentState.copy(wheel = updated, isSaved = false)
            } else currentState
        }
        updateSegmentColors()
    }

    fun removeSegment(segmentId: String) {
        _uiState.update { currentState ->
            if (currentState is EditorUiState.Success && currentState.wheel != null) {
                if (currentState.wheel.segments.size <= 2) {
                    currentState.copy(saveError = "Min 2 segments required")
                } else {
                    val updated = currentState.wheel.copy(
                        segments = currentState.wheel.segments.filter { it.id != segmentId },
                        updatedAt = Clock.System.now()
                    )
                    currentState.copy(wheel = updated, isSaved = false)
                }
            } else currentState
        }
        updateSegmentColors()
    }

    fun updateSegment(segmentId: String, name: String, weight: Float) {
        _uiState.update { currentState ->
            if (currentState is EditorUiState.Success && currentState.wheel != null) {
                val internalName = name.ifBlank { "\u200B" }
                val updated = currentState.wheel.copy(
                    segments = currentState.wheel.segments.map { 
                        if (it.id == segmentId) it.copy(name = internalName, weight = weight, isActive = true) else it
                    },
                    updatedAt = Clock.System.now()
                )
                currentState.copy(wheel = updated, isSaved = false)
            } else currentState
        }
    }

    fun moveSegmentUp(segmentId: String) {
        _uiState.update { currentState ->
            if (currentState is EditorUiState.Success && currentState.wheel != null) {
                val segments = currentState.wheel.segments.toMutableList()
                val index = segments.indexOfFirst { it.id == segmentId }
                if (index > 0) {
                    val item = segments.removeAt(index)
                    segments.add(index - 1, item)
                    val updated = currentState.wheel.copy(segments = segments, updatedAt = Clock.System.now())
                    currentState.copy(wheel = updated, isSaved = false)
                } else currentState
            } else currentState
        }
        updateSegmentColors()
    }

    fun importNames(input: String) {
        _uiState.update { currentState ->
            if (currentState is EditorUiState.Success && currentState.wheel != null) {
                val names = input.split(Regex("[,\\n]")).map { it.trim() }.filter { it.isNotBlank() }
                if (names.isEmpty()) {
                    currentState
                } else {
                    val newSegments = names.map { 
                        Segment(id = UUID.randomUUID().toString(), name = it, color = Color.Gray, weight = 1f, isActive = true) 
                    }
                    val updated = currentState.wheel.copy(
                        segments = currentState.wheel.segments + newSegments,
                        updatedAt = Clock.System.now()
                    )
                    currentState.copy(wheel = updated, isSaved = false)
                }
            } else currentState
        }
        updateSegmentColors()
    }

    private fun updateSegmentColors() {
        _uiState.update { currentState ->
            if (currentState is EditorUiState.Success && currentState.wheel != null) {
                val baseColor = ThemePalette[currentState.wheel.themePaletteIndex]
                val updatedSegments = currentState.wheel.segments.mapIndexed { i, seg ->
                    val hsv = FloatArray(3)
                    android.graphics.Color.colorToHSV(baseColor.toArgb(), hsv)
                    hsv[2] *= (0.5f + (i % 6) * 0.1f).coerceIn(0.2f, 1f)
                    seg.copy(color = Color(android.graphics.Color.HSVToColor(hsv)))
                }
                currentState.copy(wheel = currentState.wheel.copy(segments = updatedSegments))
            } else currentState
        }
    }

    fun saveWheel() {
        val state = _uiState.value as? EditorUiState.Success ?: return
        val wheel = state.wheel ?: return

        viewModelScope.launch {
            _uiState.update { 
                if (it is EditorUiState.Success) it.copy(isSaving = true) else it 
            }
            
            val result = if (state.isNew) createWheelUseCase(wheel) else updateWheelUseCase(wheel)
            
            _uiState.update { current ->
                if (current is EditorUiState.Success) {
                    when (result) {
                        is Result.Success -> current.copy(isSaving = false, isSaved = true)
                        is Result.Error -> current.copy(isSaving = false, saveError = result.exception.message)
                        else -> current.copy(isSaving = false)
                    }
                } else current
            }
        }
    }

    fun clearSaveError() {
        _uiState.update { currentState ->
            if (currentState is EditorUiState.Success) currentState.copy(saveError = null) else currentState
        }
    }
}

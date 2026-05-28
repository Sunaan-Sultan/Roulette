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
        val themeColors = listOf(
            Color(0xFF673AB7), Color(0xFF00796B), Color(0xFFD84315), Color(0xFF1976D2),
            Color(0xFFC2185B), Color(0xFFFFA000), Color(0xFF388E3C), Color(0xFF616161)
        )
        val defaultPaletteIndex = 0
        val paletteBaseColor = themeColors[defaultPaletteIndex]
        
        val initialSegments = listOf("Option 1", "Option 2", "Option 3").mapIndexed { i, name ->
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(paletteBaseColor.toArgb(), hsv)
            hsv[2] *= (0.6f + (i % 5) * 0.1f).coerceIn(0.3f, 1.0f)
            Segment(
                id = UUID.randomUUID().toString(),
                name = name,
                color = Color(android.graphics.Color.HSVToColor(hsv)),
                weight = 1f
            )
        }
        val now = Clock.System.now()
        val newWheel = Wheel(
            id = UUID.randomUUID().toString(),
            name = "New Wheel",
            segments = initialSegments,
            createdAt = now,
            updatedAt = now,
            themePaletteIndex = defaultPaletteIndex
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
            // Use a zero-width space placeholder if the name is cleared during editing
            // to avoid domain validation crash (require(name.isNotBlank()))
            // isNotBlank() returns false for whitespace-only strings like " ".
            // \u200B is a zero-width space which is NOT considered whitespace by Kotlin's isWhitespace().
            val internalName = if (name.isBlank()) "\u200B" else name
            val updated = currentState.wheel.copy(
                name = internalName,
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
    fun addSegment(name: String) {
        val currentState = _uiState.value
        if (currentState is EditorUiState.Success && currentState.wheel != null) {
            val themeColors = listOf(
                Color(0xFF673AB7), Color(0xFF00796B), Color(0xFFD84315), Color(0xFF1976D2),
                Color(0xFFC2185B), Color(0xFFFFA000), Color(0xFF388E3C), Color(0xFF616161)
            )
            val paletteBaseColor = themeColors[currentState.wheel.themePaletteIndex]
            val index = currentState.wheel.segments.size
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(paletteBaseColor.toArgb(), hsv)
            hsv[2] *= (0.6f + (index % 5) * 0.1f).coerceIn(0.3f, 1.0f)
            
            val newSegment = Segment(
                id = UUID.randomUUID().toString(),
                name = name,
                color = Color(android.graphics.Color.HSVToColor(hsv)),
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
            if (currentSegments.size <= 2) {
                // Do not allow removing if only 2 segments left
                _uiState.value = currentState.copy(
                    isSaved = false,
                    saveError = "A wheel must have at least 2 segments"
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
                        // Use placeholder to prevent crash during drafting
                        val safeName = if (name.isBlank()) "\u200B" else name
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
        
        // Final validation for Name (strip placeholders)
        val finalName = originalWheel.name.replace("\u200B", "").trim()
        if (finalName.isBlank()) {
             _uiState.value = currentState.copy(
                isSaving = false,
                saveError = "Wheel name cannot be empty",
                isSaved = false
            )
            return
        }

        if (originalWheel.segments.size < 2) {
            // Do not attempt to save a wheel with fewer than 2 segments
            _uiState.value = currentState.copy(
                isSaving = false,
                saveError = "Wheel must have at least 2 segments",
                isSaved = false
            )
            return
        }

        // Ensure segment names are not blank (defensive): replace blank names with a default
        val sanitizedSegments = originalWheel.segments.mapIndexed { index, seg ->
            val sName = seg.name.replace("\u200B", "").trim()
            if (sName.isBlank()) {
                seg.copy(name = "Option ${index + 1}")
            } else {
                seg.copy(name = sName)
            }
        }

        val sanitizedWheel = originalWheel.copy(
            name = finalName,
            segments = sanitizedSegments
        )

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
     * Update spin sound setting.
     */
    fun updateSpinSound(enabled: Boolean) {
        val currentState = _uiState.value
        if (currentState is EditorUiState.Success && currentState.wheel != null) {
            val updated = currentState.wheel.copy(spinSound = enabled)
            _uiState.value = currentState.copy(wheel = updated, isSaved = false)
        }
    }

    /**
     * Update remove after pick setting.
     */
    fun updateRemoveAfterPick(enabled: Boolean) {
        val currentState = _uiState.value
        if (currentState is EditorUiState.Success && currentState.wheel != null) {
            val updated = currentState.wheel.copy(removeAfterPick = enabled)
            _uiState.value = currentState.copy(wheel = updated, isSaved = false)
        }
    }

    /**
     * Update theme palette index and refresh all segment colors.
     */
    fun updateThemePalette(index: Int) {
        val currentState = _uiState.value
        val themeColors = listOf(
            Color(0xFF673AB7), Color(0xFF00796B), Color(0xFFD84315), Color(0xFF1976D2),
            Color(0xFFC2185B), Color(0xFFFFA000), Color(0xFF388E3C), Color(0xFF616161)
        )
        if (currentState is EditorUiState.Success && currentState.wheel != null) {
            val paletteBaseColor = themeColors[index]
            val updatedSegments = currentState.wheel.segments.mapIndexed { i, segment ->
                val hsv = FloatArray(3)
                android.graphics.Color.colorToHSV(paletteBaseColor.toArgb(), hsv)
                hsv[2] *= (0.6f + (i % 5) * 0.1f).coerceIn(0.3f, 1.0f)
                segment.copy(color = Color(android.graphics.Color.HSVToColor(hsv)))
            }
            val updated = currentState.wheel.copy(
                themePaletteIndex = index,
                segments = updatedSegments
            )
            _uiState.value = currentState.copy(wheel = updated, isSaved = false)
        }
    }

    private fun Color.toArgb(): Int {
        return (this.alpha * 255.0f + 0.5f).toInt() shl 24 or
               ((this.red * 255.0f + 0.5f).toInt() shl 16) or
               ((this.green * 255.0f + 0.5f).toInt() shl 8) or
               (this.blue * 255.0f + 0.5f).toInt()
    }

    /**
     * Import names from a comma or newline separated string.
     */
    fun importNames(input: String) {
        val currentState = _uiState.value
        if (currentState is EditorUiState.Success && currentState.wheel != null) {
            val names = input.split(Regex("[,\\n]"))
                .map { it.trim() }
                .filter { it.isNotBlank() }
            
            if (names.isEmpty()) return

            val themeColors = listOf(
                Color(0xFF673AB7), Color(0xFF00796B), Color(0xFFD84315), Color(0xFF1976D2),
                Color(0xFFC2185B), Color(0xFFFFA000), Color(0xFF388E3C), Color(0xFF616161)
            )
            val paletteBaseColor = themeColors[currentState.wheel.themePaletteIndex]
            val baseIndex = currentState.wheel.segments.size

            val newSegments = names.mapIndexed { i, name ->
                val hsv = FloatArray(3)
                android.graphics.Color.colorToHSV(paletteBaseColor.toArgb(), hsv)
                hsv[2] *= (0.6f + ((baseIndex + i) % 5) * 0.1f).coerceIn(0.3f, 1.0f)
                
                Segment(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    color = Color(android.graphics.Color.HSVToColor(hsv)),
                    weight = 1f
                )
            }
            
            val updated = currentState.wheel.copy(
                segments = currentState.wheel.segments + newSegments,
                updatedAt = Clock.System.now()
            )
            _uiState.value = currentState.copy(wheel = updated, isSaved = false)
        }
    }

    /**
     * Move segment up in the list.
     */
    fun moveSegmentUp(segmentId: String) {
        val currentState = _uiState.value
        if (currentState is EditorUiState.Success && currentState.wheel != null) {
            val segments = currentState.wheel.segments.toMutableList()
            val index = segments.indexOfFirst { it.id == segmentId }
            if (index > 0) {
                val segment = segments.removeAt(index)
                segments.add(index - 1, segment)
                val updated = currentState.wheel.copy(
                    segments = segments,
                    updatedAt = Clock.System.now()
                )
                _uiState.value = currentState.copy(wheel = updated, isSaved = false)
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

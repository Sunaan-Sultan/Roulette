package com.project.roulette.util

import com.project.roulette.domain.model.Wheel

/**
 * Temporary holder for Wheel data during preview.
 * Used to pass data between Editor and Preview screens without persisting to DB or serialization.
 */
object PreviewData {
    var previewWheel: Wheel? = null
    var onSave: (() -> Unit)? = null
}

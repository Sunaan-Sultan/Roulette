package com.project.roulette.domain.model

import kotlinx.datetime.Instant

enum class NotificationType {
    SPIN_RESULT, REMINDER, FAVOURITE, MILESTONE, UPDATE, TIP, STREAK, ANNOUNCEMENT
}

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: Instant,
    val isRead: Boolean = false,
    val targetId: String? = null
)

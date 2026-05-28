package com.project.roulette.domain.repository

import com.project.roulette.domain.model.Notification
import com.project.roulette.domain.model.Result
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getAllNotifications(): Flow<Result<List<Notification>>>
    fun getUnreadCount(): Flow<Int>
    suspend fun markAsRead(id: String): Result<Unit>
    suspend fun markAllAsRead(): Result<Unit>
    suspend fun addNotification(notification: Notification): Result<Unit>
    suspend fun deleteNotification(id: String): Result<Unit>
}

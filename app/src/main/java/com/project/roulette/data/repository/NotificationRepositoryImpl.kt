package com.project.roulette.data.repository

import com.project.roulette.data.local.database.NotificationDao
import com.project.roulette.data.mapper.NotificationMapper
import com.project.roulette.domain.model.Notification
import com.project.roulette.domain.model.Result
import com.project.roulette.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao,
    private val mapper: NotificationMapper
) : NotificationRepository {

    override fun getAllNotifications(): Flow<Result<List<Notification>>> =
        notificationDao.getAllNotifications()
            .map { entities ->
                val list = entities.map { mapper.entityToDomain(it) }
                Result.Success(list) as Result<List<Notification>>
            }
            .catch { e ->
                emit(Result.Error(Exception("Failed to load notifications: ${e.message}", e)))
            }

    override fun getUnreadCount(): Flow<Int> = notificationDao.getUnreadCount()

    override suspend fun markAsRead(id: String): Result<Unit> = try {
        notificationDao.markAsRead(id)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun markAllAsRead(): Result<Unit> = try {
        notificationDao.markAllAsRead()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun addNotification(notification: Notification): Result<Unit> = try {
        notificationDao.insertNotification(mapper.domainToEntity(notification))
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }

    override suspend fun deleteNotification(id: String): Result<Unit> = try {
        notificationDao.deleteNotification(id)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e)
    }
}

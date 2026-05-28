package com.project.roulette.data.mapper

import com.project.roulette.data.local.database.entity.NotificationEntity
import com.project.roulette.domain.model.Notification
import com.project.roulette.domain.model.NotificationType
import kotlinx.datetime.Instant
import javax.inject.Inject

class NotificationMapper @Inject constructor() {
    fun entityToDomain(entity: NotificationEntity): Notification {
        return Notification(
            id = entity.id,
            title = entity.title,
            message = entity.message,
            type = try {
                NotificationType.valueOf(entity.type)
            } catch (e: Exception) {
                NotificationType.ANNOUNCEMENT
            },
            timestamp = Instant.fromEpochMilliseconds(entity.timestamp),
            isRead = entity.isRead,
            targetId = entity.targetId
        )
    }

    fun domainToEntity(domain: Notification): NotificationEntity {
        return NotificationEntity(
            id = domain.id,
            title = domain.title,
            message = domain.message,
            type = domain.type.name,
            timestamp = domain.timestamp.toEpochMilliseconds(),
            isRead = domain.isRead,
            targetId = domain.targetId
        )
    }
}

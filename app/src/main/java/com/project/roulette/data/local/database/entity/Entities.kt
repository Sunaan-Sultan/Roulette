package com.project.roulette.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Room entity for Wheel persistence.
 * Segments are stored as a JSON string (list of SegmentEntity)
 */
@Entity(tableName = "wheels")
data class WheelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val createdAt: Long, // epoch milliseconds
    val updatedAt: Long, // epoch milliseconds
    val segments: String, // JSON serialized list of SegmentEntity
    val isFavorite: Boolean = false,
    val spinSound: Boolean = true,
    val removeAfterPick: Boolean = false,
    val themePaletteIndex: Int = 0
)

/**
 * Serializable representation of Segment used when storing inside WheelEntity.segments
 */
@Serializable
data class SegmentEntity(
    val id: String,
    val name: String,
    val colorValue: Long, // Color ARGB as Long
    val weight: Float = 1f,
    val isActive: Boolean = true
)

/**
 * Room entity for Spin History records.
 */
@Entity(tableName = "spin_history")
data class SpinHistoryEntity(
    @PrimaryKey val id: String,
    val wheelId: String,
    val selectedSegmentId: String,
    val selectedSegmentName: String,
    val spinTimestamp: Long,
    val spinDuration: Long,
    val finalAngle: Float
)

/**
 * Room entity for Notifications.
 */
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val type: String, // enum name
    val timestamp: Long,
    val isRead: Boolean = false,
    val targetId: String? = null
)

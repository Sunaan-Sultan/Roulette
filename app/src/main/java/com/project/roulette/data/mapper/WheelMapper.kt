package com.project.roulette.data.mapper

import androidx.compose.ui.graphics.Color
import com.project.roulette.data.local.database.entity.SegmentEntity
import com.project.roulette.data.local.database.entity.WheelEntity
import com.project.roulette.domain.model.Segment
import com.project.roulette.domain.model.Wheel
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mapper for converting between Wheel entities (data layer) and domain models.
 * Maintains architectural boundary between data and domain layers.
 * Single Responsibility: only responsible for mapping logic.
 */
@Singleton
class WheelMapper @Inject constructor() {
    fun segmentToEntity(segment: Segment): SegmentEntity {
        return SegmentEntity(
            id = segment.id,
            name = segment.name,
            colorValue = segment.color.value.toLong(),
            weight = segment.weight,
            isActive = segment.isActive
        )
    }

    fun entityToSegment(entity: SegmentEntity): Segment {
        // sanitize values coming from persistence to avoid domain model validation failures
        val name = if (entity.name.isBlank()) "Unnamed" else entity.name
        val weight = if (entity.weight <= 0f) 1f else entity.weight
        return Segment(
            id = entity.id,
            name = name,
            color = Color(entity.colorValue.toULong()),
            weight = weight,
            isActive = entity.isActive
        )
    }

    fun wheelToEntity(wheel: Wheel): WheelEntity {
        val segmentEntities = wheel.segments.map { segmentToEntity(it) }
        val segmentsJson = Json.encodeToString(segmentEntities)

        return WheelEntity(
            id = wheel.id,
            name = wheel.name,
            description = wheel.description,
            createdAt = wheel.createdAt.toEpochMilliseconds(),
            updatedAt = wheel.updatedAt.toEpochMilliseconds(),
            segments = segmentsJson
        )
    }

    fun entityToWheel(entity: WheelEntity): Wheel {
        val segmentEntities = try {
            Json.decodeFromString<List<SegmentEntity>>(entity.segments)
        } catch (e: Exception) {
            emptyList()
        }

        val segments = segmentEntities.map { entityToSegment(it) }

        return Wheel(
            id = entity.id,
            name = entity.name,
            segments = segments,
            createdAt = Instant.fromEpochMilliseconds(entity.createdAt),
            updatedAt = Instant.fromEpochMilliseconds(entity.updatedAt),
            description = entity.description
        )
    }

    fun createDefaultWheel(name: String, segments: List<Segment>): Wheel {
        val now = kotlinx.datetime.Clock.System.now()
        return Wheel(
            id = UUID.randomUUID().toString(),
            name = name,
            segments = segments,
            createdAt = now,
            updatedAt = now
        )
    }
}

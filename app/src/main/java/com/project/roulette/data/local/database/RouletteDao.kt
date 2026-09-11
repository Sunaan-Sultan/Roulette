package com.project.roulette.data.local.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.project.roulette.data.local.database.entity.WheelEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Wheel operations.
 * Abstraction layer for database queries.
 */
@Dao
interface WheelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWheel(wheel: WheelEntity)

    @Update
    suspend fun updateWheel(wheel: WheelEntity)

    @Delete
    suspend fun deleteWheel(wheel: WheelEntity)

    @Query("SELECT * FROM wheels WHERE id = :wheelId")
    fun getWheelById(wheelId: String): Flow<WheelEntity?>

    @Query("SELECT * FROM wheels ORDER BY updatedAt DESC")
    fun getAllWheels(): Flow<List<WheelEntity>>

    @Query("SELECT * FROM wheels WHERE name LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchWheels(query: String): Flow<List<WheelEntity>>

    @Query("SELECT COUNT(*) > 0 FROM wheels WHERE id = :wheelId")
    suspend fun wheelExists(wheelId: String): Boolean

    @Query("DELETE FROM wheels WHERE id = :wheelId")
    suspend fun deleteWheelById(wheelId: String)

    @Query("UPDATE wheels SET isFavorite = :isFavorite WHERE id = :wheelId")
    suspend fun updateFavorite(wheelId: String, isFavorite: Boolean)
}

/**
 * Data Access Object for Spin History operations.
 */
@Dao
interface SpinHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpin(spin: com.project.roulette.data.local.database.entity.SpinHistoryEntity)

    @Query("SELECT * FROM spin_history WHERE wheelId = :wheelId ORDER BY spinTimestamp DESC")
    fun getSpinsForWheel(wheelId: String): Flow<List<com.project.roulette.data.local.database.entity.SpinHistoryEntity>>

    @Query("SELECT * FROM spin_history WHERE wheelId = :wheelId ORDER BY spinTimestamp DESC LIMIT :limit")
    fun getRecentSpins(
        wheelId: String,
        limit: Int
    ): Flow<List<com.project.roulette.data.local.database.entity.SpinHistoryEntity>>

    @Query("SELECT COUNT(*) FROM spin_history WHERE wheelId = :wheelId AND selectedSegmentId = :segmentId")
    suspend fun getSegmentSpinCount(wheelId: String, segmentId: String): Int

    @Query("SELECT COUNT(*) FROM spin_history WHERE wheelId = :wheelId")
    suspend fun getTotalSpinCount(wheelId: String): Int

    @Query("SELECT COUNT(*) FROM spin_history")
    fun getGlobalSpinCountFlow(): Flow<Int>

    @Query("SELECT * FROM spin_history ORDER BY spinTimestamp DESC LIMIT :limit")
    fun getRecentSpinsGlobal(limit: Int): Flow<List<com.project.roulette.data.local.database.entity.SpinHistoryEntity>>

    @Query("SELECT * FROM spin_history WHERE spinTimestamp >= :since ORDER BY spinTimestamp DESC")
    fun getSpinsSince(since: Long): Flow<List<com.project.roulette.data.local.database.entity.SpinHistoryEntity>>

    @Query("SELECT COUNT(*) FROM spin_history WHERE spinTimestamp >= :startOfDay")
    fun getSpinsTodayCountFlow(startOfDay: Long): Flow<Int>

    @Query("DELETE FROM spin_history WHERE wheelId = :wheelId")
    suspend fun clearWheelHistory(wheelId: String)

    @Query("SELECT wheelId, COUNT(*) as count FROM spin_history GROUP BY wheelId")
    fun getAllWheelSpinCounts(): Flow<List<WheelSpinCount>>

    @Query("SELECT * FROM spin_history WHERE wheelId = :wheelId ORDER BY spinTimestamp DESC")
    suspend fun getAllSpinsForWheelSync(wheelId: String): List<com.project.roulette.data.local.database.entity.SpinHistoryEntity>
}

/**
 * Data Access Object for Notification operations.
 */
@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: com.project.roulette.data.local.database.entity.NotificationEntity)

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<com.project.roulette.data.local.database.entity.NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)
}

data class WheelSpinCount(
    val wheelId: String,
    val count: Int
)


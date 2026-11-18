package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineAttendanceDao {
    @Query("SELECT * FROM offline_attendance ORDER BY clientUpdatedAtMs DESC")
    fun observeAll(): Flow<List<OfflineAttendanceEntity>>

    @Query("SELECT COUNT(*) FROM offline_attendance WHERE syncStatus IN ('PENDING', 'FAILED')")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT * FROM offline_attendance WHERE syncStatus IN ('PENDING', 'FAILED') ORDER BY clientUpdatedAtMs ASC")
    suspend fun getPendingSyncList(): List<OfflineAttendanceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: OfflineAttendanceEntity)

    @Query("UPDATE offline_attendance SET syncStatus = :status, errorMessage = :errorMessage WHERE localId = :localId")
    suspend fun updateSyncStatus(localId: Long, status: String, errorMessage: String? = null)

    @Query("DELETE FROM offline_attendance WHERE syncStatus = 'SYNCED'")
    suspend fun clearSynced()
}

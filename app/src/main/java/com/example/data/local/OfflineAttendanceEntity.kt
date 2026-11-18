package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "offline_attendance",
    indices = [
        Index(value = ["compositeKey"], unique = true),
        Index(value = ["syncStatus"])
    ]
)
data class OfflineAttendanceEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val compositeKey: String,
    val orgId: String,
    val dateKey: String,
    val personType: String,
    val personId: String,
    val personName: String,
    val department: String,
    val className: String,
    val status: String,
    val lateArrival: Boolean,
    val auditNote: String,
    val syncEventId: String,
    val readerUidsCsv: String,
    val syncStatus: String = "PENDING", // PENDING, SYNCED, CONFLICT, FAILED
    val clientUpdatedAtMs: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)

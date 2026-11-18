package com.example.data.local

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class AttendanceSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val db = MadrasaLocalDatabase.getInstance(applicationContext)
        val offlineDao = db.offlineAttendanceDao()
        val madrasaDao = db.madrasaDao()
        val pending = offlineDao.getPendingSyncList()
        if (pending.isEmpty()) return Result.success()

        for (item in pending) {
            try {
                madrasaDao.upsertAttendance(
                    AttendanceEntity(
                        id = item.compositeKey,
                        orgId = item.orgId,
                        dateKey = item.dateKey,
                        personType = item.personType,
                        personId = item.personId,
                        personName = item.personName,
                        department = item.department,
                        className = item.className,
                        status = item.status,
                        lateArrival = item.lateArrival,
                        auditNote = item.auditNote.ifBlank { null },
                        syncEventId = item.syncEventId
                    )
                )
                offlineDao.updateSyncStatus(item.localId, "SYNCED", null)
            } catch (e: Exception) {
                offlineDao.updateSyncStatus(item.localId, "FAILED", e.localizedMessage)
            }
        }
        return Result.success()
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "madrasa_attendance_sync"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = OneTimeWorkRequestBuilder<AttendanceSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context.applicationContext)
                .enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        }
    }
}

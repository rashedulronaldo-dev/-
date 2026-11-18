package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        OfflineAttendanceEntity::class,
        OrganizationEntity::class,
        StudentEntity::class,
        TeacherEntity::class,
        GuardianEntity::class,
        AttendanceEntity::class,
        QuranProgressEntity::class,
        ExamEntity::class,
        ExamResultEntity::class,
        InvoiceEntity::class,
        PaymentEntity::class,
        LedgerAccountEntity::class,
        JournalEntryEntity::class,
        DonationEntity::class,
        NoticeEntity::class,
        AuditLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MadrasaLocalDatabase : RoomDatabase() {
    abstract fun offlineAttendanceDao(): OfflineAttendanceDao
    abstract fun madrasaDao(): MadrasaDao

    companion object {
        @Volatile
        private var INSTANCE: MadrasaLocalDatabase? = null

        fun getInstance(context: Context): MadrasaLocalDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MadrasaLocalDatabase::class.java,
                    "madrasa_local_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

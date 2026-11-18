package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MadrasaDao {
    // Organization
    @Query("SELECT * FROM organizations WHERE id = :orgId LIMIT 1")
    fun observeOrganization(orgId: String): Flow<OrganizationEntity?>

    @Query("SELECT * FROM organizations WHERE id = :orgId LIMIT 1")
    suspend fun getOrganizationOnce(orgId: String): OrganizationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOrganization(org: OrganizationEntity)

    // Students
    @Query("SELECT * FROM students ORDER BY updatedAtMillis DESC")
    fun observeStudents(): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStudent(student: StudentEntity)

    @Query("UPDATE students SET archived = 1, updatedAtMillis = :ts WHERE id = :id")
    suspend fun archiveStudent(id: String, ts: Long = System.currentTimeMillis())

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudent(id: String)

    // Teachers
    @Query("SELECT * FROM teachers ORDER BY updatedAtMillis DESC")
    fun observeTeachers(): Flow<List<TeacherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTeacher(teacher: TeacherEntity)

    // Guardians
    @Query("SELECT * FROM guardians ORDER BY updatedAtMillis DESC")
    fun observeGuardians(): Flow<List<GuardianEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGuardian(guardian: GuardianEntity)

    // Attendance
    @Query("SELECT * FROM attendance_records ORDER BY dateKey DESC, updatedAtMillis DESC")
    fun observeAttendance(): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAttendance(record: AttendanceEntity)

    // Quran Progress
    @Query("SELECT * FROM quran_progress ORDER BY dateKey DESC, updatedAtMillis DESC")
    fun observeQuranProgress(): Flow<List<QuranProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuranProgress(progress: QuranProgressEntity)

    // Exams & Results
    @Query("SELECT * FROM exams ORDER BY updatedAtMillis DESC")
    fun observeExams(): Flow<List<ExamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExam(exam: ExamEntity)

    @Query("UPDATE exams SET published = :published, updatedAtMillis = :ts WHERE id = :examId")
    suspend fun setExamPublished(examId: String, published: Boolean, ts: Long = System.currentTimeMillis())

    @Query("SELECT * FROM exam_results ORDER BY obtainedMarks DESC")
    fun observeExamResults(): Flow<List<ExamResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExamResult(result: ExamResultEntity)

    @Query("UPDATE exam_results SET published = :published, updatedAtMillis = :ts WHERE examId = :examId")
    suspend fun setExamResultsPublished(examId: String, published: Boolean, ts: Long = System.currentTimeMillis())

    // Invoices & Payments
    @Query("SELECT * FROM invoices ORDER BY updatedAtMillis DESC")
    fun observeInvoices(): Flow<List<InvoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInvoice(invoice: InvoiceEntity)

    @Query("SELECT * FROM payments ORDER BY updatedAtMillis DESC")
    fun observePayments(): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPayment(payment: PaymentEntity)

    @Query("SELECT * FROM payments WHERE id = :paymentId LIMIT 1")
    suspend fun getPaymentOnce(paymentId: String): PaymentEntity?

    // Accounting
    @Query("SELECT * FROM ledger_accounts ORDER BY code ASC")
    fun observeAccounts(): Flow<List<LedgerAccountEntity>>

    @Query("SELECT * FROM ledger_accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountOnce(id: String): LedgerAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAccount(account: LedgerAccountEntity)

    @Query("SELECT * FROM journal_entries ORDER BY createdAtMillis DESC")
    fun observeJournalEntries(): Flow<List<JournalEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntryEntity)

    // Donations
    @Query("SELECT * FROM donations ORDER BY updatedAtMillis DESC")
    fun observeDonations(): Flow<List<DonationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDonation(donation: DonationEntity)

    // Notices
    @Query("SELECT * FROM notices ORDER BY updatedAtMillis DESC")
    fun observeNotices(): Flow<List<NoticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNotice(notice: NoticeEntity)

    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY createdAtMillis DESC LIMIT 50")
    fun observeAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)
}

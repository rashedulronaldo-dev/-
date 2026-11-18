package com.example.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.core.BengaliFormat
import com.example.data.local.AttendanceEntity
import com.example.data.local.AttendanceSyncWorker
import com.example.data.local.AuditLogEntity
import com.example.data.local.DonationEntity
import com.example.data.local.ExamEntity
import com.example.data.local.ExamResultEntity
import com.example.data.local.GuardianEntity
import com.example.data.local.InvoiceEntity
import com.example.data.local.JournalEntryEntity
import com.example.data.local.LedgerAccountEntity
import com.example.data.local.MadrasaDao
import com.example.data.local.MadrasaLocalDatabase
import com.example.data.local.NoticeEntity
import com.example.data.local.OfflineAttendanceDao
import com.example.data.local.OfflineAttendanceEntity
import com.example.data.local.OrganizationEntity
import com.example.data.local.PaymentEntity
import com.example.data.local.QuranProgressEntity
import com.example.data.local.StudentEntity
import com.example.data.local.TeacherEntity
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.AuditLog
import com.example.data.model.Donation
import com.example.data.model.Exam
import com.example.data.model.ExamResult
import com.example.data.model.Guardian
import com.example.data.model.Invoice
import com.example.data.model.JournalEntry
import com.example.data.model.LedgerAccount
import com.example.data.model.Notice
import com.example.data.model.Organization
import com.example.data.model.Payment
import com.example.data.model.PaymentGateway
import com.example.data.model.PaymentStatus
import com.example.data.model.QuranProgress
import com.example.data.model.Student
import com.example.data.model.Teacher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class MadrasaRepository(
    private val appContext: Context,
    private val dao: MadrasaDao,
    private val offlineDao: OfflineAttendanceDao
) {
    constructor(context: Context) : this(
        appContext = context.applicationContext,
        dao = MadrasaLocalDatabase.getInstance(context.applicationContext).madrasaDao(),
        offlineDao = MadrasaLocalDatabase.getInstance(context.applicationContext).offlineAttendanceDao()
    )

    companion object {
        const val DEFAULT_ORG_ID = "kmnqhm_1995"
    }

    fun observeOrganization(orgId: String = DEFAULT_ORG_ID): Flow<Organization?> =
        dao.observeOrganization(orgId).map { it?.toDomain() }

    fun observeStudents(): Flow<List<Student>> =
        dao.observeStudents().map { list -> list.map { it.toDomain() } }

    fun observeTeachers(): Flow<List<Teacher>> =
        dao.observeTeachers().map { list -> list.map { it.toDomain() } }

    fun observeGuardians(): Flow<List<Guardian>> =
        dao.observeGuardians().map { list -> list.map { it.toDomain() } }

    fun observeAttendance(): Flow<List<AttendanceRecord>> =
        dao.observeAttendance().map { list -> list.map { it.toDomain() } }

    fun observeQuranProgress(): Flow<List<QuranProgress>> =
        dao.observeQuranProgress().map { list -> list.map { it.toDomain() } }

    fun observeExams(): Flow<List<Exam>> =
        dao.observeExams().map { list -> list.map { it.toDomain() } }

    fun observeExamResults(): Flow<List<ExamResult>> =
        dao.observeExamResults().map { list -> list.map { it.toDomain() } }

    fun observeInvoices(): Flow<List<Invoice>> =
        dao.observeInvoices().map { list -> list.map { it.toDomain() } }

    fun observePayments(): Flow<List<Payment>> =
        dao.observePayments().map { list -> list.map { it.toDomain() } }

    fun observeAccounts(): Flow<List<LedgerAccount>> =
        dao.observeAccounts().map { list -> list.map { it.toDomain() } }

    fun observeJournalEntries(): Flow<List<JournalEntry>> =
        dao.observeJournalEntries().map { list -> list.map { it.toDomain() } }

    fun observeDonations(): Flow<List<Donation>> =
        dao.observeDonations().map { list -> list.map { it.toDomain() } }

    fun observeNotices(): Flow<List<Notice>> =
        dao.observeNotices().map { list -> list.map { it.toDomain() } }

    fun observeAuditLogs(): Flow<List<AuditLog>> =
        dao.observeAuditLogs().map { list -> list.map { it.toDomain() } }

    fun observeOfflineAttendanceQueue(): Flow<List<OfflineAttendanceEntity>> =
        offlineDao.observeAll()

    fun observeOfflinePendingCount(): Flow<Int> =
        offlineDao.observePendingCount()

    suspend fun ensureOrganizationBootstrapped(orgId: String = DEFAULT_ORG_ID): Result<Unit> {
        return try {
            val existing = dao.getOrganizationOnce(orgId)
            if (existing == null) {
                dao.upsertOrganization(
                    OrganizationEntity(
                        id = orgId,
                        nameBn = "খেলার মাঠ নূরানী তালিমূল কোরআন ও হাফিজিয়া মাদরাসা",
                        establishedYear = "১৯৯৫",
                        currencySymbol = "৳",
                        timezone = "Asia/Dhaka",
                        academicYear = "২০২৬",
                        address = "খেলার মাঠ, ঢাকা",
                        phone = "০১৭০০-০০০০০০"
                    )
                )
                initializeChartOfAccounts(orgId)
                logAudit("প্রতিষ্ঠান স্থাপন", "মাদরাসা ও হিসাববিজ্ঞান খাত স্বয়ংক্রিয়ভাবে চালু করা হয়েছে।", orgId)
            } else if (existing.nameBn == "খেলার মাঠ নূরানী তালিমুল কুরআন ও হাফিজিয়া মাদরাসা") {
                dao.upsertOrganization(
                    existing.copy(
                        nameBn = "খেলার মাঠ নূরানী তালিমূল কোরআন ও হাফিজিয়া মাদরাসা",
                        updatedAtMillis = System.currentTimeMillis()
                    )
                )
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun initializeChartOfAccounts(orgId: String = DEFAULT_ORG_ID): Result<Unit> {
        return try {
            val defaults = listOf(
                Triple("1001", "নগদ তহবিল (Cash)", "ASSET"),
                Triple("1002", "ব্যাংক হিসাব (Bank)", "ASSET"),
                Triple("4001", "বেতন ও ফি আদায় আয়", "INCOME"),
                Triple("4002", "অনুদান ও সাদাকাহ আয়", "INCOME"),
                Triple("5001", "শিক্ষক ও স্টাফ বেতন ব্যয়", "EXPENSE"),
                Triple("5002", "মাদরাসা পরিচালনা ও লিল্লাহ বোর্ডিং ব্যয়", "EXPENSE")
            )
            for ((code, nameBn, type) in defaults) {
                val accId = "acc_$code"
                if (dao.getAccountOnce(accId) == null) {
                    dao.upsertAccount(
                        LedgerAccountEntity(
                            id = accId,
                            orgId = orgId,
                            code = code,
                            nameBn = nameBn,
                            accountType = type,
                            balance = 0.0
                        )
                    )
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrganizationSettings(
        nameBn: String,
        academicYear: String,
        address: String,
        phone: String,
        orgId: String = DEFAULT_ORG_ID
    ): Result<Unit> {
        return try {
            val current = dao.getOrganizationOnce(orgId)
            dao.upsertOrganization(
                OrganizationEntity(
                    id = orgId,
                    nameBn = nameBn.trim(),
                    establishedYear = current?.establishedYear ?: "১৯৯৫",
                    currencySymbol = current?.currencySymbol ?: "৳",
                    timezone = current?.timezone ?: "Asia/Dhaka",
                    academicYear = academicYear.trim(),
                    address = address.trim(),
                    phone = phone.trim()
                )
            )
            logAudit("সেটিংস হালনাগাদ", "মাদরাসার তথ্য ও শিক্ষাবর্ষ ($academicYear) হালনাগাদ করা হয়েছে।", orgId)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveStudent(student: Student, existingId: String? = null, orgId: String = DEFAULT_ORG_ID): Result<Unit> {
        return try {
            val id = existingId?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
            dao.upsertStudent(
                StudentEntity(
                    id = id,
                    orgId = orgId,
                    studentCode = student.studentCode,
                    fullName = student.fullName,
                    photoUri = student.photoUri,
                    dateOfBirth = student.dateOfBirth,
                    gender = student.gender,
                    fatherName = student.fatherName,
                    motherName = student.motherName,
                    guardianPhone = student.guardianPhone,
                    guardianId = student.guardianUid,
                    address = student.address,
                    admissionDate = student.admissionDate,
                    department = student.department,
                    className = student.className,
                    rollNumber = student.rollNumber,
                    previousInstitution = student.previousInstitution,
                    emergencyContact = student.emergencyContact,
                    archived = student.archived
                )
            )
            val action = if (existingId.isNullOrBlank()) "নতুন ভর্তি" else "শিক্ষার্থী সম্পাদনা"
            val summary = if (existingId.isNullOrBlank()) {
                "${student.fullName} (${student.studentCode}) ভর্তি করা হয়েছে।"
            } else {
                "${student.fullName}-এর তথ্য হালনাগাদ করা হয়েছে।"
            }
            logAudit(action, summary, orgId)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun archiveOrDeleteStudent(studentId: String, studentName: String, archiveOnly: Boolean, orgId: String = DEFAULT_ORG_ID): Result<Unit> {
        return try {
            if (archiveOnly) {
                dao.archiveStudent(studentId)
                logAudit("শিক্ষার্থী আর্কাইভ", "$studentName-কে আর্কাইভ করা হয়েছে।", orgId)
            } else {
                dao.deleteStudent(studentId)
                logAudit("শিক্ষার্থী মুছে ফেলা", "$studentName-এর রেকর্ড মুছে ফেলা হয়েছে।", orgId)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveTeacher(teacher: Teacher, existingId: String? = null, orgId: String = DEFAULT_ORG_ID): Result<Unit> {
        return try {
            val id = existingId?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
            dao.upsertTeacher(
                TeacherEntity(
                    id = id,
                    orgId = orgId,
                    fullName = teacher.fullName,
                    photoUri = teacher.photoUri,
                    phone = teacher.phone,
                    address = teacher.address,
                    joiningDate = teacher.joiningDate,
                    designation = teacher.designation,
                    qualification = teacher.qualification,
                    monthlySalary = teacher.monthlySalary,
                    department = teacher.department,
                    assignedClassesCsv = teacher.assignedClasses.joinToString(","),
                    email = teacher.loginEmail,
                    active = teacher.active
                )
            )
            val action = if (existingId.isNullOrBlank()) "নতুন শিক্ষক" else "শিক্ষক হালনাগাদ"
            val summary = if (existingId.isNullOrBlank()) {
                "শিক্ষক ${teacher.fullName} যোগ করা হয়েছে।"
            } else {
                "শিক্ষক ${teacher.fullName}-এর তথ্য হালনাগাদ হয়েছে।"
            }
            logAudit(action, summary, orgId)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun payTeacherSalary(teacher: Teacher, paymentMethod: String, orgId: String = DEFAULT_ORG_ID): Result<Unit> {
        return postJournalEntry(
            entryDate = BengaliFormat.todayDateKey(),
            referenceNo = "SAL-${System.currentTimeMillis().toString().takeLast(6)}",
            description = "শিক্ষক বেতন প্রদান: ${teacher.fullName} (${teacher.designation})",
            category = "SALARY_EXPENSE",
            debitAccountCode = "5001",
            debitAccountName = "শিক্ষক ও স্টাফ বেতন ব্যয়",
            creditAccountCode = if (paymentMethod.contains("ব্যাংক")) "1002" else "1001",
            creditAccountName = if (paymentMethod.contains("ব্যাংক")) "ব্যাংক হিসাব (Bank)" else "নগদ তহবিল (Cash)",
            amount = teacher.monthlySalary,
            orgId = orgId
        )
    }

    suspend fun saveGuardian(guardian: Guardian, existingId: String? = null, orgId: String = DEFAULT_ORG_ID): Result<Unit> {
        return try {
            val id = existingId?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
            dao.upsertGuardian(
                GuardianEntity(
                    id = id,
                    orgId = orgId,
                    fullName = guardian.fullName,
                    phone = guardian.phone,
                    email = guardian.email,
                    address = guardian.address,
                    relation = guardian.relation,
                    linkedStudentIdsCsv = guardian.linkedStudentIds.joinToString(",")
                )
            )
            if (existingId.isNullOrBlank()) {
                logAudit("অভিভাবক নিবন্ধন", "অভিভাবক ${guardian.fullName} নিবন্ধিত হয়েছেন।", orgId)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isNetworkAvailable(): Boolean {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun recordAttendance(
        dateKey: String,
        personType: String,
        personId: String,
        personName: String,
        department: String,
        className: String,
        status: AttendanceStatus,
        lateArrival: Boolean,
        auditNote: String,
        forceOfflineQueue: Boolean = false,
        orgId: String = DEFAULT_ORG_ID
    ): Result<Boolean> {
        return try {
            val compositeKey = "${orgId}_${dateKey}_${personType}_${personId}".replace("[^a-zA-Z0-9_\\-]".toRegex(), "_").take(128)
            val syncEventId = UUID.randomUUID().toString()

            val localEntity = OfflineAttendanceEntity(
                compositeKey = compositeKey,
                orgId = orgId,
                dateKey = dateKey,
                personType = personType,
                personId = personId,
                personName = personName,
                department = department,
                className = className,
                status = status.name,
                lateArrival = lateArrival,
                auditNote = auditNote,
                syncEventId = syncEventId,
                readerUidsCsv = "",
                syncStatus = "PENDING"
            )
            offlineDao.upsert(localEntity)

            if (!forceOfflineQueue && isNetworkAvailable()) {
                dao.upsertAttendance(
                    AttendanceEntity(
                        id = compositeKey,
                        orgId = orgId,
                        dateKey = dateKey,
                        personType = personType,
                        personId = personId,
                        personName = personName,
                        department = department,
                        className = className,
                        status = status.name,
                        lateArrival = lateArrival,
                        auditNote = auditNote.ifBlank { null },
                        syncEventId = syncEventId
                    )
                )
                val saved = offlineDao.getPendingSyncList().firstOrNull { it.compositeKey == compositeKey }
                if (saved != null) {
                    offlineDao.updateSyncStatus(saved.localId, "SYNCED", null)
                }
                Result.success(true)
            } else {
                AttendanceSyncWorker.enqueue(appContext)
                Result.success(false)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AttendanceSyncWorker.enqueue(appContext)
            Result.failure(e)
        }
    }

    fun triggerOfflineSyncNow() {
        AttendanceSyncWorker.enqueue(appContext)
    }

    suspend fun saveQuranProgress(progress: QuranProgress, existingId: String? = null, orgId: String = DEFAULT_ORG_ID): Result<Unit> {
        return try {
            val id = existingId?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
            dao.upsertQuranProgress(
                QuranProgressEntity(
                    id = id,
                    orgId = orgId,
                    studentId = progress.studentId,
                    studentName = progress.studentName,
                    department = progress.department,
                    dateKey = progress.dateKey,
                    lessonType = progress.lessonType,
                    surahName = progress.surahName,
                    paraNumber = progress.paraNumber,
                    ayatRange = progress.ayatRange,
                    arabicSample = progress.arabicSample,
                    accuracyPercent = progress.accuracyPercent,
                    mistakeCount = progress.mistakeCount,
                    weakLesson = progress.weakLesson,
                    approved = progress.approved,
                    teacherComment = progress.teacherComment
                )
            )
            if (existingId.isNullOrBlank()) {
                logAudit("কুরআন পাঠ অগ্রগতি", "${progress.studentName} (${progress.lessonType}): ${progress.surahName}", orgId)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createExam(exam: Exam, orgId: String = DEFAULT_ORG_ID): Result<Unit> {
        return try {
            dao.upsertExam(
                ExamEntity(
                    id = UUID.randomUUID().toString(),
                    orgId = orgId,
                    title = exam.title,
                    department = exam.department,
                    className = exam.className,
                    subject = exam.subject,
                    totalMarks = exam.totalMarks,
                    examDate = exam.examDate,
                    published = exam.published
                )
            )
            logAudit("নতুন পরীক্ষা", "${exam.title} (${exam.subject}) তৈরি করা হয়েছে।", orgId)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleExamPublish(examId: String, published: Boolean): Result<Unit> {
        return try {
            dao.setExamPublished(examId, published)
            dao.setExamResultsPublished(examId, published)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveExamResult(result: ExamResult, orgId: String = DEFAULT_ORG_ID): Result<Unit> {
        return try {
            dao.upsertExamResult(
                ExamResultEntity(
                    id = UUID.randomUUID().toString(),
                    orgId = orgId,
                    examId = result.examId,
                    examTitle = result.examTitle,
                    studentId = result.studentId,
                    studentName = result.studentName,
                    department = result.department,
                    className = result.className,
                    subject = result.subject,
                    totalMarks = result.totalMarks,
                    obtainedMarks = result.obtainedMarks,
                    grade = result.grade,
                    position = result.position,
                    published = result.published
                )
            )
            logAudit("ফলাফল এন্ট্রি", "${result.studentName} - ${result.examTitle}: ${result.obtainedMarks}/${result.totalMarks}", orgId)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createInvoice(invoice: Invoice, orgId: String = DEFAULT_ORG_ID): Result<Unit> {
        return try {
            dao.upsertInvoice(
                InvoiceEntity(
                    id = UUID.randomUUID().toString(),
                    orgId = orgId,
                    invoiceNumber = invoice.invoiceNumber,
                    studentId = invoice.studentId,
                    studentName = invoice.studentName,
                    department = invoice.department,
                    feeType = invoice.feeType,
                    billingMonth = invoice.billingMonth,
                    totalAmount = invoice.totalAmount,
                    paidAmount = invoice.paidAmount,
                    dueAmount = invoice.dueAmount,
                    status = invoice.status
                )
            )
            logAudit("বেতন চালান তৈরি", "${invoice.studentName}-এর নামে ${BengaliFormat.formatTaka(invoice.totalAmount)} চালান তৈরি।", orgId)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitPayment(
        invoice: Invoice,
        amount: Double,
        gateway: PaymentGateway,
        transactionId: String,
        sandboxMode: Boolean,
        note: String,
        orgId: String = DEFAULT_ORG_ID
    ): Result<Unit> {
        return try {
            val isCash = gateway == PaymentGateway.CASH
            val status = if (isCash) PaymentStatus.VERIFIED else PaymentStatus.PENDING
            val receipt = "REC-${System.currentTimeMillis().toString().takeLast(6)}"
            val payId = UUID.randomUUID().toString()

            dao.upsertPayment(
                PaymentEntity(
                    id = payId,
                    orgId = orgId,
                    receiptNumber = receipt,
                    invoiceId = invoice.id,
                    studentId = invoice.studentId,
                    studentName = invoice.studentName,
                    amount = amount,
                    gateway = gateway.name,
                    transactionId = transactionId.ifBlank { "TRX-${System.currentTimeMillis().toString().takeLast(8)}" },
                    status = status.name,
                    sandboxMode = sandboxMode,
                    webhookVerified = isCash,
                    note = note.ifBlank { null }
                )
            )

            if (status == PaymentStatus.VERIFIED) {
                applyVerifiedPaymentToInvoiceAndLedger(invoice, amount, gateway, receipt, orgId)
            } else {
                logAudit("অনলাইন পেমেন্ট শুরু", "${invoice.studentName} (${gateway.labelBn}) - যাচাই অপেক্ষমাণ", orgId)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyPendingPayment(payment: Payment, invoice: Invoice?, approve: Boolean, orgId: String = DEFAULT_ORG_ID): Result<Unit> {
        return try {
            val existing = dao.getPaymentOnce(payment.id) ?: return Result.failure(IllegalStateException("পেমেন্ট পাওয়া যায়নি।"))
            val newStatus = if (approve) PaymentStatus.VERIFIED else PaymentStatus.FAILED
            dao.upsertPayment(
                existing.copy(
                    status = newStatus.name,
                    webhookVerified = approve,
                    note = if (approve) "অ্যাডমিন যাচাই সম্পন্ন" else "পেমেন্ট যাচাই ব্যর্থ হয়েছে",
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
            if (approve && invoice != null) {
                applyVerifiedPaymentToInvoiceAndLedger(invoice, payment.amount, payment.gatewayEnum, payment.receiptNumber, orgId)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refundVerifiedPayment(payment: Payment, orgId: String = DEFAULT_ORG_ID): Result<Unit> {
        return try {
            val existing = dao.getPaymentOnce(payment.id) ?: return Result.failure(IllegalStateException("পেমেন্ট পাওয়া যায়নি।"))
            dao.upsertPayment(
                existing.copy(
                    status = PaymentStatus.REFUNDED.name,
                    note = "রিফান্ড প্রক্রিয়া সম্পন্ন হয়েছে",
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
            postJournalEntry(
                entryDate = BengaliFormat.todayDateKey(),
                referenceNo = "RFD-${payment.receiptNumber}",
                description = "পেমেন্ট রিফান্ড: ${payment.studentName} (${payment.receiptNumber})",
                category = "REFUND",
                debitAccountCode = "4001",
                debitAccountName = "বেতন ও ফি আদায় আয়",
                creditAccountCode = if (payment.gatewayEnum == PaymentGateway.CASH) "1001" else "1002",
                creditAccountName = if (payment.gatewayEnum == PaymentGateway.CASH) "নগদ তহবিল (Cash)" else "ব্যাংক হিসাব (Bank)",
                amount = payment.amount,
                orgId = orgId
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun applyVerifiedPaymentToInvoiceAndLedger(
        invoice: Invoice,
        amount: Double,
        gateway: PaymentGateway,
        receipt: String,
        orgId: String
    ) {
        val newPaid = (invoice.paidAmount + amount).coerceAtMost(invoice.totalAmount)
        val newDue = (invoice.totalAmount - newPaid).coerceAtLeast(0.0)
        val newStatus = when {
            newDue <= 0.0 -> "PAID"
            newPaid > 0.0 -> "PARTIAL"
            else -> "UNPAID"
        }
        dao.upsertInvoice(
            InvoiceEntity(
                id = invoice.id,
                orgId = orgId,
                invoiceNumber = invoice.invoiceNumber,
                studentId = invoice.studentId,
                studentName = invoice.studentName,
                department = invoice.department,
                feeType = invoice.feeType,
                billingMonth = invoice.billingMonth,
                totalAmount = invoice.totalAmount,
                paidAmount = newPaid,
                dueAmount = newDue,
                status = newStatus
            )
        )

        val debitCode = if (gateway == PaymentGateway.CASH) "1001" else "1002"
        val debitName = if (gateway == PaymentGateway.CASH) "নগদ তহবিল (Cash)" else "ব্যাংক হিসাব (Bank)"
        postJournalEntry(
            entryDate = BengaliFormat.todayDateKey(),
            referenceNo = receipt,
            description = "বেতন আদায় (${invoice.feeTypeEnum.labelBn}): ${invoice.studentName}",
            category = "FEE_INCOME",
            debitAccountCode = debitCode,
            debitAccountName = debitName,
            creditAccountCode = "4001",
            creditAccountName = "বেতন ও ফি আদায় আয়",
            amount = amount,
            orgId = orgId
        )
    }

    suspend fun postJournalEntry(
        entryDate: String,
        referenceNo: String,
        description: String,
        category: String,
        debitAccountCode: String,
        debitAccountName: String,
        creditAccountCode: String,
        creditAccountName: String,
        amount: Double,
        entryType: String = "STANDARD",
        reversesEntryId: String? = null,
        orgId: String = DEFAULT_ORG_ID
    ): Result<Unit> {
        return try {
            require(amount > 0.0) { "টাকার পরিমাণ শূন্যের চেয়ে বেশি হতে হবে।" }
            require(debitAccountCode != creditAccountCode) { "ডেবিট ও ক্রেডিট খাত একই হতে পারবে না।" }

            dao.insertJournalEntry(
                JournalEntryEntity(
                    id = UUID.randomUUID().toString(),
                    orgId = orgId,
                    entryDate = entryDate,
                    referenceNo = referenceNo,
                    description = description,
                    category = category,
                    debitAccountCode = debitAccountCode,
                    debitAccountName = debitAccountName,
                    creditAccountCode = creditAccountCode,
                    creditAccountName = creditAccountName,
                    amount = amount,
                    totalDebit = amount,
                    totalCredit = amount,
                    status = "POSTED",
                    entryType = entryType,
                    reversesEntryId = reversesEntryId
                )
            )
            updateAccountBalanceDelta(debitAccountCode, debitAccountName, amount, isDebit = true, orgId = orgId)
            updateAccountBalanceDelta(creditAccountCode, creditAccountName, amount, isDebit = false, orgId = orgId)
            logAudit("হিসাব দাখিলা", "$description (${BengaliFormat.formatTaka(amount)})", orgId)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reverseJournalEntry(original: JournalEntry, reason: String, orgId: String = DEFAULT_ORG_ID): Result<Unit> {
        return postJournalEntry(
            entryDate = BengaliFormat.todayDateKey(),
            referenceNo = "REV-${original.referenceNo}",
            description = "বিপরীত দাখিলা ($reason): ${original.description}",
            category = "REVERSAL",
            debitAccountCode = original.creditAccountCode,
            debitAccountName = original.creditAccountName,
            creditAccountCode = original.debitAccountCode,
            creditAccountName = original.debitAccountName,
            amount = original.amount,
            entryType = "REVERSAL",
            reversesEntryId = original.id,
            orgId = orgId
        )
    }

    private suspend fun updateAccountBalanceDelta(
        code: String,
        nameBn: String,
        amount: Double,
        isDebit: Boolean,
        orgId: String
    ) {
        val accId = "acc_$code"
        val existing = dao.getAccountOnce(accId)
        val acctType = when {
            code.startsWith("1") -> "ASSET"
            code.startsWith("2") -> "LIABILITY"
            code.startsWith("3") -> "EQUITY"
            code.startsWith("4") -> "INCOME"
            else -> "EXPENSE"
        }
        val delta = when (acctType) {
            "ASSET", "EXPENSE" -> if (isDebit) amount else -amount
            else -> if (isDebit) -amount else amount
        }
        val newBal = (existing?.balance ?: 0.0) + delta
        dao.upsertAccount(
            LedgerAccountEntity(
                id = accId,
                orgId = orgId,
                code = code,
                nameBn = existing?.nameBn ?: nameBn,
                accountType = existing?.accountType ?: acctType,
                balance = newBal
            )
        )
    }

    suspend fun saveDonation(
        donorName: String,
        phone: String,
        amount: Double,
        purpose: String,
        paymentMethod: String,
        orgId: String = DEFAULT_ORG_ID
    ): Result<Unit> {
        return try {
            val receipt = "DON-${System.currentTimeMillis().toString().takeLast(6)}"
            val dateKey = BengaliFormat.todayDateKey()
            dao.upsertDonation(
                DonationEntity(
                    id = UUID.randomUUID().toString(),
                    orgId = orgId,
                    receiptNumber = receipt,
                    donorName = donorName,
                    phone = phone,
                    amount = amount,
                    donationDate = dateKey,
                    purpose = purpose,
                    paymentMethod = paymentMethod
                )
            )
            val debitCode = if (paymentMethod.contains("ব্যাংক") || paymentMethod.contains("বিকাশ") || paymentMethod.contains("নগদ অনলাইন")) "1002" else "1001"
            val debitName = if (debitCode == "1002") "ব্যাংক হিসাব (Bank)" else "নগদ তহবিল (Cash)"
            postJournalEntry(
                entryDate = dateKey,
                referenceNo = receipt,
                description = "অনুদান গ্রহণ ($purpose): $donorName",
                category = "DONATION_INCOME",
                debitAccountCode = debitCode,
                debitAccountName = debitName,
                creditAccountCode = "4002",
                creditAccountName = "অনুদান ও সাদাকাহ আয়",
                amount = amount,
                orgId = orgId
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun publishNotice(
        title: String,
        body: String,
        targetAudience: String,
        important: Boolean,
        smsEnabled: Boolean,
        orgId: String = DEFAULT_ORG_ID
    ): Result<Unit> {
        return try {
            dao.upsertNotice(
                NoticeEntity(
                    id = UUID.randomUUID().toString(),
                    orgId = orgId,
                    title = title,
                    body = body,
                    targetAudience = targetAudience,
                    important = important,
                    smsDispatched = smsEnabled,
                    publishedDate = BengaliFormat.todayDateKey()
                )
            )
            logAudit("নোটিশ প্রকাশ", "নতুন নোটিশ প্রকাশিত হয়েছে: $title", orgId)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun logAudit(action: String, summary: String, orgId: String = DEFAULT_ORG_ID) {
        try {
            dao.insertAuditLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    orgId = orgId,
                    actorName = "অ্যাডমিন",
                    action = action,
                    summary = summary
                )
            )
        } catch (_: Exception) {
        }
    }
}

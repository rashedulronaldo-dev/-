package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AttendanceRecord
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
import com.example.data.model.QuranProgress
import com.example.data.model.Payment
import com.example.data.model.Student
import com.example.data.model.Teacher

@Entity(tableName = "organizations")
data class OrganizationEntity(
    @PrimaryKey val id: String,
    val nameBn: String,
    val establishedYear: String,
    val currencySymbol: String,
    val timezone: String,
    val academicYear: String,
    val address: String,
    val phone: String,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomain() = Organization(
        id = id,
        nameBn = nameBn,
        establishedYear = establishedYear,
        currencySymbol = currencySymbol,
        timezone = timezone,
        academicYear = academicYear,
        address = address,
        phone = phone
    )
}

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val studentCode: String,
    val fullName: String,
    val photoUri: String?,
    val dateOfBirth: String,
    val gender: String,
    val fatherName: String,
    val motherName: String,
    val guardianPhone: String,
    val guardianId: String?,
    val address: String,
    val admissionDate: String,
    val department: String,
    val className: String,
    val rollNumber: String,
    val previousInstitution: String?,
    val emergencyContact: String,
    val archived: Boolean,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomain() = Student(
        id = id,
        orgId = orgId,
        studentCode = studentCode,
        fullName = fullName,
        photoUri = photoUri,
        dateOfBirth = dateOfBirth,
        gender = gender,
        fatherName = fatherName,
        motherName = motherName,
        guardianPhone = guardianPhone,
        guardianUid = guardianId,
        address = address,
        admissionDate = admissionDate,
        department = department,
        className = className,
        rollNumber = rollNumber,
        previousInstitution = previousInstitution,
        emergencyContact = emergencyContact,
        archived = archived
    )
}

@Entity(tableName = "teachers")
data class TeacherEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val fullName: String,
    val photoUri: String?,
    val phone: String,
    val address: String,
    val joiningDate: String,
    val designation: String,
    val qualification: String,
    val monthlySalary: Double,
    val department: String,
    val assignedClassesCsv: String,
    val email: String,
    val active: Boolean,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomain() = Teacher(
        id = id,
        orgId = orgId,
        fullName = fullName,
        photoUri = photoUri,
        phone = phone,
        address = address,
        joiningDate = joiningDate,
        designation = designation,
        qualification = qualification,
        monthlySalary = monthlySalary,
        department = department,
        assignedClasses = assignedClassesCsv.split(",").map { it.trim() }.filter { it.isNotBlank() },
        loginEmail = email,
        active = active
    )
}

@Entity(tableName = "guardians")
data class GuardianEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val fullName: String,
    val phone: String,
    val email: String?,
    val address: String,
    val relation: String,
    val linkedStudentIdsCsv: String,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomain() = Guardian(
        id = id,
        orgId = orgId,
        fullName = fullName,
        phone = phone,
        email = email,
        address = address,
        relation = relation,
        linkedStudentIds = linkedStudentIdsCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
    )
}

@Entity(tableName = "attendance_records")
data class AttendanceEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val dateKey: String,
    val personType: String,
    val personId: String,
    val personName: String,
    val department: String,
    val className: String,
    val status: String,
    val lateArrival: Boolean,
    val auditNote: String?,
    val syncEventId: String,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomain() = AttendanceRecord(
        id = id,
        orgId = orgId,
        dateKey = dateKey,
        personType = personType,
        personId = personId,
        personName = personName,
        department = department,
        className = className,
        status = status,
        lateArrival = lateArrival,
        auditNote = auditNote,
        syncEventId = syncEventId
    )
}

@Entity(tableName = "quran_progress")
data class QuranProgressEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val studentId: String,
    val studentName: String,
    val department: String,
    val dateKey: String,
    val lessonType: String,
    val surahName: String,
    val paraNumber: Int,
    val ayatRange: String,
    val arabicSample: String?,
    val accuracyPercent: Int,
    val mistakeCount: Int,
    val weakLesson: Boolean,
    val approved: Boolean,
    val teacherComment: String?,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomain() = QuranProgress(
        id = id,
        orgId = orgId,
        studentId = studentId,
        studentName = studentName,
        department = department,
        dateKey = dateKey,
        lessonType = lessonType,
        surahName = surahName,
        paraNumber = paraNumber,
        ayatRange = ayatRange,
        arabicSample = arabicSample,
        accuracyPercent = accuracyPercent,
        mistakeCount = mistakeCount,
        weakLesson = weakLesson,
        approved = approved,
        teacherComment = teacherComment
    )
}

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val title: String,
    val department: String,
    val className: String,
    val subject: String,
    val totalMarks: Int,
    val examDate: String,
    val published: Boolean,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomain() = Exam(
        id = id,
        orgId = orgId,
        title = title,
        department = department,
        className = className,
        subject = subject,
        totalMarks = totalMarks,
        examDate = examDate,
        published = published
    )
}

@Entity(tableName = "exam_results")
data class ExamResultEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val examId: String,
    val examTitle: String,
    val studentId: String,
    val studentName: String,
    val department: String,
    val className: String,
    val subject: String,
    val totalMarks: Int,
    val obtainedMarks: Int,
    val grade: String,
    val position: Int,
    val published: Boolean,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomain() = ExamResult(
        id = id,
        orgId = orgId,
        examId = examId,
        examTitle = examTitle,
        studentId = studentId,
        studentName = studentName,
        department = department,
        className = className,
        subject = subject,
        totalMarks = totalMarks,
        obtainedMarks = obtainedMarks,
        grade = grade,
        position = position,
        published = published
    )
}

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val invoiceNumber: String,
    val studentId: String,
    val studentName: String,
    val department: String,
    val feeType: String,
    val billingMonth: String,
    val totalAmount: Double,
    val paidAmount: Double,
    val dueAmount: Double,
    val status: String,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomain() = Invoice(
        id = id,
        orgId = orgId,
        invoiceNumber = invoiceNumber,
        studentId = studentId,
        studentName = studentName,
        department = department,
        feeType = feeType,
        billingMonth = billingMonth,
        totalAmount = totalAmount,
        paidAmount = paidAmount,
        dueAmount = dueAmount,
        status = status
    )
}

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val receiptNumber: String,
    val invoiceId: String,
    val studentId: String,
    val studentName: String,
    val amount: Double,
    val gateway: String,
    val transactionId: String,
    val status: String,
    val sandboxMode: Boolean,
    val webhookVerified: Boolean,
    val note: String?,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomain() = Payment(
        id = id,
        orgId = orgId,
        receiptNumber = receiptNumber,
        invoiceId = invoiceId,
        studentId = studentId,
        studentName = studentName,
        amount = amount,
        gateway = gateway,
        transactionId = transactionId,
        status = status,
        sandboxMode = sandboxMode,
        webhookVerified = webhookVerified,
        note = note
    )
}

@Entity(tableName = "ledger_accounts")
data class LedgerAccountEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val code: String,
    val nameBn: String,
    val accountType: String,
    val balance: Double,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomain() = LedgerAccount(
        id = id,
        orgId = orgId,
        code = code,
        nameBn = nameBn,
        accountType = accountType,
        balance = balance
    )
}

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val entryDate: String,
    val referenceNo: String,
    val description: String,
    val category: String,
    val debitAccountCode: String,
    val debitAccountName: String,
    val creditAccountCode: String,
    val creditAccountName: String,
    val amount: Double,
    val totalDebit: Double,
    val totalCredit: Double,
    val status: String,
    val entryType: String,
    val reversesEntryId: String?,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomain() = JournalEntry(
        id = id,
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
        totalDebit = totalDebit,
        totalCredit = totalCredit,
        status = status,
        entryType = entryType,
        reversesEntryId = reversesEntryId
    )
}

@Entity(tableName = "donations")
data class DonationEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val receiptNumber: String,
    val donorName: String,
    val phone: String,
    val amount: Double,
    val donationDate: String,
    val purpose: String,
    val paymentMethod: String,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomain() = Donation(
        id = id,
        orgId = orgId,
        receiptNumber = receiptNumber,
        donorName = donorName,
        phone = phone,
        amount = amount,
        donationDate = donationDate,
        purpose = purpose,
        paymentMethod = paymentMethod
    )
}

@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val title: String,
    val body: String,
    val targetAudience: String,
    val important: Boolean,
    val smsDispatched: Boolean,
    val publishedDate: String,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomain() = Notice(
        id = id,
        orgId = orgId,
        title = title,
        body = body,
        targetAudience = targetAudience,
        important = important,
        smsDispatched = smsDispatched,
        publishedDate = publishedDate
    )
}

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val actorName: String,
    val action: String,
    val summary: String,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomain() = AuditLog(
        id = id,
        orgId = orgId,
        actorUid = "local_admin",
        actorName = actorName,
        action = action,
        summary = summary
    )
}

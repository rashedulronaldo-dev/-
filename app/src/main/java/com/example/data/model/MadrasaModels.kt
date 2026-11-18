package com.example.data.model

import androidx.annotation.Keep

enum class UserRole(val labelBn: String) {
    ADMIN("অ্যাডমিন"),
    TEACHER("শিক্ষক"),
    PARENT("অভিভাবক");

    companion object {
        fun fromRaw(raw: String?): UserRole =
            entries.firstOrNull { it.name == raw } ?: PARENT
    }
}

enum class Department(val labelBn: String, val classes: List<String>) {
    NOORANI("নূরানী বিভাগ", listOf("নূরানী-শিশু", "নূরানী-১ম", "নূরানী-২য়", "নূরানী-৩য়")),
    NAZERA("নাজেরা বিভাগ", listOf("নাজেরা-প্রাথমিক", "নাজেরা-উচ্চ")),
    HIFZ("হিফজ বিভাগ", listOf("হিফজ-প্রাথমিক", "হিফজ-মধ্যম", "হিফজ-তাকমীল"));

    companion object {
        fun fromRaw(raw: String?): Department =
            entries.firstOrNull { it.name == raw } ?: NOORANI
    }
}

enum class AttendanceStatus(val labelBn: String) {
    PRESENT("উপস্থিত"),
    ABSENT("অনুপস্থিত"),
    LEAVE("ছুটি"),
    SICK("অসুস্থ");

    companion object {
        fun fromRaw(raw: String?): AttendanceStatus =
            entries.firstOrNull { it.name == raw } ?: PRESENT
    }
}

enum class FeeType(val labelBn: String) {
    MONTHLY_TUITION("মাসিক বেতন"),
    ADMISSION("ভর্তি ফি"),
    EXAM("পরীক্ষার ফি"),
    RESIDENTIAL("আবাসিক ফি"),
    OTHER("অন্যান্য ফি");

    companion object {
        fun fromRaw(raw: String?): FeeType =
            entries.firstOrNull { it.name == raw } ?: MONTHLY_TUITION
    }
}

enum class PaymentGateway(val labelBn: String) {
    CASH("নগদ ক্যাশ"),
    BKASH("বিকাশ (bKash)"),
    NAGAD("নগদ (Nagad)"),
    SSLCOMMERZ("এসএসএলকমার্জ (SSLCommerz)");

    companion object {
        fun fromRaw(raw: String?): PaymentGateway =
            entries.firstOrNull { it.name == raw } ?: CASH
    }
}

enum class PaymentStatus(val labelBn: String) {
    PENDING("যাচাই অপেক্ষমাণ"),
    VERIFIED("যাচাইকৃত ও পরিশোধিত"),
    FAILED("ব্যর্থ হয়েছে"),
    REFUNDED("রিফান্ড সম্পন্ন");

    companion object {
        fun fromRaw(raw: String?): PaymentStatus =
            entries.firstOrNull { it.name == raw } ?: PENDING
    }
}

enum class NoticeAudience(val labelBn: String) {
    ALL("সকলের জন্য"),
    TEACHER("শিক্ষকমণ্ডলী"),
    PARENT("অভিভাবকবৃন্দ"),
    STUDENT("শিক্ষার্থীবৃন্দ");

    companion object {
        fun fromRaw(raw: String?): NoticeAudience =
            entries.firstOrNull { it.name == raw } ?: ALL
    }
}

@Keep
data class Organization(
    val id: String = "",
    val nameBn: String = "খেলার মাঠ নূরানী তালিমুল কুরআন ও হাফিজিয়া মাদরাসা",
    val establishedYear: String = "১৯৯৫",
    val currencySymbol: String = "৳",
    val timezone: String = "Asia/Dhaka",
    val academicYear: String = "২০২৬",
    val address: String = "খেলার মাঠ, ঢাকা",
    val phone: String = "০১৭০০-০০০০০০",
    val ownerUid: String = "",
    val readerUids: List<String> = emptyList()
)

@Keep
data class Member(
    val id: String = "",
    val uid: String = "",
    val orgId: String = "kmnqhm_1995",
    val email: String = "",
    val displayName: String = "",
    val role: String = UserRole.ADMIN.name,
    val assignedClasses: List<String> = emptyList(),
    val linkedStudentIds: List<String> = emptyList(),
    val active: Boolean = true
) {
    val roleEnum: UserRole get() = UserRole.fromRaw(role)
}

@Keep
data class Student(
    val id: String = "",
    val orgId: String = "kmnqhm_1995",
    val studentCode: String = "",
    val fullName: String = "",
    val photoUri: String? = null,
    val dateOfBirth: String = "",
    val gender: String = "ছাত্র",
    val fatherName: String = "",
    val motherName: String = "",
    val guardianPhone: String = "",
    val guardianUid: String? = null,
    val address: String = "",
    val admissionDate: String = "",
    val department: String = Department.NOORANI.name,
    val className: String = "",
    val rollNumber: String = "",
    val previousInstitution: String? = null,
    val emergencyContact: String = "",
    val archived: Boolean = false,
    val createdByUid: String = "",
    val readerUids: List<String> = emptyList()
) {
    val departmentEnum: Department get() = Department.fromRaw(department)
}

@Keep
data class Teacher(
    val id: String = "",
    val orgId: String = "kmnqhm_1995",
    val fullName: String = "",
    val photoUri: String? = null,
    val phone: String = "",
    val address: String = "",
    val joiningDate: String = "",
    val designation: String = "",
    val qualification: String = "",
    val monthlySalary: Double = 0.0,
    val department: String = Department.NOORANI.name,
    val assignedClasses: List<String> = emptyList(),
    val loginEmail: String = "",
    val teacherUid: String? = null,
    val active: Boolean = true,
    val createdByUid: String = "",
    val readerUids: List<String> = emptyList()
) {
    val departmentEnum: Department get() = Department.fromRaw(department)
}

@Keep
data class Guardian(
    val id: String = "",
    val orgId: String = "kmnqhm_1995",
    val fullName: String = "",
    val phone: String = "",
    val email: String? = null,
    val address: String = "",
    val relation: String = "পিতা",
    val guardianUid: String? = null,
    val linkedStudentIds: List<String> = emptyList(),
    val createdByUid: String = "",
    val readerUids: List<String> = emptyList()
)

@Keep
data class AttendanceRecord(
    val id: String = "",
    val orgId: String = "kmnqhm_1995",
    val dateKey: String = "",
    val personType: String = "STUDENT",
    val personId: String = "",
    val personName: String = "",
    val department: String = Department.NOORANI.name,
    val className: String = "",
    val status: String = AttendanceStatus.PRESENT.name,
    val lateArrival: Boolean = false,
    val auditNote: String? = null,
    val syncEventId: String = "",
    val createdByUid: String = "",
    val readerUids: List<String> = emptyList()
) {
    val statusEnum: AttendanceStatus get() = AttendanceStatus.fromRaw(status)
}

@Keep
data class QuranProgress(
    val id: String = "",
    val orgId: String = "kmnqhm_1995",
    val studentId: String = "",
    val studentName: String = "",
    val department: String = Department.HIFZ.name,
    val dateKey: String = "",
    val lessonType: String = "সবক",
    val surahName: String = "",
    val paraNumber: Int = 1,
    val ayatRange: String = "",
    val arabicSample: String? = null,
    val accuracyPercent: Int = 100,
    val mistakeCount: Int = 0,
    val weakLesson: Boolean = false,
    val approved: Boolean = true,
    val teacherComment: String? = null,
    val createdByUid: String = "",
    val readerUids: List<String> = emptyList()
)

@Keep
data class Exam(
    val id: String = "",
    val orgId: String = "kmnqhm_1995",
    val title: String = "",
    val department: String = Department.NOORANI.name,
    val className: String = "",
    val subject: String = "",
    val totalMarks: Int = 100,
    val examDate: String = "",
    val published: Boolean = false,
    val createdByUid: String = "",
    val readerUids: List<String> = emptyList()
)

@Keep
data class ExamResult(
    val id: String = "",
    val orgId: String = "kmnqhm_1995",
    val examId: String = "",
    val examTitle: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val department: String = Department.NOORANI.name,
    val className: String = "",
    val subject: String = "",
    val totalMarks: Int = 100,
    val obtainedMarks: Int = 0,
    val grade: String = "A+",
    val position: Int = 1,
    val published: Boolean = false,
    val createdByUid: String = "",
    val readerUids: List<String> = emptyList()
)

@Keep
data class Invoice(
    val id: String = "",
    val orgId: String = "kmnqhm_1995",
    val invoiceNumber: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val department: String = Department.NOORANI.name,
    val feeType: String = FeeType.MONTHLY_TUITION.name,
    val billingMonth: String = "",
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val dueAmount: Double = 0.0,
    val status: String = "UNPAID",
    val createdByUid: String = "",
    val readerUids: List<String> = emptyList()
) {
    val feeTypeEnum: FeeType get() = FeeType.fromRaw(feeType)
}

@Keep
data class Payment(
    val id: String = "",
    val orgId: String = "kmnqhm_1995",
    val receiptNumber: String = "",
    val invoiceId: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val amount: Double = 0.0,
    val gateway: String = PaymentGateway.CASH.name,
    val transactionId: String = "",
    val status: String = PaymentStatus.PENDING.name,
    val sandboxMode: Boolean = true,
    val webhookVerified: Boolean = false,
    val note: String? = null,
    val createdByUid: String = "",
    val readerUids: List<String> = emptyList()
) {
    val gatewayEnum: PaymentGateway get() = PaymentGateway.fromRaw(gateway)
    val statusEnum: PaymentStatus get() = PaymentStatus.fromRaw(status)
}

@Keep
data class LedgerAccount(
    val id: String = "",
    val orgId: String = "kmnqhm_1995",
    val code: String = "",
    val nameBn: String = "",
    val accountType: String = "ASSET",
    val balance: Double = 0.0,
    val createdByUid: String = "",
    val readerUids: List<String> = emptyList()
)

@Keep
data class JournalEntry(
    val id: String = "",
    val orgId: String = "kmnqhm_1995",
    val entryDate: String = "",
    val referenceNo: String = "",
    val description: String = "",
    val category: String = "",
    val debitAccountCode: String = "",
    val debitAccountName: String = "",
    val creditAccountCode: String = "",
    val creditAccountName: String = "",
    val amount: Double = 0.0,
    val totalDebit: Double = 0.0,
    val totalCredit: Double = 0.0,
    val status: String = "POSTED",
    val entryType: String = "STANDARD",
    val reversesEntryId: String? = null,
    val createdByUid: String = "",
    val readerUids: List<String> = emptyList()
)

@Keep
data class Donation(
    val id: String = "",
    val orgId: String = "kmnqhm_1995",
    val receiptNumber: String = "",
    val donorName: String = "",
    val phone: String = "",
    val amount: Double = 0.0,
    val donationDate: String = "",
    val purpose: String = "",
    val paymentMethod: String = "নগদ",
    val createdByUid: String = "",
    val readerUids: List<String> = emptyList()
)

@Keep
data class Notice(
    val id: String = "",
    val orgId: String = "kmnqhm_1995",
    val title: String = "",
    val body: String = "",
    val targetAudience: String = NoticeAudience.ALL.name,
    val important: Boolean = false,
    val smsDispatched: Boolean = false,
    val publishedDate: String = "",
    val createdByUid: String = "",
    val readerUids: List<String> = emptyList()
) {
    val audienceEnum: NoticeAudience get() = NoticeAudience.fromRaw(targetAudience)
}

@Keep
data class AuditLog(
    val id: String = "",
    val orgId: String = "kmnqhm_1995",
    val actorUid: String = "",
    val actorName: String = "",
    val action: String = "",
    val summary: String = "",
    val readerUids: List<String> = emptyList()
)

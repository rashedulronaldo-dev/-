package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.BengaliFormat
import com.example.core.UiState
import com.example.core.toBengaliErrorMessage
import com.example.data.local.AppPreferences
import com.example.data.local.AppSettingsData
import com.example.data.local.OfflineAttendanceEntity
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.AuditLog
import com.example.data.model.Department
import com.example.data.model.Donation
import com.example.data.model.Exam
import com.example.data.model.ExamResult
import com.example.data.model.Guardian
import com.example.data.model.Invoice
import com.example.data.model.JournalEntry
import com.example.data.model.LedgerAccount
import com.example.data.model.Member
import com.example.data.model.Notice
import com.example.data.model.Organization
import com.example.data.model.Payment
import com.example.data.model.PaymentGateway
import com.example.data.model.QuranProgress
import com.example.data.model.Student
import com.example.data.model.Teacher
import com.example.data.model.UserRole
import com.example.data.remote.DeferredSmsProviderAdapter
import com.example.data.remote.MadrasaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardMetrics(
    val totalStudents: Int = 0,
    val totalTeachers: Int = 0,
    val todayPresent: Int = 0,
    val todayAbsent: Int = 0,
    val totalFeeCollected: Double = 0.0,
    val totalFeeDue: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val newAdmissionsThisMonth: Int = 0,
    val totalDonations: Double = 0.0,
    val nooraniCount: Int = 0,
    val nazeraCount: Int = 0,
    val hifzCount: Int = 0,
    val cashBalance: Double = 0.0,
    val bankBalance: Double = 0.0
)

class MadrasaViewModel(
    private val repository: MadrasaRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    private val smsAdapter = DeferredSmsProviderAdapter()

    private val _snackbarMessages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val snackbarMessages = _snackbarMessages.asSharedFlow()

    private val _previewRoleOverride = MutableStateFlow<UserRole?>(null)
    val previewRoleOverride = _previewRoleOverride.asStateFlow()

    val currentMember: StateFlow<Member?> = MutableStateFlow(
        Member(
            id = "local_admin",
            uid = "local_admin",
            orgId = MadrasaRepository.DEFAULT_ORG_ID,
            email = "",
            displayName = "মাদরাসা অ্যাডমিন",
            role = UserRole.ADMIN.name,
            active = true
        )
    )

    val effectiveRole: StateFlow<UserRole> = _previewRoleOverride
        .map { it ?: UserRole.ADMIN }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UserRole.ADMIN)

    val settings: StateFlow<AppSettingsData> = preferences.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), AppSettingsData())

    val organization: StateFlow<Organization?> = repository.observeOrganization()
        .catch { emit(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val studentsState: StateFlow<UiState<List<Student>>> = repository.observeStudents().asUiStateFlow()
    val teachersState: StateFlow<UiState<List<Teacher>>> = repository.observeTeachers().asUiStateFlow()
    val guardiansState: StateFlow<UiState<List<Guardian>>> = repository.observeGuardians().asUiStateFlow()
    val attendanceState: StateFlow<UiState<List<AttendanceRecord>>> = repository.observeAttendance().asUiStateFlow()
    val quranProgressState: StateFlow<UiState<List<QuranProgress>>> = repository.observeQuranProgress().asUiStateFlow()
    val examsState: StateFlow<UiState<List<Exam>>> = repository.observeExams().asUiStateFlow()
    val examResultsState: StateFlow<UiState<List<ExamResult>>> = repository.observeExamResults().asUiStateFlow()
    val invoicesState: StateFlow<UiState<List<Invoice>>> = repository.observeInvoices().asUiStateFlow()
    val paymentsState: StateFlow<UiState<List<Payment>>> = repository.observePayments().asUiStateFlow()
    val accountsState: StateFlow<UiState<List<LedgerAccount>>> = repository.observeAccounts().asUiStateFlow()
    val journalState: StateFlow<UiState<List<JournalEntry>>> = repository.observeJournalEntries().asUiStateFlow()
    val donationsState: StateFlow<UiState<List<Donation>>> = repository.observeDonations().asUiStateFlow()
    val noticesState: StateFlow<UiState<List<Notice>>> = repository.observeNotices().asUiStateFlow()
    val auditLogsState: StateFlow<UiState<List<AuditLog>>> = repository.observeAuditLogs().asUiStateFlow()

    val offlineAttendanceQueue: StateFlow<List<OfflineAttendanceEntity>> =
        repository.observeOfflineAttendanceQueue()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val offlinePendingCount: StateFlow<Int> =
        repository.observeOfflinePendingCount()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0)

    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        studentsState,
        teachersState,
        attendanceState,
        invoicesState,
        journalState,
        donationsState,
        accountsState
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val students = (values[0] as? UiState.Success<List<Student>>)?.data.orEmpty().filter { !it.archived }
        @Suppress("UNCHECKED_CAST")
        val teachers = (values[1] as? UiState.Success<List<Teacher>>)?.data.orEmpty().filter { it.active }
        @Suppress("UNCHECKED_CAST")
        val attendance = (values[2] as? UiState.Success<List<AttendanceRecord>>)?.data.orEmpty()
        @Suppress("UNCHECKED_CAST")
        val invoices = (values[3] as? UiState.Success<List<Invoice>>)?.data.orEmpty()
        @Suppress("UNCHECKED_CAST")
        val journals = (values[4] as? UiState.Success<List<JournalEntry>>)?.data.orEmpty()
        @Suppress("UNCHECKED_CAST")
        val donations = (values[5] as? UiState.Success<List<Donation>>)?.data.orEmpty()
        @Suppress("UNCHECKED_CAST")
        val accounts = (values[6] as? UiState.Success<List<LedgerAccount>>)?.data.orEmpty()

        val today = BengaliFormat.todayDateKey()
        val monthPrefix = BengaliFormat.currentMonthKey()

        val todayStudentAtt = attendance.filter { it.dateKey == today && it.personType == "STUDENT" }
        val monthJournals = journals.filter { it.entryDate.startsWith(monthPrefix) }
        val income = monthJournals.sumOf { entry ->
            when {
                entry.creditAccountCode.startsWith("4") -> entry.amount
                entry.debitAccountCode.startsWith("4") -> -entry.amount
                else -> 0.0
            }
        }
        val expense = monthJournals.sumOf { entry ->
            when {
                entry.debitAccountCode.startsWith("5") -> entry.amount
                entry.creditAccountCode.startsWith("5") -> -entry.amount
                else -> 0.0
            }
        }

        DashboardMetrics(
            totalStudents = students.size,
            totalTeachers = teachers.size,
            todayPresent = todayStudentAtt.count { it.status == AttendanceStatus.PRESENT.name },
            todayAbsent = todayStudentAtt.count { it.status == AttendanceStatus.ABSENT.name },
            totalFeeCollected = invoices.sumOf { it.paidAmount },
            totalFeeDue = invoices.sumOf { it.dueAmount },
            monthlyIncome = income.coerceAtLeast(0.0),
            monthlyExpense = expense.coerceAtLeast(0.0),
            newAdmissionsThisMonth = students.count { it.admissionDate.startsWith(monthPrefix) },
            totalDonations = donations.sumOf { it.amount },
            nooraniCount = students.count { it.department == Department.NOORANI.name },
            nazeraCount = students.count { it.department == Department.NAZERA.name },
            hifzCount = students.count { it.department == Department.HIFZ.name },
            cashBalance = accounts.firstOrNull { it.code == "1001" }?.balance ?: 0.0,
            bankBalance = accounts.firstOrNull { it.code == "1002" }?.balance ?: 0.0
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DashboardMetrics())

    init {
        viewModelScope.launch {
            val res = repository.ensureOrganizationBootstrapped()
            if (res.isFailure) {
                _snackbarMessages.tryEmit(toBengaliErrorMessage(res.exceptionOrNull()))
            }
        }
    }

    private fun <T> Flow<List<T>>.asUiStateFlow(): StateFlow<UiState<List<T>>> =
        map<List<T>, UiState<List<T>>> { UiState.Success(it) }
            .catch { error ->
                if (error is CancellationException) throw error
                emit(UiState.Error(toBengaliErrorMessage(error)))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    fun setRolePreview(role: UserRole?) {
        _previewRoleOverride.value = role
    }

    fun isNetworkOnline(): Boolean = repository.isNetworkAvailable()

    fun syncOfflineAttendanceNow() {
        repository.triggerOfflineSyncNow()
        _snackbarMessages.tryEmit("অফলাইন হাজিরা সিঙ্ক প্রক্রিয়া শুরু হয়েছে।")
    }

    fun saveStudent(student: Student, existingId: String? = null) {
        viewModelScope.launch {
            val res = repository.saveStudent(student, existingId)
            _snackbarMessages.tryEmit(
                if (res.isSuccess) "শিক্ষার্থীর তথ্য সফলভাবে সংরক্ষণ করা হয়েছে।"
                else toBengaliErrorMessage(res.exceptionOrNull())
            )
        }
    }

    fun archiveOrDeleteStudent(studentId: String, studentName: String, archiveOnly: Boolean) {
        viewModelScope.launch {
            val res = repository.archiveOrDeleteStudent(studentId, studentName, archiveOnly)
            _snackbarMessages.tryEmit(
                if (res.isSuccess) if (archiveOnly) "শিক্ষার্থী আর্কাইভ করা হয়েছে।" else "শিক্ষার্থীর রেকর্ড মুছে ফেলা হয়েছে।"
                else toBengaliErrorMessage(res.exceptionOrNull())
            )
        }
    }

    fun saveTeacher(teacher: Teacher, existingId: String? = null) {
        viewModelScope.launch {
            val res = repository.saveTeacher(teacher, existingId)
            _snackbarMessages.tryEmit(
                if (res.isSuccess) "শিক্ষকের তথ্য সংরক্ষণ করা হয়েছে।"
                else toBengaliErrorMessage(res.exceptionOrNull())
            )
        }
    }

    fun payTeacherSalary(teacher: Teacher, method: String) {
        viewModelScope.launch {
            val res = repository.payTeacherSalary(teacher, method)
            _snackbarMessages.tryEmit(
                if (res.isSuccess) "${teacher.fullName}-এর বেতন হিসাবভুক্ত হয়েছে।"
                else toBengaliErrorMessage(res.exceptionOrNull())
            )
        }
    }

    fun saveGuardian(guardian: Guardian, existingId: String? = null) {
        viewModelScope.launch {
            val res = repository.saveGuardian(guardian, existingId)
            _snackbarMessages.tryEmit(
                if (res.isSuccess) "অভিভাবকের তথ্য সংরক্ষণ করা হয়েছে।"
                else toBengaliErrorMessage(res.exceptionOrNull())
            )
        }
    }

    fun recordAttendance(
        dateKey: String,
        personType: String,
        personId: String,
        personName: String,
        department: String,
        className: String,
        status: AttendanceStatus,
        lateArrival: Boolean,
        auditNote: String,
        @Suppress("UNUSED_PARAMETER") readerUids: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val forceOffline = settings.value.offlineFirstAttendance
            val res = repository.recordAttendance(
                dateKey = dateKey,
                personType = personType,
                personId = personId,
                personName = personName,
                department = department,
                className = className,
                status = status,
                lateArrival = lateArrival,
                auditNote = auditNote,
                forceOfflineQueue = forceOffline
            )
            res.onSuccess { syncedOnline ->
                _snackbarMessages.tryEmit(
                    if (syncedOnline) "$personName-এর হাজিরা (${status.labelBn}) সংরক্ষিত হয়েছে।"
                    else "$personName-এর হাজিরা অফলাইন কিউতে সংরক্ষিত হয়েছে।"
                )
            }.onFailure { err ->
                _snackbarMessages.tryEmit(toBengaliErrorMessage(err))
            }
        }
    }

    fun saveQuranProgress(progress: QuranProgress, existingId: String? = null) {
        viewModelScope.launch {
            val res = repository.saveQuranProgress(progress, existingId)
            _snackbarMessages.tryEmit(
                if (res.isSuccess) "কুরআন পাঠ অগ্রগতি সংরক্ষণ করা হয়েছে।"
                else toBengaliErrorMessage(res.exceptionOrNull())
            )
        }
    }

    fun createExam(exam: Exam) {
        viewModelScope.launch {
            val res = repository.createExam(exam)
            _snackbarMessages.tryEmit(
                if (res.isSuccess) "নতুন পরীক্ষা তৈরি করা হয়েছে।"
                else toBengaliErrorMessage(res.exceptionOrNull())
            )
        }
    }

    fun toggleExamPublish(examId: String, published: Boolean) {
        viewModelScope.launch {
            val res = repository.toggleExamPublish(examId, published)
            _snackbarMessages.tryEmit(
                if (res.isSuccess) if (published) "ফলাফল প্রকাশিত হয়েছে।" else "ফলাফল খসড়া অবস্থায় নেওয়া হয়েছে।"
                else toBengaliErrorMessage(res.exceptionOrNull())
            )
        }
    }

    fun saveExamResult(result: ExamResult) {
        viewModelScope.launch {
            val res = repository.saveExamResult(result)
            _snackbarMessages.tryEmit(
                if (res.isSuccess) "পরীক্ষার নম্বর সংরক্ষণ করা হয়েছে।"
                else toBengaliErrorMessage(res.exceptionOrNull())
            )
        }
    }

    fun createInvoice(invoice: Invoice) {
        viewModelScope.launch {
            val res = repository.createInvoice(invoice)
            _snackbarMessages.tryEmit(
                if (res.isSuccess) "বেতন/ফি চালান তৈরি করা হয়েছে।"
                else toBengaliErrorMessage(res.exceptionOrNull())
            )
        }
    }

    fun submitPayment(
        invoice: Invoice,
        amount: Double,
        gateway: PaymentGateway,
        transactionId: String,
        note: String
    ) {
        viewModelScope.launch {
            val sandbox = settings.value.paymentSandboxEnabled
            val res = repository.submitPayment(invoice, amount, gateway, transactionId, sandbox, note)
            _snackbarMessages.tryEmit(
                if (res.isSuccess) {
                    if (gateway == PaymentGateway.CASH) "নগদ পেমেন্ট গৃহীত ও চালান হালনাগাদ হয়েছে।"
                    else "${gateway.labelBn} পেমেন্ট রেকর্ড হয়েছে (যাচাই অপেক্ষমাণ)।"
                } else toBengaliErrorMessage(res.exceptionOrNull())
            )
        }
    }

    fun verifyPayment(payment: Payment, invoice: Invoice?, approve: Boolean) {
        viewModelScope.launch {
            val res = repository.verifyPendingPayment(payment, invoice, approve)
            _snackbarMessages.tryEmit(
                if (res.isSuccess) if (approve) "পেমেন্ট যাচাই ও অনুমোদন সম্পন্ন হয়েছে।" else "পেমেন্ট ব্যর্থ হিসেবে চিহ্নিত হয়েছে।"
                else toBengaliErrorMessage(res.exceptionOrNull())
            )
        }
    }

    fun refundPayment(payment: Payment) {
        viewModelScope.launch {
            val res = repository.refundVerifiedPayment(payment)
            _snackbarMessages.tryEmit(
                if (res.isSuccess) "রিফান্ড ও বিপরীত হিসাব দাখিলা সম্পন্ন হয়েছে।"
                else toBengaliErrorMessage(res.exceptionOrNull())
            )
        }
    }

    fun postJournalEntry(
        description: String,
        category: String,
        debitCode: String,
        debitName: String,
        creditCode: String,
        creditName: String,
        amount: Double
    ) {
        viewModelScope.launch {
            val ref = "JV-${System.currentTimeMillis().toString().takeLast(6)}"
            val res = repository.postJournalEntry(
                entryDate = BengaliFormat.todayDateKey(),
                referenceNo = ref,
                description = description,
                category = category,
                debitAccountCode = debitCode,
                debitAccountName = debitName,
                creditAccountCode = creditCode,
                creditAccountName = creditName,
                amount = amount
            )
            _snackbarMessages.tryEmit(
                if (res.isSuccess) "ডাবল-এন্ট্রি হিসাব দাখিলা সফলভাবে সংরক্ষিত হয়েছে।"
                else toBengaliErrorMessage(res.exceptionOrNull())
            )
        }
    }

    fun reverseJournalEntry(entry: JournalEntry, reason: String) {
        viewModelScope.launch {
            val res = repository.reverseJournalEntry(entry, reason)
            _snackbarMessages.tryEmit(
                if (res.isSuccess) "সংশোধনের জন্য রিভার্সাল এন্ট্রি পোস্ট করা হয়েছে।"
                else toBengaliErrorMessage(res.exceptionOrNull())
            )
        }
    }

    fun saveDonation(donorName: String, phone: String, amount: Double, purpose: String, method: String) {
        viewModelScope.launch {
            val res = repository.saveDonation(donorName, phone, amount, purpose, method)
            _snackbarMessages.tryEmit(
                if (res.isSuccess) "অনুদান সংরক্ষিত ও হিসাব খাতে যুক্ত হয়েছে।"
                else toBengaliErrorMessage(res.exceptionOrNull())
            )
        }
    }

    fun publishNotice(
        title: String,
        body: String,
        audience: String,
        important: Boolean,
        sendSms: Boolean,
        @Suppress("UNUSED_PARAMETER") readerUids: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            if (sendSms) {
                smsAdapter.sendBengaliSms(emptyList(), "$title: $body")
            }
            val res = repository.publishNotice(title, body, audience, important, sendSms)
            _snackbarMessages.tryEmit(
                if (res.isSuccess) "নোটিশ সফলভাবে প্রকাশিত হয়েছে।"
                else toBengaliErrorMessage(res.exceptionOrNull())
            )
        }
    }

    fun updateOrganizationSettings(nameBn: String, academicYear: String, address: String, phone: String) {
        viewModelScope.launch {
            val res = repository.updateOrganizationSettings(nameBn, academicYear, address, phone)
            _snackbarMessages.tryEmit(
                if (res.isSuccess) "মাদরাসার সেটিংস হালনাগাদ হয়েছে।"
                else toBengaliErrorMessage(res.exceptionOrNull())
            )
        }
    }

    fun updatePreferences(
        sandboxMode: Boolean? = null,
        offlineMode: Boolean? = null,
        smsEnabled: Boolean? = null
    ) {
        viewModelScope.launch {
            sandboxMode?.let { preferences.updateSandboxMode(it) }
            offlineMode?.let { preferences.updateOfflineAttendanceMode(it) }
            smsEnabled?.let { preferences.updateSmsSettings(it, settings.value.smsProviderName) }
        }
    }

    fun initializeAccountsManually() {
        viewModelScope.launch {
            val res = repository.initializeChartOfAccounts()
            _snackbarMessages.tryEmit(
                if (res.isSuccess) "হিসাব খাত (Chart of Accounts) প্রস্তুত করা হয়েছে।"
                else toBengaliErrorMessage(res.exceptionOrNull())
            )
        }
    }

    fun emitMessage(message: String) {
        _snackbarMessages.tryEmit(message)
    }
}

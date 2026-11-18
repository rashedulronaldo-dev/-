package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.BengaliFormat
import com.example.core.UiState
import com.example.data.model.Department
import com.example.data.model.NoticeAudience
import com.example.data.model.UserRole
import com.example.export.BengaliReportExporter
import com.example.export.ReportSection
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusAbsentBg
import com.example.ui.viewmodel.MadrasaViewModel

@Composable
fun NoticesScreen(viewModel: MadrasaViewModel) {
    val role by viewModel.effectiveRole.collectAsStateWithLifecycle()
    val noticesState by viewModel.noticesState.collectAsStateWithLifecycle()
    val studentsState by viewModel.studentsState.collectAsStateWithLifecycle()
    val teachersState by viewModel.teachersState.collectAsStateWithLifecycle()

    val notices = (noticesState as? UiState.Success)?.data.orEmpty()
    val students = (studentsState as? UiState.Success)?.data.orEmpty()
    val teachers = (teachersState as? UiState.Success)?.data.orEmpty()

    var showCreate by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (role == UserRole.ADMIN) {
                FloatingActionButton(
                    onClick = { showCreate = true },
                    containerColor = IslamicEmerald,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_notice_fab")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "নতুন নোটিশ প্রকাশ")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (notices.isEmpty()) {
                EmptyStateCard(
                    titleBn = "কোনো নোটিশ পাওয়া যায়নি",
                    subtitleBn = if (role == UserRole.ADMIN) "+ বোতামে চাপ দিয়ে নতুন ঘোষণা ও এসএমএস/পুশ বিজ্ঞপ্তি পাঠান।" else "মাদরাসার সকল ঘোষণা এখানে দেখা যাবে।",
                    icon = Icons.Filled.Campaign
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 84.dp)) {
                    items(notices, key = { it.id }) { n ->
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(
                                1.dp,
                                if (n.important) StatusAbsent.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(n.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    if (n.important) {
                                        Surface(shape = RoundedCornerShape(50), color = StatusAbsentBg) {
                                            Text("জরুরি", style = MaterialTheme.typography.labelSmall, color = StatusAbsent, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                        }
                                    }
                                }
                                Text(n.body, style = MaterialTheme.typography.bodyMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "${BengaliFormat.formatDateBn(n.publishedDate)} • ${n.audienceEnum.labelBn}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (n.smsDispatched) {
                                        Icon(Icons.Filled.Sms, contentDescription = "এসএমএস কিউ সম্পন্ন", tint = IslamicEmerald, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        var title by remember { mutableStateOf("") }
        var body by remember { mutableStateOf("") }
        var audience by remember { mutableStateOf(NoticeAudience.ALL) }
        var important by remember { mutableStateOf(false) }
        var sendSms by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("নতুন নোটিশ প্রকাশ", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("নোটিশের শিরোনাম *") }, singleLine = true)
                    OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("বিস্তারিত বার্তা *") })
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        NoticeAudience.entries.forEach { a ->
                            FilterChip(selected = audience == a, onClick = { audience = a }, label = { Text(a.labelBn) })
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = important, onCheckedChange = { important = it })
                        Text("জরুরি ঘোষণা হিসেবে চিহ্নিত করুন")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = sendSms, onCheckedChange = { sendSms = it })
                        Text("এসএমএস অ্যাডাপ্টারের মাধ্যমে কিউ করুন")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && body.isNotBlank()) {
                            val readers = buildList {
                                students.forEach { addAll(it.readerUids) }
                                teachers.forEach { addAll(it.readerUids) }
                            }.distinct()
                            viewModel.publishNotice(title.trim(), body.trim(), audience.name, important, sendSms, readers)
                            showCreate = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald)
                ) { Text("প্রকাশ করুন") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("বাতিল") } }
        )
    }
}

private enum class ReportKind(val titleBn: String) {
    STUDENT_LIST("শিক্ষার্থী তালিকা"),
    TEACHER_LIST("শিক্ষক তালিকা"),
    DAILY_ATTENDANCE("দৈনিক হাজিরা"),
    MONTHLY_ATTENDANCE("মাসিক হাজিরা"),
    DEPT_STUDENTS("বিভাগভিত্তিক শিক্ষার্থী"),
    FEE_COLLECTION("বেতন আদায় প্রতিবেদন"),
    DUE_FEES("বকেয়া বেতন তালিকা"),
    INCOME_EXPENSE("আয় ও ব্যয় প্রতিবেদন"),
    DONATIONS("অনুদান প্রতিবেদন"),
    EXAM_RESULTS("পরীক্ষার ফলাফল"),
    NOORANI_PROGRESS("নূরানী অগ্রগতি"),
    NAZERA_PROGRESS("নাজেরা অগ্রগতি"),
    HIFZ_PROGRESS("হিফজ অগ্রগতি")
}

@Composable
fun ReportsScreen(viewModel: MadrasaViewModel) {
    val context = LocalContext.current
    val studentsState by viewModel.studentsState.collectAsStateWithLifecycle()
    val teachersState by viewModel.teachersState.collectAsStateWithLifecycle()
    val attendanceState by viewModel.attendanceState.collectAsStateWithLifecycle()
    val invoicesState by viewModel.invoicesState.collectAsStateWithLifecycle()
    val journalState by viewModel.journalState.collectAsStateWithLifecycle()
    val donationsState by viewModel.donationsState.collectAsStateWithLifecycle()
    val resultsState by viewModel.examResultsState.collectAsStateWithLifecycle()
    val quranState by viewModel.quranProgressState.collectAsStateWithLifecycle()

    val students = (studentsState as? UiState.Success)?.data.orEmpty().filter { !it.archived }
    val teachers = (teachersState as? UiState.Success)?.data.orEmpty()
    val attendance = (attendanceState as? UiState.Success)?.data.orEmpty()
    val invoices = (invoicesState as? UiState.Success)?.data.orEmpty()
    val journals = (journalState as? UiState.Success)?.data.orEmpty()
    val donations = (donationsState as? UiState.Success)?.data.orEmpty()
    val results = (resultsState as? UiState.Success)?.data.orEmpty()
    val quran = (quranState as? UiState.Success)?.data.orEmpty()

    var selectedDept by remember { mutableStateOf<Department?>(null) }
    var dateFilter by remember { mutableStateOf(BengaliFormat.todayDateKey()) }

    fun buildReportTable(kind: ReportKind): Pair<List<String>, List<List<String>>> {
        val deptStudents = students.filter { selectedDept == null || it.department == selectedDept?.name }
        return when (kind) {
            ReportKind.STUDENT_LIST -> listOf("আইডি", "নাম", "বিভাগ", "শ্রেণি", "মোবাইল") to
                deptStudents.map { listOf(it.studentCode, it.fullName, it.departmentEnum.labelBn, it.className, it.guardianPhone) }
            ReportKind.TEACHER_LIST -> listOf("নাম", "পদবি", "বিভাগ", "মোবাইল", "বেতন") to
                teachers.map { listOf(it.fullName, it.designation, it.departmentEnum.labelBn, it.phone, BengaliFormat.formatTaka(it.monthlySalary)) }
            ReportKind.DAILY_ATTENDANCE -> listOf("তারিখ", "নাম", "শ্রেণি", "অবস্থা") to
                attendance.filter { it.dateKey == dateFilter }.map { listOf(it.dateKey, it.personName, it.className, it.statusEnum.labelBn) }
            ReportKind.MONTHLY_ATTENDANCE -> listOf("তারিখ", "নাম", "বিভাগ", "অবস্থা") to
                attendance.filter { it.dateKey.startsWith(BengaliFormat.currentMonthKey()) }
                    .map { listOf(it.dateKey, it.personName, Department.fromRaw(it.department).labelBn, it.statusEnum.labelBn) }
            ReportKind.DEPT_STUDENTS -> listOf("বিভাগ", "শিক্ষার্থী সংখ্যা") to
                Department.entries.map { d -> listOf(d.labelBn, "${BengaliFormat.toBnNumber(students.count { it.department == d.name })} জন") }
            ReportKind.FEE_COLLECTION -> listOf("রসিদ", "শিক্ষার্থী", "মাস", "মোট", "আদায়") to
                invoices.map { listOf(it.invoiceNumber, it.studentName, it.billingMonth, BengaliFormat.formatTaka(it.totalAmount), BengaliFormat.formatTaka(it.paidAmount)) }
            ReportKind.DUE_FEES -> listOf("রসিদ", "শিক্ষার্থী", "মাস", "বকেয়া") to
                invoices.filter { it.dueAmount > 0 }.map { listOf(it.invoiceNumber, it.studentName, it.billingMonth, BengaliFormat.formatTaka(it.dueAmount)) }
            ReportKind.INCOME_EXPENSE -> listOf("তারিখ", "ভাউচার", "বিবরণ", "ডেবিট", "ক্রেডিট", "পরিমাণ") to
                journals.map { listOf(it.entryDate, it.referenceNo, it.description, it.debitAccountName, it.creditAccountName, BengaliFormat.formatTaka(it.amount)) }
            ReportKind.DONATIONS -> listOf("রসিদ", "দাতার নাম", "উদ্দেশ্য", "তারিখ", "পরিমাণ") to
                donations.map { listOf(it.receiptNumber, it.donorName, it.purpose, it.donationDate, BengaliFormat.formatTaka(it.amount)) }
            ReportKind.EXAM_RESULTS -> listOf("পরীক্ষা", "শিক্ষার্থী", "বিষয়", "প্রাপ্ত/পূর্ণ", "গ্রেড") to
                results.map { listOf(it.examTitle, it.studentName, it.subject, "${it.obtainedMarks}/${it.totalMarks}", it.grade) }
            ReportKind.NOORANI_PROGRESS -> listOf("তারিখ", "শিক্ষার্থী", "পাঠ", "নির্ভুলতা") to
                quran.filter { it.department == Department.NOORANI.name }.map { listOf(it.dateKey, it.studentName, "${it.lessonType} (${it.surahName})", "${it.accuracyPercent}%") }
            ReportKind.NAZERA_PROGRESS -> listOf("তারিখ", "শিক্ষার্থী", "সূরা/পারা", "ভুল") to
                quran.filter { it.department == Department.NAZERA.name }.map { listOf(it.dateKey, it.studentName, "${it.surahName} (পারা ${it.paraNumber})", "${it.mistakeCount}") }
            ReportKind.HIFZ_PROGRESS -> listOf("তারিখ", "শিক্ষার্থী", "ধরন", "সূরা/পারা", "ভুল") to
                quran.filter { it.department == Department.HIFZ.name }.map { listOf(it.dateKey, it.studentName, it.lessonType, "${it.surahName} (পারা ${it.paraNumber})", "${it.mistakeCount}") }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = IslamicEmerald),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("বাংলা প্রতিবেদন, পিডিএফ ও এক্সেল এক্সপোর্ট", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("নোটো স্যান্স বাংলা ও আরবি ইউনিকোড RTL ফন্টসহ প্রিন্ট ও ডাউনলোড", style = MaterialTheme.typography.bodySmall, color = IslamicGoldLight)
                }
            }
        }

        item {
            OutlinedTextField(
                value = dateFilter,
                onValueChange = { dateFilter = it },
                label = { Text("তারিখ ফিল্টার (YYYY-MM-DD)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = selectedDept == null, onClick = { selectedDept = null }, label = { Text("সকল বিভাগ") })
                Department.entries.forEach { d ->
                    FilterChip(selected = selectedDept == d, onClick = { selectedDept = d }, label = { Text(d.labelBn) })
                }
            }
        }

        items(ReportKind.entries) { kind ->
            val (headers, rows) = buildReportTable(kind)
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Assessment, contentDescription = null, tint = IslamicEmerald, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(kind.titleBn, fontWeight = FontWeight.Bold)
                        }
                        Text("${BengaliFormat.toBnNumber(rows.size)} সারি", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = {
                            val res = BengaliReportExporter.generateBengaliPdf(
                                context = context,
                                fileNamePrefix = kind.name.lowercase(),
                                titleBn = kind.titleBn,
                                subtitleBn = selectedDept?.labelBn ?: "সকল বিভাগ",
                                sections = listOf(
                                    ReportSection(
                                        headingBn = kind.titleBn,
                                        arabicLine = if (kind in listOf(ReportKind.NOORANI_PROGRESS, ReportKind.NAZERA_PROGRESS, ReportKind.HIFZ_PROGRESS)) {
                                            "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ"
                                        } else null,
                                        rows = listOf(headers) + rows.ifEmpty { listOf(listOf("কোনো তথ্য পাওয়া যায়নি")) }
                                    )
                                )
                            )
                            res.onSuccess { file ->
                                BengaliReportExporter.shareFile(context, file, "application/pdf", "পিডিএফ ডাউনলোড / শেয়ার")
                            }.onFailure { viewModel.emitMessage("পিডিএফ তৈরিতে ত্রুটি হয়েছে।") }
                        }) {
                            Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("পিডিএফ")
                        }

                        OutlinedButton(onClick = {
                            val res = BengaliReportExporter.generateBengaliExcelCsv(
                                context = context,
                                fileNamePrefix = kind.name.lowercase(),
                                headers = headers,
                                rows = rows
                            )
                            res.onSuccess { file ->
                                BengaliReportExporter.shareFile(context, file, "text/csv", "এক্সেল (CSV) ডাউনলোড")
                            }.onFailure { viewModel.emitMessage("এক্সেল ফাইল তৈরিতে ত্রুটি হয়েছে।") }
                        }) {
                            Icon(Icons.Filled.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("এক্সেল (CSV)")
                        }

                        OutlinedButton(onClick = {
                            val res = BengaliReportExporter.generateBengaliPdf(
                                context = context,
                                fileNamePrefix = kind.name.lowercase(),
                                titleBn = kind.titleBn,
                                subtitleBn = selectedDept?.labelBn ?: "সকল বিভাগ",
                                sections = listOf(
                                    ReportSection(
                                        headingBn = kind.titleBn,
                                        rows = listOf(headers) + rows.ifEmpty { listOf(listOf("কোনো তথ্য পাওয়া যায়নি")) }
                                    )
                                )
                            )
                            res.onSuccess { file -> BengaliReportExporter.printPdfFile(context, file, kind.titleBn) }
                        }) {
                            Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("প্রিন্ট করুন")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: MadrasaViewModel
) {
    val role by viewModel.effectiveRole.collectAsStateWithLifecycle()
    val org by viewModel.organization.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var orgName by remember(org) { mutableStateOf(org?.nameBn ?: "খেলার মাঠ নূরানী তালিমূল কোরআন ও হাফিজিয়া মাদরাসা") }
    var academicYear by remember(org) { mutableStateOf(org?.academicYear ?: "২০২৬") }
    var address by remember(org) { mutableStateOf(org?.address ?: "খেলার মাঠ, ঢাকা") }
    var phone by remember(org) { mutableStateOf(org?.phone ?: "০১৭০০-০০০০০০") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ব্যবহারকারীর ভূমিকা পরিবর্তন", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("বর্তমান প্যানেল: ${role.labelBn}", color = IslamicEmerald, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UserRole.entries.forEach { targetRole ->
                            FilterChip(
                                selected = role == targetRole,
                                onClick = {
                                    viewModel.setRolePreview(if (targetRole == UserRole.ADMIN) null else targetRole)
                                },
                                label = { Text(targetRole.labelBn) }
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("অ্যাপ ও সিঙ্ক সেটিংস", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("অফলাইন হাজিরা মোড", fontWeight = FontWeight.SemiBold)
                            Text("স্থানীয় Room ডেটাবেজে সংরক্ষণ করে পরে সিঙ্ক করুন", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = settings.offlineFirstAttendance, onCheckedChange = { viewModel.updatePreferences(offlineMode = it) })
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("পেমেন্ট স্যান্ডবক্স মোড", fontWeight = FontWeight.SemiBold)
                            Text("bKash / Nagad / SSLCommerz স্যান্ডবক্স পরীক্ষা", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = settings.paymentSandboxEnabled, onCheckedChange = { viewModel.updatePreferences(sandboxMode = it) })
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("এসএমএস গেটওয়ে অ্যাডাপ্টার", fontWeight = FontWeight.SemiBold)
                            Text(settings.smsProviderName, style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = settings.smsGatewayEnabled, onCheckedChange = { viewModel.updatePreferences(smsEnabled = it) })
                    }
                }
            }
        }

        if (role == UserRole.ADMIN) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("মাদরাসার প্রাতিষ্ঠানিক তথ্য", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        OutlinedTextField(value = orgName, onValueChange = { orgName = it }, label = { Text("মাদরাসার নাম") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = academicYear, onValueChange = { academicYear = it }, label = { Text("শিক্ষাবর্ষ") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("ঠিকানা") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("যোগাযোগ নম্বর") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        Button(
                            onClick = { viewModel.updateOrganizationSettings(orgName, academicYear, address, phone) },
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald)
                        ) {
                            Text("সংরক্ষণ করুন")
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "অ্যাপ ডেভেলপার: MD Rashedul Islam",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }
    }
}

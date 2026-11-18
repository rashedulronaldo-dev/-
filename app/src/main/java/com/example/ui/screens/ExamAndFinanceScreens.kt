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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.data.model.Exam
import com.example.data.model.ExamResult
import com.example.data.model.FeeType
import com.example.data.model.Invoice
import com.example.data.model.JournalEntry
import com.example.data.model.Payment
import com.example.data.model.PaymentGateway
import com.example.data.model.PaymentStatus
import com.example.data.model.UserRole
import com.example.export.BengaliReportExporter
import com.example.export.ReportSection
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.MadrasaStatCard
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusAbsentBg
import com.example.ui.theme.StatusPresent
import com.example.ui.theme.StatusPresentBg
import com.example.ui.viewmodel.MadrasaViewModel

@Composable
fun ExaminationsScreen(viewModel: MadrasaViewModel) {
    val context = LocalContext.current
    val role by viewModel.effectiveRole.collectAsStateWithLifecycle()
    val examsState by viewModel.examsState.collectAsStateWithLifecycle()
    val resultsState by viewModel.examResultsState.collectAsStateWithLifecycle()
    val studentsState by viewModel.studentsState.collectAsStateWithLifecycle()

    val exams = (examsState as? UiState.Success)?.data.orEmpty()
    val results = (resultsState as? UiState.Success)?.data.orEmpty()
    val students = (studentsState as? UiState.Success)?.data.orEmpty().filter { !it.archived }

    var showCreateExam by remember { mutableStateOf(false) }
    var addResultForExam by remember { mutableStateOf<Exam?>(null) }

    Scaffold(
        floatingActionButton = {
            if (role == UserRole.ADMIN) {
                FloatingActionButton(
                    onClick = { showCreateExam = true },
                    containerColor = IslamicEmerald,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_exam_fab")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "নতুন পরীক্ষা তৈরি")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (exams.isEmpty()) {
                item {
                    EmptyStateCard(
                        titleBn = "কোনো পরীক্ষা তৈরি হয়নি",
                        subtitleBn = if (role == UserRole.ADMIN) "+ বোতামে চাপ দিয়ে পরীক্ষা তৈরি করুন।" else "প্রকাশিত পরীক্ষার ফলাফল এখানে দেখা যাবে।",
                        icon = Icons.Filled.Quiz
                    )
                }
            } else {
                items(exams, key = { it.id }) { exam ->
                    val examResults = results.filter { it.examId == exam.id }.sortedByDescending { it.obtainedMarks }
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(exam.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${Department.fromRaw(exam.department).labelBn} • ${exam.className} • বিষয়: ${exam.subject}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text("পূর্ণমান: ${BengaliFormat.toBnNumber(exam.totalMarks)} • তারিখ: ${exam.examDate}", style = MaterialTheme.typography.bodySmall)
                                }
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (exam.published) StatusPresentBg else IslamicGoldLight
                                ) {
                                    Text(
                                        if (exam.published) "প্রকাশিত" else "খসড়া",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (exam.published) StatusPresent else Color(0xFF6B4C00),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (role == UserRole.ADMIN || role == UserRole.TEACHER) {
                                    OutlinedButton(onClick = { addResultForExam = exam }) {
                                        Text("নম্বর দিন")
                                    }
                                }
                                if (role == UserRole.ADMIN) {
                                    OutlinedButton(onClick = { viewModel.toggleExamPublish(exam.id, !exam.published) }) {
                                        Text(if (exam.published) "অপ্রকাশিত করুন" else "ফলাফল প্রকাশ করুন")
                                    }
                                }
                                OutlinedButton(onClick = {
                                    val res = BengaliReportExporter.generateBengaliPdf(
                                        context = context,
                                        fileNamePrefix = "marksheet_${exam.id}",
                                        titleBn = "পরীক্ষার নম্বরপত্র (মার্কশিট)",
                                        subtitleBn = "${exam.title} — ${exam.subject}",
                                        sections = listOf(
                                            ReportSection(
                                                headingBn = "${exam.className} ফলাফল তালিকা",
                                                rows = examResults.mapIndexed { idx, r ->
                                                    listOf(
                                                        "স্থান: ${BengaliFormat.formatPositionBn(idx + 1)}",
                                                        r.studentName,
                                                        "প্রাপ্ত নম্বর: ${BengaliFormat.toBnNumber(r.obtainedMarks)}/${BengaliFormat.toBnNumber(r.totalMarks)}",
                                                        "গ্রেড: ${r.grade}"
                                                    )
                                                }.ifEmpty { listOf(listOf("এখনও নম্বর এন্ট্রি হয়নি")) }
                                            )
                                        )
                                    )
                                    res.onSuccess { file ->
                                        BengaliReportExporter.printPdfFile(context, file, exam.title)
                                    }
                                }) {
                                    Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("রিপোর্ট কার্ড প্রিন্ট")
                                }
                            }

                            if (examResults.isNotEmpty()) {
                                examResults.forEachIndexed { idx, r ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("${BengaliFormat.formatPositionBn(idx + 1)} • ${r.studentName}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                                            Text(
                                                "${BengaliFormat.toBnNumber(r.obtainedMarks)}/${BengaliFormat.toBnNumber(r.totalMarks)} (${r.grade})",
                                                fontWeight = FontWeight.Bold,
                                                color = IslamicEmerald,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateExam) {
        var title by remember { mutableStateOf("সাময়িক পরীক্ষা ২০২৬") }
        var dept by remember { mutableStateOf(Department.HIFZ) }
        var className by remember { mutableStateOf(Department.HIFZ.classes.first()) }
        var subject by remember { mutableStateOf("হিফজুল কুরআন ও তাজবিদ") }
        var totalStr by remember { mutableStateOf("100") }

        AlertDialog(
            onDismissRequest = { showCreateExam = false },
            title = { Text("নতুন পরীক্ষা তৈরি করুন", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("পরীক্ষার নাম *") }, singleLine = true)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Department.entries.forEach { d ->
                            FilterChip(selected = dept == d, onClick = { dept = d; className = d.classes.first() }, label = { Text(d.labelBn) })
                        }
                    }
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        dept.classes.forEach { c ->
                            FilterChip(selected = className == c, onClick = { className = c }, label = { Text(c) })
                        }
                    }
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("বিষয় *") }, singleLine = true)
                    OutlinedTextField(value = totalStr, onValueChange = { totalStr = it }, label = { Text("পূর্ণমান") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && subject.isNotBlank()) {
                            viewModel.createExam(
                                Exam(
                                    title = title.trim(),
                                    department = dept.name,
                                    className = className,
                                    subject = subject.trim(),
                                    totalMarks = (totalStr.toIntOrNull() ?: 100).coerceIn(1, 1000),
                                    examDate = BengaliFormat.todayDateKey(),
                                    published = false
                                )
                            )
                            showCreateExam = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald)
                ) { Text("তৈরি করুন") }
            },
            dismissButton = { TextButton(onClick = { showCreateExam = false }) { Text("বাতিল") } }
        )
    }

    addResultForExam?.let { exam ->
        val eligible = students.filter { it.department == exam.department }
        var selectedStudent by remember { mutableStateOf(eligible.firstOrNull() ?: students.firstOrNull()) }
        var marksStr by remember { mutableStateOf("85") }
        var positionStr by remember { mutableStateOf("1") }

        AlertDialog(
            onDismissRequest = { addResultForExam = null },
            title = { Text("${exam.title} — নম্বর এন্ট্রি", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (eligible.ifEmpty { students }).forEach { st ->
                            FilterChip(selected = selectedStudent?.id == st.id, onClick = { selectedStudent = st }, label = { Text(st.fullName) })
                        }
                    }
                    OutlinedTextField(value = marksStr, onValueChange = { marksStr = it }, label = { Text("প্রাপ্ত নম্বর (সর্বোচ্চ ${exam.totalMarks})") }, singleLine = true)
                    OutlinedTextField(value = positionStr, onValueChange = { positionStr = it }, label = { Text("মেধাক্রম (১, ২, ৩...)") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    enabled = selectedStudent != null,
                    onClick = {
                        val stu = selectedStudent ?: return@Button
                        val obtained = (marksStr.toIntOrNull() ?: 0).coerceIn(0, exam.totalMarks)
                        val grade = BengaliFormat.calculateGrade(obtained, exam.totalMarks)
                        viewModel.saveExamResult(
                            ExamResult(
                                examId = exam.id,
                                examTitle = exam.title,
                                studentId = stu.id,
                                studentName = stu.fullName,
                                department = exam.department,
                                className = exam.className,
                                subject = exam.subject,
                                totalMarks = exam.totalMarks,
                                obtainedMarks = obtained,
                                grade = grade,
                                position = (positionStr.toIntOrNull() ?: 1).coerceAtLeast(1),
                                published = exam.published,
                                readerUids = stu.readerUids
                            )
                        )
                        addResultForExam = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald)
                ) { Text("সংরক্ষণ করুন") }
            },
            dismissButton = { TextButton(onClick = { addResultForExam = null }) { Text("বাতিল") } }
        )
    }
}

@Composable
fun FeesScreen(viewModel: MadrasaViewModel) {
    val context = LocalContext.current
    val role by viewModel.effectiveRole.collectAsStateWithLifecycle()
    val invoicesState by viewModel.invoicesState.collectAsStateWithLifecycle()
    val studentsState by viewModel.studentsState.collectAsStateWithLifecycle()

    val invoices = (invoicesState as? UiState.Success)?.data.orEmpty()
    val students = (studentsState as? UiState.Success)?.data.orEmpty().filter { !it.archived }

    var dueOnly by remember { mutableStateOf(false) }
    var showCreateInvoice by remember { mutableStateOf(false) }
    var payInvoice by remember { mutableStateOf<Invoice?>(null) }

    val displayed = invoices.filter { !dueOnly || it.dueAmount > 0.0 }

    Scaffold(
        floatingActionButton = {
            if (role == UserRole.ADMIN) {
                FloatingActionButton(
                    onClick = { showCreateInvoice = true },
                    containerColor = IslamicEmerald,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_invoice_fab")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "নতুন বেতন চালান")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MadrasaStatCard(
                    titleBn = "মোট আদায়",
                    valueBn = BengaliFormat.formatTaka(invoices.sumOf { it.paidAmount }),
                    icon = Icons.Filled.Payments,
                    accentColor = StatusPresent,
                    modifier = Modifier.weight(1f)
                )
                MadrasaStatCard(
                    titleBn = "মোট বকেয়া",
                    valueBn = BengaliFormat.formatTaka(invoices.sumOf { it.dueAmount }),
                    icon = Icons.Filled.Payments,
                    accentColor = StatusAbsent,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            FilterChip(
                selected = dueOnly,
                onClick = { dueOnly = !dueOnly },
                label = { Text("শুধুমাত্র বকেয়া তালিকা (${BengaliFormat.toBnNumber(invoices.count { it.dueAmount > 0 })})") }
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (displayed.isEmpty()) {
                EmptyStateCard(
                    titleBn = "কোনো বেতন চালান পাওয়া যায়নি",
                    subtitleBn = if (role == UserRole.ADMIN) "+ বোতামে চাপ দিয়ে শিক্ষার্থীর নামে চালান তৈরি করুন।" else "আপনার সন্তানের বেতন সংক্রান্ত চালান এখানে দেখা যাবে।",
                    icon = Icons.Filled.Payments
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 84.dp)) {
                    items(displayed, key = { it.id }) { inv ->
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(inv.studentName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    val statusBn = when (inv.status) {
                                        "PAID" -> "পরিশোধিত"
                                        "PARTIAL" -> "আংশিক পরিশোধিত"
                                        else -> "বকেয়া"
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (inv.status == "PAID") StatusPresentBg else StatusAbsentBg
                                    ) {
                                        Text(
                                            statusBn,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (inv.status == "PAID") StatusPresent else StatusAbsent,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                Text("${inv.feeTypeEnum.labelBn} • মাস: ${inv.billingMonth} • রসিদ: ${inv.invoiceNumber}", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "মোট: ${BengaliFormat.formatTaka(inv.totalAmount)} • আদায়: ${BengaliFormat.formatTaka(inv.paidAmount)} • বকেয়া: ${BengaliFormat.formatTaka(inv.dueAmount)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (inv.dueAmount > 0.0) {
                                        Button(
                                            onClick = { payInvoice = inv },
                                            colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald)
                                        ) {
                                            Text("পরিশোধ করুন")
                                        }
                                    }
                                    OutlinedButton(onClick = {
                                        val res = BengaliReportExporter.generateBengaliPdf(
                                            context = context,
                                            fileNamePrefix = "receipt_${inv.invoiceNumber}",
                                            titleBn = "বেতন ও ফি রসিদ",
                                            subtitleBn = inv.invoiceNumber,
                                            sections = listOf(
                                                ReportSection(
                                                    headingBn = "রসিদের বিবরণ",
                                                    rows = listOf(
                                                        listOf("শিক্ষার্থীর নাম: ${inv.studentName}", "খাত: ${inv.feeTypeEnum.labelBn}"),
                                                        listOf("মাস: ${inv.billingMonth}", "মোট ফি: ${BengaliFormat.formatTaka(inv.totalAmount)}"),
                                                        listOf("পরিশোধিত: ${BengaliFormat.formatTaka(inv.paidAmount)}", "বকেয়া: ${BengaliFormat.formatTaka(inv.dueAmount)}")
                                                    )
                                                )
                                            )
                                        )
                                        res.onSuccess { file -> BengaliReportExporter.printPdfFile(context, file, inv.invoiceNumber) }
                                    }) {
                                        Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("রসিদ")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateInvoice) {
        var selectedStudent by remember { mutableStateOf(students.firstOrNull()) }
        var feeType by remember { mutableStateOf(FeeType.MONTHLY_TUITION) }
        var monthLabel by remember { mutableStateOf(BengaliFormat.currentMonthLabelBn()) }
        var amountStr by remember { mutableStateOf("1200") }

        AlertDialog(
            onDismissRequest = { showCreateInvoice = false },
            title = { Text("নতুন ফি / বেতন চালান", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        students.forEach { st ->
                            FilterChip(selected = selectedStudent?.id == st.id, onClick = { selectedStudent = st }, label = { Text(st.fullName) })
                        }
                    }
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FeeType.entries.forEach { ft ->
                            FilterChip(selected = feeType == ft, onClick = { feeType = ft }, label = { Text(ft.labelBn) })
                        }
                    }
                    OutlinedTextField(value = monthLabel, onValueChange = { monthLabel = it }, label = { Text("মাস / বিবরণ") }, singleLine = true)
                    OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("টাকার পরিমাণ (৳)") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    enabled = selectedStudent != null,
                    onClick = {
                        val stu = selectedStudent ?: return@Button
                        val amt = amountStr.toDoubleOrNull() ?: 0.0
                        if (amt > 0.0) {
                            viewModel.createInvoice(
                                Invoice(
                                    invoiceNumber = "INV-${System.currentTimeMillis().toString().takeLast(6)}",
                                    studentId = stu.id,
                                    studentName = stu.fullName,
                                    department = stu.department,
                                    feeType = feeType.name,
                                    billingMonth = monthLabel.trim(),
                                    totalAmount = amt,
                                    paidAmount = 0.0,
                                    dueAmount = amt,
                                    status = "UNPAID",
                                    readerUids = stu.readerUids
                                )
                            )
                            showCreateInvoice = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald)
                ) { Text("তৈরি করুন") }
            },
            dismissButton = { TextButton(onClick = { showCreateInvoice = false }) { Text("বাতিল") } }
        )
    }

    payInvoice?.let { inv ->
        var amountStr by remember { mutableStateOf(inv.dueAmount.toInt().toString()) }
        var gateway by remember { mutableStateOf(PaymentGateway.CASH) }
        var trxId by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { payInvoice = null },
            title = { Text("বেতন পরিশোধ — ${inv.studentName}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("বকেয়া পরিমাণ: ${BengaliFormat.formatTaka(inv.dueAmount)}")
                    OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("জমার পরিমাণ (আংশিক বা পূর্ণ)") }, singleLine = true)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PaymentGateway.entries.forEach { gw ->
                            FilterChip(selected = gateway == gw, onClick = { gateway = gw }, label = { Text(gw.labelBn) })
                        }
                    }
                    if (gateway != PaymentGateway.CASH) {
                        OutlinedTextField(value = trxId, onValueChange = { trxId = it }, label = { Text("ট্রানজেকশন আইডি (Transaction ID)") }, singleLine = true)
                        Text(
                            "নিরাপত্তা নীতি: অনলাইন পেমেন্ট ব্যাকএন্ড IPN/ওয়েবহুক যাচাইয়ের পরই চালান পরিশোধিত হিসেবে চিহ্নিত হবে।",
                            style = MaterialTheme.typography.labelSmall,
                            color = IslamicEmerald
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = (amountStr.toDoubleOrNull() ?: 0.0).coerceAtMost(inv.dueAmount)
                        if (amt > 0.0) {
                            viewModel.submitPayment(inv, amt, gateway, trxId.trim(), "${gateway.labelBn} মাধ্যমে জমা")
                            payInvoice = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald)
                ) { Text("নিশ্চিত করুন") }
            },
            dismissButton = { TextButton(onClick = { payInvoice = null }) { Text("বাতিল") } }
        )
    }
}

@Composable
fun PaymentsScreen(viewModel: MadrasaViewModel) {
    val role by viewModel.effectiveRole.collectAsStateWithLifecycle()
    val paymentsState by viewModel.paymentsState.collectAsStateWithLifecycle()
    val invoicesState by viewModel.invoicesState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val payments = (paymentsState as? UiState.Success)?.data.orEmpty()
    val invoices = (invoicesState as? UiState.Success)?.data.orEmpty()

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
                    Text("অনলাইন পেমেন্ট গেটওয়ে আর্কিটেকচার", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "bKash • Nagad • SSLCommerz (${if (settings.paymentSandboxEnabled) "স্যান্ডবক্স মোড সক্রিয়" else "লাইভ মোড"})",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicGoldLight
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "সার্ভার-সাইড IPN/Webhook যাচাই ছাড়া কোনো অনলাইন পেমেন্ট স্বয়ংক্রিয়ভাবে পরিশোধিত ধরা হয় না এবং অ্যাপে কোনো গেটওয়ে সিক্রেট সংরক্ষিত নেই।",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        if (payments.isEmpty()) {
            item {
                EmptyStateCard(
                    titleBn = "কোনো পেমেন্ট লেনদেন নেই",
                    subtitleBn = "বেতন ও ফি মডিউল থেকে নগদ বা bKash/Nagad/SSLCommerz লেনদেন করলে এখানে যাচাই ও রসিদ দেখা যাবে।",
                    icon = Icons.Filled.CreditCard
                )
            }
        } else {
            items(payments, key = { it.id }) { pay ->
                val linkedInv = invoices.firstOrNull { it.id == pay.invoiceId }
                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${pay.studentName} • ${BengaliFormat.formatTaka(pay.amount)}", fontWeight = FontWeight.Bold)
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = when (pay.statusEnum) {
                                    PaymentStatus.VERIFIED -> StatusPresentBg
                                    PaymentStatus.FAILED, PaymentStatus.REFUNDED -> StatusAbsentBg
                                    PaymentStatus.PENDING -> IslamicGoldLight
                                }
                            ) {
                                Text(
                                    pay.statusEnum.labelBn,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Text("মাধ্যম: ${pay.gatewayEnum.labelBn} • ট্রানজেকশন: ${pay.transactionId}", style = MaterialTheme.typography.bodySmall)
                        Text("রসিদ নম্বর: ${pay.receiptNumber} • ${if (pay.sandboxMode) "স্যান্ডবক্স" else "প্রোডাকশন"}", style = MaterialTheme.typography.bodySmall)
                        if (role == UserRole.ADMIN) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (pay.statusEnum == PaymentStatus.PENDING) {
                                    Button(
                                        onClick = { viewModel.verifyPayment(pay, linkedInv, approve = true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald)
                                    ) {
                                        Icon(Icons.Filled.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("যাচাই সম্পন্ন")
                                    }
                                    OutlinedButton(onClick = { viewModel.verifyPayment(pay, linkedInv, approve = false) }) {
                                        Text("ব্যর্থ চিহ্নিত", color = StatusAbsent)
                                    }
                                } else if (pay.statusEnum == PaymentStatus.VERIFIED) {
                                    OutlinedButton(onClick = { viewModel.refundPayment(pay) }) {
                                        Icon(Icons.Filled.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("রিফান্ড করুন")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountingScreen(viewModel: MadrasaViewModel) {
    val accountsState by viewModel.accountsState.collectAsStateWithLifecycle()
    val journalState by viewModel.journalState.collectAsStateWithLifecycle()
    val metrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()

    val accounts = (accountsState as? UiState.Success)?.data.orEmpty()
    val entries = (journalState as? UiState.Success)?.data.orEmpty()

    var showNewEntry by remember { mutableStateOf(false) }
    var reverseTarget by remember { mutableStateOf<JournalEntry?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewEntry = true },
                containerColor = IslamicEmerald,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_journal_fab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "নতুন আয়-ব্যয় দাখিলা")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MadrasaStatCard(
                        titleBn = "নগদ তহবিল (Cash)",
                        valueBn = BengaliFormat.formatTaka(metrics.cashBalance),
                        icon = Icons.Filled.AccountBalance,
                        modifier = Modifier.weight(1f)
                    )
                    MadrasaStatCard(
                        titleBn = "ব্যাংক ব্যালেন্স",
                        valueBn = BengaliFormat.formatTaka(metrics.bankBalance),
                        icon = Icons.Filled.AccountBalance,
                        accentColor = IslamicGold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (accounts.isEmpty()) {
                item {
                    OutlinedButton(
                        onClick = { viewModel.initializeAccountsManually() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ডাবল-এন্ট্রি হিসাব খাত (Chart of Accounts) চালু করুন")
                    }
                }
            }

            item {
                Text("ডাবল-এন্ট্রি জাবেদা (Journal Entries)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "সকল দাখিলায় ডেবিট = ক্রেডিট সমান এবং পোস্ট করা দাখিলা সরাসরি মুছে ফেলা যায় না (সংশোধনে রিভার্সাল এন্ট্রি ব্যবহার করুন)।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (entries.isEmpty()) {
                item {
                    EmptyStateCard(
                        titleBn = "কোনো হিসাব দাখিলা নেই",
                        subtitleBn = "+ বোতামে চাপ দিয়ে আয় বা ব্যয়ের ডাবল-এন্ট্রি দাখিলা যুক্ত করুন।",
                        icon = Icons.Filled.AccountBalance
                    )
                }
            } else {
                items(entries, key = { it.id }) { je ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(je.description, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text(BengaliFormat.formatTaka(je.amount), fontWeight = FontWeight.Bold, color = IslamicEmerald)
                            }
                            Text("ভাউচার: ${je.referenceNo} • তারিখ: ${BengaliFormat.formatDateBn(je.entryDate)}", style = MaterialTheme.typography.bodySmall)
                            Text("ডেবিট: ${je.debitAccountName}  |  ক্রেডিট: ${je.creditAccountName}", style = MaterialTheme.typography.bodySmall)
                            if (je.entryType == "STANDARD") {
                                TextButton(onClick = { reverseTarget = je }) {
                                    Icon(Icons.Filled.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("রিভার্সাল এন্ট্রি তৈরি করুন")
                                }
                            } else {
                                Text("সংশোধনী রিভার্সাল এন্ট্রি", style = MaterialTheme.typography.labelSmall, color = StatusAbsent)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewEntry) {
        var description by remember { mutableStateOf("") }
        var amountStr by remember { mutableStateOf("") }
        var isExpense by remember { mutableStateOf(true) }
        var useBank by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showNewEntry = false },
            title = { Text("নতুন ডাবল-এন্ট্রি হিসাব দাখিলা", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = isExpense, onClick = { isExpense = true }, label = { Text("ব্যয় দাখিলা") })
                        FilterChip(selected = !isExpense, onClick = { isExpense = false }, label = { Text("আয় দাখিলা") })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = useBank, onCheckedChange = { useBank = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (useBank) "ব্যাংক হিসাব (1002)" else "নগদ তহবিল (1001)")
                    }
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("বিবরণ (যেমন: বিদ্যুৎ বিল / খাদ্য ব্যয়) *") })
                    OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("টাকার পরিমাণ (৳) *") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountStr.toDoubleOrNull() ?: 0.0
                        if (description.isNotBlank() && amt > 0.0) {
                            val cashBankCode = if (useBank) "1002" else "1001"
                            val cashBankName = if (useBank) "ব্যাংক হিসাব (Bank)" else "নগদ তহবিল (Cash)"
                            if (isExpense) {
                                viewModel.postJournalEntry(
                                    description = description.trim(),
                                    category = "OPERATIONAL_EXPENSE",
                                    debitCode = "5002",
                                    debitName = "মাদরাসা পরিচালনা ও লিল্লাহ বোর্ডিং ব্যয়",
                                    creditCode = cashBankCode,
                                    creditName = cashBankName,
                                    amount = amt
                                )
                            } else {
                                viewModel.postJournalEntry(
                                    description = description.trim(),
                                    category = "OTHER_INCOME",
                                    debitCode = cashBankCode,
                                    debitName = cashBankName,
                                    creditCode = "4001",
                                    creditName = "বেতন ও ফি আদায় আয়",
                                    amount = amt
                                )
                            }
                            showNewEntry = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald)
                ) { Text("পোস্ট করুন") }
            },
            dismissButton = { TextButton(onClick = { showNewEntry = false }) { Text("বাতিল") } }
        )
    }

    reverseTarget?.let { entry ->
        var reason by remember { mutableStateOf("ভুল অঙ্ক সংশোধন") }
        AlertDialog(
            onDismissRequest = { reverseTarget = null },
            title = { Text("রিভার্সাল এন্ট্রি নিশ্চিতকরণ", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${entry.referenceNo} (${BengaliFormat.formatTaka(entry.amount)}) এর বিপরীত দাখিলা পোস্ট করা হবে।")
                    OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("সংশোধনের কারণ") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reverseJournalEntry(entry, reason.trim().ifBlank { "সংশোধন" })
                        reverseTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusAbsent)
                ) { Text("রিভার্সাল পোস্ট করুন") }
            },
            dismissButton = { TextButton(onClick = { reverseTarget = null }) { Text("বাতিল") } }
        )
    }
}

@Composable
fun DonationsScreen(viewModel: MadrasaViewModel) {
    val context = LocalContext.current
    val donationsState by viewModel.donationsState.collectAsStateWithLifecycle()
    val donations = (donationsState as? UiState.Success)?.data.orEmpty()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAdd = true },
                containerColor = IslamicEmerald,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_donation_fab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "নতুন অনুদান গ্রহণ")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            MadrasaStatCard(
                titleBn = "মোট অনুদান সংগ্রহ",
                valueBn = BengaliFormat.formatTaka(donations.sumOf { it.amount }),
                icon = Icons.Filled.VolunteerActivism,
                accentColor = IslamicGold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (donations.isEmpty()) {
                EmptyStateCard(
                    titleBn = "কোনো অনুদান রেকর্ড নেই",
                    subtitleBn = "+ বোতামে চাপ দিয়ে দাতার অনুদান ও রসিদ তৈরি করুন।",
                    icon = Icons.Filled.VolunteerActivism
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 84.dp)) {
                    items(donations, key = { it.id }) { don ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(don.donorName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text(BengaliFormat.formatTaka(don.amount), fontWeight = FontWeight.Bold, color = IslamicEmerald)
                                }
                                Text("উদ্দেশ্য: ${don.purpose} • মাধ্যম: ${don.paymentMethod}", style = MaterialTheme.typography.bodySmall)
                                Text("রসিদ: ${don.receiptNumber} • মোবাইল: ${don.phone} • তারিখ: ${BengaliFormat.formatDateBn(don.donationDate)}", style = MaterialTheme.typography.bodySmall)
                                OutlinedButton(onClick = {
                                    val res = BengaliReportExporter.generateBengaliPdf(
                                        context = context,
                                        fileNamePrefix = "donation_${don.receiptNumber}",
                                        titleBn = "অনুদান প্রাপ্তির রসিদ",
                                        subtitleBn = don.receiptNumber,
                                        sections = listOf(
                                            ReportSection(
                                                headingBn = "দাতা ও অনুদানের বিবরণ",
                                                rows = listOf(
                                                    listOf("দাতার নাম: ${don.donorName}", "মোবাইল: ${don.phone}"),
                                                    listOf("অনুদানের পরিমাণ: ${BengaliFormat.formatTaka(don.amount)}", "পদ্ধতি: ${don.paymentMethod}"),
                                                    listOf("উদ্দেশ্য: ${don.purpose}", "তারিখ: ${BengaliFormat.formatDateBn(don.donationDate)}")
                                                )
                                            )
                                        )
                                    )
                                    res.onSuccess { file -> BengaliReportExporter.printPdfFile(context, file, don.receiptNumber) }
                                }) {
                                    Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("রসিদ প্রিন্ট করুন")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        var donorName by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var amountStr by remember { mutableStateOf("") }
        var purpose by remember { mutableStateOf("লিল্লাহ বোর্ডিং ও এতিম তহবিল") }
        var method by remember { mutableStateOf("নগদ") }

        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("নতুন অনুদান গ্রহণ", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = donorName, onValueChange = { donorName = it }, label = { Text("দাতার নাম *") }, singleLine = true)
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("মোবাইল নম্বর *") }, singleLine = true)
                    OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("অনুদানের পরিমাণ (৳) *") }, singleLine = true)
                    OutlinedTextField(value = purpose, onValueChange = { purpose = it }, label = { Text("অনুদানের উদ্দেশ্য") })
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("নগদ", "ব্যাংক", "বিকাশ", "নগদ অনলাইন").forEach { m ->
                            FilterChip(selected = method == m, onClick = { method = m }, label = { Text(m) })
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountStr.toDoubleOrNull() ?: 0.0
                        if (donorName.isNotBlank() && phone.trim().length >= 5 && amt > 0.0) {
                            viewModel.saveDonation(donorName.trim(), phone.trim(), amt, purpose.trim(), method)
                            showAdd = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald)
                ) { Text("সংরক্ষণ করুন") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("বাতিল") } }
        )
    }
}

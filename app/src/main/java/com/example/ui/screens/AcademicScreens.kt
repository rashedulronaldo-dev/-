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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.BengaliFormat
import com.example.core.UiState
import com.example.data.model.AttendanceStatus
import com.example.data.model.Department
import com.example.data.model.QuranProgress
import com.example.data.model.Student
import com.example.data.model.UserRole
import com.example.export.BengaliReportExporter
import com.example.export.ReportSection
import com.example.ui.components.ArabicQuranBlock
import com.example.ui.components.AttendanceStatusChip
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SyncStatusBanner
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.NotoNaskhArabicFamily
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusAbsentBg
import com.example.ui.theme.StatusPresent
import com.example.ui.theme.StatusPresentBg
import com.example.ui.viewmodel.MadrasaViewModel

@Composable
fun AttendanceScreen(viewModel: MadrasaViewModel) {
    val role by viewModel.effectiveRole.collectAsStateWithLifecycle()
    val studentsState by viewModel.studentsState.collectAsStateWithLifecycle()
    val teachersState by viewModel.teachersState.collectAsStateWithLifecycle()
    val attendanceState by viewModel.attendanceState.collectAsStateWithLifecycle()
    val offlineQueue by viewModel.offlineAttendanceQueue.collectAsStateWithLifecycle()
    val pendingCount by viewModel.offlinePendingCount.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val students = (studentsState as? UiState.Success)?.data.orEmpty().filter { !it.archived }
    val teachers = (teachersState as? UiState.Success)?.data.orEmpty().filter { it.active }
    val records = (attendanceState as? UiState.Success)?.data.orEmpty()

    var selectedTab by remember { mutableStateOf(0) } // 0 = শিক্ষার্থী, 1 = শিক্ষক, 2 = অফলাইন কিউ
    var dateKey by remember { mutableStateOf(BengaliFormat.todayDateKey()) }
    var selectedDept by remember { mutableStateOf<Department?>(null) }
    var lateArrival by remember { mutableStateOf(false) }
    var auditNote by remember { mutableStateOf("") }

    val canMark = role == UserRole.ADMIN || role == UserRole.TEACHER

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        SyncStatusBanner(
            isOnline = viewModel.isNetworkOnline() && !settings.offlineFirstAttendance,
            pendingCount = pendingCount,
            onSyncNow = { viewModel.syncOfflineAttendanceNow() }
        )
        Spacer(modifier = Modifier.height(10.dp))

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("শিক্ষার্থী হাজিরা") })
            if (role == UserRole.ADMIN) {
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("শিক্ষক হাজিরা") })
            }
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("অফলাইন কিউ (${BengaliFormat.toBnNumber(offlineQueue.size)})") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedTab) {
            0 -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = dateKey,
                        onValueChange = { dateKey = it },
                        label = { Text("তারিখ (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    if (canMark) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("বিলম্বিত", style = MaterialTheme.typography.labelSmall)
                            Switch(checked = lateArrival, onCheckedChange = { lateArrival = it })
                        }
                    }
                }
                if (canMark) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = auditNote,
                        onValueChange = { auditNote = it },
                        label = { Text("সংশোধন / অডিট মন্তব্য (ঐচ্ছিক)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(selected = selectedDept == null, onClick = { selectedDept = null }, label = { Text("সকল বিভাগ") })
                    Department.entries.forEach { d ->
                        FilterChip(selected = selectedDept == d, onClick = { selectedDept = d }, label = { Text(d.labelBn) })
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                val filteredStudents = students.filter { selectedDept == null || it.department == selectedDept?.name }
                if (filteredStudents.isEmpty()) {
                    EmptyStateCard(
                        titleBn = "কোনো শিক্ষার্থী পাওয়া যায়নি",
                        subtitleBn = "হাজিরা নেওয়ার জন্য প্রথমে শিক্ষার্থী ভর্তি করুন।",
                        icon = Icons.AutoMirrored.Filled.FactCheck
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(filteredStudents, key = { it.id }) { student ->
                            val existingRec = records.firstOrNull {
                                it.dateKey == dateKey && it.personType == "STUDENT" && it.personId == student.id
                            }
                            val currentStatus = existingRec?.statusEnum
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(student.fullName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                            Text("${student.className} • রোল: ${student.rollNumber}", style = MaterialTheme.typography.bodySmall)
                                        }
                                        if (existingRec != null) {
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = IslamicEmerald.copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = existingRec.statusEnum.labelBn + if (existingRec.lateArrival) " (বিলম্ব)" else "",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = IslamicEmerald,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                    if (canMark) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            AttendanceStatus.entries.forEach { st ->
                                                AttendanceStatusChip(
                                                    status = st,
                                                    selected = currentStatus == st,
                                                    onClick = {
                                                        viewModel.recordAttendance(
                                                            dateKey = dateKey,
                                                            personType = "STUDENT",
                                                            personId = student.id,
                                                            personName = student.fullName,
                                                            department = student.department,
                                                            className = student.className,
                                                            status = st,
                                                            lateArrival = lateArrival,
                                                            auditNote = auditNote,
                                                            readerUids = student.readerUids
                                                        )
                                                    }
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
            1 -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(teachers, key = { it.id }) { t ->
                        val existingRec = records.firstOrNull {
                            it.dateKey == dateKey && it.personType == "TEACHER" && it.personId == t.id
                        }
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(t.fullName, fontWeight = FontWeight.Bold)
                                Text("${t.designation} • ${t.departmentEnum.labelBn}", style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    AttendanceStatus.entries.forEach { st ->
                                        AttendanceStatusChip(
                                            status = st,
                                            selected = existingRec?.statusEnum == st,
                                            onClick = {
                                                viewModel.recordAttendance(
                                                    dateKey = dateKey,
                                                    personType = "TEACHER",
                                                    personId = t.id,
                                                    personName = t.fullName,
                                                    department = t.department,
                                                    className = t.assignedClasses.firstOrNull() ?: t.departmentEnum.labelBn,
                                                    status = st,
                                                    lateArrival = lateArrival,
                                                    auditNote = auditNote,
                                                    readerUids = t.readerUids
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                if (offlineQueue.isEmpty()) {
                    EmptyStateCard(
                        titleBn = "অফলাইন কিউ খালি",
                        subtitleBn = "ইন্টারনেট বিচ্ছিন্ন অবস্থায় নেওয়া সকল হাজিরা এখানে জমা হবে এবং সংযোগ ফিরলে স্বয়ংক্রিয়ভাবে সিঙ্ক হবে।",
                        icon = Icons.Filled.CheckCircle
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(offlineQueue, key = { it.localId }) { q ->
                            Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(q.personName, fontWeight = FontWeight.Bold)
                                        Text("${q.dateKey} • ${AttendanceStatus.fromRaw(q.status).labelBn}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    val statusLabel = when (q.syncStatus) {
                                        "SYNCED" -> "সিঙ্ক সম্পন্ন"
                                        "FAILED" -> "ব্যর্থ"
                                        else -> "অপেক্ষমাণ"
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (q.syncStatus == "SYNCED") StatusPresentBg else IslamicGoldLight
                                    ) {
                                        Text(statusLabel, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
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
fun NooraniScreen(viewModel: MadrasaViewModel) {
    DepartmentQuranModuleScreen(
        viewModel = viewModel,
        department = Department.NOORANI,
        titleBn = "নূরানী বিভাগ ও কায়দা-আমপারা পাঠ",
        lessonTypes = listOf("আরবি হরফ শিক্ষা", "মাখরাজ অনুশীলন", "বানান ও যুক্তাক্ষর", "নূরানী কুরআন পাঠ"),
        showArabicAlphabet = true,
        defaultArabicSample = "أَبْجَدْ هَوَّزْ حُطِّي كَلَمُنْ"
    )
}

@Composable
fun NazeraScreen(viewModel: MadrasaViewModel) {
    DepartmentQuranModuleScreen(
        viewModel = viewModel,
        department = Department.NAZERA,
        titleBn = "নাজেরা বিভাগ — দৈনিক তিলাওয়াত ও সংশোধন",
        lessonTypes = listOf("দৈনিক নাজেরা পাঠ", "পুনরাবৃত্তি", "তাজবিদ মূল্যায়ন"),
        showArabicAlphabet = false,
        defaultArabicSample = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ۝ الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ"
    )
}

@Composable
fun HifzScreen(viewModel: MadrasaViewModel) {
    DepartmentQuranModuleScreen(
        viewModel = viewModel,
        department = Department.HIFZ,
        titleBn = "হিফজ বিভাগ — সবক, সবকি ও মনজিল",
        lessonTypes = listOf("সবক", "সবকি", "মনজিল", "সাপ্তাহিক দোহরাই"),
        showArabicAlphabet = false,
        defaultArabicSample = "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ"
    )
}

@Composable
private fun DepartmentQuranModuleScreen(
    viewModel: MadrasaViewModel,
    department: Department,
    titleBn: String,
    lessonTypes: List<String>,
    showArabicAlphabet: Boolean,
    defaultArabicSample: String
) {
    val context = LocalContext.current
    val role by viewModel.effectiveRole.collectAsStateWithLifecycle()
    val studentsState by viewModel.studentsState.collectAsStateWithLifecycle()
    val progressState by viewModel.quranProgressState.collectAsStateWithLifecycle()

    val students = (studentsState as? UiState.Success)?.data.orEmpty()
        .filter { !it.archived && it.department == department.name }
    val records = (progressState as? UiState.Success)?.data.orEmpty()
        .filter { it.department == department.name }

    var showDialog by remember { mutableStateOf(false) }
    var filterWeakOnly by remember { mutableStateOf(false) }
    val canEdit = role == UserRole.ADMIN || role == UserRole.TEACHER

    val displayed = records.filter { !filterWeakOnly || it.weakLesson }

    Scaffold(
        floatingActionButton = {
            if (canEdit) {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = IslamicEmerald,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_quran_progress_fab")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "নতুন পাঠ সংরক্ষণ")
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
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = IslamicEmerald),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(titleBn, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "শিক্ষার্থী: ${BengaliFormat.toBnNumber(students.size)} জন  •  মোট পাঠ রেকর্ড: ${BengaliFormat.toBnNumber(records.size)}টি",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicGoldLight
                        )
                    }
                }
            }

            item {
                ArabicQuranBlock(
                    arabicText = defaultArabicSample,
                    captionBn = "কুরআনুল কারীম (RTL আরবি পাঠ)"
                )
            }

            if (showArabicAlphabet) {
                item {
                    Text("নূরানী আরবি হরফ পরিচিতি", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(BengaliFormat.nooraniArabicLetters) { (arabic, nameBn) ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, IslamicGold.copy(alpha = 0.5f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = arabic,
                                            fontFamily = NotoNaskhArabicFamily,
                                            fontSize = 24.sp,
                                            color = IslamicEmerald
                                        )
                                        Text(nameBn, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = filterWeakOnly,
                        onClick = { filterWeakOnly = !filterWeakOnly },
                        label = { Text("দুর্বল পাঠ (${BengaliFormat.toBnNumber(records.count { it.weakLesson })})") }
                    )
                    OutlinedButton(
                        onClick = {
                            val res = BengaliReportExporter.generateBengaliPdf(
                                context = context,
                                fileNamePrefix = "${department.name.lowercase()}_progress",
                                titleBn = titleBn,
                                subtitleBn = "মাসিক অগ্রগতি প্রতিবেদন",
                                sections = listOf(
                                    ReportSection(
                                        headingBn = "${department.labelBn}-এর দৈনিক পাঠ তালিকা",
                                        arabicLine = defaultArabicSample,
                                        rows = displayed.map { r ->
                                            listOf(
                                                r.studentName,
                                                r.lessonType,
                                                "${r.surahName} (পারা ${BengaliFormat.toBnNumber(r.paraNumber)}, আয়াত ${r.ayatRange})",
                                                "নির্ভুলতা: ${BengaliFormat.toBnNumber(r.accuracyPercent)}%",
                                                "ভুল: ${BengaliFormat.toBnNumber(r.mistakeCount)}"
                                            )
                                        }.ifEmpty { listOf(listOf("কোনো পাঠ রেকর্ড পাওয়া যায়নি")) }
                                    )
                                )
                            )
                            res.onSuccess { file ->
                                BengaliReportExporter.shareFile(context, file, "application/pdf", "প্রতিবেদন শেয়ার করুন")
                            }.onFailure {
                                viewModel.emitMessage("পিডিএফ তৈরি ব্যর্থ হয়েছে।")
                            }
                        }
                    ) {
                        Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("অগ্রগতি পিডিএফ")
                    }
                }
            }

            if (displayed.isEmpty()) {
                item {
                    EmptyStateCard(
                        titleBn = "কোনো পাঠ অগ্রগতি নেই",
                        subtitleBn = if (canEdit) "+ বোতামে চাপ দিয়ে দৈনিক পাঠের হিসাব লিখুন।" else "শিক্ষক পাঠ অনুমোদন করলে এখানে দেখা যাবে।",
                        icon = if (department == Department.HIFZ) Icons.Filled.Mosque else Icons.AutoMirrored.Filled.MenuBook
                    )
                }
            } else {
                items(displayed, key = { it.id }) { item ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            1.dp,
                            if (item.weakLesson) StatusAbsent.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.studentName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (item.weakLesson) StatusAbsentBg else StatusPresentBg
                                ) {
                                    Text(
                                        text = "${item.lessonType} • ${if (item.approved) "অনুমোদিত" else "অপেক্ষমাণ"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (item.weakLesson) StatusAbsent else StatusPresent,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Text(
                                "সূরা: ${item.surahName} • পারা: ${BengaliFormat.toBnNumber(item.paraNumber)} • আয়াত: ${item.ayatRange}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (!item.arabicSample.isNullOrBlank()) {
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                    Text(
                                        text = item.arabicSample,
                                        fontFamily = NotoNaskhArabicFamily,
                                        fontSize = 18.sp,
                                        color = IslamicEmerald,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                            Text(
                                "নির্ভুলতা: ${BengaliFormat.toBnNumber(item.accuracyPercent)}% • ভুলের সংখ্যা: ${BengaliFormat.toBnNumber(item.mistakeCount)}টি • তারিখ: ${BengaliFormat.formatDateBn(item.dateKey)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (!item.teacherComment.isNullOrBlank()) {
                                Text("শিক্ষকের মন্তব্য: ${item.teacherComment}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (item.weakLesson) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Warning, contentDescription = null, tint = StatusAbsent, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("দুর্বল পাঠ হিসেবে চিহ্নিত — অতিরিক্ত পুনরাবৃত্তি প্রয়োজন", style = MaterialTheme.typography.labelSmall, color = StatusAbsent)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        QuranProgressEntryDialog(
            department = department,
            students = students,
            lessonTypes = lessonTypes,
            defaultArabicSample = defaultArabicSample,
            onDismiss = { showDialog = false },
            onSave = { prog ->
                viewModel.saveQuranProgress(prog)
                showDialog = false
            }
        )
    }
}

@Composable
private fun QuranProgressEntryDialog(
    department: Department,
    students: List<Student>,
    lessonTypes: List<String>,
    defaultArabicSample: String,
    onDismiss: () -> Unit,
    onSave: (QuranProgress) -> Unit
) {
    var selectedStudent by remember { mutableStateOf(students.firstOrNull()) }
    var lessonType by remember { mutableStateOf(lessonTypes.first()) }
    var surahName by remember { mutableStateOf("সূরা আল-বাকারা") }
    var paraStr by remember { mutableStateOf("1") }
    var ayatRange by remember { mutableStateOf("১-১০") }
    var arabicSample by remember { mutableStateOf(defaultArabicSample) }
    var accuracyStr by remember { mutableStateOf("95") }
    var mistakesStr by remember { mutableStateOf("1") }
    var comment by remember { mutableStateOf("") }
    var approved by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${department.labelBn} — দৈনিক পাঠ এন্ট্রি", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (students.isEmpty()) {
                    Text("এই বিভাগে কোনো শিক্ষার্থী পাওয়া যায়নি। প্রথমে শিক্ষার্থী ভর্তি করুন।", color = StatusAbsent)
                } else {
                    Text("শিক্ষার্থী নির্বাচন করুন:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        students.forEach { st ->
                            FilterChip(
                                selected = selectedStudent?.id == st.id,
                                onClick = { selectedStudent = st },
                                label = { Text(st.fullName) }
                            )
                        }
                    }
                }

                Text("পাঠের ধরন:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    lessonTypes.forEach { lt ->
                        FilterChip(selected = lessonType == lt, onClick = { lessonType = lt }, label = { Text(lt) })
                    }
                }

                OutlinedTextField(value = surahName, onValueChange = { surahName = it }, label = { Text("সূরার নাম / কায়দা পাঠ") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = paraStr, onValueChange = { paraStr = it }, label = { Text("পারা (০-৩০)") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = ayatRange, onValueChange = { ayatRange = it }, label = { Text("আয়াত পরিসর") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = accuracyStr, onValueChange = { accuracyStr = it }, label = { Text("নির্ভুলতা (%)") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = mistakesStr, onValueChange = { mistakesStr = it }, label = { Text("ভুলের সংখ্যা") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = arabicSample, onValueChange = { arabicSample = it }, label = { Text("আরবি আয়াত / অংশ (ঐচ্ছিক)") })
                OutlinedTextField(value = comment, onValueChange = { comment = it }, label = { Text("শিক্ষকের মন্তব্য") })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = approved, onCheckedChange = { approved = it })
                    Text("শিক্ষকের অনুমোদন সম্পন্ন")
                }
            }
        },
        confirmButton = {
            Button(
                enabled = selectedStudent != null,
                onClick = {
                    val stu = selectedStudent ?: return@Button
                    val para = (paraStr.toIntOrNull() ?: 1).coerceIn(0, 30)
                    val acc = (accuracyStr.toIntOrNull() ?: 90).coerceIn(0, 100)
                    val mistakes = (mistakesStr.toIntOrNull() ?: 0).coerceIn(0, 500)
                    onSave(
                        QuranProgress(
                            studentId = stu.id,
                            studentName = stu.fullName,
                            department = department.name,
                            dateKey = BengaliFormat.todayDateKey(),
                            lessonType = lessonType,
                            surahName = surahName.trim().ifBlank { "কুরআন পাঠ" },
                            paraNumber = para,
                            ayatRange = ayatRange.trim().ifBlank { "১-৫" },
                            arabicSample = arabicSample.trim().ifBlank { null },
                            accuracyPercent = acc,
                            mistakeCount = mistakes,
                            weakLesson = mistakes >= 5 || acc < 70,
                            approved = approved,
                            teacherComment = comment.trim().ifBlank { null },
                            readerUids = stu.readerUids
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald)
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}

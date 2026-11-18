package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.BengaliFormat
import com.example.core.UiState
import com.example.data.model.AttendanceStatus
import com.example.data.model.Department
import com.example.data.model.UserRole
import com.example.ui.components.DepartmentBadge
import com.example.ui.components.DepartmentDistributionChart
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.IncomeExpenseChart
import com.example.ui.components.MadrasaStatCard
import com.example.ui.components.MonthlyAttendanceChart
import com.example.ui.components.SyncStatusBanner
import com.example.ui.navigation.AppRoute
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusPresent
import com.example.ui.viewmodel.MadrasaViewModel

@Composable
fun AdminDashboardScreen(
    viewModel: MadrasaViewModel,
    onNavigate: (AppRoute) -> Unit
) {
    val metrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val org by viewModel.organization.collectAsStateWithLifecycle()
    val member by viewModel.currentMember.collectAsStateWithLifecycle()
    val previewRole by viewModel.previewRoleOverride.collectAsStateWithLifecycle()
    val attendanceState by viewModel.attendanceState.collectAsStateWithLifecycle()
    val noticesState by viewModel.noticesState.collectAsStateWithLifecycle()
    val auditState by viewModel.auditLogsState.collectAsStateWithLifecycle()
    val pendingSync by viewModel.offlinePendingCount.collectAsStateWithLifecycle()

    val monthPrefix = BengaliFormat.currentMonthKey()
    val monthlyAttendance = (attendanceState as? UiState.Success)?.data
        .orEmpty()
        .filter { it.personType == "STUDENT" && it.dateKey.startsWith(monthPrefix) }
    val notices = (noticesState as? UiState.Success)?.data.orEmpty().take(3)
    val audits = (auditState as? UiState.Success)?.data.orEmpty().take(5)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(IslamicEmeraldDark, IslamicEmerald)))
                        .padding(18.dp)
                ) {
                    Column {
                        Text(
                            text = org?.nameBn ?: "খেলার মাঠ নূরানী তালিমুল কুরআন ও হাফিজিয়া মাদরাসা",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "প্রতিষ্ঠিত: ${org?.establishedYear ?: "১৯৯৫"}  •  শিক্ষাবর্ষ: ${org?.academicYear ?: "২০২৬"}  •  ${BengaliFormat.currentMonthLabelBn()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicGoldLight
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(shape = RoundedCornerShape(50), color = IslamicGold.copy(alpha = 0.25f)) {
                                Text(
                                    text = "নগদ: ${BengaliFormat.formatTaka(metrics.cashBalance)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                            Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.16f)) {
                                Text(
                                    text = "ব্যাংক: ${BengaliFormat.formatTaka(metrics.bankBalance)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (member?.roleEnum == UserRole.ADMIN) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ভিউ পরিবর্তন:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    UserRole.entries.forEach { role ->
                        FilterChip(
                            selected = (previewRole ?: UserRole.ADMIN) == role,
                            onClick = {
                                viewModel.setRolePreview(if (role == UserRole.ADMIN) null else role)
                                onNavigate(AppRoute.homeFor(role))
                            },
                            label = { Text(role.labelBn) }
                        )
                    }
                }
            }
        }

        item {
            SyncStatusBanner(
                isOnline = viewModel.isNetworkOnline(),
                pendingCount = pendingSync,
                onSyncNow = { viewModel.syncOfflineAttendanceNow() }
            )
        }

        item {
            Text("দ্রুত কার্যক্রম", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionChip("নতুন ভর্তি", Icons.Filled.PersonAdd, "quick_admit_btn") { onNavigate(AppRoute.STUDENTS) }
                QuickActionChip("হাজিরা নিন", Icons.AutoMirrored.Filled.FactCheck, "quick_attendance_btn") { onNavigate(AppRoute.ATTENDANCE) }
                QuickActionChip("বেতন আদায়", Icons.Filled.Payments, "quick_fees_btn") { onNavigate(AppRoute.FEES) }
                QuickActionChip("অনুদান গ্রহণ", Icons.Filled.VolunteerActivism, "quick_donation_btn") { onNavigate(AppRoute.DONATIONS) }
                QuickActionChip("নোটিশ প্রকাশ", Icons.Filled.Campaign, "quick_notice_btn") { onNavigate(AppRoute.NOTICES) }
                QuickActionChip("পিডিএফ প্রতিবেদন", Icons.Filled.Assessment, "quick_report_btn") { onNavigate(AppRoute.REPORTS) }
            }
        }

        // 10 Real-Time Database KPI Cards in 2-column grid rows
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MadrasaStatCard(
                        titleBn = "মোট শিক্ষার্থী",
                        valueBn = "${BengaliFormat.toBnNumber(metrics.totalStudents)} জন",
                        icon = Icons.Filled.Groups,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppRoute.STUDENTS) }
                    )
                    MadrasaStatCard(
                        titleBn = "মোট শিক্ষক",
                        valueBn = "${BengaliFormat.toBnNumber(metrics.totalTeachers)} জন",
                        icon = Icons.Filled.Badge,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppRoute.TEACHERS) }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MadrasaStatCard(
                        titleBn = "আজকের উপস্থিতি",
                        valueBn = "${BengaliFormat.toBnNumber(metrics.todayPresent)} জন",
                        icon = Icons.Filled.CheckCircle,
                        accentColor = StatusPresent,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppRoute.ATTENDANCE) }
                    )
                    MadrasaStatCard(
                        titleBn = "আজকের অনুপস্থিতি",
                        valueBn = "${BengaliFormat.toBnNumber(metrics.todayAbsent)} জন",
                        icon = Icons.Filled.Warning,
                        accentColor = StatusAbsent,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppRoute.ATTENDANCE) }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MadrasaStatCard(
                        titleBn = "মোট বেতন আদায়",
                        valueBn = BengaliFormat.formatTaka(metrics.totalFeeCollected),
                        icon = Icons.Filled.AccountBalanceWallet,
                        accentColor = IslamicEmerald,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppRoute.FEES) }
                    )
                    MadrasaStatCard(
                        titleBn = "মোট বকেয়া",
                        valueBn = BengaliFormat.formatTaka(metrics.totalFeeDue),
                        icon = Icons.Filled.MoneyOff,
                        accentColor = StatusAbsent,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppRoute.FEES) }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MadrasaStatCard(
                        titleBn = "মাসিক আয়",
                        valueBn = BengaliFormat.formatTaka(metrics.monthlyIncome),
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        accentColor = StatusPresent,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppRoute.ACCOUNTING) }
                    )
                    MadrasaStatCard(
                        titleBn = "মাসিক ব্যয়",
                        valueBn = BengaliFormat.formatTaka(metrics.monthlyExpense),
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        accentColor = StatusAbsent,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppRoute.ACCOUNTING) }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MadrasaStatCard(
                        titleBn = "নতুন ভর্তি (চলতি মাস)",
                        valueBn = "${BengaliFormat.toBnNumber(metrics.newAdmissionsThisMonth)} জন",
                        icon = Icons.Filled.PersonAdd,
                        accentColor = IslamicGold,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppRoute.STUDENTS) }
                    )
                    MadrasaStatCard(
                        titleBn = "মোট অনুদান",
                        valueBn = BengaliFormat.formatTaka(metrics.totalDonations),
                        icon = Icons.Filled.VolunteerActivism,
                        accentColor = IslamicGold,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(AppRoute.DONATIONS) }
                    )
                }
            }
        }

        item {
            MonthlyAttendanceChart(
                presentCount = monthlyAttendance.count { it.status == AttendanceStatus.PRESENT.name },
                absentCount = monthlyAttendance.count { it.status == AttendanceStatus.ABSENT.name },
                leaveCount = monthlyAttendance.count { it.status == AttendanceStatus.LEAVE.name },
                sickCount = monthlyAttendance.count { it.status == AttendanceStatus.SICK.name }
            )
        }

        item {
            IncomeExpenseChart(
                monthlyIncome = metrics.monthlyIncome,
                monthlyExpense = metrics.monthlyExpense
            )
        }

        item {
            DepartmentDistributionChart(
                nooraniCount = metrics.nooraniCount,
                nazeraCount = metrics.nazeraCount,
                hifzCount = metrics.hifzCount
            )
        }

        item {
            Text("সর্বশেষ নোটিশ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        if (notices.isEmpty()) {
            item {
                EmptyStateCard(
                    titleBn = "কোনো নোটিশ প্রকাশিত হয়নি",
                    subtitleBn = "শিক্ষক ও অভিভাবকদের জানাতে নতুন নোটিশ প্রকাশ করুন।",
                    icon = Icons.Filled.Campaign
                )
            }
        } else {
            items(notices) { notice ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(AppRoute.NOTICES) },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(notice.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(notice.body, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${BengaliFormat.formatDateBn(notice.publishedDate)} • ${notice.audienceEnum.labelBn}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Text("সাম্প্রতিক কার্যক্রম", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        if (audits.isEmpty()) {
            item {
                EmptyStateCard(
                    titleBn = "এখনও কোনো কার্যক্রম রেকর্ড হয়নি",
                    subtitleBn = "ভর্তি, হাজিরা, বেতন বা অনুদান যুক্ত করলে এখানে দেখা যাবে।",
                    icon = Icons.Filled.History
                )
            }
        } else {
            items(audits) { log ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.History, contentDescription = null, tint = IslamicEmerald, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(log.action, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text(log.summary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionChip(
    labelBn: String,
    icon: ImageVector,
    tag: String,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        modifier = Modifier
            .height(48.dp)
            .testTag(tag),
        label = { Text(labelBn, fontWeight = FontWeight.SemiBold) },
        leadingIcon = { Icon(icon, contentDescription = labelBn, tint = IslamicEmerald, modifier = Modifier.size(18.dp)) }
    )
}

@Composable
fun TeacherDashboardScreen(
    viewModel: MadrasaViewModel,
    onNavigate: (AppRoute) -> Unit
) {
    val member by viewModel.currentMember.collectAsStateWithLifecycle()
    val studentsState by viewModel.studentsState.collectAsStateWithLifecycle()
    val quranState by viewModel.quranProgressState.collectAsStateWithLifecycle()
    val noticesState by viewModel.noticesState.collectAsStateWithLifecycle()
    val pendingSync by viewModel.offlinePendingCount.collectAsStateWithLifecycle()
    val previewRole by viewModel.previewRoleOverride.collectAsStateWithLifecycle()

    val assignedClasses = member?.assignedClasses.orEmpty()
    val allStudents = (studentsState as? UiState.Success)?.data.orEmpty().filter { !it.archived }
    val myStudents = if (assignedClasses.isEmpty()) allStudents else allStudents.filter { it.className in assignedClasses }
    val todayProgress = (quranState as? UiState.Success)?.data.orEmpty().filter { it.dateKey == BengaliFormat.todayDateKey() }
    val notices = (noticesState as? UiState.Success)?.data.orEmpty().take(4)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (member?.roleEnum == UserRole.ADMIN && previewRole != null) {
            item {
                AssistChip(
                    onClick = {
                        viewModel.setRolePreview(null)
                        onNavigate(AppRoute.ADMIN_DASHBOARD)
                    },
                    label = { Text("অ্যাডমিন ড্যাশবোর্ডে ফিরে যান") }
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = IslamicEmerald),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("শিক্ষক প্যানেল — আসসালামু আলাইকুম", color = IslamicGoldLight, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        member?.displayName ?: "সম্মানিত উস্তাদ",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "দায়িত্বপ্রাপ্ত শ্রেণি: ${if (assignedClasses.isEmpty()) "সকল শ্রেণি" else assignedClasses.joinToString(", ")}",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            SyncStatusBanner(
                isOnline = viewModel.isNetworkOnline(),
                pendingCount = pendingSync,
                onSyncNow = { viewModel.syncOfflineAttendanceNow() }
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MadrasaStatCard(
                    titleBn = "নির্ধারিত শিক্ষার্থী",
                    valueBn = "${BengaliFormat.toBnNumber(myStudents.size)} জন",
                    icon = Icons.Filled.Groups,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(AppRoute.STUDENTS) }
                )
                MadrasaStatCard(
                    titleBn = "আজকের সবক/পাঠ",
                    valueBn = "${BengaliFormat.toBnNumber(todayProgress.size)}টি",
                    icon = Icons.Filled.Mosque,
                    accentColor = IslamicGold,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(AppRoute.HIFZ) }
                )
            }
        }

        item {
            Text("শ্রেণি ও পাঠ কার্যক্রম", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionChip("দৈনিক হাজিরা", Icons.AutoMirrored.Filled.FactCheck, "teacher_att_btn") { onNavigate(AppRoute.ATTENDANCE) }
                QuickActionChip("নূরানী পাঠ", Icons.Filled.Groups, "teacher_noorani_btn") { onNavigate(AppRoute.NOORANI) }
                QuickActionChip("নাজেরা পাঠ", Icons.Filled.Groups, "teacher_nazera_btn") { onNavigate(AppRoute.NAZERA) }
                QuickActionChip("হিফজ সবক/সবকি", Icons.Filled.Mosque, "teacher_hifz_btn") { onNavigate(AppRoute.HIFZ) }
                QuickActionChip("পরীক্ষার নম্বর", Icons.Filled.Assessment, "teacher_exam_btn") { onNavigate(AppRoute.EXAMINATIONS) }
            }
        }

        item {
            Text("দায়িত্বপ্রাপ্ত শিক্ষার্থীবৃন্দ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        if (myStudents.isEmpty()) {
            item {
                EmptyStateCard(
                    titleBn = "কোনো শিক্ষার্থী নির্ধারিত নেই",
                    subtitleBn = "অ্যাডমিন প্যানেল থেকে শিক্ষার্থী ও শ্রেণি যুক্ত করা হলে এখানে দেখা যাবে।",
                    icon = Icons.Filled.Groups
                )
            }
        } else {
            items(myStudents.take(8)) { student ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(student.fullName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("শ্রেণি: ${student.className} • রোল: ${student.rollNumber}", style = MaterialTheme.typography.bodySmall)
                        }
                        DepartmentBadge(student.departmentEnum)
                    }
                }
            }
        }

        item {
            Text("নোটিশ বোর্ড", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(notices) { notice ->
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(notice.title, fontWeight = FontWeight.Bold)
                    Text(notice.body, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun ParentDashboardScreen(
    viewModel: MadrasaViewModel,
    onNavigate: (AppRoute) -> Unit
) {
    val member by viewModel.currentMember.collectAsStateWithLifecycle()
    val previewRole by viewModel.previewRoleOverride.collectAsStateWithLifecycle()
    val studentsState by viewModel.studentsState.collectAsStateWithLifecycle()
    val attendanceState by viewModel.attendanceState.collectAsStateWithLifecycle()
    val invoicesState by viewModel.invoicesState.collectAsStateWithLifecycle()
    val resultsState by viewModel.examResultsState.collectAsStateWithLifecycle()
    val quranState by viewModel.quranProgressState.collectAsStateWithLifecycle()
    val noticesState by viewModel.noticesState.collectAsStateWithLifecycle()

    val children = (studentsState as? UiState.Success)?.data.orEmpty().filter { !it.archived }
    val attendance = (attendanceState as? UiState.Success)?.data.orEmpty()
    val invoices = (invoicesState as? UiState.Success)?.data.orEmpty()
    val results = (resultsState as? UiState.Success)?.data.orEmpty().filter { it.published }
    val quran = (quranState as? UiState.Success)?.data.orEmpty()
    val notices = (noticesState as? UiState.Success)?.data.orEmpty().take(4)

    val presentCount = attendance.count { it.status == AttendanceStatus.PRESENT.name }
    val attPct = if (attendance.isEmpty()) 0 else (presentCount * 100) / attendance.size
    val totalDue = invoices.sumOf { it.dueAmount }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (member?.roleEnum == UserRole.ADMIN && previewRole != null) {
            item {
                AssistChip(
                    onClick = {
                        viewModel.setRolePreview(null)
                        onNavigate(AppRoute.ADMIN_DASHBOARD)
                    },
                    label = { Text("অ্যাডমিন ড্যাশবোর্ডে ফিরে যান") }
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = IslamicEmerald),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("অভিভাবক পোর্টাল — আসসালামু আলাইকুম", color = IslamicGoldLight, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        member?.displayName ?: "সম্মানিত অভিভাবক",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "আপনার সন্তানের হাজিরা, বেতন, পরীক্ষা এবং হিফজ/নাজেরা অগ্রগতি",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MadrasaStatCard(
                    titleBn = "উপস্থিতির হার",
                    valueBn = "${BengaliFormat.toBnNumber(attPct)}%",
                    icon = Icons.Filled.CheckCircle,
                    accentColor = StatusPresent,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(AppRoute.ATTENDANCE) }
                )
                MadrasaStatCard(
                    titleBn = "মোট বকেয়া ফি",
                    valueBn = BengaliFormat.formatTaka(totalDue),
                    icon = Icons.Filled.Payments,
                    accentColor = if (totalDue > 0) StatusAbsent else IslamicEmerald,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(AppRoute.FEES) }
                )
            }
        }

        item {
            Text("সন্তানের তথ্য", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        if (children.isEmpty()) {
            item {
                EmptyStateCard(
                    titleBn = "কোনো শিক্ষার্থী সংযুক্ত নেই",
                    subtitleBn = "শিক্ষার্থী ব্যবস্থাপনা থেকে নতুন শিক্ষার্থী ভর্তি বা অভিভাবকের সাথে সংযুক্ত করুন।",
                    icon = Icons.Filled.Groups
                )
            }
        } else {
            items(children) { child ->
                val latestQuran = quran.firstOrNull { it.studentId == child.id }
                val latestResult = results.firstOrNull { it.studentId == child.id }
                Card(
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(child.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            DepartmentBadge(child.departmentEnum)
                        }
                        Text("আইডি: ${child.studentCode} • শ্রেণি: ${child.className} • রোল: ${child.rollNumber}", style = MaterialTheme.typography.bodySmall)
                        if (latestQuran != null) {
                            Text(
                                "সর্বশেষ কুরআন পাঠ (${latestQuran.lessonType}): ${latestQuran.surahName} • পারা ${BengaliFormat.toBnNumber(latestQuran.paraNumber)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = IslamicEmerald
                            )
                        }
                        if (latestResult != null) {
                            Text(
                                "সর্বশেষ ফলাফল (${latestResult.examTitle}): ${BengaliFormat.toBnNumber(latestResult.obtainedMarks)}/${BengaliFormat.toBnNumber(latestResult.totalMarks)} (গ্রেড: ${latestResult.grade})",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        item {
            Text("মাদরাসার নোটিশ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(notices) { notice ->
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(notice.title, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(notice.body, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

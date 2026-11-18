package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.BengaliFormat
import com.example.data.model.AttendanceStatus
import com.example.data.model.Department
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicEmeraldContainer
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.MadrasaSage
import com.example.ui.theme.NotoNaskhArabicFamily
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusAbsentBg
import com.example.ui.theme.StatusLeave
import com.example.ui.theme.StatusLeaveBg
import com.example.ui.theme.StatusPresent
import com.example.ui.theme.StatusPresentBg
import com.example.ui.theme.StatusSick
import com.example.ui.theme.StatusSickBg

@Composable
fun MadrasaStatCard(
    titleBn: String,
    valueBn: String,
    icon: ImageVector,
    accentColor: Color = IslamicEmerald,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = titleBn,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(IslamicGold)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = valueBn,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = titleBn,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DepartmentBadge(department: Department, modifier: Modifier = Modifier) {
    val bg = when (department) {
        Department.NOORANI -> IslamicEmeraldContainer
        Department.NAZERA -> IslamicGoldLight
        Department.HIFZ -> Color(0xFFDCEEF8)
    }
    val fg = when (department) {
        Department.NOORANI -> IslamicEmerald
        Department.NAZERA -> Color(0xFF6B4C00)
        Department.HIFZ -> Color(0xFF0C4A6E)
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = bg
    ) {
        Text(
            text = department.labelBn,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun AttendanceStatusChip(
    status: AttendanceStatus,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (fg, bg) = when (status) {
        AttendanceStatus.PRESENT -> StatusPresent to StatusPresentBg
        AttendanceStatus.ABSENT -> StatusAbsent to StatusAbsentBg
        AttendanceStatus.LEAVE -> StatusLeave to StatusLeaveBg
        AttendanceStatus.SICK -> StatusSick to StatusSickBg
    }
    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) fg else bg.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, if (selected) fg else fg.copy(alpha = 0.4f))
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
            Text(
                text = status.labelBn,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) Color.White else fg,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ArabicQuranBlock(
    arabicText: String,
    captionBn: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = IslamicEmeraldContainer.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, IslamicGold.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (!captionBn.isNullOrBlank()) {
                Text(
                    text = captionBn,
                    style = MaterialTheme.typography.labelSmall,
                    color = IslamicEmerald
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    text = arabicText,
                    fontFamily = NotoNaskhArabicFamily,
                    fontSize = 22.sp,
                    lineHeight = 36.sp,
                    color = IslamicEmerald,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun SyncStatusBanner(
    isOnline: Boolean,
    pendingCount: Int,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = when {
        pendingCount > 0 -> StatusLeaveBg
        isOnline -> IslamicEmeraldContainer.copy(alpha = 0.7f)
        else -> StatusAbsentBg
    }
    val fg = when {
        pendingCount > 0 -> StatusLeave
        isOnline -> IslamicEmerald
        else -> StatusAbsent
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = bg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = if (isOnline) Icons.Filled.CloudDone else Icons.Filled.CloudOff,
                    contentDescription = "সিঙ্ক অবস্থা",
                    tint = fg,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = when {
                        pendingCount > 0 -> "অফলাইন সংরক্ষিত: ${BengaliFormat.toBnNumber(pendingCount)}টি সিঙ্ক বাকি"
                        isOnline -> "অনলাইন সিঙ্ক সক্রিয় আছে"
                        else -> "অফলাইন মোড — স্থানীয়ভাবে সংরক্ষিত হবে"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = fg,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (pendingCount > 0) {
                OutlinedButton(
                    onClick = onSyncNow,
                    modifier = Modifier.testTag("sync_now_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = fg),
                    border = BorderStroke(1.dp, fg)
                ) {
                    Icon(Icons.Filled.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("সিঙ্ক করুন", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun BengaliSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholderBn: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .testTag("search_input"),
        placeholder = { Text(placeholderBn) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "খুঁজুন") },
        singleLine = true,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun EmptyStateCard(
    titleBn: String,
    subtitleBn: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(IslamicEmerald.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = titleBn, tint = IslamicEmerald, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = titleBn,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitleBn,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MonthlyAttendanceChart(
    presentCount: Int,
    absentCount: Int,
    leaveCount: Int,
    sickCount: Int,
    modifier: Modifier = Modifier
) {
    val total = (presentCount + absentCount + leaveCount + sickCount).coerceAtLeast(1)
    val bars = listOf(
        Triple("উপস্থিত", presentCount, StatusPresent),
        Triple("অনুপস্থিত", absentCount, StatusAbsent),
        Triple("ছুটি", leaveCount, StatusLeave),
        Triple("অসুস্থ", sickCount, StatusSick)
    )
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("মাসিক হাজিরার চার্ট", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                val barWidth = size.width / (bars.size * 2f)
                val maxVal = bars.maxOf { it.second }.coerceAtLeast(1)
                bars.forEachIndexed { idx, (_, count, color) ->
                    val ratio = count.toFloat() / maxVal.toFloat()
                    val barHeight = (size.height - 16f) * ratio
                    val x = (idx * 2f + 0.5f) * barWidth
                    drawRoundRect(
                        color = color.copy(alpha = 0.16f),
                        topLeft = Offset(x, 0f),
                        size = Size(barWidth, size.height - 10f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f)
                    )
                    if (barHeight > 0f) {
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(x, size.height - 10f - barHeight),
                            size = Size(barWidth, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                bars.forEach { (label, count, color) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = BengaliFormat.toBnNumber(count),
                            style = MaterialTheme.typography.labelLarge,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$label (${BengaliFormat.toBnNumber((count * 100) / total)}%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IncomeExpenseChart(
    monthlyIncome: Double,
    monthlyExpense: Double,
    modifier: Modifier = Modifier
) {
    val maxAmt = maxOf(monthlyIncome, monthlyExpense, 1.0)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("মাসিক আয় ও ব্যয়ের তুলনা", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                val incRatio = (monthlyIncome / maxAmt).toFloat().coerceIn(0f, 1f)
                val expRatio = (monthlyExpense / maxAmt).toFloat().coerceIn(0f, 1f)
                drawRoundRect(
                    color = IslamicEmeraldContainer,
                    topLeft = Offset(0f, 4f),
                    size = Size(size.width, 20f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                )
                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(IslamicEmerald, MadrasaSage)),
                    topLeft = Offset(0f, 4f),
                    size = Size(size.width * incRatio, 20f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                )
                drawRoundRect(
                    color = StatusAbsentBg,
                    topLeft = Offset(0f, 36f),
                    size = Size(size.width, 20f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                )
                drawRoundRect(
                    color = StatusAbsent,
                    topLeft = Offset(0f, 36f),
                    size = Size(size.width * expRatio, 20f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("আয়: ${BengaliFormat.formatTaka(monthlyIncome)}", color = IslamicEmerald, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Text("ব্যয়: ${BengaliFormat.formatTaka(monthlyExpense)}", color = StatusAbsent, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun DepartmentDistributionChart(
    nooraniCount: Int,
    nazeraCount: Int,
    hifzCount: Int,
    modifier: Modifier = Modifier
) {
    val total = (nooraniCount + nazeraCount + hifzCount).coerceAtLeast(1)
    val slices = listOf(
        Triple("নূরানী", nooraniCount, IslamicEmerald),
        Triple("নাজেরা", nazeraCount, IslamicGold),
        Triple("হিফজ", hifzCount, Color(0xFF0284C7))
    )
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Canvas(modifier = Modifier.size(92.dp)) {
                var startAngle = -90f
                val stroke = Stroke(width = 20f, cap = StrokeCap.Butt)
                if (nooraniCount + nazeraCount + hifzCount == 0) {
                    drawArc(
                        color = IslamicEmeraldContainer,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = stroke
                    )
                } else {
                    slices.forEach { (_, count, color) ->
                        val sweep = (count.toFloat() / total.toFloat()) * 360f
                        if (sweep > 0f) {
                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = stroke
                            )
                            startAngle += sweep
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(18.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("বিভাগভিত্তিক শিক্ষার্থী", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                slices.forEach { (label, count, color) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$label: ${BengaliFormat.toBnNumber(count)} জন",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BengaliConfirmDialog(
    titleBn: String,
    messageBn: String,
    confirmTextBn: String = "নিশ্চিত করুন",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titleBn, fontWeight = FontWeight.Bold) },
        text = { Text(messageBn) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmTextBn, color = StatusAbsent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}

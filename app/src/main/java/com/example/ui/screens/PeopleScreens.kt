package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.core.BengaliFormat
import com.example.core.UiState
import com.example.data.model.Department
import com.example.data.model.Guardian
import com.example.data.model.Student
import com.example.data.model.Teacher
import com.example.data.model.UserRole
import com.example.export.BengaliReportExporter
import com.example.export.ReportSection
import com.example.ui.components.BengaliConfirmDialog
import com.example.ui.components.BengaliSearchBar
import com.example.ui.components.DepartmentBadge
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.StatusAbsent
import com.example.ui.viewmodel.MadrasaViewModel

@Composable
fun StudentsScreen(viewModel: MadrasaViewModel) {
    val context = LocalContext.current
    val role by viewModel.effectiveRole.collectAsStateWithLifecycle()
    val studentsState by viewModel.studentsState.collectAsStateWithLifecycle()
    val guardiansState by viewModel.guardiansState.collectAsStateWithLifecycle()
    val guardians = (guardiansState as? UiState.Success)?.data.orEmpty()

    var searchQuery by remember { mutableStateOf("") }
    var selectedDept by remember { mutableStateOf<Department?>(null) }
    var showFormDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    var viewingStudent by remember { mutableStateOf<Student?>(null) }
    var deletingStudent by remember { mutableStateOf<Student?>(null) }

    val allStudents = (studentsState as? UiState.Success)?.data.orEmpty().filter { !it.archived }
    val filtered = remember(allStudents, searchQuery, selectedDept) {
        allStudents.filter { s ->
            val matchesDept = selectedDept == null || s.department == selectedDept?.name
            val matchesQuery = searchQuery.isBlank() ||
                s.fullName.contains(searchQuery, ignoreCase = true) ||
                s.studentCode.contains(searchQuery, ignoreCase = true) ||
                s.fatherName.contains(searchQuery, ignoreCase = true) ||
                s.guardianPhone.contains(searchQuery, ignoreCase = true)
            matchesDept && matchesQuery
        }
    }

    Scaffold(
        floatingActionButton = {
            if (role == UserRole.ADMIN) {
                FloatingActionButton(
                    onClick = {
                        editingStudent = null
                        showFormDialog = true
                    },
                    containerColor = IslamicEmerald,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_student_fab")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "নতুন শিক্ষার্থী ভর্তি")
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
            BengaliSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholderBn = "শিক্ষার্থীর নাম, আইডি বা মোবাইল নম্বর খুঁজুন..."
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedDept == null,
                    onClick = { selectedDept = null },
                    label = { Text("সকল বিভাগ (${BengaliFormat.toBnNumber(allStudents.size)})") }
                )
                Department.entries.forEach { dept ->
                    val count = allStudents.count { it.department == dept.name }
                    FilterChip(
                        selected = selectedDept == dept,
                        onClick = { selectedDept = dept },
                        label = { Text("${dept.labelBn} (${BengaliFormat.toBnNumber(count)})") }
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            if (filtered.isEmpty()) {
                EmptyStateCard(
                    titleBn = "কোনো শিক্ষার্থী পাওয়া যায়নি",
                    subtitleBn = if (role == UserRole.ADMIN) "নিচের + বোতামে চাপ দিয়ে নতুন শিক্ষার্থী ভর্তি করুন।" else "আপনার জন্য নির্ধারিত শিক্ষার্থী তালিকা এখানে দেখাবে।",
                    icon = Icons.Filled.Groups
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 84.dp)
                ) {
                    items(filtered, key = { it.id }) { student ->
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewingStudent = student }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(IslamicEmerald.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!student.photoUri.isNullOrBlank()) {
                                        AsyncImage(
                                            model = student.photoUri,
                                            contentDescription = student.fullName,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            text = student.fullName.take(1),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = IslamicEmerald,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(student.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        "আইডি: ${student.studentCode} • রোল: ${student.rollNumber}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "পিতা: ${student.fatherName} • শ্রেণি: ${student.className}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    DepartmentBadge(student.departmentEnum)
                                }
                                if (role == UserRole.ADMIN) {
                                    Column {
                                        IconButton(onClick = {
                                            editingStudent = student
                                            showFormDialog = true
                                        }) {
                                            Icon(Icons.Filled.Edit, contentDescription = "সম্পাদনা", tint = IslamicEmerald)
                                        }
                                        IconButton(onClick = { deletingStudent = student }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "মুছে ফেলুন", tint = StatusAbsent)
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

    if (showFormDialog) {
        StudentAdmissionDialog(
            initial = editingStudent,
            nextSerial = allStudents.size + 1,
            guardians = guardians,
            onDismiss = { showFormDialog = false },
            onSave = { student, existingId ->
                viewModel.saveStudent(student, existingId)
                showFormDialog = false
            }
        )
    }

    viewingStudent?.let { s ->
        AlertDialog(
            onDismissRequest = { viewingStudent = null },
            title = { Text(s.fullName, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("শিক্ষার্থী আইডি: ${s.studentCode}")
                    Text("বিভাগ: ${s.departmentEnum.labelBn}")
                    Text("শ্রেণি: ${s.className} • রোল: ${s.rollNumber}")
                    Text("জন্মতারিখ: ${s.dateOfBirth} • লিঙ্গ: ${s.gender}")
                    Text("পিতার নাম: ${s.fatherName}")
                    Text("মাতার নাম: ${s.motherName}")
                    Text("অভিভাবকের মোবাইল: ${s.guardianPhone}")
                    Text("জরুরি যোগাযোগ: ${s.emergencyContact}")
                    Text("ঠিকানা: ${s.address}")
                    Text("ভর্তির তারিখ: ${s.admissionDate}")
                    if (!s.previousInstitution.isNullOrBlank()) {
                        Text("পূর্ববর্তী প্রতিষ্ঠান: ${s.previousInstitution}")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val res = BengaliReportExporter.generateBengaliPdf(
                            context = context,
                            fileNamePrefix = "student_${s.studentCode}",
                            titleBn = "শিক্ষার্থীর ভর্তি ও প্রোফাইল বিবরণী",
                            subtitleBn = s.fullName,
                            sections = listOf(
                                ReportSection(
                                    headingBn = "ব্যক্তিগত ও প্রাতিষ্ঠানিক তথ্য",
                                    rows = listOf(
                                        listOf("শিক্ষার্থী আইডি: ${s.studentCode}", "নাম: ${s.fullName}"),
                                        listOf("বিভাগ: ${s.departmentEnum.labelBn}", "শ্রেণি: ${s.className}", "রোল: ${s.rollNumber}"),
                                        listOf("পিতার নাম: ${s.fatherName}", "মাতার নাম: ${s.motherName}"),
                                        listOf("অভিভাবকের মোবাইল: ${s.guardianPhone}", "জরুরি যোগাযোগ: ${s.emergencyContact}"),
                                        listOf("ভর্তির তারিখ: ${s.admissionDate}", "ঠিকানা: ${s.address}")
                                    )
                                )
                            )
                        )
                        res.onSuccess { file ->
                            BengaliReportExporter.printPdfFile(context, file, "শিক্ষার্থী-${s.studentCode}")
                        }.onFailure {
                            viewModel.emitMessage("পিডিএফ তৈরিতে সমস্যা হয়েছে।")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald)
                ) {
                    Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("প্রিন্ট করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewingStudent = null }) { Text("বন্ধ করুন") }
            }
        )
    }

    deletingStudent?.let { s ->
        AlertDialog(
            onDismissRequest = { deletingStudent = null },
            title = { Text("শিক্ষার্থী ব্যবস্থাপনা", fontWeight = FontWeight.Bold) },
            text = { Text("${s.fullName}-কে আর্কাইভ করতে চান নাকি স্থায়ীভাবে মুছে ফেলতে চান?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.archiveOrDeleteStudent(s.id, s.fullName, archiveOnly = true)
                    deletingStudent = null
                }) {
                    Icon(Icons.Filled.Archive, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("আর্কাইভ করুন")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        viewModel.archiveOrDeleteStudent(s.id, s.fullName, archiveOnly = false)
                        deletingStudent = null
                    }) {
                        Text("মুছে ফেলুন", color = StatusAbsent)
                    }
                    TextButton(onClick = { deletingStudent = null }) {
                        Text("বাতিল")
                    }
                }
            }
        )
    }
}

@Composable
private fun StudentAdmissionDialog(
    initial: Student?,
    nextSerial: Int,
    guardians: List<Guardian>,
    onDismiss: () -> Unit,
    onSave: (Student, String?) -> Unit
) {
    val autoId = remember(initial, nextSerial) {
        initial?.studentCode ?: "KMN-1995-${nextSerial.toString().padStart(3, '0')}"
    }
    var fullName by remember { mutableStateOf(initial?.fullName ?: "") }
    var photoUri by remember { mutableStateOf(initial?.photoUri ?: "") }
    var dob by remember { mutableStateOf(initial?.dateOfBirth ?: "2015-01-01") }
    var gender by remember { mutableStateOf(initial?.gender ?: "ছাত্র") }
    var fatherName by remember { mutableStateOf(initial?.fatherName ?: "") }
    var motherName by remember { mutableStateOf(initial?.motherName ?: "") }
    var guardianPhone by remember { mutableStateOf(initial?.guardianPhone ?: "") }
    var address by remember { mutableStateOf(initial?.address ?: "") }
    var admissionDate by remember { mutableStateOf(initial?.admissionDate ?: BengaliFormat.todayDateKey()) }
    var department by remember { mutableStateOf(initial?.departmentEnum ?: Department.NOORANI) }
    var className by remember { mutableStateOf(initial?.className ?: Department.NOORANI.classes.first()) }
    var rollNumber by remember { mutableStateOf(initial?.rollNumber ?: BengaliFormat.toBnNumber(nextSerial)) }
    var previousInst by remember { mutableStateOf(initial?.previousInstitution ?: "") }
    var emergencyContact by remember { mutableStateOf(initial?.emergencyContact ?: "") }
    var selectedGuardianUid by remember { mutableStateOf(initial?.guardianUid ?: "") }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) photoUri = uri.toString()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "নতুন শিক্ষার্থী ভর্তি ফর্ম" else "শিক্ষার্থীর তথ্য সম্পাদনা", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("শিক্ষার্থী আইডি: $autoId", style = MaterialTheme.typography.labelLarge, color = IslamicEmerald)
                OutlinedButton(
                    onClick = {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (photoUri.isBlank()) "শিক্ষার্থীর ছবি নির্বাচন করুন" else "ছবি নির্বাচিত হয়েছে")
                }

                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("শিক্ষার্থীর নাম *") }, singleLine = true)
                OutlinedTextField(value = fatherName, onValueChange = { fatherName = it }, label = { Text("পিতার নাম *") }, singleLine = true)
                OutlinedTextField(value = motherName, onValueChange = { motherName = it }, label = { Text("মাতার নাম") }, singleLine = true)
                OutlinedTextField(value = guardianPhone, onValueChange = { guardianPhone = it }, label = { Text("অভিভাবকের মোবাইল নম্বর *") }, singleLine = true)
                OutlinedTextField(value = emergencyContact, onValueChange = { emergencyContact = it }, label = { Text("জরুরি যোগাযোগ") }, singleLine = true)
                OutlinedTextField(value = dob, onValueChange = { dob = it }, label = { Text("জন্মতারিখ (YYYY-MM-DD)") }, singleLine = true)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ছাত্র", "ছাত্রী").forEach { g ->
                        FilterChip(selected = gender == g, onClick = { gender = g }, label = { Text(g) })
                    }
                }

                Text("বিভাগ নির্বাচন করুন", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Department.entries.forEach { d ->
                        FilterChip(
                            selected = department == d,
                            onClick = {
                                department = d
                                className = d.classes.first()
                            },
                            label = { Text(d.labelBn) }
                        )
                    }
                }

                Text("শ্রেণি নির্বাচন করুন", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    department.classes.forEach { cls ->
                        FilterChip(selected = className == cls, onClick = { className = cls }, label = { Text(cls) })
                    }
                }

                OutlinedTextField(value = rollNumber, onValueChange = { rollNumber = it }, label = { Text("রোল নম্বর *") }, singleLine = true)
                OutlinedTextField(value = admissionDate, onValueChange = { admissionDate = it }, label = { Text("ভর্তির তারিখ") }, singleLine = true)
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("ঠিকানা") })
                OutlinedTextField(value = previousInst, onValueChange = { previousInst = it }, label = { Text("পূর্ববর্তী প্রতিষ্ঠানের তথ্য (ঐচ্ছিক)") })

                if (guardians.isNotEmpty()) {
                    Text("নিবন্ধিত অভিভাবকের সাথে সংযুক্ত করুন (ঐচ্ছিক):", style = MaterialTheme.typography.labelSmall)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        guardians.forEach { g ->
                            val guardianKey = g.id
                            FilterChip(
                                selected = selectedGuardianUid == guardianKey,
                                onClick = { selectedGuardianUid = if (selectedGuardianUid == guardianKey) "" else guardianKey },
                                label = { Text(g.fullName) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = fullName.trim()
                    val cleanFather = fatherName.trim()
                    val cleanPhone = guardianPhone.trim()
                    if (cleanName.isNotBlank() && cleanFather.isNotBlank() && cleanPhone.length >= 5) {
                        onSave(
                            Student(
                                studentCode = autoId,
                                fullName = cleanName,
                                photoUri = photoUri.ifBlank { null },
                                dateOfBirth = dob.trim(),
                                gender = gender,
                                fatherName = cleanFather,
                                motherName = motherName.trim(),
                                guardianPhone = cleanPhone,
                                guardianUid = selectedGuardianUid.ifBlank { null },
                                address = address.trim(),
                                admissionDate = admissionDate.trim(),
                                department = department.name,
                                className = className,
                                rollNumber = rollNumber.trim().ifBlank { "১" },
                                previousInstitution = previousInst.trim().ifBlank { null },
                                emergencyContact = emergencyContact.trim().ifBlank { cleanPhone }
                            ),
                            initial?.id
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald),
                modifier = Modifier.testTag("save_student_button")
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}

@Composable
fun TeachersScreen(viewModel: MadrasaViewModel) {
    val teachersState by viewModel.teachersState.collectAsStateWithLifecycle()
    val teachers = (teachersState as? UiState.Success)?.data.orEmpty()

    var searchQuery by remember { mutableStateOf("") }
    var showForm by remember { mutableStateOf(false) }
    var editingTeacher by remember { mutableStateOf<Teacher?>(null) }
    var salaryConfirmTeacher by remember { mutableStateOf<Teacher?>(null) }

    val filtered = remember(teachers, searchQuery) {
        teachers.filter {
            searchQuery.isBlank() ||
                it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery, ignoreCase = true) ||
                it.designation.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTeacher = null
                    showForm = true
                },
                containerColor = IslamicEmerald,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_teacher_fab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "নতুন শিক্ষক যোগ করুন")
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
            BengaliSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholderBn = "শিক্ষকের নাম, পদবি বা মোবাইল নম্বর খুঁজুন..."
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (filtered.isEmpty()) {
                EmptyStateCard(
                    titleBn = "কোনো শিক্ষকের তথ্য পাওয়া যায়নি",
                    subtitleBn = "নতুন শিক্ষক যোগ করতে + বোতামে চাপ দিন।",
                    icon = Icons.Filled.Badge
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 84.dp)
                ) {
                    items(filtered, key = { it.id }) { t ->
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(IslamicGold.copy(alpha = 0.16f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!t.photoUri.isNullOrBlank()) {
                                            AsyncImage(
                                                model = t.photoUri,
                                                contentDescription = t.fullName,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(Icons.Filled.Badge, contentDescription = null, tint = IslamicEmerald)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(t.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("${t.designation} • ${t.qualification}", style = MaterialTheme.typography.bodySmall)
                                        Text("মোবাইল: ${t.phone} • বেতন: ${BengaliFormat.formatTaka(t.monthlySalary)}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    IconButton(onClick = {
                                        editingTeacher = t
                                        showForm = true
                                    }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "সম্পাদনা", tint = IslamicEmerald)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    DepartmentBadge(t.departmentEnum)
                                    OutlinedButton(onClick = { salaryConfirmTeacher = t }) {
                                        Icon(Icons.Filled.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("বেতন প্রদান করুন")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        TeacherFormDialog(
            initial = editingTeacher,
            onDismiss = { showForm = false },
            onSave = { teacher, existingId ->
                viewModel.saveTeacher(teacher, existingId)
                showForm = false
            }
        )
    }

    salaryConfirmTeacher?.let { t ->
        BengaliConfirmDialog(
            titleBn = "শিক্ষকের বেতন প্রদান",
            messageBn = "${t.fullName}-এর মাসিক বেতন (${BengaliFormat.formatTaka(t.monthlySalary)}) নগদ তহবিল থেকে ব্যয় হিসেবে হিসাবভুক্ত করতে চান?",
            confirmTextBn = "বেতন প্রদান করুন",
            onConfirm = {
                viewModel.payTeacherSalary(t, "নগদ")
                salaryConfirmTeacher = null
            },
            onDismiss = { salaryConfirmTeacher = null }
        )
    }
}

@Composable
private fun TeacherFormDialog(
    initial: Teacher?,
    onDismiss: () -> Unit,
    onSave: (Teacher, String?) -> Unit
) {
    var fullName by remember { mutableStateOf(initial?.fullName ?: "") }
    var photoUri by remember { mutableStateOf(initial?.photoUri ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var address by remember { mutableStateOf(initial?.address ?: "") }
    var joiningDate by remember { mutableStateOf(initial?.joiningDate ?: BengaliFormat.todayDateKey()) }
    var designation by remember { mutableStateOf(initial?.designation ?: "সিনিয়র শিক্ষক") }
    var qualification by remember { mutableStateOf(initial?.qualification ?: "দাওরায়ে হাদীস / হাফেজ") }
    var salaryStr by remember { mutableStateOf(initial?.monthlySalary?.toInt()?.toString() ?: "15000") }
    var department by remember { mutableStateOf(initial?.departmentEnum ?: Department.HIFZ) }
    var loginEmail by remember { mutableStateOf(initial?.loginEmail ?: "") }
    var selectedClasses by remember { mutableStateOf(initial?.assignedClasses ?: listOf(Department.HIFZ.classes.first())) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) photoUri = uri.toString()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "নতুন শিক্ষক যুক্ত করুন" else "শিক্ষকের তথ্য সম্পাদনা", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (photoUri.isBlank()) "শিক্ষকের ছবি নির্বাচন করুন" else "ছবি নির্বাচিত")
                }
                OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("শিক্ষকের নাম *") }, singleLine = true)
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("মোবাইল নম্বর *") }, singleLine = true)
                OutlinedTextField(value = designation, onValueChange = { designation = it }, label = { Text("দায়িত্ব / পদবি *") }, singleLine = true)
                OutlinedTextField(value = qualification, onValueChange = { qualification = it }, label = { Text("শিক্ষাগত যোগ্যতা") }, singleLine = true)
                OutlinedTextField(value = salaryStr, onValueChange = { salaryStr = it }, label = { Text("মাসিক বেতন (৳)") }, singleLine = true)
                OutlinedTextField(value = joiningDate, onValueChange = { joiningDate = it }, label = { Text("যোগদানের তারিখ") }, singleLine = true)
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("ঠিকানা") })
                OutlinedTextField(value = loginEmail, onValueChange = { loginEmail = it }, label = { Text("ইমেইল (ঐচ্ছিক)") }, singleLine = true)

                Text("দায়িত্বপ্রাপ্ত বিভাগ", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Department.entries.forEach { d ->
                        FilterChip(
                            selected = department == d,
                            onClick = {
                                department = d
                                selectedClasses = listOf(d.classes.first())
                            },
                            label = { Text(d.labelBn) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val salary = salaryStr.toDoubleOrNull() ?: 0.0
                    if (fullName.isNotBlank() && phone.trim().length >= 5) {
                        onSave(
                            Teacher(
                                fullName = fullName.trim(),
                                photoUri = photoUri.ifBlank { null },
                                phone = phone.trim(),
                                address = address.trim(),
                                joiningDate = joiningDate.trim(),
                                designation = designation.trim(),
                                qualification = qualification.trim(),
                                monthlySalary = salary,
                                department = department.name,
                                assignedClasses = selectedClasses,
                                loginEmail = loginEmail.trim(),
                                teacherUid = initial?.teacherUid,
                                active = true
                            ),
                            initial?.id
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald),
                modifier = Modifier.testTag("save_teacher_button")
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("বাতিল") } }
    )
}

@Composable
fun GuardiansScreen(viewModel: MadrasaViewModel) {
    val guardiansState by viewModel.guardiansState.collectAsStateWithLifecycle()
    val studentsState by viewModel.studentsState.collectAsStateWithLifecycle()
    val guardians = (guardiansState as? UiState.Success)?.data.orEmpty()
    val students = (studentsState as? UiState.Success)?.data.orEmpty().filter { !it.archived }

    var showDialog by remember { mutableStateOf(false) }
    var editingGuardian by remember { mutableStateOf<Guardian?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingGuardian = null
                    showDialog = true
                },
                containerColor = IslamicEmerald,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_guardian_fab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "নতুন অভিভাবক নিবন্ধন")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (guardians.isEmpty()) {
                EmptyStateCard(
                    titleBn = "কোনো অভিভাবক নিবন্ধিত নেই",
                    subtitleBn = "নতুন অভিভাবক নিবন্ধন করুন এবং এক বা একাধিক সন্তান সংযুক্ত করুন।",
                    icon = Icons.Filled.FamilyRestroom
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 84.dp)) {
                    items(guardians, key = { it.id }) { g ->
                        val linkedNames = students.filter { it.id in g.linkedStudentIds }.joinToString(", ") { it.fullName }
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${g.fullName} (${g.relation})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("মোবাইল: ${g.phone}", style = MaterialTheme.typography.bodySmall)
                                    if (!g.email.isNullOrBlank()) Text("ইমেইল: ${g.email}", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "সংযুক্ত সন্তান: ${linkedNames.ifBlank { "${BengaliFormat.toBnNumber(g.linkedStudentIds.size)} জন" }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = IslamicEmerald
                                    )
                                }
                                IconButton(onClick = {
                                    editingGuardian = g
                                    showDialog = true
                                }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "সম্পাদনা", tint = IslamicEmerald)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        var fullName by remember { mutableStateOf(editingGuardian?.fullName ?: "") }
        var phone by remember { mutableStateOf(editingGuardian?.phone ?: "") }
        var email by remember { mutableStateOf(editingGuardian?.email ?: "") }
        var address by remember { mutableStateOf(editingGuardian?.address ?: "") }
        var relation by remember { mutableStateOf(editingGuardian?.relation ?: "পিতা") }
        var selectedStudentIds by remember { mutableStateOf(editingGuardian?.linkedStudentIds?.toSet() ?: emptySet()) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("অভিভাবক নিবন্ধন ও সন্তান সংযুক্তি", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("অভিভাবকের নাম *") }, singleLine = true)
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("মোবাইল নম্বর *") }, singleLine = true)
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("ইমেইল (ঐচ্ছিক)") }, singleLine = true)
                    OutlinedTextField(value = relation, onValueChange = { relation = it }, label = { Text("সম্পর্ক (পিতা/মাতা/অভিভাবক)") }, singleLine = true)
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("ঠিকানা") })

                    if (students.isNotEmpty()) {
                        Text("সন্তান নির্বাচন করুন (একাধিক সম্ভব):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        students.forEach { st ->
                            val checked = st.id in selectedStudentIds
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedStudentIds = if (checked) selectedStudentIds - st.id else selectedStudentIds + st.id
                                    }
                            ) {
                                Checkbox(checked = checked, onCheckedChange = {
                                    selectedStudentIds = if (checked) selectedStudentIds - st.id else selectedStudentIds + st.id
                                })
                                Text("${st.fullName} (${st.className})", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (fullName.isNotBlank() && phone.trim().length >= 5) {
                            viewModel.saveGuardian(
                                Guardian(
                                    fullName = fullName.trim(),
                                    phone = phone.trim(),
                                    email = email.trim().ifBlank { null },
                                    address = address.trim(),
                                    relation = relation.trim().ifBlank { "পিতা" },
                                    guardianUid = editingGuardian?.guardianUid,
                                    linkedStudentIds = selectedStudentIds.toList()
                                ),
                                editingGuardian?.id
                            )
                            showDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald)
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("বাতিল") } }
        )
    }
}

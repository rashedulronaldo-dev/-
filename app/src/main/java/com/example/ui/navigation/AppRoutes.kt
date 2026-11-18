package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.model.UserRole

enum class AppRoute(
    val route: String,
    val titleBn: String,
    val icon: ImageVector,
    val allowedRoles: Set<UserRole>
) {
    ADMIN_DASHBOARD(
        route = "admin_dashboard",
        titleBn = "ড্যাশবোর্ড",
        icon = Icons.Filled.Dashboard,
        allowedRoles = setOf(UserRole.ADMIN)
    ),
    TEACHER_DASHBOARD(
        route = "teacher_dashboard",
        titleBn = "শিক্ষক ড্যাশবোর্ড",
        icon = Icons.Filled.Dashboard,
        allowedRoles = setOf(UserRole.TEACHER)
    ),
    PARENT_DASHBOARD(
        route = "parent_dashboard",
        titleBn = "অভিভাবক পোর্টাল",
        icon = Icons.Filled.Dashboard,
        allowedRoles = setOf(UserRole.PARENT)
    ),
    STUDENTS(
        route = "students",
        titleBn = "শিক্ষার্থী",
        icon = Icons.Filled.Groups,
        allowedRoles = setOf(UserRole.ADMIN, UserRole.TEACHER, UserRole.PARENT)
    ),
    TEACHERS(
        route = "teachers",
        titleBn = "শিক্ষকবৃন্দ",
        icon = Icons.Filled.Badge,
        allowedRoles = setOf(UserRole.ADMIN)
    ),
    GUARDIANS(
        route = "guardians",
        titleBn = "অভিভাবক",
        icon = Icons.Filled.FamilyRestroom,
        allowedRoles = setOf(UserRole.ADMIN)
    ),
    ATTENDANCE(
        route = "attendance",
        titleBn = "হাজিরা",
        icon = Icons.AutoMirrored.Filled.FactCheck,
        allowedRoles = setOf(UserRole.ADMIN, UserRole.TEACHER, UserRole.PARENT)
    ),
    NOORANI(
        route = "noorani",
        titleBn = "নূরানী বিভাগ",
        icon = Icons.AutoMirrored.Filled.MenuBook,
        allowedRoles = setOf(UserRole.ADMIN, UserRole.TEACHER, UserRole.PARENT)
    ),
    NAZERA(
        route = "nazera",
        titleBn = "নাজেরা বিভাগ",
        icon = Icons.AutoMirrored.Filled.MenuBook,
        allowedRoles = setOf(UserRole.ADMIN, UserRole.TEACHER, UserRole.PARENT)
    ),
    HIFZ(
        route = "hifz",
        titleBn = "হিফজ বিভাগ",
        icon = Icons.Filled.Mosque,
        allowedRoles = setOf(UserRole.ADMIN, UserRole.TEACHER, UserRole.PARENT)
    ),
    EXAMINATIONS(
        route = "examinations",
        titleBn = "পরীক্ষা ও ফলাফল",
        icon = Icons.Filled.Quiz,
        allowedRoles = setOf(UserRole.ADMIN, UserRole.TEACHER, UserRole.PARENT)
    ),
    FEES(
        route = "fees",
        titleBn = "বেতন ও ফি",
        icon = Icons.Filled.Payments,
        allowedRoles = setOf(UserRole.ADMIN, UserRole.PARENT)
    ),
    PAYMENTS(
        route = "payments",
        titleBn = "অনলাইন পেমেন্ট",
        icon = Icons.Filled.CreditCard,
        allowedRoles = setOf(UserRole.ADMIN, UserRole.PARENT)
    ),
    ACCOUNTING(
        route = "accounting",
        titleBn = "আয়-ব্যয় হিসাব",
        icon = Icons.Filled.AccountBalance,
        allowedRoles = setOf(UserRole.ADMIN)
    ),
    DONATIONS(
        route = "donations",
        titleBn = "অনুদান",
        icon = Icons.Filled.VolunteerActivism,
        allowedRoles = setOf(UserRole.ADMIN)
    ),
    NOTICES(
        route = "notices",
        titleBn = "নোটিশ ও বিজ্ঞপ্তি",
        icon = Icons.Filled.Campaign,
        allowedRoles = setOf(UserRole.ADMIN, UserRole.TEACHER, UserRole.PARENT)
    ),
    REPORTS(
        route = "reports",
        titleBn = "প্রতিবেদন ও পিডিএফ",
        icon = Icons.Filled.Assessment,
        allowedRoles = setOf(UserRole.ADMIN, UserRole.TEACHER)
    ),
    SETTINGS(
        route = "settings",
        titleBn = "সেটিংস",
        icon = Icons.Filled.Settings,
        allowedRoles = setOf(UserRole.ADMIN, UserRole.TEACHER, UserRole.PARENT)
    );

    companion object {
        fun homeFor(role: UserRole): AppRoute = when (role) {
            UserRole.ADMIN -> ADMIN_DASHBOARD
            UserRole.TEACHER -> TEACHER_DASHBOARD
            UserRole.PARENT -> PARENT_DASHBOARD
        }

        fun routesFor(role: UserRole): List<AppRoute> =
            entries.filter { role in it.allowedRoles }
    }
}

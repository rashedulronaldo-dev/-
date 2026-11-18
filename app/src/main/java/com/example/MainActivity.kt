package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.local.AppPreferences
import com.example.data.remote.MadrasaRepository
import com.example.ui.navigation.AppRoute
import com.example.ui.screens.AccountingScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.DonationsScreen
import com.example.ui.screens.ExaminationsScreen
import com.example.ui.screens.FeesScreen
import com.example.ui.screens.GuardiansScreen
import com.example.ui.screens.HifzScreen
import com.example.ui.screens.NazeraScreen
import com.example.ui.screens.NooraniScreen
import com.example.ui.screens.NoticesScreen
import com.example.ui.screens.ParentDashboardScreen
import com.example.ui.screens.PaymentsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StudentsScreen
import com.example.ui.screens.TeacherDashboardScreen
import com.example.ui.screens.TeachersScreen
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.MadrasaTheme
import com.example.ui.viewmodel.MadrasaViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MadrasaTheme {
                MadrasaAppRoot()
            }
        }
    }
}

@Composable
fun MadrasaAppRoot() {
    var splashCompleted by remember { mutableStateOf(false) }

    if (!splashCompleted) {
        SplashScreen(onFinished = { splashCompleted = true })
    } else {
        val viewModel: MadrasaViewModel = viewModel(
            factory = viewModelFactory {
                initializer {
                    val app = checkNotNull(this[APPLICATION_KEY])
                    MadrasaViewModel(
                        repository = MadrasaRepository(app),
                        preferences = AppPreferences(app)
                    )
                }
            }
        )
        MadrasaMainShell(viewModel = viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MadrasaMainShell(
    viewModel: MadrasaViewModel
) {
    val effectiveRole by viewModel.effectiveRole.collectAsStateWithLifecycle()
    val allowedRoutes = remember(effectiveRole) { AppRoute.routesFor(effectiveRole) }
    val homeRoute = remember(effectiveRole) { AppRoute.homeFor(effectiveRole) }
    var currentRoute by remember(homeRoute) { mutableStateOf(homeRoute) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(allowedRoutes) {
        if (currentRoute !in allowedRoutes) {
            currentRoute = homeRoute
        }
    }

    if (currentRoute != homeRoute) {
        BackHandler {
            currentRoute = homeRoute
        }
    }

    val bottomRoutes = remember(allowedRoutes) { allowedRoutes.take(4) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(IslamicEmerald)
                            .padding(horizontal = 18.dp, vertical = 20.dp)
                    ) {
                        Text(
                            text = "খেলার মাঠ নূরানী ও হাফিজিয়া মাদরাসা",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "প্রতিষ্ঠিত: ১৯৯৫  •  রোল: ${effectiveRole.labelBn}",
                            style = MaterialTheme.typography.labelMedium,
                            color = IslamicGoldLight
                        )
                    }
                    HorizontalDivider()
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 8.dp)
                    ) {
                        allowedRoutes.forEach { route ->
                            NavigationDrawerItem(
                                label = { Text(route.titleBn) },
                                selected = currentRoute == route,
                                onClick = {
                                    currentRoute = route
                                    scope.launch { drawerState.close() }
                                },
                                icon = { Icon(route.icon, contentDescription = route.titleBn) },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                    }
                }
            }
        ) {
            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = currentRoute.titleBn,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "খেলার মাঠ নূরানী তালিমুল কুরআন ও হাফিজিয়া মাদরাসা",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IslamicGoldLight,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                                modifier = Modifier.testTag("open_drawer_button")
                            ) {
                                Icon(Icons.Filled.Menu, contentDescription = "মেনু খুলুন", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = IslamicEmerald,
                            titleContentColor = Color.White
                        )
                    )
                },
                bottomBar = {
                    if (!isWideScreen) {
                        NavigationBar {
                            bottomRoutes.forEach { route ->
                                NavigationBarItem(
                                    selected = currentRoute == route,
                                    onClick = { currentRoute = route },
                                    icon = { Icon(route.icon, contentDescription = route.titleBn) },
                                    label = { Text(route.titleBn, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isWideScreen) {
                        NavigationRail {
                            allowedRoutes.take(7).forEach { route ->
                                NavigationRailItem(
                                    selected = currentRoute == route,
                                    onClick = { currentRoute = route },
                                    icon = { Icon(route.icon, contentDescription = route.titleBn) },
                                    label = { Text(route.titleBn, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                )
                            }
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        when (currentRoute) {
                            AppRoute.ADMIN_DASHBOARD -> AdminDashboardScreen(viewModel) { currentRoute = it }
                            AppRoute.TEACHER_DASHBOARD -> TeacherDashboardScreen(viewModel) { currentRoute = it }
                            AppRoute.PARENT_DASHBOARD -> ParentDashboardScreen(viewModel) { currentRoute = it }
                            AppRoute.STUDENTS -> StudentsScreen(viewModel)
                            AppRoute.TEACHERS -> TeachersScreen(viewModel)
                            AppRoute.GUARDIANS -> GuardiansScreen(viewModel)
                            AppRoute.ATTENDANCE -> AttendanceScreen(viewModel)
                            AppRoute.NOORANI -> NooraniScreen(viewModel)
                            AppRoute.NAZERA -> NazeraScreen(viewModel)
                            AppRoute.HIFZ -> HifzScreen(viewModel)
                            AppRoute.EXAMINATIONS -> ExaminationsScreen(viewModel)
                            AppRoute.FEES -> FeesScreen(viewModel)
                            AppRoute.PAYMENTS -> PaymentsScreen(viewModel)
                            AppRoute.ACCOUNTING -> AccountingScreen(viewModel)
                            AppRoute.DONATIONS -> DonationsScreen(viewModel)
                            AppRoute.NOTICES -> NoticesScreen(viewModel)
                            AppRoute.REPORTS -> ReportsScreen(viewModel)
                            AppRoute.SETTINGS -> SettingsScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}

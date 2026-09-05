package com.mi.bibliarv1960

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.mi.bibliarv1960.ui.notes.AllNotesScreen
import com.mi.bibliarv1960.ui.notes.NotesViewModel
import com.mi.bibliarv1960.ui.challenges.ChallengeViewModel
import com.mi.bibliarv1960.ui.challenges.ChallengeScreen
import com.mi.bibliarv1960.ui.challenges.DailyChallengeStatus
import com.mi.bibliarv1960.ui.navigation.Screen
import com.mi.bibliarv1960.ui.screens.*
import com.mi.bibliarv1960.ui.components.ThemeToggleButton
import com.mi.bibliarv1960.ui.theme.BibliaRV1960Theme
import com.mi.bibliarv1960.ui.viewmodel.BibleViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

@dagger.hilt.android.AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: BibleViewModel by viewModels()
    private val notesViewModel: NotesViewModel by viewModels()
    private val challengeViewModel: ChallengeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            
            BibliaRV1960Theme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                
                val showTriviaSetting by viewModel.showTrivia.collectAsState()
                val showDevotionalSetting by viewModel.showDevocional.collectAsState()
                
                // --- Gestión de Navegación Segura ---
                var lastNavTime by remember { mutableLongStateOf(0L) }
                
                fun safeNavigate(route: String) {
                    val now = System.currentTimeMillis()
                    if (now - lastNavTime > 600L && navController.currentDestination?.route != route) {
                        lastNavTime = now
                        navController.navigate(route)
                    }
                }

                fun safePopBack() {
                    val now = System.currentTimeMillis()
                    if (now - lastNavTime > 400L && navController.previousBackStackEntry != null) {
                        lastNavTime = now
                        navController.popBackStack()
                    }
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        val configuration = LocalConfiguration.current
                        val screenWidthDp = configuration.screenWidthDp
                        val drawerWidth = (screenWidthDp * 0.86f).dp.coerceIn(280.dp, 360.dp)

                        ModalDrawerSheet(modifier = Modifier.width(drawerWidth)) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                // Header
                                Column(modifier = Modifier.padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 12.dp)) {
                                    Text(
                                        "Verbo Libre",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Text(
                                        "Lectura y meditación diaria",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
                                    label = { Text("Inicio") },
                                    selected = false,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        if (navController.currentDestination?.route != Screen.Home.route) {
                                            safeNavigate(Screen.Home.route)
                                        }
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp).heightIn(min = 48.dp)
                                )

                                // --- SECCIÓN HOY ---
                                Text(
                                    "HOY",
                                    modifier = Modifier.padding(start = 28.dp, top = 12.dp, bottom = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    letterSpacing = 1.sp
                                )
                                
                                NavigationDrawerItem(
                                    icon = { 
                                        Icon(
                                            Icons.Outlined.AutoStories, 
                                            contentDescription = null,
                                            tint = if (showDevotionalSetting) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.38f)
                                        ) 
                                    },
                                    label = { 
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "Devocional",
                                                    color = if (showDevotionalSetting) Color.Unspecified else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                                )
                                                if (showDevotionalSetting) {
                                                    val showDot by viewModel.showDevotionalDot.collectAsState()
                                                    if (showDot) {
                                                        Spacer(modifier = Modifier.weight(1f))
                                                        BreathingNotificationDot()
                                                    }
                                                }
                                            }
                                            if (!showDevotionalSetting) {
                                                Text(
                                                    "Desactivado - Actívalo en Ajustes",
                                                    fontSize = 9.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                                )
                                            }
                                        }
                                    },
                                    selected = false,
                                    onClick = {
                                        if (showDevotionalSetting) {
                                            viewModel.markDevotionalVisited()
                                            scope.launch { drawerState.close() }
                                            safeNavigate(Screen.Devotional.route)
                                        }
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp).heightIn(min = 48.dp)
                                )

                                NavigationDrawerItem(
                                    icon = { 
                                        Icon(
                                            Icons.Outlined.WbSunny, 
                                            contentDescription = null,
                                            tint = LocalContentColor.current 
                                        ) 
                                    },
                                    label = { 
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Versículo de hoy") 
                                            val showDot by viewModel.showDailyVerseDot.collectAsState()
                                            if (showDot) {
                                                Spacer(modifier = Modifier.weight(1f))
                                                BreathingNotificationDot()
                                            }
                                        }
                                    },
                                    selected = false,
                                    onClick = {
                                        viewModel.markDailyVerseVisited()
                                        scope.launch { drawerState.close() }
                                        safeNavigate(Screen.DailyVerse.route)
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp).heightIn(min = 48.dp)
                                )

                                NavigationDrawerItem(
                                    icon = { 
                                        Icon(
                                            Icons.Outlined.Extension, 
                                            contentDescription = null,
                                            tint = if (showTriviaSetting) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.38f)
                                        ) 
                                    }, 
                                    label = { 
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "Reto del día",
                                                    color = if (showTriviaSetting) Color.Unspecified else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                                )
                                                if (showTriviaSetting) {
                                                    val showDot by challengeViewModel.showChallengeDot.collectAsState()
                                                    if (showDot) {
                                                        Spacer(modifier = Modifier.weight(1f))
                                                        BreathingNotificationDot()
                                                    }
                                                }
                                            }
                                            if (!showTriviaSetting) {
                                                Text(
                                                    "Desactivado - Actívalo en Ajustes",
                                                    fontSize = 9.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                                )
                                            }
                                        }
                                    },
                                    selected = false,
                                    onClick = {
                                        if (showTriviaSetting) {
                                            challengeViewModel.markChallengeVisited()
                                            scope.launch { drawerState.close() }
                                            safeNavigate(Screen.Challenge.route)
                                        }
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp).heightIn(min = 48.dp)
                                )


                                // --- SECCIÓN TU ACTIVIDAD ---
                                Text(
                                    "TU ACTIVIDAD",
                                    modifier = Modifier.padding(start = 28.dp, top = 12.dp, bottom = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    letterSpacing = 1.sp
                                )
                                
                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Outlined.BookmarkBorder, contentDescription = null) },
                                    label = { Text("Marcadores") },
                                    selected = false,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        safeNavigate("bookmarks")
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp).heightIn(min = 48.dp)
                                )

                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Outlined.EditNote, contentDescription = null) },
                                    label = { Text("Mis Notas") },
                                    selected = false,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        safeNavigate("all_notes")
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp).heightIn(min = 48.dp)
                                )


                                // --- SECCIÓN MÁS ---
                                Text(
                                    "MÁS",
                                    modifier = Modifier.padding(start = 28.dp, top = 12.dp, bottom = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    letterSpacing = 1.sp
                                )

                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Outlined.Explore, contentDescription = null) },
                                    label = { Text("Propósito") },
                                    selected = false,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        safeNavigate(Screen.Purpose.route)
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp).heightIn(min = 48.dp)
                                )

                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                                    label = { Text("Ajustes") },
                                    selected = false,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        safeNavigate(Screen.Settings.route)
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp).heightIn(min = 48.dp)
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                // --- TARJETA DE PROGRESO GLOBAL ---
                                val globalProgress by viewModel.globalProgressPercent.collectAsState()
                                val allProgress by viewModel.allProgress.collectAsState()
                                
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp, 
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = buildAnnotatedString {
                                                    append("Has leído ")
                                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                                        append("${allProgress.size}")
                                                    }
                                                    append(" capítulos")
                                                },
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${(globalProgress * 100).toInt()}%",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF33506E)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(globalProgress)
                                                    .fillMaxHeight()
                                                    .background(Color(0xFF33506E))
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Verbo Libre · v1.0",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .padding(top = 14.dp, bottom = 24.dp)
                                        .align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                LaunchedEffect(currentRoute) {
                    requestedOrientation = if (currentRoute?.startsWith("reader") == true) {
                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    } else {
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                }

                NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                viewModel = viewModel,
                                onChapterSelected = { bookId, chapter, verse ->
                                    val route = Screen.Reader.createRoute(bookId, chapter, verse)
                                    if (navController.currentDestination?.route != route) {
                                        navController.navigate(route)
                                    }
                                },
                                onNavigate = { route -> safeNavigate(route) },
                                onOpenDrawer = { scope.launch { drawerState.open() } }
                            )
                        }
                        composable(
                            route = Screen.Reader.route,
                            arguments = listOf(
                                navArgument("bookId") { type = NavType.IntType },
                                navArgument("chapter") { type = NavType.IntType },
                                navArgument("verse") { 
                                    type = NavType.IntType
                                    defaultValue = -1 
                                }
                            )
                        ) { backStackEntry ->
                            val bookId = backStackEntry.arguments?.getInt("bookId") ?: 1
                            val chapter = backStackEntry.arguments?.getInt("chapter") ?: 1
                            val verse = backStackEntry.arguments?.getInt("verse") ?: -1

                            LaunchedEffect(bookId, chapter) {
                                viewModel.loadChapter(bookId, chapter)
                            }

                            ReaderScreen(
                                viewModel = viewModel,
                                notesViewModel = notesViewModel,
                                targetVerse = if (verse != -1) verse else null,
                                onOpenDrawer = {
                                    scope.launch { drawerState.open() }
                                }
                            )
                        }
                        composable("bookmarks") {
                            BookmarksScreen(
                                viewModel = viewModel,
                                onOpenDrawer = { scope.launch { drawerState.open() } },
                                onBookmarkClick = { bId, ch, v ->
                                    safeNavigate(Screen.Reader.createRoute(bId, ch, v))
                                }
                            )
                        }
                        composable("all_notes") {
                            val notes by notesViewModel.allNotes.collectAsState()
                            val allBooks by viewModel.allBooks.collectAsState()
                            AllNotesScreen(
                                notes = notes,
                                books = allBooks,
                                viewModel = viewModel,
                                onNoteClick = { note ->
                                    safeNavigate(Screen.Reader.createRoute(note.bookId, note.chapter, note.verseNumber))
                                },
                                onOpenDrawer = { scope.launch { drawerState.open() } }
                            )
                        }
                        composable(Screen.Purpose.route) {
                            PurposeScreen(
                                onOpenDrawer = { scope.launch { drawerState.open() } }
                            )
                        }
                        composable(Screen.DailyVerse.route) {
                            DailyVerseScreen(
                                viewModel = viewModel,
                                onNavigate = { route -> safeNavigate(route) },
                                onBack = { 
                                    navController.popBackStack(Screen.Home.route, inclusive = false)
                                }
                            )
                        }
                        composable(Screen.Devotional.route) {
                            DevotionalScreen(
                                viewModel = viewModel,
                                onBack = { 
                                    navController.popBackStack(Screen.Home.route, inclusive = false)
                                }
                            )
                        }
                        composable(Screen.Challenge.route) {
                            ChallengeScreen(
                                viewModel = challengeViewModel,
                                onClose = {
                                    if (navController.previousBackStackEntry != null) {
                                        navController.popBackStack()
                                    } else {
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(Screen.Challenge.route) { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                        composable(Screen.Search.route) {
                            SearchScreen()
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                viewModel = viewModel,
                                onBack = { safePopBack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BreathingNotificationDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing_dot")
    
    // Core animation: 1.0 to 1.25 pulse
    val coreScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_scale"
    )

    // Halo animation: 1.0 to 2.8 expansion with fade out
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "halo_scale"
    )
    
    val haloOpacity by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "halo_opacity"
    )

    val dotColor = Color(0xFFE0764F)

    Box(
        modifier = Modifier
            .padding(end = 12.dp)
            .size(12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Halo
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = dotColor,
                radius = (size.minDimension / 2) * haloScale,
                alpha = haloOpacity
            )
        }
        // Core
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = dotColor,
                radius = (size.minDimension / 2) * coreScale
            )
        }
    }
}

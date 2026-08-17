package com.mi.bibliarv1960

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
import com.mi.bibliarv1960.ui.viewmodel.BibleViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {

    private val viewModel: BibleViewModel by viewModels {
        val app = application as BibleApplication
        BibleViewModelFactory(app.repository, app.noteRepository, app.dataStoreManager)
    }

    private val notesViewModel: NotesViewModel by viewModels {
        val app = application as BibleApplication
        BibleViewModelFactory(app.repository, app.noteRepository, app.dataStoreManager)
    }

    private val challengeViewModel: ChallengeViewModel by viewModels {
        val app = application as BibleApplication
        BibleViewModelFactory(app.repository, app.noteRepository, app.dataStoreManager)
    }

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

                // --- Auto-start Reto del Día ---
                LaunchedEffect(Unit) {
                    val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    // Usamos un pequeño delay para asegurar que el NavHost esté listo
                    kotlinx.coroutines.delay(100)
                    val status = challengeViewModel.challengeStatus.first()
                    if (status == null || status.date != todayDate) {
                        navController.navigate(Screen.Challenge.route)
                    }
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Column(modifier = Modifier.padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)) {
                                Text(
                                    "Verbo Libre",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Lectura y meditación diaria",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
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
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Outlined.AutoStories, contentDescription = null) },
                                label = { 
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Devocional")
                                        Spacer(modifier = Modifier.weight(1f))
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondary,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "Hoy",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onSecondary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    safeNavigate(Screen.Devotional.route)
                                },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Outlined.WbSunny, contentDescription = null) },
                                label = { Text("Versículo de hoy") },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    safeNavigate(Screen.DailyVerse.route)
                                },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Outlined.BookmarkBorder, contentDescription = null) },
                                label = { Text("Marcadores") },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    safeNavigate("bookmarks")
                                },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Outlined.EditNote, contentDescription = null) },
                                label = { Text("Mis Notas") },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    safeNavigate("all_notes")
                                },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Outlined.Extension, contentDescription = null) }, 
                                label = { Text("Reto del día") },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    safeNavigate(Screen.Challenge.route)
                                },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Outlined.Explore, contentDescription = null) },
                                label = { Text("Propósito") },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    safeNavigate(Screen.Purpose.route)
                                },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            // --- MODO OSCURO TOGGLE ---
                            Surface(
                                onClick = { viewModel.toggleDarkMode() },
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp, 
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            "Modo oscuro",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            "Activa el tema nocturno",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    ThemeToggleButton(
                                        isDark = isDarkMode,
                                        onClick = { viewModel.toggleDarkMode() },
                                        size = 38.dp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "Verbo Libre · v1.0",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .padding(bottom = 24.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                ) {
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
                                onBack = { safePopBack() },
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
                            val allTranslations by viewModel.allTranslations.collectAsState()
                            AllNotesScreen(
                                notes = notes,
                                books = allBooks,
                                translations = allTranslations,
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
                    }
                }
            }
        }
    }
}

package com.mi.bibliarv1960.ui.screens

import android.content.Intent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily as ComposeFontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.core.graphics.toColorInt
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import com.mi.bibliarv1960.data.local.entities.BookmarkCategoryEntity
import com.mi.bibliarv1960.data.local.entities.VerseEntity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.app.Activity
import androidx.compose.ui.viewinterop.AndroidView
import com.mi.bibliarv1960.R
import com.mi.bibliarv1960.ui.components.ThemeToggleButton
import com.mi.bibliarv1960.ui.components.ReaderOnboarding
import com.mi.bibliarv1960.ui.theme.LinoIcons
import com.mi.bibliarv1960.utils.findActivity
import com.mi.bibliarv1960.ui.viewmodel.BibleViewModel
import com.mi.bibliarv1960.ui.notes.NotesViewModel
import com.mi.bibliarv1960.ui.notes.NoteEditorSheet
import kotlinx.coroutines.delay
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: BibleViewModel,
    notesViewModel: NotesViewModel,
    targetVerse: Int? = null,
    onOpenDrawer: () -> Unit,
) {
    val currentBook by viewModel.currentBook.collectAsState()
    val verses by viewModel.currentVerses.collectAsState()
    val bookmarks by viewModel.allBookmarks.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val fontFamilyName by viewModel.fontFamily.collectAsState()
    val translations by viewModel.allTranslations.collectAsState()
    val selectedTranslationId by viewModel.selectedTranslationId.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val currentSpeakingVerse by viewModel.currentSpeakingVerse.collectAsState()
    val isDiscontinuousMode by viewModel.isDiscontinuousMode.collectAsState()
    val notedVerseKeys by notesViewModel.notedVerseKeys.collectAsState()
    val allNotes by notesViewModel.allNotes.collectAsState()

    val noteIndicatorColor = MaterialTheme.colorScheme.primary
    val noteTextColor = if (isDarkMode) Color(0xFFFF8A80) else Color(0xFFB8310F)

    val context = LocalContext.current
    val density = LocalDensity.current
    
    val readerOnboarding = remember { 
        val activity = context.findActivity()
        if (activity != null) ReaderOnboarding(activity) else null
    }

    var onboardingActive by remember { mutableStateOf(readerOnboarding != null && !readerOnboarding.isShown()) }

    var menuAnchor by remember { mutableStateOf<View?>(null) }
    var translationAnchor by remember { mutableStateOf<View?>(null) }
    var progressAnchor by remember { mutableStateOf<View?>(null) }
    var audioAnchor by remember { mutableStateOf<View?>(null) }
    var themeAnchor by remember { mutableStateOf<View?>(null) }
    var verseAnchor by remember { mutableStateOf<View?>(null) }

    LaunchedEffect(menuAnchor, translationAnchor, progressAnchor, audioAnchor, themeAnchor, verseAnchor) {
        if (onboardingActive && menuAnchor != null && translationAnchor != null && progressAnchor != null &&
            audioAnchor != null && themeAnchor != null && verseAnchor != null && 
            readerOnboarding != null) {
            
            delay(1000) 
            readerOnboarding.start(menuAnchor!!, translationAnchor!!, progressAnchor!!, audioAnchor!!, themeAnchor!!, verseAnchor!!) {
                onboardingActive = false
            }
        }
    }

    var showTranslationSelector by remember { mutableStateOf(value = false) }

    val scrollState = rememberScrollState()
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var tappedVerseOffset by remember { mutableStateOf(Offset.Zero) }
    
    var showColorPickerByVerse by remember { mutableStateOf<VerseEntity?>(null) }
    var lastChapterSeen by remember { mutableStateOf<Int?>(null) }
    var lastScrolledVerse by remember { mutableStateOf<Int?>(null) }
    var flashingVerse by remember { mutableStateOf<Int?>(null) }
    var isManualChapterChange by remember { mutableStateOf(value = false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha",
    )

    val bookName = currentBook?.name ?: ""

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopSpeaking()
        }
    }

    LaunchedEffect(flashingVerse) {
        if (flashingVerse != null) {
            delay(3000.milliseconds)
            flashingVerse = null
        }
    }

    LaunchedEffect(verses, textLayoutResult, targetVerse) {
        if ((verses.isNotEmpty()) && (textLayoutResult != null)) {
            val currentChapter = verses.first().chapter
            
            if (isManualChapterChange) {
                scrollState.scrollTo(0)
                flashingVerse = null
                lastChapterSeen = currentChapter
                isManualChapterChange = false
            } else if ((targetVerse != null) && ((targetVerse != lastScrolledVerse) || (currentChapter != lastChapterSeen))) {
                val annotations = textLayoutResult!!.layoutInput.text.getStringAnnotations("VERSE", 0, textLayoutResult!!.layoutInput.text.length)
                val targetAnnotation = annotations.find { it.item == targetVerse.toString() }
                if (targetAnnotation != null) {
                    val line = textLayoutResult!!.getLineForOffset(targetAnnotation.start)
                    val top = textLayoutResult!!.getLineTop(line)
                    scrollState.scrollTo(top.toInt())
                    lastScrolledVerse = targetVerse
                    lastChapterSeen = currentChapter
                    flashingVerse = targetVerse
                }
            } else if (targetVerse == null && currentChapter != lastChapterSeen) {
                scrollState.scrollTo(0)
                lastChapterSeen = currentChapter
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "$bookName ${verses.firstOrNull()?.chapter ?: ""}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        
                        Spacer(modifier = Modifier.width(6.dp))

                        Box {
                            Surface(
                                onClick = { showTranslationSelector = true },
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = CircleShape,
                                modifier = Modifier.height(24.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 9.dp)
                                ) {
                                    val currentAbbr = translations.find { it.id == selectedTranslationId }?.abbreviation ?: "..."
                                    Text(
                                        text = currentAbbr,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(9.dp),
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                            if (onboardingActive) {
                                AndroidView(
                                    factory = { ctx ->
                                        View(ctx).apply {
                                            visibility = View.INVISIBLE
                                            translationAnchor = this
                                        }
                                    },
                                    modifier = Modifier.size(1.dp).align(Alignment.Center)
                                )
                            }
                        }

                        if (showTranslationSelector) {
                            Popup(
                                alignment = Alignment.TopCenter,
                                onDismissRequest = { showTranslationSelector = false },
                                properties = PopupProperties(focusable = true)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .padding(top = 32.dp)
                                        .width(280.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 8.dp,
                                    shadowElevation = 8.dp,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "ELEGIR TRADUCCIÓN",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(10.dp)
                                        )

                                        translations.forEach { translation ->
                                            val isSelected = translation.id == selectedTranslationId
                                            val desc = when(translation.id) {
                                                "rv1909" -> "Revisión clásica"
                                                "vbl" -> "Lenguaje moderno"
                                                else -> "Traducción bíblica"
                                            }

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                                                        RoundedCornerShape(10.dp)
                                                    )
                                                    .clickable {
                                                        viewModel.selectTranslation(translation.id)
                                                        showTranslationSelector = false
                                                    }
                                                    .padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = translation.name,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "${translation.abbreviation} · $desc",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .border(
                                                            1.5.dp,
                                                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                                            CircleShape
                                                        )
                                                        .background(
                                                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                            CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (isSelected) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(12.dp),
                                                            tint = MaterialTheme.colorScheme.onPrimary
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
                },
                navigationIcon = {
                    Box {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(
                                imageVector = LinoIcons.MenuAsymmetric,
                                contentDescription = "Menú",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (onboardingActive) {
                            AndroidView(
                                factory = { ctx ->
                                    View(ctx).apply {
                                        visibility = View.INVISIBLE
                                        menuAnchor = this
                                    }
                                },
                                modifier = Modifier.size(1.dp).align(Alignment.Center)
                            )
                        }
                    }
                },
                actions = {
                    val isRead by viewModel.currentChapterIsRead.collectAsState()
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            IconButton(
                                onClick = { viewModel.toggleCurrentChapterRead() }
                            ) {
                                Icon(
                                    imageVector = if (isRead) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                                    contentDescription = if (isRead) "Marcar como no leído" else "Marcar como leído",
                                    tint = if (isRead) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (onboardingActive) {
                                AndroidView(
                                    factory = { ctx ->
                                        View(ctx).apply {
                                            visibility = View.INVISIBLE
                                            progressAnchor = this
                                        }
                                    },
                                    modifier = Modifier.size(1.dp)
                                )
                            }
                        }
                        
                        Box(contentAlignment = Alignment.Center) {
                            IconButton(
                                onClick = { viewModel.toggleSpeaking(verses) }
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                    contentDescription = "Escuchar",
                                    tint = if (isSpeaking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (onboardingActive) {
                                AndroidView(
                                    factory = { ctx ->
                                        View(ctx).apply {
                                            visibility = View.INVISIBLE
                                            audioAnchor = this
                                        }
                                    },
                                    modifier = Modifier.size(1.dp)
                                )
                            }
                        }

                        // Envolver el botón de tema para que tenga el mismo "footprint" (48dp) que un IconButton
                        Box(
                            modifier = Modifier
                                .size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ThemeToggleButton(
                                isDark = isDarkMode,
                                onClick = { viewModel.toggleDarkMode() },
                                size = 30.dp
                            )
                            // Hidden anchor for Onboarding (TapTargetView)
                            if (onboardingActive) {
                                AndroidView(
                                    factory = { ctx ->
                                        View(ctx).apply {
                                            visibility = View.INVISIBLE
                                            themeAnchor = this
                                        }
                                    },
                                    modifier = Modifier.size(1.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0.dp, 24.dp, 0.dp, 0.dp)
            )
        },
        bottomBar = {
            Column {
                // --- BARRA DE CONTROL DE AUDIO ---
                if (isSpeaking) {
                    Surface(
                        color = Color(0xFF33506E),
                        modifier = Modifier.fillMaxWidth().height(64.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Botón Play/Pause
                            Surface(
                                onClick = { viewModel.togglePauseResume() },
                                color = Color.White,
                                shape = CircleShape,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                        contentDescription = null,
                                        tint = Color(0xFF33506E),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // 2. Multiplicador de Velocidad (en el lugar "rojo"/izquierdo)
                            Surface(
                                onClick = { viewModel.cyclePlaybackSpeed() },
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    Text(
                                        text = "${playbackSpeed}x",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // 3. Texto de estado (Centro)
                            Text(
                                text = "Leyendo $bookName ${verses.firstOrNull()?.chapter} · versículo $currentSpeakingVerse",
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // 4. Tuerca de Configuración (donde estaba la velocidad)
                            IconButton(
                                onClick = { viewModel.openVoiceSettings() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Ajustes",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets(0, 0, 0, 0)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val currentChapter = verses.firstOrNull()?.chapter ?: 1
                        val totalChapters = currentBook?.chaptersCount ?: 1

                        TextButton(
                            onClick = { 
                                if (currentChapter > 1) {
                                    isManualChapterChange = true
                                    viewModel.loadChapter(currentBook?.id ?: 1, currentChapter - 1) 
                                }
                            },
                            enabled = currentChapter > 1
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
                            Text("Anterior")
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FontSizeButton(text = "A-") { viewModel.updateFontSize(-1f) }
                            FontSizeButton(text = "A+") { viewModel.updateFontSize(1f) }
                        }

                        TextButton(
                            onClick = { 
                                if (currentChapter < totalChapters) {
                                    isManualChapterChange = true
                                    viewModel.loadChapter(currentBook?.id ?: 1, currentChapter + 1) 
                                }
                            },
                            enabled = currentChapter < totalChapters
                        ) {
                            Text("Siguiente")
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (onboardingActive) {
                // Anchor fijo para el paso de "Tocar versículo"
                // Solo existe mientras el onboarding está activo
                AndroidView(
                    factory = { ctx ->
                        View(ctx).apply {
                            visibility = View.INVISIBLE
                            verseAnchor = this
                        }
                    },
                    modifier = Modifier.size(1.dp).align(Alignment.Center)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 26.dp, vertical = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Text(
                text = "Capítulo ${verses.firstOrNull()?.chapter ?: ""}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
                textAlign = TextAlign.Center
            )

            fun buildVerseKey(bookId: Int, chapter: Int, verseNumber: Int): String =
                "${bookId}_${chapter}_$verseNumber"

            val primaryColor = MaterialTheme.colorScheme.primary
            val onBackgroundColor = MaterialTheme.colorScheme.onBackground
            
            val currentFontFamily = when(fontFamilyName) {
                "SERIF" -> ComposeFontFamily.Serif
                "MONOSPACE" -> ComposeFontFamily.Monospace
                else -> ComposeFontFamily.SansSerif
            }

            val annotatedString = remember(verses, fontSize, fontFamilyName, notedVerseKeys, selectedTranslationId, isDiscontinuousMode, allNotes, primaryColor, onBackgroundColor, noteTextColor) {
                buildAnnotatedString {
                    verses.forEach { verse ->
                        val verseKey = buildVerseKey(verse.book_id, verse.chapter, verse.verse)
                        val hasNote = notedVerseKeys.contains(verseKey)
                        val noteText = allNotes.find { it.verseKey == verseKey }?.text

                        // 1. ANOTACIÓN DE VERSÍCULO (Texto Bíblico solamente para TTS y Marcadores)
                        pushStringAnnotation(tag = "VERSE", annotation = verse.verse.toString())
                        
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                fontSize = (fontSize * 0.7f).sp,
                                baselineShift = BaselineShift.Superscript,
                                fontFamily = currentFontFamily
                            )
                        ) {
                            append("${verse.verse} ")
                        }
                        
                        val isDisputed = verse.text.contains("[No incluido en los manuscritos más antiguos]")
                        withStyle(
                            style = SpanStyle(
                                fontSize = fontSize.sp,
                                color = if (isDisputed) Color.Gray else onBackgroundColor,
                                fontStyle = if (isDisputed) FontStyle.Italic else null,
                                textDecoration = null,
                                fontFamily = currentFontFamily
                            )
                        ) {
                            append(verse.text)
                        }
                        pop() // Fin de VERSE

                        // 2. LÓGICA DE NOTAS Y ESPACIADO
                        if (isDiscontinuousMode) {
                            if (!noteText.isNullOrBlank()) {
                                pushStringAnnotation(tag = "NOTE_ACTION", annotation = verseKey)
                                withStyle(style = ParagraphStyle(lineHeight = (fontSize * 1.0f).sp)) {
                                    // Salto de línea de 1sp dentro del bloque de la nota para eliminar el "aire" superior
                                    withStyle(style = SpanStyle(fontSize = 1.sp)) {
                                        append("\n")
                                    }
                                    withStyle(
                                        style = SpanStyle(
                                            color = noteTextColor,
                                            fontSize = (fontSize * 0.9f).sp,
                                            fontStyle = FontStyle.Italic,
                                            fontFamily = currentFontFamily
                                        )
                                    ) {
                                        append(noteText)
                                    }
                                }
                                pop()

                                // Salto de línea controlado para el espacio con el siguiente versículo
                                withStyle(style = SpanStyle(fontSize = (fontSize * 0.6f).sp)) {
                                    append("\n\n")
                                }
                            } else {
                                append("\n\n")
                            }
                        } else {
                            append(" ")
                            if (hasNote) {
                                pushStringAnnotation(tag = "NOTE_ACTION", annotation = verseKey)
                                appendInlineContent("note_icon", "[nota]")
                                pop()
                            }
                        }
                    }
                }
            }

            // --- OPTIMIZACIÓN: Pre-cálculo de Geometría ---
            val verseGeometries = remember(annotatedString, textLayoutResult) {
                val layout = textLayoutResult ?: return@remember emptyMap<Int, List<Rect>>()
                val map = mutableMapOf<Int, List<Rect>>()
                
                val annotations = layout.layoutInput.text.getStringAnnotations("VERSE", 0, layout.layoutInput.text.length)
                annotations.forEach { annotation ->
                    val verseNum = annotation.item.toIntOrNull() ?: return@forEach
                    val rects = mutableListOf<Rect>()
                    val firstLine = layout.getLineForOffset(annotation.start)
                    val lastLine = layout.getLineForOffset(annotation.end)
                    
                    for (lineIndex in firstLine..lastLine) {
                        val left = if (lineIndex == firstLine) layout.getHorizontalPosition(annotation.start, true) else layout.getLineLeft(lineIndex)
                        val right = if (lineIndex == lastLine) layout.getHorizontalPosition(annotation.end, true) else layout.getLineRight(lineIndex)
                        val top = layout.getLineTop(lineIndex)
                        val bottom = layout.getLineBottom(lineIndex)
                        rects.add(Rect(left, top, right, bottom))
                    }
                    map[verseNum] = rects
                }
                map
            }

            val inlineContent = remember(fontSize) {
                mapOf(
                    "note_icon" to InlineTextContent(
                        Placeholder(
                            width = (fontSize * 1.6f).sp,
                            height = (fontSize * 1.6f).sp,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EditNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                val speakingHighlightColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                
                Canvas(modifier = Modifier.matchParentSize()) {
                    if (verseGeometries.isEmpty()) return@Canvas
                    
                    val currentChapter = verses.firstOrNull()?.chapter ?: -1
                    val currentBookId = currentBook?.id ?: -1
                    
                    // 1. Dibujar Marcadores
                    bookmarks.forEach { bookmark ->
                        if (bookmark.bookId == currentBookId && bookmark.chapter == currentChapter) {
                            val category = categories.find { it.id == bookmark.categoryId }
                            val rects = verseGeometries[bookmark.verse]
                            if (category != null && rects != null) {
                                val color = parseColor(category.colorHex).copy(alpha = 0.32f)
                                rects.forEach { rect ->
                                    val height = rect.height
                                    drawRoundRect(
                                        color = color,
                                        topLeft = Offset(rect.left, rect.top + height * 0.15f),
                                        size = Size(rect.width, height * 0.7f),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )
                                }
                            }
                        }
                    }

                    // 2. Dibujar Subrayado de Notas
                    if (!isDiscontinuousMode) {
                        verseGeometries.forEach { (verseNum, rects) ->
                            val vKey = buildVerseKey(currentBookId, currentChapter, verseNum)
                            if (vKey in notedVerseKeys) {
                                val lineThickness = 1.dp.toPx()
                                val spacing = 1.dp.toPx()
                                rects.forEach { rect ->
                                    val bottom = rect.bottom - 2.dp.toPx()
                                    drawRect(
                                        color = noteIndicatorColor,
                                        topLeft = Offset(rect.left, bottom - lineThickness * 2 - spacing),
                                        size = Size(rect.width, lineThickness)
                                    )
                                    drawRect(
                                        color = noteIndicatorColor,
                                        topLeft = Offset(rect.left, bottom - lineThickness),
                                        size = Size(rect.width, lineThickness)
                                    )
                                }
                            }
                        }
                    }

                    // 3. Dibujar Versículo en Reproducción
                    currentSpeakingVerse?.let { sVerse ->
                        verseGeometries[sVerse]?.forEach { rect ->
                            drawRect(
                                color = speakingHighlightColor,
                                topLeft = Offset(rect.left - 4.dp.toPx(), rect.top),
                                size = Size(rect.width + 8.dp.toPx(), rect.height)
                            )
                        }
                    }

                    // 4. Dibujar Versículo Parpadeante (Búsqueda)
                    flashingVerse?.let { fVerse ->
                        verseGeometries[fVerse]?.forEach { rect ->
                            val height = rect.height
                            drawRoundRect(
                                color = Color(0xFFFFE047).copy(alpha = pulseAlpha),
                                topLeft = Offset(rect.left, rect.top + height * 0.1f),
                                size = Size(rect.width, height * 0.8f),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = annotatedString,
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(annotatedString, textLayoutResult, verses) {
                                detectTapGestures { offset ->
                                    textLayoutResult?.let { layout ->
                                        val charOffset = layout.getOffsetForPosition(offset)
                                        
                                        // 1. Buscamos primero por proximidad a las notas (Hit Slop de 8dp)
                                        val noteAnnotations = annotatedString.getStringAnnotations(tag = "NOTE_ACTION", start = 0, end = annotatedString.length)
                                        
                                        var closestNote: String? = null
                                        var minDistance = Float.MAX_VALUE

                                        noteAnnotations.forEach { annotation ->
                                            val firstLine = layout.getLineForOffset(annotation.start)
                                            val lastLine = layout.getLineForOffset(annotation.end)
                                            
                                            for (lineIndex in firstLine..lastLine) {
                                                val left = if (lineIndex == firstLine) layout.getHorizontalPosition(annotation.start, true) else layout.getLineLeft(lineIndex)
                                                val right = if (lineIndex == lastLine) layout.getHorizontalPosition(annotation.end, true) else layout.getLineRight(lineIndex)
                                                val top = layout.getLineTop(lineIndex)
                                                val bottom = layout.getLineBottom(lineIndex)
                                                
                                                // Extend hit area slightly for better UX (slop)
                                                val slop = with(density) { 8.dp.toPx() }
                                                if (offset.x >= left - slop && offset.x <= right + slop &&
                                                    offset.y >= top - slop && offset.y <= bottom + slop) {
                                                    
                                                    // Calculate distance to center of this line segment for priority
                                                    val centerX = (left + right) / 2f
                                                    val centerY = (top + bottom) / 2f
                                                    val dx = offset.x - centerX
                                                    val dy = offset.y - centerY
                                                    val dist = sqrt(dx * dx + dy * dy)
                                                    
                                                    if (dist < minDistance) {
                                                        minDistance = dist
                                                        closestNote = annotation.item
                                                    }
                                                }
                                            }
                                        }

                                        // 2. Si hay una nota cerca (Hit Slop), siempre tiene prioridad
                                        closestNote?.let { noteKey ->
                                            val noteParts = noteKey.split("_")
                                            if (noteParts.size == 3) {
                                                notesViewModel.openEditor(
                                                    verseKey = noteKey,
                                                    bookId = noteParts[0].toInt(),
                                                    chapter = noteParts[1].toInt(),
                                                    verseNumber = noteParts[2].toInt()
                                                )
                                                return@detectTapGestures
                                            }
                                        }

                                        // 3. Si no hay nota, verificamos si se tocó el texto de un versículo (Marcadores)
                                        annotatedString.getStringAnnotations(tag = "VERSE", start = charOffset, end = charOffset)
                                            .firstOrNull()?.let { annotation ->
                                                val verseNum = annotation.item.toInt()
                                                verses.find { it.verse == verseNum }?.let { verse ->
                                                    val rect = layout.getBoundingBox(charOffset)
                                                    tappedVerseOffset = Offset(rect.left + rect.width / 2f, rect.top)
                                                    showColorPickerByVerse = verse
                                                }
                                            }
                                    }
                                }
                            },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = (fontSize * 1.8f).sp
                        ),
                        inlineContent = inlineContent,
                        onTextLayout = { textLayoutResult = it }
                    )
                }

                showColorPickerByVerse?.let { verse ->
                    val bookmark = bookmarks.find { 
                        (it.bookId == verse.book_id) && (it.chapter == verse.chapter) && (it.verse == verse.verse) 
                    }
                    
                    val verseKey = remember(verse) {
                        "${verse.book_id}_${verse.chapter}_${verse.verse}"
                    }
                    val hasNote = verseKey in notedVerseKeys

                    Popup(
                        popupPositionProvider = object : PopupPositionProvider {
                            override fun calculatePosition(
                                anchorBounds: IntRect,
                                windowSize: IntSize,
                                layoutDirection: LayoutDirection,
                                popupContentSize: IntSize
                            ): IntOffset {
                                // Anchor position in window coordinates
                                val anchorWindowTopLeft = anchorBounds.topLeft
                                
                                // Point to center above (relative to anchor)
                                val xLocal = tappedVerseOffset.x
                                val yLocal = tappedVerseOffset.y
                                
                                // Absolute window coordinates
                                val xWindow = anchorWindowTopLeft.x + xLocal
                                val yWindow = anchorWindowTopLeft.y + yLocal
                                
                                // Vertical adjustment: place popup ABOVE the line
                                val margin = with(density) { 8.dp.toPx() }
                                val finalY = yWindow - popupContentSize.height - margin
                                
                                // Horizontal adjustment: center the popup on X
                                var finalX = xWindow - (popupContentSize.width / 2f)
                                
                                // Clamping to screen width
                                val horizontalMargin = with(density) { 16.dp.toPx() }
                                if (finalX < horizontalMargin) finalX = horizontalMargin
                                if (finalX + popupContentSize.width > windowSize.width - horizontalMargin) {
                                    finalX = (windowSize.width - popupContentSize.width - horizontalMargin)
                                }
                                
                                return IntOffset(finalX.toInt(), finalY.toInt())
                            }
                        },
                        onDismissRequest = { showColorPickerByVerse = null },
                        properties = PopupProperties(focusable = true)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp,
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .widthIn(max = 340.dp)
                                .border(androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(24.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LazyRow(
                                    modifier = Modifier.weight(1f, fill = false),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                ) {
                                    items(categories, key = { it.id }) { category ->
                                        val isSelected = bookmark?.categoryId == category.id
                                        ColorOption(
                                            category = category,
                                            isSelected = isSelected,
                                            onClick = {
                                                viewModel.toggleBookmark(verse, category.id, bookName)
                                                showColorPickerByVerse = null
                                            }
                                        )
                                    }
                                }

                                VerticalDivider(
                                    modifier = Modifier
                                        .height(32.dp)
                                        .padding(horizontal = 4.dp),
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )

                                IconButton(
                                    onClick = {
                                        notesViewModel.openEditor(
                                            verseKey = verseKey,
                                            bookId = verse.book_id,
                                            chapter = verse.chapter,
                                            verseNumber = verse.verse
                                        )
                                        showColorPickerByVerse = null
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.EditNote,
                                        contentDescription = if (hasNote) "Editar nota" else "Añadir nota",
                                        tint = if (hasNote) Color(0xFFB9915A) else MaterialTheme.colorScheme.primary
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val abbr = translations.find { it.id == selectedTranslationId }?.abbreviation ?: ""
                                        val shareText = "\"${verse.text}\"\n— $bookName ${verse.chapter}:${verse.verse} ($abbr)\n\nCompartido desde Verbo Libre"
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, null)
                                        context.startActivity(shareIntent)
                                        showColorPickerByVerse = null
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Compartir",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                val editorState by notesViewModel.editorState.collectAsState()
                editorState?.let { state ->
                    val verse = verses.find { it.verse == state.verseNumber }
                    val translation = remember(translations, selectedTranslationId) {
                        translations.find { it.id == selectedTranslationId }
                    }
                    NoteEditorSheet(
                        state = state,
                        verseText = verse?.text.orEmpty(),
                        reference = "$bookName ${state.chapter}:${state.verseNumber}",
                        translationName = translation?.abbreviation ?: (translation?.name ?: ""),
                        onTextChange = notesViewModel::updateDraftText,
                        onSave = notesViewModel::saveCurrentNote,
                        onDelete = notesViewModel::deleteCurrentNote,
                        onDismiss = notesViewModel::closeEditor
                    )
                }
            }
        }
    }
}
}

@Composable
fun FontSizeButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .border(androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(15.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ColorOption(
    category: BookmarkCategoryEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = parseColor(category.colorHex)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color, CircleShape)
                .then(
                    if (isSelected) Modifier.border(androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary), CircleShape)
                    else Modifier
                )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun parseColor(colorHex: String): Color {
    return try {
        Color(colorHex.toColorInt())
    } catch (e: Exception) {
        println("Color parsing error: ${e.message}")
        Color.Gray
    }
}

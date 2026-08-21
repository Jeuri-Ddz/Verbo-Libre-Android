package com.mi.bibliarv1960.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mi.bibliarv1960.data.local.entities.BookEntity
import com.mi.bibliarv1960.ui.components.ChapterSelectionDialog
import com.mi.bibliarv1960.ui.viewmodel.BibleViewModel
import java.text.Normalizer
import java.util.regex.Pattern
import com.mi.bibliarv1960.ui.theme.LinoIcons
import com.mi.bibliarv1960.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: BibleViewModel,
    onChapterSelected: (bookId: Int, chapter: Int, verse: Int?) -> Unit,
    onNavigate: (String) -> Unit,
    onOpenDrawer: () -> Unit,
) {
    val allBooks by viewModel.allBooks.collectAsState()
    val todayVerse by viewModel.displayDailyVerse.collectAsState()
    val bookProgress by viewModel.bookProgress.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedBook by remember { mutableStateOf<BookEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var searchError by remember { mutableStateOf<String?>(null) }

    val filteredBooks = allBooks.filter { book ->
        val normalizedSearch = searchQuery.normalize()
        val normalizedBookName = book.name.normalize()
        val testamentFilter = if (selectedTab == 0) book.testament == 0 else book.testament == 1
        val searchFilter = normalizedBookName.contains(normalizedSearch, ignoreCase = true)
        if (searchQuery.isEmpty()) testamentFilter else searchFilter
    }

    fun performSearch() {
        searchError = null
        if (searchQuery.isBlank()) return
        val regex = Regex("^([a-záéíóúñ0-9° ]+?)\\s+(\\d+)(?::(\\d+))?$", RegexOption.IGNORE_CASE)
        val match = regex.find(searchQuery.trim())
        if (match != null) {
            val bookPrefix = match.groupValues[1].trim().normalize()
            val chapter = match.groupValues[2].toIntOrNull() ?: 1
            val verse = match.groupValues[3].takeIf { it.isNotEmpty() }?.toIntOrNull()
            val foundBook = allBooks.find { it.name.normalize().startsWith(bookPrefix, ignoreCase = true) }
            if (foundBook != null) {
                if (chapter > foundBook.chaptersCount) {
                    searchError = "${foundBook.name} solo tiene ${foundBook.chaptersCount} capítulos"
                } else {
                    onChapterSelected(foundBook.id, chapter, verse)
                }
            } else {
                searchError = "Libro no encontrado"
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Verbo Libre", 
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        val selectedTranslationId by viewModel.selectedTranslationId.collectAsState()
                        val translations by viewModel.allTranslations.collectAsState()
                        val currentTranslation = translations.find { it.id == selectedTranslationId }
                        currentTranslation?.let {
                            Text(
                                text = it.abbreviation,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            imageVector = LinoIcons.MenuAsymmetric,
                            contentDescription = "Menú",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                windowInsets = WindowInsets(0.dp, 24.dp, 0.dp, 0.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp, 24.dp, 0.dp, 0.dp)
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(
                top = innerPadding.calculateTopPadding(),
                start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                bottom = 0.dp
            )
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { 
                    searchQuery = it
                    searchError = null 
                },
                onSearchAction = { performSearch() },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (searchError != null) {
                Text(
                    text = searchError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 4.dp)
                )
            } else {
                Text(
                    text = "Escribe un libro, capítulo o 'libro cap:versículo' para saltar directo",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 4.dp)
                )
            }

            // Daily Verse Banner
            todayVerse?.let { dv ->
                Surface(
                    onClick = { onNavigate(Screen.DailyVerse.route) },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                                )
                            )
                            .padding(vertical = 14.dp, horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "☀", fontSize = 18.sp, color = Color.White, modifier = Modifier.padding(end = 16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "VERSÍCULO DE HOY",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.7f),
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = dv.verseText,
                                fontSize = 13.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Segmented Control Tabs
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SegmentedTab(
                        text = "Antiguo Testamento",
                        isSelected = selectedTab == 0,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedTab = 0 }
                    )
                    SegmentedTab(
                        text = "Nuevo Testamento",
                        isSelected = selectedTab == 1,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedTab = 1 }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredBooks) { book ->
                    val progress = bookProgress[book.id] ?: Pair(0, book.chaptersCount)
                    BookRow(
                        book = book,
                        readChapters = progress.first,
                        totalChapters = progress.second,
                        onClick = { selectedBook = book }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 22.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }

    selectedBook?.let { book ->
        ChapterSelectionDialog(
            book = book,
            onChapterSelected = { chapter ->
                selectedBook = null
                onChapterSelected(book.id, chapter, null)
            }
        ) { selectedBook = null }
    }
}

@Composable
fun SegmentedTab(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = if (isSelected) 2.dp else 0.dp,
        modifier = modifier
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(vertical = 10.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, onSearchAction: () -> Unit, modifier: Modifier = Modifier) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Buscar libro y capítulo, ej. génesis 1:2", fontSize = 14.sp) },
        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                Text(
                    text = "Ir",
                    modifier = Modifier.clickable { onSearchAction() }.padding(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        shape = RoundedCornerShape(14.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { onSearchAction() })
    )
}

@Composable
fun BookRow(book: BookEntity, readChapters: Int, totalChapters: Int, onClick: () -> Unit) {
    val progress = if (totalChapters > 0) readChapters.toFloat() / totalChapters.toFloat() else 0f
    val isComplete = readChapters == totalChapters && totalChapters > 0
    val percentage = (progress * 100).toInt()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = book.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isComplete) Color(0xFF3C9D6B) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "$totalChapters capítulos · $readChapters/$totalChapters leídos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Barra de progreso personalizada sin decoraciones (puntos) en los extremos
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(if (isComplete) Color(0xFF3C9D6B) else Color(0xFF33506E))
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = "›", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun String.normalize(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
    val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
    return pattern.matcher(normalized).replaceAll("")
}

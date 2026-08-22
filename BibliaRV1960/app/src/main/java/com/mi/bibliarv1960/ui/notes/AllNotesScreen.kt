package com.mi.bibliarv1960.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mi.bibliarv1960.data.local.entities.BookEntity
import com.mi.bibliarv1960.data.local.entities.NoteEntity
import com.mi.bibliarv1960.ui.components.ContextualTooltip
import com.mi.bibliarv1960.ui.theme.LinoIcons
import com.mi.bibliarv1960.ui.viewmodel.BibleViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllNotesScreen(
    notes: List<NoteEntity>,
    books: List<BookEntity>,
    viewModel: BibleViewModel,
    onNoteClick: (NoteEntity) -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }

    // --- Tooltip Contextual ---
    val tooltipSeen by viewModel.tooltipNotesSearch.collectAsState()
    var searchBarCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val tooltipText = "Usa el buscador para filtrar tus notas por cualquier palabra o frase que hayas escrito."

    LaunchedEffect(tooltipSeen, searchBarCoordinates) {
        if (!tooltipSeen && (searchBarCoordinates != null)) {
            kotlinx.coroutines.delay(1000.milliseconds)
            viewModel.speakTooltip(tooltipText)
        }
    }

    val filtered = remember(notes, query) {
        notes.filter { note ->
            if (query.isBlank()) true else note.text.contains(query, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Mis notas", 
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            imageVector = LinoIcons.MenuAsymmetric, 
                            contentDescription = "Menú", 
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0.dp, 24.dp, 0.dp, 0.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp, 24.dp, 0.dp, 0.dp)
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .onGloballyPositioned { searchBarCoordinates = it },
                placeholder = { Text("Buscar en mis notas...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                )
            )

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (query.isBlank()) "Aún no has escrito notas" else "Sin resultados",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontFamily = FontFamily.Serif
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filtered, key = { it.id }) { note ->
                        val bookName = books.find { it.id == note.bookId }?.name ?: "Libro ${note.bookId}"

                        NoteListItem(
                            note = note, 
                            bookName = bookName
                        ) { onNoteClick(note) }
                    }
                }
            }
        }
    }

    if (!tooltipSeen && (searchBarCoordinates != null)) {
        ContextualTooltip(
            targetCoordinates = searchBarCoordinates,
            text = tooltipText,
            onDismiss = { viewModel.dismissNotesSearchTooltip() }
        )
    }
}

@Composable
private fun NoteListItem(note: NoteEntity, bookName: String, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("d MMM", Locale("es", "ES")) }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$bookName ${note.chapter}:${note.verseNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Serif,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dateFormat.format(Date(note.updatedAt)).lowercase(),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = note.text,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = 3
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoteListItemPreview() {
    MaterialTheme {
        NoteListItem(
            note = NoteEntity(
                id = 1,
                verseKey = "1_1_1",
                bookId = 1,
                chapter = 1,
                verseNumber = 1,
                text = "Esta es una nota de prueba para verificar el diseño del item.",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            bookName = "Génesis",
            onClick = {}
        )
    }
}

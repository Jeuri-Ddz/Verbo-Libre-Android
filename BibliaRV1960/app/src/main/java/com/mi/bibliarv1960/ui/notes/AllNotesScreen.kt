package com.mi.bibliarv1960.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mi.bibliarv1960.data.local.entities.BookEntity
import com.mi.bibliarv1960.data.local.entities.NoteEntity
import com.mi.bibliarv1960.data.local.entities.TranslationEntity
import com.mi.bibliarv1960.ui.theme.LinoIcons
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllNotesScreen(
    notes: List<NoteEntity>,
    books: List<BookEntity>,
    translations: List<TranslationEntity>,
    onNoteClick: (NoteEntity) -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var selectedTranslations by remember { mutableStateOf(setOf<String>()) }

    val filtered = remember(notes, query, selectedTranslations) {
        notes.filter { note ->
            val matchesQuery = if (query.isBlank()) true else note.text.contains(query, ignoreCase = true)
            val translationId = note.verseKey.split("_").firstOrNull() ?: ""
            val matchesTranslation = if (selectedTranslations.isEmpty()) true else selectedTranslations.contains(translationId)
            matchesQuery && matchesTranslation
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
                    .padding(horizontal = 20.dp, vertical = 8.dp),
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

            // Version Filters
            if (translations.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(translations) { translation ->
                        val isSelected = selectedTranslations.contains(translation.id)
                        TranslationFilterChip(
                            translation = translation,
                            isSelected = isSelected,
                            onClick = {
                                selectedTranslations = if (isSelected) {
                                    selectedTranslations - translation.id
                                } else {
                                    selectedTranslations + translation.id
                                }
                            }
                        )
                    }
                }
            }

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
                        val translationId = note.verseKey.split("_").firstOrNull()
                        val translation = translations.find { it.id == translationId }
                        val translationName = translation?.abbreviation ?: translation?.name

                        NoteListItem(
                            note = note, 
                            bookName = bookName,
                            translationName = translationName,
                            onClick = { onNoteClick(note) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteListItem(note: NoteEntity, bookName: String, translationName: String?, onClick: () -> Unit) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$bookName ${note.chapter}:${note.verseNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Serif,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (translationName != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = translationName,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }
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

@Composable
private fun TranslationFilterChip(
    translation: TranslationEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorAccent = MaterialTheme.colorScheme.primary
    
    val backgroundColor = if (isSelected) colorAccent.copy(alpha = 0.15f) else Color.Transparent
    val borderColor = if (isSelected) colorAccent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
    val textColor = if (isSelected) colorAccent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(colorAccent, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = translation.abbreviation,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
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
                verseKey = "rv1960_1_1_1",
                bookId = 1,
                chapter = 1,
                verseNumber = 1,
                text = "Esta es una nota de prueba para verificar el diseño del item.",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            bookName = "Génesis",
            translationName = "RV1960",
            onClick = {}
        )
    }
}

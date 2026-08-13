package com.mi.bibliarv1960.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mi.bibliarv1960.data.local.entities.BookEntity
import com.mi.bibliarv1960.data.local.entities.NoteEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val NavyColor = Color(0xFF2E4A66)
private val GoldColor = Color(0xFFB9915A)
private val CreamColor = Color(0xFFFBFAF6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllNotesScreen(
    notes: List<NoteEntity>,
    books: List<BookEntity>,
    onNoteClick: (NoteEntity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }

    val filtered = remember(notes, query) {
        if (query.isBlank()) notes
        else notes.filter { it.text.contains(query, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Mis notas", 
                        fontFamily = FontFamily.Serif, 
                        fontWeight = FontWeight.Bold, 
                        color = NavyColor
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Atrás", 
                            tint = NavyColor
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CreamColor
                ),
                windowInsets = WindowInsets(0.dp, 24.dp, 0.dp, 0.dp)
            )
        },
        containerColor = CreamColor,
        contentWindowInsets = WindowInsets(0.dp, 24.dp, 0.dp, 0.dp)
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(CreamColor)
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
                    focusedBorderColor = GoldColor,
                    unfocusedBorderColor = NavyColor.copy(alpha = 0.1f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                )
            )

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (query.isBlank()) "Aún no has escrito notas" else "Sin resultados",
                        color = NavyColor.copy(alpha = 0.5f),
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
                            bookName = bookName,
                            onClick = { onNoteClick(note) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteListItem(note: NoteEntity, bookName: String, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("d MMM", Locale("es", "ES")) }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
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
                    fontFamily = FontFamily.Serif,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyColor
                )
                Text(
                    text = dateFormat.format(Date(note.updatedAt)).lowercase(),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = note.text,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = NavyColor.copy(alpha = 0.8f),
                maxLines = 3
            )
        }
    }
}

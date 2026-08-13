package com.mi.bibliarv1960.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mi.bibliarv1960.data.local.entities.NoteEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val NavyColor = Color(0xFF2E4A66)
private val GoldColor = Color(0xFFB9915A)
private val CreamColor = Color(0xFFFBFAF6)

/**
 * IA: conecta el parámetro onNoteClick a la navegación real del proyecto
 * para saltar al versículo correspondiente (bookId/chapter/verseNumber
 * ya vienen en cada NoteEntity).
 */
@Composable
fun AllNotesScreen(
    notes: List<NoteEntity>,
    onNoteClick: (NoteEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }

    val filtered = remember(notes, query) {
        if (query.isBlank()) notes
        else notes.filter { it.text.contains(query, ignoreCase = true) }
    }

    Column(modifier = modifier.fillMaxSize().background(CreamColor)) {
        Text(
            text = "Mis notas",
            fontFamily = FontFamily.Serif,
            fontSize = 24.sp,
            color = NavyColor,
            modifier = Modifier.padding(20.dp)
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            placeholder = { Text("Buscar en mis notas...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldColor,
                unfocusedBorderColor = NavyColor.copy(alpha = 0.2f)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (query.isBlank()) "Aún no has escrito notas" else "Sin resultados",
                    color = NavyColor.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { note ->
                    NoteListItem(note = note, onClick = { onNoteClick(note) })
                }
            }
        }
    }
}

@Composable
private fun NoteListItem(note: NoteEntity, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("d MMM yyyy", Locale("es", "ES")) }

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    // IA: reemplaza por el nombre real del libro usando bookId
                    // (probablemente ya tienes una función bookName(bookId) 
                    // o una tabla de libros existente en el proyecto)
                    text = "Libro ${note.bookId} ${note.chapter}:${note.verseNumber}",
                    fontFamily = FontFamily.Serif,
                    fontSize = 15.sp,
                    color = NavyColor
                )
                Text(
                    text = dateFormat.format(Date(note.updatedAt)),
                    fontSize = 11.sp,
                    color = NavyColor.copy(alpha = 0.4f)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = note.text,
                fontSize = 13.sp,
                color = NavyColor.copy(alpha = 0.8f),
                maxLines = 3
            )
        }
    }
}

package com.mi.bibliarv1960.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mi.bibliarv1960.data.local.entities.BookEntity

@Composable
fun ChapterSelectionDialog(
    book: BookEntity,
    readChapters: Set<Int>,
    lastReadChapter: Int?,
    onChapterSelected: (Int) -> Unit,
    onContinueReading: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Seleccionar Capítulo: ${book.name}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    items(book.chaptersCount) { index ->
                        val chapter = index + 1
                        val isRead = readChapters.contains(chapter)
                        val isLastRead = chapter == lastReadChapter
                        
                        ChapterItem(
                            chapter = chapter,
                            isRead = isRead,
                            isLastRead = isLastRead
                        ) { onChapterSelected(chapter) }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(
                onClick = onContinueReading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF33506E)
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text("Continuar lectura", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun ChapterItem(
    chapter: Int,
    isRead: Boolean,
    isLastRead: Boolean,
    onClick: () -> Unit
) {
    val successColor = Color(0xFF3C9D6B) 
    val lastReadBorderColor = Color(0xFFB9915A)
    
    // Detectamos modo claro/oscuro de forma robusta
    val isLight = MaterialTheme.colorScheme.background == Color(0xFFFAFAF7)

    val backgroundColor = when {
        isRead -> {
            if (isLight) MaterialTheme.colorScheme.primary // Azul Oscuro Navy
            else successColor.copy(alpha = 0.15f) // Verde en modo oscuro
        }
        else -> {
            if (isLight) MaterialTheme.colorScheme.secondaryContainer // El azul clarito base
            else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) // Base oscura
        }
    }
    
    val contentColor = when {
        isRead -> {
            if (isLight) MaterialTheme.colorScheme.onPrimary // Blanco sobre Navy
            else successColor // Verde sobre oscuro
        }
        else -> {
            MaterialTheme.colorScheme.onSecondaryContainer
        }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = if (isLastRead) androidx.compose.foundation.BorderStroke(2.dp, lastReadBorderColor) else null,
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = chapter.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isRead || isLastRead) FontWeight.ExtraBold else FontWeight.Bold,
                    color = contentColor
                )
                if (isRead) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = if (isLight) Color.White else successColor
                    )
                }
            }
        }
    }
}

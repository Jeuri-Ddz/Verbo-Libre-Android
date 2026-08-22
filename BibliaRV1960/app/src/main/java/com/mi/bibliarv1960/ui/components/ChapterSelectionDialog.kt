package com.mi.bibliarv1960.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.remember
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
    // OPTIMIZACIÓN: Detectar modo y preparar colores UNA SOLA VEZ para todo el diálogo
    val isLight = MaterialTheme.colorScheme.background == Color(0xFFFAFAF7)
    val themeColors = remember(isLight) {
        ChapterThemeColors(
            isLight = isLight,
            primary = if (isLight) Color(0xFF33506E) else Color(0xFF6E93B8), // LinoAccent vs DarkAccent
            onPrimary = if (isLight) Color.White else Color(0xFF1A1B1D),
            secondaryContainer = if (isLight) Color(0xFFD8E1E8) else Color(0xFF2A3B52),
            onSecondaryContainer = if (isLight) Color(0xFF33506E) else Color(0xFF6E93B8),
            success = Color(0xFF3C9D6B),
            lastReadBorder = Color(0xFFB9915A)
        )
    }

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
                    items(book.chaptersCount, key = { it }) { index ->
                        val chapter = index + 1
                        val isRead = readChapters.contains(chapter)
                        val isLastRead = chapter == lastReadChapter
                        
                        ChapterItem(
                            chapter = chapter,
                            isRead = isRead,
                            isLastRead = isLastRead,
                            theme = themeColors
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
                    containerColor = themeColors.primary
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text("Continuar lectura", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

data class ChapterThemeColors(
    val isLight: Boolean,
    val primary: Color,
    val onPrimary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val success: Color,
    val lastReadBorder: Color
)

@Composable
fun ChapterItem(
    chapter: Int,
    isRead: Boolean,
    isLastRead: Boolean,
    theme: ChapterThemeColors,
    onClick: () -> Unit
) {
    // OPTIMIZACIÓN: Cálculos de color simplificados y jerarquía de UI plana
    val backgroundColor = remember(isRead, theme) {
        if (isRead) {
            if (theme.isLight) theme.primary
            else theme.success.copy(alpha = 0.15f)
        } else {
            if (theme.isLight) theme.secondaryContainer
            else theme.secondaryContainer.copy(alpha = 0.5f)
        }
    }
    
    val contentColor = remember(isRead, theme) {
        if (isRead) {
            if (theme.isLight) theme.onPrimary
            else theme.success
        } else {
            theme.onSecondaryContainer
        }
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .then(
                if (isLastRead) Modifier.border(2.dp, theme.lastReadBorder, RoundedCornerShape(12.dp))
                else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
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
                    tint = if (theme.isLight) Color.White else theme.success
                )
            }
        }
    }
}

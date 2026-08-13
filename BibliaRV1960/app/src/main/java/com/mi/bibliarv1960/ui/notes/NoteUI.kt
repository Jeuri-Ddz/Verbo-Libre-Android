package com.mi.bibliarv1960.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Paleta reutilizada del proyecto (navy/gold/cream), ver DevotionalCard.kt.
 * IA: si estos valores ya existen centralizados en un Theme.kt, usa esas
 * referencias en vez de estos literales.
 */
private val NavyColor = Color(0xFF2E4A66)
private val GoldColor = Color(0xFFB9915A)
private val CreamColor = Color(0xFFFBFAF6)

/**
 * Ícono pequeño junto a un versículo. Relleno (sólido) si ya tiene nota,
 * contorno si no. Tocarlo abre el editor.
 */
@Composable
fun VerseNoteIndicator(
    hasNote: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(28.dp)
    ) {
        if (hasNote) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(GoldColor, CircleShape)
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.EditNote,
                contentDescription = "Añadir nota",
                tint = NavyColor.copy(alpha = 0.35f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Hoja inferior para escribir o editar la nota de un versículo.
 * IA: conecta verseText (el texto del versículo en cuestión) desde donde
 * ya tengas ese dato disponible en la pantalla de lectura.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorSheet(
    state: NoteEditorState,
    verseText: String,
    reference: String, // ej: "Juan 3:16"
    onTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val GoldColor = Color(0xFFB9915A)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = reference,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = verseText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = GoldColor.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 220.dp),
                placeholder = { Text("Escribe tu reflexión o notas de estudio...") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!state.isNew) {
                    TextButton(onClick = onDelete) {
                        Text("Eliminar", color = Color(0xFFB33A3A))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Button(
                    onClick = onSave,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Guardar", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

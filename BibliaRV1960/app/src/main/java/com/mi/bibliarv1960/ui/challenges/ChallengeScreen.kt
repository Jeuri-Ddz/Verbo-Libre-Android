package com.mi.bibliarv1960.ui.challenges

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mi.bibliarv1960.data.local.entities.ChallengeEntity
import com.mi.bibliarv1960.ui.components.LinoButton
import com.mi.bibliarv1960.ui.components.LinoButtonVariant
import androidx.compose.foundation.layout.FlowRow

@Composable
fun ChallengeScreen(
    viewModel: ChallengeViewModel,
    onClose: () -> Unit
) {
    val challenge by viewModel.currentChallenge.collectAsState()
    val status by viewModel.challengeStatus.collectAsState()
    val completedCount by viewModel.completedCount.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Header (Fijo arriba)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFB9915A), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Retos acertados: $completedCount",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            // 2. Contenedor del reto Adaptativo (Full Screen Adaptable)
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val maxHeightPx = this.constraints.maxHeight
                
                // PASO 1: Calcular tamaño inicial según altura disponible
                val initialTextSize = remember(maxHeightPx) {
                    when {
                        maxHeightPx < 400 -> 14.sp
                        maxHeightPx < 600 -> 18.sp
                        maxHeightPx < 800 -> 22.sp
                        else -> 26.sp
                    }
                }

                // PASO 2: Estados separados para medición y ajuste
                var textSize by remember(challenge) { mutableStateOf(initialTextSize) }
                var contentHeight by remember { mutableIntStateOf(0) }
                var iterations by remember { mutableIntStateOf(0) }

                // PASO 3: LaunchedEffect que reduce en UN SOLO PASO proporcional
                LaunchedEffect(contentHeight, maxHeightPx, iterations) {
                    if ((maxHeightPx > 0) && (contentHeight > 0) && (iterations < 20)) {
                        val ratio = contentHeight.toFloat() / maxHeightPx.toFloat()
                        if (ratio > 0.95f) {
                            // Calcular reducción exacta necesaria
                            val reductionFactor = (ratio - 0.85f).coerceIn(0.1f, 0.4f)
                            val newSize = (textSize.value * (1f - reductionFactor)).coerceAtLeast(14f)
                            if (newSize < textSize.value) {
                                textSize = newSize.sp
                                iterations++  // Safety net: máximo 20 intentos
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { size ->
                            contentHeight = size.height  // Solo actualiza la medición
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Punto rojo decorativo
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFE4574C), CircleShape)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "RETO DE HOY",
                        style = MaterialTheme.typography.labelLarge,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )

                    challenge?.let { ch ->
                        Log.d("ChallengeScreen", "Cargando reto ID: ${ch.id}, Tipo: ${ch.type}")
                        Text(
                            text = if (ch.type == "FILL_VERSE") "Completa el versículo" else "Trivia bíblica",
                            fontSize = if (ch.type == "FILL_VERSE") (textSize.value * 1.1f).sp else textSize,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                            textAlign = TextAlign.Center
                        )

                        if (ch.type == "FILL_VERSE") {
                            FillVerseLayout(
                                challenge = ch,
                                status = status,
                                currentTextSize = textSize
                            ) { result, correct ->
                                viewModel.submitResult(result, correct)
                            }
                        } else {
                            TriviaLayout(ch, status, currentTextSize = textSize) { index ->
                                viewModel.submitResult(ChallengeResult.Trivia(index), index == ch.correctIndex)
                            }
                        }
                    }
                }
            }

            // 3. Footer (Fijo abajo)
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (status?.isCompleted == true) {
                    Text(
                        text = "¡Vuelve mañana para un nuevo reto!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LinoButton(
                        text = "Continuar",
                        onClick = onClose,
                        variant = LinoButtonVariant.PRIMARY
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Firma para testeo (Clickable para ciclar retos)
                Text(
                    text = "Verbo Libre",
                    fontFamily = FontFamily.Serif,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .clickable { viewModel.nextChallengePreview() }
                )
            }
        }
    }
}

@Composable
fun FillVerseLayout(
    challenge: ChallengeEntity,
    status: DailyChallengeStatus?,
    currentTextSize: TextUnit = 24.sp,
    onCheck: (ChallengeResult.FillVerse, Boolean) -> Unit
) {
    val verseText = challenge.verseText ?: ""
    val correctWords = challenge.correctWords ?: emptyList()
    
    // PASO 8: Validar consistencia de correctWords con placeholders
    val placeholderCount = Regex("\\{\\d+\\}").findAll(verseText).count()
    if (correctWords.isEmpty() || placeholderCount != correctWords.size) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Reto no disponible",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Por favor, intenta de nuevo más tarde",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
        return
    }

    val allWordsBank = remember(challenge) {
        (correctWords + (challenge.distractors ?: emptyList())).shuffled()
    }

    var userSelections by remember(challenge) { mutableStateOf(List(correctWords.size) { "" }) }

    LaunchedEffect(status) {
        val result = status?.result
        if (result is ChallengeResult.FillVerse) userSelections = result.userWords
    }

    val isLocked = status?.isCompleted == true

    // PASO 4: Calcular effectiveTextSize según longitud máxima de palabras
    val effectiveTextSize = remember(currentTextSize, correctWords) {
        val maxWordLength = (correctWords + (challenge.distractors ?: emptyList()))
            .maxOfOrNull { it.length } ?: 0
        when {
            maxWordLength > 14 -> (currentTextSize.value * 0.65f).sp
            maxWordLength > 12 -> (currentTextSize.value * 0.7f).sp
            maxWordLength > 10 -> (currentTextSize.value * 0.8f).sp
            else -> currentTextSize
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val isAllFilled = userSelections.none { it.isEmpty() }
        
        // PASO 2: Renderizar versículo secuencialmente con FlowRow (Inline Blanks)
        val parts = verseText.split(Regex("\\{\\d+\\}"))
        
        // Altura de línea uniforme para evitar saltos inconsistentes
        val lineSpacing = (effectiveTextSize.value * 2.2f).dp

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Center
        ) {
            parts.forEachIndexed { index, part ->
                if (part.isNotBlank()) {
                    Box(
                        modifier = Modifier.height(lineSpacing),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = part.trim() + " ",
                            fontSize = effectiveTextSize,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                if (index < userSelections.size) {
                    val word = userSelections[index]
                    val isCorrect = isLocked && word == correctWords.getOrNull(index)
                    val isWrong = isLocked && word.isNotEmpty() && !isCorrect
                    
                    val underlineColor = when {
                        isCorrect -> Color(0xFF3C9D6B)
                        isWrong -> Color(0xFFE4574C)
                        word.isNotEmpty() -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    }

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .height(lineSpacing)
                            .widthIn(min = 64.dp)
                            .drawBehind {
                                val strokeWidth = 1.5.dp.toPx()
                                val y = size.height - 6.dp.toPx()
                                drawLine(
                                    color = underlineColor,
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = strokeWidth
                                )
                            }
                            .clickable(enabled = !isLocked && word.isNotEmpty()) {
                                val newList = userSelections.toMutableList()
                                newList[index] = ""
                                userSelections = newList
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = word,
                            fontSize = effectiveTextSize,
                            fontFamily = FontFamily.Serif,
                            fontStyle = if (word.isNotEmpty()) FontStyle.Italic else FontStyle.Normal,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            softWrap = false,
                            color = if (word.isNotEmpty()) underlineColor else Color.Transparent,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                    // Espacio sutil después del blank
                    Spacer(modifier = Modifier.width(2.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!isLocked) {
            LinoButton(
                text = "Comprobar versículo",
                onClick = {
                    val isCorrect = userSelections == correctWords
                    onCheck(ChallengeResult.FillVerse(userSelections), isCorrect)
                },
                variant = LinoButtonVariant.PRIMARY,
                enabled = isAllFilled,
                fixedHeight = 44.dp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 2. Banco de palabras o Explicación
        if (isLocked) {
            ChallengeExplanation(
                reference = challenge.reference,
                explanation = challenge.explanation,
                currentTextSize = currentTextSize
            )
        } else {
            Text(
                text = "Toca las palabras para completar:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // PASO 5: FlowRow dinámico en el banco de palabras
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allWordsBank.forEach { word ->
                    val isUsed = userSelections.contains(word)
                    Surface(
                        onClick = {
                            if (!isUsed) {
                                val firstEmpty = userSelections.indexOfFirst { it.isEmpty() }
                                if (firstEmpty != -1) {
                                    val newList = userSelections.toMutableList()
                                    newList[firstEmpty] = word
                                    userSelections = newList
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = if (isUsed) 0.dp else 2.dp,
                        color = if (isUsed) Color.Transparent else MaterialTheme.colorScheme.surface,
                        border = if (isUsed)
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        enabled = !isUsed,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .wrapContentWidth()
                            .heightIn(min = 44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = word,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = (currentTextSize.value * 0.7f).coerceAtLeast(14f).sp,
                                maxLines = 1,
                                softWrap = false,
                                color = if (isUsed) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TriviaLayout(
    challenge: ChallengeEntity,
    status: DailyChallengeStatus?,
    currentTextSize: TextUnit = 24.sp,
    onAnswer: (Int) -> Unit
) {
    val isLocked = status?.isCompleted == true
    val selectedIndex = (status?.result as? ChallengeResult.Trivia)?.selectedIndex ?: -1

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Pregunta con estilo elegante
        Text(
            text = challenge.question ?: "Cargando pregunta...",
            style = MaterialTheme.typography.headlineSmall,
            fontSize = currentTextSize,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            lineHeight = (currentTextSize.value * 1.3f).sp,
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 28.dp)
        )

        // 2. Opciones de respuesta
        val options = challenge.options ?: emptyList()
        options.forEachIndexed { index, option ->
            val isSelected = index == selectedIndex
            val isCorrect = isLocked && index == challenge.correctIndex
            val isWrong = isLocked && isSelected && !isCorrect

            Surface(
                onClick = { if (!isLocked) onAnswer(index) },
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 0.dp,
                color = when {
                    isCorrect -> Color(0xFF3C9D6B).copy(alpha = 0.12f)
                    isWrong -> Color(0xFFE4574C).copy(alpha = 0.12f)
                    isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                },
                border = BorderStroke(
                    width = if (isCorrect || isWrong || isSelected) 2.dp else 1.dp,
                    color = when {
                        isCorrect -> Color(0xFF3C9D6B)
                        isWrong -> Color(0xFFE4574C)
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Círculo indicador A, B, C, D
                    Surface(
                        shape = CircleShape,
                        tonalElevation = 0.dp,
                        color = when {
                            isCorrect -> Color(0xFF3C9D6B)
                            isWrong -> Color(0xFFE4574C)
                            isSelected -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = listOf("A", "B", "C", "D").getOrElse(index) { "?" },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = if (isCorrect || isWrong || isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = (currentTextSize.value * 0.75f).coerceAtLeast(14f).sp,
                        fontWeight = if (isSelected || isCorrect) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 3. Resultado y Explicación (Solo si está bloqueado/completado)
        if (isLocked) {
            Spacer(modifier = Modifier.height(20.dp))
            ChallengeExplanation(
                reference = challenge.reference,
                explanation = challenge.explanation,
                currentTextSize = currentTextSize
            )
        }
    }
}

@Composable
fun ChallengeExplanation(
    reference: String,
    explanation: String,
    currentTextSize: TextUnit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                RoundedCornerShape(16.dp)
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = reference,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB9915A)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = explanation,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = (currentTextSize.value * 0.65f).coerceAtLeast(14f).sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            lineHeight = (currentTextSize.value * 1.0f).sp
        )
    }
}

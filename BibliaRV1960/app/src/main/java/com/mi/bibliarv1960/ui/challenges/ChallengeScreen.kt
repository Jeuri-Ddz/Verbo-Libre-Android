package com.mi.bibliarv1960.ui.challenges

import android.util.Log
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mi.bibliarv1960.data.local.entities.ChallengeEntity
import com.mi.bibliarv1960.ui.components.LinoButton
import com.mi.bibliarv1960.ui.components.LinoButtonVariant

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
        color = MaterialTheme.colorScheme.background
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
                var textSize by remember(challenge) { mutableStateOf(24.sp) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { layoutCoordinates ->
                            val currentHeight = layoutCoordinates.size.height
                            if (maxHeightPx > 0 && currentHeight > maxHeightPx * 0.95f && textSize.value > 12f) {
                                if (textSize.value > 12.5f) {
                                    textSize = (textSize.value - 0.5f).sp
                                } else {
                                    textSize = 12.sp
                                }
                            }
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
                            FillVerseLayout(ch, status, currentTextSize = textSize, onCheck = { result, correct ->
                                viewModel.submitResult(result, correct)
                            })
                        } else {
                            TriviaLayout(ch, status, currentTextSize = textSize, onAnswer = { index ->
                                viewModel.submitResult(ChallengeResult.Trivia(index), index == ch.correctIndex)
                            })
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
    currentTextSize: androidx.compose.ui.unit.TextUnit = 24.sp,
    onCheck: (ChallengeResult.FillVerse, Boolean) -> Unit
) {
    val correctWords = challenge.correctWords ?: emptyList()
    val allWordsBank = remember(challenge) {
        (correctWords + (challenge.distractors ?: emptyList())).shuffled()
    }
    
    var userSelections by remember(challenge) { mutableStateOf(List(correctWords.size) { "" }) }

    LaunchedEffect(status) {
        if (status?.result is ChallengeResult.FillVerse) {
            userSelections = (status.result as ChallengeResult.FillVerse).userWords
        }
    }

    val isLocked = status?.isCompleted == true

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Área del Versículo con huecos
        val verseText = challenge.verseText ?: ""
        val parts = if (verseText.isNotEmpty()) {
            verseText.split(Regex("\\{\\d+\\}"))
        } else {
            emptyList()
        }
        val totalElementsCount = parts.size + userSelections.size
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var currentIndex = 0
            val maxPerRow = 2 
            val rows = (0 until totalElementsCount).chunked(maxPerRow)
            
            rows.forEach { rowIndices ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    rowIndices.forEach { globalIndex ->
                        val partIdx = globalIndex / 2
                        if (globalIndex % 2 == 0) {
                            // Fragmento de texto
                            if (partIdx < parts.size) {
                                Text(
                                    text = parts[partIdx],
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontSize = currentTextSize,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = (currentTextSize.value * 1.4f).sp,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        } else {
                            // Hueco interactivo / Palabra colocada
                            if (partIdx < userSelections.size) {
                                val word = userSelections[partIdx]
                                val isCorrect = isLocked && word == correctWords[partIdx]
                                val isWrong = isLocked && word != correctWords[partIdx] && word.isNotEmpty()
                                
                                Surface(
                                    onClick = { 
                                        if (!isLocked && word.isNotEmpty()) {
                                            val newList = userSelections.toMutableList()
                                            newList[partIdx] = ""
                                            userSelections = newList
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = when {
                                        isCorrect -> Color(0xFF3C9D6B).copy(alpha = 0.15f)
                                        isWrong -> Color(0xFFE4574C).copy(alpha = 0.15f)
                                        word.isEmpty() -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    },
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 1.5.dp,
                                        color = when {
                                            isCorrect -> Color(0xFF3C9D6B)
                                            isWrong -> Color(0xFFE4574C)
                                            word.isEmpty() -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                ) {
                                    Text(
                                        text = word.ifEmpty { "        " },
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontSize = currentTextSize,
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            isCorrect -> Color(0xFF3C9D6B)
                                            isWrong -> Color(0xFFE4574C)
                                            else -> MaterialTheme.colorScheme.primary
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        currentIndex++
                    }
                }
            }
        }

        // 2. Banco de palabras o Explicación
        if (isLocked) {
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
                    text = challenge.reference,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB9915A)
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = challenge.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = (currentTextSize.value * 0.65f).sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    lineHeight = (currentTextSize.value * 1.0f).sp
                )
            }
        } else {
            // Banco de palabras estilizado como Chips
            Text(
                text = "Toca las palabras para completar:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                allWordsBank.chunked(3).forEach { wordRow ->
                    Row(
                        modifier = Modifier.padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        wordRow.forEach { word ->
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
                                color = if (isUsed) Color.Transparent else MaterialTheme.colorScheme.surface,
                                shadowElevation = if (isUsed) 0.dp else 2.dp,
                                border = if (isUsed) 
                                    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                enabled = !isUsed
                            ) {
                                Text(
                                    text = word,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = (currentTextSize.value * 0.7f).sp,
                                    color = if (isUsed) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            if (userSelections.none { it.isEmpty() }) {
                Spacer(modifier = Modifier.height(24.dp))
                LinoButton(
                    text = "Comprobar versículo",
                    onClick = {
                        val isCorrect = userSelections == correctWords
                        onCheck(ChallengeResult.FillVerse(userSelections), isCorrect)
                    },
                    variant = LinoButtonVariant.PRIMARY
                )
            }
        }
    }
}

@Composable
fun TriviaLayout(
    challenge: ChallengeEntity,
    status: DailyChallengeStatus?,
    currentTextSize: androidx.compose.ui.unit.TextUnit = 24.sp,
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
                color = when {
                    isCorrect -> Color(0xFF3C9D6B).copy(alpha = 0.12f)
                    isWrong -> Color(0xFFE4574C).copy(alpha = 0.12f)
                    isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                },
                border = androidx.compose.foundation.BorderStroke(
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
                        fontSize = (currentTextSize.value * 0.75f).sp,
                        fontWeight = if (isSelected || isCorrect) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 3. Resultado y Explicación (Solo si está bloqueado/completado)
        if (isLocked) {
            Spacer(modifier = Modifier.height(20.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = challenge.reference,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB9915A)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = challenge.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = (currentTextSize.value * 0.65f).sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    lineHeight = (currentTextSize.value * 1.0f).sp
                )
            }
        }
    }
}

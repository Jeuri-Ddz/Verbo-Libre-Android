package com.mi.bibliarv1960.ui.challenges

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.mi.bibliarv1960.data.local.entities.ChallengeEntity
import com.mi.bibliarv1960.ui.components.LinoButton
import com.mi.bibliarv1960.ui.components.LinoButtonVariant
import com.mi.bibliarv1960.ui.theme.BibliaRV1960Theme

@Composable
fun ChallengeScreen(
    viewModel: ChallengeViewModel,
    onClose: () -> Unit
) {
    val challenge by viewModel.currentChallenge.collectAsState()
    val status by viewModel.challengeStatus.collectAsState()
    val completedCount by viewModel.completedCount.collectAsState()

    ChallengeScreenContent(
        challenge = challenge,
        status = status,
        completedCount = completedCount,
        onClose = onClose,
        onCheckFillVerse = { result, correct -> viewModel.submitResult(result, correct) },
        onCheckTrivia = { index -> 
            val correct = index == challenge?.correctIndex
            viewModel.submitResult(ChallengeResult.Trivia(index), correct)
        },
        onNextChallengePreview = { viewModel.nextChallengePreview() }
    )
}

@Composable
private fun ChallengeScreenContent(
    challenge: ChallengeEntity?,
    status: DailyChallengeStatus?,
    completedCount: Int,
    onClose: () -> Unit,
    onCheckFillVerse: (ChallengeResult.FillVerse, Boolean) -> Unit,
    onCheckTrivia: (Int) -> Unit,
    onNextChallengePreview: () -> Unit
) {
    // Levantamiento de estado para FillVerse
    val correctWords = challenge?.correctWords ?: emptyList()
    var userSelections by remember(challenge) {
        val result = status?.result
        val initial = if (result is ChallengeResult.FillVerse) {
            result.userWords
        } else {
            List(correctWords.size) { "" }
        }
        mutableStateOf(initial)
    }

    val allWordsBank = remember(challenge) {
        (correctWords + (challenge?.distractors ?: emptyList())).shuffled()
    }

    var selectedTriviaIndex by remember(challenge) {
        val result = status?.result
        val initial = if (result is ChallengeResult.Trivia) result.selectedIndex else -1
        mutableStateOf(initial)
    }

    val isLocked = status?.isCompleted == true
    val allBlanksFilled = userSelections.none { it.isEmpty() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Header (Fijo) - Ahora incluye el título del reto
            ChallengeHeader(
                challenge = challenge,
                completedCount = completedCount,
                onClose = onClose
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Content (Flexible) - Únicamente el contenido variable
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                challenge?.let { ch ->
                    if (ch.type == "FILL_VERSE") {
                        FillVerseLayout(
                            challenge = ch,
                            status = status,
                            userSelections = userSelections,
                            onBlankClick = { index ->
                                if (!isLocked) {
                                    val newList = userSelections.toMutableList()
                                    newList[index] = ""
                                    userSelections = newList
                                }
                            }
                        )
                    } else {
                        TriviaLayout(
                            challenge = ch,
                            status = status,
                            selectedIndex = selectedTriviaIndex,
                            onSelect = { index ->
                                if (!isLocked) selectedTriviaIndex = index
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Footer (Banco + Botón + Branding)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isLocked && (challenge?.type == "FILL_VERSE" || challenge?.type == "TRIVIA")) {
                    if (challenge.type == "FILL_VERSE") {
                        WordBankFlowRow(
                            options = allWordsBank,
                            userSelections = userSelections,
                            onWordClick = { word ->
                                val firstEmpty = userSelections.indexOfFirst { it.isEmpty() }
                                if (firstEmpty != -1) {
                                    val newList = userSelections.toMutableList()
                                    newList[firstEmpty] = word
                                    userSelections = newList
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    val isEnabled = if (challenge.type == "FILL_VERSE") allBlanksFilled else selectedTriviaIndex != -1
                    val buttonText = if (challenge.type == "FILL_VERSE") "Comprobar versículo" else "Comprobar respuesta"

                    LinoButton(
                        text = buttonText,
                        onClick = {
                            if (challenge.type == "FILL_VERSE") {
                                val isCorrect = userSelections == correctWords
                                onCheckFillVerse(ChallengeResult.FillVerse(userSelections), isCorrect)
                            } else {
                                onCheckTrivia(selectedTriviaIndex)
                            }
                        },
                        variant = LinoButtonVariant.ACCENT,
                        enabled = isEnabled,
                        fixedHeight = 44.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                BrandingFooter(onClick = onNextChallengePreview)
            }
        }
    }
}

@Composable
private fun ChallengeHeader(
    challenge: ChallengeEntity?,
    completedCount: Int,
    onClose: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFB9915A),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Retos acertados: $completedCount",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }

        if (challenge != null) {
            Spacer(modifier = Modifier.height(16.dp))
            // Indicador visual superior
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

            Text(
                text = if (challenge.type == "FILL_VERSE") "Completa el versículo" else "Trivia bíblica",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun FillVerseLayout(
    challenge: ChallengeEntity,
    status: DailyChallengeStatus?,
    userSelections: List<String>,
    onBlankClick: (Int) -> Unit
) {
    val verseText = challenge.verseText ?: ""
    val correctWords = challenge.correctWords ?: emptyList()
    
    val fullVerseForLength = remember(challenge) {
        var text = verseText
        correctWords.forEachIndexed { i, word ->
            text = text.replace("{$i}", word)
        }
        text
    }

    val verseFontSize = when (fullVerseForLength.length) {
        in 0..60 -> 22.sp
        in 61..90 -> 19.sp
        else -> 16.sp
    }

    val isLocked = status?.isCompleted == true

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val parts = verseText.split(Regex("\\{\\d+\\}"))
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Center
        ) {
            parts.forEachIndexed { index, part ->
                if (part.isNotBlank()) {
                    Text(
                        text = part.trim() + " ",
                        style = TextStyle(
                            fontSize = verseFontSize,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 1.9.em,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }

                if (index < userSelections.size) {
                    val word = userSelections[index]
                    val isCorrect = isLocked && word == correctWords.getOrNull(index)
                    val isWrong = isLocked && word.isNotEmpty() && !isCorrect
                    
                    val underlineColor = when {
                        isCorrect -> Color(0xFF3C9D6B)
                        isWrong -> Color(0xFFE4574C)
                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
                    }

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .widthIn(min = 60.dp)
                            .drawBehind {
                                val strokeWidth = 1.5.dp.toPx()
                                val y = size.height - 4.dp.toPx()
                                drawLine(
                                    color = underlineColor,
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = strokeWidth
                                )
                            }
                            .clickable(enabled = !isLocked && word.isNotEmpty()) {
                                onBlankClick(index)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = word,
                            style = TextStyle(
                                fontSize = verseFontSize,
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Normal,
                                color = if (word.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Transparent,
                                textAlign = TextAlign.Center
                            ),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        if (isLocked) {
            Spacer(modifier = Modifier.height(32.dp))
            ChallengeExplanation(
                reference = challenge.reference,
                explanation = challenge.explanation
            )
        }
    }
}

@Composable
fun TriviaLayout(
    challenge: ChallengeEntity,
    status: DailyChallengeStatus?,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val isLocked = status?.isCompleted == true
    val finalSelectedIndex = if (isLocked) {
        (status.result as? ChallengeResult.Trivia)?.selectedIndex ?: selectedIndex
    } else {
        selectedIndex
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = challenge.question ?: "",
            style = TextStyle(
                fontSize = 20.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 1.4.em,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        val options = challenge.options ?: emptyList()
        options.forEachIndexed { index, option ->
            val isSelected = index == finalSelectedIndex
            val isCorrect = isLocked && index == challenge.correctIndex
            val isWrong = isLocked && isSelected && !isCorrect

            val triviaOptionFontSize = when (option.length) {
                in 0..30 -> 16.sp
                else -> 14.sp
            }

            Surface(
                onClick = { if (!isLocked) onSelect(index) },
                shape = RoundedCornerShape(12.dp),
                color = when {
                    isCorrect -> Color(0xFF3C9D6B).copy(alpha = 0.1f)
                    isWrong -> Color(0xFFE4574C).copy(alpha = 0.1f)
                    isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                },
                border = BorderStroke(
                    width = 1.dp,
                    color = when {
                        isCorrect -> Color(0xFF3C9D6B)
                        isWrong -> Color(0xFFE4574C)
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = option,
                    style = TextStyle(
                        fontSize = triviaOptionFontSize,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    textAlign = TextAlign.Start
                )
            }
        }

        if (isLocked) {
            Spacer(modifier = Modifier.height(16.dp))
            ChallengeExplanation(
                reference = challenge.reference,
                explanation = challenge.explanation
            )
        }
    }
}

@Composable
private fun WordBankFlowRow(
    options: List<String>,
    userSelections: List<String>,
    onWordClick: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { word ->
            val isUsed = userSelections.contains(word)
            
            Surface(
                onClick = { if (!isUsed) onWordClick(word) },
                shape = RoundedCornerShape(8.dp),
                color = if (isUsed) Color.Transparent else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .wrapContentWidth()
                    .heightIn(min = 44.dp)
                    .alpha(if (isUsed) 0.3f else 1f),
                enabled = !isUsed
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = word,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
fun ChallengeExplanation(
    reference: String,
    explanation: String
) {
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
            text = reference,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB9915A)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = explanation,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun BrandingFooter(onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Verbo Libre",
            fontFamily = FontFamily.Serif,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
            modifier = Modifier
                .padding(vertical = 8.dp)
                .clickable { onClick() }
        )
    }
}

// Previews
@Preview(showBackground = true, widthDp = 360)
@Composable
fun PreviewShortVerse() {
    val challenge = ChallengeEntity(
        id = 1,
        type = "FILL_VERSE",
        reference = "Salmos 23:1",
        verseText = "Jehová es mi {0}; nada me {1}.",
        correctWords = listOf("pastor", "faltará"),
        distractors = listOf("guía", "sucederá"),
        explanation = "La figura del pastor garantiza provisión y cuidado."
    )
    BibliaRV1960Theme {
        ChallengeScreenContent(
            challenge = challenge,
            status = null,
            completedCount = 5,
            onClose = {},
            onCheckFillVerse = { _, _ -> },
            onCheckTrivia = {},
            onNextChallengePreview = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun PreviewLongVerse() {
    val challenge = ChallengeEntity(
        id = 2,
        type = "FILL_VERSE",
        reference = "Isaías 41:10",
        verseText = "No {0}, que yo soy contigo; no {1}, que yo soy tu Dios que te esfuerzo: siempre te {2}, siempre te sustentaré con la diestra de mi justicia.",
        correctWords = listOf("temas", "desmayes", "ayudaré"),
        distractors = listOf("llores", "temas", "cuidaré"),
        explanation = "Dios promete su compañía y sostén en todo momento."
    )
    BibliaRV1960Theme {
        ChallengeScreenContent(
            challenge = challenge,
            status = null,
            completedCount = 10,
            onClose = {},
            onCheckFillVerse = { _, _ -> },
            onCheckTrivia = {},
            onNextChallengePreview = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun PreviewWordBankLongWords() {
    val challenge = ChallengeEntity(
        id = 3,
        type = "FILL_VERSE",
        reference = "Proverbios 3:5",
        verseText = "Fíate de {0} de todo tu {1}, y no estribes en tu {2}.",
        correctWords = listOf("Jehová", "corazón", "prudencia"),
        distractors = listOf("entendimiento", "conocimiento", "sabiduría"),
        explanation = "Confiar en Dios supera nuestra propia lógica."
    )
    BibliaRV1960Theme {
        ChallengeScreenContent(
            challenge = challenge,
            status = null,
            completedCount = 15,
            onClose = {},
            onCheckFillVerse = { _, _ -> },
            onCheckTrivia = {},
            onNextChallengePreview = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 280) // Pantalla angosta
@Composable
fun PreviewNarrowScreen() {
    val challenge = ChallengeEntity(
        id = 4,
        type = "TRIVIA",
        reference = "Juan 14:6",
        question = "¿Cuál es el camino, la verdad y la vida?",
        options = listOf("Las buenas obras", "El conocimiento", "Jesucristo", "La religión"),
        correctIndex = 2,
        explanation = "Jesús es el único camino al Padre."
    )
    BibliaRV1960Theme {
        ChallengeScreenContent(
            challenge = challenge,
            status = null,
            completedCount = 20,
            onClose = {},
            onCheckFillVerse = { _, _ -> },
            onCheckTrivia = {},
            onNextChallengePreview = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun PreviewCheckButtonStates() {
    val challenge = ChallengeEntity(
        id = 5,
        type = "FILL_VERSE",
        reference = "Mateo 6:33",
        verseText = "Mas buscad {0} el {1} de Dios.",
        correctWords = listOf("primeramente", "reino"),
        distractors = listOf("siempre", "camino"),
        explanation = ""
    )
    BibliaRV1960Theme {
        Column {
            Text("Deshabilitado:")
            ChallengeScreenContent(
                challenge = challenge,
                status = null,
                completedCount = 0,
                onClose = {},
                onCheckFillVerse = { _, _ -> },
                onCheckTrivia = {},
                onNextChallengePreview = {}
            )
        }
    }
}

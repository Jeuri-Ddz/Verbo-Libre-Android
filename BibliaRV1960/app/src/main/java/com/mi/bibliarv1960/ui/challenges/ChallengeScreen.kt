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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
    onClose: () -> Unit,
) {
    val challenge by viewModel.currentChallenge.collectAsState()
    val status by viewModel.challengeStatus.collectAsState()
    val completedCount by viewModel.completedCount.collectAsState()
    val isStatusLoaded by viewModel.isStatusLoaded.collectAsState()

    if (!isStatusLoaded || (challenge == null)) {
        // Loading state (Solo cabecera mínima o Box vacío para evitar parpadeo)
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            }
        }
    } else {
        ChallengeScreenContent(
            challenge = challenge,
            status = status,
            completedCount = completedCount,
            onClose = onClose,
            onCheckTrivia = { index -> 
                val correct = index == challenge?.correctIndex
                viewModel.submitResult(ChallengeResult.Trivia(index), correct)
            }
        ) { viewModel.nextChallengePreview() }
    }
}

@Composable
private fun ChallengeScreenContent(
    challenge: ChallengeEntity?,
    status: DailyChallengeStatus?,
    completedCount: Int,
    onClose: () -> Unit,
    onCheckTrivia: (Int) -> Unit,
    onNextChallengePreview: () -> Unit
) {
    val initial = (status?.result as? ChallengeResult.Trivia)?.selectedIndex ?: -1
    var selectedTriviaIndex by remember(challenge) { mutableIntStateOf(initial) }

    val isLocked = status?.isCompleted == true

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
                    TriviaLayout(
                        challenge = ch,
                        status = status,
                        selectedIndex = selectedTriviaIndex,
                    ) { index ->
                        if (!isLocked) selectedTriviaIndex = index
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Footer (Banco + Botón + Branding)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isLocked && (challenge != null)) {
                    LinoButton(
                        text = "Comprobar respuesta",
                        onClick = { onCheckTrivia(selectedTriviaIndex) },
                        variant = LinoButtonVariant.ACCENT,
                        enabled = selectedTriviaIndex != -1,
                        fixedHeight = 44.dp,
                        modifier = Modifier
                            .fillMaxWidth()
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
    onClose: () -> Unit,
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
                text = "Trivia bíblica",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
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
fun PreviewTrivia() {
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
            onCheckTrivia = {},
            onNextChallengePreview = {}
        )
    }
}

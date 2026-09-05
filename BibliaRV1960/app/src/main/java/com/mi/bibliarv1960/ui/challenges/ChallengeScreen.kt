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
import androidx.compose.ui.text.font.FontStyle
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
    LaunchedEffect(Unit) {
        viewModel.markChallengeVisited()
    }

    val challenge by viewModel.currentChallenge.collectAsState()
    val status by viewModel.challengeStatus.collectAsState()
    val completedCount by viewModel.completedCount.collectAsState()
    val isStatusLoaded by viewModel.isStatusLoaded.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFDFBF5) // Fondo cremita suave
    ) {
        if (!isStatusLoaded || (challenge == null)) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFB9915A).copy(alpha = 0.5f))
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ChallengeHeader(
            completedCount = completedCount,
            onClose = onClose
        )

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            challenge?.let { ch ->
                TriviaLayout(
                    challenge = ch,
                    status = status,
                    selectedIndex = selectedTriviaIndex,
                    onCheckTrivia = onCheckTrivia,
                    onSelect = { index ->
                        if (!isLocked) selectedTriviaIndex = index
                    }
                )
            }
        }

        BrandingFooter(onClick = onNextChallengePreview)
    }
}

@Composable
private fun ChallengeHeader(
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
                    tint = Color(0xFF2B4A5E).copy(alpha = 0.4f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFB9915A),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Retos acertados: $completedCount",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2B4A5E).copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun TriviaLayout(
    challenge: ChallengeEntity,
    status: DailyChallengeStatus?,
    selectedIndex: Int,
    onCheckTrivia: (Int) -> Unit,
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
        // Títulos en el flujo centrado
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(Color(0xFFB9915A), CircleShape)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "RETO DE HOY",
            style = MaterialTheme.typography.labelLarge,
            letterSpacing = 2.sp,
            color = Color(0xFFB9915A),
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Trivia bíblica",
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2B4A5E),
            modifier = Modifier.padding(top = 4.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = challenge.question ?: "",
            style = TextStyle(
                fontSize = 22.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 1.4.em,
                color = Color(0xFF2B4A5E)
            ),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        val options = challenge.options ?: emptyList()
        options.forEachIndexed { index, option ->
            val isSelected = index == finalSelectedIndex
            val isCorrect = isLocked && index == challenge.correctIndex
            val isWrong = isLocked && isSelected && !isCorrect

            val triviaOptionFontSize = if (option.length > 40) 15.sp else 16.sp

            Surface(
                onClick = { if (!isLocked) onSelect(index) },
                shape = RoundedCornerShape(14.dp),
                color = when {
                    isCorrect -> Color(0xFF3C9D6B).copy(alpha = 0.1f)
                    isWrong -> Color(0xFFE4574C).copy(alpha = 0.1f)
                    isSelected -> Color(0xFFB9915A).copy(alpha = 0.12f)
                    else -> Color.White
                },
                border = BorderStroke(
                    width = 1.dp,
                    color = when {
                        isCorrect -> Color(0xFF3C9D6B)
                        isWrong -> Color(0xFFE4574C)
                        isSelected -> Color(0xFFB9915A)
                        else -> Color(0xFF2B4A5E).copy(alpha = 0.08f)
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Text(
                    text = option,
                    style = TextStyle(
                        fontSize = triviaOptionFontSize,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isLocked && !isSelected && !isCorrect) Color(0xFF2B4A5E).copy(alpha = 0.4f) else Color(0xFF2B4A5E)
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    textAlign = TextAlign.Start
                )
            }
        }

        if (isLocked) {
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color(0xFFB9915A).copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(24.dp))
            ChallengeExplanation(
                reference = challenge.reference,
                explanation = challenge.explanation
            )
        } else {
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { onCheckTrivia(selectedIndex) },
                enabled = selectedIndex != -1,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2B4A5E),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF2B4A5E).copy(alpha = 0.3f)
                )
            ) {
                Text("Comprobar respuesta", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = reference,
            style = MaterialTheme.typography.titleMedium,
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB9915A)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = explanation,
            style = TextStyle(
                fontSize = 17.sp,
                fontFamily = FontFamily.Serif,
                lineHeight = 1.5.em,
                textAlign = TextAlign.Start,
                color = Color(0xFF2B4A5E).copy(alpha = 0.8f)
            )
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
            color = Color(0xFF2B4A5E).copy(alpha = 0.3f),
            modifier = Modifier
                .padding(vertical = 16.dp)
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

package com.mi.bibliarv1960.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mi.bibliarv1960.R
import com.mi.bibliarv1960.ui.components.LinoButton
import com.mi.bibliarv1960.ui.components.LinoButtonVariant
import com.mi.bibliarv1960.ui.components.ShareableVerseCard
import com.mi.bibliarv1960.ui.navigation.Screen
import com.mi.bibliarv1960.ui.viewmodel.BibleViewModel
import com.mi.bibliarv1960.utils.ShareUtils
import kotlinx.coroutines.launch

@Composable
fun DailyVerseScreen(
    viewModel: BibleViewModel,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) {
        viewModel.markDailyVerseVisited()
    }

    val dailyVerse by viewModel.displayDailyVerse.collectAsState()
    val dailyBgIndex by viewModel.displayVerseBgIndex.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()

    val bgResourceId = when (dailyBgIndex) {
        1 -> R.drawable.devotional_bg_1
        2 -> R.drawable.devotional_bg_2
        3 -> R.drawable.devotional_bg_3
        4 -> R.drawable.devotional_bg_4
        5 -> R.drawable.devotional_bg_5
        else -> 0
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- 1. CAPA INVISIBLE PARA CAPTURA (OFF-SCREEN) ---
        Box(
            modifier = Modifier
                .size(360.dp, 640.dp) // Proporción 9:16
                .alpha(0f) 
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }
                }
        ) {
            dailyVerse?.let { dv ->
                ShareableVerseCard(
                    bgResourceId = bgResourceId,
                    verseText = dv.verseText,
                    reference = dv.reference,
                    title = null,
                    reflection = null
                )
            }
        }

        // --- 2. UI VISIBLE ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onBack() } 
        ) {
            // Capa de fondo
            if (bgResourceId != 0) {
                Image(
                    painter = painterResource(id = bgResourceId),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(2.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF6B7D81), Color(0xFF28362F))
                            )
                        )
                        .blur(2.dp)
                )
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título Superior
                Text(
                    text = "Verbo Libre",
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .padding(top = 64.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                dailyVerse?.let { dv ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "\"${dv.verseText}\"",
                            fontSize = 28.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 40.sp,
                            fontFamily = FontFamily.Serif,
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        Text(
                            text = dv.reference.uppercase(),
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            letterSpacing = 3.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }

                    Spacer(modifier = Modifier.weight(1.2f))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LinoButton(
                            text = "Ver devocional de hoy",
                            variant = LinoButtonVariant.PRIMARY,
                            onClick = { onNavigate(Screen.Devotional.route) }
                        )

                        LinoButton(
                            text = "Leer capítulo completo",
                            variant = LinoButtonVariant.OUTLINE,
                            onClick = { onNavigate(Screen.Reader.createRoute(dv.bookId, dv.chapter, dv.verse)) }
                        )

                        LinoButton(
                            text = "↗ Compartir",
                            variant = LinoButtonVariant.GHOST,
                            onClick = {
                                coroutineScope.launch {
                                    val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                    val shareText = "\"${dv.verseText}\"\n— ${dv.reference}\n\nCompartido desde Verbo Libre"
                                    ShareUtils.shareBitmap(context, bitmap, shareText)
                                }
                            },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "O, toca en cualquier lugar para continuar",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp),
                )
            }
        }
    }
}

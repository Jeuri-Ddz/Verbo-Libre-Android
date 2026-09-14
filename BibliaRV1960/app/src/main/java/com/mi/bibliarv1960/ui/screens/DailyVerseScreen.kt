package com.mi.bibliarv1960.ui.screens

import android.os.Build
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mi.bibliarv1960.R
import com.mi.bibliarv1960.ui.components.LinoButton
import com.mi.bibliarv1960.ui.components.LinoButtonVariant
import com.mi.bibliarv1960.ui.navigation.Screen
import com.mi.bibliarv1960.ui.theme.CormorantFamily
import com.mi.bibliarv1960.ui.theme.VigiliaDarkPalette
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
                .width(360.dp)
                .wrapContentHeight()
                .alpha(0f) 
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }
                }
        ) {
            dailyVerse?.let { dv ->
                DailyVerseScene(
                    verseText = dv.verseText,
                    reference = dv.reference,
                    bgResourceId = bgResourceId,
                    isCapture = true
                )
            }
        }

        // --- 2. UI VISIBLE ---
        dailyVerse?.let { dv ->
            DailyVerseScene(
                verseText = dv.verseText,
                reference = dv.reference,
                bgResourceId = bgResourceId,
                isCapture = false,
                onBackgroundClick = onBack,
                actions = {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
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
            )
        }
    }
}

@Composable
fun DailyVerseScene(
    verseText: String,
    reference: String,
    bgResourceId: Int,
    isCapture: Boolean = false,
    onBackgroundClick: (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null
) {
    val vigiliaColors = VigiliaDarkPalette
    
    val sceneModifier = if (isCapture) {
        Modifier.width(360.dp).wrapContentHeight()
    } else {
        Modifier.fillMaxSize().then(
            if (onBackgroundClick != null) Modifier.clickable { onBackgroundClick() } else Modifier
        )
    }

    Box(modifier = sceneModifier.background(vigiliaColors.void)) {
        // --- 1. FONDO FOTOGRÁFICO ---
        if (bgResourceId != 0) {
            val isApi31 = Build.VERSION.SDK_INT >= 31
            val colorMatrix = remember {
                ColorMatrix().apply {
                    setToSaturation(1.05f)
                    val m = values
                    m[0] *= 0.82f
                    m[6] *= 0.82f
                    m[12] *= 0.82f
                }
            }
            Image(
                painter = painterResource(id = bgResourceId),
                contentDescription = null,
                colorFilter = ColorFilter.colorMatrix(colorMatrix),
                modifier = Modifier
                    .matchParentSize()
                    .scale(1.12f)
                    .graphicsLayer { clip = true }
                    .then(if (isApi31) Modifier.blur(8.dp) else Modifier),
                contentScale = ContentScale.Crop
            )
        }
        
        // Scrim (Velo)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        0.00f to vigiliaColors.scrim0,
                        0.24f to vigiliaColors.scrim24,
                        0.62f to vigiliaColors.scrim62,
                        1.00f to vigiliaColors.scrim100
                    )
                )
        )

        if (!isCapture) {
            GrainOverlay()
        }

        Column(
            modifier = if (isCapture) Modifier.width(360.dp).wrapContentHeight() else Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título Superior
            Text(
                text = "Verbo Libre",
                fontSize = 24.sp,
                fontFamily = CormorantFamily,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier
                    .padding(top = if (isCapture) 40.dp else 64.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
            ) {
                Text(
                    text = "\"${verseText}\"",
                    fontSize = 32.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 44.sp,
                    fontFamily = CormorantFamily,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = reference.uppercase(),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 3.sp,
                    fontFamily = CormorantFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.weight(1.2f))
            
            if (isCapture) {
                Text(
                    text = "Alimenta tu alma cada día",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 40.dp)
                )
            } else {
                actions?.invoke()
                Spacer(modifier = Modifier.height(32.dp))
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

package com.mi.bibliarv1960.ui.screens

import android.app.Activity
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.mi.bibliarv1960.data.local.entities.DevotionalEntity
import com.mi.bibliarv1960.ui.components.DevotionalTier
import com.mi.bibliarv1960.ui.components.DropCapText
import com.mi.bibliarv1960.ui.theme.*
import com.mi.bibliarv1960.ui.viewmodel.BibleViewModel
import com.mi.bibliarv1960.utils.DevotionalUtils
import com.mi.bibliarv1960.utils.ShareUtils
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random

@Composable
fun DevotionalScreen(
    viewModel: BibleViewModel,
    onBack: () -> Unit,
) {
    val devotional by viewModel.displayDevotional.collectAsState()
    val dailyBgIndex by viewModel.todayDevotionalBgIndex.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val isReadToday by viewModel.isReadToday.collectAsState()
    
    val vigiliaColors = VigiliaDarkPalette
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    val snackbarHostState = remember { SnackbarHostState() }

    var showStreakInfo by remember { mutableStateOf(false) }

    // Animaciones de entrada (Efecto "Rise")
    val contentAlpha = remember { Animatable(0f) }
    val contentOffsetY = remember { Animatable(10f) }

    LaunchedEffect(devotional) {
        if (devotional != null) {
            contentAlpha.snapTo(0f)
            contentOffsetY.snapTo(10f)
            launch { contentAlpha.animateTo(1f, tween(550, easing = LinearOutSlowInEasing)) }
            launch { contentOffsetY.animateTo(0f, tween(550, easing = LinearOutSlowInEasing)) }
        }
    }

    SideEffect {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false
    }

    LaunchedEffect(Unit) {
        viewModel.markDevotionalVisited()
    }

    @Suppress("DiscouragedApi")
    val bgResourceId = remember(dailyBgIndex) {
        val bgResName = "devotional_bg_$dailyBgIndex"
        context.resources.getIdentifier(bgResName, "drawable", context.packageName)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        
        // --- 0. CAPA INVISIBLE PARA CAPTURA (OFF-SCREEN) ---
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
            devotional?.let { dev ->
                VigiliaDevotionalScene(
                    devotional = dev,
                    bgResourceId = bgResourceId,
                    isCapture = true
                )
            }
        }

        // --- 1. ESCENA VISUAL PRINCIPAL ---
        devotional?.let { dev ->
            VigiliaDevotionalScene(
                devotional = dev,
                bgResourceId = bgResourceId,
                contentAlpha = contentAlpha.value,
                contentOffsetY = contentOffsetY.value,
                isCapture = false
            )
        }

        // --- 2. CAPA DE INTERFAZ (BOTONES) ---
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .padding(top = 48.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                        contentDescription = "Volver", 
                        tint = vigiliaColors.parchmentInk.copy(alpha = 0.65f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .clickable { showStreakInfo = true }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔥 $currentStreak días",
                        color = vigiliaColors.candle,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = InterFamily
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Acciones Inferiores (Sin fondo ni borde)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                devotional?.let { dev ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Acción: Marcar
                        TextButton(
                            onClick = { viewModel.markAsRead() },
                            enabled = !isReadToday,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = vigiliaColors.parchmentInk.copy(alpha = 0.62f),
                                disabledContentColor = vigiliaColors.candle
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Check, 
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = if (isReadToday) "Leído" else "Leído hoy",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.5.sp,
                                    fontFamily = InterFamily
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(40.dp))

                        // Acción: Compartir
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                    val shareText = "*${dev.topic}*\n\"${dev.verseText}\"\n— ${dev.reference}\n\n${dev.reflection}\n\nCompartido desde Verbo Libre"
                                    ShareUtils.shareBitmap(context, bitmap, shareText)
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = vigiliaColors.parchmentInk.copy(alpha = 0.62f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                Icon(
                                    imageVector = Icons.Outlined.Share, 
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Compartir",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.5.sp,
                                    fontFamily = InterFamily
                                )
                            }
                        }
                    }
                }

                // Footer Invisible para clicks de debug
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .height(30.dp)
                        .width(120.dp)
                        .clickable { viewModel.showNextDevotionalDebug() }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 150.dp)
        )
    }

    if (showStreakInfo) {
        AlertDialog(
            onDismissRequest = { showStreakInfo = false },
            title = { Text("¿Qué son las rachas?") },
            text = { 
                Text("Tu racha aumenta cada día que marcas un devocional como leído. Si fallas un día, el contador vuelve a cero. ¡Mantén el hábito!") 
            },
            confirmButton = {
                TextButton(onClick = { showStreakInfo = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}

@Composable
fun VigiliaDevotionalScene(
    devotional: DevotionalEntity,
    bgResourceId: Int,
    contentAlpha: Float = 1f,
    contentOffsetY: Float = 0f,
    isCapture: Boolean = false
) {
    val vigiliaColors = VigiliaDarkPalette
    
    val sceneModifier = if (isCapture) {
        Modifier.width(360.dp).wrapContentHeight()
    } else {
        Modifier.fillMaxSize()
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

        // --- 2. EFECTOS ANIMADOS (Solo si no es captura) ---
        if (!isCapture) {
            CandleGlow(vigiliaColors.candle)
            MoteParticles(vigiliaColors.candle)
        }

        // --- 3. CONTENIDO PRINCIPAL ---
        Column(
            modifier = if (isCapture) Modifier.width(360.dp).wrapContentHeight() else Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Espaciado Superior: Menor en captura para aprovechar espacio
            Spacer(modifier = Modifier.height(if (isCapture) 60.dp else 110.dp))

            // Área de Lectura
            val tier = DevotionalUtils.calculateVerseTier(devotional.verseText)
            val totalLength = devotional.verseText.length + devotional.reflection.length
            val needsScroll = !isCapture && totalLength > 320

            val contentModifier = if (isCapture) {
                Modifier.fillMaxWidth().padding(horizontal = 32.dp)
            } else {
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = 32.dp)
            }

            Box(
                modifier = contentModifier
                    .alpha(contentAlpha)
                    .offset { IntOffset(0, contentOffsetY.dp.roundToPx()) },
                contentAlignment = if (needsScroll) Alignment.TopCenter else Alignment.Center
            ) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (needsScroll) Modifier.verticalScroll(scrollState) else Modifier),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = if (needsScroll) Arrangement.Top else Arrangement.Center
                ) {
                    // Fecha
                    val today = LocalDate.now()
                    val dateStr = today.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale.forLanguageTag("es")))
                    Text(
                        text = dateStr,
                        color = vigiliaColors.parchmentInk.copy(alpha = 0.65f),
                        fontSize = 14.sp,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                    
                    // Tema
                    Text(
                        text = devotional.topic,
                        color = vigiliaColors.candle,
                        fontSize = 45.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = CormorantFamily,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Escritura con Capitular
                    DropCapText(
                        text = devotional.verseText,
                        tier = tier,
                        accentColor = vigiliaColors.candle,
                        textColor = vigiliaColors.parchmentInk
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Referencia
                    Text(
                        text = devotional.reference,
                        color = vigiliaColors.candle,
                        fontSize = 17.sp,
                        fontStyle = FontStyle.Italic,
                        fontFamily = CormorantFamily,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = if (tier == DevotionalTier.XL) TextAlign.Center else TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Divisor
                    Box(
                        modifier = Modifier
                            .padding(vertical = 24.dp)
                            .width(56.dp)
                            .height(1.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    0.0f to Color.Transparent,
                                    0.5f to vigiliaColors.candle.copy(alpha = 0.55f),
                                    1.0f to Color.Transparent
                                )
                            )
                    )

                    // Label REFLEXIÓN
                    Text(
                        text = "REFLEXIÓN",
                        color = vigiliaColors.candle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = InterFamily,
                        letterSpacing = 2.5.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Texto de Reflexión
                    val reflectionSize = DevotionalUtils.calculateReflectionSize(devotional.reflection)
                    val reflectionLineHeight = DevotionalUtils.getReflectionLineHeight(devotional.reflection)
                    Text(
                        text = devotional.reflection,
                        color = Color(0xFFC6BFB2),
                        fontSize = reflectionSize,
                        fontFamily = CormorantFamily,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        ),
                        lineHeight = reflectionLineHeight
                    )
                    
                    Spacer(modifier = Modifier.height(if (isCapture) 20.dp else 60.dp))
                }

                // Degradados de scroll (Masking)
                if (needsScroll) {
                    val isScrolled by remember { derivedStateOf { scrollState.value > 0 } }
                    val canScrollForward by remember { derivedStateOf { scrollState.canScrollForward } }

                    if (isScrolled) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .align(Alignment.TopCenter)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(vigiliaColors.void.copy(alpha = 0.9f), Color.Transparent)
                                    )
                                )
                        )
                    }
                    if (canScrollForward) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, vigiliaColors.void.copy(alpha = 0.9f))
                                    )
                                )
                        )
                    }
                }
            }

            // Espaciado Inferior
            Spacer(modifier = Modifier.height(if (isCapture) 40.dp else 100.dp))

            // Branding "Verbo Libre"
            Text(
                text = "Verbo Libre",
                fontFamily = CormorantFamily,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                color = vigiliaColors.parchmentInk.copy(alpha = 0.55f),
                modifier = Modifier.padding(bottom = if (isCapture) 40.dp else 28.dp)
            )
        }
    }
}

@Composable
fun GrainOverlay() {
    Canvas(modifier = Modifier.fillMaxSize().alpha(0.05f)) {
        val random = Random(42)
        drawIntoCanvas {
            for (i in 0 until 1500) {
                drawCircle(
                    color = Color.White,
                    radius = 0.6.dp.toPx(),
                    center = Offset(
                        random.nextFloat() * size.width,
                        random.nextFloat() * size.height
                    ),
                    blendMode = BlendMode.Overlay
                )
            }
        }
    }
}

@Composable
fun CandleGlow(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "candle")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.09f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4500
                1f at 0
                1.05f at 810
                0.98f at 1485
                1.09f at 2340
                1.01f at 3060
                1.06f at 3825
                1f at 4500
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )
    
    val opacity by infiniteTransition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4500
                0.72f at 0
                0.95f at 810
                0.8f at 1485
                1f at 2340
                0.78f at 3060
                0.92f at 3825
                0.72f at 4500
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "opacity"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithContent {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color.copy(alpha = 0.16f * opacity), Color.Transparent),
                        center = Offset(size.width / 2f, 150.dp.toPx()),
                        radius = 170.dp.toPx() * scale
                    )
                )
            }
    )
}

@Composable
fun MoteParticles(color: Color) {
    val particles = remember { List(16) { MoteState() } }
    
    Box(modifier = Modifier.fillMaxSize()) {
        particles.forEach { mote ->
            val infiniteTransition = rememberInfiniteTransition(label = "mote")
            
            val progress by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(mote.duration, easing = LinearEasing, delayMillis = mote.delay),
                    repeatMode = RepeatMode.Restart
                ),
                label = "progress"
            )

            val currentOpacity = when {
                progress < 0.08f -> (progress / 0.08f) * mote.peakAlpha
                progress < 0.85f -> mote.peakAlpha
                else -> (1f - (progress - 0.85f) / 0.15f) * mote.peakAlpha
            }
            
            val currentScale = when {
                progress < 0.5f -> 0.6f + (progress / 0.5f) * 0.4f
                else -> 1f - ((progress - 0.5f) / 0.5f) * 0.6f
            }

            val yPos = 1000f - (progress * 1620f)
            val xPos = mote.startX + (progress * mote.drift)

            Box(
                modifier = Modifier
                    .offset { IntOffset(xPos.dp.roundToPx(), yPos.dp.roundToPx()) }
                    .size((mote.size * currentScale).dp)
                    .alpha(currentOpacity)
                    .background(
                        brush = Brush.radialGradient(
                            listOf(color.copy(alpha = 0.95f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )
        }
    }
}

class MoteState {
    val size = Random.nextFloat() * (4f - 1.6f) + 1.6f
    val startX = Random.nextFloat() * 360f
    val drift = (Random.nextFloat() * 36f) - 18f
    val duration = Random.nextInt(7000, 13000)
    val delay = Random.nextInt(0, 5000)
    val peakAlpha = Random.nextFloat() * (0.85f - 0.5f) + 0.5f
}

package com.mi.bibliarv1960.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mi.bibliarv1960.ui.components.DevotionalCard
import com.mi.bibliarv1960.ui.components.LinoButton
import com.mi.bibliarv1960.ui.components.LinoButtonVariant
import com.mi.bibliarv1960.ui.components.ShareableVerseCard
import com.mi.bibliarv1960.ui.viewmodel.BibleViewModel
import com.mi.bibliarv1960.utils.ShareUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DevotionalScreen(
    viewModel: BibleViewModel,
    onBack: () -> Unit,
) {
    val devotional by viewModel.displayDevotional.collectAsState()
    val dailyBgIndex by viewModel.todayDevotionalBgIndex.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val lastReadDate by viewModel.lastReadDate.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()

    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val isReadToday = lastReadDate == todayStr

    @Suppress("DiscouragedApi")
    val bgResName = "devotional_bg_$dailyBgIndex"
    @Suppress("DiscouragedApi")
    val bgResourceId = context.resources.getIdentifier(bgResName, "drawable", context.packageName)

    Box(modifier = Modifier.fillMaxSize()) {
        // --- 1. CAPA INVISIBLE PARA CAPTURA (OFF-SCREEN) ---
        Box(
            modifier = Modifier
                .size(360.dp, 640.dp) 
                .alpha(0f) 
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }
                }
        ) {
            devotional?.let { dev ->
                ShareableVerseCard(
                    bgResourceId = bgResourceId,
                    verseText = dev.verseText,
                    reference = dev.reference,
                    title = dev.topic,
                    reflection = dev.reflection
                )
            }
        }

        // --- 2. UI VISIBLE ---
        if (bgResourceId != 0) {
            Image(
                painter = painterResource(id = bgResourceId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().blur(2.dp),
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
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.7f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onBack,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                }

                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = "🔥 $currentStreak días",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                devotional?.let { dev ->
                    DevotionalCard(
                        topic = dev.topic,
                        verseText = dev.verseText,
                        reference = dev.reference,
                        reflection = dev.reflection,
                        modifier = Modifier.fillMaxWidth(0.95f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                devotional?.let { dev ->
                    LinoButton(
                        text = "✓ Marcar como leído",
                        variant = LinoButtonVariant.PRIMARY,
                        onClick = { if (!isReadToday) viewModel.markAsRead() }
                    )
                    LinoButton(
                        text = "↗ Compartir",
                        variant = LinoButtonVariant.GHOST,
                        onClick = {
                            coroutineScope.launch {
                                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                val shareText = "*${dev.topic}*\n\"${dev.verseText}\"\n— ${dev.reference}\n\n${dev.reflection}\n\nCompartido desde Verbo Libre"
                                ShareUtils.shareBitmap(context, bitmap, shareText)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))
                Text(
                    text = "Verbo Libre",
                    fontFamily = FontFamily.Serif,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .clickable { viewModel.nextDevotionalPreview() }
                )
            }
        }
    }
}

package com.mi.bibliarv1960.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mi.bibliarv1960.ui.theme.BibliaRV1960Theme

@Composable
fun ShareableVerseCard(
    bgResourceId: Int,
    verseText: String,
    reference: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    reflection: String? = null
) {
    BibliaRV1960Theme {
        Box(
            modifier = modifier
                .size(width = 360.dp, height = 640.dp) // Proporción 9:16 (1080x1920 / 3)
                .background(Color.Black)
        ) {
            // 1. Fondo de Paisaje
            if (bgResourceId != 0) {
                Image(
                    painter = painterResource(id = bgResourceId),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            // 2. Overlay Oscuro
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header (Branding)
                Text(
                    text = "Verbo Libre",
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 10.dp)
                )

                // 3. CONTENIDO CENTRAL
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (reflection != null) {
                        // MODO DEVOCIONAL: Tarjeta Cream
                        DevotionalCard(
                            topic = title ?: "Devocional",
                            verseText = verseText,
                            reference = reference,
                            reflection = reflection,
                            modifier = Modifier.fillMaxWidth(0.95f)
                        )
                    } else {
                        // MODO VERSÍCULO: Texto Centrado (Story style)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "\"$verseText\"",
                                fontSize = 24.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                lineHeight = 34.sp,
                                fontFamily = FontFamily.Serif,
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = reference.uppercase(),
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                letterSpacing = 3.sp,
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Footer
                Text(
                    text = "Alimenta tu alma cada día",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
        }
    }
}

package com.mi.bibliarv1960.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mi.bibliarv1960.ui.theme.BibliaRV1960Theme
import com.mi.bibliarv1960.ui.theme.JakartaFamily

@Composable
fun DevotionalCard(
    topic: String,
    verseText: String,
    reference: String,
    reflection: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFFFAFAF7).copy(alpha = 0.96f),
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 10.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(540.dp) 
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. TEMA (Respiro superior)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = topic,
                fontSize = 24.sp,
                fontFamily = JakartaFamily,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF33506E),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            SectionDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                lineColor = Color(0xFF33506E).copy(alpha = 0.12f)
            )

            // 2. SLOT VERSÍCULO + REFERENCIA (Unificados para equilibrio visual)
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AutoResizingText(
                        text = "\"$verseText\"",
                        targetTextSize = 24.sp,
                        minTextSize = 16.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        fontStyle = FontStyle.Italic,
                        lineHeight = 1.3,
                        color = Color(0xFF22252A)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = reference,
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFF6B87A3),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // 3. SEPARADOR DINÁMICO (Súper compacto)
            SectionDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                lineColor = Color(0xFF33506E).copy(alpha = 0.08f),
                dotColor = Color(0xFFC09B6C),
                useHeart = true
            )

            // 4. SLOT REFLEXIÓN
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "REFLEXIÓN",
                        fontSize = 10.sp,
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF33506E).copy(alpha = 0.5f),
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 4.dp, top = 2.dp)
                    )

                    AutoResizingText(
                        text = reflection,
                        targetTextSize = 17.sp,
                        minTextSize = 14.sp,
                        fontFamily = FontFamily.Serif,
                        lineHeight = 1.4,
                        color = Color(0xFF22252A)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun AutoResizingText(
    text: String,
    targetTextSize: TextUnit,
    modifier: Modifier = Modifier,
    minTextSize: TextUnit = 12.sp,
    fontFamily: FontFamily = FontFamily.Default,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: FontStyle = FontStyle.Normal,
    lineHeight: Double = 1.0,
    color: Color = Color.Unspecified
) {
    var textSize by remember(text) { mutableStateOf(targetTextSize) }
    var readyToDraw by remember(text) { mutableStateOf(false) }

    Text(
        text = text,
        color = color,
        modifier = modifier
            .fillMaxWidth()
            .then(if (readyToDraw) Modifier.verticalScroll(rememberScrollState()) else Modifier)
            .drawWithContent {
                if (readyToDraw) drawContent()
            },
        fontSize = textSize,
        fontFamily = fontFamily,
        fontWeight = fontWeight,
        fontStyle = fontStyle,
        lineHeight = (textSize.value * lineHeight).sp,
        textAlign = TextAlign.Center,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowHeight && textSize > minTextSize) {
                textSize = (textSize.value * 0.95f).sp
            } else {
                readyToDraw = true
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DevotionalCardPreview() {
    BibliaRV1960Theme {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            DevotionalCard(
                topic = "Transformación",
                verseText = "El fruto del Espíritu es amor, gozo y paz.",
                reference = "Gálatas 5:22",
                reflection = "Cuando Dios transforma tu interior, también cambia tu manera de vivir."
            )
        }
    }
}

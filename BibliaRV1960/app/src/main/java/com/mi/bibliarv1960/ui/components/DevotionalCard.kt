package com.mi.bibliarv1960.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        color = Color(0xFFFAFAF7), // 100% opaco para evitar costuras con el degradado
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 10.dp,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 22.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. TEMA
            Text(
                text = topic,
                fontSize = 24.sp,
                fontFamily = JakartaFamily,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF33506E),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            SectionDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                lineColor = Color(0xFF33506E).copy(alpha = 0.12f)
            )

            // 2. VERSÍCULO
            Box(
                modifier = Modifier
                    .weight(1.2f, fill = false)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                AutoResizingBlock(
                    mainText = "\"$verseText\"",
                    footerText = reference,
                    targetTextSize = 24.sp,
                    minTextSize = 16.sp,
                    mainStyle = TextStyleParams(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Medium,
                        fontStyle = FontStyle.Italic,
                        lineHeight = 1.3,
                        color = Color(0xFF22252A)
                    ),
                    footerStyle = TextStyleParams(
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFF6B87A3),
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            // 3. SEPARADOR
            SectionDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                lineColor = Color(0xFF33506E).copy(alpha = 0.08f),
                dotColor = Color(0xFFC09B6C),
                useHeart = true
            )

            // 4. REFLEXIÓN
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                AutoResizingBlock(
                    headerLabel = "REFLEXIÓN",
                    mainText = reflection,
                    targetTextSize = 17.sp,
                    minTextSize = 14.sp,
                    headerStyle = TextStyleParams(
                        fontSize = 10.sp,
                        fontFamily = JakartaFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF33506E).copy(alpha = 0.5f),
                        letterSpacing = 1.5.sp
                    ),
                    mainStyle = TextStyleParams(
                        fontFamily = FontFamily.Serif,
                        lineHeight = 1.4,
                        color = Color(0xFF22252A)
                    )
                )
            }
        }
    }
}

data class TextStyleParams(
    val fontSize: TextUnit = TextUnit.Unspecified,
    val fontFamily: FontFamily = FontFamily.Default,
    val fontWeight: FontWeight = FontWeight.Normal,
    val fontStyle: FontStyle = FontStyle.Normal,
    val lineHeight: Double = 1.0,
    val color: Color = Color.Unspecified,
    val letterSpacing: TextUnit = TextUnit.Unspecified
)

@Composable
fun AutoResizingBlock(
    mainText: String,
    targetTextSize: TextUnit,
    minTextSize: TextUnit,
    modifier: Modifier = Modifier,
    headerLabel: String? = null,
    footerText: String? = null,
    headerStyle: TextStyleParams = TextStyleParams(),
    mainStyle: TextStyleParams = TextStyleParams(),
    footerStyle: TextStyleParams = TextStyleParams()
) {
    var textSize by remember(mainText) { mutableStateOf(targetTextSize) }
    val scrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val maxHeightPx = this.constraints.maxHeight
        
        Column(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // 1. CABECERA (Estática)
            if (headerLabel != null) {
                Text(
                    text = headerLabel,
                    fontSize = headerStyle.fontSize,
                    fontFamily = headerStyle.fontFamily,
                    fontWeight = headerStyle.fontWeight,
                    color = headerStyle.color,
                    letterSpacing = headerStyle.letterSpacing,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // 2. CUERPO (Área exclusiva de scroll y degradado)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                Text(
                    text = mainText,
                    color = mainStyle.color,
                    fontSize = textSize,
                    fontFamily = mainStyle.fontFamily,
                    fontWeight = mainStyle.fontWeight,
                    fontStyle = mainStyle.fontStyle,
                    lineHeight = (textSize.value * mainStyle.lineHeight).sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    onTextLayout = { textLayoutResult ->
                        if (textLayoutResult.didOverflowHeight || textLayoutResult.size.height > maxHeightPx * 0.85f) {
                            if (textSize > minTextSize) {
                                textSize = (textSize.value * 0.95f).sp
                            }
                        }
                    }
                )

                // Degradado suave: 48dp de altura y stops nítidos hacia el color sólido de la card
                if (scrollState.canScrollForward) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                brush = Brush.verticalGradient(
                                    0.0f to Color.Transparent,
                                    0.5f to Color(0xFFFAFAF7).copy(alpha = 0.5f),
                                    1.0f to Color(0xFFFAFAF7) // 100% opaco, coincide con la Surface
                                )
                            )
                    )
                }
            }

            // 3. PIE / REFERENCIA (Estático y nítido)
            if (footerText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = footerText,
                    fontSize = footerStyle.fontSize,
                    fontStyle = footerStyle.fontStyle,
                    color = footerStyle.color,
                    fontWeight = footerStyle.fontWeight,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun DevotionalCardPreview() {
    DevotionalCard(
        topic = "CONFIANZA EN DIOS",
        verseText = "No temas, porque yo estoy contigo; no desmayes, porque yo soy tu Dios que te esfuerzo; siempre te ayudaré, siempre te sustentaré con la diestra de mi justicia.",
        reference = "Isaías 41:10",
        reflection = "Cuando enfrentamos vientos fuertes, la Biblia nos promete que Dios está con nosotros. Su presencia no depende de que todo salga bien, sino de que Él sostiene, con su propia justicia, lo que nosotros no podemos cargar. Confiar no es ignorar el problema, es saber quién tiene el control."
    )
}

package com.mi.bibliarv1960.ui.components

import android.graphics.Paint as AndroidPaint
import android.graphics.Rect
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mi.bibliarv1960.ui.theme.CormorantFamily
import com.mi.bibliarv1960.utils.DevotionalUtils

enum class DevotionalTier {
    XL, // <= 40
    LG, // <= 100
    MD, // <= 170
    SM  // > 170
}

/**
 * Calcula, para un carácter/tamaño/estilo de fuente dados, el hueco real (en px)
 * entre la línea de "ascent" teórica (donde Compose posiciona el techo de la caja
 * de texto cuando se usa Trim.Top/Both) y el techo REAL de tinta del glifo.
 *
 * Esto es lo que un simple ajuste de lineHeight no puede corregir: ese colchón
 * viene grabado en las métricas de la propia fuente (reservado para acentos,
 * diacríticos, etc.), no del interlineado que le pidamos a Compose.
 */
@Composable
private fun rememberInkTopGapPx(
    sampleChar: String,
    fontWeight: FontWeight,
    fontStyle: FontStyle,
    fontSizeSp: Float,
): Float {
    val density = LocalDensity.current
    val fontFamilyResolver = LocalFontFamilyResolver.current
    return remember(sampleChar, fontWeight, fontStyle, fontSizeSp) {
        val typefaceResult = fontFamilyResolver.resolve(
            fontFamily = CormorantFamily,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
        )
        val androidTypeface = typefaceResult.value as android.graphics.Typeface

        val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            typeface = androidTypeface
            textSize = with(density) { fontSizeSp.sp.toPx() }
        }

        val rect = Rect()
        paint.getTextBounds(sampleChar, 0, sampleChar.length, rect)

        // rect.top es negativo: distancia (hacia arriba) del baseline al techo REAL de tinta.
        val inkTopPx = -rect.top.toFloat()
        // Métrica teórica de ascent que usa Compose para el techo de la caja.
        val fontAscentPx = -paint.fontMetrics.ascent

        // Lo que sobra por encima de la tinta real. Nunca negativo: si por lo que sea
        // la tinta sobrepasa el ascent teórico, no desplazamos hacia abajo.
        (fontAscentPx - inkTopPx).coerceAtLeast(0f)
    }
}

/**
 * A diferencia de Modifier.offset(), esto NO solo desplaza el dibujo hacia arriba:
 * también reduce el alto que el composable reserva en su padre, exactamente en
 * la misma medida. Sin esto, el Row/Column de más arriba sigue calculando el
 * siguiente elemento a partir de la altura ORIGINAL (sin recortar), dejando un
 * hueco visible del tamaño del offset que aplicamos — que es justo el bug que
 * causaba el espacio extra entre la 2ª y 3ª línea.
 */
private fun Modifier.trimTopSpace(gap: Dp): Modifier = this.layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val gapPx = gap.roundToPx().coerceIn(0, placeable.height)
    layout(placeable.width, placeable.height - gapPx) {
        placeable.placeRelative(x = 0, y = -gapPx)
    }
}

@Composable
private fun rememberInkTopGapDp(
    sampleChar: String,
    fontWeight: FontWeight,
    fontStyle: FontStyle,
    fontSizeSp: Float,
): Dp {
    val density = LocalDensity.current
    val gapPx = rememberInkTopGapPx(sampleChar, fontWeight, fontStyle, fontSizeSp)
    return with(density) { gapPx.toDp() }
}

@Composable
fun DropCapText(
    text: String,
    tier: DevotionalTier,
    accentColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    val fontSize = DevotionalUtils.getVerseFontSize(tier)
    val lineHeight = DevotionalUtils.getVerseLineHeight(tier)
    val textMeasurer = rememberTextMeasurer()

    if (tier == DevotionalTier.XL) {
        // Estilo: Iluminar solo la primera palabra, centrado (Mockup XL)
        val words = text.split(" ")
        val annotatedString = buildAnnotatedString {
            if (words.isNotEmpty()) {
                withStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.SemiBold)) {
                    append(words[0])
                }
                if (words.size > 1) {
                    append(" ")
                    append(words.asSequence().drop(1).joinToString(" "))
                }
            }
        }
        Text(
            text = annotatedString,
            fontSize = fontSize,
            fontFamily = CormorantFamily,
            fontStyle = FontStyle.Italic,
            color = textColor,
            textAlign = TextAlign.Center,
            lineHeight = lineHeight,
            modifier = modifier.fillMaxWidth()
        )
    } else {
        // Estilo: Capitular manuscrita (Mockup LG, MD, SM) con Wrap-around de 2 líneas
        val firstChar = text.take(1)
        val restOfText = text.drop(1)

        val dropCapSize = when (tier) {
            DevotionalTier.LG -> 88.sp
            DevotionalTier.MD -> 70.sp
            DevotionalTier.SM -> 54.sp
            else -> 88.sp
        }

        val density = LocalDensity.current

        // --- NUEVO: gap real de tinta para capitular y cuerpo, medido con Paint.getTextBounds ---
        val dropCapInkGap = rememberInkTopGapDp(
            sampleChar = firstChar,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Normal,
            fontSizeSp = dropCapSize.value,
        )
        val bodyFirstChar = restOfText.trim().take(1).ifEmpty { "A" }
        val bodyInkGap = rememberInkTopGapDp(
            sampleChar = bodyFirstChar,
            fontWeight = FontWeight.Medium,
            fontStyle = FontStyle.Italic,
            fontSizeSp = fontSize.value,
        )

        BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
            val maxWidthPx = constraints.maxWidth

            // 1. Medir el ancho de la capitular
            val dropCapStyle = TextStyle(
                fontSize = dropCapSize,
                fontFamily = CormorantFamily,
                fontWeight = FontWeight.SemiBold,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeight = dropCapSize,
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Top,
                    trim = LineHeightStyle.Trim.Both
                )
            )
            val dropCapLayout = textMeasurer.measure(firstChar, dropCapStyle)
            val dropCapWidthPx = dropCapLayout.size.width

            // 2. Calcular espacio para el texto indentado (primeras 2 líneas)
            val offsetCompensationPx = with(density) { 2.dp.toPx() }
            val availableWidthPx = maxWidthPx - dropCapWidthPx + offsetCompensationPx

            // 3. Medir cuánto texto cabe en las primeras 2 líneas
            val bodyStyle = TextStyle(
                fontSize = fontSize,
                fontFamily = CormorantFamily,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium,
                lineHeight = lineHeight,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Top,
                    trim = LineHeightStyle.Trim.Both
                )
            )

            val indentedLayout = textMeasurer.measure(
                text = restOfText,
                style = bodyStyle,
                constraints = Constraints(maxWidth = availableWidthPx.toInt()),
                maxLines = 2
            )

            // 4. Split del texto: si hay más de 2 líneas, cortamos en el fin de la línea 2
            val splitIndex = if (indentedLayout.lineCount >= 2 && indentedLayout.hasVisualOverflow) {
                indentedLayout.getLineEnd(1, visibleEnd = true)
            } else {
                restOfText.length
            }

            val part1 = restOfText.substring(0, splitIndex)
            val part2 = restOfText.substring(splitIndex).trimStart()

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = firstChar,
                        style = dropCapStyle,
                        color = accentColor,
                        // Subimos la capitular Y recortamos el espacio reservado por la
                        // misma cantidad, para que no quede un hueco fantasma debajo.
                        modifier = Modifier.trimTopSpace(dropCapInkGap)
                    )
                    Text(
                        text = part1,
                        style = bodyStyle,
                        color = textColor,
                        modifier = Modifier
                            .weight(1f)
                            .trimTopSpace(bodyInkGap)
                            .offset(x = (-2).dp)
                    )
                }
                if (part2.isNotEmpty()) {
                    Text(
                        text = part2,
                        style = bodyStyle,
                        color = textColor,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
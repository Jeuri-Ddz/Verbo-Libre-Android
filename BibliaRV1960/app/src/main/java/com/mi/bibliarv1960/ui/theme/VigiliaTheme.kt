package com.mi.bibliarv1960.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class VigiliaColors(
    val void: Color,
    val candle: Color,
    val candleDim: Color,
    val parchmentInk: Color,
    val rubric: Color,
    val reflectionText: Color,
    val scrim0: Color,
    val scrim24: Color,
    val scrim62: Color,
    val scrim100: Color
)

val VigiliaDarkPalette = VigiliaColors(
    void = Color(0xFF14181D),
    candle = Color(0xFFE7B768),
    candleDim = Color(0xFFB8863E),
    parchmentInk = Color(0xFFF2ECDE),
    rubric = Color(0xFFC06A4C),
    reflectionText = Color(0xFFF2ECDE).copy(alpha = 0.82f),
    scrim0 = Color(0xFF0F0C0A).copy(alpha = 0.55f),
    scrim24 = Color(0xFF0F0C0A).copy(alpha = 0.22f),
    scrim62 = Color(0xFF0F0C0A).copy(alpha = 0.30f),
    scrim100 = Color(0xFF0A0807).copy(alpha = 0.68f)
)

val VigiliaLightPalette = VigiliaDarkPalette 

val LocalVigiliaColors = staticCompositionLocalOf { VigiliaDarkPalette }

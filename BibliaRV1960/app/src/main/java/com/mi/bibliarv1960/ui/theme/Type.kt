package com.mi.bibliarv1960.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.mi.bibliarv1960.R

val CormorantFamily = FontFamily(
    Font(R.font.cormorant_garamond_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.cormorant_garamond_medium, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.cormorant_garamond_semibold, FontWeight.SemiBold, FontStyle.Normal),
    Font(R.font.cormorant_garamond_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.cormorant_garamond_medium_italic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.cormorant_garamond_semibold_italic, FontWeight.SemiBold, FontStyle.Italic)
)

@OptIn(ExperimentalTextApi::class)
private fun interWeight(weight: Int, isItalic: Boolean = false) = Font(
    resId = if (isItalic) R.font.inter_italic_variable else R.font.inter_variable,
    weight = FontWeight(weight),
    style = if (isItalic) FontStyle.Italic else FontStyle.Normal,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight),
        FontVariation.italic(if (isItalic) 1f else 0f)
    )
)

@OptIn(ExperimentalTextApi::class)
private fun jakartaWeight(weight: Int) = Font(
    resId = R.font.plus_jakarta_sans_variable,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight))
)

// Inter -> tipografía de lectura y UI base
val InterFamily = FontFamily(
    interWeight(400),
    interWeight(500),
    interWeight(600),
    interWeight(400, true),
    interWeight(500, true),
    interWeight(600, true)
)

// Plus Jakarta Sans -> tipografía de interfaz legacy / títulos
val JakartaFamily = FontFamily(
    jakartaWeight(500),
    jakartaWeight(600),
    jakartaWeight(700)
)

val BibliaTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = JakartaFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = JakartaFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = JakartaFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.5.sp,
        lineHeight = 28.sp
    ),
    bodySmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
    labelLarge = TextStyle(
        fontFamily = JakartaFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp
    )
)

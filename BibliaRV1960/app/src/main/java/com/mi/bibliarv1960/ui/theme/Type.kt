package com.mi.bibliarv1960.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.mi.bibliarv1960.R

@OptIn(ExperimentalTextApi::class)
private fun interWeight(weight: Int) = Font(
    resId = R.font.inter_variable,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight))
)

@OptIn(ExperimentalTextApi::class)
private fun jakartaWeight(weight: Int) = Font(
    resId = R.font.plus_jakarta_sans_variable,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight))
)

// Inter -> tipografía de lectura (cuerpo de los versículos)
val InterFamily = FontFamily(
    interWeight(400),
    interWeight(500)
)

// Plus Jakarta Sans -> tipografía de interfaz (títulos, botones, tabs)
val JakartaFamily = FontFamily(
    jakartaWeight(500),
    jakartaWeight(600),
    jakartaWeight(700)
)

val BibliaTypography = Typography(
    // Título de app / encabezados grandes
    headlineSmall = TextStyle(
        fontFamily = JakartaFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    // Título de libro en el lector ("Capítulo 1")
    titleLarge = TextStyle(
        fontFamily = JakartaFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),
    // Nombre de libro en las tarjetas / listas
    titleMedium = TextStyle(
        fontFamily = JakartaFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    ),
    // Texto de los versículos (cuerpo largo de lectura)
    bodyLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.5.sp,
        lineHeight = 28.sp
    ),
    // Metadatos: "50 capítulos", breadcrumbs, hints
    bodySmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
    // Botones y tabs
    labelLarge = TextStyle(
        fontFamily = JakartaFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp
    )
)

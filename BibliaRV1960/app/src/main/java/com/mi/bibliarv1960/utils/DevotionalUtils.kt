package com.mi.bibliarv1960.utils

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.mi.bibliarv1960.ui.components.DevotionalTier

object DevotionalUtils {

    fun calculateVerseTier(text: String): DevotionalTier {
        val length = text.length
        return when {
            length <= 40 -> DevotionalTier.XL
            length <= 100 -> DevotionalTier.LG
            length <= 170 -> DevotionalTier.MD
            else -> DevotionalTier.SM
        }
    }

    fun getVerseFontSize(tier: DevotionalTier): TextUnit {
        return when (tier) {
            DevotionalTier.XL -> 40.sp
            DevotionalTier.LG -> 34.sp
            DevotionalTier.MD -> 27.sp
            DevotionalTier.SM -> 21.5.sp
        }
    }

    fun getVerseLineHeight(tier: DevotionalTier): TextUnit {
        val fontSize = getVerseFontSize(tier).value
        return when (tier) {
            DevotionalTier.XL -> (fontSize * 1.34).sp
            DevotionalTier.LG -> (fontSize * 1.34).sp // Bajado de 1.4 a 1.34 para compenetrar mejor
            DevotionalTier.MD -> (fontSize * 1.36).sp // Bajado de 1.42 a 1.36
            DevotionalTier.SM -> (fontSize * 1.4).sp // Bajado de 1.46 a 1.4
        }
    }

    fun calculateReflectionSize(text: String): TextUnit {
        val length = text.length
        return when {
            length <= 70 -> 24.sp
            length <= 130 -> 21.sp
            else -> 20.sp
        }
    }

    fun getReflectionLineHeight(text: String): TextUnit {
        val length = text.length
        val fontSize = calculateReflectionSize(text).value
        val multiplier = when {
            length <= 70 -> 1.65f
            length <= 130 -> 1.6f
            else -> 1.56f
        }
        return (fontSize * multiplier).sp
    }
}
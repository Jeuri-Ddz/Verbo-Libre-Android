package com.mi.bibliarv1960.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mi.bibliarv1960.ui.theme.LinoAccent
import com.mi.bibliarv1960.ui.theme.LinoBg

enum class LinoButtonVariant {
    PRIMARY,
    OUTLINE,
    GHOST
}

@Composable
fun LinoButton(
    text: String,
    onClick: () -> Unit,
    variant: LinoButtonVariant = LinoButtonVariant.PRIMARY,
    modifier: Modifier = Modifier
) {
    val height = if (variant == LinoButtonVariant.GHOST) 44.dp else 52.dp
    val containerColor = when (variant) {
        LinoButtonVariant.PRIMARY -> LinoBg
        else -> Color.Transparent
    }
    val contentColor = when (variant) {
        LinoButtonVariant.PRIMARY -> LinoAccent
        LinoButtonVariant.OUTLINE -> LinoBg
        LinoButtonVariant.GHOST -> LinoBg.copy(alpha = 0.85f)
    }
    val border = when (variant) {
        LinoButtonVariant.PRIMARY -> null
        LinoButtonVariant.OUTLINE -> BorderStroke(1.5.dp, LinoBg.copy(alpha = 0.55f))
        LinoButtonVariant.GHOST -> BorderStroke(1.5.dp, LinoBg.copy(alpha = 0.30f))
    }
    val fontSize = if (variant == LinoButtonVariant.GHOST) 13.5.sp else 14.5.sp
    val fontWeight = if (variant == LinoButtonVariant.GHOST) FontWeight.Medium else FontWeight.SemiBold

    Surface(
        onClick = onClick,
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(14.dp),
        border = border,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = fontSize,
                fontWeight = fontWeight
            )
        }
    }
}

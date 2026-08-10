package com.mi.bibliarv1960.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SectionDivider(
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFFD9D2C2),
    dotColor: Color = Color(0xFF6B87A3),
    useHeart: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Línea izquierda
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, lineColor)
                    )
                )
        )

        Spacer(modifier = Modifier.width(12.dp))

        if (useHeart) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = dotColor,
                modifier = Modifier.size(14.dp)
            )
        } else {
            // Punto central con halo suave
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .clip(CircleShape)
                        .background(dotColor.copy(alpha = 0.14f))
                )
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Línea derecha
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(lineColor, Color.Transparent)
                    )
                )
        )
    }
}

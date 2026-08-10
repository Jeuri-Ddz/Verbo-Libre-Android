package com.mi.bibliarv1960.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ThemeToggleButton(
    isDark: Boolean,
    onClick: () -> Unit,
    size: Dp = 38.dp
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF2A3B52) else Color(0xFFD8E1E8),
        animationSpec = tween(350),
        label = "bgColor"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isDark) 90f else 0f,
        animationSpec = tween(450, easing = FastOutSlowInEasing),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Sun Icon
        Icon(
            imageVector = Icons.Outlined.WbSunny,
            contentDescription = "Light Mode",
            tint = Color(0xFF33506E),
            modifier = Modifier
                .size(size * 0.6f)
                .rotate(rotation)
                .alpha(1f - (rotation / 90f))
        )
        
        // Moon Icon
        Icon(
            imageVector = Icons.Outlined.DarkMode,
            contentDescription = "Dark Mode",
            tint = Color(0xFFEDEDEB),
            modifier = Modifier
                .size(size * 0.6f)
                .rotate(rotation - 90f)
                .alpha(rotation / 90f)
        )
    }
}

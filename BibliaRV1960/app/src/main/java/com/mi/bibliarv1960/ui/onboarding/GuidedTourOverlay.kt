package com.mi.bibliarv1960.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize

@Composable
fun GuidedTourOverlay(
    viewModel: GuidedTourViewModel,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onDrawerAction: (Boolean) -> Unit
) {
    val isTourActive by viewModel.isTourActive
    val currentStepIndex by viewModel.currentStepIndex
    val targetRect by viewModel.targetRect.collectAsState()
    val step = viewModel.steps.getOrNull(currentStepIndex)
    
    // Condición de estado real para mostrar el spotlight
    val shouldShowSpotlight = remember(step, targetRect, currentRoute) {
        if (step == null) return@remember false
        val routeMatches = currentRoute?.startsWith(step.route.substringBefore("/")) == true
        val targetReady = step.targetKey == null || targetRect != null
        routeMatches && targetReady
    }

    if (isTourActive) {
        // Bloquear navegación hacia atrás
        BackHandler {
            viewModel.skipTour()
        }

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Scrim con Spotlight
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                ) {
                    // Fondo oscuro
                    drawRect(color = Color.Black.copy(alpha = 0.7f))

                    // Spotlight
                    targetRect?.let { rect ->
                        val padding = 8.dp.toPx()
                        drawRoundRect(
                            color = Color.Transparent,
                            topLeft = Offset(rect.left - padding, rect.top - padding),
                            size = Size(rect.width + padding * 2, rect.height + padding * 2),
                            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                            blendMode = BlendMode.Clear
                        )
                    }
                }

                // Tooltip y Botones
                if (shouldShowSpotlight && step != null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Botón Saltar (Siempre visible)
                        TextButton(
                            onClick = { viewModel.skipTour() },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 16.dp, end = 16.dp)
                        ) {
                            Text("Saltar", color = Color.White)
                        }

                        // Tooltip
                        TourTooltip(
                            text = step.text,
                            targetRect = targetRect,
                            onNext = { viewModel.nextStep(onNavigate, onDrawerAction) },
                            isLastStep = currentStepIndex == viewModel.steps.size - 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BoxScope.TourTooltip(
    text: String,
    targetRect: Rect?,
    onNext: () -> Unit,
    isLastStep: Boolean
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    // Calcular posición del tooltip
    val tooltipModifier = if (targetRect == null) {
        Modifier.align(Alignment.Center).padding(32.dp)
    } else {
        val targetCenterY = targetRect.center.y
        val spaceAbove = targetRect.top
        val spaceBelow = screenHeightPx - targetRect.bottom
        
        if (spaceBelow > spaceAbove) {
            Modifier
                .padding(top = with(density) { (targetRect.bottom + 16.dp.toPx()).toDp() })
                .align(Alignment.TopCenter)
        } else {
            Modifier
                .padding(bottom = with(density) { (screenHeightPx - targetRect.top + 16.dp.toPx()).toDp() })
                .align(Alignment.BottomCenter)
        }
    }

    Surface(
        modifier = tooltipModifier
            .widthIn(max = 300.dp)
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onNext,
                modifier = Modifier.align(Alignment.End),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isLastStep) "Comenzar" else "Siguiente")
            }
        }
    }
}

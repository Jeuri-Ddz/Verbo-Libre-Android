package com.mi.bibliarv1960.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

@Composable
fun ContextualTooltip(
    targetCoordinates: LayoutCoordinates?,
    text: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if ((targetCoordinates == null) || !targetCoordinates.isAttached) return

    val density = LocalDensity.current
    val targetBounds = targetCoordinates.boundsInWindow()
    val bubbleColor = Color(0xFF33506E)

    // Estado para saber si el tooltip quedó arriba o abajo del elemento
    var isPlacedAbove by remember { mutableStateOf(false) }
    // El desplazamiento horizontal de la flecha relativo al centro de la pantalla
    var arrowXOffsetFromCenter by remember { mutableFloatStateOf(0f) }

    Popup(
        popupPositionProvider = object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                val screenWidth = windowSize.width.toFloat()
                val screenHeight = windowSize.height.toFloat()
                
                // 1. La burbuja siempre centrada horizontalmente
                val finalX = (screenWidth - popupContentSize.width) / 2f
                
                // 2. Determinar posición vertical (Y)
                val spacingPx = with(density) { 8.dp.toPx() }
                var y = targetBounds.bottom + spacingPx
                var above = false
                if (y + popupContentSize.height > screenHeight) {
                    y = targetBounds.top - popupContentSize.height - spacingPx
                    above = true
                }
                isPlacedAbove = above

                // 3. Calcular la posición de la flecha relativa al centro de la burbuja
                // Posición horizontal absoluta del centro del target
                val targetCenterX = targetBounds.center.x
                // Posición horizontal absoluta del centro de la burbuja (que está centrada en pantalla)
                val bubbleCenterX = screenWidth / 2f
                
                // Desplazamiento de la flecha respecto al centro de la burbuja
                val rawOffset = targetCenterX - bubbleCenterX
                
                // Margen de seguridad para no chocar con las esquinas redondeadas (16dp de radio)
                val safetyMargin = with(density) { 24.dp.toPx() }
                val maxOffset = (popupContentSize.width / 2f) - safetyMargin
                arrowXOffsetFromCenter = rawOffset.coerceIn(-maxOffset, maxOffset)

                return IntOffset(finalX.toInt(), y.toInt())
            }
        },
        onDismissRequest = { /* Forzar click en botón */ },
        properties = PopupProperties(focusable = true, dismissOnClickOutside = false)
    ) {
        // Ancho fijo: 90% de la pantalla (aproximado usando Box con fillMaxWidth y padding)
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp), // Esto garantiza el ~90% de ancho
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.width(IntrinsicSize.Max),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isPlacedAbove) {
                    TooltipArrow(color = bubbleColor, xOffsetFromCenter = arrowXOffsetFromCenter, isUp = true)
                }

                Surface(
                    color = bubbleColor,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = text,
                            color = Color.White,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("Entendido", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (isPlacedAbove) {
                    TooltipArrow(color = bubbleColor, xOffsetFromCenter = arrowXOffsetFromCenter, isUp = false)
                }
            }
        }
    }
}

@Composable
private fun TooltipArrow(
    color: Color,
    xOffsetFromCenter: Float,
    isUp: Boolean
) {
    val density = LocalDensity.current
    val arrowWidth = 20.dp
    val arrowHeight = 10.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(arrowHeight),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset(x = with(density) { xOffsetFromCenter.toDp() })
                .size(arrowWidth, arrowHeight)
                .background(
                    color = color,
                    shape = GenericShape { size, _ ->
                        if (isUp) {
                            moveTo(size.width / 2f, 0f)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                        } else {
                            moveTo(0f, 0f)
                            lineTo(size.width, 0f)
                            lineTo(size.width / 2f, size.height)
                        }
                        close()
                    }
                )
        )
    }
}

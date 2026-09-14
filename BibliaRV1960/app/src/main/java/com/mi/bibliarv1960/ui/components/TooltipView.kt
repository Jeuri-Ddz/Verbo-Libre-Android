package com.mi.bibliarv1960.ui.components

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.mi.bibliarv1960.R

class TooltipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.tooltip_bg)
        style = Paint.Style.FILL
    }

    private val path = Path()
    private val bodyPath = Path()
    private val arrowPath = Path()

    private val cornerRadius = 20.dpToPx()
    private val arrowWidth = 24.dpToPx()
    private val arrowHeight = 14.dpToPx()
    private val bodyRect = RectF()

    var arrowOffsetX: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        if (width == 0f || height == 0f) return

        path.reset()
        bodyPath.reset()
        arrowPath.reset()

        // 1. Draw rounded rectangle body
        // We start drawing from Y = arrowHeight to leave space for the arrow at the top
        bodyRect.set(0f, arrowHeight, width, height)
        bodyPath.addRoundRect(bodyRect, cornerRadius, cornerRadius, Path.Direction.CW)

        // 2. Draw triangle arrow
        // Coerce arrowOffsetX to stay within rounded corners
        val minX = cornerRadius + arrowWidth / 2f
        val maxX = width - cornerRadius - arrowWidth / 2f
        val centerX = arrowOffsetX.coerceIn(minX, maxX)

        arrowPath.moveTo(centerX, 0f) // Tip
        arrowPath.lineTo(centerX - arrowWidth / 2f, arrowHeight + 1f) // Bottom left (+1 for overlap)
        arrowPath.lineTo(centerX + arrowWidth / 2f, arrowHeight + 1f) // Bottom right (+1 for overlap)
        arrowPath.close()

        // 3. Union
        path.op(bodyPath, arrowPath, Path.Op.UNION)

        canvas.drawPath(path, paint)
    }

    private fun Int.dpToPx(): Float = this.toFloat() * context.resources.displayMetrics.density
}

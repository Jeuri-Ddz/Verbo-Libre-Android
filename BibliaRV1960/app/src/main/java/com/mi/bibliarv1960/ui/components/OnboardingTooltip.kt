package com.mi.bibliarv1960.ui.components

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.mi.bibliarv1960.databinding.TooltipLayoutBinding

class OnboardingTooltip(private val context: Context) {

    private val sharedPrefs = context.getSharedPreferences("onboarding_tooltips", Context.MODE_PRIVATE)
    private var popupWindow: PopupWindow? = null

    fun show(anchorView: View, message: String, onDismiss: () -> Unit = {}) {
        if (sharedPrefs.getBoolean("tooltip_dark_mode_shown", false)) {
            return
        }

        val binding = TooltipLayoutBinding.inflate(LayoutInflater.from(context))
        binding.tooltipMessage.text = message

        popupWindow = PopupWindow(
            binding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            isOutsideTouchable = true
            setOnDismissListener {
                onDismiss()
            }
        }

        binding.btnDismiss.setOnClickListener {
            sharedPrefs.edit().putBoolean("tooltip_dark_mode_shown", true).apply()
            popupWindow?.dismiss()
        }

        // --- Calculate Position ---
        binding.root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupWidth = binding.root.measuredWidth
        val popupHeight = binding.root.measuredHeight

        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)
        val anchorX = location[0]
        val anchorY = location[1]
        val anchorWidth = anchorView.width
        val anchorHeight = anchorView.height
        val anchorCenterX = anchorX + anchorWidth / 2f

        val density = context.resources.displayMetrics.density
        val screenWidth = context.resources.displayMetrics.widthPixels
        val margin = (16 * density).toInt()

        // Horizontal position: Center popup relative to anchor
        var popupX = (anchorCenterX - popupWidth / 2f).toInt()

        // Clamping to screen edges
        if (popupX < margin) popupX = margin
        if (popupX + popupWidth > screenWidth - margin) {
            popupX = screenWidth - popupWidth - margin
        }

        // Vertical position: Below anchor
        val popupY = anchorY + anchorHeight + (8 * density).toInt()

        // Calculate arrow offset relative to popup start
        val relativeArrowX = anchorCenterX - popupX
        binding.tooltipBackground.arrowOffsetX = relativeArrowX

        popupWindow?.showAtLocation(anchorView.rootView, 0, popupX, popupY)
    }

    fun isShown(): Boolean = sharedPrefs.getBoolean("tooltip_dark_mode_shown", false)
}

package com.mi.bibliarv1960.ui.components

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.view.View
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.mi.bibliarv1960.R

class BookmarksOnboarding(private val activity: Activity) {

    private val sharedPrefs = activity.getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)

    fun start(
        categoriesView: View,
        editView: View,
        fabView: View
    ) {
        if (sharedPrefs.getBoolean("onboarding_bookmarks_shown", false)) {
            return
        }

        TapTargetSequence(activity)
            .targets(
                TapTarget.forView(categoriesView, "Categorías", "Toca las categorías para filtrar tus marcadores por color. Puedes seleccionar varias a la vez.")
                    .outerCircleColor(R.color.tooltip_bg)
                    .targetCircleColor(R.color.white)
                    .titleTextSize(22)
                    .descriptionTextSize(16)
                    .descriptionTextColor(R.color.white)
                    .textColor(R.color.white)
                    .textTypeface(Typeface.SANS_SERIF)
                    .dimColor(R.color.black)
                    .drawShadow(true)
                    .cancelable(false)
                    .tintTarget(true)
                    .transparentTarget(true)
                    .targetRadius(70),

                TapTarget.forView(editView, "Editar", "Activa el modo edición para renombrar tus categorías personalizadas.")
                    .outerCircleColor(R.color.tooltip_bg)
                    .targetCircleColor(R.color.white)
                    .titleTextSize(22)
                    .descriptionTextSize(16)
                    .descriptionTextColor(R.color.white)
                    .textColor(R.color.white)
                    .textTypeface(Typeface.SANS_SERIF)
                    .dimColor(R.color.black)
                    .drawShadow(true)
                    .cancelable(false)
                    .tintTarget(true)
                    .transparentTarget(true)
                    .targetRadius(30),

                TapTarget.forView(fabView, "Nueva categoría", "Usa este botón para agregar una nueva categoría de marcadores personalizada.")
                    .outerCircleColor(R.color.tooltip_bg)
                    .targetCircleColor(R.color.white)
                    .titleTextSize(22)
                    .descriptionTextSize(16)
                    .descriptionTextColor(R.color.white)
                    .textColor(R.color.white)
                    .textTypeface(Typeface.SANS_SERIF)
                    .dimColor(R.color.black)
                    .drawShadow(true)
                    .cancelable(false)
                    .tintTarget(true)
                    .transparentTarget(true)
                    .targetRadius(30)
            )
            .listener(object : TapTargetSequence.Listener {
                override fun onSequenceFinish() { markAsShown() }
                override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {}
                override fun onSequenceCanceled(lastTarget: TapTarget?) { markAsShown() }
            })
            .start()
    }

    private fun markAsShown() {
        sharedPrefs.edit().putBoolean("onboarding_bookmarks_shown", true).apply()
    }

    fun isShown(): Boolean = sharedPrefs.getBoolean("onboarding_bookmarks_shown", false)
}

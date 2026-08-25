package com.mi.bibliarv1960.ui.components

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.View
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.mi.bibliarv1960.R

class ReaderOnboarding(private val activity: Activity) {

    private val sharedPrefs = activity.getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)

    fun start(
        menuView: View,
        translationView: View,
        progressView: View,
        audioView: View,
        themeView: View,
        verseView: View,
        onFinished: () -> Unit = {}
    ) {
        if (sharedPrefs.getBoolean("onboarding_reader_shown", false)) {
            Log.d("ReaderOnboarding", "Tour already shown, skipping.")
            onFinished()
            return
        }

        Log.d("ReaderOnboarding", "Starting sequence with 6 steps.")

        TapTargetSequence(activity)
            .targets(
                // 1. Menú
                TapTarget.forView(menuView, "Menú", "Toca aquí para moverse entre las diferentes pantallas de la app: inicio, notas, marcadores, y más.")
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
                    .id(0)
                    .targetRadius(30),

                // 2. Versiones de la Biblia
                TapTarget.forView(translationView, "Versiones de la Biblia", "Toca aquí para cambiar entre diferentes versiones, como la RVA 1909 o Lenguaje Moderno.")
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
                    .id(1)
                    .targetRadius(30),

                // 3. Marcado como leído
                TapTarget.forView(progressView, "Marcado como leído", "Usa este botón para marcar un capítulo como leído cuando lo hayas terminado completo.")
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
                    .id(2)
                    .targetRadius(30),

                // 4. Lectura por voz
                TapTarget.forView(audioView, "Lectura por voz", "Pulsa el icono del altavoz para escuchar el capítulo actual.")
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
                    .id(3)
                    .targetRadius(30),

                // 5. Modo oscuro
                TapTarget.forView(themeView, "Modo oscuro", "Alterna entre el modo claro y oscuro para una lectura más cómoda.")
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
                    .id(4)
                    .targetRadius(30),

                // 6. Interacción con versículos
                TapTarget.forView(verseView, "Tocar versículo", "Toca cualquier versículo para añadir notas, marcadores de color o compartirlo.")
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
                    .tintTarget(false)
                    .transparentTarget(true)
                    .id(5)
                    .targetRadius(80)
            )
            .listener(object : TapTargetSequence.Listener {
                override fun onSequenceFinish() {
                    Log.d("ReaderOnboarding", "Sequence FINISHED.")
                    markAsShown()
                    onFinished()
                }

                override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {
                    Log.d("ReaderOnboarding", "Step completed. ID: ${lastTarget?.id()}")
                }

                override fun onSequenceCanceled(lastTarget: TapTarget?) {
                    Log.d("ReaderOnboarding", "Sequence CANCELED at ID: ${lastTarget?.id()}")
                    markAsShown()
                    onFinished()
                }
            })
            .start()
    }

    private fun markAsShown() {
        sharedPrefs.edit().putBoolean("onboarding_reader_shown", true).apply()
    }

    fun isShown(): Boolean = sharedPrefs.getBoolean("onboarding_reader_shown", false)
}

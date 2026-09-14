package com.mi.bibliarv1960.ui.onboarding

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned

fun Modifier.spotlightTarget(
    key: String,
    viewModel: GuidedTourViewModel
): Modifier = this.onGloballyPositioned { coordinates ->
    val currentStep = viewModel.steps.getOrNull(viewModel.currentStepIndex.value)
    if (viewModel.isTourActive.value && currentStep?.targetKey == key) {
        viewModel.updateTargetRect(coordinates.boundsInWindow())
    }
}

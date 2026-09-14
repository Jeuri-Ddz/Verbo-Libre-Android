package com.mi.bibliarv1960.ui.onboarding

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mi.bibliarv1960.data.preferences.DataStoreManager
import com.mi.bibliarv1960.utils.SpeechManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TourStep(
    val id: Int,
    val text: String,
    val targetKey: String?,
    val route: String,
    val drawerRequired: Boolean = false
)

@HiltViewModel
class GuidedTourViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val speechManager: SpeechManager
) : ViewModel() {

    private val _currentStepIndex = mutableStateOf(-1)
    val currentStepIndex: State<Int> = _currentStepIndex

    private val _targetRect = MutableStateFlow<Rect?>(null)
    val targetRect: StateFlow<Rect?> = _targetRect

    private val _isTourActive = mutableStateOf(false)
    val isTourActive: State<Boolean> = _isTourActive

    val hasSeenOnboarding: StateFlow<Boolean> = dataStoreManager.hasSeenOnboarding
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val steps = listOf(
        TourStep(
            id = 0,
            text = "Bienvenido a Verbo Libre. Un espacio para leer, meditar y crecer en tu fe, un día a la vez.",
            targetKey = null,
            route = "home"
        ),
        TourStep(
            id = 1,
            text = "Explora la Biblia completa en Reina-Valera. Toca cualquier libro para elegir el capítulo que quieras leer.",
            targetKey = "book_row_1",
            route = "home"
        ),
        TourStep(
            id = 2,
            text = "¿Cómo marcar tu lectura? Usa el botón ✓ para marcar un capítulo como leído solo cuando lo hayas terminado completo. Si te quedas a la mitad, guarda un marcador en el último versículo que leíste — así, la próxima vez, sabes exactamente por dónde continuar. Cuando termines el capítulo completo, entonces marca ✓.",
            targetKey = "reader_check_button",
            route = "reader/1/1"
        ),
        TourStep(
            id = 3,
            text = "Guarda tus versículos favoritos o el punto donde te quedaste, para volver a ellos en un toque.",
            targetKey = "drawer_bookmarks",
            route = "reader/1/1",
            drawerRequired = true
        ),
        TourStep(
            id = 4,
            text = "Escribe tus reflexiones directamente sobre cualquier versículo. Tus notas quedan guardadas junto al pasaje, listas para releer cuando quieras.",
            targetKey = "notes_search",
            route = "all_notes"
        ),
        TourStep(
            id = 5,
            text = "Cada día tenemos una meditación distinta para ti, con un versículo y una reflexión para empezar el día con propósito.",
            targetKey = "devotional_content",
            route = "devotional"
        ),
        TourStep(
            id = 6,
            text = "Una palabra de aliento diferente cada día, lista para leer o compartir con quien la necesite.",
            targetKey = "daily_verse_share",
            route = "daily_verse"
        ),
        TourStep(
            id = 7,
            text = "Pon a prueba lo que sabes con un reto bíblico nuevo cada día. Es rápido, divertido, y te ayuda a recordar.",
            targetKey = "challenge_start",
            route = "challenge"
        ),
        TourStep(
            id = 8,
            text = "Ajusta la app a tu gusto, de día o de noche, desde el menú lateral.",
            targetKey = "drawer_theme_toggle",
            route = "challenge",
            drawerRequired = true
        )
    )

    fun startTour() {
        _currentStepIndex.value = 0
        _isTourActive.value = true
        _targetRect.value = null
        speakCurrentStep()
    }

    fun nextStep(onNavigate: (String) -> Unit, onDrawerAction: (Boolean) -> Unit) {
        val nextIndex = _currentStepIndex.value + 1
        if (nextIndex < steps.size) {
            _currentStepIndex.value = nextIndex
            _targetRect.value = null
            val nextStep = steps[nextIndex]
            
            // Navegación
            onNavigate(nextStep.route)
            
            // Control del Drawer
            onDrawerAction(nextStep.drawerRequired)
            
            speakCurrentStep()
        } else {
            completeTour()
        }
    }

    fun skipTour() {
        completeTour()
    }

    private fun completeTour() {
        _isTourActive.value = false
        _currentStepIndex.value = -1
        _targetRect.value = null
        speechManager.stop()
        viewModelScope.launch {
            dataStoreManager.saveHasSeenOnboarding(true)
        }
    }

    fun updateTargetRect(rect: Rect) {
        _targetRect.value = rect
    }

    private fun speakCurrentStep() {
        val index = _currentStepIndex.value
        if (index in steps.indices) {
            speechManager.speakVerse(0, steps[index].text)
        }
    }

    fun stopSpeaking() {
        speechManager.stop()
    }
}

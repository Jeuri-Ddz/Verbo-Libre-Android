package com.mi.bibliarv1960.ui.challenges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mi.bibliarv1960.data.local.entities.ChallengeEntity
import com.mi.bibliarv1960.data.preferences.DataStoreManager
import com.mi.bibliarv1960.data.repository.BibleRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Random
import javax.inject.Inject

@Serializable
sealed class ChallengeResult {
    @Serializable
    data class Trivia(val selectedIndex: Int) : ChallengeResult()
}

@Serializable
data class DailyChallengeStatus(
    val date: String,
    val isCompleted: Boolean,
    val isCorrect: Boolean,
    val result: ChallengeResult? = null
)

@HiltViewModel
class ChallengeViewModel @Inject constructor(
    private val repository: BibleRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val _currentChallenge = MutableStateFlow<ChallengeEntity?>(null)
    val currentChallenge: StateFlow<ChallengeEntity?> = _currentChallenge.asStateFlow()

    private val _challengeStatus = MutableStateFlow<DailyChallengeStatus?>(null)
    val challengeStatus: StateFlow<DailyChallengeStatus?> = _challengeStatus.asStateFlow()

    private val _isStatusLoaded = MutableStateFlow(false)
    val isStatusLoaded: StateFlow<Boolean> = _isStatusLoaded.asStateFlow()

    private var allChallengesList: List<ChallengeEntity> = emptyList()
    private var isInitialized = false

    val completedCount: StateFlow<Int> = dataStoreManager.challengeCompletedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        loadChallengesAndStatus()
    }

    private fun loadChallengesAndStatus() {
        if (isInitialized) return
        isInitialized = true
        
        viewModelScope.launch {
            try {
                // 1. Cargar el estado guardado primero
                loadChallengeStatus()
                
                // 2. Observar retos y seleccionar el del día
                repository.allChallenges
                    .catch { e -> e.printStackTrace() }
                    .collect { challenges ->
                        allChallengesList = challenges
                        if (challenges.isNotEmpty()) {
                            selectDailyChallenge(challenges)
                        }
                        // Marcar como cargado si no se hizo en loadChallengeStatus
                        _isStatusLoaded.value = true
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                _isStatusLoaded.value = true
            }
        }
    }

    private suspend fun loadChallengeStatus() {
        try {
            val todayDate = LocalDate.now().format(dateFormatter)
            val lastDate = dataStoreManager.lastChallengeDate.firstOrNull()
            
            if (lastDate == todayDate) {
                val resultJson = dataStoreManager.lastChallengeResult.firstOrNull()
                if (!resultJson.isNullOrBlank()) {
                    try {
                        val status = Json.decodeFromString<DailyChallengeStatus>(resultJson)
                        _challengeStatus.value = status
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _challengeStatus.value = null  // Reset si falla el parsing
                    }
                }
            } else {
                _challengeStatus.value = null  // Nuevo día, limpiar estado
            }
            _isStatusLoaded.value = true
        } catch (e: Exception) {
            e.printStackTrace()
            _isStatusLoaded.value = true
        }
    }

    private suspend fun selectDailyChallenge(challenges: List<ChallengeEntity>) {
        val today = LocalDate.now()
        
        // 1. Obtener o generar la semilla y fecha de inicio
        val seed = dataStoreManager.deviceSeed.first() ?: "global"
        val firstLaunchStr = dataStoreManager.firstLaunchDate.first() ?: run {
            val current = today.format(dateFormatter)
            dataStoreManager.saveFirstLaunchDate(current)
            current
        }
        val firstLaunchDate = LocalDate.parse(firstLaunchStr, dateFormatter)
        val diasTranscurridos = ChronoUnit.DAYS.between(firstLaunchDate, today).toInt()

        // 2. Filtrar y preparar pool
        val filteredChallenges = challenges.filter { it.type == "TRIVIA" }
        val pool = if (filteredChallenges.isNotEmpty()) filteredChallenges else challenges
        
        if (pool.isNotEmpty()) {
            val n = pool.size
            val ciclo = diasTranscurridos / n
            val diaEnCiclo = diasTranscurridos % n
            
            // 3. Shuffle determinista (offset 2000)
            val random = Random(seed.hashCode().toLong() + ciclo + 2000)
            val shuffledIndices = (0 until n).toList().shuffled(random)
            
            _currentChallenge.value = pool[shuffledIndices[diaEnCiclo]]
        }
    }

    fun submitResult(result: ChallengeResult, isCorrect: Boolean) {
        viewModelScope.launch {
            val todayDate = LocalDate.now().format(dateFormatter)
            val status = DailyChallengeStatus(
                date = todayDate,
                isCompleted = true,
                isCorrect = isCorrect,
                result = result
            )
            val resultJson = Json.encodeToString(status)
            dataStoreManager.saveChallengeResult(todayDate, resultJson, isCorrect)
            _challengeStatus.value = status
        }
    }

    private var previewIndex = -1
    fun nextChallengePreview() {
        if (allChallengesList.isNotEmpty()) {
            previewIndex = (previewIndex + 1) % allChallengesList.size
            _currentChallenge.value = allChallengesList[previewIndex]
            _challengeStatus.value = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        allChallengesList = emptyList()
        isInitialized = false
    }
}

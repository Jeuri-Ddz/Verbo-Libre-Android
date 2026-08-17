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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Serializable
sealed class ChallengeResult {
    @Serializable
    data class FillVerse(val userWords: List<String>) : ChallengeResult()
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

class ChallengeViewModel(
    private val repository: BibleRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _currentChallenge = MutableStateFlow<ChallengeEntity?>(null)
    val currentChallenge: StateFlow<ChallengeEntity?> = _currentChallenge.asStateFlow()

    private val _challengeStatus = MutableStateFlow<DailyChallengeStatus?>(null)
    val challengeStatus: StateFlow<DailyChallengeStatus?> = _challengeStatus.asStateFlow()

    private var allChallengesList: List<ChallengeEntity> = emptyList()

    val completedCount: StateFlow<Int> = dataStoreManager.challengeCompletedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        // Cargar todos los retos una vez de forma segura
        viewModelScope.launch {
            try {
                repository.allChallenges.collect { challenges ->
                    allChallengesList = challenges
                    if (challenges.isNotEmpty() && _currentChallenge.value == null) {
                        selectDailyChallenge(challenges)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        loadChallengeStatus()
    }

    private fun loadChallengeStatus() {
        viewModelScope.launch {
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val lastDate = dataStoreManager.lastChallengeDate.firstOrNull()
            if (lastDate == todayDate) {
                val resultJson = dataStoreManager.lastChallengeResult.firstOrNull()
                if (resultJson != null) {
                    try {
                        _challengeStatus.value = Json.decodeFromString<DailyChallengeStatus>(resultJson)
                    } catch (e: Exception) { e.printStackTrace() }
                }
            }
        }
    }

    private fun selectDailyChallenge(challenges: List<ChallengeEntity>) {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        
        val targetType = if (dayOfYear % 2 == 0) "FILL_VERSE" else "TRIVIA"
        val filteredChallenges = challenges.filter { it.type == targetType }
        val pool = if (filteredChallenges.isNotEmpty()) filteredChallenges else challenges
        
        if (pool.isNotEmpty()) {
            val combinedSeed = "global_challenge_$todayDate"
            val index = abs(combinedSeed.hashCode()) % pool.size
            _currentChallenge.value = pool[index]
        }
    }

    fun submitResult(result: ChallengeResult, isCorrect: Boolean) {
        viewModelScope.launch {
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
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
}

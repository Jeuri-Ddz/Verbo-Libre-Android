package com.mi.bibliarv1960.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mi.bibliarv1960.DebugConfig
import com.mi.bibliarv1960.data.repository.BibleRepository
import com.mi.bibliarv1960.data.local.entities.*
import com.mi.bibliarv1960.data.preferences.DataStoreManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.abs

import com.mi.bibliarv1960.utils.SpeechManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BibleViewModel @Inject constructor(
    private val repository: BibleRepository,
    private val dataStoreManager: DataStoreManager,
    private val speechManager: SpeechManager
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val _todayDevotional = MutableStateFlow<DevotionalEntity?>(null)

    val allDevotionals: StateFlow<List<DevotionalEntity>> = repository.allDevotionals.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    private val _previewDevotionalIndex = MutableStateFlow<Int?>(null)
    private val _previewDevotionalBgIndex = MutableStateFlow<Int?>(null)
    
    val displayDevotional: StateFlow<DevotionalEntity?> = combine(
        _todayDevotional,
        allDevotionals,
        _previewDevotionalIndex
    ) { today, all, previewIdx ->
        if ((previewIdx != null) && (all.isNotEmpty())) {
            all[previewIdx % all.size]
        } else {
            today
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null,
    )

    private val _todayVerse = MutableStateFlow<DailyVerseEntity?>(null)

    val allDailyVerses: StateFlow<List<DailyVerseEntity>> = repository.allDailyVerses.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    private val _previewVerseIndex = MutableStateFlow<Int?>(null)
    private val _previewVerseBgIndex = MutableStateFlow<Int?>(null)

    val displayDailyVerse: StateFlow<DailyVerseEntity?> = combine(
        _todayVerse,
        allDailyVerses,
        _previewVerseIndex
    ) { today, all, previewIdx ->
        if ((previewIdx != null) && (all.isNotEmpty())) {
            all[previewIdx % all.size]
        } else {
            today
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null,
    )

    // Índices de fondo calculados centralmente
    private val _todayVerseBgIndex = MutableStateFlow(1)
    val todayVerseBgIndex: StateFlow<Int> = _todayVerseBgIndex

    val displayVerseBgIndex: StateFlow<Int> = combine(
        _todayVerseBgIndex,
        _previewVerseBgIndex
    ) { todayBg, previewBg ->
        previewBg ?: todayBg
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 1,
    )

    private val _todayDevotionalBgIndex = MutableStateFlow(6)
    val todayDevotionalBgIndex: StateFlow<Int> = _todayDevotionalBgIndex

    val currentStreak: StateFlow<Int> = dataStoreManager.currentStreak.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0,
    )

    val lastReadDate: StateFlow<String?> = dataStoreManager.lastReadDate.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null,
    )

    val isDarkMode: StateFlow<Boolean> = dataStoreManager.isDarkMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false,
    )

    val tooltipReaderRead: StateFlow<Boolean> = dataStoreManager.tooltipReaderRead.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false,
    )

    val tooltipReaderVerse: StateFlow<Boolean> = dataStoreManager.tooltipReaderVerse.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false,
    )

    val tooltipHomeSearch: StateFlow<Boolean> = dataStoreManager.tooltipHomeSearch.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false,
    )

    val tooltipNotesSearch: StateFlow<Boolean> = dataStoreManager.tooltipNotesSearch.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false,
    )

    val tooltipBookmarksFilter: StateFlow<Boolean> = dataStoreManager.tooltipBookmarksFilter.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false,
    )

    init {
        initializeDailyData()
        speechManager.setOnVerseCompleteListener { _ ->
            speakNext()
        }
    }

    private fun initializeDailyData() {
        viewModelScope.launch {
            // 1. Obtener o generar la semilla UNA SOLA VEZ de forma segura
            val seed = dataStoreManager.deviceSeed.first() ?: run {
                val newSeed = UUID.randomUUID().toString()
                dataStoreManager.saveDeviceSeed(newSeed)
                newSeed
            }

            val todayDate = LocalDate.now().format(dateFormatter)

            // 2. Sorteo de Contenido Devocional
            launch {
                repository.allDevotionals.collect { devotionals ->
                    if (devotionals.isNotEmpty()) {
                        val forcedId = DebugConfig.DEBUG_FORCE_DEVOTIONAL_ID
                        if (forcedId != null) {
                            _todayDevotional.value = devotionals.find { it.id == forcedId } ?: devotionals.first()
                        } else {
                            // Usamos un salt específico para devocionales
                            val combined = "${seed}_devo_$todayDate"
                            _todayDevotional.value = devotionals[abs(combined.hashCode()) % devotionals.size]
                        }
                    }
                }
            }
            
            // 3. Sorteo de Contenido Versículo
            launch {
                repository.allDailyVerses.collect { verses ->
                    if (verses.isNotEmpty()) {
                        // Usamos un salt específico para versículos
                        val combined = "${seed}_verse_$todayDate"
                        _todayVerse.value = verses[abs(combined.hashCode()) % verses.size]
                    }
                }
            }

            // 4. Sorteo de Fondos (Determinístico con Salt único)
            val bgVerseStr = "${seed}_bg_v_$todayDate"
            _todayVerseBgIndex.value = (abs(bgVerseStr.hashCode()) % 5) + 1 // 1 al 5

            val bgDevoStr = "${seed}_bg_d_$todayDate"
            _todayDevotionalBgIndex.value = (abs(bgDevoStr.hashCode()) % 5) + 6 // 6 al 10
        }
    }

    fun markAsRead() {
        viewModelScope.launch {
            val todayDate = LocalDate.now().format(dateFormatter)
            val lastRead = dataStoreManager.lastReadDate.first()
            
            if (lastRead == todayDate) return@launch 

            val yesterdayDate = LocalDate.now().minusDays(1).format(dateFormatter)

            val currentStreakValue = currentStreak.value
            val newStreak = if (lastRead == yesterdayDate) {
                currentStreakValue + 1
            } else {
                1
            }
            dataStoreManager.updateStreak(newStreak, todayDate)
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            dataStoreManager.toggleDarkMode(!isDarkMode.value)
        }
    }

    fun dismissReaderTooltip() {
        viewModelScope.launch {
            dataStoreManager.saveTooltipReaderRead(true)
        }
    }

    fun dismissReaderVerseTooltip() {
        viewModelScope.launch {
            dataStoreManager.saveTooltipReaderVerse(true)
        }
    }

    fun dismissHomeSearchTooltip() {
        viewModelScope.launch {
            dataStoreManager.saveTooltipHomeSearch(true)
        }
    }

    fun dismissNotesSearchTooltip() {
        viewModelScope.launch {
            dataStoreManager.saveTooltipNotesSearch(true)
        }
    }

    fun dismissBookmarksFilterTooltip() {
        viewModelScope.launch {
            dataStoreManager.saveTooltipBookmarksFilter(true)
        }
    }

    fun nextDevotionalPreview() {
        val currentList = allDevotionals.value
        if (currentList.isEmpty()) return

        val forcedId = DebugConfig.DEBUG_FORCE_DEVOTIONAL_ID
        if (forcedId != null) {
            // --- MODO DEBUG SECUENCIAL (1 al 200) ---
            val currentDevo = displayDevotional.value
            val currentId = currentDevo?.id ?: forcedId

            // Avanzar al siguiente ID, volviendo al 1 después del 200
            val nextId = if (currentId < 200) currentId + 1 else 1
            val nextIndex = currentList.indexOfFirst { it.id == nextId }

            if (nextIndex != -1) {
                _previewDevotionalIndex.value = nextIndex
            } else {
                // Si el ID exacto no existe, usamos el siguiente índice disponible
                val currentIndex = _previewDevotionalIndex.value ?: -1
                _previewDevotionalIndex.value = (currentIndex + 1) % currentList.size
            }
        } else {
            // --- COMPORTAMIENTO NORMAL ---
            val currentIndex = _previewDevotionalIndex.value ?: -1
            _previewDevotionalIndex.value = (currentIndex + 1) % currentList.size
        }

        val currentBgIndex = _previewDevotionalBgIndex.value ?: todayDevotionalBgIndex.value
        // Fondos de devo son del 6 al 10. (6-1=5, 5%5=0, 0+6=6...)
        // Lógica simple: si es 10 -> 6, si no -> +1
        _previewDevotionalBgIndex.value = if (currentBgIndex >= 10) 6 else currentBgIndex + 1
    }

    fun nextDailyVersePreview() {
        val currentList = allDailyVerses.value
        if (currentList.isEmpty()) return

        val currentIndex = _previewVerseIndex.value ?: -1
        _previewVerseIndex.value = (currentIndex + 1) % currentList.size

        val currentBgIndex = _previewVerseBgIndex.value ?: todayVerseBgIndex.value
        _previewVerseBgIndex.value = (currentBgIndex % 5) + 1 // Ciclo 1-5
    }

    val allTranslations: StateFlow<List<TranslationEntity>> = repository.allTranslations.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    private val _selectedTranslationId = dataStoreManager.selectedTranslationId.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "rv1909",
    )
    val selectedTranslationId: StateFlow<String> = _selectedTranslationId

    fun selectTranslation(id: String) {
        viewModelScope.launch {
            dataStoreManager.saveTranslationId(id)
        }
    }

    val allBooks: StateFlow<List<BookEntity>> = repository.allBooks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    val allBookmarks: StateFlow<List<BookmarkEntity>> = repository.allBookmarks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    val allCategories: StateFlow<List<BookmarkCategoryEntity>> = repository.allCategories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    private val _activeCategoryFilters = MutableStateFlow<Set<Int>?>(null)
    val activeCategoryFilters: StateFlow<Set<Int>?> = _activeCategoryFilters

    val filteredBookmarks: StateFlow<List<BookmarkEntity>> = combine(
        allBookmarks,
        _activeCategoryFilters,
    ) { bookmarks, filters ->
        if (filters == null) bookmarks
        else bookmarks.filter { it.categoryId in filters }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    fun toggleCategoryFilter(categoryId: Int) {
        val current = _activeCategoryFilters.value
        if (current == null) {
            _activeCategoryFilters.value = setOf(categoryId)
        } else {
            _activeCategoryFilters.value = if (current.contains(categoryId)) {
                current - categoryId
            } else {
                current + categoryId
            }
        }
    }

    @Suppress("unused")
    fun setAllFiltersActive() {
        _activeCategoryFilters.value = null
    }

    private val _currentBookId = MutableStateFlow(1)
    private val _currentChapter = MutableStateFlow(1)

    val currentBook: StateFlow<BookEntity?> = _currentBookId.flatMapLatest { id ->
        repository.getBookById(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null,
    )

    val currentVerses: StateFlow<List<VerseEntity>> = combine(
        _currentBookId,
        _currentChapter,
        _selectedTranslationId
    ) { bookId, chapter, translationId ->
        Triple(bookId, chapter, translationId)
    }.flatMapLatest { (bookId, chapter, translationId) ->
        repository.getVersesByChapter(bookId, chapter, translationId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    private val _fontSize = MutableStateFlow(18f)
    val fontSize: StateFlow<Float> = _fontSize

    // --- Audio Biblia (TTS) ---
    private val _isSpeaking = MutableStateFlow(value = false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    private val _isPaused = MutableStateFlow(value = false)
    val isPaused: StateFlow<Boolean> = _isPaused

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed

    private val _currentSpeakingVerse = MutableStateFlow<Int?>(null)
    val currentSpeakingVerse: StateFlow<Int?> = _currentSpeakingVerse

    private var speakingList: List<VerseEntity> = emptyList()
    private var currentVerseIndex: Int = -1

    fun toggleSpeaking(verses: List<VerseEntity>) {
        if (_isSpeaking.value) {
            stopSpeaking()
        } else {
            startSpeaking(verses)
        }
    }

    fun togglePauseResume() {
        if (_isPaused.value) {
            _isPaused.value = false
            speakCurrent()
        } else {
            _isPaused.value = true
            speechManager.pause()
        }
    }

    fun cyclePlaybackSpeed() {
        val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f)
        val currentIndex = speeds.indexOf(_playbackSpeed.value)
        val nextIndex = (currentIndex + 1) % speeds.size
        val nextSpeed = speeds[nextIndex]
        _playbackSpeed.value = nextSpeed
        speechManager.setSpeed(nextSpeed)
    }

    fun openVoiceSettings() {
        speechManager.openSystemSettings()
    }

    fun speakTooltip(text: String) {
        speechManager.speakVerse(0, text) // Usamos ID 0 para tooltips
    }

    private fun startSpeaking(verses: List<VerseEntity>) {
        if (verses.isEmpty()) return
        speakingList = verses
        currentVerseIndex = 0
        _isSpeaking.value = true
        _isPaused.value = false
        speakCurrent()
    }

    private fun speakCurrent() {
        if (currentVerseIndex in speakingList.indices) {
            val verse = speakingList[currentVerseIndex]
            _currentSpeakingVerse.value = verse.verse
            speechManager.speakVerse(verse.verse, verse.text)
        } else {
            stopSpeaking()
        }
    }

    private fun speakNext() {
        currentVerseIndex++
        viewModelScope.launch {
            speakCurrent()
        }
    }

    fun stopSpeaking() {
        speechManager.stop()
        _isSpeaking.value = false
        _isPaused.value = false
        _currentSpeakingVerse.value = null
        currentVerseIndex = -1
    }

    override fun onCleared() {
        speechManager.shutdown()
    }

    fun loadChapter(bookId: Int, chapter: Int) {
        stopSpeaking()
        _currentBookId.value = bookId
        _currentChapter.value = chapter
    }

    fun updateFontSize(delta: Float) {
        _fontSize.value = (_fontSize.value + delta).coerceIn(12f, 40f)
    }

    fun toggleBookmark(verse: VerseEntity, categoryId: Int, bookName: String) {
        viewModelScope.launch {
            repository.deleteBookmark(verse.book_id, verse.chapter, verse.verse)
            
            val existingBookmark = allBookmarks.value.find { 
                (it.bookId == verse.book_id) && (it.chapter == verse.chapter) && (it.verse == verse.verse)
            }
            
            if (existingBookmark == null || existingBookmark.categoryId != categoryId) {
                repository.insertBookmark(
                    BookmarkEntity(
                        bookId = verse.book_id,
                        bookName = bookName,
                        chapter = verse.chapter,
                        verse = verse.verse,
                        text = verse.text,
                        categoryId = categoryId
                    )
                )
            }
        }
    }

    fun addCategory(name: String, colorHex: String) {
        viewModelScope.launch {
            repository.insertCategory(BookmarkCategoryEntity(name = name, colorHex = colorHex))
        }
    }

    fun renameCategory(id: Int, newName: String) {
        viewModelScope.launch {
            repository.updateCategoryName(id, newName)
        }
    }

    @Suppress("unused")
    fun removeCategory(id: Int) {
        viewModelScope.launch {
            repository.deleteCategory(id)
        }
    }

    @Suppress("unused")
    fun selectBook(bookId: Int) {
        _currentBookId.value = bookId
        _currentChapter.value = 1
    }

    @Suppress("unused")
    fun selectChapter(chapter: Int) {
        _currentChapter.value = chapter
    }

    // --- Manual Reading Progress (Optimized) ---
    val allProgress: StateFlow<List<ReadingProgressEntity>> = repository.getAllProgress().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    // Mapa optimizado para consultas rápidas: BookId -> Set de capítulos leídos
    val progressByBook: StateFlow<Map<Int, Set<Int>>> = allProgress.map { list ->
        list.groupBy { it.bookId }.mapValues { entry -> entry.value.map { it.chapter }.toSet() }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap(),
    )

    val globalChapterCount: StateFlow<Int> = repository.getTotalChapterCount().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 1189,
    )

    val bookChapterCounts: StateFlow<List<BookChapterCount>> = repository.getChapterCountsByBook().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    val globalProgressPercent: StateFlow<Float> = combine(
        allProgress,
        globalChapterCount
    ) { progress, total ->
        if (total == 0) 0f else progress.size.toFloat() / total.toFloat()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f,
    )

    val bookProgress: StateFlow<Map<Int, Pair<Int, Int>>> = combine(
        allBooks,
        progressByBook,
        bookChapterCounts
    ) { books, progressMap, counts ->
        val countsMap = counts.associate { it.bookId to it.chapterCount }
        books.associate { book ->
            val readCount = progressMap[book.id]?.size ?: 0
            val totalCount = countsMap[book.id] ?: book.chaptersCount
            book.id to Pair(readCount, totalCount)
        }
    }.flowOn(kotlinx.coroutines.Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap(),
    )

    val currentChapterIsRead: StateFlow<Boolean> = combine(
        _currentBookId,
        _currentChapter,
        progressByBook
    ) { bookId, chapter, progressMap ->
        progressMap[bookId]?.contains(chapter) == true
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false,
    )

    fun markChapterAsRead(bookId: Int, chapter: Int) {
        viewModelScope.launch {
            repository.markChapterAsRead(bookId, chapter)
        }
    }

    fun unmarkChapter(bookId: Int, chapter: Int) {
        viewModelScope.launch {
            repository.unmarkChapter(bookId, chapter)
        }
    }

    fun toggleCurrentChapterRead() {
        val bookId = _currentBookId.value
        val chapter = _currentChapter.value
        val isRead = currentChapterIsRead.value
        if (isRead) {
            unmarkChapter(bookId, chapter)
        } else {
            markChapterAsRead(bookId, chapter)
        }
    }
}

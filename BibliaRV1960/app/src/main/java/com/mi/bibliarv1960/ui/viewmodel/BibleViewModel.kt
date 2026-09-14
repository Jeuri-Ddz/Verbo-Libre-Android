package com.mi.bibliarv1960.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mi.bibliarv1960.data.repository.BibleRepository
import com.mi.bibliarv1960.data.repository.NoteRepository
import com.mi.bibliarv1960.data.local.entities.*
import com.mi.bibliarv1960.data.preferences.DataStoreManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.*
import java.time.temporal.ChronoUnit
import android.app.NotificationManager
import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext

import com.mi.bibliarv1960.utils.SpeechManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BibleViewModel @Inject constructor(
    private val repository: BibleRepository,
    private val noteRepository: NoteRepository,
    private val dataStoreManager: DataStoreManager,
    private val speechManager: SpeechManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val _debugOffset = MutableStateFlow(0)

    val displayDevotional: StateFlow<DevotionalEntity?> = combine(
        repository.allDevotionals.filter { it.isNotEmpty() },
        _debugOffset
    ) { devotionals, offset ->
        // Obtenemos los datos necesarios para el cálculo determinista
        val seed = dataStoreManager.deviceSeed.first() ?: "default"
        val firstLaunchStr = dataStoreManager.firstLaunchDate.first() ?: LocalDate.now().format(dateFormatter)
        val firstLaunchDate = LocalDate.parse(firstLaunchStr, dateFormatter)
        val today = LocalDate.now()
        val totalDays = ChronoUnit.DAYS.between(firstLaunchDate, today).toInt() + offset

        val n = devotionals.size
        val ciclo = totalDays / n
        val diaEnCiclo = totalDays % n

        val random = Random(seed.hashCode().toLong() + ciclo)
        val shuffledIndices = (0 until n).toList().shuffled(random)
        devotionals[shuffledIndices[diaEnCiclo]]
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun showNextDevotionalDebug() {
        _debugOffset.value += 1
    }

    private val _isReadToday = MutableStateFlow(false)
    val isReadToday: StateFlow<Boolean> = _isReadToday.asStateFlow()

    private val _streakLostEvent = MutableSharedFlow<Unit>()
    val streakLostEvent: SharedFlow<Unit> = _streakLostEvent.asSharedFlow()

    private val _exportEvent = MutableSharedFlow<String>()
    val exportEvent: SharedFlow<String> = _exportEvent.asSharedFlow()

    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent: SharedFlow<String> = _messageEvent.asSharedFlow()

    private val _todayVerse = MutableStateFlow<DailyVerseEntity?>(null)

    val displayDailyVerse: StateFlow<DailyVerseEntity?> = _todayVerse.asStateFlow()

    // Índices de fondo calculados centralmente
    private val _todayVerseBgIndex = MutableStateFlow(1)
    val displayVerseBgIndex: StateFlow<Int> = _todayVerseBgIndex.asStateFlow()

    private val _todayDevotionalBgIndex = MutableStateFlow(3)
    val todayDevotionalBgIndex: StateFlow<Int> = combine(_todayDevotionalBgIndex, _debugOffset) { index, offset ->
        // Si el offset es 0, usamos el fondo fijo 3 solicitado.
        // Si el usuario presiona "Verbo Libre", rotamos para permitir auditoría.
        if (offset == 0) 3 else (3 + offset - 1) % 10 + 1
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3)

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

    val tooltipReaderTts: StateFlow<Boolean> = dataStoreManager.tooltipReaderTts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false,
    )

    val tooltipReaderTranslation: StateFlow<Boolean> = dataStoreManager.tooltipReaderTranslation.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false,
    )

    val tooltipReaderVerse: StateFlow<Boolean> = dataStoreManager.tooltipReaderVerse.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false,
    )

    val tooltipReaderTheme: StateFlow<Boolean> = dataStoreManager.tooltipReaderTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false,
    )

    val tooltipHomeWelcome: StateFlow<Boolean> = dataStoreManager.tooltipHomeWelcome.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false,
    )

    val tooltipNotesSearch: StateFlow<Boolean> = dataStoreManager.tooltipNotesSearch.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false,
    )

    val tooltipBookmarksCategories: StateFlow<Boolean> = dataStoreManager.tooltipBookmarksCategories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false,
    )

    val tooltipBookmarksInfo: StateFlow<Boolean> = dataStoreManager.tooltipBookmarksInfo.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false,
    )

    val tooltipBookmarksFab: StateFlow<Boolean> = dataStoreManager.tooltipBookmarksFab.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false,
    )

    val isDiscontinuousMode: StateFlow<Boolean> = dataStoreManager.isDiscontinuousMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false,
    )

    val autoDndOnReading: StateFlow<Boolean> = dataStoreManager.autoDndOnReading.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false,
    )

    val fontSize: StateFlow<Float> = dataStoreManager.fontSize.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 18f,
    )

    val fontFamily: StateFlow<String> = dataStoreManager.fontFamily.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "SANS_SERIF",
    )

    val preferredTtsSpeed: StateFlow<Float> = dataStoreManager.preferredTtsSpeed.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 1.0f,
    )

    val showTrivia: StateFlow<Boolean> = dataStoreManager.showTrivia.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true,
    )

    val showDevocional: StateFlow<Boolean> = dataStoreManager.showDevocional.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true,
    )

    val showDevotionalDot: StateFlow<Boolean> = dataStoreManager.lastDevotionalVisitDate.map { lastDate ->
        lastDate != LocalDate.now().format(dateFormatter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val showDailyVerseDot: StateFlow<Boolean> = dataStoreManager.lastDailyVerseVisitDate.map { lastDate ->
        lastDate != LocalDate.now().format(dateFormatter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

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

            val today = LocalDate.now()
            val todayStr = today.format(dateFormatter)
            
            // 2. Obtener o guardar la fecha de primer inicio (Día 0)
            val firstLaunchStr = dataStoreManager.firstLaunchDate.first() ?: run {
                dataStoreManager.saveFirstLaunchDate(todayStr)
                todayStr
            }
            val firstLaunchDate = LocalDate.parse(firstLaunchStr, dateFormatter)
            val diasTranscurridos = ChronoUnit.DAYS.between(firstLaunchDate, today).toInt()

            // 2.5 Validación de racha y estado de lectura hoy
            val lastRead = dataStoreManager.lastReadDate.first()
            _isReadToday.value = (lastRead == todayStr)

            if (lastRead != null && lastRead != todayStr) {
                val yesterdayStr = today.minusDays(1).format(dateFormatter)
                if (lastRead != yesterdayStr) {
                    // Racha perdida: no leyó ayer ni hoy
                    val currentStreakValue = dataStoreManager.currentStreak.first()
                    if (currentStreakValue > 0) {
                        dataStoreManager.updateStreak(0, lastRead)
                        _streakLostEvent.emit(Unit)
                    }
                }
            }
            
            // Sincronización reactiva de la velocidad de voz (TTS)
            // Se lanza en un job separado dentro del scope para no bloquear el resto de la inicialización
            launch {
                preferredTtsSpeed.collect { speed ->
                    _playbackSpeed.value = speed
                    if (_isSpeaking.value) {
                        speechManager.setSpeed(speed)
                    }
                }
            }

            // 3. Sorteo de Contenido Devocional (Mazo Barajado)
            // Ya no es necesario asignar _todayDevotional.value aquí porque ahora es reactivo a través de displayDevotional

            // 4. Sorteo de Contenido Versículo (Mazo Barajado)
            launch {
                val verses = repository.allDailyVerses.filter { it.isNotEmpty() }.first()
                val n = verses.size
                val ciclo = diasTranscurridos / n
                val diaEnCiclo = diasTranscurridos % n
                
                // Seed determinista por ciclo (usamos un offset diferente para variedad)
                val random = Random(seed.hashCode().toLong() + ciclo + 1000)
                val shuffledIndices = (0 until n).toList().shuffled(random)
                
                _todayVerse.value = verses[shuffledIndices[diaEnCiclo]]
            }

            // 5. Sorteo de Fondos (Determinístico basado en el día)
            // Versículo: 1 al 5
            val randomBg = Random(seed.hashCode().toLong() + diasTranscurridos)
            _todayVerseBgIndex.value = randomBg.nextInt(5) + 1
            // Devocional: se inicializa en 3 según solicitud por ahora
            _todayDevotionalBgIndex.value = 3
        }
    }

    fun markAsRead() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val todayStr = today.format(dateFormatter)
            val lastRead = dataStoreManager.lastReadDate.first()
            
            if (lastRead == todayStr) {
                _isReadToday.value = true
                return@launch 
            }

            val yesterdayStr = today.minusDays(1).format(dateFormatter)
            val currentStreakValue = dataStoreManager.currentStreak.first()
            
            val newStreak = if (lastRead == yesterdayStr) {
                currentStreakValue + 1
            } else {
                1
            }
            
            dataStoreManager.updateStreak(newStreak, todayStr)
            _isReadToday.value = true
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

    fun dismissReaderTtsTooltip() {
        viewModelScope.launch {
            dataStoreManager.saveTooltipReaderTts(true)
        }
    }

    fun dismissReaderTranslationTooltip() {
        viewModelScope.launch {
            dataStoreManager.saveTooltipReaderTranslation(true)
        }
    }

    fun dismissReaderVerseTooltip() {
        viewModelScope.launch {
            dataStoreManager.saveTooltipReaderVerse(true)
        }
    }

    fun dismissReaderThemeTooltip() {
        viewModelScope.launch {
            dataStoreManager.saveTooltipReaderTheme(true)
        }
    }

    fun dismissHomeWelcomeTooltip() {
        viewModelScope.launch {
            dataStoreManager.saveTooltipHomeWelcome(true)
        }
    }

    fun dismissNotesSearchTooltip() {
        viewModelScope.launch {
            dataStoreManager.saveTooltipNotesSearch(true)
        }
    }

    fun dismissBookmarksCategoriesTooltip() {
        viewModelScope.launch {
            dataStoreManager.saveTooltipBookmarksCategories(true)
        }
    }

    fun dismissBookmarksInfoTooltip() {
        viewModelScope.launch {
            dataStoreManager.saveTooltipBookmarksInfo(true)
        }
    }

    fun dismissBookmarksFabTooltip() {
        viewModelScope.launch {
            dataStoreManager.saveTooltipBookmarksFab(true)
        }
    }

    fun toggleDiscontinuousMode() {
        viewModelScope.launch {
            dataStoreManager.saveDiscontinuousMode(!isDiscontinuousMode.value)
        }
    }

    fun setAutoDndOnReading(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.saveAutoDndOnReading(enabled)
        }
    }

    fun toggleAutoDnd() {
        setAutoDndOnReading(!autoDndOnReading.value)
    }

    private var originalDndFilter: Int = NotificationManager.INTERRUPTION_FILTER_ALL

    fun hasNotificationPolicyAccess(): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.isNotificationPolicyAccessGranted
    }

    fun setDndActive(active: Boolean) {
        if (!autoDndOnReading.value || !hasNotificationPolicyAccess()) return
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            if (active) {
                originalDndFilter = notificationManager.currentInterruptionFilter
                // Cambiamos de NONE (Silencio total) a PRIORITY (Prioridad)
                // Esto permite que el audio de medios (TTS) siga funcionando mientras se bloquean notificaciones
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
            } else {
                notificationManager.setInterruptionFilter(originalDndFilter)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val allTranslations: StateFlow<List<TranslationEntity>> = repository.allTranslations.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    private val _selectedTranslationId = dataStoreManager.selectedTranslationId.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "vbl",
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
        
        // Guardar como preferencia
        viewModelScope.launch {
            dataStoreManager.savePreferredTtsSpeed(nextSpeed)
        }
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
        
        // Aplicar velocidad preferida al iniciar
        val speed = preferredTtsSpeed.value
        _playbackSpeed.value = speed
        speechManager.setSpeed(speed)
        
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
        viewModelScope.launch {
            val current = fontSize.value
            val newSize = (current + delta).coerceIn(12f, 40f)
            dataStoreManager.saveFontSize(newSize)
        }
    }

    fun setFontFamily(family: String) {
        viewModelScope.launch {
            dataStoreManager.saveFontFamily(family)
        }
    }

    fun setPreferredTtsSpeed(speed: Float) {
        viewModelScope.launch {
            dataStoreManager.savePreferredTtsSpeed(speed)
        }
    }

    fun setShowTrivia(show: Boolean) {
        viewModelScope.launch {
            dataStoreManager.saveShowTrivia(show)
        }
    }

    fun setShowDevocional(show: Boolean) {
        viewModelScope.launch {
            dataStoreManager.saveShowDevocional(show)
        }
    }

    fun markDevotionalVisited() {
        viewModelScope.launch {
            val today = LocalDate.now().format(dateFormatter)
            dataStoreManager.saveLastDevotionalVisitDate(today)
        }
    }

    fun markDailyVerseVisited() {
        viewModelScope.launch {
            val today = LocalDate.now().format(dateFormatter)
            dataStoreManager.saveLastDailyVerseVisitDate(today)
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            dataStoreManager.clearAllOnboarding()
        }
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

    fun deleteBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            repository.deleteBookmark(bookmark.bookId, bookmark.chapter, bookmark.verse)
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

    fun exportData() {
        viewModelScope.launch {
            val notes = noteRepository.observeAllNotes().first()
            val bookmarks = repository.allBookmarks.first()
            val categories = repository.allCategories.first()
            val progress = repository.getAllProgress().first()
            
            val settings = SettingsBackup(
                isDarkMode = isDarkMode.value,
                fontSize = fontSize.value,
                fontFamily = fontFamily.value,
                selectedTranslationId = selectedTranslationId.value,
                preferredTtsSpeed = preferredTtsSpeed.value,
                showTrivia = showTrivia.value,
                showDevocional = showDevocional.value,
                currentStreak = currentStreak.value,
                lastReadDate = lastReadDate.value,
                isDiscontinuousMode = isDiscontinuousMode.value,
                autoDndOnReading = autoDndOnReading.value,
                firstLaunchDate = dataStoreManager.firstLaunchDate.first(),
                deviceSeed = dataStoreManager.deviceSeed.first()
            )
            
            val backup = BackupData(
                notes = notes,
                bookmarks = bookmarks,
                categories = categories,
                progress = progress,
                settings = settings
            )
            
            val json = Json { prettyPrint = true }.encodeToString(backup)
            _exportEvent.emit(json)
        }
    }

    fun importData(json: String) {
        viewModelScope.launch {
            try {
                val backup = Json { 
                    ignoreUnknownKeys = true 
                    coerceInputValues = true
                }.decodeFromString<BackupData>(json)
                
                // Importar en orden de dependencias
                if (backup.categories.isNotEmpty()) {
                    repository.insertAllCategories(backup.categories)
                }
                if (backup.bookmarks.isNotEmpty()) {
                    repository.insertAllBookmarks(backup.bookmarks)
                }
                if (backup.notes.isNotEmpty()) {
                    noteRepository.insertAllNotes(backup.notes)
                }
                if (backup.progress.isNotEmpty()) {
                    repository.insertAllProgress(backup.progress)
                }
                
                // Restaurar ajustes
                backup.settings?.let { settings ->
                    dataStoreManager.importSettings(settings)
                }
                
                _messageEvent.emit("Importación completada con éxito")
            } catch (e: Exception) {
                e.printStackTrace()
                _messageEvent.emit("Error al importar: ${e.localizedMessage}")
            }
        }
    }
}

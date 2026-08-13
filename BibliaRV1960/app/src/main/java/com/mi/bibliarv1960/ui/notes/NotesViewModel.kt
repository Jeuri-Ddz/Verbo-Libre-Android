package com.mi.bibliarv1960.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mi.bibliarv1960.data.local.entities.NoteEntity
import com.mi.bibliarv1960.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * IA: reemplaza NoteRepository por como se inyecte en el resto del proyecto
 * (Hilt, constructor manual, ServiceLocator, lo que ya estén usando).
 */
class NotesViewModel(
    private val repository: NoteRepository
) : ViewModel() {

    // Claves de versículo con nota, para pintar el indicador en la lista de lectura
    val notedVerseKeys: StateFlow<Set<String>> = repository.observeAllNotedVerseKeys()
        .map { list -> list.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Todas las notas, para la pantalla "Mis notas"
    val allNotes: StateFlow<List<NoteEntity>> = repository.observeAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _editorState = MutableStateFlow<NoteEditorState?>(null)
    val editorState: StateFlow<NoteEditorState?> = _editorState

    /** Abre el editor para un versículo, precargando la nota existente si ya tiene una. */
    fun openEditor(verseKey: String, bookId: Int, chapter: Int, verseNumber: Int) {
        viewModelScope.launch {
            val existing = repository.getNoteForVerse(verseKey)
            _editorState.value = NoteEditorState(
                verseKey = verseKey,
                bookId = bookId,
                chapter = chapter,
                verseNumber = verseNumber,
                text = existing?.text.orEmpty(),
                isNew = existing == null
            )
        }
    }

    fun updateDraftText(text: String) {
        _editorState.value = _editorState.value?.copy(text = text)
    }

    fun saveCurrentNote() {
        val state = _editorState.value ?: return
        if (state.text.isBlank()) {
            deleteCurrentNote()
            return
        }
        viewModelScope.launch {
            repository.saveNote(
                verseKey = state.verseKey,
                bookId = state.bookId,
                chapter = state.chapter,
                verseNumber = state.verseNumber,
                text = state.text
            )
            _editorState.value = null
        }
    }

    fun deleteCurrentNote() {
        val state = _editorState.value ?: return
        viewModelScope.launch {
            repository.deleteNote(state.verseKey)
            _editorState.value = null
        }
    }

    fun closeEditor() {
        _editorState.value = null
    }
}

data class NoteEditorState(
    val verseKey: String,
    val bookId: Int,
    val chapter: Int,
    val verseNumber: Int,
    val text: String,
    val isNew: Boolean
)

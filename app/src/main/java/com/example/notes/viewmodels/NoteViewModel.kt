package com.example.notes.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.data.Note
import com.example.notes.repository.NoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    private val isSortedByDateAdded = MutableStateFlow(true)

    val notes: StateFlow<List<Note>> = isSortedByDateAdded.flatMapLatest { isSorted ->
        if (isSorted) repository.getNotesOrderedByDateAdded()
        else repository.getNotesOrderedByTitle()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedNote = MutableStateFlow<Note?>(null)
    val selectedNote: StateFlow<Note?> = _selectedNote.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState

    fun loadNoteById(id: Int) {
        viewModelScope.launch {
            try {
                _selectedNote.value = repository.getNoteById(id)
            } catch (e: Exception) {
                _errorState.value = "Failed to load note: ${e.message}"
            }
        }
    }

    fun clearSelectedNote() {
        _selectedNote.value = null
    }


    fun upsertNote(title: String, description: String) {
        viewModelScope.launch {
            val note = Note(
                title = title,
                disp = description,
                dateAdded = System.currentTimeMillis()
            )
            repository.upsert(note)
        }
    }

    fun deleteNote() {
        viewModelScope.launch {
            _selectedNote.value?.let { repository.delete(it) }
        }
    }

    fun toggleSortOrder() {
        isSortedByDateAdded.value = !isSortedByDateAdded.value
    }
}

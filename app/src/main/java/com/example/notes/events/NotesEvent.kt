package com.example.notes.events

import com.example.notes.data.Note

sealed interface NotesEvent {
    object SortNotes : NotesEvent
    data class DeleteNote(val note: Note) : NotesEvent
    data class SaveNote(
        val title: String,
        val disp: String
    ) : NotesEvent
    data class UpdateNote(
        val note: Note
    ) : NotesEvent
}

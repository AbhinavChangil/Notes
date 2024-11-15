package com.example.notes.repository

import com.example.notes.data.Note
import com.example.notes.data.NoteDao
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {

    fun getNotesOrderedByDateAdded(): Flow<List<Note>> = noteDao.getOrderedByDateAdded()

    fun getNotesOrderedByTitle(): Flow<List<Note>> = noteDao.getOrderedByTitle()

    suspend fun upsert(note: Note) = noteDao.upsertNote(note)

    suspend fun delete(note: Note) = noteDao.deleteNote(note)

    suspend fun getNoteById(id: Int): Note? = noteDao.getNoteById(id)
}

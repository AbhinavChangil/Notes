package com.example.notes.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM Note ORDER BY dateAdded DESC")
    fun getOrderedByDateAdded(): Flow<List<Note>>

    @Query("SELECT * FROM Note ORDER BY title ASC")
    fun getOrderedByTitle(): Flow<List<Note>>

    // Add this function to get a note by its ID
    @Query("SELECT * FROM Note WHERE id = :noteId")
    suspend fun getNoteById(noteId: Int): Note?

    @Query("SELECT * FROM Note WHERE title LIKE '%' || :query || '%' OR disp LIKE '%' || :query || '%' ORDER BY dateAdded DESC")
    fun getNotesFilteredByQuery(query: String): Flow<List<Note>>

    @Query("SELECT * FROM Note WHERE title LIKE '%' || :query || '%' ORDER BY dateAdded DESC")
    fun getNotesFilteredByQueryAndSortedByDate(query: String): Flow<List<Note>>

    @Query("SELECT * FROM Note WHERE title LIKE '%' || :query || '%' ORDER BY title ASC")
    fun getNotesFilteredByQueryAndSortedByTitle(query: String): Flow<List<Note>>

    @Query("SELECT COUNT(*) FROM Note")
    suspend fun getNoteCount(): Int

}

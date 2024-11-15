import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.data.Note
import com.example.notes.repository.NoteRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    private val isSortedByDateAdded = MutableStateFlow(true)
    private val searchQuery = MutableStateFlow("")

    val notes: StateFlow<List<Note>> = searchQuery.flatMapLatest { query ->
        if (query.isEmpty()) repository.getNotesOrderedByDateAdded()
        else repository.getNotesFilteredByQuery(query) // Add this method in your DAO
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun filterNotes(query: String) {
        searchQuery.value = query
    }

    private val _selectedNote = MutableLiveData<Note?>()
    val selectedNote: LiveData<Note?> get() = _selectedNote

    fun selectNote(note: Note) {
        _selectedNote.value = note
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

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.delete(note)
        }
    }

    fun toggleSortOrder() {
        isSortedByDateAdded.value = !isSortedByDateAdded.value
    }
}

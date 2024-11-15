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

    val notes: StateFlow<List<Note>> = combine(searchQuery, isSortedByDateAdded) { query, isSorted ->
        Pair(query, isSorted)
    }.flatMapLatest { (query, isSorted) ->
        if (query.isEmpty()) {
            if (isSorted) {
                repository.getNotesOrderedByDateAdded()
            }
            else repository.getNotesOrderedByTitle()
        } else {
            if (isSorted) repository.getNotesFilteredByQueryAndSortedByDate(query)
            else repository.getNotesFilteredByQueryAndSortedByTitle(query)
        }
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
    fun textSortBy() : String{
        if(isSortedByDateAdded.value){
            return "Date"
        }
        else{
            return "Title"
        }
    }
}

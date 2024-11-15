package com.example.notes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.notes.R
import com.example.notes.data.NoteDatabase
import com.example.notes.repository.NoteRepository
import com.example.notes.viewmodels.NoteViewModel
import com.example.notes.viewmodels.NoteViewModelFactory

class AddNoteFragment : Fragment() {

    private lateinit var noteViewModel: NoteViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add_note, container, false)

        // Initialize the repository and ViewModel
        val repository = NoteRepository(NoteDatabase.getInstance(requireContext()).noteDao())
        val factory = NoteViewModelFactory(repository)
        noteViewModel = ViewModelProvider(this, factory).get(NoteViewModel::class.java)

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val btnSaveNote = view.findViewById<Button>(R.id.btnSaveNote)

        btnSaveNote.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter both title and description", Toast.LENGTH_SHORT).show()
            } else {
                noteViewModel.upsertNote(title, description)
                Toast.makeText(requireContext(), "Note saved", Toast.LENGTH_SHORT).show()
                navigateBack()
            }
        }

        return view
    }

    private fun navigateBack() {
        parentFragmentManager.popBackStack()
    }
}

package com.example.notes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.notes.databinding.FragmentNoteDetailBinding
import com.example.notes.data.NoteDatabase
import com.example.notes.repository.NoteRepository
import com.example.notes.viewmodels.NoteViewModel
import com.example.notes.viewmodels.NoteViewModelFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private const val ARG_NOTE_ID = "noteId"

class NoteDetailFragment : Fragment() {
    private var noteId: Int? = null
    private var _binding: FragmentNoteDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NoteViewModel by viewModels {
        val noteDao = NoteDatabase.getInstance(requireContext()).noteDao()
        val repository = NoteRepository(noteDao)
        NoteViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            noteId = it.getInt(ARG_NOTE_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load the note details
        loadNoteDetails()

        // Set up the save button listener
        binding.btnEditNote.setOnClickListener {
            saveNote()
        }
    }

    private fun loadNoteDetails() {
        lifecycleScope.launch {
            noteId?.let { id ->
                viewModel.loadNoteById(id)
                viewModel.selectedNote.collect { note ->
                    if (note != null) {
                        binding.etTitle.setText(note.title)
                        binding.etDescription.setText(note.disp)
                    } else {
                        Toast.makeText(requireContext(), "Note not found", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }
                }
            } ?: run {
                Toast.makeText(requireContext(), "Invalid note ID", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun saveNote() {
        val updatedTitle = binding.etTitle.text.toString().trim()
        val updatedDescription = binding.etDescription.text.toString().trim()

        if (updatedTitle.isEmpty() || updatedDescription.isEmpty()) {
            Toast.makeText(requireContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show()
        } else {
            lifecycleScope.launch {
                noteId?.let {
                    val updatedNote = viewModel.selectedNote.value?.copy(
                        title = updatedTitle,
                        disp = updatedDescription
                    )
                    updatedNote?.let { note ->
                        viewModel.upsertNote(note)
                        Toast.makeText(requireContext(), "Note updated successfully", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(noteId: Int) =
            NoteDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_NOTE_ID, noteId)
                }
            }
    }

}

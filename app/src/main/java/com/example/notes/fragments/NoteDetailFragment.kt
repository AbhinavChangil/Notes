package com.example.notes.fragments

import NoteViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.notes.data.NoteDatabase
import com.example.notes.databinding.FragmentNoteDetailBinding
import com.example.notes.repository.NoteRepository
import com.example.notes.viewmodels.NoteViewModelFactory


class NoteDetailFragment : Fragment() {
    private val viewModel: NoteViewModel by activityViewModels {
        val noteDao = NoteDatabase.getInstance(requireContext()).noteDao()
        val repository = NoteRepository(noteDao)
        NoteViewModelFactory(repository)
    }

    private var _binding: FragmentNoteDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe the selected note from the ViewModel
        viewModel.selectedNote.observe(viewLifecycleOwner) { note ->
            if (note != null) {
                binding.etTitle.setText(note.title)
                binding.etDescription.setText(note.disp)
            } else {
                Toast.makeText(requireContext(), "No note selected", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }

        binding.btnEditNote.setOnClickListener {
            saveNote()
        }
    }

    private fun saveNote() {
        val updatedTitle = binding.etTitle.text.toString().trim()
        val updatedDescription = binding.etDescription.text.toString().trim()
        val updatedDate = System.currentTimeMillis()

        if (updatedTitle.isEmpty() || updatedDescription.isEmpty()) {
            Toast.makeText(requireContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.upsertNote(updatedTitle, updatedDescription)
            Toast.makeText(requireContext(), "Note updated successfully", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

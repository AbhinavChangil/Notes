package com.example.notes.fragments

import NoteViewModel
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notes.R
import com.example.notes.adapters.NoteAdapter
import com.example.notes.data.NoteDatabase
import com.example.notes.repository.NoteRepository
import com.example.notes.viewmodels.NoteViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.activityViewModels
import com.example.notes.data.Note
import kotlinx.coroutines.launch

class NoteListFragment : Fragment() {
    private val viewModel: NoteViewModel by activityViewModels {
        val noteDao = NoteDatabase.getInstance(requireContext()).noteDao()
        val repository = NoteRepository(noteDao)
        NoteViewModelFactory(repository)
    }

    private lateinit var noteAdapter: NoteAdapter
    private lateinit var motionLayout: MotionLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_note_list, container, false)

        motionLayout = view.findViewById(R.id.motion_layout)
        setupRecyclerView(view)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notes.collect { notes ->
                noteAdapter.submitList(notes)
            }
        }

        view.findViewById<FloatingActionButton>(R.id.fab_add_note).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddNoteFragment())
                .addToBackStack(null)
                .commit()
        }
        view.findViewById<ImageView>(R.id.ivSort1).setOnClickListener {
            viewModel.toggleSortOrder();
        }
        view.findViewById<ImageView>(R.id.ivSort).setOnClickListener {
            viewModel.toggleSortOrder();
        }

        return view
    }

    private fun setupRecyclerView(view: View) {
        noteAdapter = NoteAdapter(
            onNoteClick = { note ->
                viewModel.selectNote(note)
                navigateToNoteDetailFragment()
            },
            onDeleteClick = { note ->
                deleteNoteConfirmation(note)
            }
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = noteAdapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val totalScrollRange = recyclerView.computeVerticalScrollRange()
                val scrollOffset = recyclerView.computeVerticalScrollOffset()
                val progress = scrollOffset.toFloat() / totalScrollRange.toFloat()

                if (progress >= 0.1f) {
                    if (motionLayout.currentState != R.id.end) {
                        motionLayout.transitionToEnd()
                    }
                } else {
                    if (motionLayout.currentState != R.id.start) {
                        motionLayout.transitionToStart()
                    }
                }
            }
        })
    }

    private fun navigateToNoteDetailFragment() {
        val fragment = NoteDetailFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun deleteNoteConfirmation(note: Note) {
        // Show a confirmation dialog before deleting
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_note, null)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            alertDialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            viewModel.deleteNote(note)
            alertDialog.dismiss()
        }

        alertDialog.show()
    }
}

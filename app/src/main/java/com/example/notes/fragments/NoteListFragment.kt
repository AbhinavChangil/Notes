package com.example.notes.fragments

import NoteViewModel
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
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
import com.google.android.material.search.SearchBar
import kotlinx.coroutines.launch

class NoteListFragment : Fragment() {
    private val viewModel: NoteViewModel by activityViewModels {
        val noteDao = NoteDatabase.getInstance(requireContext()).noteDao()
        val repository = NoteRepository(noteDao)
        NoteViewModelFactory(repository)
    }

    private lateinit var noteAdapter: NoteAdapter
    private lateinit var motionLayout: MotionLayout
    private lateinit var searchView: SearchView
    private lateinit var ivSearch1: ImageView
    private lateinit var ivSearch2: ImageView
    private lateinit var ivSort1: ImageView
    private lateinit var tvAllNotes2: TextView
    private lateinit var tvSortBy: TextView
    private lateinit var tvNotesCount: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_note_list, container, false)

        motionLayout = view.findViewById(R.id.motion_layout)
        searchView = view.findViewById(R.id.search_view)
        ivSearch1 = view.findViewById(R.id.ivSearch1)
        ivSearch2 = view.findViewById(R.id.ivSearch2)
        ivSort1 = view.findViewById(R.id.ivSort1)
        tvAllNotes2 = view.findViewById(R.id.tvAllNotes)
        tvSortBy = view.findViewById(R.id.tvSortBy)
        tvNotesCount = view.findViewById(R.id.tvNoteCount1)



        setupRecyclerView(view)
        setupSearchView()


        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notes.collect { notes ->
                noteAdapter.submitList(notes)
            }
        }

        viewModel.updateNoteCount()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.noteCount.collect { count ->
                tvNotesCount.text = "${count.toString()} notes"
            }
        }



        view.findViewById<FloatingActionButton>(R.id.fab_add_note).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddNoteFragment())
                .addToBackStack(null)
                .commit()
        }

        ivSearch1.setOnClickListener {
            toggleSearchViewVisibility(true)
        }
        ivSearch2.setOnClickListener {
            toggleSearchViewVisibility(true)
        }


        ivSort1.setOnClickListener {
            viewModel.toggleSortOrder()
            tvSortBy.text = viewModel.textSortBy()
        }
        viewModel.updateNoteCount()


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

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    filterNotes(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    filterNotes(newText)
                } else {
                    filterNotes("") // Reset filter if input is cleared
                }
                return true
            }
        })

        searchView.setOnCloseListener {
            toggleSearchViewVisibility(false) // Reset visibility
            filterNotes("") // Reset filter
            true
        }
    }


    private fun filterNotes(query: String) {
        // Use the query to filter notes from the ViewModel
        viewModel.filterNotes(query)
    }

    private fun toggleSearchViewVisibility(show: Boolean) {
        if (show) {
            searchView.visibility = View.VISIBLE
            ivSearch1.visibility = View.GONE
            ivSearch2.visibility = View.GONE
            ivSort1.visibility = View.GONE
            tvSortBy.visibility = View.GONE
        } else {
            searchView.visibility = View.GONE
            ivSearch1.visibility = View.VISIBLE
            ivSearch2.visibility = View.VISIBLE
            ivSort1.visibility = View.VISIBLE
            tvSortBy.visibility = View.VISIBLE
        }
    }

    private fun navigateToNoteDetailFragment() {
        val fragment = NoteDetailFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    @SuppressLint("SetTextI18n")
    private fun deleteNoteConfirmation(note: Note) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_note, null)
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            viewModel.updateNoteCount()
            alertDialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            viewModel.deleteNote(note)

            viewModel.updateNoteCount()

            alertDialog.dismiss()
        }

        alertDialog.show()
    }
}

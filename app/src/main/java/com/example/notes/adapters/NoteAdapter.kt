package com.example.notes.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.notes.R
import com.example.notes.data.Note
import java.text.SimpleDateFormat
import java.util.Locale

class NoteAdapter(
    private val onNoteClick: (Note) -> Unit,
    private val onDeleteClick: (Note) -> Unit
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view, onNoteClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NoteViewHolder(
        itemView: View,
        val onNoteClick: (Note) -> Unit,
        val onDeleteClick: (Note) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tvTitle)
        private val description: TextView = itemView.findViewById(R.id.tvDescription)
        private val dateAdded: TextView = itemView.findViewById(R.id.tvTime)
        private val deleteButton: ImageView = itemView.findViewById(R.id.ivDelete)
        private val itemCard: View = itemView.findViewById(R.id.cardView)

        fun bind(note: Note) {
            title.text = note.title
            description.text = note.disp
            // Format the timestamp
            val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val formattedDate = dateFormatter.format(note.dateAdded)
            dateAdded.text = formattedDate

            // Handle click on the card
            itemCard.setOnClickListener {
                onNoteClick(note)
            }

            // Handle delete button click
            deleteButton.setOnClickListener {
                onDeleteClick(note)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Note, newItem: Note) = oldItem == newItem
    }
}

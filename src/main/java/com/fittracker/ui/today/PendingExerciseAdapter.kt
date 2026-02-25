package com.fittracker.ui.today

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fittracker.data.Exercise
import com.fittracker.databinding.ItemPendingExerciseBinding

/**
 * Shows exercises the user has NOT yet done today.
 * Each item is a large, full-width tappable button — single tap = mark done.
 */
class PendingExerciseAdapter(
    private val onMarkDone: (Long) -> Unit
) : ListAdapter<Exercise, PendingExerciseAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPendingExerciseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class ViewHolder(private val b: ItemPendingExerciseBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(exercise: Exercise) {
            b.textName.text    = exercise.name
            b.textTarget.text  = "${exercise.quantity.display()} ${exercise.unit}"

            // The entire card is the tap target
            b.root.setOnClickListener { onMarkDone(exercise.id) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Exercise>() {
        override fun areItemsTheSame(a: Exercise, b: Exercise) = a.id == b.id
        override fun areContentsTheSame(a: Exercise, b: Exercise) = a == b
    }

    private fun Double.display() =
        if (this == toLong().toDouble()) toLong().toString() else toString()
}

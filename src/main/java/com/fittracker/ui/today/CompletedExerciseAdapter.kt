package com.fittracker.ui.today

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fittracker.data.Exercise
import com.fittracker.databinding.ItemCompletedExerciseBinding

/**
 * Compact list of exercises already marked done today.
 * Shows a checkmark and an undo button in case of accidental tap.
 */
class CompletedExerciseAdapter(
    private val onUndo: (Long) -> Unit
) : ListAdapter<Exercise, CompletedExerciseAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCompletedExerciseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class ViewHolder(private val b: ItemCompletedExerciseBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(exercise: Exercise) {
            b.textName.text   = "✓  ${exercise.name}"
            b.textTarget.text = "${exercise.quantity.display()} ${exercise.unit}"
            b.btnUndo.setOnClickListener { onUndo(exercise.id) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Exercise>() {
        override fun areItemsTheSame(a: Exercise, b: Exercise) = a.id == b.id
        override fun areContentsTheSame(a: Exercise, b: Exercise) = a == b
    }

    private fun Double.display() =
        if (this == toLong().toDouble()) toLong().toString() else toString()
}

package com.fittracker.ui.today

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fittracker.R
import com.fittracker.databinding.ItemTodayExerciseBinding

class TodayExerciseAdapter(
    private val onToggle: (Long) -> Unit
) : ListAdapter<ExerciseWithCompletion, TodayExerciseAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTodayExerciseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class ViewHolder(private val b: ItemTodayExerciseBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(item: ExerciseWithCompletion) {
            val ex = item.exercise
            b.textName.text = ex.name
            b.textTarget.text = "Target: ${ex.quantity.display()} ${ex.unit}"

            if (item.isCompleted) {
                b.btnToggle.text = "✓  Done"
                b.btnToggle.setBackgroundColor(
                    ContextCompat.getColor(b.root.context, R.color.completed_green)
                )
                b.root.alpha = 0.7f
            } else {
                b.btnToggle.text = "Mark Done"
                b.btnToggle.setBackgroundColor(
                    ContextCompat.getColor(b.root.context, R.color.colorPrimary)
                )
                b.root.alpha = 1.0f
            }

            b.btnToggle.setOnClickListener { onToggle(ex.id) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ExerciseWithCompletion>() {
        override fun areItemsTheSame(a: ExerciseWithCompletion, b: ExerciseWithCompletion) =
            a.exercise.id == b.exercise.id
        override fun areContentsTheSame(a: ExerciseWithCompletion, b: ExerciseWithCompletion) =
            a == b
    }

    /** Format quantity: show as integer when it's a whole number (50 not 50.0) */
    private fun Double.display() =
        if (this == toLong().toDouble()) toLong().toString() else toString()
}

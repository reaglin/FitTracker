package com.fittracker.ui.today

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.fittracker.databinding.FragmentTodayBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class TodayFragment : Fragment() {

    private var _binding: FragmentTodayBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TodayViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Date header
        val fmt = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault())
        binding.textDate.text = LocalDate.now().format(fmt)

        // ── Adapters ──────────────────────────────────────────────────────

        val pendingAdapter = PendingExerciseAdapter { exerciseId ->
            viewModel.markDone(exerciseId)
        }
        binding.recyclerPending.adapter = pendingAdapter

        val completedAdapter = CompletedExerciseAdapter { exerciseId ->
            viewModel.unmarkDone(exerciseId)
        }
        binding.recyclerCompleted.adapter = completedAdapter

        // ── Observe pending exercises ─────────────────────────────────────

        viewModel.pendingExercises.observe(viewLifecycleOwner) { pending ->
            pendingAdapter.submitList(pending)

            val hasPending = pending.isNotEmpty()
            binding.recyclerPending.visibility  = if (hasPending) View.VISIBLE else View.GONE
            binding.textNoPending.visibility    = if (hasPending) View.GONE   else View.VISIBLE
        }

        // ── Observe completed exercises ────────────────────────────────────

        viewModel.completedExercises.observe(viewLifecycleOwner) { completed ->
            completedAdapter.submitList(completed)

            val hasCompleted = completed.isNotEmpty()
            binding.sectionCompleted.visibility  = if (hasCompleted) View.VISIBLE else View.GONE
            binding.recyclerCompleted.visibility = if (hasCompleted) View.VISIBLE else View.GONE
        }

        // ── Empty state (no exercises defined at all) ──────────────────────

        viewModel.totalExerciseCount.observe(viewLifecycleOwner) { total ->
            binding.textNoExercises.visibility = if (total == 0) View.VISIBLE else View.GONE
        }

        // ── Progress bar & status ──────────────────────────────────────────

        fun updateProgress() {
            val completed = viewModel.completedCount.value  ?: 0
            val goal      = viewModel.dailyGoal.value       ?: 5

            binding.progressBar.max      = goal
            binding.progressBar.progress = minOf(completed, goal)
            binding.textProgress.text    = "$completed / $goal completed today"

            binding.textGoalStatus.text = when {
                completed >= goal -> "🎉 Daily goal of $goal reached!"
                completed == 0    -> "Tap an exercise below to mark it done"
                else              -> "${goal - completed} more to reach your goal of $goal"
            }

            binding.textGoalStatus.setTextColor(
                if (completed >= goal)
                    requireContext().getColor(com.fittracker.R.color.completed_green)
                else
                    requireContext().getColor(com.fittracker.R.color.text_secondary)
            )
        }

        viewModel.completedCount.observe(viewLifecycleOwner) { updateProgress() }
        viewModel.dailyGoal.observe(viewLifecycleOwner)      { updateProgress() }

        // ── Goal chip — tap to change goal ────────────────────────────────

        binding.chipGoal.setOnClickListener { showGoalDialog() }
        viewModel.dailyGoal.observe(viewLifecycleOwner) { goal ->
            binding.chipGoal.text = "Goal: $goal"
        }
    }

    /** Number picker dialog to set the daily exercise goal */
    private fun showGoalDialog() {
        val picker = NumberPicker(requireContext()).apply {
            minValue    = 1
            maxValue    = 20
            value       = viewModel.dailyGoal.value ?: 5
            wrapSelectorWheel = false
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Set Daily Goal")
            .setMessage("How many exercises do you want to complete each day?")
            .setView(picker)
            .setPositiveButton("Save") { _, _ -> viewModel.setDailyGoal(picker.value) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

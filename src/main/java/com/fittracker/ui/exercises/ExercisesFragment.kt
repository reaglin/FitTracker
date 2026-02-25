package com.fittracker.ui.exercises

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.fittracker.R
import com.fittracker.data.Exercise
import com.fittracker.databinding.FragmentExercisesBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ExercisesFragment : Fragment() {

    private var _binding: FragmentExercisesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExercisesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExercisesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ExerciseListAdapter(
            onEdit = { showExerciseDialog(it) },
            onDelete = { confirmDelete(it) }
        )
        binding.recyclerExercises.adapter = adapter

        viewModel.allExercises.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.textEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabAdd.setOnClickListener { showExerciseDialog(null) }
    }

    /** Shared dialog for both Add (exercise == null) and Edit */
    private fun showExerciseDialog(exercise: Exercise?) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_exercise, null)
        val etName     = dialogView.findViewById<EditText>(R.id.etName)
        val etQuantity = dialogView.findViewById<EditText>(R.id.etQuantity)
        val etUnit     = dialogView.findViewById<EditText>(R.id.etUnit)

        if (exercise != null) {
            etName.setText(exercise.name)
            etQuantity.setText(exercise.quantity.display())
            etUnit.setText(exercise.unit)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (exercise == null) "Add Exercise" else "Edit Exercise")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                val qty  = etQuantity.text.toString().trim().toDoubleOrNull() ?: return@setPositiveButton
                val unit = etUnit.text.toString().trim()
                if (name.isEmpty() || unit.isEmpty()) return@setPositiveButton

                if (exercise == null) {
                    viewModel.addExercise(name, qty, unit)
                } else {
                    viewModel.updateExercise(exercise.copy(name = name, quantity = qty, unit = unit))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDelete(exercise: Exercise) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Exercise")
            .setMessage("Delete \"${exercise.name}\"?\nAll completion history for this exercise will also be removed.")
            .setPositiveButton("Delete") { _, _ -> viewModel.deleteExercise(exercise) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun Double.display() =
        if (this == toLong().toDouble()) toLong().toString() else toString()
}

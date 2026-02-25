package com.fittracker.ui.history

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.fittracker.data.Exercise
import com.fittracker.databinding.FragmentHistoryBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels()

    private val displayFmt = DateTimeFormatter.ofPattern("MMM d, yyyy")
    private val isoFmt     = DateTimeFormatter.ISO_LOCAL_DATE
    private val shortFmt   = DateTimeFormatter.ofPattern("M/d")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChart()

        // ── Date picker button ─────────────────────────────────────────────
        binding.btnPickDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Pick a date")
                .build()
            picker.addOnPositiveButtonClickListener { millis ->
                val date = Instant.ofEpochMilli(millis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                viewModel.setSelectedDate(date.format(isoFmt))
            }
            picker.show(parentFragmentManager, "date_picker")
        }

        // ── Selected date label + completion list ──────────────────────────
        viewModel.selectedDate.observe(viewLifecycleOwner) { dateStr ->
            val date = LocalDate.parse(dateStr, isoFmt)
            binding.textSelectedDate.text = date.format(displayFmt)
        }

        viewModel.completionsForDate.observe(viewLifecycleOwner) { exercises ->
            binding.textCompletedList.text = if (exercises.isEmpty()) {
                "No exercises completed on this date."
            } else {
                exercises.joinToString("\n") { ex ->
                    "• ${ex.name}  (${ex.quantity.display()} ${ex.unit})"
                }
            }
        }

        // ── Summary stats ──────────────────────────────────────────────────
        viewModel.totalCompletions.observe(viewLifecycleOwner) { total ->
            updateSummary()
        }
        viewModel.totalActiveDays.observe(viewLifecycleOwner) { _ -> updateSummary() }

        // ── Per-exercise frequency table ───────────────────────────────────
        viewModel.exerciseStats.observe(viewLifecycleOwner) { stats ->
            binding.textExerciseStats.text = if (stats.isEmpty()) {
                "No data yet."
            } else {
                stats.joinToString("\n") { s ->
                    "%-20s  %d times".format(s.exercise.name, s.totalCompletions)
                }
            }
        }

        // ── Bar chart ─────────────────────────────────────────────────────
        viewModel.last30DayCounts.observe(viewLifecycleOwner) { counts ->
            updateChart(counts.takeLast(30))
        }
    }

    private fun updateSummary() {
        val total = viewModel.totalCompletions.value ?: 0
        val days  = viewModel.totalActiveDays.value  ?: 0
        val avg   = if (days > 0) String.format("%.1f", total.toFloat() / days) else "—"
        binding.textSummaryStats.text =
            "Total completions: $total   •   Active days: $days   •   Avg per day: $avg"
    }

    private fun setupChart() {
        binding.barChart.apply {
            description.isEnabled = false
            legend.isEnabled      = false
            setDrawGridBackground(false)
            axisRight.isEnabled   = false
            axisLeft.apply {
                granularity      = 1f
                setDrawGridLines(true)
            }
            xAxis.apply {
                position         = XAxis.XAxisPosition.BOTTOM
                granularity      = 1f
                setDrawGridLines(false)
                labelRotationAngle = -45f
            }
        }
    }

    private fun updateChart(counts: List<com.fittracker.data.DailyCount>) {
        if (counts.isEmpty()) return

        val labels  = mutableListOf<String>()
        val entries = mutableListOf<BarEntry>()

        counts.forEachIndexed { i, dc ->
            val date = LocalDate.parse(dc.date, isoFmt)
            labels.add(date.format(shortFmt))
            entries.add(BarEntry(i.toFloat(), dc.count.toFloat()))
        }

        val dataSet = BarDataSet(entries, "Completions").apply {
            color       = Color.parseColor("#4CAF50")
            valueTextColor = Color.DKGRAY
            valueTextSize  = 9f
        }

        binding.barChart.apply {
            data = BarData(dataSet)
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelCount = minOf(labels.size, 10)
            animateY(600)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun Double.display() =
        if (this == toLong().toDouble()) toLong().toString() else toString()
}

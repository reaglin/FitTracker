package com.fittracker.ui.history

import android.app.Application
import androidx.lifecycle.*
import com.fittracker.data.AppDatabase
import com.fittracker.data.DailyCount
import com.fittracker.data.Exercise
import com.fittracker.data.ExerciseRepository
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ExerciseWithCount(
    val exercise: Exercise,
    val totalCompletions: Int
)

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repo = ExerciseRepository(db.exerciseDao(), db.completionDao())

    // ── Selected date (default = today) ───────────────────────────────────

    private val _selectedDate = MutableLiveData(
        LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    )
    val selectedDate: LiveData<String> = _selectedDate

    fun setSelectedDate(date: String) { _selectedDate.value = date }

    // ── Exercises completed on the selected date ───────────────────────────

    val completionsForDate: LiveData<List<Exercise>> =
        selectedDate.switchMap { date ->
            repo.allExercises
                .combine(repo.getCompletionsForDate(date)) { exercises, completions ->
                    val ids = completions.map { it.exerciseId }.toSet()
                    exercises.filter { it.id in ids }
                }
                .asLiveData()
        }

    // ── Per-exercise total completion counts ──────────────────────────────

    val exerciseStats: LiveData<List<ExerciseWithCount>> =
        repo.allExercises
            .combine(repo.getCountsPerExercise()) { exercises, counts ->
                val countMap = counts.associate { it.exerciseId to it.count }
                exercises.map { ex ->
                    ExerciseWithCount(ex, countMap[ex.id] ?: 0)
                }.sortedByDescending { it.totalCompletions }
            }
            .asLiveData()

    // ── Summary stats ──────────────────────────────────────────────────────

    val totalCompletions: LiveData<Int> = repo.getTotalCompletions().asLiveData()
    val totalActiveDays:  LiveData<Int> = repo.getTotalActiveDays().asLiveData()

    // ── Bar-chart data: last 30 days with daily completion counts ──────────

    val last30DayCounts: LiveData<List<DailyCount>> =
        repo.getDailyCompletionCounts().asLiveData()
}

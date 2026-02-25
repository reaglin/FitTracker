package com.fittracker.ui.today

import android.app.Application
import androidx.lifecycle.*
import com.fittracker.data.AppDatabase
import com.fittracker.data.Exercise
import com.fittracker.data.ExerciseRepository
import com.fittracker.data.UserPreferences
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ExerciseWithCompletion(
    val exercise: Exercise,
    val isCompleted: Boolean
)

class TodayViewModel(application: Application) : AndroidViewModel(application) {

    private val db    = AppDatabase.getDatabase(application)
    private val repo  = ExerciseRepository(db.exerciseDao(), db.completionDao())
    private val prefs = UserPreferences(application)

    val today: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    // ── Daily goal (wraps SharedPreferences in LiveData so UI reacts) ──────

    private val _dailyGoal = MutableLiveData(prefs.dailyGoal)
    val dailyGoal: LiveData<Int> = _dailyGoal

    fun setDailyGoal(goal: Int) {
        prefs.dailyGoal = goal
        _dailyGoal.value = goal
    }

    // ── All exercises paired with today's completion status ─────────────────

    private val allWithCompletion: LiveData<List<ExerciseWithCompletion>> =
        repo.allExercises
            .combine(repo.getCompletionsForDate(today)) { exercises, completions ->
                val completedIds = completions.map { it.exerciseId }.toSet()
                exercises.map { ExerciseWithCompletion(it, it.id in completedIds) }
            }
            .asLiveData()

    /** Exercises NOT yet marked done today — shown as action buttons */
    val pendingExercises: LiveData<List<Exercise>> =
        allWithCompletion.map { list -> list.filter { !it.isCompleted }.map { it.exercise } }

    /** Exercises marked done today — shown in the "Completed" section */
    val completedExercises: LiveData<List<Exercise>> =
        allWithCompletion.map { list -> list.filter { it.isCompleted }.map { it.exercise } }

    /** Count of completed exercises today */
    val completedCount: LiveData<Int> =
        allWithCompletion.map { list -> list.count { it.isCompleted } }

    /** Total number of exercises the user has defined */
    val totalExerciseCount: LiveData<Int> =
        allWithCompletion.map { it.size }

    /** True when the user has hit their daily goal */
    val goalReached: LiveData<Boolean> =
        completedCount.switchMap { count ->
            dailyGoal.map { goal -> count >= goal }
        }

    // ── Actions ─────────────────────────────────────────────────────────────

    /** One-tap: mark an exercise done from the pending list */
    fun markDone(exerciseId: Long) = viewModelScope.launch {
        repo.toggleCompletion(exerciseId, today)
    }

    /** Unmark a completed exercise from the completed section */
    fun unmarkDone(exerciseId: Long) = viewModelScope.launch {
        repo.toggleCompletion(exerciseId, today)
    }
}

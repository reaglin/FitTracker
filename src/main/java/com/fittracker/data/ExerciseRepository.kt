package com.fittracker.data

import kotlinx.coroutines.flow.Flow

class ExerciseRepository(
    private val exerciseDao: ExerciseDao,
    private val completionDao: CompletionDao
) {

    // ── Exercises ──────────────────────────────────────────────────────────

    val allExercises: Flow<List<Exercise>> = exerciseDao.getAllExercises()

    suspend fun insertExercise(exercise: Exercise) = exerciseDao.insert(exercise)
    suspend fun updateExercise(exercise: Exercise) = exerciseDao.update(exercise)
    suspend fun deleteExercise(exercise: Exercise) = exerciseDao.delete(exercise)

    // ── Completions ────────────────────────────────────────────────────────

    fun getCompletionsForDate(date: String) = completionDao.getCompletionsForDate(date)
    fun getCountForDate(date: String) = completionDao.getCountForDate(date)
    fun getDailyCompletionCounts() = completionDao.getDailyCompletionCounts()
    fun getAllDatesWithCompletions() = completionDao.getAllDatesWithCompletions()
    fun getTotalForExercise(id: Long) = completionDao.getTotalForExercise(id)
    fun getCountsPerExercise() = completionDao.getCountsPerExercise()
    fun getTotalActiveDays() = completionDao.getTotalActiveDays()
    fun getTotalCompletions() = completionDao.getTotalCompletions()

    /** Toggles completion for an exercise on a given date (tap to mark / unmark) */
    suspend fun toggleCompletion(exerciseId: Long, date: String) {
        val existing = completionDao.getForExerciseAndDate(exerciseId, date)
        if (existing != null) {
            completionDao.delete(existing)
        } else {
            completionDao.insert(Completion(exerciseId = exerciseId, date = date))
        }
    }
}

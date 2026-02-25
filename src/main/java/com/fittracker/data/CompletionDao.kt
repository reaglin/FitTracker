package com.fittracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletionDao {

    // ── Today / specific-date queries ──────────────────────────────────────

    @Query("SELECT * FROM completions WHERE date = :date")
    fun getCompletionsForDate(date: String): Flow<List<Completion>>

    @Query("SELECT COUNT(*) FROM completions WHERE date = :date")
    fun getCountForDate(date: String): Flow<Int>

    @Query("""
        SELECT * FROM completions 
        WHERE exerciseId = :exerciseId AND date = :date 
        LIMIT 1
    """)
    suspend fun getForExerciseAndDate(exerciseId: Long, date: String): Completion?

    // ── Per-exercise stats ─────────────────────────────────────────────────

    @Query("SELECT COUNT(*) FROM completions WHERE exerciseId = :exerciseId")
    fun getTotalForExercise(exerciseId: Long): Flow<Int>

    @Query("SELECT exerciseId, COUNT(*) as count FROM completions GROUP BY exerciseId")
    fun getCountsPerExercise(): Flow<List<ExerciseCount>>

    // ── History / calendar ─────────────────────────────────────────────────

    @Query("""
        SELECT date, COUNT(*) as count 
        FROM completions 
        GROUP BY date 
        ORDER BY date DESC
    """)
    fun getDailyCompletionCounts(): Flow<List<DailyCount>>

    @Query("SELECT DISTINCT date FROM completions ORDER BY date DESC")
    fun getAllDatesWithCompletions(): Flow<List<String>>

    @Query("SELECT COUNT(DISTINCT date) FROM completions")
    fun getTotalActiveDays(): Flow<Int>

    @Query("SELECT COUNT(*) FROM completions")
    fun getTotalCompletions(): Flow<Int>

    // ── Write operations ───────────────────────────────────────────────────

    @Insert
    suspend fun insert(completion: Completion)

    @Delete
    suspend fun delete(completion: Completion)
}

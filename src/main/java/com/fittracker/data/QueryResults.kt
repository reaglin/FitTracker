package com.fittracker.data

/** Returned by "completions per day" aggregate query */
data class DailyCount(
    val date: String,
    val count: Int
)

/** Returned by "completions per exercise" aggregate query */
data class ExerciseCount(
    val exerciseId: Long,
    val count: Int
)

package com.fittracker.ui.exercises

import android.app.Application
import androidx.lifecycle.*
import com.fittracker.data.AppDatabase
import com.fittracker.data.Exercise
import com.fittracker.data.ExerciseRepository
import kotlinx.coroutines.launch

class ExercisesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repo = ExerciseRepository(db.exerciseDao(), db.completionDao())

    val allExercises: LiveData<List<Exercise>> = repo.allExercises.asLiveData()

    fun addExercise(name: String, quantity: Double, unit: String) = viewModelScope.launch {
        repo.insertExercise(Exercise(name = name, quantity = quantity, unit = unit))
    }

    fun updateExercise(exercise: Exercise) = viewModelScope.launch {
        repo.updateExercise(exercise)
    }

    fun deleteExercise(exercise: Exercise) = viewModelScope.launch {
        repo.deleteExercise(exercise)
    }
}

package com.example.snail.data

import android.content.Context
import com.example.snail.ui.models.toExerciseTitle

object ExerciseNameMigration {
    fun apply(context: Context) {
        val preferences = context.getSharedPreferences("snail_migrations", Context.MODE_PRIVATE)
        if (preferences.getBoolean("exercise_title_names_v1", false)) return

        val exercises = ExerciseStorage.load(context)
        val updatedExercises = exercises.map { it.copy(name = it.name.toExerciseTitle()) }
        if (updatedExercises != exercises) ExerciseStorage.save(context, updatedExercises)

        val routines = RoutineStorage.load(context)
        val updatedRoutines = routines.map { routine ->
            routine.copy(exercises = routine.exercises.map {
                it.copy(name = it.name.toExerciseTitle())
            })
        }
        if (updatedRoutines != routines) RoutineStorage.save(context, updatedRoutines)

        val workouts = WorkoutStorage.load(context)
        val updatedWorkouts = workouts.map { workout ->
            workout.copy(exercises = workout.exercises.map {
                it.copy(name = it.name.toExerciseTitle())
            })
        }
        if (updatedWorkouts != workouts) WorkoutStorage.save(context, updatedWorkouts)

        preferences.edit().putBoolean("exercise_title_names_v1", true).apply()
    }
}

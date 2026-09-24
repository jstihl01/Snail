package com.example.snail.data

import android.content.Context
import com.example.snail.ui.models.CompletedExercise
import com.example.snail.ui.models.CompletedSet
import com.example.snail.ui.models.SavedWorkout
import com.example.snail.ui.models.normalizedRoutineColorIndex
import com.example.snail.ui.models.DefaultEndMotivation
import com.example.snail.ui.models.DefaultStartMotivation
import com.example.snail.ui.models.WorkoutMotivations
import org.json.JSONArray
import org.json.JSONObject

object WorkoutStorage {
    private const val PreferencesName = "snail_workouts"
    private const val WorkoutsKey = "workouts"

    fun load(context: Context): List<SavedWorkout> {
        val serialized = context
            .getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
            .getString(WorkoutsKey, null)
            ?: return emptyList()

        var needsMigration = false
        val loaded = runCatching {
            val workoutsJson = JSONArray(serialized)
            List(workoutsJson.length()) { workoutIndex ->
                val workoutJson = workoutsJson.getJSONObject(workoutIndex)
                val exercisesJson = workoutJson.getJSONArray("exercises")
                val storedColorIndex = workoutJson.optInt("colorIndex", 0)
                val colorIndex = normalizedRoutineColorIndex(storedColorIndex)
                if (storedColorIndex != colorIndex) needsMigration = true
                val storedStartMotivation = workoutJson.optString("startMotivation")
                val storedEndMotivation = workoutJson.optString("endMotivation")
                val startMotivation = storedStartMotivation
                    .takeIf { it in WorkoutMotivations } ?: DefaultStartMotivation
                val endMotivation = storedEndMotivation
                    .takeIf { it in WorkoutMotivations } ?: DefaultEndMotivation
                if (storedStartMotivation != startMotivation || storedEndMotivation != endMotivation) {
                    needsMigration = true
                }
                SavedWorkout(
                    id = workoutJson.getLong("id"),
                    routineName = workoutJson.getString("routineName"),
                    completedAt = workoutJson.optLong("completedAt", 0L),
                    colorIndex = colorIndex,
                    routineId = if (workoutJson.has("routineId") &&
                        !workoutJson.isNull("routineId")) {
                        workoutJson.getLong("routineId")
                    } else null,
                    startMotivation = startMotivation,
                    endMotivation = endMotivation,
                    exercises = List(exercisesJson.length()) { exerciseIndex ->
                        val exerciseJson = exercisesJson.getJSONObject(exerciseIndex)
                        val setsJson = exerciseJson.getJSONArray("sets")
                        CompletedExercise(
                            name = exerciseJson.getString("name"),
                            sets = List(setsJson.length()) { setIndex ->
                                val setJson = setsJson.getJSONObject(setIndex)
                                CompletedSet(
                                    number = setJson.getInt("number"),
                                    kilograms = setJson.getString("kilograms"),
                                    repetitions = setJson.getString("repetitions"),
                                    rir = setJson.optString("rir")
                                )
                            }
                        )
                    }
                )
            }
        }.getOrDefault(emptyList())
        if (needsMigration) save(context, loaded)
        return loaded
    }

    fun save(context: Context, workouts: List<SavedWorkout>) {
        val workoutsJson = JSONArray()
        workouts.forEach { workout ->
            val exercisesJson = JSONArray()
            workout.exercises.forEach { exercise ->
                val setsJson = JSONArray()
                exercise.sets.forEach { set ->
                    setsJson.put(
                        JSONObject()
                            .put("number", set.number)
                            .put("kilograms", set.kilograms)
                            .put("repetitions", set.repetitions)
                            .put("rir", set.rir)
                    )
                }
                exercisesJson.put(
                    JSONObject()
                        .put("name", exercise.name)
                        .put("sets", setsJson)
                )
            }
            workoutsJson.put(
                JSONObject()
                    .put("id", workout.id)
                    .put("routineName", workout.routineName)
                    .put("completedAt", workout.completedAt)
                    .put("colorIndex", workout.colorIndex)
                    .put("routineId", workout.routineId ?: JSONObject.NULL)
                    .put("startMotivation", workout.startMotivation ?: JSONObject.NULL)
                    .put("endMotivation", workout.endMotivation ?: JSONObject.NULL)
                    .put("exercises", exercisesJson)
            )
        }

        context
            .getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
            .edit()
            .putString(WorkoutsKey, workoutsJson.toString())
            .apply()
    }
}

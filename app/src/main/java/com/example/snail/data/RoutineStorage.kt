package com.example.snail.data

import android.content.Context
import com.example.snail.ui.models.RoutineExercise
import com.example.snail.ui.models.SavedRoutine
import org.json.JSONArray
import org.json.JSONObject

object RoutineStorage {
    private const val PreferencesName = "snail_routines"
    private const val RoutinesKey = "routines"

    fun load(context: Context): List<SavedRoutine> {
        val serialized = context
            .getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
            .getString(RoutinesKey, null)
            ?: return emptyList()

        return runCatching {
            val routinesJson = JSONArray(serialized)
            List(routinesJson.length()) { routineIndex ->
                val routineJson = routinesJson.getJSONObject(routineIndex)
                val exercisesJson = routineJson.getJSONArray("exercises")
                SavedRoutine(
                    id = routineJson.getLong("id"),
                    name = routineJson.getString("name"),
                    colorIndex = routineJson.optInt("colorIndex", 0),
                    exercises = List(exercisesJson.length()) { exerciseIndex ->
                        val exerciseJson = exercisesJson.getJSONObject(exerciseIndex)
                        RoutineExercise(
                            id = exerciseJson.getLong("id"),
                            name = exerciseJson.getString("name"),
                            series = exerciseJson.getString("series"),
                            repetitions = exerciseJson.getString("repetitions"),
                            rir = exerciseJson.getString("rir")
                        )
                    }
                )
            }
        }.getOrDefault(emptyList())
    }

    fun save(context: Context, routines: List<SavedRoutine>) {
        val routinesJson = JSONArray()
        routines.forEach { routine ->
            val exercisesJson = JSONArray()
            routine.exercises.forEach { exercise ->
                exercisesJson.put(
                    JSONObject()
                        .put("id", exercise.id)
                        .put("name", exercise.name)
                        .put("series", exercise.series)
                        .put("repetitions", exercise.repetitions)
                        .put("rir", exercise.rir)
                )
            }
            routinesJson.put(
                JSONObject()
                    .put("id", routine.id)
                    .put("name", routine.name)
                    .put("colorIndex", routine.colorIndex)
                    .put("exercises", exercisesJson)
            )
        }

        context
            .getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
            .edit()
            .putString(RoutinesKey, routinesJson.toString())
            .apply()
    }
}

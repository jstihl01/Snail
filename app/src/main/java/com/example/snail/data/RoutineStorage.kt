package com.example.snail.data

import android.content.Context
import com.example.snail.ui.models.RoutineExercise
import com.example.snail.ui.models.SavedRoutine
import com.example.snail.ui.models.splitRepetitionRange
import com.example.snail.ui.models.normalizedRoutineColorIndex
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

        var needsMigration = false
        val loaded = runCatching {
            val routinesJson = JSONArray(serialized)
            List(routinesJson.length()) { routineIndex ->
                val routineJson = routinesJson.getJSONObject(routineIndex)
                val exercisesJson = routineJson.getJSONArray("exercises")
                val storedColorIndex = routineJson.optInt("colorIndex", 0)
                val colorIndex = normalizedRoutineColorIndex(storedColorIndex)
                if (storedColorIndex != colorIndex) needsMigration = true
                SavedRoutine(
                    id = routineJson.getLong("id"),
                    name = routineJson.getString("name"),
                    colorIndex = colorIndex,
                    exercises = List(exercisesJson.length()) { exerciseIndex ->
                        val exerciseJson = exercisesJson.getJSONObject(exerciseIndex)
                        val legacyRange = splitRepetitionRange(exerciseJson.optString("repetitions", ""))
                        if (!exerciseJson.has("minRepetitions")) needsMigration = true
                        RoutineExercise(
                            id = exerciseJson.getLong("id"),
                            name = exerciseJson.getString("name"),
                            series = exerciseJson.getString("series"),
                            minRepetitions = exerciseJson.optString("minRepetitions", legacyRange.first),
                            maxRepetitions = exerciseJson.optString("maxRepetitions", legacyRange.second),
                            rir = exerciseJson.getString("rir")
                        )
                    }
                )
            }
        }.getOrNull() ?: return emptyList()
        if (needsMigration) save(context, loaded)
        return loaded
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
                        .put("minRepetitions", exercise.minRepetitions)
                        .put("maxRepetitions", exercise.maxRepetitions)
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

package com.example.snail.data

import android.content.Context
import com.example.snail.ui.screens.ExerciseItem
import org.json.JSONArray
import org.json.JSONObject

object ExerciseStorage {
    private const val PreferencesName = "snail_exercises"
    private const val ExercisesKey = "exercises"

    fun load(context: Context): List<ExerciseItem> {
        val serialized = context
            .getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
            .getString(ExercisesKey, null)
            ?: return emptyList()

        return runCatching {
            val exercisesJson = JSONArray(serialized)
            List(exercisesJson.length()) { index ->
                val exerciseJson = exercisesJson.getJSONObject(index)
                ExerciseItem(
                    id = exerciseJson.getLong("id"),
                    name = exerciseJson.getString("name")
                )
            }
        }.getOrDefault(emptyList())
    }

    fun save(context: Context, exercises: List<ExerciseItem>) {
        val exercisesJson = JSONArray()
        exercises.forEach { exercise ->
            exercisesJson.put(
                JSONObject()
                    .put("id", exercise.id)
                    .put("name", exercise.name)
            )
        }

        context
            .getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
            .edit()
            .putString(ExercisesKey, exercisesJson.toString())
            .apply()
    }
}

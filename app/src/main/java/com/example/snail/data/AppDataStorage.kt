package com.example.snail.data

import android.content.Context
import com.example.snail.ui.models.*
import com.example.snail.ui.screens.ExerciseItem
import org.json.JSONArray
import org.json.JSONObject

data class AppData(
    val exercises: List<ExerciseItem> = emptyList(),
    val routines: List<SavedRoutine> = emptyList(),
    val workouts: List<SavedWorkout> = emptyList(),
    val folders: List<RoutineFolder> = emptyList()
) {
    fun nextId(): Long = sequenceOf(
        exercises.map { it.id }, routines.map { it.id }, workouts.map { it.id }, folders.map { it.id }
    ).flatten().maxOrNull()?.plus(1) ?: 1L
}

enum class ImportCategory(val label: String) {
    EXERCISES("Ejercicios"), ROUTINES("Rutinas y carpetas"), WORKOUTS("Entrenamientos e historial")
}

object AppDataStorage {
    fun load(context: Context): AppData {
        val prefs = context.getSharedPreferences("snail_data", Context.MODE_PRIVATE)
        val saved = prefs.getString("snapshot", null)
        if (saved != null) return DataJson.decode(saved)
        // Legacy stores remain untouched as a recovery copy.
        val migrateNames = !context.getSharedPreferences("snail_migrations", Context.MODE_PRIVATE)
            .getBoolean("exercise_title_names_v1", false)
        fun name(value: String) = if (migrateNames) value.toExerciseTitle() else value
        val exercises = ExerciseStorage.load(context, strict = true).filter { it.name.isNotBlank() }
            .map { it.copy(name = name(it.name), confirmed = true) }
        val routines = RoutineStorage.load(context, strict = true, migrate = false).map { routine ->
            routine.copy(exercises = routine.exercises.map { it.copy(name = name(it.name)) })
        }
        val workouts = WorkoutStorage.load(context, strict = true, migrate = false).map { workout ->
            workout.copy(routineId = workout.routineId ?: routines.singleOrNull { it.name == workout.routineName }?.id,
                exercises = workout.exercises.map { it.copy(name = name(it.name)) })
        }
        return AppData(exercises, routines, workouts)
            .also { save(context, it) }
    }

    fun save(context: Context, data: AppData) {
        check(context.getSharedPreferences("snail_data", Context.MODE_PRIVATE).edit()
            .putString("snapshot", DataJson.encode(data)).commit()) { "No se pudieron guardar los datos" }
    }
}

object DataJson {
    private fun array(values: List<JSONObject>) = JSONArray().apply { values.forEach { put(it) } }
    private fun JSONArray.objects() = List(length()) { getJSONObject(it) }
    private fun JSONObject.idOrNull(key: String) = if (isNull(key)) null else getLong(key)
    private fun JSONObject.name() = getString("name").also { require(it.isNotBlank()) { "Nombre vacío" } }
    private fun JSONObject.metadata(note: String, group: String, cardio: Boolean, source: Long?) =
        put("note", note).put("muscleGroup", group).put("isCardio", cardio)
            .put("sourceExerciseId", source ?: JSONObject.NULL)

    fun encode(data: AppData): String = JSONObject().put("format", "snail-backup").put("version", 1)
        .put("exercises", array(data.exercises.filter { it.confirmed && it.name.isNotBlank() }.map {
            JSONObject().put("id", it.id).put("name", it.name)
                .metadata(it.note, it.muscleGroup, it.isCardio, null)
        }))
        .put("folders", array(data.folders.map { JSONObject().put("id", it.id).put("name", it.name) }))
        .put("routines", array(data.routines.map { routine ->
            JSONObject().put("id", routine.id).put("name", routine.name).put("colorIndex", routine.colorIndex)
                .put("folderId", routine.folderId ?: JSONObject.NULL)
                .put("exercises", array(routine.exercises.map {
                    JSONObject().put("id", it.id).put("name", it.name).put("series", it.series)
                        .put("minRepetitions", it.minRepetitions).put("maxRepetitions", it.maxRepetitions)
                        .put("rir", it.rir).put("intensity", it.intensity).put("minutes", it.minutes)
                        .metadata(it.note, it.muscleGroup, it.isCardio, it.sourceExerciseId)
                }))
        }))
        .put("workouts", array(data.workouts.map { workout ->
            JSONObject().put("id", workout.id).put("routineName", workout.routineName)
                .put("completedAt", workout.completedAt).put("colorIndex", workout.colorIndex)
                .put("routineId", workout.routineId ?: JSONObject.NULL)
                .put("startMotivation", workout.startMotivation).put("endMotivation", workout.endMotivation)
                .put("exercises", array(workout.exercises.map { exercise ->
                    JSONObject().put("name", exercise.name)
                        .metadata(exercise.note, exercise.muscleGroup, exercise.isCardio, exercise.sourceExerciseId)
                        .put("targetRepetitions", exercise.targetRepetitions).put("targetRir", exercise.targetRir)
                        .put("sets", array(exercise.sets.map {
                            JSONObject().put("number", it.number).put("kilograms", it.kilograms)
                                .put("repetitions", it.repetitions).put("rir", it.rir)
                                .put("intensity", it.intensity).put("minutes", it.minutes).put("isExtra", it.isExtra)
                        }))
                }))
        })).toString(2)

    fun decode(text: String): AppData {
        val root = JSONObject(text)
        require(root.getString("format") == "snail-backup" && root.getInt("version") == 1) {
            "Formato de copia no compatible"
        }
        val data = AppData(
            exercises = root.getJSONArray("exercises").objects().map {
                ExerciseItem(it.getLong("id"), it.name(), confirmed = true, note = it.optString("note"),
                    muscleGroup = it.optString("muscleGroup"), isCardio = it.optBoolean("isCardio"))
            },
            folders = root.getJSONArray("folders").objects().map { RoutineFolder(it.getLong("id"), it.name()) },
            routines = root.getJSONArray("routines").objects().map { routine ->
                SavedRoutine(routine.getLong("id"), routine.name(),
                    routine.getJSONArray("exercises").objects().map {
                        RoutineExercise(it.getLong("id"), it.name(), it.getString("series"),
                            it.optString("minRepetitions"), it.optString("maxRepetitions"), it.optString("rir"),
                            it.optString("note"), it.optString("muscleGroup"), it.optBoolean("isCardio"),
                            it.optString("intensity"), it.optString("minutes"), it.idOrNull("sourceExerciseId"))
                    }, normalizedRoutineColorIndex(routine.optInt("colorIndex")), routine.idOrNull("folderId"))
            },
            workouts = root.getJSONArray("workouts").objects().map { workout ->
                SavedWorkout(workout.getLong("id"), workout.getString("routineName"), workout.getLong("completedAt"),
                    workout.getJSONArray("exercises").objects().map { exercise ->
                        CompletedExercise(exercise.name(), exercise.getJSONArray("sets").objects().map {
                            CompletedSet(it.getInt("number"), it.optString("kilograms"), it.optString("repetitions"),
                                it.optString("rir"), it.optString("intensity"), it.optString("minutes"), it.optBoolean("isExtra"))
                        }, exercise.optString("note"), exercise.optString("muscleGroup"), exercise.optBoolean("isCardio"),
                            exercise.optString("targetRepetitions"), exercise.optString("targetRir"), exercise.idOrNull("sourceExerciseId"))
                    }, normalizedRoutineColorIndex(workout.optInt("colorIndex")), workout.idOrNull("routineId"),
                    workout.optString("startMotivation").takeIf { it in WorkoutMotivations } ?: DefaultStartMotivation,
                    workout.optString("endMotivation").takeIf { it in WorkoutMotivations } ?: DefaultEndMotivation)
            }
        )
        listOf(data.exercises.map { it.id }, data.routines.map { it.id }, data.workouts.map { it.id }, data.folders.map { it.id })
            .forEach { ids -> require(ids.distinct().size == ids.size && ids.all { it >= 0 && it < Long.MAX_VALUE }) { "Identificadores inválidos" } }
        data.routines.forEach { routine ->
            require(routine.folderId == null || data.folders.any { it.id == routine.folderId }) { "Carpeta inexistente" }
            require(routine.exercises.map { it.id }.distinct().size == routine.exercises.size) { "Ejercicios duplicados" }
        }
        data.workouts.forEach { workout -> workout.exercises.forEach {
            require(it.sets.map { set -> set.number }.distinct().size == it.sets.size && it.sets.all { set -> set.number > 0 }) { "Series inválidas" }
        } }
        return data
    }
}

/** Imports append snapshots and remap links so colliding ids never alter existing records. */
fun AppData.importData(incoming: AppData, categories: Set<ImportCategory>): AppData {
    var next = nextId()
    val exerciseIds = if (ImportCategory.EXERCISES in categories) incoming.exercises.associate { it.id to next++ } else emptyMap()
    val folderIds = if (ImportCategory.ROUTINES in categories) incoming.folders.associate { it.id to next++ } else emptyMap()
    val routineIds = if (ImportCategory.ROUTINES in categories) incoming.routines.associate { it.id to next++ } else emptyMap()
    return copy(
        exercises = exercises + incoming.exercises.filter { it.id in exerciseIds }.map { it.copy(id = exerciseIds.getValue(it.id)) },
        folders = folders + incoming.folders.filter { it.id in folderIds }.map { it.copy(id = folderIds.getValue(it.id)) },
        routines = routines + incoming.routines.filter { it.id in routineIds }.map { routine ->
            routine.copy(id = routineIds.getValue(routine.id), folderId = folderIds[routine.folderId],
                exercises = routine.exercises.map { it.copy(sourceExerciseId = exerciseIds[it.sourceExerciseId]) })
        },
        workouts = (workouts + if (ImportCategory.WORKOUTS in categories) incoming.workouts.map { workout ->
            workout.copy(id = next++, routineId = routineIds[workout.routineId],
                exercises = workout.exercises.map { it.copy(sourceExerciseId = exerciseIds[it.sourceExerciseId]) })
        } else emptyList()).sortedBy { it.completedAt }
    )
}

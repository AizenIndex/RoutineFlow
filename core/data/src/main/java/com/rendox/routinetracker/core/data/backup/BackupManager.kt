package com.rendox.routinetracker.core.data.backup

import android.content.Context
import android.net.Uri
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.model.habit.HabitType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

object BackupManager {

    suspend fun exportBackup(
        context: Context,
        database: RoutineTrackerDatabase,
        destinationUri: Uri,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject()
            root.put("version", 1)
            root.put("appName", "RoutineFlow")
            root.put("exportTimestamp", System.currentTimeMillis())

            // 1. Export Habits
            val habitsArray = JSONArray()
            val habits = database.habitEntityQueries.getAllHabits().executeAsList()
            for (h in habits) {
                val obj = JSONObject()
                obj.put("id", h.id)
                obj.put("type", h.type.name)
                obj.put("name", h.name)
                obj.put("description", h.description ?: JSONObject.NULL)
                obj.put("sessionDurationMinutes", h.sessionDurationMinutes ?: JSONObject.NULL)
                obj.put("progress", h.progress?.toDouble() ?: JSONObject.NULL)
                obj.put("defaultCompletionTimeHour", h.defaultCompletionTimeHour ?: JSONObject.NULL)
                obj.put("defaultCompletionTimeMinute", h.defaultCompletionTimeMinute ?: JSONObject.NULL)
                habitsArray.put(obj)
            }
            root.put("habits", habitsArray)

            // 2. Export Completion History
            val completionsArray = JSONArray()
            val completions = database.completionHistoryEntityQueries.getAll().executeAsList()
            for (c in completions) {
                val obj = JSONObject()
                obj.put("habitId", c.habitId)
                obj.put("date", c.date.toString())
                obj.put("numOfTimesCompleted", c.numOfTimesCompleted.toDouble())
                completionsArray.put(obj)
            }
            root.put("completions", completionsArray)

            // Write JSON to destination stream
            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                outputStream.write(root.toString(2).toByteArray(Charsets.UTF_8))
                outputStream.flush()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importBackup(
        context: Context,
        database: RoutineTrackerDatabase,
        sourceUri: Uri,
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).readText()
            } ?: return@withContext Result.failure(Exception("Failed to open backup file"))

            val root = JSONObject(jsonString)
            val habitsArray = root.optJSONArray("habits") ?: JSONArray()
            val completionsArray = root.optJSONArray("completions") ?: JSONArray()

            var restoredCount = 0

            database.transaction {
                for (i in 0 until habitsArray.length()) {
                    val obj = habitsArray.getJSONObject(i)
                    val id = obj.getLong("id")
                    val name = obj.getString("name")
                    val desc = if (obj.isNull("description")) null else obj.getString("description")
                    val duration = if (obj.isNull("sessionDurationMinutes")) null else obj.getInt("sessionDurationMinutes")
                    val progress = if (obj.isNull("progress")) null else obj.getDouble("progress").toFloat()
                    val hour = if (obj.isNull("defaultCompletionTimeHour")) null else obj.getInt("defaultCompletionTimeHour")
                    val min = if (obj.isNull("defaultCompletionTimeMinute")) null else obj.getInt("defaultCompletionTimeMinute")

                    val existing = database.habitEntityQueries.getHabitById(id).executeAsOneOrNull()
                    if (existing == null) {
                        database.habitEntityQueries.insertHabit(
                            id = id,
                            type = HabitType.YesNoHabit,
                            name = name,
                            description = desc,
                            sessionDurationMinutes = duration,
                            progress = progress,
                            defaultCompletionTimeHour = hour,
                            defaultCompletionTimeMinute = min,
                        )
                    } else {
                        database.habitEntityQueries.updateHabitNameAndDescription(
                            name = name,
                            description = desc,
                            id = id,
                        )
                    }
                    restoredCount++
                }

                for (i in 0 until completionsArray.length()) {
                    val obj = completionsArray.getJSONObject(i)
                    val habitId = obj.getLong("habitId")
                    val dateStr = obj.getString("date")
                    val count = obj.getDouble("numOfTimesCompleted").toFloat()
                    val localDate = kotlinx.datetime.LocalDate.parse(dateStr)

                    database.completionHistoryEntityQueries.insertCompletion(
                        habitId = habitId,
                        date = localDate,
                        numOfTimesCompleted = count,
                    )
                }
            }

            Result.success(restoredCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

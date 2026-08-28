package com.rendox.routinetracker.core.data.backup

import android.content.Context
import android.net.Uri
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.database.model.habit.HabitType
import com.rendox.routinetracker.core.database.model.schedule.ScheduleType
import com.rendox.routinetracker.core.model.WeekDayNumberMonthRelated
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
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
            root.put("version", 2)
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

            // 2. Export Schedules
            val schedulesArray = JSONArray()
            val schedules = database.scheduleEntityQueries.getAll().executeAsList()
            for (s in schedules) {
                val obj = JSONObject()
                obj.put("id", s.id)
                obj.put("type", s.type.name)
                obj.put("startDate", s.startDate.toString())
                obj.put("endDate", s.endDate?.toString() ?: JSONObject.NULL)
                obj.put("backlogEnabled", s.backlogEnabled)
                obj.put("cancelDuenessIfDoneAhead", s.cancelDuenessIfDoneAhead)
                obj.put("startDayOfWeekInWeeklySchedule", s.startDayOfWeekInWeeklySchedule?.name ?: JSONObject.NULL)
                obj.put("startFromHabitStartInMonthlyAndAnnualSchedule", s.startFromHabitStartInMonthlyAndAnnualSchedule ?: JSONObject.NULL)
                obj.put("includeLastDayOfMonthInMonthlySchedule", s.includeLastDayOfMonthInMonthlySchedule ?: JSONObject.NULL)
                obj.put("periodicSeparationEnabledInPeriodicSchedule", s.periodicSeparationEnabledInPeriodicSchedule ?: JSONObject.NULL)
                obj.put("numOfDueDaysInByNumOfDueDaysSchedule", s.numOfDueDaysInByNumOfDueDaysSchedule ?: JSONObject.NULL)
                obj.put("numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule", s.numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule ?: JSONObject.NULL)
                obj.put("numOfDaysInAlternateDaysSchedule", s.numOfDaysInAlternateDaysSchedule ?: JSONObject.NULL)
                schedulesArray.put(obj)
            }
            root.put("schedules", schedulesArray)

            // 3. Export Due Dates
            val dueDatesArray = JSONArray()
            val dueDates = database.dueDateEntityQueries.getAll().executeAsList()
            for (d in dueDates) {
                val obj = JSONObject()
                obj.put("scheduleId", d.scheduleId)
                obj.put("dueDateNumber", d.dueDateNumber)
                obj.put("completionTimeHour", d.completionTimeHour ?: JSONObject.NULL)
                obj.put("completionTimeMinute", d.completionTimeMinute ?: JSONObject.NULL)
                dueDatesArray.put(obj)
            }
            root.put("dueDates", dueDatesArray)

            // 4. Export WeekDayMonthRelated
            val weekDayMonthArray = JSONArray()
            val weekDayMonthList = database.weekDayMonthRelatedEntityQueries.getAll().executeAsList()
            for (w in weekDayMonthList) {
                val obj = JSONObject()
                obj.put("id", w.id)
                obj.put("scheduleId", w.scheduleId)
                obj.put("weekDayIndex", w.weekDayIndex)
                obj.put("weekDayNumberMonthRelated", w.weekDayNumberMonthRelated.name)
                weekDayMonthArray.put(obj)
            }
            root.put("weekDayMonthRelated", weekDayMonthArray)

            // 5. Export Vacations
            val vacationsArray = JSONArray()
            val vacations = database.vacationEntityQueries.getAll().executeAsList()
            for (v in vacations) {
                val obj = JSONObject()
                obj.put("habitId", v.habitId)
                obj.put("startDate", v.startDate.toString())
                obj.put("endDate", v.endDate?.toString() ?: JSONObject.NULL)
                vacationsArray.put(obj)
            }
            root.put("vacations", vacationsArray)

            // 6. Export Completion History
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
            val schedulesArray = root.optJSONArray("schedules") ?: JSONArray()
            val dueDatesArray = root.optJSONArray("dueDates") ?: JSONArray()
            val weekDayMonthArray = root.optJSONArray("weekDayMonthRelated") ?: JSONArray()
            val vacationsArray = root.optJSONArray("vacations") ?: JSONArray()
            val completionsArray = root.optJSONArray("completions") ?: JSONArray()

            var restoredCount = 0

            database.transaction {
                // 1. Insert Habits
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

                // 2. Insert Schedules
                for (i in 0 until schedulesArray.length()) {
                    val obj = schedulesArray.getJSONObject(i)
                    val id = obj.getLong("id")
                    val type = try { ScheduleType.valueOf(obj.getString("type")) } catch (_: Exception) { ScheduleType.EveryDaySchedule }
                    val startDate = LocalDate.parse(obj.getString("startDate"))
                    val endDate = if (obj.isNull("endDate")) null else LocalDate.parse(obj.getString("endDate"))
                    val backlogEnabled = obj.optBoolean("backlogEnabled", false)
                    val cancelDueness = obj.optBoolean("cancelDuenessIfDoneAhead", false)
                    val startDayOfWeek = if (obj.isNull("startDayOfWeekInWeeklySchedule")) null else try { DayOfWeek.valueOf(obj.getString("startDayOfWeekInWeeklySchedule")) } catch (_: Exception) { null }
                    val startFromHabitStart = if (obj.isNull("startFromHabitStartInMonthlyAndAnnualSchedule")) null else obj.getBoolean("startFromHabitStartInMonthlyAndAnnualSchedule")
                    val includeLastDay = if (obj.isNull("includeLastDayOfMonthInMonthlySchedule")) null else obj.getBoolean("includeLastDayOfMonthInMonthlySchedule")
                    val periodicSeparation = if (obj.isNull("periodicSeparationEnabledInPeriodicSchedule")) null else obj.getBoolean("periodicSeparationEnabledInPeriodicSchedule")
                    val numOfDueDays = if (obj.isNull("numOfDueDaysInByNumOfDueDaysSchedule")) null else obj.getInt("numOfDueDaysInByNumOfDueDaysSchedule")
                    val numOfDueDaysFirst = if (obj.isNull("numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule")) null else obj.getInt("numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule")
                    val numOfDaysAlternate = if (obj.isNull("numOfDaysInAlternateDaysSchedule")) null else obj.getInt("numOfDaysInAlternateDaysSchedule")

                    database.scheduleEntityQueries.insertSchedule(
                        id = id,
                        type = type,
                        startDate = startDate,
                        endDate = endDate,
                        backlogEnabled = backlogEnabled,
                        cancelDuenessIfDoneAhead = cancelDueness,
                        startDayOfWeekInWeeklySchedule = startDayOfWeek,
                        startFromHabitStartInMonthlyAndAnnualSchedule = startFromHabitStart,
                        includeLastDayOfMonthInMonthlySchedule = includeLastDay,
                        periodicSeparationEnabledInPeriodicSchedule = periodicSeparation,
                        numOfDueDaysInByNumOfDueDaysSchedule = numOfDueDays,
                        numOfDueDaysInFirstPeriodInByNumOfDueDaysSchedule = numOfDueDaysFirst,
                        numOfDaysInAlternateDaysSchedule = numOfDaysAlternate,
                    )
                }

                // 3. Insert Due Dates
                for (i in 0 until dueDatesArray.length()) {
                    val obj = dueDatesArray.getJSONObject(i)
                    val scheduleId = obj.getLong("scheduleId")
                    val dueDateNumber = obj.getInt("dueDateNumber")
                    val hour = if (obj.isNull("completionTimeHour")) null else obj.getInt("completionTimeHour")
                    val min = if (obj.isNull("completionTimeMinute")) null else obj.getInt("completionTimeMinute")

                    database.dueDateEntityQueries.insertDueDate(
                        scheduleId = scheduleId,
                        dueDateNumber = dueDateNumber,
                        completionTimeHour = hour,
                        completionTimeMinute = min,
                    )
                }

                // 4. Insert WeekDayMonthRelated
                for (i in 0 until weekDayMonthArray.length()) {
                    val obj = weekDayMonthArray.getJSONObject(i)
                    val id = obj.getLong("id")
                    val scheduleId = obj.getLong("scheduleId")
                    val weekDayIndex = obj.getInt("weekDayIndex")
                    val type = try { WeekDayNumberMonthRelated.valueOf(obj.getString("weekDayNumberMonthRelated")) } catch (_: Exception) { WeekDayNumberMonthRelated.First }

                    database.weekDayMonthRelatedEntityQueries.insertWeekDayMonthRelatedEntry(
                        id = id,
                        scheduleId = scheduleId,
                        weekDayIndex = weekDayIndex,
                        weekDayNumberMonthRelated = type,
                    )
                }

                // 5. Insert Vacations
                for (i in 0 until vacationsArray.length()) {
                    val obj = vacationsArray.getJSONObject(i)
                    val habitId = obj.getLong("habitId")
                    val startDate = LocalDate.parse(obj.getString("startDate"))
                    val endDate = if (obj.isNull("endDate")) null else LocalDate.parse(obj.getString("endDate"))

                    database.vacationEntityQueries.insertVacation(
                        habitId = habitId,
                        startDate = startDate,
                        endDate = endDate,
                    )
                }

                // 6. Insert Completions
                for (i in 0 until completionsArray.length()) {
                    val obj = completionsArray.getJSONObject(i)
                    val habitId = obj.getLong("habitId")
                    val dateStr = obj.getString("date")
                    val count = obj.getDouble("numOfTimesCompleted").toFloat()
                    val localDate = LocalDate.parse(dateStr)

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

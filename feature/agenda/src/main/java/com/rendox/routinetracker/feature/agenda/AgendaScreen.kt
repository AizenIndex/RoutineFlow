package com.rendox.routinetracker.feature.agenda

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rendox.routinetracker.core.domain.completionhistory.InsertHabitCompletionUseCase.IllegalDateEditAttemptException
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.ui.components.CompletionCelebration
import com.rendox.routinetracker.core.ui.components.SettingsScreen
import com.rendox.routinetracker.core.ui.helpers.HapticsHelper
import com.rendox.routinetracker.core.ui.helpers.LocalLocale
import com.rendox.routinetracker.core.ui.helpers.ObserveUiEvent
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun AgendaRoute(
    modifier: Modifier = Modifier,
    onRoutineClick: (Long) -> Unit,
    onAddRoutineClick: () -> Unit,
    viewModel: AgendaScreenViewModel = koinViewModel(),
) {
    val currentDate by viewModel.currentDateFlow.collectAsStateWithLifecycle()
    val visibleRoutines by viewModel.visibleRoutinesFlow.collectAsStateWithLifecycle()
    val showAllRoutines by viewModel.showAllRoutinesFlow.collectAsStateWithLifecycle()
    val completionAttemptBlockedEvent by viewModel.completionAttemptBlockedEvent.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage = when (completionAttemptBlockedEvent?.data) {
        is IllegalDateEditAttemptException.NotStartedHabitDateEditAttemptException -> {
            stringResource(
                id = com.rendox.routinetracker.core.ui.R.string.not_started_date_completion_attempt_snackbar_message,
            )
        }

        is IllegalDateEditAttemptException.FinishedHabitDateEditAttemptException -> {
            stringResource(
                id = com.rendox.routinetracker.core.ui.R.string.finished_date_completion_attempt_snackbar_message,
            )
        }

        is IllegalDateEditAttemptException.FutureDateEditAttemptException -> {
            stringResource(
                id = com.rendox.routinetracker.core.ui.R.string.future_date_completion_attempt_snackbar_message,
            )
        }

        null -> ""
    }

    ObserveUiEvent(completionAttemptBlockedEvent) {
        snackbarHostState.showSnackbar(message = snackbarMessage)
    }

    AgendaScreen(
        modifier = modifier,
        currentDate = currentDate.toJavaLocalDate(),
        routineList = visibleRoutines,
        today = LocalDate.now(),
        onAddRoutineClick = onAddRoutineClick,
        onRoutineClick = onRoutineClick,
        insertCompletion = { routineId, completionRecord ->
            viewModel.onRoutineComplete(routineId, completionRecord)
        },
        deleteHabits = { habitIds ->
            viewModel.deleteHabits(habitIds)
        },
        onDateChange = { viewModel.onDateChange(it.toKotlinLocalDate()) },
        onNotDueRoutinesVisibilityToggle = {
            viewModel.onNotDueRoutinesVisibilityToggle()
        },
        showAllRoutines = showAllRoutines,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
internal fun AgendaScreen(
    modifier: Modifier = Modifier,
    currentDate: LocalDate,
    routineList: List<DisplayRoutine>?,
    today: LocalDate,
    showAllRoutines: Boolean,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAddRoutineClick: () -> Unit,
    onRoutineClick: (Long) -> Unit,
    insertCompletion: (Long, Habit.CompletionRecord) -> Unit,
    deleteHabits: (Set<Long>) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onNotDueRoutinesVisibilityToggle: () -> Unit,
) {
    val context = LocalContext.current
    val locale = LocalLocale.current
    var celebrationTriggered by remember { mutableStateOf(false) }
    var showSettingsScreen by remember { mutableStateOf(false) }

    // Multi-Selection State
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedRoutineIds by remember { mutableStateOf(setOf<Long>()) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showSettingsScreen) {
        SettingsScreen(
            onBackClick = { showSettingsScreen = false },
        )
        return
    }

    val totalRoutines = routineList?.size ?: 0
    val completedRoutines = routineList?.count { it.numOfTimesCompleted > 0f } ?: 0
    val progress = if (totalRoutines > 0) completedRoutines.toFloat() / totalRoutines.toFloat() else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600),
        label = "appBarProgress",
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = onAddRoutineClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(id = R.string.fab_icon_description),
                    )
                }
            }
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, MMM d", locale) }
            val formattedDate = remember(currentDate, locale) {
                if (currentDate == today) "Today, " + currentDate.format(DateTimeFormatter.ofPattern("MMM d", locale))
                else currentDate.format(dateFormatter)
            }

            val currentHour = remember { LocalTime.now().hour }
            val greeting = remember(currentHour, currentDate, today) {
                if (currentDate == today) {
                    when (currentHour) {
                        in 5..11 -> "Good morning"
                        in 12..16 -> "Good afternoon"
                        in 17..21 -> "Good evening"
                        else -> "Quiet night"
                    }
                } else {
                    "Agenda for"
                }
            }

            // Top App Bar
            AgendaTopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(78.dp),
                greeting = greeting,
                title = formattedDate,
                totalRoutines = totalRoutines,
                completedRoutines = completedRoutines,
                progress = animatedProgress,
                showAllRoutines = showAllRoutines,
                isSelectionMode = isSelectionMode,
                selectedCount = selectedRoutineIds.size,
                onNotDueRoutinesVisibilityToggle = onNotDueRoutinesVisibilityToggle,
                onSettingsClick = { showSettingsScreen = true },
                onCancelSelection = {
                    isSelectionMode = false
                    selectedRoutineIds = emptySet()
                },
                onSelectAll = {
                    routineList?.map { it.id }?.let { allIds ->
                        selectedRoutineIds = allIds.toSet()
                    }
                },
                onDeleteSelected = {
                    if (selectedRoutineIds.isNotEmpty()) {
                        showDeleteConfirmDialog = true
                    }
                },
            )

            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                Spacer(
                    modifier = Modifier
                        .height(78.dp)
                        .systemBarsPadding(),
                )

                val weekCalendarHeight = 70.dp
                RoutineTrackerWeekCalendar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 8.dp)
                        .height(weekCalendarHeight),
                    selectedDate = currentDate,
                    initialDate = today,
                    dateOnClick = onDateChange,
                    today = today,
                )

                if (routineList?.isNotEmpty() == true) {
                    val onStatusCheckmarkClick: (DisplayRoutine) -> Unit = { routine ->
                        when (routine.type) {
                            DisplayRoutineType.YesNoHabit -> {
                                val isCompleting = routine.numOfTimesCompleted == 0F
                                val numOfTimesCompleted =
                                    if (isCompleting) 1F else 0F

                                // Trigger tactile mechanical haptic feedback
                                HapticsHelper.performClick(context)

                                if (isCompleting) {
                                    celebrationTriggered = true
                                    HapticsHelper.performCelebration(context)
                                }
                                val completion = Habit.YesNoHabit.CompletionRecord(
                                    date = currentDate.toKotlinLocalDate(),
                                    numOfTimesCompleted = numOfTimesCompleted,
                                )
                                insertCompletion(routine.id, completion)
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        PureAgendaList(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            routineList = routineList,
                            isSelectionMode = isSelectionMode,
                            selectedRoutineIds = selectedRoutineIds,
                            onRoutineClick = onRoutineClick,
                            onRoutineLongClick = { routineId ->
                                HapticsHelper.performClick(context)
                                isSelectionMode = true
                                selectedRoutineIds = if (selectedRoutineIds.contains(routineId)) {
                                    selectedRoutineIds - routineId
                                } else {
                                    selectedRoutineIds + routineId
                                }
                                if (selectedRoutineIds.isEmpty()) {
                                    isSelectionMode = false
                                }
                            },
                            onStatusCheckmarkClick = onStatusCheckmarkClick,
                        )
                        Spacer(modifier = Modifier.height(84.dp))
                    }
                }

                if (routineList?.isEmpty() == true) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        val smallTopAppBarHeight = 78.dp
                        NothingScheduled(
                            modifier = Modifier.padding(
                                bottom = when (LocalConfiguration.current.orientation) {
                                    Configuration.ORIENTATION_LANDSCAPE -> 0.dp
                                    else -> smallTopAppBarHeight + weekCalendarHeight
                                },
                            ),
                        )
                    }
                }
            }

            CompletionCelebration(
                isTriggered = celebrationTriggered,
                onAnimationEnd = { celebrationTriggered = false },
            )

            if (showDeleteConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = false },
                    title = { Text(text = "Delete Habits?") },
                    text = {
                        Text(text = "Are you sure you want to permanently delete ${selectedRoutineIds.size} selected habit(s) and all their streak history?")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                deleteHabits(selectedRoutineIds)
                                showDeleteConfirmDialog = false
                                isSelectionMode = false
                                selectedRoutineIds = emptySet()
                            },
                        ) {
                            Text(text = "Delete", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmDialog = false }) {
                            Text(text = "Cancel")
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun AgendaTopAppBar(
    modifier: Modifier = Modifier,
    greeting: String,
    title: String,
    totalRoutines: Int,
    completedRoutines: Int,
    progress: Float,
    showAllRoutines: Boolean,
    isSelectionMode: Boolean,
    selectedCount: Int,
    onNotDueRoutinesVisibilityToggle: () -> Unit,
    onSettingsClick: () -> Unit,
    onCancelSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            if (isSelectionMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onCancelSelection) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel selection",
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$selectedCount selected",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onSelectAll) {
                            Text(text = "Select All")
                        }
                        IconButton(onClick = onDeleteSelected) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            // Stylized Brand Emblem & Typography Header
                            Surface(
                                modifier = Modifier.size(24.dp),
                                shape = RoundedCornerShape(7.dp),
                                color = MaterialTheme.colorScheme.primary,
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(15.dp),
                                    )
                                }
                            }

                            Text(
                                text = "RoutineFlow",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp,
                                ),
                                color = MaterialTheme.colorScheme.primary,
                            )

                            if (totalRoutines > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 7.dp, vertical = 2.dp),
                                ) {
                                    Text(
                                        text = "$completedRoutines/$totalRoutines",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }
                            }
                        }

                        Text(
                            text = "$greeting • $title",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Surface(
                            modifier = Modifier
                                .clip(CircleShape)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                    CircleShape,
                                ),
                            shape = CircleShape,
                            color = if (showAllRoutines) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                        ) {
                            IconButton(
                                onClick = onNotDueRoutinesVisibilityToggle,
                                modifier = Modifier.size(42.dp),
                            ) {
                                if (showAllRoutines) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_visibility_on_24),
                                        contentDescription = stringResource(
                                            id = R.string.routine_visibility_icon_toggle_all_visible_description,
                                        ),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_visibility_off_24),
                                        contentDescription = stringResource(
                                            id = R.string.routine_visibility_icon_toggle_some_routines_hidden_description,
                                        ),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .clip(CircleShape)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                    CircleShape,
                                ),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                        ) {
                            IconButton(
                                onClick = onSettingsClick,
                                modifier = Modifier.size(42.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }

                if (totalRoutines > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.5.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        strokeCap = StrokeCap.Round,
                    )
                }
            }
        }
    }
}

@Composable
fun NothingScheduled(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                RoundedCornerShape(24.dp),
            ),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    modifier = Modifier.size(30.dp),
                    painter = painterResource(id = R.drawable.empty_calendar_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(id = R.string.nothing_scheduled),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Take a break or add a habit with the + button below.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
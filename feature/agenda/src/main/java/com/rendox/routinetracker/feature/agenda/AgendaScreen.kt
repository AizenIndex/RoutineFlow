package com.rendox.routinetracker.feature.agenda

import android.content.res.Configuration
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
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rendox.routinetracker.core.domain.completionhistory.InsertHabitCompletionUseCase.IllegalDateEditAttemptException
import com.rendox.routinetracker.core.model.Habit
import com.rendox.routinetracker.core.ui.components.CompletionCelebration
import com.rendox.routinetracker.core.ui.components.SettingsDialog
import com.rendox.routinetracker.core.ui.helpers.HapticsHelper
import com.rendox.routinetracker.core.ui.helpers.LocalLocale
import com.rendox.routinetracker.core.ui.helpers.ObserveUiEvent
import com.rendox.routinetracker.feature.agenda.databinding.AgendaRecyclerviewBinding
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
    onDateChange: (LocalDate) -> Unit,
    onNotDueRoutinesVisibilityToggle: () -> Unit,
) {
    val locale = LocalLocale.current
    var celebrationTriggered by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
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
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            val dateFormatter =
                remember { DateTimeFormatter.ofPattern("EEEE, MMM d", locale) }
            val formattedDate = remember(currentDate, locale) {
                if (currentDate == today) "Today, " + currentDate.format(DateTimeFormatter.ofPattern("MMM d", locale))
                else currentDate.format(dateFormatter)
            }

            AgendaTopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
                title = formattedDate,
                showAllRoutines = showAllRoutines,
                onNotDueRoutinesVisibilityToggle = onNotDueRoutinesVisibilityToggle,
                onSettingsClick = { showSettingsDialog = true },
            )

            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                Spacer(
                    modifier = Modifier
                        .height(68.dp)
                        .systemBarsPadding(),
                )

                // Claude-style Greeting & Daily Overview Card
                ClaudeAgendaHeroCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    currentDate = currentDate,
                    today = today,
                    routineList = routineList,
                )

                val weekCalendarHeight = 70.dp
                RoutineTrackerWeekCalendar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 6.dp)
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
                                if (isCompleting) {
                                    celebrationTriggered = true
                                }
                                val completion = Habit.YesNoHabit.CompletionRecord(
                                    date = currentDate.toKotlinLocalDate(),
                                    numOfTimesCompleted = numOfTimesCompleted,
                                )
                                insertCompletion(routine.id, completion)
                            }
                        }
                    }
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        AgendaList(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            routineList = routineList,
                            onRoutineClick = onRoutineClick,
                            onStatusCheckmarkClick = onStatusCheckmarkClick,
                        )
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }

                if (routineList?.isEmpty() == true) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        val smallTopAppBarHeight = 68.dp
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

            if (showSettingsDialog) {
                SettingsDialog(
                    onDismissRequest = { showSettingsDialog = false },
                )
            }
        }
    }
}

@Composable
private fun ClaudeAgendaHeroCard(
    modifier: Modifier = Modifier,
    currentDate: LocalDate,
    today: LocalDate,
    routineList: List<DisplayRoutine>?,
) {
    val totalRoutines = routineList?.size ?: 0
    val completedRoutines = routineList?.count { it.numOfTimesCompleted > 0f } ?: 0
    val progress = if (totalRoutines > 0) completedRoutines.toFloat() / totalRoutines.toFloat() else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600),
        label = "heroProgress",
    )

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
            "Schedule for"
        }
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                RoundedCornerShape(20.dp),
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp,
                        ),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = if (totalRoutines == 0) "No habits scheduled"
                        else if (completedRoutines == totalRoutines) "All habits completed! 🎉"
                        else "$completedRoutines of $totalRoutines habits completed",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                if (totalRoutines > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }

            if (totalRoutines > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round,
                )
            }
        }
    }
}

@Composable
private fun AgendaTopAppBar(
    modifier: Modifier = Modifier,
    title: String,
    showAllRoutines: Boolean,
    onNotDueRoutinesVisibilityToggle: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "RoutineFlow",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                ),
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
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
                    modifier = Modifier.size(38.dp),
                ) {
                    if (showAllRoutines) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_visibility_on_24),
                            contentDescription = stringResource(
                                id = R.string.routine_visibility_icon_toggle_all_visible_description,
                            ),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_visibility_off_24),
                            contentDescription = stringResource(
                                id = R.string.routine_visibility_icon_toggle_some_routines_hidden_description,
                            ),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

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
                    modifier = Modifier.size(38.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AgendaList(
    modifier: Modifier = Modifier,
    routineList: List<DisplayRoutine>,
    onRoutineClick: (Long) -> Unit,
    onStatusCheckmarkClick: (DisplayRoutine) -> Unit,
) {
    AndroidViewBinding(
        modifier = modifier,
        factory = AgendaRecyclerviewBinding::inflate,
    ) {
        val adapter = AgendaListAdapter(
            routineList = routineList,
            onRoutineClick = onRoutineClick,
            onCheckmarkClick = onStatusCheckmarkClick,
        )
        agendaRecyclerview.adapter = adapter
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
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    modifier = Modifier.size(32.dp),
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

@Preview(showSystemUi = true)
@Composable
private fun NothingScheduledPreview() {
    Surface {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            NothingScheduled()
        }
    }
}
package com.rendox.routinetracker.feature.agenda

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendox.routinetracker.core.model.HabitStatus
import com.rendox.routinetracker.core.ui.helpers.getStringResourceId
import com.rendox.routinetracker.core.ui.theme.routineStatusColors

@Composable
fun PureAgendaList(
    modifier: Modifier = Modifier,
    routineList: List<DisplayRoutine>,
    isSelectionMode: Boolean,
    selectedRoutineIds: Set<Long>,
    onRoutineClick: (Long) -> Unit,
    onRoutineLongClick: (Long) -> Unit,
    onStatusCheckmarkClick: (DisplayRoutine) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        routineList.forEach { routine ->
            val isSelected = selectedRoutineIds.contains(routine.id)
            AgendaItem(
                modifier = Modifier.fillMaxWidth(),
                routine = routine,
                isSelectionMode = isSelectionMode,
                isSelected = isSelected,
                onRoutineClick = {
                    if (isSelectionMode) {
                        onRoutineLongClick(routine.id)
                    } else {
                        onRoutineClick(routine.id)
                    }
                },
                onRoutineLongClick = { onRoutineLongClick(routine.id) },
                onStatusCheckmarkClick = { onStatusCheckmarkClick(routine) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AgendaItem(
    modifier: Modifier = Modifier,
    routine: DisplayRoutine,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onRoutineClick: () -> Unit,
    onRoutineLongClick: () -> Unit = {},
    onStatusCheckmarkClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                onClick = onRoutineClick,
                onLongClick = onRoutineLongClick,
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                shape = RoundedCornerShape(18.dp),
            )
            .alpha(if (routine.hasGrayedOutLook) 0.6f else 1f),
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Multi-select checkbox when selection mode is active
            AnimatedVisibility(
                visible = isSelectionMode,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onRoutineLongClick() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    modifier = Modifier.padding(end = 8.dp),
                )
            }

            if (!isSelectionMode) {
                StatusCheckmark(
                    modifier = Modifier.padding(end = 14.dp),
                    status = routine.status,
                    onClick = onStatusCheckmarkClick,
                    statusToggleIsDisabled = routine.statusToggleIsDisabled,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = routine.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.size(4.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = stringResource(id = routine.status.getStringResourceId()),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusCheckmark(
    modifier: Modifier = Modifier,
    status: HabitStatus,
    onClick: () -> Unit,
    statusToggleIsDisabled: Boolean,
) {
    val backgroundColor: Color
    var icon: ImageVector?
    var iconColor: Color?

    when (status) {
        HabitStatus.Failed -> {
            backgroundColor = MaterialTheme.routineStatusColors.skippedOutOfStreak
            icon = Icons.Filled.Close
            iconColor = MaterialTheme.routineStatusColors.failedStroke
        }

        HabitStatus.Planned, HabitStatus.OnVacation, HabitStatus.NotDue,
        HabitStatus.Backlog, HabitStatus.AlreadyCompleted,
        HabitStatus.CompletedLater, HabitStatus.NotStarted, HabitStatus.Finished,
        -> {
            backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp)
            icon = null
            iconColor = null
        }

        HabitStatus.Completed, HabitStatus.OverCompleted, HabitStatus.SortedOutBacklog,
        HabitStatus.PartiallyCompleted,
        -> {
            backgroundColor = MaterialTheme.routineStatusColors.skippedInStreak
            icon = Icons.Filled.Done
            iconColor = MaterialTheme.routineStatusColors.completedStroke
        }
    }

    val iconSize: Dp
    if (statusToggleIsDisabled) {
        icon = Icons.Outlined.Lock
        iconColor = MaterialTheme.colorScheme.outline
        iconSize = 16.dp
    } else {
        iconSize = 22.dp
    }

    Surface(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .clickable(
                onClick = onClick,
                enabled = !statusToggleIsDisabled,
            )
            .border(
                width = 1.5.dp,
                color = when (status) {
                    HabitStatus.Completed, HabitStatus.OverCompleted, HabitStatus.SortedOutBacklog, HabitStatus.PartiallyCompleted -> MaterialTheme.routineStatusColors.completedStroke
                    HabitStatus.Failed -> MaterialTheme.routineStatusColors.failedStroke
                    else -> MaterialTheme.colorScheme.outlineVariant
                },
                shape = CircleShape,
            ),
        shape = CircleShape,
        color = backgroundColor,
    ) {
        if (icon != null) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    modifier = Modifier.size(iconSize),
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor ?: MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
package com.rendox.routinetracker.routinedetails.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rendox.routinetracker.routinedetails.CalendarDateData
import kotlinx.datetime.LocalDate

data class MilestoneBadge(
    val title: String,
    val targetDays: Int,
    val isAchieved: Boolean,
)

@Composable
fun StreakStatsCard(
    modifier: Modifier = Modifier,
    currentStreakDurationInDays: Int,
    longestStreakDurationInDays: Int,
    routineCalendarDates: Map<LocalDate, CalendarDateData>,
) {
    // Calculate total completed dates in loaded calendar
    val totalCompletions = routineCalendarDates.values.count { it.numOfTimesCompleted > 0f }
    val totalDueDays = routineCalendarDates.values.count { it.isScheduled }
    val consistencyRate = if (totalDueDays > 0) {
        ((totalCompletions.toFloat() / totalDueDays.toFloat()) * 100f).coerceIn(0f, 100f)
    } else if (totalCompletions > 0) {
        100f
    } else {
        0f
    }

    val animatedConsistency by animateFloatAsState(
        targetValue = consistencyRate / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "consistencyAnimation",
    )

    val milestones = listOf(
        MilestoneBadge(title = "7 Days", targetDays = 7, isAchieved = longestStreakDurationInDays >= 7),
        MilestoneBadge(title = "30 Days", targetDays = 30, isAchieved = longestStreakDurationInDays >= 30),
        MilestoneBadge(title = "100 Days", targetDays = 100, isAchieved = longestStreakDurationInDays >= 100),
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh ?: MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Habit Insights & Milestones",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "${consistencyRate.toInt()}% Consistency",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { animatedConsistency },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Milestone Badges Row
            Text(
                text = "Streak Milestones",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                milestones.forEach { milestone ->
                    MilestoneBadgeItem(
                        modifier = Modifier.weight(1f),
                        badge = milestone,
                    )
                }
            }
        }
    }
}

@Composable
private fun MilestoneBadgeItem(
    modifier: Modifier = Modifier,
    badge: MilestoneBadge,
) {
    val backgroundColor = if (badge.isAchieved) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest ?: MaterialTheme.colorScheme.surface
    }

    val contentColor = if (badge.isAchieved) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (badge.isAchieved) Color(0xFFFFB800).copy(alpha = 0.2f) else Color.Transparent,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (badge.isAchieved) Color(0xFFFFB800) else contentColor,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = badge.title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (badge.isAchieved) FontWeight.Bold else FontWeight.Normal,
                ),
                color = contentColor,
            )

            Text(
                text = if (badge.isAchieved) "Unlocked" else "In Progress",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = contentColor.copy(alpha = 0.8f),
            )
        }
    }
}

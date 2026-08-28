package com.rendox.routinetracker.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val color: Color,
    val angle: Double,
    val speed: Float,
    val radius: Float,
)

/**
 * A lightweight, modern particle burst celebration effect for habit completions.
 */
@Composable
fun CompletionCelebration(
    modifier: Modifier = Modifier,
    isTriggered: Boolean,
    onAnimationEnd: () -> Unit = {},
) {
    if (!isTriggered) return

    val progress = remember { Animatable(0f) }
    val colors = remember {
        listOf(
            Color(0xFF6366F1), // Indigo
            Color(0xFF06B6D4), // Cyan
            Color(0xFF10B981), // Emerald
            Color(0xFFF59E0B), // Amber
            Color(0xFFEC4899), // Pink
            Color(0xFF8B5CF6), // Violet
        )
    }

    val particles = remember {
        List(28) {
            Particle(
                color = colors[it % colors.size],
                angle = Random.nextDouble(0.0, 2 * Math.PI),
                speed = Random.nextFloat() * 120f + 60f,
                radius = Random.nextFloat() * 4f + 3f,
            )
        }
    }

    LaunchedEffect(isTriggered) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 650,
                easing = FastOutSlowInEasing,
            ),
        )
        onAnimationEnd()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val currentProgress = progress.value
        val alpha = (1f - currentProgress).coerceIn(0f, 1f)

        particles.forEach { particle ->
            val distance = particle.speed * currentProgress
            val x = center.x + (distance * cos(particle.angle)).toFloat()
            val y = center.y + (distance * sin(particle.angle)).toFloat()
            val currentRadius = particle.radius * (1f - (currentProgress * 0.4f))

            drawCircle(
                color = particle.color.copy(alpha = alpha),
                radius = currentRadius,
                center = Offset(x, y),
            )
        }
    }
}

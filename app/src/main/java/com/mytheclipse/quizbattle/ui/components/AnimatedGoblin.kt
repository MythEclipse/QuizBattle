package com.mytheclipse.quizbattle.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import com.mytheclipse.quizbattle.R
import kotlinx.coroutines.delay

enum class GoblinAnimation {
    IDLE,
    ATTACK,
    HURT,
    DEAD
}

@Composable
fun AnimatedGoblin(
    animation: GoblinAnimation,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 96.dp
) {
    // Mapping frame resources berdasarkan folder Goblin
    // Idle: 0001-0040 (40 frames)
    // Atk1: 0070-0095 (26 frames)
    // Dead: 0160-0183 (24 frames)
    val frameResources = when (animation) {
        GoblinAnimation.IDLE -> listOf(
            R.drawable.goblin_idle_0001, R.drawable.goblin_idle_0002, R.drawable.goblin_idle_0003,
            R.drawable.goblin_idle_0004, R.drawable.goblin_idle_0005, R.drawable.goblin_idle_0006,
            R.drawable.goblin_idle_0007, R.drawable.goblin_idle_0008, R.drawable.goblin_idle_0009,
            R.drawable.goblin_idle_0010, R.drawable.goblin_idle_0011, R.drawable.goblin_idle_0012,
            R.drawable.goblin_idle_0013, R.drawable.goblin_idle_0014, R.drawable.goblin_idle_0015,
            R.drawable.goblin_idle_0016, R.drawable.goblin_idle_0017, R.drawable.goblin_idle_0018,
            R.drawable.goblin_idle_0019, R.drawable.goblin_idle_0020, R.drawable.goblin_idle_0021,
            R.drawable.goblin_idle_0022, R.drawable.goblin_idle_0023, R.drawable.goblin_idle_0024,
            R.drawable.goblin_idle_0025, R.drawable.goblin_idle_0026, R.drawable.goblin_idle_0027,
            R.drawable.goblin_idle_0028, R.drawable.goblin_idle_0029, R.drawable.goblin_idle_0030,
            R.drawable.goblin_idle_0031, R.drawable.goblin_idle_0032, R.drawable.goblin_idle_0033,
            R.drawable.goblin_idle_0034, R.drawable.goblin_idle_0035, R.drawable.goblin_idle_0036,
            R.drawable.goblin_idle_0037, R.drawable.goblin_idle_0038, R.drawable.goblin_idle_0039,
            R.drawable.goblin_idle_0040
        )
        GoblinAnimation.ATTACK -> listOf(
            R.drawable.goblin_atk1_0070, R.drawable.goblin_atk1_0071, R.drawable.goblin_atk1_0072,
            R.drawable.goblin_atk1_0073, R.drawable.goblin_atk1_0074, R.drawable.goblin_atk1_0075,
            R.drawable.goblin_atk1_0076, R.drawable.goblin_atk1_0077, R.drawable.goblin_atk1_0078,
            R.drawable.goblin_atk1_0079, R.drawable.goblin_atk1_0080, R.drawable.goblin_atk1_0081,
            R.drawable.goblin_atk1_0082, R.drawable.goblin_atk1_0083, R.drawable.goblin_atk1_0084,
            R.drawable.goblin_atk1_0085, R.drawable.goblin_atk1_0086, R.drawable.goblin_atk1_0087,
            R.drawable.goblin_atk1_0088, R.drawable.goblin_atk1_0089, R.drawable.goblin_atk1_0090,
            R.drawable.goblin_atk1_0091, R.drawable.goblin_atk1_0092, R.drawable.goblin_atk1_0093,
            R.drawable.goblin_atk1_0094, R.drawable.goblin_atk1_0095
        )
        GoblinAnimation.HURT -> listOf(
            R.drawable.goblin_idle_0001 // Placeholder, bisa diganti dengan hurt frames jika ada
        )
        GoblinAnimation.DEAD -> listOf(
            R.drawable.goblin_dead_0160, R.drawable.goblin_dead_0161, R.drawable.goblin_dead_0162,
            R.drawable.goblin_dead_0163, R.drawable.goblin_dead_0164, R.drawable.goblin_dead_0165,
            R.drawable.goblin_dead_0166, R.drawable.goblin_dead_0167, R.drawable.goblin_dead_0168,
            R.drawable.goblin_dead_0169, R.drawable.goblin_dead_0170, R.drawable.goblin_dead_0171,
            R.drawable.goblin_dead_0172, R.drawable.goblin_dead_0173, R.drawable.goblin_dead_0174,
            R.drawable.goblin_dead_0175, R.drawable.goblin_dead_0176, R.drawable.goblin_dead_0177,
            R.drawable.goblin_dead_0178, R.drawable.goblin_dead_0179, R.drawable.goblin_dead_0180,
            R.drawable.goblin_dead_0181, R.drawable.goblin_dead_0182, R.drawable.goblin_dead_0183
        )
    }

    val frameCount = frameResources.size
    val animDuration = when (animation) {
        GoblinAnimation.IDLE -> 1600   // 40 frames
        GoblinAnimation.ATTACK -> 900  // 26 frames
        GoblinAnimation.HURT -> 300    // 1 frame (placeholder)
        GoblinAnimation.DEAD -> 1200   // 24 frames
    }

    val loop = animation == GoblinAnimation.IDLE
    var currentIdx by remember(animation) { mutableStateOf(0) }

    if (loop) {
        val infiniteTransition = rememberInfiniteTransition(label = "goblin_loop")
        val frame by infiniteTransition.animateValue(
            initialValue = 0,
            targetValue = frameCount - 1,
            typeConverter = Int.VectorConverter,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = animDuration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "goblin_frame"
        )
        currentIdx = frame
    } else {
        LaunchedEffect(animation) {
            val perFrame = (animDuration / frameCount.toFloat()).toLong().coerceAtLeast(16L)
            for (i in 0 until frameCount) {
                currentIdx = i
                delay(perFrame)
            }
            currentIdx = frameCount - 1
        }
    }

    val currentFrame = ImageBitmap.imageResource(frameResources[currentIdx])

    Image(
        bitmap = currentFrame,
        contentDescription = "Goblin $animation",
        modifier = modifier
            .size(size)
            .graphicsLayer(scaleX = -1f), // Flip horizontal to face left
        contentScale = ContentScale.Fit
    )
}

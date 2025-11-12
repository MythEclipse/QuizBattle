package com.mytheclipse.quizbattle.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import com.mytheclipse.quizbattle.R
import kotlinx.coroutines.delay

enum class KnightAnimation {
    IDLE,
    ATTACK,
    HURT,
    DEAD
}

@Composable
fun AnimatedKnight(
    animation: KnightAnimation,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 80.dp
) {
    // Frame resources for each animation
    val frameResources = when (animation) {
        KnightAnimation.IDLE -> listOf(
            R.drawable.knight_idle_0001, R.drawable.knight_idle_0002,
            R.drawable.knight_idle_0003, R.drawable.knight_idle_0004
        )
        KnightAnimation.ATTACK -> listOf(
            R.drawable.knight_attack_0001, R.drawable.knight_attack_0002,
            R.drawable.knight_attack_0003, R.drawable.knight_attack_0004,
            R.drawable.knight_attack_0005
        )
        KnightAnimation.HURT -> listOf(
            R.drawable.knight_hurt_0001, R.drawable.knight_hurt_0002
        )
        KnightAnimation.DEAD -> listOf(
            R.drawable.knight_dead_0001, R.drawable.knight_dead_0002,
            R.drawable.knight_dead_0003, R.drawable.knight_dead_0004,
            R.drawable.knight_dead_0005, R.drawable.knight_dead_0006
        )
    }

    val frameCount = frameResources.size
    val animDuration = when (animation) {
        KnightAnimation.IDLE -> 1200
        KnightAnimation.ATTACK -> 500
        KnightAnimation.HURT -> 300
        KnightAnimation.DEAD -> 900
    }

    val loop = animation == KnightAnimation.IDLE
    var currentIdx by remember(animation) { mutableStateOf(0) }

    if (loop) {
        val infiniteTransition = rememberInfiniteTransition(label = "knight_loop")
        val frame by infiniteTransition.animateValue(
            initialValue = 0,
            targetValue = frameCount - 1,
            typeConverter = Int.VectorConverter,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = animDuration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "knight_frame"
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
        contentDescription = "Knight $animation",
        modifier = modifier.size(size),
        contentScale = ContentScale.Fit
    )
}

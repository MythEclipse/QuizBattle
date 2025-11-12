package com.mytheclipse.quizbattle.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
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
    val spriteSheet = ImageBitmap.imageResource(R.drawable.enemy_goblin)

    val cols = 9
    val rows = 6
    val frameWidth = spriteSheet.width / cols
    val frameHeight = spriteSheet.height / rows

    // Sprite sheet: 9 kolom x 6 baris = 54 frame total (0-53)
    // Setiap baris punya 9 frame:
    // Baris 0 (frame 0-8): IDLE
    // Baris 1 (frame 9-17): ATTACK
    // Baris 2 (frame 18-26): Walk (tidak dipakai)
    // Baris 3 (frame 27-35): Run (tidak dipakai)
    // Baris 4 (frame 36-44): HURT
    // Baris 5 (frame 45-53): DEAD
    val frameMap = mapOf(
        GoblinAnimation.IDLE to (0..8).toList(),    // 9 frames
        GoblinAnimation.ATTACK to (9..17).toList(), // 9 frames
        GoblinAnimation.HURT to (36..44).toList(),  // 9 frames
        GoblinAnimation.DEAD to (45..53).toList()   // 9 frames
    )

    val frames = frameMap[animation] ?: error("Unknown animation: $animation")
    val frameCount = frames.size

    val animDuration = when (animation) {
        GoblinAnimation.IDLE -> 1200
        GoblinAnimation.ATTACK -> 900
        GoblinAnimation.HURT -> 500
        GoblinAnimation.DEAD -> 1000
    }

    val loop = animation == GoblinAnimation.IDLE
    var currentIdx by remember { mutableStateOf(0) }

    if (loop) {
        val infiniteTransition = rememberInfiniteTransition(label = "goblin_loop")
        val frame by infiniteTransition.animateValue(
            initialValue = 0,
            targetValue = frameCount - 1,
            typeConverter = Int.VectorConverter,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = animDuration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
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

    val absoluteIndex = frames[currentIdx]
    val col = absoluteIndex % cols
    val row = absoluteIndex / cols

    Canvas(modifier = modifier.size(size)) {
        val srcOffset = IntOffset(col * frameWidth, row * frameHeight)
        val srcSize = IntSize(frameWidth, frameHeight)
        scale(
            scaleX = this.size.width / frameWidth,
            scaleY = this.size.height / frameHeight
        ) {
            drawImage(
                image = spriteSheet,
                srcOffset = srcOffset,
                srcSize = srcSize
            )
        }
    }
}

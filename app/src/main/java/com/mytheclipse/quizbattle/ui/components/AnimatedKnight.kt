package com.mytheclipse.quizbattle.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.mytheclipse.quizbattle.R

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
    val context = LocalContext.current
    
    // Load sprite sheet based on animation
    val (spriteSheet, frameCount, frameWidth, frameHeight) = when (animation) {
        KnightAnimation.IDLE -> {
            val bitmap = ImageBitmap.imageResource(context.resources, R.drawable.knight_idle)
            // Idle: 290x86, 10 frames
            FourTuple(bitmap, 10, 29, 86)
        }
        KnightAnimation.ATTACK -> {
            val bitmap = ImageBitmap.imageResource(context.resources, R.drawable.knight_attack)
            // Attack: 430x86, 4 frames
            FourTuple(bitmap, 4, 107, 86)
        }
        KnightAnimation.HURT -> {
            val bitmap = ImageBitmap.imageResource(context.resources, R.drawable.knight_hurt)
            // Hurt: 140x86, 3 frames
            FourTuple(bitmap, 3, 46, 86)
        }
        KnightAnimation.DEAD -> {
            val bitmap = ImageBitmap.imageResource(context.resources, R.drawable.knight_dead)
            // Dead: 290x86, 10 frames
            FourTuple(bitmap, 10, 29, 86)
        }
    }
    
    // Animate frame index
    val infiniteTransition = rememberInfiniteTransition(label = "knight_animation")
    val frameIndex by infiniteTransition.animateValue(
        initialValue = 0,
        targetValue = frameCount,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (animation) {
                    KnightAnimation.IDLE -> 1000
                    KnightAnimation.ATTACK -> 400
                    KnightAnimation.HURT -> 300
                    KnightAnimation.DEAD -> 800
                },
                easing = LinearEasing
            ),
            repeatMode = if (animation == KnightAnimation.DEAD) RepeatMode.Restart else RepeatMode.Restart
        ),
        label = "frame_index"
    )
    
    Canvas(modifier = modifier.size(size)) {
        val currentFrame = frameIndex % frameCount
        val srcOffset = IntOffset(currentFrame * frameWidth, 0)
        val srcSize = IntSize(frameWidth, frameHeight)
        
        scale(
            scaleX = this.size.width / frameWidth,
            scaleY = this.size.height / frameHeight
        ) {
            drawImage(
                image = spriteSheet,
                srcOffset = srcOffset,
                srcSize = srcSize,
                dstOffset = Offset.Zero,
                dstSize = Size(frameWidth.toFloat(), frameHeight.toFloat())
            )
        }
    }
}

// Helper data class to hold 4 values
private data class FourTuple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

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

enum class OrcAnimation {
    IDLE,
    ATTACK,
    HURT,
    DEAD
}

@Composable
fun AnimatedOrc(
    animation: OrcAnimation,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 80.dp
) {
    val context = LocalContext.current
    
    // Load sprite sheet based on animation
    val (spriteSheet, frameCount, frameWidth, frameHeight) = when (animation) {
        OrcAnimation.IDLE -> {
            val bitmap = ImageBitmap.imageResource(context.resources, R.drawable.orc_idle)
            // Idle: 600x100, 6 frames
            FourTuple(bitmap, 6, 100, 100)
        }
        OrcAnimation.ATTACK -> {
            val bitmap = ImageBitmap.imageResource(context.resources, R.drawable.orc_attack)
            // Attack: ~400x100, 4 frames
            FourTuple(bitmap, 4, 100, 100)
        }
        OrcAnimation.HURT -> {
            val bitmap = ImageBitmap.imageResource(context.resources, R.drawable.orc_hurt)
            // Hurt: ~200x100, 2 frames
            FourTuple(bitmap, 2, 100, 100)
        }
        OrcAnimation.DEAD -> {
            val bitmap = ImageBitmap.imageResource(context.resources, R.drawable.orc_dead)
            // Dead: ~400x100, 4 frames
            FourTuple(bitmap, 4, 100, 100)
        }
    }
    
    // Animate frame index
    val infiniteTransition = rememberInfiniteTransition(label = "orc_animation")
    val frameIndex by infiniteTransition.animateValue(
        initialValue = 0,
        targetValue = frameCount,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (animation) {
                    OrcAnimation.IDLE -> 800
                    OrcAnimation.ATTACK -> 400
                    OrcAnimation.HURT -> 300
                    OrcAnimation.DEAD -> 600
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
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

// Helper data class
private data class FourTuple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

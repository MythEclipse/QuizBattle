package com.mytheclipse.quizbattle.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalContext
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
            val bitmap = ImageBitmap.imageResource(R.drawable.knight_idle)
            // Idle: 290x86, 4 frames (approximately 72-73 px each)
            AnimData(bitmap, 4, 72, 86)
        }
        KnightAnimation.ATTACK -> {
            val bitmap = ImageBitmap.imageResource(R.drawable.knight_attack)
            // Attack: 430x86, 5 frames @ 86px each
            AnimData(bitmap, 5, 86, 86)
        }
        KnightAnimation.HURT -> {
            val bitmap = ImageBitmap.imageResource(R.drawable.knight_hurt)
            // Hurt: 140x86, 2 frames @ 70px each
            AnimData(bitmap, 2, 70, 86)
        }
        KnightAnimation.DEAD -> {
            val bitmap = ImageBitmap.imageResource(R.drawable.knight_dead)
            // Dead: 480x86, 6 frames @ 80px each
            AnimData(bitmap, 6, 80, 86)
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
                srcSize = srcSize
            )
        }
    }
}

// Helper data class
private data class AnimData(
    val bitmap: ImageBitmap,
    val frameCount: Int,
    val frameWidth: Int,
    val frameHeight: Int
)

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
    size: androidx.compose.ui.unit.Dp = 80.dp
) {
    val context = LocalContext.current
    val spriteSheet = ImageBitmap.imageResource(R.drawable.enemy_goblin)
    
    // Goblin sheet is 2700x900 (9 columns x 6 rows)
    // Each frame is 300x150
    val frameWidth = 300
    val frameHeight = 150
    
    // Define animation rows and frame counts (9 frames per row)
    // Row 0: Idle
    // Row 1: Walk  
    // Row 2: Run
    // Row 3: Attack
    // Row 4: Hurt
    // Row 5: Dead
    
    val (row, frameCount, animDuration) = when (animation) {
        GoblinAnimation.IDLE -> Triple(0, 9, 1200)
        GoblinAnimation.ATTACK -> Triple(3, 9, 600)
        GoblinAnimation.HURT -> Triple(4, 9, 400)
        GoblinAnimation.DEAD -> Triple(5, 9, 800)
    }
    
    // Animate frame index
    val infiniteTransition = rememberInfiniteTransition(label = "goblin_animation")
    val frameIndex by infiniteTransition.animateValue(
        initialValue = 0,
        targetValue = frameCount,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animDuration,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "frame_index"
    )
    
    Canvas(modifier = modifier.size(size)) {
        val currentFrame = frameIndex % frameCount
        val srcOffset = IntOffset(currentFrame * frameWidth, row * frameHeight)
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

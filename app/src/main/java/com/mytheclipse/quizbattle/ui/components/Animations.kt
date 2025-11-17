package com.mytheclipse.quizbattle.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ConfettiBurst(
    modifier: Modifier = Modifier,
    particleCount: Int = 18,
    durationMillis: Int = 900,
    maxRadius: Dp = 120.dp,
    colors: List<Color> = listOf(
        Color(0xFFEF5350), Color(0xFF42A5F5), Color(0xFF66BB6A),
        Color(0xFFFFCA28), Color(0xFFAB47BC)
    ),
    onFinished: (() -> Unit)? = null
) {
    val maxRadiusPx = with(androidx.compose.ui.platform.LocalDensity.current) { maxRadius.toPx() }
    val anim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        anim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing)
        )
        onFinished?.invoke()
    }

    val particles = remember(particleCount) {
        List(particleCount) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = 0.5f + Random.nextFloat() * 0.5f
            val color = colors[Random.nextInt(colors.size)]
            val size = 4f + Random.nextFloat() * 6f
            Triple(angle, speed, Pair(color, size))
        }
    }

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val progress = anim.value
        val alpha = (1f - progress).coerceIn(0f, 1f)

        particles.forEach { (angle, speed, colorSize) ->
            val distance = progress * speed * maxRadiusPx
            val x = center.x + cos(angle) * distance
            val y = center.y + sin(angle) * distance
            val c = colorSize.first.copy(alpha = alpha)
            val r = colorSize.second
            drawCircle(color = c, radius = r, center = Offset(x, y))
        }
    }
}

@Composable
fun FallingConfetti(
    modifier: Modifier = Modifier,
    particleCount: Int = 20,
    durationMillis: Int = 1400,
    colors: List<Color> = listOf(
        Color(0xFFEF5350), Color(0xFF42A5F5), Color(0xFF66BB6A),
        Color(0xFFFFCA28), Color(0xFFAB47BC)
    )
) {
    val anim = rememberInfiniteTransition(label = "falling")
    val phases = remember {
        List(particleCount) { Random.nextFloat() }
    }

    val progresses = phases.map { phase ->
        anim.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset((phase * durationMillis).toInt())
            ),
            label = "progress"
        )
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        progresses.forEachIndexed { index, state ->
            val p = state.value
            val x = (index + 1f) / (progresses.size + 1f) * width + (sin(p * 6f) * 20f)
            val y = p * height
            val color = colors[index % colors.size].copy(alpha = (1f - p).coerceAtLeast(0.2f))
            drawCircle(color, radius = 4f, center = Offset(x, y))
        }
    }
}

@Composable
fun PulseRings(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF4CAF50),
    maxRadius: Dp = 140.dp
) {
    val maxRadiusPx = with(androidx.compose.ui.platform.LocalDensity.current) { maxRadius.toPx() }
    val transition = rememberInfiniteTransition(label = "pulse")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "p"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        for (i in 0..2) {
            val phase = ((progress + i * 0.33f) % 1f)
            val radius = phase * maxRadiusPx
            val alpha = (1f - phase).coerceIn(0f, 1f)
            drawCircle(color.copy(alpha = alpha * 0.4f), radius = radius, center = center)
        }
    }
}

@Composable
fun DimmedOverlay(modifier: Modifier = Modifier, alpha: Float = 0.2f) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = alpha))
    )
}

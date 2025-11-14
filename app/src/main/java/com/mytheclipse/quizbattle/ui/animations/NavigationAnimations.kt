package com.mytheclipse.quizbattle.ui.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry

/**
 * Standard slide in/out animations for navigation
 */
@OptIn(ExperimentalAnimationApi::class)
fun slideInFromRight(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(300))
}

@OptIn(ExperimentalAnimationApi::class)
fun slideOutToLeft(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(300))
}

@OptIn(ExperimentalAnimationApi::class)
fun slideInFromLeft(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(300))
}

@OptIn(ExperimentalAnimationApi::class)
fun slideOutToRight(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(300))
}

/**
 * Fade in/out animations
 */
@OptIn(ExperimentalAnimationApi::class)
fun fadeIn(): EnterTransition {
    return fadeIn(animationSpec = tween(300))
}

@OptIn(ExperimentalAnimationApi::class)
fun fadeOut(): ExitTransition {
    return fadeOut(animationSpec = tween(300))
}

/**
 * Scale animations for dialogs or pop-ups
 */
@OptIn(ExperimentalAnimationApi::class)
fun scaleInEnter(): EnterTransition {
    return scaleIn(
        initialScale = 0.8f,
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(300))
}

@OptIn(ExperimentalAnimationApi::class)
fun scaleOutExit(): ExitTransition {
    return scaleOut(
        targetScale = 0.8f,
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(300))
}

/**
 * Vertical slide for bottom sheets or modals
 */
@OptIn(ExperimentalAnimationApi::class)
fun slideInFromBottom(): EnterTransition {
    return slideInVertically(
        initialOffsetY = { fullHeight -> fullHeight },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(300))
}

@OptIn(ExperimentalAnimationApi::class)
fun slideOutToBottom(): ExitTransition {
    return slideOutVertically(
        targetOffsetY = { fullHeight -> fullHeight },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(300))
}

/**
 * Modifier extension for button press animation
 */
@Composable
fun Modifier.pressAnimation(
    pressed: Boolean,
    targetScale: Float = 0.95f
): Modifier {
    val scale = animateFloatAsState(
        targetValue = if (pressed) targetScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "button_press"
    )
    
    return this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

/**
 * Pulse animation for attention-grabbing elements
 */
@Composable
fun rememberPulseAnimation(): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    return scale.value
}

/**
 * Shake animation for errors or invalid actions
 */
@Composable
fun Modifier.shakeAnimation(trigger: Boolean): Modifier {
    val offset = animateDpAsState(
        targetValue = if (trigger) {
            if ((System.currentTimeMillis() / 50) % 2 == 0L) 8.dp else (-8).dp
        } else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "shake"
    )
    
    return this.graphicsLayer {
        translationX = offset.value.toPx()
    }
}

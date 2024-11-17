package com.thomasstubblefield.always.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import com.thomasstubblefield.always.Chip

@Composable
fun AnimatedChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var wasSelected by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    LaunchedEffect(isSelected) {
        if (isSelected && !wasSelected) {
            isPressed = true
            delay(200)
            isPressed = false
        }
        wasSelected = isSelected
    }

    val transition = updateTransition(
        targetState = isPressed,
        label = "chip_animation"
    )
    
    val scale by transition.animateFloat(
        label = "chip_scale",
        transitionSpec = {
            if (targetState) {
                spring(
                    dampingRatio = 0.9f,
                    stiffness = Spring.StiffnessHigh
                )
            } else {
                spring(
                    dampingRatio = 0.8f,
                    stiffness = Spring.StiffnessHigh
                )
            }
        }
    ) { pressed ->
        if (pressed) 0.95f else 1f
    }

    Box(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        Chip(
            text = text,
            isSelected = isSelected,
            onClick = onClick
        )
    }
} 
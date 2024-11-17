package com.thomasstubblefield.always.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.thomasstubblefield.always.Chip

@Composable
fun AnimatedChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Chip(
        text = text,
        isSelected = isSelected,
        onClick = onClick
    )
} 
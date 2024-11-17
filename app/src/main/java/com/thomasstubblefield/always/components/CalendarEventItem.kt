package com.thomasstubblefield.always.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun CalendarEventItem(
    title: String,
    startTime: LocalDateTime,
    endTime: LocalDateTime,
    color: Color,
    blockHeight: Dp,
    hoursFromStart: Float,
    eventDurationHours: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 64.dp, end = 16.dp)
            .offset(y = (-24).dp + 46.dp + (blockHeight * hoursFromStart))
            .height(blockHeight * eventDurationHours)
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title.ifEmpty { "Untitled Event" },
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            
            Text(
                text = "${startTime.format(DateTimeFormatter.ofPattern("h:mm a"))} - ${endTime.format(DateTimeFormatter.ofPattern("h:mm a"))}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
    }
} 
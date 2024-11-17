package com.thomasstubblefield.always.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import com.thomasstubblefield.always.Event
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip

@Composable
fun Timeline(
    selectedEvent: Map.Entry<String, Event>?,
    selectedChipIndex: Int,
    blockHeight: Dp = 91.dp
) {
    fun generateTimeSlots(startTime: LocalDateTime, stopTime: LocalDateTime): List<String> {
        val formatter = DateTimeFormatter.ofPattern("EEE\nh a")
        val times = mutableListOf<String>()
        var current = startTime.truncatedTo(ChronoUnit.HOURS)
        var lastDay = ""

        while (current <= stopTime) {
            val day = current.format(DateTimeFormatter.ofPattern("EEE"))
            val time = current.format(DateTimeFormatter.ofPattern("h a"))
            if (day != lastDay) {
                times.add("$day\n$time")
                lastDay = day
            } else {
                times.add(time)
            }
            current = current.plusHours(1)
        }

        return times
    }

    val startTime = selectedEvent?.let {
        LocalDateTime.parse(it.value.startTime.removeSuffix("Z"))
    } ?: LocalDateTime.now()
    
    val stopTime = selectedEvent?.let {
        LocalDateTime.parse(it.value.endTime.removeSuffix("Z"))
    } ?: LocalDateTime.now().plusHours(8)

    val timeSlots = generateTimeSlots(startTime, stopTime)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(timeSlots) { index, time ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(blockHeight)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(blockHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = time,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray,
                                lineHeight = 12.sp
                            ),
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    HorizontalDivider(
                        color = Color.Gray,
                        thickness = 1.dp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        selectedEvent?.value?.calendar_events?.forEach { calendarEvent ->
            val eventStart = LocalDateTime.parse(calendarEvent.startTime.removeSuffix("Z"))
            val eventEnd = LocalDateTime.parse(calendarEvent.endTime.removeSuffix("Z"))
            
            val hoursSinceStart = ChronoUnit.HOURS.between(startTime, eventStart).toFloat()
            val eventDurationHours = ChronoUnit.HOURS.between(eventStart, eventEnd).toFloat()
            
            val topPadding = (hoursSinceStart * blockHeight.value).dp + 46.dp
            val eventHeight = blockHeight * eventDurationHours

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 64.dp, end = 16.dp)
                    .offset(y = topPadding)
                    .height(eventHeight)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0293D4))
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = calendarEvent.title.ifEmpty { "Untitled Event" },
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    
                    Text(
                        text = "${eventStart.format(DateTimeFormatter.ofPattern("h:mm a"))} - ${eventEnd.format(DateTimeFormatter.ofPattern("h:mm a"))}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
} 
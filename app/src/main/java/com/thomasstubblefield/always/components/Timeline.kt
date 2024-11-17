package com.thomasstubblefield.always.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex

@Composable
fun Timeline(
    selectedEvent: Map.Entry<String, Event>?,
    selectedChipIndex: Int,
    blockHeight: Dp = 91.dp
) {
    // Add platform density
    val density = LocalDensity.current

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

    val listState = rememberLazyListState()
    val totalHeight = blockHeight * timeSlots.size

    Box(modifier = Modifier.fillMaxSize()) {
        // Container that defines the total scrollable height
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            item {
                Box(modifier = Modifier.height(totalHeight)) {
                    // Time slots layer
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(y = (-24).dp)
                    ) {
                        timeSlots.forEach { time ->
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
                                            color = Color.Gray.copy(alpha = 0.8f),
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

                    // Events layer
                    selectedEvent?.value?.calendar_events?.forEach { calendarEvent ->
                        val eventStart = LocalDateTime.parse(calendarEvent.startTime.removeSuffix("Z"))
                        val eventEnd = LocalDateTime.parse(calendarEvent.endTime.removeSuffix("Z"))
                        
                        val hoursFromStart = ChronoUnit.HOURS.between(
                            startTime.truncatedTo(ChronoUnit.HOURS),
                            eventStart.truncatedTo(ChronoUnit.HOURS)
                        ).toFloat()
                        
                        val eventDurationHours = ChronoUnit.HOURS.between(eventStart, eventEnd).toFloat()
                        
                        val eventColor = calendarEvent.color?.let { colorString ->
                            val (r, g, b) = colorString.split(",").map { it.trim().toInt() }
                            Color(r, g, b)
                        } ?: Color(2, 147, 212)
                        
                        CalendarEventItem(
                            title = calendarEvent.title,
                            startTime = eventStart,
                            endTime = eventEnd,
                            color = eventColor,
                            blockHeight = blockHeight,
                            hoursFromStart = hoursFromStart,
                            eventDurationHours = eventDurationHours
                        )
                    }
                }
            }
        }
    }
} 
package com.thomasstubblefield.always.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thomasstubblefield.always.Event
import com.thomasstubblefield.always.Chip

@Composable
fun TabBar(
    selectedEvent: Map.Entry<String, Event>?,
    selectedChipIndex: Int,
    onChipSelected: (Int) -> Unit,
    onChipCountChanged: (Int) -> Unit
) {
    // Create chip list with "Schedule" and "You" as initial items
    val chipItems = remember(selectedEvent) {
        buildList {
            add("Schedule")
            add("You")
            selectedEvent?.value?.teamMembers?.forEach { member ->
                add(member.name)
            }
        }
    }

    // Notify parent of chip count changes
    LaunchedEffect(chipItems.size) {
        onChipCountChanged(chipItems.size)
    }

    val chipListState = rememberLazyListState()

    LaunchedEffect(selectedChipIndex) {
        // Get the visible items info
        val visibleItems = chipListState.layoutInfo.visibleItemsInfo
        val selectedItemVisible = visibleItems.any { 
            it.index == selectedChipIndex && 
            it.offset >= 0 && // not cut off at start
            (it.offset + it.size) <= chipListState.layoutInfo.viewportEndOffset // not cut off at end
        }
        
        // Scroll if the selected item isn't fully visible
        if (!selectedItemVisible) {
            chipListState.animateScrollToItem(selectedChipIndex)
        }
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        state = chipListState
    ) {
        itemsIndexed(chipItems) { index, text ->
            AnimatedChip(
                text = text,
                isSelected = selectedChipIndex == index,
                onClick = { onChipSelected(index) }
            )
        }
    }
} 
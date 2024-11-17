package com.thomasstubblefield.always

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import android.content.Context
import com.thomasstubblefield.always.TokenManager
import kotlinx.coroutines.launch
import com.thomasstubblefield.always.AuthResponse
import androidx.compose.ui.unit.Dp
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.IOException
import com.thomasstubblefield.always.Event
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import androidx.compose.ui.input.pointer.pointerInput
import android.view.MotionEvent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInteropFilter
import kotlin.math.abs
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween

@Composable
fun ProfilePicture(
    url: String?,
    name: String,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    if (url != null) {
        SubcomposeAsyncImage(
            model = url,
            contentDescription = "Profile picture of $name",
            modifier = modifier
                .size(size)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            loading = {
                CircularProgressIndicator(
                    modifier = Modifier.size(size / 2),
                    strokeWidth = 2.dp
                )
            },
            error = {
                ProfileInitial(name = name, size = size)
            }
        )
    } else {
        ProfileInitial(name = name, size = size)
    }
}

@Composable
private fun ProfileInitial(
    name: String,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = Color.LightGray
    ) {
        Text(
            text = name.firstOrNull()?.uppercase() ?: "?",
            modifier = Modifier.wrapContentSize(),
            style = MaterialTheme.typography.titleLarge,
            color = Color.DarkGray
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }
    var userData: AuthResponse? by remember { mutableStateOf(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<Map.Entry<String, Event>?>(null) }
    var showEventPicker by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var isDeletingAccount by remember { mutableStateOf(false) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                isUploadingPhoto = true
                try {
                    val file = createTempFileFromUri(context, uri)
                    val newProfilePictureUrl = uploadProfilePicture(file, tokenManager.getToken() ?: "")
                    // Re-authenticate to get updated user data
                    tokenManager.authenticate()
                        .onSuccess { response ->
                            userData = response
                        }
                } catch (e: Exception) {
                    // TODO: Show error to user
                    println("Profile picture upload error: ${e.message}")
                } finally {
                    isUploadingPhoto = false
                }
            }
        }
    }

    // Authenticate when the screen loads
    LaunchedEffect(Unit) {
        tokenManager.authenticate()
            .onSuccess { response ->
                userData = response
                // Try to restore the previously selected event, fall back to first event if none saved
                val savedEventId = tokenManager.getSelectedEventId()
                selectedEvent = if (savedEventId != null) {
                    response.events?.entries?.find { it.key == savedEventId }
                } else {
                    response.events?.entries?.firstOrNull()
                }?.also { event ->
                    // Save the event ID in case we fell back to the first event
                    tokenManager.saveSelectedEventId(event.key)
                }
            }
            .onFailure {
                // If auth fails, navigate back to onboarding
                navController.navigate("onboarding") {
                    popUpTo(0) { inclusive = true }
                }
            }
        isLoading = false
    }

    val blockHeight = 72.dp // Define the block height

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

    val startTime = LocalDateTime.of(2024, 11, 22, 9, 0) // November 22nd, 2024, 9 AM
    val stopTime = LocalDateTime.of(2024, 11, 24, 17, 0) // November 24th, 2024, 5 PM

    val timeSlots = generateTimeSlots(startTime, stopTime)

    val chipItems = listOf("Schedule", "You", "Dieter", "Sam", "Dev", "Nila", "Vela", "JC")
    var selectedChipIndex by remember { mutableStateOf(0) }

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

    var touchX by remember { mutableStateOf(0f) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        Text(
                            "Always",
                            style = MaterialTheme.typography.headlineLarge
                        ) 
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showMenu = !showMenu }) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    userData?.let { user ->
                                        ProfilePicture(
                                            url = user.profile_picture_url,
                                            name = user.name,
                                            size = 56.dp
                                        )
                                    }
                                }
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.width(200.dp)
                            ) {

                                DropdownMenuItem(
                                    text = { 
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Update Avatar")
                                            if (isUploadingPhoto) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        if (!isUploadingPhoto) {  // Prevent multiple uploads
                                            showMenu = false
                                            photoPickerLauncher.launch("image/*")
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector =Icons.Filled.AccountCircle,
                                            contentDescription = "Profile Picture"
                                        )
                                    },
                                    enabled = !isUploadingPhoto  // Disable the button while uploading
                                )
                                
                                DropdownMenuItem(
                                    text = { Text("Swap Events") },
                                    onClick = {
                                        showMenu = false
                                        showEventPicker = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.SwapHoriz,
                                            contentDescription = "Swap Events"
                                        )
                                    }
                                )



                                DropdownMenuItem(
                                    text = { Text("Logout") },
                                    onClick = {
                                        showMenu = false
                                        scope.launch {
                                            tokenManager.deleteToken()
                                            tokenManager.clearSelectedEventId()
                                            navController.navigate("onboarding") {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Logout,
                                            contentDescription = "Logout"
                                        )
                                    }
                                )

                                DropdownMenuItem(
                                    text = { 
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "Delete Account",
                                                color = Color.Red
                                            )
                                            if (isDeletingAccount) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        showMenu = false
                                        showDeleteConfirmation = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Account",
                                            tint = Color.Red
                                        )
                                    }
                                )
                            }
                        }
                    }
                )
                LazyRow(
                    state = chipListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chipItems.size) { index ->
                        val chip = chipItems[index]
                        Chip(
                            text = chip,
                            isSelected = index == selectedChipIndex,
                            onClick = { selectedChipIndex = index }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        val haptics = LocalHapticFeedback.current

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                    top = paddingValues.calculateTopPadding()
                )
                .pointerInteropFilter { event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            touchX = event.x
                            true
                        }
                        MotionEvent.ACTION_UP -> {
                            val deltaX = event.x - touchX
                            when {
                                deltaX > 50 && selectedChipIndex > 0 -> {
                                    selectedChipIndex--
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                deltaX < -50 && selectedChipIndex < chipItems.size - 1 -> {
                                    selectedChipIndex++
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            }
                            true
                        }
                        else -> true
                    }
                }
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Your main content here
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
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

//                    // Overlay Text
//                    Text(
//                        text = "Overlay Text",
//                        modifier = Modifier
//                            .absoluteOffset(x = 0.dp, y = 0.dp)
//                            .zIndex(1f),
//                        style = MaterialTheme.typography.bodySmall.copy(
//                            color = Color.Black
//                        )
//                    )
                }
            }
        }
    }

    if (showEventPicker) {
        AlertDialog(
            onDismissRequest = { showEventPicker = false },
            title = { Text("Select Event") },
            text = {
                Column {
                    userData?.events?.entries?.forEach { event ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedEvent = event
                                    tokenManager.saveSelectedEventId(event.key)  // Save the selection
                                    showEventPicker = false
                                }
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(event.value.title)
                            if (event.key == selectedEvent?.key) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    } ?: Text("No events available")
                }
            },
            confirmButton = {
                TextButton(onClick = { showEventPicker = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Account?") },
            text = { 
                Text(
                    "This action cannot be undone. All your data, including events, " +
                    "tasks, and profile information will be permanently deleted."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            isDeletingAccount = true
                            try {
                                tokenManager.deleteAccount()
                                // On success, clear local data and navigate to onboarding
                                tokenManager.deleteToken()
                                tokenManager.clearSelectedEventId()
                                navController.navigate("onboarding") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } catch (e: Exception) {
                                // TODO: Show error to user
                                println("Delete account error: ${e.message}")
                            } finally {
                                isDeletingAccount = false
                                showDeleteConfirmation = false
                            }
                        }
                    },
                    enabled = !isDeletingAccount,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Text("Delete Account")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false },
                    enabled = !isDeletingAccount
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Add these helper functions outside the HomeScreen composable
private suspend fun createTempFileFromUri(context: Context, uri: Uri): File = withContext(Dispatchers.IO) {
    val stream = context.contentResolver.openInputStream(uri)
    val file = File.createTempFile("profile_picture", ".jpg", context.cacheDir)
    stream?.use { input ->
        file.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    file
}

private suspend fun uploadProfilePicture(file: File, token: String): String = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    
    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("token", token)
        .addFormDataPart(
            "profilePicture",
            file.name,
            file.asRequestBody("image/*".toMediaType())
        )
        .build()

    val request = Request.Builder()
        .url("https://serenidad.click/hacktime/changeProfilePicture")
        .post(requestBody)
        .build()

    val response = client.newCall(request).execute()
    if (!response.isSuccessful) {
        throw Exception("Unexpected code ${response.code}")
    }

    response.body?.string() ?: throw Exception("Empty response")
}

@Composable
fun Chip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val textColor = if (isSelected) Color.White else Color.Gray.copy(alpha = 0.6f)
    
    // Animate the color changes
    val animatedColor = animateColorAsState(
        targetValue = textColor,
        animationSpec = tween(durationMillis = 300)
    )

    Surface(
        modifier = Modifier
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = if (isSelected) Color.Black else Color.Transparent,
        border = if (!isSelected) BorderStroke(1.dp, textColor) else null
    ) {
        Text(
            text = text,
            color = animatedColor.value,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
} 
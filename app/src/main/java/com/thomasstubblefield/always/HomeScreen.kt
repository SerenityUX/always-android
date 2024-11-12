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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }
    var userData: AuthResponse? by remember { mutableStateOf(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Authenticate when the screen loads
    LaunchedEffect(Unit) {
        tokenManager.authenticate()
            .onSuccess { response ->
                userData = response
            }
            .onFailure {
                // If auth fails, navigate back to onboarding
                navController.navigate("onboarding") {
                    popUpTo(0) { inclusive = true }
                }
            }
        isLoading = false
    }

    Scaffold(
        topBar = {
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
                                text = { Text("Swap Events") },
                                onClick = {
                                    // TODO: Implement event swapping
                                    showMenu = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Update Avatar") },
                                onClick = {
                                    // TODO: Implement avatar update
                                    showMenu = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Logout") },
                                onClick = {
                                    tokenManager.deleteToken()
                                    navController.navigate("onboarding") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                    showMenu = false
                                }
                            )

                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "Delete Account",
                                        color = Color.Red
                                    )
                                },
                                onClick = {
                                    // TODO: Implement account deletion
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Text (
                    text="schedule here"
                )
                // userData?.let { data ->
                //     // Display user data and events
                //     Text(
                //         text = "Welcome, ${data.name}",
                //         style = MaterialTheme.typography.headlineMedium,
                //         modifier = Modifier.padding(16.dp)
                //     )
                    
                //     // Add your events list here
                // }
            }
        }
    }
} 
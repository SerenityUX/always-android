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
import com.thomasstubblefield.always.api.AuthResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }
    var userData by remember { mutableStateOf<AuthResponse?>(null) }
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
                title = { Text("Always") },
                actions = {
                    // Profile picture and menu
                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                userData?.profile_picture_url?.let { url ->
                                    SubcomposeAsyncImage(
                                        model = url,
                                        contentDescription = "Profile picture",
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                        loading = {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp
                                            )
                                        },
                                        error = {
                                            // Fallback icon or initial
                                            Surface(
                                                modifier = Modifier.size(32.dp),
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primary
                                            ) {
                                                Text(
                                                    text = userData?.name?.firstOrNull()?.uppercase() ?: "?",
                                                    modifier = Modifier.wrapContentSize(),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    )
                                } ?: run {
                                    // No profile picture URL, show initial
                                    Surface(
                                        modifier = Modifier.size(32.dp),
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = userData?.name?.firstOrNull()?.uppercase() ?: "?",
                                            modifier = Modifier.wrapContentSize(),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
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
                userData?.let { data ->
                    // Display user data and events
                    Text(
                        text = "Welcome, ${data.name}",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                    
                    // Add your events list here
                }
            }
        }
    }
} 
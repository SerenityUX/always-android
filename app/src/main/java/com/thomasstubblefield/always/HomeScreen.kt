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
    var isUploadingPhoto by remember { mutableStateOf(false) }

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
                                enabled = !isUploadingPhoto  // Disable the button while uploading
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
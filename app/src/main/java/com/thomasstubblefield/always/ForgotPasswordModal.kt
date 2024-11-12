package com.thomasstubblefield.always

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ForgotPasswordModal(
    onDismiss: () -> Unit,
    email: String
) {
    var step by remember { mutableStateOf(1) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentEmail by remember { mutableStateOf(email) }
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }

                Text(
                    text = if (step == 1) "Reset Password" else "Enter Code",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                if (step == 1) {
                    OutlinedTextField(
                        value = currentEmail,
                        onValueChange = { currentEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                isLoading = true
                                errorMessage = null
                                try {
                                    val response = requestPasswordReset(currentEmail)
                                    withContext(Dispatchers.Main) {
                                        if (response) {
                                            step = 2
                                        } else {
                                            errorMessage = "Failed to send reset code. Please verify your email."
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        errorMessage = "Failed to send reset code. Please try again."
                                    }
                                }
                                withContext(Dispatchers.Main) {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && currentEmail.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Send Reset Code")
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Enter Code") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                try {
                                    val response = changePassword(currentEmail, code, newPassword)
                                    if (response) {
                                        onDismiss()
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Failed to reset password. Please try again."
                                }
                                isLoading = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && code.isNotBlank() && newPassword.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Reset Password")
                        }
                    }
                }
            }
        }
    }
}

private suspend fun requestPasswordReset(email: String): Boolean {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val json = Json { ignoreUnknownKeys = true }
        
        println("Attempting password reset for email: $email")
        
        val requestBody = json.encodeToString(mapOf("email" to email))
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://serenidad.click/hacktime/forgotPasswordRequest")
            .post(requestBody)
            .build()

        println("Sending request to: ${request.url}")
        println("Request body: $requestBody")

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            println("Response code: ${response.code}")
            println("Response body: $responseBody")
            response.isSuccessful
        } catch (e: Exception) {
            println("Error during password reset request: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}

private suspend fun changePassword(email: String, code: String, newPassword: String): Boolean {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val json = Json { ignoreUnknownKeys = true }
        
        println("Attempting to change password for email: $email with code: $code")
        
        val requestBody = json.encodeToString(
            mapOf(
                "email" to email,
                "oneTimeCode" to code,
                "newPassword" to newPassword
            )
        ).toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://serenidad.click/hacktime/changePassword")
            .post(requestBody)
            .build()

        println("Sending request to: ${request.url}")
        println("Request body: $requestBody")

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            println("Response code: ${response.code}")
            println("Response body: $responseBody")
            response.isSuccessful
        } catch (e: Exception) {
            println("Error during password change: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
} 
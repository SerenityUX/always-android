package com.thomasstubblefield.always

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.serialization.encodeToString

@Serializable
data class AuthResponse(
    val email: String,
    val name: String,
    val profile_picture_url: String?,
    val token: String,
    val events: Map<String, Event>
)

@Serializable
data class Event(
    val id: String,
    val title: String,
    val owner: String,
    val startTime: String,
    val endTime: String,
    val calendar_events: List<CalendarEvent>,
    val teamMembers: List<TeamMember>,
    val tasks: List<Task>,
    val announcements: List<Announcement>
)

@Serializable
data class CalendarEvent(
    val id: String,
    val title: String,
    val startTime: String,
    val endTime: String,
    val color: String
)

@Serializable
data class TeamMember(
    val name: String,
    val profilePicture: String?,
    val email: String,
    val roleDescription: String
)

@Serializable
data class Task(
    val id: String,
    val title: String,
    val description: String,
    val startTime: String,
    val endTime: String,
    val assignedTo: List<TeamMember>?
)

@Serializable
data class Announcement(
    val id: String,
    val sender: TeamMember,
    val timeSent: String,
    val content: String
)

class TokenManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("TokenPrefs", Context.MODE_PRIVATE)
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    private val mediaType = "application/json; charset=utf-8".toMediaType()
    private val baseUrl = "https://serenidad.click/hacktime"
    
    fun saveToken(token: String) {
        prefs.edit().putString("user_token", token).apply()
    }
    
    fun getToken(): String? {
        return prefs.getString("user_token", null)
    }
    
    fun deleteToken() {
        prefs.edit().remove("user_token").apply()
    }

    suspend fun login(email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val requestBody = json.encodeToString(
                mapOf(
                    "email" to email,
                    "password" to password
                )
            ).toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$baseUrl/login")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val loginResponse = json.decodeFromString<Map<String, String>>(responseBody)
                saveToken(loginResponse["token"] ?: throw Exception("No token in response"))
                Result.success(Unit)
            } else {
                Result.failure(Exception(responseBody ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun authenticate(): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val token = getToken() ?: return@withContext Result.failure(Exception("No token found"))
            
            val requestBody = json.encodeToString(
                mapOf("token" to token)
            ).toRequestBody(mediaType)

            val request =
                Request.Builder()
                    .url("$baseUrl/authenticate")
                    .post(requestBody)
                    .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val authResponse = json.decodeFromString<AuthResponse>(responseBody)
                Result.success(authResponse)
            } else {
                Result.failure(Exception(responseBody ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 
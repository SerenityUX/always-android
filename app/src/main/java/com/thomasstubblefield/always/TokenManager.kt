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
    val profilePicture: String? = null,
    val email: String,
    val roleDescription: String? = null
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
    private val json = Json { 
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }
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
            println("Attempting to authenticate with token: $token")
            
            val requestBody = json.encodeToString(
                mapOf("token" to token)
            ).toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$baseUrl/auth")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            println("Auth response code: ${response.code}")
            println("Auth response body: $responseBody")

            if (response.isSuccessful && responseBody != null) {
                try {
                    val authResponse = json.decodeFromString<AuthResponse>(responseBody)
                    println("Successfully parsed auth response")
                    Result.success(authResponse)
                } catch (e: Exception) {
                    println("JSON parsing error: ${e.message}")
                    println("Use 'coerceInputValues = true' in 'Json {}' builder to coerce nulls to default values.")
                    println("JSON input: ${responseBody.take(100)}.....")
                    Result.failure(e)
                }
            } else {
                Result.failure(Exception(responseBody ?: "Unknown error"))
            }
        } catch (e: Exception) {
            println("Auth error: ${e.message}")
            Result.failure(e)
        }
    }

    fun saveSelectedEventId(eventId: String) {
        prefs.edit().putString("selected_event_id", eventId).apply()
    }
    
    fun getSelectedEventId(): String? {
        return prefs.getString("selected_event_id", null)
    }
    
    fun clearSelectedEventId() {
        prefs.edit().remove("selected_event_id").apply()
    }

    suspend fun signup(email: String, password: String, name: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val requestBody = json.encodeToString(
                mapOf(
                    "email" to email,
                    "password" to password,
                    "name" to name
                )
            ).toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$baseUrl/signup")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val signupResponse = json.decodeFromString<Map<String, String>>(responseBody)
                saveToken(signupResponse["token"] ?: throw Exception("No token in response"))
                Result.success(Unit)
            } else {
                Result.failure(Exception(responseBody ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = getToken() ?: return@withContext Result.failure(Exception("No token found"))
            
            val requestBody = json.encodeToString(
                mapOf("token" to token)
            ).toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$baseUrl/deleteAccount")
                .delete(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(responseBody ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 
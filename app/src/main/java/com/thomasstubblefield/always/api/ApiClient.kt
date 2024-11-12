package com.thomasstubblefield.always.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.serialization.encodeToString

private const val BASE_URL = "https://serenidad.click/hacktime"

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String
)

@Serializable
data class AuthRequest(
    val token: String
)

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

class ApiClient {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun login(email: String, password: String): Result<LoginResponse> = try {
        val requestBody = json.encodeToString(LoginRequest(email, password))
            .toRequestBody(mediaType)

        val request = Request.Builder()
            .url("$BASE_URL/login")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()

        if (response.isSuccessful && responseBody != null) {
            Result.success(json.decodeFromString(responseBody))
        } else {
            Result.failure(Exception(responseBody ?: "Unknown error"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun authenticate(token: String): Result<AuthResponse> = try {
        val requestBody = json.encodeToString(AuthRequest(token))
            .toRequestBody(mediaType)

        val request = Request.Builder()
            .url("$BASE_URL/auth")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()

        if (response.isSuccessful && responseBody != null) {
            Result.success(json.decodeFromString(responseBody))
        } else {
            Result.failure(Exception(responseBody ?: "Unknown error"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
} 
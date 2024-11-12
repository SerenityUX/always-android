package com.thomasstubblefield.always

class TokenManager(context: android.content.Context) {
    private val prefs = context.getSharedPreferences("TokenPrefs", android.content.Context.MODE_PRIVATE)
    
    fun saveToken(token: String) {
        prefs.edit().putString("user_token", token).apply()
    }
    
    fun getToken(): String? {
        return prefs.getString("user_token", null)
    }
    
    fun deleteToken() {
        prefs.edit().remove("user_token").apply()
    }
} 
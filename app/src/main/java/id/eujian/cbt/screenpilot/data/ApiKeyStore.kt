package id.eujian.cbt.screenpilot.data

import android.content.Context

interface ApiKeyStore {
    fun getGeminiApiKey(context: Context): String
    fun storeGeminiApiKey(context: Context, apiKey: String): Result<Unit>
    fun clearGeminiApiKey(context: Context)
}


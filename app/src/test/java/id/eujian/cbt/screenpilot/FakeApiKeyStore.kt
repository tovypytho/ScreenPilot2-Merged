package id.eujian.cbt.screenpilot

import android.content.Context
import id.eujian.cbt.screenpilot.data.ApiKeyStore

class FakeApiKeyStore : ApiKeyStore {
    private var geminiKey: String = ""

    override fun getGeminiApiKey(context: Context): String = geminiKey

    override fun storeGeminiApiKey(context: Context, apiKey: String): Result<Unit> {
        geminiKey = apiKey
        return Result.success(Unit)
    }

    override fun clearGeminiApiKey(context: Context) {
        geminiKey = ""
    }
}

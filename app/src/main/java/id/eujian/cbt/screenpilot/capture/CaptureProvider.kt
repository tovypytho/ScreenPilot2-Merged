package id.eujian.cbt.screenpilot.capture

import android.graphics.Bitmap

sealed class CaptureResult {
    data class Success(val bitmap: Bitmap) : CaptureResult()
    object Denied : CaptureResult()
    data class Error(val message: String) : CaptureResult()
}

interface CaptureProvider {
    suspend fun capture(): CaptureResult
}

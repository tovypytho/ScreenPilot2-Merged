package id.eujian.cbt.screenpilot.capture

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FakeCaptureProvider(
    private val width: Int = 100,
    private val height: Int = 100,
    private val color: Int = Color.LTGRAY
) : CaptureProvider {

    override suspend fun capture(): CaptureResult = withContext(Dispatchers.Default) {
        try {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(color)
            CaptureResult.Success(bitmap)
        } catch (e: Exception) {
            CaptureResult.Error(e.message ?: "Unknown capture error")
        }
    }
}
